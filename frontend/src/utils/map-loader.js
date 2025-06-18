/**
 * 地图加载器 - 负责加载中国地图数据
 */
import * as echarts from 'echarts'

/**
 * 动态加载中国地图
 * @returns {Promise} 加载完成的Promise
 */
export function loadChinaMap() {
  return new Promise((resolve, reject) => {
    if (echarts.getMap('china')) {
      // 如果已加载地图，直接返回
      resolve()
      return
    }

    // 确保全局 window 对象上有 echarts 实例，供 china.js 使用
    window.echarts = echarts

    // 动态创建script标签加载地图
    const script = document.createElement('script')
    script.src = '/static/china.js'
    document.body.appendChild(script)
    
    script.onload = () => {
      // 检查地图是否注册成功（在 china.js 中注册为 'china1'）
      if (echarts.getMap('china1')) {
        // 将 'china1' 重命名为 'china' 以便在组件中使用
        const chinaMap = echarts.getMap('china1')
        echarts.registerMap('china', chinaMap)
        resolve()
      } else {
        reject(new Error('加载中国地图失败，china.js加载成功但地图未注册'))
      }
    }
    
    script.onerror = () => {
      reject(new Error('加载中国地图失败，无法加载china.js文件'))
    }
  })
}

/**
 * 初始化地图图表
 * @param {HTMLElement} container 容器元素
 * @param {Object} options 图表配置项
 * @returns {echarts.ECharts} 图表实例
 */
export async function initMapChart(container, options = {}) {
  try {
    // 先加载地图数据
    await loadChinaMap()
    
    // 初始化图表
    const chart = echarts.init(container)
    chart.setOption(options)
    
    return chart
  } catch (error) {
    console.error('初始化地图图表失败:', error)
    throw error
  }
} 