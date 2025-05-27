<template>
  <div class="home-container">
    <div class="header">
      <div class="title-wrapper">
        <h1 class="main-title">FUFU智能体</h1>
      </div>
      <p class="subtitle">/ 探索FUFU的无限可能 /</p>
      <div class="separator-line"></div>
    </div>

    <div class="apps-container">
      <div class="app-card" @click="navigateTo('/fufu-master')">
        <div class="app-icon">
          <span>✨</span> </div>
        <div class="app-info">
          <div class="app-title">FUFU大师</div>
          <div class="app-desc">智能顾问，帮你解答烦恼</div>
        </div>
        <div class="app-button">
          <span class="btn-text">立即体验</span>
          <span class="btn-icon">→</span>
        </div>
      </div>

      <div class="app-card" @click="navigateTo('/super-agent')">
        <div class="app-icon">
          <span>🚀</span> </div>
        <div class="app-info">
          <div class="app-title">AI智能体</div>
          <div class="app-desc">全能型AI助手，解决各类专业问题</div>
        </div>
        <div class="app-button">
          <span class="btn-text">立即体验</span>
          <span class="btn-icon">→</span>
        </div>
      </div>
    </div>

    <div class="decorative-circles">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppFooter from '../components/AppFooter.vue' // 确保路径正确

// 设置页面标题和元数据
useHead({
  title: 'FUFU智能体应用平台 - 首页',
  meta: [
    {
      name: 'description',
      content: 'FUFU智能体应用平台，清新明亮的AI体验，提供FUFU大师和AI超级智能体服务。'
    },
    {
      name: 'keywords',
      content: 'FUFU智能体, AI助手, 智能顾问, 清新风格AI'
    }
  ]
})

const router = useRouter()

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
/* 引入推荐字体 Inter */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

/* FUFU 清新明亮风格色板 */
:root {
  --fufu-primary-blue: #72BFF4; /* 清爽的天蓝色 - 用于强调元素 */
  --fufu-hover-blue: #5DA0D9;   /* 主蓝色的悬停加深色 */
  --fufu-light-blue: #E0F2FF;  /* 极浅的婴儿蓝 - 可用于背景或点缀 */
  --fufu-white: #FFFFFF;        /* 纯白色 - 主要背景色，卡片背景 */
  --fufu-light-gray: #F4F7F9;  /* 浅灰色 - 用于次要背景、分割线 */
  --fufu-border-gray: #E2E8F0; /* 边框灰 */
  --fufu-text-dark: #33475B;   /* 深蓝灰色 - 主要文字颜色 */
  --fufu-text-medium: #677A8C;  /* 中灰色 - 次要文字、描述文字 */
  --fufu-text-light: #A0AEC0;   /* 浅灰色 - 辅助性文字 */
}

.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--fufu-white); /* 大面积白色背景 */
  /* 或者使用极浅的蓝色作为背景: background-color: var(--fufu-light-blue); */
  position: relative;
  overflow-x: hidden; /* 防止横向滚动 */
  font-family: 'Inter', sans-serif; /* 全局应用新字体 */
}

/* 头部区域 */
.header {
  padding: 60px 20px 40px; /* 调整内边距 */
  text-align: center;
  position: relative;
  z-index: 2;
}

.title-wrapper { /* 替换 .glitch-wrapper */
  position: relative;
  display: inline-block;
  margin-bottom: 15px; /* 调整间距 */
}

.main-title { /* 替换 .glitch-title */
  font-size: 2.8rem;
  font-weight: 700;
  color: var(--fufu-primary-blue);
  text-shadow: none; /* 移除旧阴影 */
  letter-spacing: 1px;
  /* 移除 glitch 相关动画和伪元素 */
}

.subtitle {
  font-size: 1.1rem;
  color: var(--fufu-text-medium);
  max-width: 600px;
  margin: 0 auto 25px; /* 调整间距 */
  letter-spacing: 0.5px;
  text-transform: none; /* 移除大写，更柔和 */
}

.separator-line { /* 替换 .cyber-line */
  height: 2px;
  width: 120px; /* 调整宽度 */
  margin: 0 auto;
  background: var(--fufu-primary-blue);
  border-radius: 1px;
}

/* 应用卡片区域 */
.apps-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 40px; /* 调整卡片间距 */
  max-width: 900px; /* 调整最大宽度 */
  margin: 40px auto; /* 调整外边距 */
  padding: 0 20px;
  flex: 1;
  position: relative;
  z-index: 2;
}

.app-card {
  width: 300px; /* 调整卡片宽度 */
  background-color: var(--fufu-white);
  border-radius: 16px; /* 圆角卡片 */
  box-shadow: 0 6px 18px rgba(114, 191, 244, 0.15); /* 清新风格阴影 */
  border: 1px solid var(--fufu-border-gray); /* 浅灰色边框 */
  padding: 25px;
  cursor: pointer;
  transition: all 0.3s ease-in-out;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center; /* 卡片内容居中 */
}

.app-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 10px 25px rgba(114, 191, 244, 0.25);
}

.app-icon {
  font-size: 2.5rem; /* 适用于临时占位符，SVG/IMG需自行调整大小 */
  margin-bottom: 20px;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px; /* 圆角矩形图标背景 */
  background: var(--fufu-light-blue);
  color: var(--fufu-primary-blue);
  /* 如果使用图片或SVG: */
  /* background: transparent; */
}
/* 移除 .love-icon, .robot-icon 特定样式 */

.app-info {
  margin-bottom: 25px;
  width: 100%;
}

.app-title {
  font-size: 1.5rem; /* 调整字体大小 */
  font-weight: 600;
  color: var(--fufu-text-dark);
  margin-bottom: 10px;
}

.app-desc {
  font-size: 0.9rem;
  color: var(--fufu-text-medium);
  line-height: 1.6;
}

.app-button {
  background: var(--fufu-primary-blue);
  color: var(--fufu-white);
  padding: 10px 24px;
  border-radius: 20px; /* 圆角按钮 */
  font-weight: 500;
  transition: all 0.3s ease;
  margin-top: auto; /* 使按钮在卡片底部对齐（如果内容高度不一） */
  display: flex;
  align-items: center;
  justify-content: center; /* 按钮内容居中 */
  border: none;
  text-decoration: none; /* 移除可能的下划线 */
}

.app-button:hover {
  background: var(--fufu-hover-blue);
  box-shadow: 0 4px 12px rgba(114, 191, 244, 0.3);
  transform: scale(1.03);
}

.btn-text {
  margin-right: 8px;
  letter-spacing: 0.5px;
}

.btn-icon {
  font-size: 1.1rem;
  transition: transform 0.3s ease;
}

.app-button:hover .btn-icon {
  transform: translateX(3px);
}

/* 背景装饰圆圈 (简化版) */
.decorative-circles { /* 替换 .cyber-circles */
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0; /* 在内容之下 */
  pointer-events: none; /* 不干扰鼠标事件 */
}

.circle {
  position: absolute;
  border-radius: 50%;
  animation: gentle-float 20s infinite alternate ease-in-out;
}

.circle-1 {
  width: 250px;
  height: 250px;
  top: -80px;
  right: -100px;
  background: radial-gradient(circle, rgba(114, 191, 244, 0.15) 0%, rgba(114, 191, 244, 0) 70%);
}

.circle-2 {
  width: 350px;
  height: 350px;
  bottom: -120px;
  left: -150px;
  background: radial-gradient(circle, rgba(224, 242, 255, 0.5) 0%, rgba(224, 242, 255, 0) 70%);
  animation-duration: 25s; /* 动画时长变化 */
  animation-direction: alternate-reverse;
}
/* .circle-3 (已在HTML中注释掉) */

/* 移除旧的故障动画 */
/* @keyframes glitch, @keyframes glitch-anim, @keyframes glitch-anim-2 */

/* 新的柔和漂浮动画 */
@keyframes gentle-float {
  0% {
    transform: translate(0, 0) rotate(0deg);
    opacity: 0.5;
  }
  50% {
    opacity: 0.8;
  }
  100% {
    transform: translate(20px, -30px) rotate(15deg);
    opacity: 0.4;
  }
}

/* 响应式设计 (基于原有断点，可按需细调) */
@media (max-width: 768px) {
  .main-title {
    font-size: 2.3rem;
  }
  .subtitle {
    font-size: 1rem;
  }
  .apps-container {
    gap: 30px;
    margin: 30px auto;
    max-width: 100%;
  }
  .app-card {
    width: 90%; /* 在小屏幕上卡片更宽 */
    max-width: 380px;
    padding: 20px;
  }
  .app-icon {
    width: 60px;
    height: 60px;
    font-size: 2rem;
  }
  .decorative-circles {
    display: none; /* 在小屏幕上可以考虑隐藏背景动画以提升性能和简洁度 */
  }
}

@media (max-width: 480px) {
  .header {
    padding: 40px 15px 30px;
  }
  .main-title {
    font-size: 2rem;
  }
  .subtitle {
    font-size: 0.9rem;
    letter-spacing: 0.2px;
  }
  .apps-container {
    margin: 20px auto;
    padding: 0 15px;
  }
  .app-card {
    padding: 20px;
  }
  .app-icon {
    width: 50px;
    height: 50px;
    font-size: 1.8rem;
    margin-bottom: 15px;
  }
  .app-title {
    font-size: 1.3rem;
  }
  .app-desc {
    font-size: 0.85rem;
  }
  .app-button {
    padding: 8px 20px;
    font-size: 0.9rem;
  }
}
</style>