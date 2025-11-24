(ns mcp-bb-clj.mcp.prompts-test
  (:require [clojure.test :refer [deftest is testing]]
            [mcp-bb-clj.mcp.prompts :as prompts]))

(deftest valid-prompt?-test
  (testing "valid prompt"
    (is (prompts/valid-prompt? {:name "test" :messages []})))
  (testing "invalid prompt"
    (is (nil? (prompts/valid-prompt? {:name "" :messages []})))
    (is (nil? (prompts/valid-prompt? {:name "test" :messages nil})))))

(deftest apply-template-test
  (let [msg {:content {:type "text" :text "Hello {{name}}"}}
        args {:name "World"}]
    (is (= "Hello World"
           (get-in (#'prompts/apply-template msg args) [:content :text])))))

(deftest list-prompts-test
  (let [prompts {"test" {:name "test" :description "desc" :messages [] :arguments []}}
        result (prompts/list-prompts prompts nil)]
    (is (= {:prompts [{:name "test" :description "desc" :arguments []}]}
           result))))

(deftest get-prompt-test
  (let [prompts {"test" {:name "test"
                         :messages [{:role "user" :content {:type "text" :text "Hi {{name}}"}}]}
                 "repl" prompts/repl-prompt}]
    (testing "get existing prompt without args"
      (let [result (prompts/get-prompt prompts {:name "test"})]
        (is (= [{:role "user" :content {:type "text" :text "Hi {{name}}"}}
]
               (:messages result)))))

    (testing "get existing prompt with args"
      (let [result (prompts/get-prompt prompts {:name "test" :arguments {:name "Jules"}})]
        (is (= [{:role "user" :content {:type "text" :text "Hi Jules"}}]
               (:messages result)))))

    (testing "get existing prompt with non-string args"
      (let [result (prompts/get-prompt prompts {:name "test" :arguments {:name 123}})]
        (is (= [{:role "user" :content {:type "text" :text "Hi 123"}}]
               (:messages result)))))

    (testing "get non-existing prompt"
      (let [result (prompts/get-prompt prompts {:name "other"})]
        (is (:isError result))))))
