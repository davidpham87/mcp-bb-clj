(ns mcp-bb-clj.mcp.json-rpc-test
  (:require [clojure.test :refer [deftest is]]
            [mcp-bb-clj.mcp.json-rpc :as rpc]))

(deftest request-test
  (is (= {:jsonrpc "2.0" :id 1 :method "test" :params {:foo "bar"}}
         (rpc/request 1 "test" {:foo "bar"}))))

(deftest notification-test
  (is (= {:jsonrpc "2.0" :method "test" :params {:foo "bar"}}
         (rpc/notification "test" {:foo "bar"}))))

(deftest success-response-test
  (is (= {:jsonrpc "2.0" :id 1 :result {:foo "bar"}}
         (rpc/success-response 1 {:foo "bar"}))))

(deftest error-response-test
  (is (= {:jsonrpc "2.0" :id 1 :error {:code 123 :message "error"}}
         (rpc/error-response 1 {:code 123 :message "error"}))))

(deftest parse-test
  (is (= {:jsonrpc "2.0" :id 1 :method "test" :params {:foo "bar"}}
         (rpc/parse "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"test\", \"params\": {\"foo\": \"bar\"}}"))))

(deftest generate-test
  (is (= "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"test\",\"params\":{\"foo\":\"bar\"}}"
         (rpc/generate {:jsonrpc "2.0" :id 1 :method "test" :params {:foo "bar"}}))))
