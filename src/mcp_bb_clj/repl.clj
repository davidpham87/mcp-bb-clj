(ns mcp-bb-clj.repl
  (:require [nrepl.server :as nrepl]
            [cider.nrepl :as cider-nrepl]
            [babashka.cli :as cli]))

(defn -main [& args]
  (let [opts (cli/parse-opts args {:spec {:port {:alias :p :coerce :long :default 7888}
                                           :host {:alias :H :coerce :string :default "127.0.0.1"}}})
        {:keys [host port]} opts]
    (println (str "Starting nREPL server on " host ":" port))
    (nrepl/start-server :port port :bind host :handler cider-nrepl/cider-nrepl-handler)
    @(promise)))
