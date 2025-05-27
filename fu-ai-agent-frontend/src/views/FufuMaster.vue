<template>
  <div class="fufu-master-container">
    <div class="page-header">
      <div class="back-button" @click="goBack">返回</div>
      <h1 class="page-title">AI-FUFU</h1>
      <div class="chat-id">会话ID: {{ chatId }}</div>
    </div>

    <div class="content-wrapper">
      <div class="chat-area">
        <ChatRoom
            :messages="messages"
            :connection-status="connectionStatus"
            ai-type="love"
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
// ... Script部分保持不变 ...
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from '../components/AppFooter.vue'
import { chatWithLoveApp } from '../api'

useHead({
  title: 'AI-FUFU - FUFU智能体',
  meta: [
    {
      name: 'description',
      content: '与AI-FUFU聊天，它是您的专业顾问，帮您解答各种问题。'
    },
    {
      name: 'keywords',
      content: 'AI-FUFU, FUFU智能体, 聊天机器人, 专业顾问, AI对话'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
let eventSource = null

const generateChatId = () => {
  return 'fufu_chat_' + Math.random().toString(36).substring(2, 10)
}

const addMessage = (content, isUser) => {
  messages.value.push({
    content,
    isUser,
    time: new Date().getTime()
  })
}

const sendMessage = (message) => {
  addMessage(message, true)

  if (eventSource) {
    eventSource.close()
  }

  const aiMessageIndex = messages.value.length
  addMessage('', false)

  connectionStatus.value = 'connecting'

  try {
    eventSource = chatWithLoveApp(message, chatId.value)
    if (!eventSource || typeof eventSource.onmessage === 'undefined') {
      throw new Error("chatWithLoveApp did not return a valid EventSource object.");
    }
  } catch (error) {
    console.error("Failed to initialize EventSource:", error);
    if (aiMessageIndex < messages.value.length) {
      messages.value[aiMessageIndex].content = "抱歉，AI-FUFU 连接失败了，请稍后再试。";
    }
    connectionStatus.value = 'error';
    return;
  }


  eventSource.onmessage = (event) => {
    connectionStatus.value = 'connected';
    const data = event.data
    if (data && data.trim() !== '[DONE]') {
      if (aiMessageIndex < messages.value.length) {
        messages.value[aiMessageIndex].content += data
      }
    }

    if (data.trim() === '[DONE]') {
      connectionStatus.value = 'disconnected'
      if(eventSource) eventSource.close()
    }
  }

  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    if (aiMessageIndex < messages.value.length && messages.value[aiMessageIndex].content === '') {
      messages.value[aiMessageIndex].content = "抱歉，AI-FUFU 遇到了一点小问题，请稍后再试。";
    }
    connectionStatus.value = 'error'
    if (eventSource) eventSource.close()
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  chatId.value = generateChatId()
  addMessage('欢迎来到AI-FUFU，请告诉我你的问题，我会尽力给予帮助和建议。', false)
  connectionStatus.value = 'disconnected';
})

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

:root {
  --fufu-primary-blue: #72BFF4;
  --fufu-hover-blue: #5DA0D9;
  --fufu-light-blue: #E0F2FF;
  --fufu-white: #FFFFFF;
  --fufu-light-gray: #F4F7F9;
  --fufu-border-gray: #E2E8F0;
  --fufu-text-dark: #33475B;
  --fufu-text-medium: #677A8C;
  --fufu-text-light: #A0AEC0;
}

.fufu-master-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh; /* 页面至少和视口一样高 */
  /* REMOVED: height: 100vh; and overflow: hidden; to allow page scroll if content exceeds viewport */
  background-color: var(--fufu-light-gray);
  font-family: 'Inter', sans-serif;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: var(--fufu-primary-blue);
  color: var(--fufu-white);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  position: sticky; /* Header sticks at the top */
  top: 0;
  z-index: 1000;
  flex-shrink: 0; /* Header does not shrink */
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
  flex-grow: 1;
}

.chat-id {
  font-size: 0.875rem;
  opacity: 0.8;
  white-space: nowrap;
  margin-left: 16px;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1 1 0; /* Grow to fill available space, basis 0 */
  overflow: hidden; /* Contains chat area, prevents its overflow from affecting this wrapper */
}

.chat-area {
  display: flex;
  flex-direction: column;
  flex: 1 1 0; /* Grow to fill content-wrapper, basis 0 */
  overflow: hidden; /* ChatRoom inside handles its own scrolling */
  padding: 16px;
}

.footer-container {
  flex-shrink: 0; /* Footer does not shrink */
  background-color: var(--fufu-white);
  border-top: 1px solid var(--fufu-border-gray);
  /* No margin-top: auto needed here, natural flow will place it after content-wrapper */
}

/* Responsive Styles - unchanged from previous version, assumed to be fine */
@media (max-width: 768px) {
  .page-header { padding: 12px 16px; }
  .page-title { font-size: 1.125rem; }
  .chat-id { font-size: 0.75rem; }
  .chat-area { padding: 12px; }
  .back-button { font-size: 0.9rem; }
  .back-button:before { margin-right: 6px; }
}

@media (max-width: 480px) {
  .page-header { padding: 10px 12px; }
  .back-button { font-size: 0.875rem; }
  .back-button:before { margin-right: 4px; }
  .page-title { font-size: 1rem; }
  .chat-id { display: none; }
  .chat-area { padding: 8px; }
}
</style>