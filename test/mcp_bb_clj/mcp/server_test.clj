(ns mcp-bb-clj.mcp.server-test
  (:require [clojure.test :refer [deftest is]]
            [mcp-bb-clj.mcp.server :as server]
            [mcp-bb-clj.mcp.json-rpc :as rpc]))

(def test-tool
  {:name "test-tool"
   :implementation (fn [args] {:result (:foo args)})})

(deftest handle-request-test
  (let [s (server/create-server)]
    (server/add-tool! s test-tool)

    (is (= {:jsonrpc "2.0", :id 1, :result {:protocolVersion "2025-06-18", :serverInfo {:name "mcp-bb-clj", :version "0.0.1"}, :capabilities {:tools {:listChanged false}}}}
           (server/handle-request (rpc/request 1 "initialize" {}) s)))

    (is (= {:jsonrpc "2.0", :id 3, :result {:result "bar"}}
           (server/handle-request (rpc/request 3 "tools/call" {:name "test-tool" :arguments {:foo "bar"}}) s)))

    (is (= {:jsonrpc "2.0", :id 4, :error {:code -32601, :message "Method not found: non-existent-method"}}
           (server/handle-request (rpc/request 4 "non-existent-method" {}) s)))))
