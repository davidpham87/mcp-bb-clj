(ns mcp-bb-clj.tools-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [mcp-bb-clj.tools :as tools]
            [mcp-bb-clj.prompts :as prompts]
            [clojure.java.io :as io]))

(defn cleanup-prompts [f]
  (let [dir (io/file "prompts")]
    (when (.exists dir)
      (doseq [file (.listFiles dir)]
        (.delete file))
      (.delete dir)))
  (f)
  (let [dir (io/file "prompts")]
    (when (.exists dir)
      (doseq [file (.listFiles dir)]
        (.delete file))
      (.delete dir))))

(use-fixtures :each cleanup-prompts)

(deftest create-prompt-tool-test
  (testing "create-prompt-tool creates a valid tool with implementation"
    (let [tool-def {:name "test-tool"
                    :description "A test tool"
                    :inputSchema {}
                    :prompt-fn (fn [params] (str "Hello " (:name params)))}
          tool (prompts/create-prompt-tool tool-def)]
      (is (= "test-tool" (:name tool)))
      (is (fn? (:implementation tool)))
      (is (= {:content [{:type "text" :text "Hello World"}]}
             ((:implementation tool) {:name "World"}))))))

(deftest load-prompt-tool-test
  (testing "load-prompt-tool loads an existing prompt"
    (let [prompt-name "test-prompt"
          prompt-content {:messages [{:role "user" :content {:type "text" :text "Hello"}}]}]
      ;; Save prompt first
      (prompts/save-prompt! prompt-name prompt-content)

      ;; Test load-prompt-tool
      (let [tool-impl (:implementation tools/load-prompt-tool)
            result (tool-impl {:name prompt-name})]
        (is (= prompt-content
               (get-in result [:structuredContent :prompt])))))))

(deftest clj-kondo-tool-test
  (testing "clj-kondo-tool lints code"
    (let [tool-impl (:implementation tools/clj-kondo-tool)
          code "(def x 1) (def x 2)"
          result (tool-impl {:code code})]
      (is (not (:isError result)))
      (is (some? (:structured-content result)))
      (let [findings (:findings (:structured-content result))]
        (is (= 1 (count findings)))
        (is (= "redefined var #'user/x" (:message (first findings))))
        (is (= "input" (:filename (first findings))))))))
