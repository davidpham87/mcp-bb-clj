(ns mcp-bb-clj.mcp.server
  (:require [mcp-bb-clj.mcp.json-rpc :as rpc]))

;;; Server state
(def initial-state
  {:tools {}
   :protocol-version "2025-06-18"
   :server-info {:name "mcp-bb-clj" :version "0.0.1"}
   :capabilities {:tools {:listChanged false}}})

(defn create-server
  "Creates a new server instance with an initial state."
  ([] (create-server {}))
  ([opts] (atom (merge initial-state opts))))

;;; Method handling
(defmulti handle-request :method)

(defmethod handle-request "initialize"
  [{:keys [id params] :as request} server-atom]
  (let [state @server-atom]
    (rpc/success-response
     id
     {:protocolVersion (:protocol-version state)
      :serverInfo (:server-info state)
      :capabilities (:capabilities state)})))

(defmethod handle-request "tools/list"
  [{:keys [id] :as request} server-atom]
  (let [tools (vals (:tools @server-atom))]
    (rpc/success-response
     id
     {:tools tools})))

(defmethod handle-request "tools/call"
  [{:keys [id params] :as request} server-atom]
  (let [tool-name (:name params)
        tool-impl (get-in @server-atom [:tools tool-name :implementation])]
    (if tool-impl
      (let [result (tool-impl (:arguments params))]
        (rpc/success-response id result))
      (rpc/error-response id {:code -32601 :message "Method not found"}))))

(defmethod handle-request :default
  [{:keys [id method] :as request} server-atom]
  (rpc/error-response id {:code -32601
                           :message (str "Method not found: " method)}))


(defn handle-message
  "Handles a parsed JSON-RPC message and returns a response map (or nil for notifications)."
  [message server-atom]
  (cond
    (:method message) (handle-request message server-atom)
    ;; For now, we don't handle responses from the client or notifications
    :else nil))

;;; Tool management
(defn add-tool!
  "Adds a tool to the server."
  [server-atom tool]
  (swap! server-atom update-in [:tools] assoc (:name tool) tool))
