<template>
  <div class="chat-container">
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in props.messages" :key="msg.time + '-' + index" class="message-wrapper"> <div v-if="!msg.isUser"
                                                                                                              class="message ai-message"
                                                                                                              :class="[msg.type, { 'first-of-sequence': isFirstAiOfSequence(index) }]">
        <div class="avatar ai-avatar" :class="{ 'avatar-visible': isFirstAiOfSequence(index) }">
          <AiAvatarFallback :type="props.aiType" />
        </div>
        <div class="message-bubble">
          <div class="message-content">
            {{ msg.content }}
            <span v-if="props.connectionStatus === 'connecting' && index === props.messages.length - 1 && !msg.content.endsWith('▋')" class="typing-indicator">▋</span>
          </div>
          <div class="message-time">{{ formatTime(msg.time) }}</div>
        </div>
      </div>

        <div v-else class="message user-message">
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
          <div class="avatar user-avatar">
            <div class="avatar-placeholder">我</div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-container">
      <div class="chat-input-inner">
        <textarea
            v-model="inputMessage"
            @keydown.enter.prevent="handleSend"
            placeholder="请输入消息..."
            class="input-box"
            :disabled="props.connectionStatus === 'connecting'"
            ref="inputBoxRef"
            rows="1"
            @input="autoGrowTextarea"
        ></textarea>
        <button
            @click="handleSend"
            class="send-button"
            :disabled="props.connectionStatus === 'connecting' || !inputMessage.trim()"
        >
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"></path></svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, onMounted } from 'vue'
import AiAvatarFallback from './AiAvatarFallback.vue' // 确保路径正确

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  connectionStatus: {
    type: String,
    default: 'disconnected' // 'connecting', 'connected', 'error', 'disconnected'
  },
  aiType: {
    type: String,
    default: 'default'  // 'love' 或 'super'
  }
})

const emit = defineEmits(['send-message'])

const inputMessage = ref('')
const messagesContainer = ref(null)
const inputBoxRef = ref(null)

const handleSend = () => {
  if (!inputMessage.value.trim()) return
  emit('send-message', inputMessage.value)
  inputMessage.value = ''
  nextTick(() => { // 重置textarea高度
    if(inputBoxRef.value) {
      inputBoxRef.value.style.height = 'auto';
    }
  });
}

const autoGrowTextarea = (event) => {
  const textarea = event.target;
  textarea.style.height = 'auto'; // Reset height
  textarea.style.height = textarea.scrollHeight + 'px'; // Set to scroll height
}


const formatTime = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
}

const scrollToBottom = async (smooth = false) => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: smooth ? 'smooth' : 'auto'
    });
  }
}

// 判断是否是AI连续消息的第一个
const isFirstAiOfSequence = (index) => {
  if (index === 0 && !props.messages[index].isUser) return true; // 第一个消息是AI消息
  if (!props.messages[index].isUser && props.messages[index-1]?.isUser) return true; // 当前是AI，上一个是用户
  return false;
}


watch(() => props.messages.length, () => {
  scrollToBottom(true); // 新消息平滑滚动
}, { deep: true }) // deep true for watching content changes too

watch(() => props.messages[props.messages.length - 1]?.content, () => {
  scrollToBottom(); // AI消息流式输出时，自动滚动
});


onMounted(() => {
  scrollToBottom()
  if (inputBoxRef.value) { // 初始化textarea高度
    inputBoxRef.value.style.height = 'auto';
    inputBoxRef.value.style.height = inputBoxRef.value.scrollHeight + 'px';
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500&display=swap');

:root {
  --fufu-primary-blue: #72BFF4;
  --fufu-hover-blue: #5DA0D9;
  --fufu-light-blue: #E0F2FF; /* AI消息气泡背景 */
  --fufu-white: #FFFFFF;
  --fufu-light-gray: #F4F7F9; /* 聊天区域背景 */
  --fufu-border-gray: #E2E8F0;
  --fufu-text-dark: #33475B;   /* AI消息文字颜色 */
  --fufu-text-medium: #677A8C;
  --fufu-text-light: #A0AEC0;  /* 时间戳颜色 */
  --fufu-user-avatar-bg: #B2EBF2; /* 用户头像背景色，可选 */
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%; /* 撑满父容器 .chat-area */
  background-color: var(--fufu-white); /* 聊天容器用白色背景 */
  border-radius: 12px; /* 圆角与父容器协调 */
  overflow: hidden;
  position: relative;
  font-family: 'Inter', sans-serif;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 轻微阴影增加层次感 */
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  /* padding-bottom is handled by the space for input container */
  scrollbar-width: thin;
  scrollbar-color: var(--fufu-border-gray) transparent;
}

/* Webkit 浏览器滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}
.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}
.chat-messages::-webkit-scrollbar-thumb {
  background-color: var(--fufu-border-gray);
  border-radius: 3px;
}

.message-wrapper {
  margin-bottom: 12px; /* 消息间距 */
  display: flex;
  flex-direction: column; /* 确保wrapper内部消息和头像是堆叠的 */
  width: 100%;
}

.message {
  display: flex;
  align-items: flex-end; /* 头像和气泡底部对齐 */
  max-width: 75%; /* 消息最大宽度 */
}

.user-message {
  align-self: flex-end; /* 用户消息靠右 */
  flex-direction: row-reverse; /* 头像在右 */
}

.ai-message {
  align-self: flex-start; /* AI消息靠左 */
  flex-direction: row; /* 头像在左 */
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--fufu-light-gray); /* 默认头像背景 */
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.ai-avatar {
  margin-right: 10px;
  opacity: 0; /* 默认隐藏 */
  visibility: hidden;
}
.ai-avatar.avatar-visible {
  opacity: 1;
  visibility: visible;
}


.user-avatar {
  margin-left: 10px;
  background-color: var(--fufu-primary-blue); /* 用户头像背景 */
}

.avatar-placeholder { /* 用户头像文字 "我" */
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--fufu-white);
  font-weight: 500;
  font-size: 14px;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 18px;
  position: relative;
  word-break: break-word; /* 长单词换行 */
  box-shadow: 0 1px 2px rgba(0,0,0,0.08);
}

.user-message .message-bubble {
  background-color: var(--fufu-primary-blue);
  color: var(--fufu-white);
  border-bottom-right-radius: 6px; /* 小三角效果 */
}

.ai-message .message-bubble {
  background-color: var(--fufu-light-blue);
  color: var(--fufu-text-dark);
  border-bottom-left-radius: 6px; /* 小三角效果 */
}
.ai-message.first-of-sequence .message-bubble {
  /* 可选：第一个AI气泡特殊处理，如果头像可见 */
}


.message-content {
  font-size: 0.95rem; /* 15.2px */
  line-height: 1.6;
  white-space: pre-wrap; /* 保留换行和空格 */
}

.message-time {
  font-size: 0.75rem; /* 12px */
  opacity: 0.8; /* 调整透明度，使其不那么突出 */
  margin-top: 5px;
  text-align: right;
  color: inherit; /* 继承气泡颜色，AI气泡内是深色，用户气泡内是白色 */
}
.user-message .message-time {
  color: rgba(255,255,255,0.8);
}
.ai-message .message-time {
  color: var(--fufu-text-light);
}


.chat-input-container {
  background-color: var(--fufu-white);
  border-top: 1px solid var(--fufu-border-gray);
  padding: 10px 15px; /* 调整内边距 */
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.03);
}

.chat-input-inner {
  display: flex;
  align-items: flex-end; /* 使输入框和按钮底部对齐 */
  gap: 10px;
  width: 100%;
  max-width: 800px; /* 可选：限制输入区域最大宽度 */
  margin: 0 auto;   /* 如果限制了最大宽度，则居中 */
}

.input-box {
  flex-grow: 1;
  border: 1px solid var(--fufu-border-gray);
  border-radius: 20px;
  padding: 10px 15px;
  font-size: 0.95rem;
  resize: none;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  font-family: 'Inter', sans-serif;
  line-height: 1.5;
  max-height: 100px; /* 最多显示约4-5行 */
  overflow-y: auto; /* 超出则滚动 */
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.input-box::-webkit-scrollbar { display: none; }


.input-box:focus {
  border-color: var(--fufu-primary-blue);
  box-shadow: 0 0 0 3px rgba(114, 191, 244, 0.2);
}

.send-button {
  background-color: var(--fufu-primary-blue);
  color: var(--fufu-white);
  border: none;
  border-radius: 50%; /* 圆形按钮 */
  width: 40px; /* 固定宽高 */
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.2s;
  flex-shrink: 0; /* 防止按钮被压缩 */
  padding:0; /* 移除内边距，SVG会撑开 */
}

.send-button svg {
  fill: var(--fufu-white);
}

.send-button:hover:not(:disabled) {
  background-color: var(--fufu-hover-blue);
}

.input-box:disabled, .send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.typing-indicator {
  display: inline-block;
  animation: blink 0.8s infinite steps(1, start);
  margin-left: 2px;
  color: var(--fufu-primary-blue); /* 指示器颜色 */
}

@keyframes blink {
  50% { opacity: 0; }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .message { max-width: 85%; }
  .message-content { font-size: 0.9rem; }
  .input-box { font-size: 0.9rem; padding: 8px 12px; }
  .send-button { width: 36px; height: 36px; }
  .send-button svg { width: 18px; height: 18px; }
  .chat-input-container { padding: 8px 10px; }
}

@media (max-width: 480px) {
  .avatar { width: 28px; height: 28px; }
  .message-bubble { padding: 8px 12px; }
  .message-content { font-size: 0.875rem; }
  .message-time { font-size: 0.7rem; }
  .ai-avatar { margin-right: 8px; }
  .user-avatar { margin-left: 8px; }
  .input-box { border-radius: 18px; }
  .send-button { border-radius: 50%; }
}

/* 连续AI消息，仅显示第一个头像 */
.ai-message + .ai-message {
  margin-top: 2px; /* 连续消息间距减小 */
}
</style>