<template>
  <div class="super-agent-container">
    <div class="page-header">
      <div class="back-button" @click="goBack">返回</div>
      <h1 class="page-title">AI智能体</h1>
      <div class="header-placeholder"></div>
    </div>

    <div class="content-wrapper">
      <div class="chat-area">
        <ChatRoom
            :messages="messages"
            :connection-status="connectionStatus"
            ai-type="super"
            @send-message="sendMessage"
        />
      </div>
    </div>

    <div class="footer-container">
      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue' // 子组件，用于显示消息
import AppFooter from '../components/AppFooter.vue'
import { chatWithManus } from '../api' // 假设这个API方法返回一个 EventSource 实例

useHead({
  title: 'AI智能体 - FUFU智能体应用平台',
  meta: [
    {
      name: 'description',
      content: 'AI智能体是FUFU平台的全能助手，能解答各类专业问题，提供精准建议和解决方案。'
    },
    {
      name: 'keywords',
      content: 'AI智能体, FUFU平台, 专业问答, AI助手, 解决方案'
    }
  ]
})

const router = useRouter()
const messages = ref([]) // 这个数组会传递给 ChatRoom 组件
const connectionStatus = ref('disconnected') // 'disconnected', 'connecting', 'connected', 'streaming', 'error', 'completed'
let eventSource = null

// 通用方法：向消息列表中添加消息
const addMessage = (content, isUser, type = 'ai-answer') => { // 默认 type 为 'ai-answer'，可以根据需要调整
  messages.value.push({
    content,
    isUser,
    type, // 例如：'user-question', 'ai-update', 'ai-error', 'ai-completion', 'status'
    time: new Date().getTime()
  })
}

const sendMessage = (userInput) => {
  addMessage(userInput, true, 'user-question') // 添加用户发送的消息

  if (eventSource) {
    eventSource.close() // 关闭任何已存在的连接
  }

  connectionStatus.value = 'connecting'
  addMessage("AI Agent 正在连接并思考中...", false, "status"); // 初始状态消息

  try {
    // chatWithManus 现在应该返回一个 EventSource 实例
    eventSource = chatWithManus(userInput) // userInput 是来自 ChatRoom 的用户消息内容

    if (!eventSource || typeof eventSource.addEventListener !== 'function') {
      console.error("chatWithManus did not return a valid EventSource object.", eventSource);
      throw new Error("chatWithManus did not return a valid EventSource object.");
    }
  } catch (error) {
    console.error("初始化 EventSource 失败:", error);
    addMessage("抱歉，连接AI智能体失败（初始化错误），请稍后再试。", false, "ai-error");
    connectionStatus.value = 'error';
    return;
  }

  eventSource.onopen = () => {
    console.log("SSE 连接已建立");
    connectionStatus.value = 'connected'; // 连接已建立，等待数据
    // 可以更新或移除之前的 "正在连接并思考中..." 消息
    const statusMsgIndex = messages.value.findIndex(m => m.type === 'status' && m.content.includes("正在连接"));
    if (statusMsgIndex !== -1) {
      messages.value[statusMsgIndex].content = "AI Agent 已连接，等待回复...";
    } else {
      addMessage("AI Agent 已连接，等待回复...", false, "status");
    }
  };

  // 监听 'agent_update' 事件 (用于AI的逐步回复)
  eventSource.addEventListener('agent_update', (event) => {
    console.log("收到 agent_update:", event.data);
    connectionStatus.value = 'streaming'; // 正在接收数据流
    // 移除"等待回复"之类的状态消息 (如果存在)
    const statusMsgIndex = messages.value.findIndex(m => m.type === 'status');
    if (statusMsgIndex !== -1 && messages.value.length > (statusMsgIndex +1) ) { // 只有当后面还有其他消息时才移除，或者根据具体逻辑
      // 为简单起见，可以不清，让新消息直接追加
    }
    addMessage(event.data, false, 'ai-update'); // 将AI的每一步回复作为新消息添加
  });

  // 监听 'agent_error' 事件 (用于服务器发送的应用级错误)
  eventSource.addEventListener('agent_error', (event) => {
    console.error("收到 agent_error:", event.data);
    addMessage(`AI处理错误: ${event.data}`, false, 'ai-error');
    connectionStatus.value = 'error';
    if (eventSource) eventSource.close();
  });

  // 监听 'agent_completion' 事件 (用于任务完成的最终消息)
  eventSource.addEventListener('agent_completion', (event) => {
    console.log("收到 agent_completion:", event.data);
    addMessage(event.data, false, 'ai-completion');
    connectionStatus.value = 'completed'; // 或者 'disconnected'
    if (eventSource) eventSource.close();
  });

  // 处理通用的SSE错误 (网络问题等)
  eventSource.onerror = (error) => {
    console.error('SSE 连接错误:', error);
    // 只有在连接尚未被明确标记为完成或错误时才添加新的错误消息
    if (connectionStatus.value !== 'completed' && connectionStatus.value !== 'error') {
      addMessage("抱歉，与AI智能体的通讯发生网络错误或连接意外中断。", false, "ai-error");
    }
    connectionStatus.value = 'error'; // 最终状态标记为错误
    if (eventSource) eventSource.close();
  };
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  addMessage('你好，我是AI智能体。我可以解答各类问题，提供专业建议，请问有什么可以帮助你的吗？', false, 'ai-answer') // 初始AI欢迎消息
  connectionStatus.value = 'disconnected'
})

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
    console.log('SSE 连接已在组件卸载时关闭。')
  }
})
</script>

<style scoped>
/* 你的现有CSS样式保持不变 */
/* ... (省略你提供的所有CSS，请保留它们) ... */

/* 你可以考虑在 ChatRoom.vue 组件的 <style scoped> 或全局样式中
   为新的消息类型添加特定的CSS规则，例如：
*/
/* // 示例CSS (理想情况下放在 ChatRoom.vue 或全局)
  .ai-message.ai-update .message-bubble {
    background-color: var(--fufu-light-blue); // 已有的AI消息颜色
    color: var(--fufu-text-dark);
  }
  .ai-message.ai-error .message-bubble {
    background-color: #ffe9e9;
    color: #d8000c;
    border: 1px solid #f5c6cb;
  }
  .ai-message.ai-completion .message-bubble {
    background-color: #e9ffe9;
    color: #155724;
    border: 1px solid #c3e6cb;
    font-weight: bold;
  }
  .ai-message.status .message-bubble { // 用于 "AI正在思考中..." 等状态消息
    background-color: #f0f0f0;
    color: #555;
    font-style: italic;
    text-align: center;
  }
*/
</style>

<style scoped>
/* Import Inter font (optional if globally imported) */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

/* FUFU Fresh and Bright Style Palette (consistent with other pages) */
:root {
  --fufu-primary-blue: #72BFF4;
  --fufu-hover-blue: #5DA0D9;
  --fufu-light-blue: #E0F2FF;
  --fufu-white: #FFFFFF;
  --fufu-light-gray: #F4F7F9; /* Page background */
  --fufu-border-gray: #E2E8F0;
  --fufu-text-dark: #33475B;
  --fufu-text-medium: #677A8C;
  --fufu-text-light: #A0AEC0;
}

.super-agent-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh; /* Ensures the container takes at least full viewport height */
  height: 100vh; /* Optional: force it to be exactly viewport height if no page scroll desired */
  background-color: var(--fufu-light-gray);
  font-family: 'Inter', sans-serif;
  overflow: hidden; /* Prevents scrollbars on the main container if content overflows slightly */
}

.page-header {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background-color: var(--fufu-primary-blue);
  color: var(--fufu-white);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  position: sticky; /* Sticky header is fine, it won't affect flexbox height calculation for content-wrapper */
  top: 0;
  z-index: 1000;
  flex-shrink: 0; /* Header should not shrink */
}

.back-button {
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: opacity 0.2s ease-in-out, background-color 0.2s ease-in-out;
  padding: 4px 8px;
  border-radius: 6px;
  justify-self: start;
}

.back-button:hover {
  opacity: 0.85;
  background-color: rgba(255, 255, 255, 0.1);
}

.back-button:before {
  content: '←';
  margin-right: 8px;
  font-weight: bold;
}

.page-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
  text-align: center;
  grid-column: 2 / 3;
}

.header-placeholder {
  width: auto;
  min-width: 60px; /* Adjust to balance the back button's visual weight */
  justify-self: end;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1 1 0; /* Allow content-wrapper to grow and shrink, with a basis of 0 */
  overflow: hidden; /* Crucial to prevent its content from overflowing and breaking layout */
  /* min-height: 0; /* Optional: Override any browser default min-height for flex items */
}

.chat-area {
  display: flex;
  flex-direction: column;
  flex: 1 1 0; /* Allow chat-area to grow and shrink within content-wrapper */
  overflow: hidden; /* ChatRoom itself handles internal scrolling */
  padding: 16px;    /* Padding around the ChatRoom component */
  /* min-height: 0; /* Optional: Override browser default min-height */
}

.footer-container {
  flex-shrink: 0; /* Footer should not shrink */
  background-color: var(--fufu-white);
  border-top: 1px solid var(--fufu-border-gray);
  /* margin-top: auto; /* Not needed if content-wrapper fills space due to flex: 1 */
}

/* Responsive Styles */
@media (max-width: 768px) {
  .page-header {
    padding: 12px 16px;
    gap: 12px;
  }
  .page-title {
    font-size: 1.125rem;
  }
  .chat-area {
    padding: 12px;
  }
  .back-button {
    font-size: 0.9rem;
  }
  .back-button:before {
    margin-right: 6px;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 10px 12px;
    grid-template-columns: auto 1fr;
  }
  .header-placeholder {
    display: none;
  }
  .back-button {
    font-size: 0.875rem;
  }
  .back-button:before {
    margin-right: 4px;
  }
  .page-title {
    font-size: 1rem;
    text-align: left;
    padding-left: 8px;
  }
  .chat-area {
    padding: 8px;
  }
}
</style>