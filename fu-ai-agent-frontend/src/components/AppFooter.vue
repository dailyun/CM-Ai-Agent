<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-main-info">
        <div class="footer-logo">
          <h3>FUFU智能体</h3> </div>
        <div class="footer-links">
          <a href="#">用户协议</a>
          <a href="#">隐私政策</a>
        </div>
      </div>
    </div>
    <div class="footer-bottom">
      <p>© {{ currentYear }} FUFU智能体. All Rights Reserved.</p> </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500&display=swap');

:root {
  --fufu-primary-blue: #72BFF4;
  --fufu-white: #FFFFFF;
  --fufu-border-gray: #E2E8F0;
  --fufu-text-dark: #33475B;
  --fufu-text-medium: #677A8C;
  --fufu-text-light: #A0AEC0;
}

.app-footer {
  background-color: var(--fufu-white);
  padding: 15px 20px 10px 20px; /* 大幅减少垂直内边距 */
  color: var(--fufu-text-medium);
  border-top: 1px solid var(--fufu-border-gray);
  width: 100%;
  font-family: 'Inter', sans-serif;
  flex-shrink: 0; /* 防止页脚被压缩 */
  text-align: center; /* 内容居中 */
}

.footer-content {
  max-width: 1100px;
  margin: 0 auto;
  /* display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;  *移除复杂flex，简化为堆叠或内联块* */
  margin-bottom: 8px; /* 底部内容与copyright的间距 */
}

.footer-main-info {
  display: flex;
  flex-direction: column; /* 小屏幕上垂直堆叠 */
  align-items: center;
  gap: 8px; /* 各元素间的小间距 */
}

@media (min-width: 600px) { /* 在稍大屏幕上可以水平排列 */
  .footer-main-info {
    flex-direction: row;
    justify-content: center;
    gap: 20px;
  }
}

.footer-logo h3 {
  font-size: 1rem; /* 减小字体 */
  font-weight: 500;
  margin: 0; /* 移除外边距 */
  color: var(--fufu-text-dark);
}

.footer-links {
  display: flex;
  gap: 15px; /* 链接之间的间距 */
}

.footer-links a {
  color: var(--fufu-text-medium);
  text-decoration: none;
  transition: color 0.2s ease-in-out;
  font-size: 0.8rem; /* 减小字体 */
}

.footer-links a:hover {
  color: var(--fufu-primary-blue);
}

.footer-bottom {
  padding-top: 8px; /* 与上方内容的间距 */
  /* margin-top: 8px; * margin-bottom已处理间距 */
  border-top: 1px solid var(--fufu-border-gray); /* 如果footer-content不为空，则保留此分隔线 */
  color: var(--fufu-text-light);
  font-size: 0.75rem; /* 减小字体 */
}

/* 如果footer-content为空（例如在超小屏幕隐藏了logo和links），则隐藏分隔线 */
.footer-content:empty + .footer-bottom {
  border-top: none;
  padding-top: 0;
}


/* 针对非常小的屏幕，可以进一步简化，例如只显示版权 */
@media (max-width: 480px) {
  .app-footer {
    padding: 10px 15px;
  }
  .footer-main-info {
    /* 此时已是flex-direction: column */
  }
  .footer-logo h3 {
    font-size: 0.9rem;
  }
  .footer-links a {
    font-size: 0.75rem;
  }
  .footer-links {
    gap: 10px;
  }
  .footer-bottom {
    font-size: 0.7rem;
  }
}
</style>