(ns mcp-bb-clj.e2e-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [mcp-bb-clj.mcp.server :as mcp-server]
            [mcp-bb-clj.mcp.client :as mcp-client]
            [mcp-bb-clj.core :as core]
            [mcp-bb-clj.tools :as tools]
            [org.httpkit.server :as http-server]
            [babashka.http-client :as http]
            [mcp-bb-clj.mcp.json-rpc :as rpc]
            [babashka.json :as json]
            [clojure.string :as str]))

(def port 19876)
(def server-url (str "http://localhost:" port))

(defn with-server [f]
  (let [mcp-s (mcp-server/create-server)
        _ (mcp-server/add-tool! mcp-s {:tools [tools/eval-tool tools/start-repl-tool]})
        stop-server (http-server/run-server (core/app mcp-s) {:port port})]
    (try
      (f)
      (finally
        (stop-server)))))

(use-fixtures :each with-server)

(deftest e2e-repl-test
  (testing "E2E: Client connects, lists tools, evaluates code, and starts REPL"
    (let [client-atom (mcp-client/create-client
                       (fn [_] nil) ;; Placeholder, updated below
                       {:client-info {:name "test-client" :version "1.0"}})]

      ;; Update send-fn to use the client-atom
      (swap! client-atom assoc :send-fn
             (fn [json-str]
               (let [resp (http/post server-url {:body json-str})
                     body (:body resp)
                     parsed (rpc/parse body)]
                 (mcp-client/handle-message client-atom parsed))))

      ;; 1. Initialize
      (let [init-res @(mcp-client/initialize! client-atom)]
        (is (= "2025-06-18" (:protocolVersion init-res))))

      ;; 2. List Tools
      (let [tools-res @(mcp-client/list-tools client-atom)
            tools (:tools tools-res)
            tool-names (set (map :name tools))]
        (is (contains? tool-names "eval"))
        (is (contains? tool-names "start-repl")))

      ;; 3. Eval Code
      (let [eval-res @(mcp-client/call-tool client-atom "eval" {:code "(+ 1 2)"})
            content (get-in eval-res [:content 0 :text])]
        (is (= "Result: 3" content)))

      ;; 4. Start REPL
      (let [repl-res @(mcp-client/call-tool client-atom "start-repl" {:host "localhost" :port 19877})
            content (get-in repl-res [:content 0 :text])]
        (is (str/includes? content "nREPL server started on localhost:19877"))))))
