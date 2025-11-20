(ns mcp-bb-clj.client
  (:require [org.httpkit.client :as http]
            [mcp-bb-clj.mcp.client :as mcp-client]
            [mcp-bb-clj.mcp.json-rpc :as rpc]))

(defn -main [& args]
  (let [url (or (first args) "http://127.0.0.1:8080")
        client-info {:name "mcp-bb-clj-client" :version "0.0.1"}
        client-atom (atom nil)

        ;; Define the function that sends messages to the server and handles responses.
        send-fn (fn [json-rpc-str]
                  (http/post url {:body json-rpc-str}
                             (fn [{:keys [body error]}]
                               (if error
                                 (println "HTTP Error:" error)
                                 (try
                                   (let [message (rpc/parse body)]
                                     (mcp-client/handle-message @client-atom message))
                                   (catch Exception e
                                     (println "Error parsing server response:" e)))))))

        ;; Create the client with the send function.
        _ (reset! client-atom (mcp-client/create-client send-fn {:client-info client-info}))]

    ;; Perform the client operations
    (println "Initializing...")
    (let [init-response @(mcp-client/initialize! @client-atom)]
      (println "Server response to initialize:" init-response))

    (println "\nListing tools...")
    (let [tools-response @(mcp-client/list-tools @client-atom)]
      (println "Server response to tools/list:" tools-response))

    (println "\nCalling 'echo' tool...")
    (let [echo-response @(mcp-client/call-tool @client-atom "echo" {:text "Hello from client!"})]
      (println "Server response to tools/call (echo):" echo-response))

    (println "\nListing resources...")
    (let [resources-response @(mcp-client/list-resources @client-atom)]
      (println "Server response to resources/list:" resources-response))

    (println "\nReading resource 'test://resource'...")
    (let [read-response @(mcp-client/read-resource @client-atom "test://resource")]
      (println "Server response to resources/read:" read-response))

    ;; Keep the process alive for a moment to receive async responses.
    (Thread/sleep 1000)
    (println "\nClient finished.")))
