import { defineStore } from 'pinia'
import axios from 'axios'
import apiClient from '../api/client'
import { ElMessage } from 'element-plus'
import router from '../router'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: null,
    username: '',
    avatar: '',
    email: '',
    phone: '',
    token: '',
    roles: [],
    balance: 0,
    loginStatus: false,
    isInitialized: false
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token && state.loginStatus,
    userInfo: (state) => ({
      userId: state.userId,
      username: state.username,
      avatar: state.avatar,
      email: state.email,
      phone: state.phone
    })
  },
  
  actions: {
    setUser(user) {
      this.userId = user.userId;
      this.username = user.username;
      this.avatar = user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
      this.email = user.email;
      this.phone = user.phone;
      this.roles = user.roles || [];
      this.balance = user.balance || 0;
    },
    
    setToken(token) {
      this.token = token;
      localStorage.setItem('token', token);
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      this.loginStatus = true;
    },
    
    clearUserInfo() {
      this.userId = null;
      this.username = '';
      this.avatar = '';
      this.email = '';
      this.phone = '';
      this.token = '';
      this.roles = [];
      this.balance = 0;
      this.loginStatus = false;
      localStorage.removeItem('token');
      delete apiClient.defaults.headers.common['Authorization'];
    },
    
    logout() {
      this.clearUserInfo();
      ElMessage.success('已退出登录');
    },
    
    async login(loginData) {
      try {
        console.log('开始登录请求:', loginData);
        const response = await apiClient.post('/users/login', loginData);
        console.log('登录响应数据1232131:', response);
        
        // 查找token (根据实际API响应格式调整)
        let token = '';
        let userId = null;
        let username = '';
    
        // 分析响应结构
        if (response.msg && response.code === 200) {
          // 特殊处理：API将token放在msg字段
         
          if (response.msg && typeof response.msg === 'string') {
            token = response.msg ;
            console.log('从msg字段获取到token');
            

            // 尝试从JWT中解析用户信息
            try {
              const base64Url = token.split('.')[1];
              const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
              const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
              }).join(''));
              
              const payload = JSON.parse(jsonPayload);
              console.log('JWT payload:', payload);
              
              // 从JWT中提取用户ID和用户名
              userId = payload.userId || payload.user_id || payload.sub;
              username = payload.username || payload.sub;
            } catch (e) {
              console.error('解析JWT失败:', e);
            }
          // 标准格式：token在data.token字段
          } else if (response.data.data && response.data.data.token) {
            token = response.data.data.token;
          }
        }
        
        console.log('最终解析到的token:', token);
        
        if (!token) {
          console.error('未获取到有效token，但请求成功');
          ElMessage.warning('登录成功但未获取到有效凭证，请联系管理员');
          return false;
        }
        
        this.setToken(token);
        
        // 使用从JWT中解析的信息或从响应中获取的信息
        const userInfo = {
          userId: userId,
          username: username || loginData.username // 如果无法从JWT获取，就使用登录表单中的用户名
        };
        
        this.setUser(userInfo);
        console.log('设置的用户信息:', userInfo);
        
        ElMessage.success('登录成功');
        return true;
      } catch (error) {
        console.error('登录请求失败:', error);
        ElMessage.error(error.response?.data?.msg || '登录失败，请稍后再试');
        return false;
      }
    },

    async register(registerData) {
      try {
        const response = await apiClient.post('/users/register', registerData);
        ElMessage.success('注册成功，请登录');
        return true;
      } catch (error) {
        console.error('注册请求失败:', error);
        ElMessage.error(error.response?.data?.msg || '注册失败，请稍后再试');
        return false;
      }
    },

    async getUserInfo() {
      if (!this.token) return false;
      
      try {
        const response = await apiClient.get('/users/details');
        this.setUser(response.data);
        return true;
      } catch (error) {
        console.error('获取用户信息失败:', error);
        return false;
      }
    },

    async updateUserProfile(profileData) {
      try {
        const response = await apiClient.put('/users/profile', profileData);
        // 更新本地用户信息
        this.setUser({
          ...this.userInfo,
          ...profileData
        });
        ElMessage.success('个人信息更新成功');
        return true;
      } catch (error) {
        console.error('更新个人信息失败:', error);
        ElMessage.error(error.response?.data?.msg || '更新失败，请稍后再试');
        return false;
      }
    },

    async updatePassword(passwordData) {
      try {
        const response = await apiClient.post('/users/password', passwordData);
        ElMessage.success('密码修改成功，请重新登录');
        this.logout();
        return true;
      } catch (error) {
        console.error('修改密码失败:', error);
        ElMessage.error(error.response?.data?.msg || '修改密码失败，请稍后再试');
        return false;
      }
    },

    // 初始化，从本地存储恢复登录状态
    async initFromStorage() {
      const token = localStorage.getItem('token');
      if (token) {
        this.token = token;
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        this.loginStatus = true;
        await this.getUserInfo(); // 获取最新用户信息
      }
      this.isInitialized = true;
    },
    
    // 恢复会话，从本地存储恢复登录状态并获取用户信息
    async restoreSession() {
      if (this.isInitialized) return;
      
      const token = localStorage.getItem('token');
      if (token) {
        this.token = token;
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        this.loginStatus = true;
        
        try {
          await this.getUserInfo();
        } catch (error) {
          console.error('恢复会话失败，可能是token已过期:', error);
          this.clearUserInfo();
        }
      }
      
      this.isInitialized = true;
    }
  }
}) 