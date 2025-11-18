(ns mcp-bb-clj.prompts
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def prompts-dir "prompts")

(defn load-prompt [prompt-name]
  (let [prompt-file (io/file prompts-dir (str prompt-name ".edn"))]
    (when (.exists prompt-file)
      (edn/read-string (slurp prompt-file)))))

(defn save-prompt [prompt-name content]
  (let [prompt-file (io/file prompts-dir (str prompt-name ".edn"))]
    (spit prompt-file (pr-str content))))
