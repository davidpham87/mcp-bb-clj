(ns mcp-bb-clj.mcp.property-test
  (:require [clojure.test :refer [deftest is testing]]
            [mcp-bb-clj.mcp.spec :as spec]
            [mcp-bb-clj.mcp.json-rpc :as rpc]
            [malli.generator :as mg]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

(defspec roundtrip-test 100
  (prop/for-all [msg (mg/generator spec/Message)]
    (let [json (rpc/generate msg)
          parsed (rpc/parse json)]
      (= msg parsed))))
