(ns mcp-bb-clj.mcp.server
  (:require [mcp-bb-clj.mcp.json-rpc :as rpc]))

;;; Server state
(def initial-state
  {:tools {}
   :prompts {}
   :protocol-version "2025-06-18"
   :server-info {:name "mcp-bb-clj" :version "0.0.1"}
   :capabilities {:tools {:list-changed false}
                  :prompts {:list-changed false}}})

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
     {:protocol-version (:protocol-version state)
      :server-info (:server-info state)
      :capabilities (:capabilities state)})))

(defmethod handle-request "tools/list"
  [{:keys [id] :as request} server-atom]
  (let [tools (vals (:tools @server-atom))]
    (rpc/success-response
     id
     {:tools (map #(select-keys % [:name :description :input-schema]) tools)})))

(defmethod handle-request "tools/call"
  [{:keys [id params] :as request} server-atom]
  (let [tool-name (:name params)
        tool-impl (get-in @server-atom [:tools tool-name :implementation])]
    (if tool-impl
      (let [result (tool-impl (:arguments params))]
        (rpc/success-response id result))
      (rpc/error-response id {:code -32601 :message "Method not found"}))))

(defmethod handle-request "prompts/list"
  [{:keys [id] :as request} server-atom]
  (let [prompts (vals (:prompts @server-atom))]
    (rpc/success-response
     id
     {:prompts (map #(select-keys % [:name :description :arguments]) prompts)})))

(defmethod handle-request "prompts/get"
  [{:keys [id params] :as request} server-atom]
  (let [prompt-name (:name params)
        prompt (get-in @server-atom [:prompts prompt-name])]
    (if prompt
      (let [result ((:prompt-fn prompt) (:arguments params))]
        (rpc/success-response id {:messages [{:role "user"
                                              :content {:type "text"
                                                        :text result}}]}))
      (rpc/error-response id {:code -32601 :message "Prompt not found"}))))


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

(defn add-prompt!
  "Adds a prompt to the server."
  [server-atom prompt]
  (swap! server-atom update-in [:prompts] assoc (:name prompt) prompt))
