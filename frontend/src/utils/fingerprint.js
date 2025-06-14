/**
 * 浏览器指纹生成工具
 * 用于生成唯一的浏览器标识，帮助后端跟踪用户浏览行为
 */

/**
 * 生成简单的浏览器指纹
 * 基于用户代理、屏幕分辨率、时区、语言等信息
 * @returns {string} 生成的指纹哈希值
 */
export function generateFingerprint() {
  try {
    // 收集浏览器信息
    const components = [
      navigator.userAgent,
      navigator.language,
      new Date().getTimezoneOffset(),
      screen.width + 'x' + screen.height,
      screen.colorDepth,
      navigator.platform,
      !!navigator.cookieEnabled,
      !!navigator.doNotTrack,
      typeof window.indexedDB !== 'undefined',
      typeof window.localStorage !== 'undefined',
      typeof window.sessionStorage !== 'undefined',
    ];

    // 添加已安装的插件信息
    if (navigator.plugins) {
      const pluginsStr = Array.from(navigator.plugins)
        .map(p => p.name)
        .join(',');
      components.push(pluginsStr);
    }

    // 将所有组件连接成一个字符串
    const fingerprint = components.join('|||');
    
    // 使用简单的哈希函数生成指纹
    return simpleHash(fingerprint);
  } catch (error) {
    console.error('生成浏览器指纹时出错:', error);
    // 返回一个随机值作为备用
    return 'fp_' + Math.random().toString(36).substring(2, 15);
  }
}

/**
 * 简单的字符串哈希函数
 * @param {string} str 要哈希的字符串
 * @returns {string} 哈希结果
 */
function simpleHash(str) {
  let hash = 0;
  if (str.length === 0) return hash.toString(16);
  
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; // Convert to 32bit integer
  }
  
  // 转换为16进制字符串并添加前缀
  return 'fp_' + (hash >>> 0).toString(16);
}

/**
 * 获取持久化的指纹
 * 首先尝试从localStorage获取，如果不存在则生成新的并保存
 * @returns {string} 指纹值
 */
export function getFingerprint() {
  try {
    // 尝试从localStorage获取已存储的指纹
    const storedFingerprint = localStorage.getItem('browser_fingerprint');
    
    if (storedFingerprint) {
      return storedFingerprint;
    }
    
    // 如果不存在，生成新的指纹并存储
    const newFingerprint = generateFingerprint();
    localStorage.setItem('browser_fingerprint', newFingerprint);
    
    return newFingerprint;
  } catch (error) {
    console.error('获取指纹时出错:', error);
    // 如果出错（例如localStorage被禁用），则每次返回新生成的指纹
    return generateFingerprint();
  }
}

export default {
  generateFingerprint,
  getFingerprint
}; 