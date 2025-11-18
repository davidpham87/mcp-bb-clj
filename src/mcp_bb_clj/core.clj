(ns mcp-bb-clj.core
  (:require [org.httpkit.server :as server]
            [mcp-bb-clj.mcp.server :as mcp-server]
            [mcp-bb-clj.mcp.json-rpc :as rpc]
            [mcp-bb-clj.malli-tools :as malli-tools]))

(defn app
  "The http-kit request handler. It processes MCP requests."
  [server-atom]
  (fn [req]
    (try
      (let [body-str (slurp (:body req))
            mcp-req (rpc/parse body-str)
            mcp-resp (mcp-server/handle-message mcp-req server-atom)]
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (rpc/generate mcp-resp)})
      (catch Exception e
        {:status 400
         :headers {"Content-Type" "application/json"}
         :body (rpc/generate
                (rpc/error-response nil {:code -32700
                                         :message (str "Parse error: " (.getMessage e))}))}))))

(def echo-tool
  {:name "echo"
   :description "Echoes the input text"
   :inputSchema {:type "object"
                 :properties {"text" {:type "string"}}
                 :required ["text"]}
   :implementation (fn [{:keys [text]}]
                     {:content [{:type "text" :text text}]
                      :isError false})})

(defn -main [& args]
  (let [port (Integer/parseInt (or (first args) "8080"))
        mcp-server (mcp-server/create-server)]
    (mcp-server/add-tool! mcp-server
                          echo-tool
                          malli-tools/validate-schema-tool
                          malli-tools/generate-sample-tool
                          malli-tools/infer-schema-tool)
    (server/run-server (app mcp-server) {:port port})
    (println (str "server running at http://127.0.0.1:" port))))
