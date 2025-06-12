<template>
  <el-footer height="70px" class="main-footer glass-nav">
    <div class="footer-content">
      <p>&copy; {{ currentYear }} ShopSphere. All rights reserved.</p>
      <div class="social-links">
        <a href="#" target="_blank" aria-label="WeChat"><el-icon><ChatDotRound /></el-icon></a>
        <a href="#" target="_blank" aria-label="Weibo"><el-icon><Share /></el-icon></a>
        <a href="#" target="_blank" aria-label="QQ"><el-icon><Message /></el-icon></a>
      </div>
    </div>
  </el-footer>
</template>

<script setup>
import { ref } from 'vue'
import { ChatDotRound, Share, Message } from '@element-plus/icons-vue'

const currentYear = ref(new Date().getFullYear())
</script>

<style scoped>
.main-footer {
  padding: 20px 40px;
  text-align: center;
  font-size: 14px;
  color: #fff;
}

.footer-content {
  max-width: 1400px;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.social-links {
  display: flex;
  gap: 15px;
  margin-top: 5px;
}

.social-links a {
  color: #fff;
  font-size: 20px;
  transition: all 0.3s ease;
}

.social-links a:hover {
  color: rgba(255, 255, 255, 0.9);
  transform: translateY(-3px);
}
</style> 