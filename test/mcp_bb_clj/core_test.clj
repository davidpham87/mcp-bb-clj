(ns mcp-bb-clj.core-test
  (:require [clojure.test :refer [deftest is]]
            [mcp-bb-clj.core :as core]))

(deftest app-test
  (let [response (core/app {})]
    (is (= 200 (:status response)))
    (is (= "hello http-kit" (:body response)))))
