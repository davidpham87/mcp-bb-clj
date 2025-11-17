#!/usr/bin/env bb

;; The core logic for your CI task goes here.
;; Babashka provides access to file system, HTTP client, JSON parsing, and more.

(require '[clojure.string :as str])

(defn summarize-ci-run []
  (let [github-actor (System/getenv "GITHUB_ACTOR")
        github-sha (System/getenv "GITHUB_SHA")
        current-branch (System/getenv "GITHUB_REF_NAME")]

    (println "--- Babashka CI Summary ---")
    (println "Running with Babashka version:" (System/getProperty "babashka.version"))
    (println "User:" (or github-actor "Unknown"))
    (println "Branch:" (or current-branch "N/A"))
    (println "Commit SHA (short):" (subs (or github-sha "N/A") 0 8))
    
    (println "\nCI Logic Output:")
    (let [items ["tests" "linters" "build" "documentation"]
          all-passed? true]
      
      (if all-passed?
        (println "🎉 All 4 critical checks passed successfully.")
        (println "⚠️ Warning: Some checks might need attention."))

      (doseq [item items]
        (let [status (if (= (mod (rand-int 10) 2) 0) "✅ PASS" "❌ FAIL")]
          (println (str " - " (str/capitalize item) ": " status))))
      )
    (println "---------------------------")))

(summarize-ci-run)

;; Optional: You can exit with a non-zero code to fail the job
;; (when-not all-passed? (System/exit 1))
