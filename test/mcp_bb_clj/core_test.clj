(ns mcp-bb-clj.core-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [mcp-bb-clj.core :as core]))

(deftest app-test
  (let [response (core/app {})]
    (is (= 200 (:status response)))
    (is (= "hello http-kit" (:body response)))))

(defn -main []
  (let [{:keys [fail error]} (run-tests 'mcp-bb-clj.core-test)]
    (if (zero? (+ fail error))
      (println "Tests passed!")
      (println "Tests failed!"))))
