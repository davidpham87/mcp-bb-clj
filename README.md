# mcp-bb-clj
An MCP server/client written in clojure for babashka


Implementation of MCP protocol (https://modelcontextprotocol.io/specification/2025-06-18/schema) using only babashka (clojure) libraries 
https://book.babashka.org/#libraries

Split implementation:
- Data manipulation through pure functions
- Expose the function using http-skit server.
- Make a client as well.

Inspiration:
- https://github.com/hugoduncan/mcp-clj

## Tools

The server comes with several built-in tools:

- **cljfmt**: Formats Clojure code using [cljfmt](https://github.com/weavejester/cljfmt).
  - Input: `code` (string)
- **zprint**: Formats Clojure code or EDN using [zprint](https://github.com/kkinnear/zprint).
  - Input: `code` (string), `options` (optional EDN string)
- **find-malformed-delimiters**: Checks Clojure code for malformed delimiters.
  - Input: `code` (string)
- **echo**: Echoes the input text.
- **Malli Tools**: Tools for working with Malli schemas (`validate-schema`, `generate-sample`, `infer-schema`).

## Adding new Tools and Prompts

### Adding a Tool

To add a new tool, define it as a map and register it in `src/mcp_bb_clj/core.clj`.

1. **Define the tool**:

   A tool definition requires a `:name`, `:description`, `:inputSchema` (JSON Schema), and an `:implementation` function.

   Example:
   ```clojure
   (def my-tool
     {:name "my-tool"
      :description "A custom tool description"
      :inputSchema {:type "object"
                    :properties {"arg" {:type "string"}}
                    :required ["arg"]}
      :implementation (fn [{:keys [arg]}]
                        {:content [{:type "text" :text (str "You said: " arg)}]})})
   ```

2. **Register the tool**:

   Add the tool to the `mcp-server/add-tool!` call in `src/mcp_bb_clj/core.clj`.

   ```clojure
   (mcp-server/add-tool! mcp-server
                         {:tools [echo-tool
                                  my-tool]})
   ```

### Adding a Prompt

Prompts can be defined similarly and used as templates.

1. **Define the prompt**:

   ```clojure
   (def my-prompt
     {:name "explain-code"
      :description "Explains the provided code."
      :arguments {:type "object"
                  :properties {"code" {:type "string"}}
                  :required ["code"]}
      :messages [{:role "user"
                  :content "Explain this code:\n\n{{code}}"}]})
   ```
