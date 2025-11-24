(ns mcp-bb-clj.tools
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [mcp-bb-clj.prompts :as prompts]
            [babashka.nrepl.server :as nrepl-server]
            [portal.api :as p]
            [cljfmt.core :as cljfmt]
            [zprint.core :as zprint]
            [rewrite-clj.parser :as parser]))

(def start-repl-tool
  {:name "start-repl"
   :description "Starts a Babashka nREPL server."
   :inputSchema {:type "object"
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
                          :isError true})))})

(def eval-tool
  {:name "eval"
   :description "Evaluates a string of Clojure code. WARNING: This tool is a security risk and should not be exposed to untrusted users."
   :inputSchema {:type "object"
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
                          :isError true})))})

(def load-prompt-tool
  {:name "load-prompt"
   :description "Loads a prompt from a file."
   :inputSchema {:type "object"
                 :properties {"name" {:type "string"}}
                 :required ["name"]}
   :implementation (fn [{:keys [name]}]
                     (try
                       (if-let [content (prompts/get-prompt name)]
                         {:content [{:type "text"
                                     :text (str "Prompt loaded: " (pr-str content))}]
                          :structuredContent {:prompt content}}
                         {:content [{:type "text"
                                     :text (str "Prompt not found: " name)}]
                          :isError true})
                       (catch Exception e
                         {:content [{:type "text"
                                     :text (str "Error loading prompt: " (.getMessage e))}]
                          :isError true})))})

(def save-prompt-tool
  {:name "save-prompt"
   :description "Saves a prompt to a file."
   :inputSchema {:type "object"
                 :properties {"name" {:type "string"}
                              "content" {:type "any"}}
                 :required ["name" "content"]}
   :implementation (fn [{:keys [name content]}]
                     (try
                       (prompts/save-prompt! name content)
                       {:content [{:type "text"
                                   :text (str "Prompt saved: " name)}]}
                       (catch Exception e
                         {:content [{:type "text"
                                     :text (str "Error saving prompt: " (.getMessage e))}]
                          :isError true})))})

(def greeting-tool
  (prompts/create-prompt-tool
   {:name "greeting"
    :description "Generates a greeting message."
    :inputSchema {:type "object"
                  :properties {"name" {:type "string"}}
                  :required ["name"]}
    :prompt-fn (fn [{:keys [name]}]
                 (str "Hello, " name "!"))}))

(defonce portal-atom (atom nil))

(def portal-tool
  {:name        "portal"
   :description "Sends a value to a portal."
   :inputSchema {:type       "object"
                 :properties {"code" {:type "string"}}
                 :required   ["code"]}
   :implementation
   (fn [{:keys [code]}]
     (try
       (when (nil? @portal-atom)
         (let [p (p/open {:value (atom [])})]
           (add-tap #'p/submit)
           (reset! portal-atom p)))
       (let [result (eval (read-string code))]
         (p/submit result)
         {:content          [{:type "text"
                              :text (str "Result: " (pr-str result))}]
          :structuredContent {:result result}})
       (catch Exception e
         {:content [{:type    "text"
                     :text    (str "Error evaluating code: " (.getMessage e))}]
          :isError true})))})

(def cljfmt-tool
  {:name "cljfmt"
   :description "Formats Clojure code using cljfmt."
   :inputSchema {:type "object"
                 :properties {"code" {:type "string"}}
                 :required ["code"]}
   :implementation (fn [{:keys [code]}]
                     (try
                       (let [formatted (cljfmt/reformat-string code)]
                         {:content [{:type "text" :text formatted}]})
                       (catch Exception e
                         {:content [{:type "text" :text (str "Error formatting code: " (.getMessage e))}]
                          :isError true})))})

(def zprint-tool
  {:name "zprint"
   :description "Formats Clojure code or EDN using zprint."
   :inputSchema {:type "object"
                 :properties {"code" {:type "string"}
                              "options" {:type "string" :description "Optional zprint options as an EDN string."}}
                 :required ["code"]}
   :implementation (fn [{:keys [code options]}]
                     (try
                       (let [opts (if options (edn/read-string options) {})
                             formatted (zprint/zprint-str code opts)]
                         {:content [{:type "text" :text formatted}]})
                       (catch Exception e
                         {:content [{:type "text" :text (str "Error formatting code: " (.getMessage e))}]
                          :isError true})))})

(def find-malformed-delimiters-tool
  {:name "find-malformed-delimiters"
   :description "Checks Clojure code for malformed delimiters (unbalanced parentheses, brackets, braces)."
   :inputSchema {:type "object"
                 :properties {"code" {:type "string"}}
                 :required ["code"]}
   :implementation (fn [{:keys [code]}]
                     (try
                       (parser/parse-string-all code)
                       {:content [{:type "text" :text "No malformed delimiters found."}]}
                       (catch Exception e
                         {:content [{:type "text" :text (str "Malformed delimiters found: " (.getMessage e))}]
                          :isError true})))})
