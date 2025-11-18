(ns mcp-bb-clj.prompts
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def prompts-dir "prompts")

(defn get-prompt
  "Loads a prompt from a file."
  [prompt-name]
  (let [prompt-file (io/file prompts-dir (str prompt-name ".edn"))]
    (when (.exists prompt-file)
      (edn/read-string (slurp prompt-file)))))

(defn save-prompt!
  "Saves a prompt to a file."
  [prompt-name content]
  (.mkdirs (io/file prompts-dir))
  (let [prompt-file (io/file prompts-dir (str prompt-name ".edn"))]
    (spit prompt-file (pr-str content))))
