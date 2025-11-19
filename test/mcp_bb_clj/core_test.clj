(ns mcp-bb-clj.core-test
  (:require [clojure.test :refer [deftest is]]
            [mcp-bb-clj.core :as core]
            [mcp-bb-clj.mcp.server :as server]
            [mcp-bb-clj.mcp.json-rpc :as rpc]
            [babashka.json :as json]))

(deftest app-test
  (let [server-atom (server/create-server)
        handler (core/app server-atom)
        req {:body (java.io.ByteArrayInputStream. (.getBytes (json/write-str (rpc/request 1 "initialize" {}))))}
        response (handler req)]
    (is (= 200 (:status response)))
    (is (= "application/json" (get-in response [:headers "Content-Type"])))
    (let [body (json/read-str (:body response) {:key-fn keyword})]
      (is (= "2.0" (:jsonrpc body)))
      (is (= 1 (:id body))))))
