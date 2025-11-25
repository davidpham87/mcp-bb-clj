(ns mcp-bb-clj.mcp.spec-test
  (:require [clojure.test :refer [deftest is]]
            [mcp-bb-clj.mcp.spec :as spec]
            [malli.generator :as mg]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [babashka.json :as json]))

(defspec json-rpc-message-roundtrip 100
  (prop/for-all [message (mg/generator spec/Message)]
    (let [json-string (json/write-str message)
          parsed-message (json/read-str json-string {:key-fn keyword})]
      (is (= message parsed-message)))))
