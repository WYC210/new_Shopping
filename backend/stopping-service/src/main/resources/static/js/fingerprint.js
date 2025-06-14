// 浏览器指纹生成工具
(function() {
    // 简单的浏览器指纹生成函数
    function generateFingerprint() {
        const userAgent = navigator.userAgent;
        const screenWidth = window.screen.width;
        const screenHeight = window.screen.height;
        const colorDepth = window.screen.colorDepth;
        const timezone = new Date().getTimezoneOffset();
        const language = navigator.language || navigator.userLanguage;
        const platform = navigator.platform;
        const plugins = Array.from(navigator.plugins || []).map(p => p.name).join(',');
        
        // 组合这些信息并生成一个哈希值
        const rawFingerprint = `${userAgent}|${screenWidth}x${screenHeight}|${colorDepth}|${timezone}|${language}|${platform}|${plugins}`;
        
        // 简单的哈希函数
        function simpleHash(str) {
            let hash = 0;
            if (str.length === 0) return hash;
            for (let i = 0; i < str.length; i++) {
                const char = str.charCodeAt(i);
                hash = ((hash << 5) - hash) + char;
                hash = hash & hash; // 转换为32位整数
            }
            return Math.abs(hash).toString(16);
        }
        
        return simpleHash(rawFingerprint);
    }
    
    // 生成指纹并存储到localStorage
    function initFingerprint() {
        let fingerprint = localStorage.getItem('browser_fingerprint');
        if (!fingerprint) {
            fingerprint = generateFingerprint();
            localStorage.setItem('browser_fingerprint', fingerprint);
        }
        return fingerprint;
    }
    
    // 初始化指纹
    const fingerprint = initFingerprint();
    
    // 添加到全局变量，方便调试
    window.getFingerprint = function() {
        return fingerprint;
    };
    
    // 拦截所有XMLHttpRequest请求，添加指纹头
    const originalXhrOpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function() {
        const originalSend = this.send;
        this.send = function() {
            this.setRequestHeader('X-Fingerprint', fingerprint);
            return originalSend.apply(this, arguments);
        };
        return originalXhrOpen.apply(this, arguments);
    };
    
    // 如果使用fetch API，也添加指纹头
    const originalFetch = window.fetch;
    window.fetch = function(url, options) {
        options = options || {};
        options.headers = options.headers || {};
        
        // 确保headers是Headers对象
        if (!(options.headers instanceof Headers)) {
            options.headers = new Headers(options.headers);
        }
        
        options.headers.set('X-Fingerprint', fingerprint);
        return originalFetch.call(this, url, options);
    };
    
    // 如果使用axios，在这里添加拦截器
    if (window.axios) {
        window.axios.interceptors.request.use(config => {
            config.headers['X-Fingerprint'] = fingerprint;
            return config;
        });
    }
    
    console.log('Browser fingerprint initialized:', fingerprint);
})(); 