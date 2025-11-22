(ns mcp-bb-clj.malli-tools
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [malli.provider :as mp]
            [malli.util]))

(def validate-schema-tool
  {:name "validate-schema"
   :description "Validates data against a Malli schema."
   :input-schema {:type "object"
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
                        :structured-content {:valid valid?
                                             :explanation explanation}
                        :is-error (not valid?)}))})

(def generate-sample-tool
  {:name "generate-sample"
   :description "Generates a sample value from a Malli schema."
   :input-schema {:type "object"
                  :properties {"schema" {:type "any"}}
                  :required ["schema"]}
   :implementation (fn [{:keys [schema]}]
                     (try
                       (let [sample (mg/generate schema)]
                         {:content [{:type "text"
                                     :text (str "Generated sample: " sample)}]
                          :structured-content {:sample sample}})
                       (catch Exception e
                         {:content [{:type "text"
                                     :text (str "Error generating sample: " (.getMessage e))}]
                          :is-error true})))})

(def infer-schema-tool
  {:name "infer-schema"
   :description "Infers a Malli schema from a collection of data."
   :input-schema {:type "object"
                  :properties {"data" {:type "any"}}
                  :required ["data"]}
   :implementation (fn [{:keys [data]}]
                     (try
                       (let [schema (mp/provide data)]
                         {:content [{:type "text"
                                     :text (str "Inferred schema: " schema)}]
                          :structured-content {:schema schema}})
                       (catch Exception e
                         {:content [{:type "text"
                                     :text (str "Error inferring schema: " (.getMessage e))}]
                          :is-error true})))})
