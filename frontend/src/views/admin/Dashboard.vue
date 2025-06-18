<template>
  <div class="dashboard-container">
    <!-- 加载状态覆盖层 -->
    <div v-if="loading" class="loading-overlay">
      <el-icon class="loading-spinner"><Loading /></el-icon>
      <div class="loading-text">数据加载中...</div>
    </div>
    
    <div class="dashboard-header">
      <div class="title">
        <span class="main-title">ShopSphere 数据统计中心</span>
        <span class="sub-title">实时监控 · 数据分析 · 业务决策</span>
      </div>
      <div class="header-right">
        <div class="time-info">
          <div class="date">{{ currentDate }}</div>
          <div class="time">{{ currentTime }}</div>
        </div>
        <el-button class="back-button" type="primary" @click="goBack">
          <el-icon><Back /></el-icon> 返回商城
        </el-button>
      </div>
    </div>
    
    <div class="dashboard-content">
      <!-- 第一行卡片 -->
      <div class="stat-cards">
        <div class="stat-card" v-for="(item, index) in statCards" :key="index">
          <div class="card-value">
            <count-to :end-val="item.value" :duration="2000" />
            <span class="unit">{{ item.unit }}</span>
          </div>
          <div class="card-info">
            <div class="card-title">{{ item.title }}</div>
            <div class="card-trend" :class="{ 'up': item.trend > 0, 'down': item.trend < 0 }">
              <el-icon v-if="item.trend > 0"><ArrowUp /></el-icon>
              <el-icon v-else-if="item.trend < 0"><ArrowDown /></el-icon>
              {{ Math.abs(item.trend) }}%
            </div>
          </div>
          <div class="card-icon">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 第二行图表 -->
      <div class="chart-row">
        <div class="chart-card sales-trend">
          <div class="chart-header">
            <div class="chart-title">销售趋势</div>
            <el-radio-group v-model="salesTimeRange" size="small">
              <el-radio-button value="week">本周</el-radio-button>
              <el-radio-button value="month">本月</el-radio-button>
              <el-radio-button value="year">全年</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-content" ref="salesChartRef"></div>
        </div>
        
        <div class="chart-card category-distribution">
          <div class="chart-header">
            <div class="chart-title">品类分布</div>
          </div>
          <div class="chart-content" ref="categoryChartRef"></div>
        </div>
      </div>
      
      <!-- 第三行图表 -->
      <div class="chart-row">
        <div class="chart-card map-chart">
          <div class="chart-header">
            <div class="chart-title">销售地区分布</div>
          </div>
          <div class="chart-content" ref="mapChartRef"></div>
        </div>
        
        <div class="chart-card user-trend">
          <div class="chart-header">
            <div class="chart-title">用户增长趋势</div>
          </div>
          <div class="chart-content" ref="userChartRef"></div>
        </div>
      </div>
      
      <!-- 第四行表格 -->
      <div class="table-row">
        <div class="table-card">
          <div class="chart-header">
            <div class="chart-title">热销商品TOP10</div>
          </div>
          <el-table :data="topProducts" stripe style="width: 100%">
            <el-table-column prop="rank" label="排名" width="60">
              <template #default="scope">
                <div class="rank-badge" :class="{ 
                  'rank-1': scope.row.rank === 1, 
                  'rank-2': scope.row.rank === 2, 
                  'rank-3': scope.row.rank === 3 
                }">
                  {{ scope.row.rank }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="sales" label="销量" width="120" />
            <el-table-column prop="amount" label="销售额" width="120">
              <template #default="scope">¥{{ scope.row.amount.toLocaleString() }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import { ArrowUp, ArrowDown, GoodsFilled, User, ShoppingCart, Money, Back, Loading } from '@element-plus/icons-vue'
import { useIntervalFn } from '@vueuse/core'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import CountTo from '@/components/CountTo.vue'
import { getShopDashboardOverview, getShopCityDistribution, getShopDashboardTrend } from '@/api/dashboard'

const router = useRouter()

// 返回商城首页
const goBack = () => {
  router.push('/')
}

// 数据加载状态
const loading = ref(true)

// 统计卡片数据
const statCards = ref([
  { title: '今日销售额', value: 0, unit: '元', trend: 0, icon: 'Money' },
  { title: '今日订单数', value: 0, unit: '单', trend: 0, icon: 'ShoppingCart' },
  { title: '新增用户', value: 0, unit: '人', trend: 0, icon: 'User' },
  { title: '商品总数', value: 0, unit: '件', trend: 0, icon: 'GoodsFilled' }
])

// 城市用户分布数据
const cityDistribution = ref([])

// 趋势数据
const trendData = ref([])

// 热销商品数据
const topProducts = ref([
  { rank: 1, name: '华为Mate60 Pro', category: '电子产品', sales: 356, amount: 1246000 },
  { rank: 2, name: '小米14 Ultra', category: '电子产品', sales: 215, amount: 1075000 },
  { rank: 3, name: '智能手表 GT4', category: '穿戴设备', sales: 189, amount: 567000 },
  { rank: 4, name: '华为FreeBuds Pro 3', category: '音频设备', sales: 176, amount: 528000 },
  { rank: 5, name: '投影仪T3', category: '家用电器', sales: 152, amount: 456000 },
  { rank: 6, name: '智能家居套装', category: '智能家居', sales: 147, amount: 441000 },
  { rank: 7, name: '索尼A7M5', category: '摄影器材', sales: 135, amount: 405000 },
  { rank: 8, name: 'PS5游戏主机', category: '游戏设备', sales: 128, amount: 384000 },
  { rank: 9, name: 'iPad Pro M3', category: '电子产品', sales: 119, amount: 357000 },
  { rank: 10, name: '小度智能音箱', category: '音频设备', sales: 104, amount: 312000 }
])

const salesTimeRange = ref('month')

// 日期时间
const currentDate = ref('')
const currentTime = ref('')

const updateDateTime = () => {
  const now = dayjs()
  currentDate.value = now.format('YYYY年MM月DD日 dddd')
  currentTime.value = now.format('HH:mm:ss')
}

// 每秒更新时间
const { pause } = useIntervalFn(updateDateTime, 1000)

// 图表引用
const salesChartRef = ref(null)
const categoryChartRef = ref(null)
const mapChartRef = ref(null)
const userChartRef = ref(null)

// 获取仪表盘总览数据
const fetchDashboardOverview = async () => {
  try {
    const res = await getShopDashboardOverview()
    if (res.code === 200 && res.data) {
      const data = res.data
      
      // 更新统计卡片数据
      statCards.value[0].value = data.paidTotalAmount || 0
      statCards.value[1].value = data.orderCount || 0
      statCards.value[2].value = data.userCount || 0
      statCards.value[3].value = data.productCount || 0
      
      // 模拟趋势变化（实际项目中应从API获取）
      statCards.value[0].trend = 12.5
      statCards.value[1].trend = 8.2
      statCards.value[2].trend = -2.8
      statCards.value[3].trend = 1.2
      
      console.log('获取仪表盘总览数据成功:', data)
    }
  } catch (error) {
    console.error('获取仪表盘总览数据失败:', error)
  }
}

// 获取城市分布数据
const fetchCityDistribution = async () => {
  try {
    const res = await getShopCityDistribution()
    if (res.code === 200 && res.data) {
      cityDistribution.value = res.data
      console.log('获取城市分布数据成功:', res.data)
    }
  } catch (error) {
    console.error('获取城市分布数据失败:', error)
  }
}

// 获取趋势数据
const fetchTrendData = async () => {
  // 计算日期范围
  let startDate, endDate
  const now = dayjs()
  
  switch (salesTimeRange.value) {
    case 'week':
      startDate = now.subtract(6, 'day').format('YYYY-MM-DD')
      endDate = now.format('YYYY-MM-DD')
      break
    case 'month':
      startDate = now.subtract(29, 'day').format('YYYY-MM-DD')
      endDate = now.format('YYYY-MM-DD')
      break
    case 'year':
      startDate = now.subtract(11, 'month').startOf('month').format('YYYY-MM-DD')
      endDate = now.format('YYYY-MM-DD')
      break
  }
  
  try {
    const res = await getShopDashboardTrend(startDate, endDate)
    if (res.code === 200 && res.data) {
      trendData.value = res.data
      console.log('获取趋势数据成功:', res.data)
      
      // 更新趋势图表
      updateSalesChart()
      updateUserChart()
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

// 销售趋势图表
let salesChart = null
const initSalesChart = () => {
  if (!salesChartRef.value) return
  
  salesChart = echarts.init(salesChartRef.value)
  updateSalesChart()
}

const updateSalesChart = () => {
  if (!salesChart) return
  
  // 如果没有趋势数据，使用默认数据
  if (trendData.value.length === 0) {
    const option = {
      grid: {
        top: '5%',
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          crossStyle: {
            color: '#999'
          }
        }
      },
      legend: {
        data: ['订单量', '销售额'],
        right: 10,
        textStyle: {
          color: '#fff'
        }
      },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        axisLine: {
          lineStyle: {
            color: 'rgba(255,255,255,0.3)'
          }
        },
        axisLabel: {
          color: 'rgba(255,255,255,0.7)'
        }
      },
      yAxis: [
        {
          type: 'value',
          name: '订单量',
          splitLine: {
            lineStyle: {
              color: 'rgba(255,255,255,0.1)'
            }
          },
          axisLine: {
            show: true,
            lineStyle: {
              color: 'rgba(255,255,255,0.3)'
            }
          },
          axisLabel: {
            color: 'rgba(255,255,255,0.7)'
          }
        },
        {
          type: 'value',
          name: '销售额',
          splitLine: {
            show: false
          },
          axisLine: {
            show: true,
            lineStyle: {
              color: 'rgba(255,255,255,0.3)'
            }
          },
          axisLabel: {
            color: 'rgba(255,255,255,0.7)',
            formatter: '{value} 元'
          }
        }
      ],
      series: [
        {
          name: '订单量',
          type: 'bar',
          barWidth: '20%',
          data: [120, 132, 101, 134, 90, 230, 210],
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          }
        },
        {
          name: '销售额',
          type: 'line',
          yAxisIndex: 1,
          data: [45000, 49200, 38000, 52300, 36500, 87500, 79800],
          lineStyle: {
            color: '#f5b723'
          },
          itemStyle: {
            color: '#f5b723'
          }
        }
      ]
    }
    
    salesChart.setOption(option)
    return
  }
  
  // 使用API数据
  const dates = trendData.value.map(item => {
    // 根据不同时间范围格式化日期
    if (salesTimeRange.value === 'week' || salesTimeRange.value === 'month') {
      return dayjs(item.date).format('MM-DD')
    } else {
      return dayjs(item.date).format('YYYY-MM')
    }
  })
  
  const orderCounts = trendData.value.map(item => item.orderCount || 0)
  const amounts = trendData.value.map(item => item.paidTotalAmount || 0)
  
  const option = {
    grid: {
      top: '5%',
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    legend: {
      data: ['订单量', '销售额'],
      right: 10,
      textStyle: {
        color: '#fff'
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: {
        lineStyle: {
          color: 'rgba(255,255,255,0.3)'
        }
      },
      axisLabel: {
        color: 'rgba(255,255,255,0.7)',
        rotate: dates.length > 7 ? 45 : 0
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '订单量',
        splitLine: {
          lineStyle: {
            color: 'rgba(255,255,255,0.1)'
          }
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: 'rgba(255,255,255,0.3)'
          }
        },
        axisLabel: {
          color: 'rgba(255,255,255,0.7)'
        }
      },
      {
        type: 'value',
        name: '销售额',
        splitLine: {
          show: false
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: 'rgba(255,255,255,0.3)'
          }
        },
        axisLabel: {
          color: 'rgba(255,255,255,0.7)',
          formatter: '{value} 元'
        }
      }
    ],
    series: [
      {
        name: '订单量',
        type: 'bar',
        barWidth: '20%',
        data: orderCounts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      },
      {
        name: '销售额',
        type: 'line',
        yAxisIndex: 1,
        data: amounts,
        lineStyle: {
          color: '#f5b723'
        },
        itemStyle: {
          color: '#f5b723'
        }
      }
    ]
  }
  
  salesChart.setOption(option)
}

// 品类分布图表
let categoryChart = null
const initCategoryChart = () => {
  if (!categoryChartRef.value) return
  
  categoryChart = echarts.init(categoryChartRef.value)
  
  // 品类分布模拟数据
  const categoryData = [
    { value: 1048, name: '电子产品' },
    { value: 735, name: '服装鞋帽' },
    { value: 580, name: '家居用品' },
    { value: 484, name: '食品饮料' },
    { value: 300, name: '美妆个护' }
  ]
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: {
        color: '#fff'
      }
    },
    series: [
      {
        name: '品类分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#0f1621',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold',
            formatter: '{b}: {c} ({d}%)'
          }
        },
        labelLine: {
          show: false
        },
        data: categoryData
      }
    ]
  }
  
  categoryChart.setOption(option)
}

// 中国地图
let mapChart = null
const initMapChart = async () => {
  if (!mapChartRef.value) return
  
  try {
    // 导入地图加载工具
    const { initMapChart: initMap } = await import('@/utils/map-loader')
    
    // 准备地图数据
    let mapData = []
    
    // 如果有城市分布数据，使用API数据
    if (cityDistribution.value.length > 0) {
      // 地图需要省份数据，这里简单模拟 - 实际项目中可能需要更复杂的映射
      const cityToProvince = {
        '北京': '北京',
        '上海': '上海',
        '广州': '广东',
        '深圳': '广东',
        '南京': '江苏',
        '杭州': '浙江',
        '成都': '四川',
        '武汉': '湖北',
        '西安': '陕西',
        '重庆': '重庆',
        '天津': '天津',
        '青岛': '山东',
        '长沙': '湖南',
        '沈阳': '辽宁',
        '大连': '辽宁'
      }
      
      // 将城市数据转换为省份数据
      const provinceData = {}
      
      cityDistribution.value.forEach(item => {
        const province = cityToProvince[item.city] || '其他'
        if (!provinceData[province]) {
          provinceData[province] = 0
        }
        provinceData[province] += item.userCount
      })
      
      // 转换为地图需要的格式
      mapData = Object.keys(provinceData).map(province => {
        return {
          name: province,
          value: provinceData[province]
        }
      })
    } else {
      // 使用默认数据
      mapData = [
        { name: '北京', value: 2380 },
        { name: '天津', value: 1290 },
        { name: '上海', value: 2570 },
        { name: '重庆', value: 1350 },
        { name: '河北', value: 970 },
        { name: '河南', value: 1250 },
        { name: '云南', value: 785 },
        { name: '辽宁', value: 1050 },
        { name: '黑龙江', value: 689 },
        { name: '湖南', value: 1456 },
        { name: '安徽', value: 985 },
        { name: '山东', value: 1850 },
        { name: '新疆', value: 542 },
        { name: '江苏', value: 2104 },
        { name: '浙江', value: 1967 },
        { name: '江西', value: 756 },
        { name: '湖北', value: 1378 },
        { name: '广西', value: 987 },
        { name: '甘肃', value: 423 },
        { name: '山西', value: 876 },
        { name: '内蒙古', value: 654 },
        { name: '陕西', value: 1121 },
        { name: '吉林', value: 745 },
        { name: '福建', value: 1456 },
        { name: '贵州', value: 632 },
        { name: '广东', value: 2385 },
        { name: '青海', value: 387 },
        { name: '西藏', value: 298 },
        { name: '四川', value: 1583 },
        { name: '宁夏', value: 345 },
        { name: '海南', value: 876 },
        { name: '台湾', value: 1267 },
        { name: '香港', value: 1428 },
        { name: '澳门', value: 967 }
      ]
    }
    
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}<br/>用户数: {c}人'
      },
      visualMap: {
        min: 0,
        max: 2500,
        left: 'left',
        top: 'bottom',
        text: ['高', '低'],
        calculable: true,
        inRange: {
          color: ['#0a2dae', '#1258dc', '#6f94f0', '#a1b9ff']
        },
        textStyle: {
          color: '#fff'
        }
      },
      series: [
        {
          name: '用户分布',
          type: 'map',
          map: 'china',
          roam: true,
          emphasis: {
            label: {
              show: true
            }
          },
          data: mapData
        }
      ]
    }
    
    // 使用地图加载器初始化地图
    mapChart = await initMap(mapChartRef.value, option)
  } catch (error) {
    console.error('初始化地图失败:', error)
  }
}

// 用户增长趋势图表
let userChart = null
const initUserChart = () => {
  if (!userChartRef.value) return
  
  userChart = echarts.init(userChartRef.value)
  updateUserChart()
}

const updateUserChart = () => {
  if (!userChart) return
  
  // 如果没有趋势数据，使用默认数据
  if (trendData.value.length === 0) {
    const option = {
      grid: {
        top: '5%',
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        axisLine: {
          lineStyle: {
            color: 'rgba(255,255,255,0.3)'
          }
        },
        axisLabel: {
          color: 'rgba(255,255,255,0.7)'
        }
      },
      yAxis: {
        type: 'value',
        splitLine: {
          lineStyle: {
            color: 'rgba(255,255,255,0.1)'
          }
        },
        axisLine: {
          lineStyle: {
            color: 'rgba(255,255,255,0.3)'
          }
        },
        axisLabel: {
          color: 'rgba(255,255,255,0.7)'
        }
      },
      series: [
        {
          name: '用户数',
          type: 'line',
          smooth: true,
          areaStyle: {
            opacity: 0.8,
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              {
                offset: 0,
                color: 'rgba(128, 255, 165, 0.7)'
              },
              {
                offset: 1,
                color: 'rgba(1, 191, 236, 0)'
              }
            ])
          },
          emphasis: {
            focus: 'series'
          },
          lineStyle: {
            width: 3
          },
          data: [1200, 1450, 1680, 1890, 2100, 2580, 2890, 3250, 3580, 3950, 4280, 4650]
        }
      ]
    }
    
    userChart.setOption(option)
    return
  }
  
  // 使用API数据
  const dates = trendData.value.map(item => {
    // 根据不同时间范围格式化日期
    if (salesTimeRange.value === 'week' || salesTimeRange.value === 'month') {
      return dayjs(item.date).format('MM-DD')
    } else {
      return dayjs(item.date).format('YYYY-MM')
    }
  })
  
  const userCounts = trendData.value.map(item => item.userCount || 0)
  
  const option = {
    grid: {
      top: '5%',
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: {
        lineStyle: {
          color: 'rgba(255,255,255,0.3)'
        }
      },
      axisLabel: {
        color: 'rgba(255,255,255,0.7)',
        rotate: dates.length > 7 ? 45 : 0
      }
    },
    yAxis: {
      type: 'value',
      splitLine: {
        lineStyle: {
          color: 'rgba(255,255,255,0.1)'
        }
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(255,255,255,0.3)'
        }
      },
      axisLabel: {
        color: 'rgba(255,255,255,0.7)'
      }
    },
    series: [
      {
        name: '用户数',
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(128, 255, 165, 0.7)'
            },
            {
              offset: 1,
              color: 'rgba(1, 191, 236, 0)'
            }
          ])
        },
        emphasis: {
          focus: 'series'
        },
        lineStyle: {
          width: 3
        },
        data: userCounts
      }
    ]
  }
  
  userChart.setOption(option)
}

// 监听时间范围变化
watch(salesTimeRange, () => {
  try {
    fetchTrendData()
  } catch (error) {
    console.error('获取趋势数据失败:', error)
    // 使用模拟数据
    useMockData()
  }
})

// 初始化所有图表
const initCharts = async () => {
  try {
    // 先获取数据
    await fetchDashboardOverview()
    await fetchCityDistribution()
    await fetchTrendData()
    
    // 再初始化图表
    initSalesChart()
    initCategoryChart()
    await initMapChart()
    initUserChart()
    
    // 加载完成
    loading.value = false
  } catch (error) {
    console.error('初始化图表失败:', error)
    // 使用模拟数据
    useMockData()
    // 加载完成
    loading.value = false
  }
}

// 使用模拟数据
const useMockData = () => {
  console.log('使用模拟数据')
  
  // 统计卡片模拟数据
  statCards.value[0].value = 198650
  statCards.value[1].value = 853
  statCards.value[2].value = 321
  statCards.value[3].value = 12763
  
  statCards.value[0].trend = 12.5
  statCards.value[1].trend = 8.2
  statCards.value[2].trend = -2.8
  statCards.value[3].trend = 1.2
  
  // 模拟城市分布数据
  cityDistribution.value = [
    { city: '北京', userCount: 4567 },
    { city: '上海', userCount: 3892 },
    { city: '广州', userCount: 2435 },
    { city: '深圳', userCount: 2103 },
    { city: '成都', userCount: 1876 },
    { city: '杭州', userCount: 1654 },
    { city: '武汉', userCount: 1432 },
    { city: '西安', userCount: 1289 },
    { city: '南京', userCount: 1154 },
    { city: '重庆', userCount: 1087 }
  ]
  
  // 模拟趋势数据 - 按月
  const mockTrendData = []
  const now = dayjs()
  
  // 根据当前选择的时间范围生成不同的模拟数据
  if (salesTimeRange.value === 'week') {
    // 最近7天
    for (let i = 6; i >= 0; i--) {
      const date = now.subtract(i, 'day')
      mockTrendData.push({
        date: date.format('YYYY-MM-DD'),
        orderCount: Math.floor(Math.random() * 100) + 80,
        paidOrderCount: Math.floor(Math.random() * 80) + 60,
        paidTotalAmount: Math.floor(Math.random() * 40000) + 20000,
        totalAmount: Math.floor(Math.random() * 50000) + 25000,
        userCount: Math.floor(Math.random() * 50) + 20
      })
    }
  } else if (salesTimeRange.value === 'month') {
    // 最近30天
    for (let i = 29; i >= 0; i--) {
      const date = now.subtract(i, 'day')
      mockTrendData.push({
        date: date.format('YYYY-MM-DD'),
        orderCount: Math.floor(Math.random() * 120) + 100,
        paidOrderCount: Math.floor(Math.random() * 100) + 80,
        paidTotalAmount: Math.floor(Math.random() * 60000) + 30000,
        totalAmount: Math.floor(Math.random() * 70000) + 35000,
        userCount: Math.floor(Math.random() * 80) + 30
      })
    }
  } else {
    // 全年12个月
    for (let i = 11; i >= 0; i--) {
      const date = now.subtract(i, 'month').startOf('month')
      mockTrendData.push({
        date: date.format('YYYY-MM-DD'),
        orderCount: Math.floor(Math.random() * 2000) + 1500,
        paidOrderCount: Math.floor(Math.random() * 1800) + 1300,
        paidTotalAmount: Math.floor(Math.random() * 800000) + 500000,
        totalAmount: Math.floor(Math.random() * 1000000) + 600000,
        userCount: Math.floor(Math.random() * 1000) + 500
      })
    }
  }
  
  trendData.value = mockTrendData
  
  // 更新图表
  updateSalesChart()
  updateUserChart()
}

// 窗口大小改变时重新绘制图表
const handleResize = () => {
  salesChart && salesChart.resize()
  categoryChart && categoryChart.resize()
  mapChart && mapChart.resize()
  userChart && userChart.resize()
}

onMounted(() => {
  // 更新日期时间
  updateDateTime()
  
  // 初始化图表，需要确保DOM已经渲染
  setTimeout(async () => {
    await initCharts()
  }, 0)
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  // 清除定时器
  pause()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
  
  // 销毁图表实例
  salesChart && salesChart.dispose()
  categoryChart && categoryChart.dispose()
  mapChart && mapChart.dispose()
  userChart && userChart.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  height: 100%;
  background-color: #0f1621;
  color: #fff;
  overflow: auto;
  position: relative;
  
  /* 加载状态覆盖层 */
  .loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(15, 22, 33, 0.9);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    
    .loading-spinner {
      font-size: 32px;
      color: #3a7bd5;
      animation: spin 1.5s linear infinite;
    }
    
    @keyframes spin {
      0% {
        transform: rotate(0deg);
      }
      100% {
        transform: rotate(360deg);
      }
    }
    
    .loading-text {
      margin-top: 15px;
      font-size: 16px;
      color: #8eb0e5;
    }
  }
  
  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: linear-gradient(90deg, rgba(16, 28, 45, 0.8) 0%, rgba(18, 31, 53, 0.9) 100%);
    border-bottom: 1px solid rgba(58, 123, 213, 0.3);
    
    .title {
      display: flex;
      flex-direction: column;
      
      .main-title {
        font-size: 24px;
        font-weight: bold;
        color: #3a7bd5;
        text-shadow: 0 0 10px rgba(58, 123, 213, 0.5);
        letter-spacing: 1px;
      }
      
      .sub-title {
        font-size: 14px;
        color: #8eb0e5;
        margin-top: 5px;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      
      .time-info {
        text-align: right;
        
        .date {
          font-size: 16px;
          color: #8eb0e5;
        }
        
        .time {
          font-size: 24px;
          font-weight: bold;
          color: #3a7bd5;
          margin-top: 5px;
        }
      }
      
      .back-button {
        margin-left: 20px;
      }
    }
  }
  
  .dashboard-content {
    padding: 20px;
    
    .stat-cards {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20px;
      margin-bottom: 20px;
      
      .stat-card {
        position: relative;
        background: linear-gradient(135deg, rgba(38, 57, 85, 0.7) 0%, rgba(26, 41, 65, 0.7) 100%);
        border-radius: 10px;
        padding: 20px;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
        border: 1px solid rgba(58, 123, 213, 0.2);
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 3px;
          background: linear-gradient(90deg, #3a7bd5, #00d2ff);
        }
        
        .card-value {
          font-size: 28px;
          font-weight: bold;
          color: #fff;
          margin-bottom: 10px;
          
          .unit {
            font-size: 14px;
            margin-left: 5px;
            opacity: 0.7;
          }
        }
        
        .card-info {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .card-title {
            font-size: 14px;
            color: rgba(255, 255, 255, 0.7);
          }
          
          .card-trend {
            display: flex;
            align-items: center;
            font-size: 12px;
            
            &.up {
              color: #52c41a;
            }
            
            &.down {
              color: #ff4d4f;
            }
            
            .el-icon {
              margin-right: 2px;
            }
          }
        }
        
        .card-icon {
          position: absolute;
          right: 20px;
          top: 20px;
          font-size: 36px;
          opacity: 0.2;
          color: #3a7bd5;
        }
      }
    }
    
    .chart-row {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 20px;
      margin-bottom: 20px;
      
      .chart-card {
        background: linear-gradient(135deg, rgba(38, 57, 85, 0.7) 0%, rgba(26, 41, 65, 0.7) 100%);
        border-radius: 10px;
        padding: 20px;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
        border: 1px solid rgba(58, 123, 213, 0.2);
        
        .chart-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 15px;
          
          .chart-title {
            font-size: 16px;
            font-weight: bold;
            color: rgba(255, 255, 255, 0.9);
          }
        }
        
        .chart-content {
          height: 300px;
        }
      }
    }
    
    .table-row {
      margin-bottom: 20px;
      
      .table-card {
        background: linear-gradient(135deg, rgba(38, 57, 85, 0.7) 0%, rgba(26, 41, 65, 0.7) 100%);
        border-radius: 10px;
        padding: 20px;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
        border: 1px solid rgba(58, 123, 213, 0.2);
        
        .chart-header {
          margin-bottom: 15px;
          
          .chart-title {
            font-size: 16px;
            font-weight: bold;
            color: rgba(255, 255, 255, 0.9);
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr) !important;
  }
  
  .chart-row {
    grid-template-columns: 1fr !important;
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: 1fr !important;
  }
}

// 自定义Element Plus表格样式
:deep(.el-table) {
  background-color: transparent !important;
  color: #fff !important;
  
  &::before {
    display: none;
  }
  
  .el-table__header-wrapper th {
    background-color: rgba(58, 123, 213, 0.2) !important;
    color: rgba(255, 255, 255, 0.9) !important;
    border-bottom: 1px solid rgba(58, 123, 213, 0.5) !important;
  }
  
  .el-table__row {
    background-color: transparent !important;
    
    &:hover > td {
      background-color: rgba(58, 123, 213, 0.2) !important;
    }
    
    &.el-table__row--striped > td {
      background-color: rgba(0, 0, 0, 0.15) !important;
    }
  }
  
  td {
    border-bottom: 1px solid rgba(58, 123, 213, 0.2) !important;
  }
}

// 排名徽章样式
.rank-badge {
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 50%;
  font-weight: bold;
  margin: 0 auto;
  background-color: rgba(255, 255, 255, 0.1);
  
  &.rank-1 {
    background: linear-gradient(135deg, #FFD700, #FFA500);
    color: #000;
    box-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
  }
  
  &.rank-2 {
    background: linear-gradient(135deg, #C0C0C0, #A9A9A9);
    color: #000;
    box-shadow: 0 0 8px rgba(192, 192, 192, 0.5);
  }
  
  &.rank-3 {
    background: linear-gradient(135deg, #CD7F32, #A0522D);
    color: #fff;
    box-shadow: 0 0 8px rgba(205, 127, 50, 0.5);
  }
}
</style> 