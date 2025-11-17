(ns mcp-bb-clj.client
  (:require [org.httpkit.client :as http]))

(defn -main [& args]
  (let [url (or (first args) "http://127.0.0.1:8080")]
    (let [response @(http/get url)]
      (println (:body response)))))
