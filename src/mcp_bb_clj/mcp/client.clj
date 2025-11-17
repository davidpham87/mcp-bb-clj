(ns mcp-bb-clj.mcp.client
  (:require [mcp-bb-clj.mcp.json-rpc :as rpc]))

;;; Client state
(defn create-client
  "Creates a new client instance with an initial state.
   `send-fn` is a function that takes a JSON-RPC string and sends it to the server.
   `opts` is a map of options, including `:client-info`."
  [send-fn opts]
  (atom {:send-fn send-fn
         :request-id 0
         :pending-requests {} ; map of id -> promise
         :server-info nil
         :server-capabilities nil
         :client-info (:client-info opts)
         :protocol-version "2025-06-18"}))

;;; Private helpers
(defn- call-remote
  "Sends a request to the server and returns a promise for the result."
  [client-atom method params]
  (let [id (swap! client-atom update :request-id inc)
        req (rpc/request (:request-id @client-atom) method params)
        p (promise)]
    (swap! client-atom assoc-in [:pending-requests (:id req)] p)
    ((:send-fn @client-atom) (rpc/generate req))
    p))

(defn- notify-remote
  "Sends a notification to the server."
  [client-atom method params]
  (let [notif (rpc/notification method params)]
    ((:send-fn @client-atom) (rpc/generate notif))))

;;; Public API
(defn initialize!
  "Sends an initialize request to the server."
  [client-atom]
  (let [params {:protocolVersion (:protocol-version @client-atom)
                :clientInfo (:client-info @client-atom)
                :capabilities {}}]
    (call-remote client-atom "initialize" params)))

(defn list-tools
  "Sends a tools/list request to the server."
  [client-atom]
  (call-remote client-atom "tools/list" {}))

(defn call-tool
  "Sends a tools/call request to the server."
  [client-atom tool-name arguments]
  (call-remote client-atom "tools/call" {:name tool-name :arguments arguments}))

;;; Message Handling
(defn- handle-response
  [client-atom {:keys [id result error]}]
  (let [pending-req-p (get-in @client-atom [:pending-requests id])]
    (when pending-req-p
      (if error
        (deliver pending-req-p {:error error})
        (deliver pending-req-p result))
      (swap! client-atom update :pending-requests dissoc id))))

(defn handle-message
  "Handles a parsed JSON-RPC message from the server."
  [client-atom message]
  (cond
    (or (:result message) (:error message)) (handle-response client-atom message)
    (:method message) (println "Received notification:" (:method message)) ; Placeholder for notification handling
    :else (println "Received unknown message from server:" message)))
