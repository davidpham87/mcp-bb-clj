(ns mcp-bb-clj.core
  (:require [org.httpkit.server :as server]))

(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello http-kit"})

(defn -main [& args]
  (let [port (Integer/parseInt (or (first args) "8080"))]
    (server/run-server app {:port port})
    (println (str "server running at http://127.0.0.1:" port))))
