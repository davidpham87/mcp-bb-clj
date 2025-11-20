(ns mcp-bb-clj.malli-tools-test
  (:require [clojure.test :refer [deftest is]]
            [mcp-bb-clj.malli-tools :as malli-tools]
            [malli.core :as m]))

(deftest validate-schema-tool-test
  (let [impl (:implementation malli-tools/validate-schema-tool)]
    (is (= {:content [{:type "text", :text "Validation successful."}]
            :structured-content {:valid true, :explanation nil}
            :is-error false}
           (impl {:schema :int :data 1})))
    (let [result (impl {:schema :int :data "a"})]
      (is (= false (get-in result [:structured-content :valid]))))))

(deftest generate-sample-tool-test
  (let [impl (:implementation malli-tools/generate-sample-tool)]
    (let [result (impl {:schema :int})]
      (is (m/validate :int (get-in result [:structured-content :sample]))))))

(deftest infer-schema-tool-test
  (let [impl (:implementation malli-tools/infer-schema-tool)]
    (let [result (impl {:data [1 2 3]})]
      (is (m/validate (get-in result [:structured-content :schema]) 4)))))
