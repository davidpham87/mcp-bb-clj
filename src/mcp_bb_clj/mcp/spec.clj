(ns mcp-bb-clj.mcp.spec
  (:require [malli.core :as m]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; JSON-RPC 2.0 specs

(def json-registry
  {::json [:or :string :int :double :boolean :nil
           [:vector [:ref ::json]]
           [:map-of :keyword [:ref ::json]]]})

(def JSONValue
  [:schema {:registry json-registry} ::json])

;; Explicitly repeating [:jsonrpc [:= "2.0"]] to avoid [:merge]

(def Request
  [:map
   [:jsonrpc [:= "2.0"]]
   [:id [:or :string :int]]
   [:method :string]
   [:params {:optional true} [:map-of :keyword JSONValue]]])

(def Notification
  [:map
   [:jsonrpc [:= "2.0"]]
   [:method :string]
   [:params {:optional true} [:map-of :keyword JSONValue]]])

(def Response
  [:map
   [:jsonrpc [:= "2.0"]]
   [:id [:or :string :int]]
   [:result JSONValue]])

(def ErrorResponse
  [:map
   [:jsonrpc [:= "2.0"]]
   [:id [:or :string :int]]
   [:error [:map-of :keyword JSONValue]]])

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Tools, Resources, Prompts

(def Tool
  [:map
   [:name :string]
   [:description {:optional true} :string]
   [:inputSchema [:map-of :keyword JSONValue]]])

(def Resource
  [:map
   [:uri :string]
   [:name :string]
   [:description {:optional true} :string]
   [:mimeType {:optional true} :string]
   [:annotations {:optional true} Annotations]])

(def PromptArgument
  [:map
   [:name :string]
   [:description {:optional true} :string]
   [:required {:optional true} :boolean]])

(def Prompt
  [:map
   [:name :string]
   [:description {:optional true} :string]
   [:arguments {:optional true} [:vector PromptArgument]]])
