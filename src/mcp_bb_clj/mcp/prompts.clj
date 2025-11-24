(ns mcp-bb-clj.mcp.prompts
  "MCP prompt endpoints"
  (:require
    [clojure.string :as str]))

(defn- validate-argument
  "Validate a prompt argument definition"
  [{:keys [name description]}]
  (and (string? name)
       (not (str/blank? name))
       (or (nil? description)
           (string? description))))

(defn- validate-message
  "Validate a prompt message"
  [{:keys [role content]}]
  (and (contains? #{"user" "assistant" "system"} role)
       (or (and (map? content)
                (= "text" (:type content))
                (string? (:text content)))
           (and (map? content)
                (= "resource" (:type content))
                (map? (:resource content))))))

(defn valid-prompt?
  "Validate a prompt definition"
  [{:keys [name description arguments messages] :as prompt}]
  (when (and (string? name)
             (not (str/blank? name))
             (or (nil? description)
                 (string? description))
             (or (nil? arguments)
                 (and (vector? arguments)
                      (every? validate-argument arguments)))
             (vector? messages)
             (every? validate-message messages))
    prompt))

(defn- apply-template
  "Apply template arguments to a message"
  [message arguments]
  (if (and (= "text" (get-in message [:content :type]))
           (seq arguments))
    (update-in message [:content :text]
               (fn [text]
                 (reduce-kv
                   (fn [t k v]
                     (str/replace t (str "{{" (name k) "}}") (str v)))
                   text
                   arguments)))
    message))

(defn prompt-definition
  "Get the prompt definition without implementation details"
  [prompt]
  (select-keys prompt [:name :description :arguments :title]))

(defn list-prompts
  "List available prompts"
  [prompts _params]
  {:prompts (mapv prompt-definition (vals prompts))})

(defn get-prompt
  "Get a specific prompt with optional arguments"
  [prompts {:keys [name arguments] :as params}]
  (if-let [prompt (get prompts name)]
    (let [messages (if arguments
                     (mapv #(apply-template % arguments) (:messages prompt))
                     (:messages prompt))]
      {:messages messages
       :description (:description prompt)})
    {:content [{:type "text"
                :text (str "Prompt not found: " name)}]
     :isError true}))

;; Define the default REPL prompt
(def repl-prompt
  (valid-prompt?
    {:name "repl"
     :description "Standard REPL prompt for code evaluation"
     :messages [{:role "system"
                 :content {:type "text"
                           :text "You are interacting with a Clojure REPL."}}
                {:role "user"
                 :content {:type "text"
                           :text "Please evaluate {{code}}"}}]
     :arguments [{:name "code"
                  :description "Clojure code to evaluate"
                  :required true}]}))

(def default-prompts
  "Default set of built-in prompts"
  {"repl" repl-prompt})
