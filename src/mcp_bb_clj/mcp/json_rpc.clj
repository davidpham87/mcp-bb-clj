(ns mcp-bb-clj.mcp.json-rpc
  (:require [babashka.json :as json]
            [mcp-bb-clj.mcp.spec :as mcp-spec]
            [malli.core :as m]))

(defn request
  "Creates a JSON-RPC request map."
  [id method params]
  {:jsonrpc "2.0"
   :id id
   :method method
   :params params})

(defn notification
  "Creates a JSON-RPC notification map."
  [method params]
  {:jsonrpc "2.0"
   :method method
   :params params})

(defn success-response
  "Creates a JSON-RPC success response map."
  [id result]
  {:jsonrpc "2.0"
   :id id
   :result result})

(defn error-response
  "Creates a JSON-RPC error response map."
  [id error]
  {:jsonrpc "2.0"
   :id id
   :error error})

(defn parse
  "Parses a JSON string and validates it as a JSON-RPC message."
  [json-string]
  (let [data (json/read-str json-string {:key-fn keyword})]
    (if (m/validate mcp-spec/Message data)
      data
      (throw (ex-info "Invalid JSON-RPC message" {:data data
                                                  :explanation (m/explain mcp-spec/Message data)})))))

(defn generate
  "Generates a JSON-RPC message string."
  [message]
  (json/write-str message))
