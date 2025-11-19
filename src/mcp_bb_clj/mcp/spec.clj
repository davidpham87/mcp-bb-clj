(ns mcp-bb-clj.mcp.spec
  (:require [malli.core :as m]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; JSON-RPC 2.0 specs

(def Request
  [:map
   [:jsonrpc [:= "2.0"]]
   [:id [:or :string :int]]
   [:method :string]
   [:params {:optional true} :map]])

(def Notification
  [:map
   [:jsonrpc [:= "2.0"]]
   [:method :string]
   [:params {:optional true} :map]])

(def Response
  [:map
   [:jsonrpc [:= "2.0"]]
   [:id [:or :string :int]]
   [:result :any]])

(def ErrorResponse
  [:map
   [:jsonrpc [:= "2.0"]]
   [:id [:or :string :int]]
   [:error :map]])

(def Message
  [:or Request Notification Response ErrorResponse])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; MCP common types

(def Role [:enum "user" "assistant"])

(def Annotations
  [:map
   [:audience {:optional true} [:vector Role]]
   [:lastModified {:optional true} :string]
   [:priority {:optional true} :number]])

(def TextContent
  [:map
   [:type [:= "text"]]
   [:text :string]
   [:annotations {:optional true} Annotations]])

(def ImageContent
  [:map
   [:type [:= "image"]]
   [:data :string]
   [:mimeType :string]
   [:annotations {:optional true} Annotations]])

(def AudioContent
  [:map
   [:type [:= "audio"]]
   [:data :string]
   [:mimeType :string]
   [:annotations {:optional true} Annotations]])

(def ResourceLink
  [:map
   [:type [:= "resource_link"]]
   [:uri :string]
   [:name :string]
   [:description {:optional true} :string]
   [:mimeType {:optional true} :string]
   [:size {:optional true} :number]
   [:title {:optional true} :string]
   [:annotations {:optional true} Annotations]])

(def EmbeddedResource
  [:map
   [:type [:= "resource"]]
   [:resource [:or :map]]
   [:annotations {:optional true} Annotations]])

(def ContentBlock
  [:or TextContent ImageContent AudioContent ResourceLink EmbeddedResource])
