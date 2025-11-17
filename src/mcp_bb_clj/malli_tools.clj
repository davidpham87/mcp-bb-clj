(ns mcp-bb-clj.malli-tools
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [malli.provider :as mp]))

(def validate-schema-tool
  {:name "validate-schema"
   :description "Validates data against a Malli schema."
   :inputSchema {:type "object"
                 :properties {"schema" {:type "any"}
                              "data" {:type "any"}}
                 :required ["schema" "data"]}
   :implementation (fn [{:keys [schema data]}]
                     (let [valid? (m/validate schema data)
                           explanation (when-not valid? (m/explain schema data))]
                       {:content [{:type "text"
                                   :text (if valid?
                                           "Validation successful."
                                           (str "Validation failed: " explanation))}]
                        :structuredContent {:valid valid?
                                            :explanation explanation}
                        :isError (not valid?)}))})

(def generate-sample-tool
  {:name "generate-sample"
   :description "Generates a sample value from a Malli schema."
   :inputSchema {:type "object"
                 :properties {"schema" {:type "any"}}
                 :required ["schema"]}
   :implementation (fn [{:keys [schema]}]
                     (try
                       (let [sample (m/generate schema)]
                         {:content [{:type "text"
                                     :text (str "Generated sample: " sample)}]
                          :structuredContent {:sample sample}})
                       (catch Exception e
                         {:content [{:type "text"
                                     :text (str "Error generating sample: " (.getMessage e))}]
                          :isError true})))})

(def infer-schema-tool
  {:name "infer-schema"
   :description "Infers a Malli schema from a collection of data."
   :inputSchema {:type "object"
                 :properties {"data" {:type "any"}}
                 :required ["data"]}
   :implementation (fn [{:keys [data]}]
                     (try
                       (let [schema (mp/provide data)]
                         {:content [{:type "text"
                                     :text (str "Inferred schema: " schema)}]
                          :structuredContent {:schema schema}})
                       (catch Exception e
                         {:content [{:type "text"
                                     :text (str "Error inferring schema: " (.getMessage e))}]
                          :isError true})))})
