(ns mcp-bb-clj.core-test
  (:require [clojure.test :refer [deftest is]]
            [mcp-bb-clj.core :as core]
            [mcp-bb-clj.mcp.server :as server]
            [mcp-bb-clj.mcp.json-rpc :as rpc]))

(deftest app-test
  (let [s (server/create-server)
        handler (core/app s)
        req-body (rpc/generate (rpc/request 1 "initialize" {}))
        req {:body (java.io.ByteArrayInputStream. (.getBytes req-body "UTF-8"))}
        response (handler req)]
    (is (= 200 (:status response)))
    (let [resp-body (rpc/parse (:body response))]
      (is (= "2.0" (:jsonrpc resp-body)))
      (is (= 1 (:id resp-body))))))
