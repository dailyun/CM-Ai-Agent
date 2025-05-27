<template>
  <div class="ai-avatar-fallback" :class="typeClass">
    <span v-if="props.type === 'love'">FU</span> <span v-else-if="props.type === 'super'">AI</span> <span v-else>FU</span> </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  type: {
    type: String,
    default: 'default' // 'love', 'super', 'default'
  }
});

// 根据type选择不同的class，虽然当前样式统一，但保留了未来扩展性
const typeClass = computed(() => {
  if (props.type === 'love') return 'type-love';
  if (props.type === 'super') return 'type-super';
  return 'type-default';
});
</script>

<style scoped>
/* 引入字体 (如果全局已引入，此处可选) */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@500&display=swap');

:root { /* 定义组件内也可能用到的颜色变量，确保一致性 */
  --fufu-primary-blue: #72BFF4;
  --fufu-light-blue: #E0F2FF;
  --fufu-white: #FFFFFF;
  --fufu-text-dark-on-light-bg: #33475B; /* 深色文字用在浅色背景上 */
}

.ai-avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px; /* 根据头像大小调整 */
  font-weight: 500;
  font-family: 'Inter', sans-serif;
  border-radius: 50%;
  background-color: var(--fufu-light-blue); /* 统一用浅蓝色背景 */
  color: var(--fufu-primary-blue); /* 图标/文字用主蓝色 */
  text-transform: uppercase; /* 可选，让文字更大气一点 */
  line-height: 1; /* 确保文字垂直居中 */
}

/* 保留类型类，以便未来需要针对不同类型进行微调 */
.type-love {
  /* background-color: var(--fufu-light-blue); */
  /* color: var(--fufu-primary-blue); */
}

.type-default,
.type-super {
  /* background-color: var(--fufu-light-blue); */
  /* color: var(--fufu-primary-blue); */
}
</style>