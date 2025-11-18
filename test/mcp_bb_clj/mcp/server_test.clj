(ns mcp-bb-clj.mcp.server-test
  (:require [clojure.test :refer [deftest is]]
            [mcp-bb-clj.mcp.server :as server]
            [mcp-bb-clj.mcp.json-rpc :as rpc]))

(def test-tool
  {:name "test-tool"
   :description "A test tool"
   :input-schema {:type "object"}
   :implementation (fn [args] {:result (:foo args)})})

(def test-prompt
  {:name "test-prompt"
   :description "A test prompt"
   :arguments []
   :prompt-fn (fn [args] (str "Hello, " (:name args)))})

(deftest handle-request-test
  (let [s (server/create-server)]
    (server/add-tool! s test-tool)
    (server/add-prompt! s test-prompt)

    (is (= {:jsonrpc "2.0"
            :id 1
            :result {:protocol-version "2025-06-18"
                     :server-info {:name "mcp-bb-clj"
                                   :version "0.0.1"}
                     :capabilities {:tools {:list-changed false}
                                    :prompts {:list-changed false}}}}
           (server/handle-request (rpc/request 1 "initialize" {}) s)))

    (is (= {:jsonrpc "2.0"
            :id 2
            :result {:tools [{:name "test-tool"
                              :description "A test tool"
                              :input-schema {:type "object"}}]}}
           (server/handle-request (rpc/request 2 "tools/list" {}) s)))

    (is (= {:jsonrpc "2.0", :id 3, :result {:result "bar"}}
           (server/handle-request (rpc/request 3 "tools/call" {:name "test-tool" :arguments {:foo "bar"}}) s)))

    (is (= {:jsonrpc "2.0", :id 4, :error {:code -32601, :message "Method not found: non-existent-method"}}
           (server/handle-request (rpc/request 4 "non-existent-method" {}) s)))

    (is (= {:jsonrpc "2.0"
            :id 5
            :result {:prompts [{:name "test-prompt"
                                :description "A test prompt"
                                :arguments []}]}}
           (server/handle-request (rpc/request 5 "prompts/list" {}) s)))

    (is (= {:jsonrpc "2.0"
            :id 6
            :result {:messages [{:role "user"
                                 :content {:type "text"
                                           :text "Hello, world"}}]}}
           (server/handle-request (rpc/request 6 "prompts/get" {:name "test-prompt" :arguments {:name "world"}}) s)))))
