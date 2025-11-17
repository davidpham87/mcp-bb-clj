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
