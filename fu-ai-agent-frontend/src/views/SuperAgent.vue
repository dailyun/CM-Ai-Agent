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
// ... (脚本部分保持不变，这里省略以突出样式更改) ...
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from '../components/AppFooter.vue'
import { chatWithManus } from '../api'

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
const messages = ref([])
const connectionStatus = ref('disconnected')
let eventSource = null

const addMessage = (content, isUser, type = '') => {
  messages.value.push({
    content,
    isUser,
    type,
    time: new Date().getTime()
  })
}

const sendMessage = (message) => {
  addMessage(message, true, 'user-question')

  if (eventSource) {
    eventSource.close()
  }

  connectionStatus.value = 'connecting'
  let messageBuffer = []
  let lastBubbleTime = 0
  let isFirstResponseChunk = true

  const chineseEndPunctuation = ['。', '！', '？', '…', '\n']
  const minBubbleInterval = 700
  const maxBubbleLength = 60

  const createBubble = (content, bubbleType = 'ai-answer') => {
    if (!content.trim()) return

    const now = Date.now()
    const timeSinceLastBubble = now - lastBubbleTime

    const processAdd = () => {
      addMessage(content, false, bubbleType)
      lastBubbleTime = Date.now()
      messageBuffer = []
      isFirstResponseChunk = false
    }

    if (isFirstResponseChunk || timeSinceLastBubble >= minBubbleInterval) {
      processAdd()
    } else {
      setTimeout(processAdd, minBubbleInterval - timeSinceLastBubble)
    }
  }

  try {
    eventSource = chatWithManus(message)
    if (!eventSource || typeof eventSource.onmessage === 'undefined') {
      throw new Error("chatWithManus did not return a valid EventSource object.");
    }
  } catch (error) {
    console.error("Failed to initialize EventSource:", error);
    addMessage("抱歉，连接AI智能体失败，请稍后再试。", false, "ai-error");
    connectionStatus.value = 'error';
    return;
  }

  eventSource.onmessage = (event) => {
    connectionStatus.value = 'connected';
    const data = event.data

    if (data && data.trim() !== '[DONE]') {
      messageBuffer.push(data)
      const combinedText = messageBuffer.join('')
      const lastChar = data.charAt(data.length - 1)

      if (chineseEndPunctuation.includes(lastChar) || combinedText.length >= maxBubbleLength) {
        createBubble(combinedText)
      }
    }

    if (data.trim() === '[DONE]') {
      if (messageBuffer.length > 0) {
        createBubble(messageBuffer.join(''), 'ai-final')
      }
      connectionStatus.value = 'disconnected'
      if (eventSource) eventSource.close()
    }
  }

  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    if (messageBuffer.length > 0) {
      createBubble(messageBuffer.join('') + "\n(连接中断)", 'ai-error')
    } else if (connectionStatus.value !== 'disconnected') {
      addMessage("抱歉，与AI智能体的通讯发生错误，请刷新页面或稍后再试。", false, "ai-error")
    }
    connectionStatus.value = 'error'
    if (eventSource) eventSource.close()
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  addMessage('你好，我是AI智能体。我可以解答各类问题，提供专业建议，请问有什么可以帮助你的吗？', false, 'ai-answer')
  connectionStatus.value = 'disconnected'
})

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

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