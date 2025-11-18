(ns mcp-bb-clj.prompts
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def prompts-dir "prompts")

(defn load-prompt [prompt-name]
  (let [prompt-file (io/file prompts-dir (str prompt-name ".edn"))]
    (when (.exists prompt-file)
      (edn/read-string (slurp prompt-file)))))

(defn save-prompt [prompt-name content]
  (.mkdirs (io/file prompts-dir))
  (let [prompt-file (io/file prompts-dir (str prompt-name ".edn"))]
    (spit prompt-file (pr-str content))))

(defn create-prompt-tool
  "Creates a new MCP tool from a prompt definition."
  [{:keys [name description inputSchema prompt-fn]}]
  {:name name
   :description description
   :inputSchema inputSchema
   :implementation (fn [params]
                     (try
                       (let [content (prompt-fn params)]
                         {:content [{:type "text"
                                     :text content}]
                          :structuredContent {:prompt content}})
                       (catch Exception e
                         {:content [{:type "text"
                                     :text (str "Error creating prompt: " (.getMessage e))}]
                          :isError true})))})
