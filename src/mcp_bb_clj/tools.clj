(ns mcp-bb-clj.tools
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [mcp-bb-clj.prompts :as prompts]
            [babashka.nrepl.server :as nrepl-server]))

(def start-repl-tool
  {:name "start-repl"
   :description "Starts a Babashka nREPL server."
   :input-schema {:type "object"
                  :properties {"host" {:type "string" :default "127.0.0.1"}
                               "port" {:type "integer" :default 7888}}
                  :required ["host" "port"]}
   :implementation (fn [{:keys [host port]}]
                     (try
                       (future (nrepl-server/start-server! {:host host :port port}))
                       {:content [{:type "text"
                                   :text (str "nREPL server started on " host ":" port)}]
                        :structured-content {:host host :port port}}
                       (catch Exception e
                         {:content [{:type "text"
                                     :text (str "Error starting nREPL server: " (.getMessage e))}]
                          :is-error true})))})

(def eval-tool
  {:name "eval"
   :description "Evaluates a string of Clojure code. WARNING: This tool is a security risk and should not be exposed to untrusted users."
   :input-schema {:type "object"
                  :properties {"code" {:type "string"}}
                  :required ["code"]}
   :implementation (fn [{:keys [code]}]
                     (try
                       (let [result (eval (read-string code))]
                         {:content [{:type "text"
                                     :text (str "Result: " (pr-str result))}]
                          :structured-content {:result result}})
                       (catch Exception e
                         {:content [{:type "text"
                                     :text (str "Error evaluating code: " (.getMessage e))}]
                          :is-error true})))})
