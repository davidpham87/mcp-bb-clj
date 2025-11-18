(ns mcp-bb-clj.eval
  (:require [babashka.cli :as cli]))

(defn -main [& args]
  (let [opts (cli/parse-opts args {:spec {:code {:alias :c :coerce :string}}})
        {:keys [code]} opts]
    (when code
      (prn (eval (read-string code))))))
