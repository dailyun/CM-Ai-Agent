# CM AI Agent

## 项目简介
CM AI Agent 是一个基于 Spring Boot 3 的多智能体应用示例，整合 Spring AI、DashScope、LangChain4j 等能力，提供向量检索问答、工具编排与流式对话能力，同时配套了一个基于 Vue 3 + Vite 的前端界面。【F:pom.xml†L5-L80】【F:fu-ai-agent-frontend/package.json†L1-L20】后端提供健康检查和面向聊天场景的 SSE 接口，前端通过事件流渲染实时响应，适合用于构建中文场景下的智能助手。

## 功能特性
- **聊天与工具编排**：`/ai/fufu_app` 系列接口支持同步或 SSE 流式响应，并可在对话中调用注册的工具；`/ai/manus/chat` 暴露多步骤执行的超级智能体，能够在需要时串联多个工具完成任务。【F:src/main/java/com/dali/fuaiagent/controller/AiController.java†L30-L74】
- **知识增强 (RAG)**：`FuFuApp` 通过文件式聊天记忆、查询改写与向量检索增强模型回答能力，默认会将 `classpath*:document/*.md` 下的 Markdown 文档加载进 SimpleVectorStore。【F:src/main/java/com/dali/fuaiagent/app/FuFuApp.java†L40-L132】【F:src/main/java/com/dali/fuaiagent/chatmemory/FileBasedChatMemory.java†L27-L88】【F:src/main/java/com/dali/fuaiagent/rag/FuFuAppDocumentLoader.java†L26-L44】【F:src/main/java/com/dali/fuaiagent/rag/QueryRewriter.java†L15-L28】【F:src/main/java/com/dali/fuaiagent/rag/FuFuAppVectorStoreConfig.java†L26-L35】
- **外部工具与 MCP 扩展**：系统注册了文件操作、网页搜索/抓取、资源下载、终端命令、PDF 生成及终止器等工具，可供代理按需调用；其中终端工具默认使用 `cmd.exe`，在 Linux/Mac 上需要自行调整实现。【F:src/main/java/com/dali/fuaiagent/tool/ToolRegistration.java†L12-L32】【F:src/main/java/com/dali/fuaiagent/tool/TerminalOperationTool.java†L12-L33】
- **多步骤智能体**：`FuManus` 继承自通用的 ReAct/ToolCall Agent 基类，拥有更详细的系统提示、步骤上限与 SSE 推送实现，适合处理复杂任务流。【F:src/main/java/com/dali/fuaiagent/agent/FuManus.java†L12-L46】【F:src/main/java/com/dali/fuaiagent/agent/BaseAgent.java†L30-L173】
- **前端体验**：前端应用提供 FUFU 聊天界面，使用 SSE 持续接收分片消息并展示连接状态；API 基址默认指向生产地址，可按需改为本地后端。【F:fu-ai-agent-frontend/src/views/FufuMaster.vue†L49-L133】【F:fu-ai-agent-frontend/src/api/index.js†L5-L57】
- **基础运维能力**：提供 `/health` 健康检查方便探活与监控。【F:src/main/java/com/dali/fuaiagent/controller/HealthController.java†L8-L15】

## 目录结构
- `src/main/java/com/dali/fuaiagent`：后端核心代码（控制器、智能体、RAG、工具等）。
- `src/main/resources/document`：示例知识库 Markdown 文档，启动时会被载入向量库。【F:src/main/java/com/dali/fuaiagent/rag/FuFuAppDocumentLoader.java†L26-L44】
- `fu-ai-agent-frontend/`：Vue 3 + Vite 前端工程及静态资源。【F:fu-ai-agent-frontend/package.json†L6-L19】
- `chat-memory/`：运行时自动创建的本地对话记忆目录，采用 Kryo 序列化存储会话数据。【F:src/main/java/com/dali/fuaiagent/chatmemory/FileBasedChatMemory.java†L27-L88】

## 环境要求
- Java 21 及 Maven（项目内包含 `mvnw`/`mvnw.cmd` 可直接使用）。【F:pom.xml†L5-L33】
- Node.js（建议 18+）与 npm，用于运行前端脚本和 Vite 开发服务器。【F:fu-ai-agent-frontend/package.json†L6-L19】
- 可用的 DashScope API Key，并在 Spring AI 中配置对应的 `ChatModel` Bean（代码通过 `dashscopeChatModel` 注入）。【F:src/main/java/com/dali/fuaiagent/app/FuFuApp.java†L40-L53】【F:src/main/java/com/dali/fuaiagent/controller/AiController.java†L21-L37】
- SearchAPI 的 `search-api.api-key`，供网页搜索工具使用。【F:src/main/java/com/dali/fuaiagent/tool/ToolRegistration.java†L12-L32】
- PostgreSQL + pgvector（可选），若需使用 `PgVectorStore` 存储长久向量数据需提供标准数据源配置并确保扩展可用。【F:src/main/java/com/dali/fuaiagent/rag/PgVectorVectorStoreConfig.java†L23-L35】

## 配置指南
1. 在 `application.yml` 或环境变量中配置 Spring AI 所需的 DashScope 参数，例如：
   ```yaml
   spring:
     ai:
       dashscope:
         api-key: ${DASHSCOPE_API_KEY}
   ```
   并按需配置数据库 `spring.datasource.*` 以启用 `PgVectorStore`。
2. 设置网页搜索工具所需的 API Key：
   ```yaml
   search-api:
     api-key: ${SEARCH_API_KEY}
   ```
3. 如需扩展知识库，将新的 Markdown 文档放入 `src/main/resources/document`，应用重启后会自动读取并写入向量库。【F:src/main/java/com/dali/fuaiagent/rag/FuFuAppDocumentLoader.java†L26-L44】【F:src/main/java/com/dali/fuaiagent/rag/FuFuAppVectorStoreConfig.java†L26-L35】
4. 若后端部署在自定义域名/端口，请修改前端 `API_BASE_URL` 以指向正确的反向代理或网关地址。【F:fu-ai-agent-frontend/src/api/index.js†L5-L57】
5. 在非 Windows 环境下使用终端工具时，请将 `cmd.exe` 调整为适配本机的 shell（如 `/bin/bash`）。【F:src/main/java/com/dali/fuaiagent/tool/TerminalOperationTool.java†L16-L33】

## 后端运行
```bash
./mvnw spring-boot:run
```
应用启动后可访问：
- `GET /health`：健康检查。
- `GET /ai/fufu_app/chat/sse?message=...&chatId=...`：FUFU 智能体 SSE 聊天。
- `GET /ai/manus/chat?message=...`：FuManus 多步骤代理流式聊天。【F:src/main/java/com/dali/fuaiagent/controller/AiController.java†L30-L74】

## 前端运行
```bash
cd fu-ai-agent-frontend
npm install
npm run dev
```
开发服务器默认使用 Vite，运行后即可通过浏览器体验聊天界面。需要与后端进行跨域通信时，可借助后端 `CorsConfig` 允许来自任意来源的请求。【F:src/main/java/com/dali/fuaiagent/config/CorsConfig.java†L10-L23】【F:fu-ai-agent-frontend/package.json†L6-L19】

## 常见问题
- **SSE 代理**：前端和智能体均使用 Server-Sent Events 推送，需要确保网关或反向代理支持长连接并正确转发 `text/event-stream` 响应。【F:src/main/java/com/dali/fuaiagent/controller/AiController.java†L35-L74】【F:fu-ai-agent-frontend/src/views/FufuMaster.vue†L67-L116】
- **聊天记忆持久化**：`chat-memory` 目录默认创建在应用工作目录，可通过挂载或更改 `FileBasedChatMemory` 构造参数将数据写入持久化存储。【F:src/main/java/com/dali/fuaiagent/chatmemory/FileBasedChatMemory.java†L27-L88】
- **工具安全**：文件、终端、下载等工具具备较高权限，请在受信任的环境中启用，并根据实际需求裁剪注册的工具列表。【F:src/main/java/com/dali/fuaiagent/tool/ToolRegistration.java†L16-L32】

## 许可证
仓库未显式声明开源许可证，若需对外发布请先补充相应的 License 文件。
