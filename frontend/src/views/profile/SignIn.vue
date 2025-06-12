<template>
  <div class="sign-in-container">
    <el-card class="sign-in-card">
      <template #header>
        <div class="card-header">
          <h2>每日签到</h2>
          <el-button 
            :type="signInStatus.todaySigned ? 'success' : 'primary'"
            :disabled="signInStatus.todaySigned" 
            :loading="signingIn"
            :class="{'signed-button': signInStatus.todaySigned}"
            @click="handleSignIn"
          >
            {{ signInStatus.todaySigned ? '今日已签到' : '立即签到' }}
          </el-button>
        </div>
      </template>
      
      <div class="sign-in-stats">
        <div class="stat-item">
          <div class="stat-value">{{ signInStatus.continuousDays }}</div>
          <div class="stat-label">连续签到</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ signInStatus.monthDays }}</div>
          <div class="stat-label">本月签到</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ signInStatus.totalDays }}</div>
          <div class="stat-label">总签到天数</div>
        </div>
      </div>
      
      <div class="sign-in-calendar">
        <h3>{{ currentYear }}年{{ currentMonth }}月签到日历</h3>
        <div class="calendar-container">
          <div class="calendar-header">
            <div v-for="day in weekDays" :key="day" class="calendar-day-name">{{ day }}</div>
          </div>
          <div class="calendar-body">
            <div 
              v-for="(day, index) in calendarDays" 
              :key="index" 
              class="calendar-day"
              :class="{
                'empty': !day.date,
                'signed': day.signed,
                'today': day.isToday,
                'disabled': day.disabled
              }"
            >
              <span v-if="day.date">{{ day.date }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const signingIn = ref(false)

// 签到状态
const signInStatus = computed(() => userStore.signInStatus)

// 获取当前年月
const today = new Date()
const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth() + 1)

// 星期名称
const weekDays = ['日', '一', '二', '三', '四', '五', '六']

// 生成日历数据
const calendarDays = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)
  const daysInMonth = lastDay.getDate()
  const firstDayOfWeek = firstDay.getDay()
  
  // 创建一个包含当月所有天的数组
  const days = []
  
  // 填充月初的空白天
  for (let i = 0; i < firstDayOfWeek; i++) {
    days.push({ date: null })
  }
  
  // 填充当月的天
  for (let i = 1; i <= daysInMonth; i++) {
    const currentDate = new Date(year, month - 1, i)
    const isToday = currentDate.getDate() === today.getDate() && 
                    currentDate.getMonth() === today.getMonth() && 
                    currentDate.getFullYear() === today.getFullYear()
    
    // 检查是否已签到
    const signed = userStore.signInCalendar[i] === true
    
    // 未来的日期不可签到
    const disabled = currentDate > today
    
    days.push({
      date: i,
      signed,
      isToday,
      disabled
    })
  }
  
  return days
})

// 执行签到
const handleSignIn = async () => {
  if (signInStatus.value.todaySigned) {
    ElMessage.info('今天已经签到过了')
    return
  }
  
  signingIn.value = true
  try {
    const success = await userStore.signIn()
    if (success) {
      // 签到成功后播放动画或显示奖励信息
      playSignInAnimation()
    }
  } catch (error) {
    console.error('签到失败:', error)
    // 处理已签到的情况
    if (error.response?.data?.code === 500 && error.response?.data?.msg === '今天已经签到过了') {
      // 更新签到状态为已签到
      userStore.signInStatus.todaySigned = true
      ElMessage.info('今天已经签到过了')
    } else {
      ElMessage.error('签到失败，请稍后再试')
    }
  } finally {
    signingIn.value = false
  }
}

// 签到成功动画
const playSignInAnimation = () => {
  // 这里可以添加签到成功的动画效果
  // 例如使用confetti库实现撒花效果
}

// 初始化数据
onMounted(async () => {
  try {
    // 初始化签到数据
    await userStore.initSignInData()
  } catch (error) {
    console.error('初始化签到页面数据失败:', error)
  }
})
</script>

<style scoped>
.sign-in-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.sign-in-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.sign-in-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--el-color-primary);
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-top: 5px;
}

.sign-in-calendar {
  margin-top: 20px;
}

.sign-in-calendar h3 {
  margin-bottom: 15px;
  font-size: 16px;
  text-align: center;
}

.calendar-container {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background-color: var(--el-color-primary-light-9);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.calendar-day-name {
  padding: 10px 0;
  text-align: center;
  font-weight: bold;
  font-size: 14px;
}

.calendar-body {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.calendar-day {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid var(--el-border-color-lighter);
  border-bottom: 1px solid var(--el-border-color-lighter);
  position: relative;
}

.calendar-day:nth-child(7n) {
  border-right: none;
}

.calendar-day.empty {
  background-color: var(--el-fill-color-lighter);
}

.calendar-day.signed {
  background-color: var(--el-color-success-light-9);
}

.calendar-day.signed::after {
  content: '✓';
  position: absolute;
  top: 5px;
  right: 5px;
  color: var(--el-color-success);
  font-size: 12px;
}

.calendar-day.today {
  color: var(--el-color-primary);
  font-weight: bold;
}

.calendar-day.disabled {
  color: var(--el-text-color-placeholder);
}

.signed-button {
  opacity: 0.8;
  cursor: not-allowed;
}
</style> 