import { defineStore } from 'pinia'
import axios from 'axios'
import apiClient from '../api/client'
import { ElMessage } from 'element-plus'
import router from '../router'
import { userApi } from '../api/user'

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
    isInitialized: false,
    // 签到相关状态
    signInStatus: {
      todaySigned: false,
      continuousDays: 0,
      totalDays: 0,
      monthDays: 0
    },
    signInCalendar: {}
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token && state.loginStatus,
    userInfo: (state) => ({
      userId: state.userId,
      username: state.username,
      avatar: state.avatar,
      email: state.email,
      phone: state.phone,
      balance: state.balance
    })
  },
  
  actions: {
    setUser(user) {
      this.userId = user.userId;
      this.username = user.username;
      this.avatar = user.avatar || '/images/2.jpg';
      this.email = user.email;
      this.phone = user.phone;
      this.roles = user.roles || [];
      this.balance = user.balance ? user.balance : 0;
    },
    
    setToken(token) {
      this.token = token;
      this.loginStatus = true;
      
      // 保存token到本地存储
      localStorage.setItem('token', token);
      
      // 设置axios默认请求头
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
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
      
      // 清除本地存储的token
      localStorage.removeItem('token');
      
      // 清除请求头中的Authorization
      delete apiClient.defaults.headers.common['Authorization'];
      
      // 重置签到相关状态
      this.signInStatus = {
        todaySigned: false,
        continuousDays: 0,
        totalDays: 0,
        monthDays: 0
      };
      this.signInCalendar = {};
    },
    
    async login(loginData) {
      try {
        const response = await userApi.login(loginData);
        
        // 从响应中获取token
        let token = '';
        
        // 分析响应结构
        if (response.code === 200) {
          // 从响应中获取token 
          if (response.msg && typeof response.msg === 'string') {
            token = response.msg;
          } else if (response.data && response.data.token) {
            token = response.data.data.token;
          }
        }
        
        if (!token) {
          ElMessage.warning('登录成功但未获取到有效凭证，请联系管理员');
          return false;
        }
        
        this.setToken(token);
        
        // 获取用户信息
        await this.getUserInfo();
        
        ElMessage.success('登录成功');
        return true;
      } catch (error) {
        ElMessage.error(error.response?.data?.msg || '登录失败，请稍后再试');
        return false;
      }
    },
    
    async logout() {
      try {
        // 调用登出API，服务端会使token失效
        await userApi.logout();
        
        // 清除本地用户信息
        this.clearUserInfo();
        
        ElMessage.success('已安全退出登录');
        return true;
      } catch (error) {
        console.error('登出失败:', error);
        
        // 即使API调用失败，也清除本地状态
        this.clearUserInfo();
        
        ElMessage.warning('登出过程中发生错误，但已清除本地登录状态');
        return false;
      }
    },

    async getUserInfo() {
      if (!this.token) return false;
      
      try {
        const response = await userApi.getUserDetails();
        console.log('获取用户详情响应:', response);
        
        if (response.code === 200 && response.data) {
          // 直接使用response.data作为用户数据
          this.setUser(response.data);
          console.log('用户数据已设置，余额:', this.balance);
          return true;
        }
        return false;
      } catch (error) {
        console.error('获取用户信息失败:', error);
        return false;
      }
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
    },
    
    // ===== 签到相关方法 =====
    
    // 执行签到
    async signIn() {
      try {
        const response = await userApi.signIn();
        console.log('签到响应数据:', response);
        if (response.data && response.code === 200) {
          const signData = response.data;
          
          // 更新签到状态
          this.signInStatus = {
            todaySigned: true,
            continuousDays: signData.continuousCount || this.signInStatus.continuousDays + 1,
            totalDays: this.signInStatus.totalDays + 1,
            monthDays: signData.monthCount || this.signInStatus.monthDays + 1
          };
          
          // 更新日历
          await this.getMonthSignRecord();
          
          ElMessage.success(`签到成功！已连续签到${this.signInStatus.continuousDays}天`);
          return true;
        }
        return false;
      } catch (error) {
        // 处理已签到的情况
        if (error.response?.data?.code === 500 && error.response?.data?.msg === '今天已经签到过了') {
          // 更新签到状态为已签到
          this.signInStatus.todaySigned = true;
          // 更新日历
          await this.getMonthSignRecord();
          ElMessage.info('今天已经签到过了');
          return false;
        }
        ElMessage.error(error.response?.data?.msg || '签到失败，请稍后再试');
        return false;
      }
    },
    
    // 检查今天是否已签到
    async checkSignIn() {
      try {
        const response = await userApi.checkSignIn();
        if (response.data && response.code === 200) {
          this.signInStatus.todaySigned = response.data;
          return response.data;
        }
        return false;
      } catch (error) {
        console.error('检查签到状态失败:', error);
        return false;
      }
    },
    
    // 获取当月签到记录
    async getMonthSignRecord() {
      try {
        const response = await userApi.getMonthSignRecord();
        if (response.data && response.code === 200) {
          this.signInCalendar = response.data || {};
          return true;
        }
        return false;
      } catch (error) {
        console.error('获取月度签到记录失败:', error);
        return false;
      }
    },
    
    // 获取签到统计信息
    async getSignStats() {
      try {
        const response = await userApi.getSignStats();
        if (response.data && response.code === 200) {
          const stats = response.data || {};
          this.signInStatus = {
            todaySigned: stats.todaySigned || false,
            continuousDays: stats.continuousDays || 0,
            totalDays: stats.totalDays || 0,
            monthDays: stats.monthDays || 0
          };
          return true;
        }
        return false;
      } catch (error) {
        console.error('获取签到统计信息失败:', error);
        return false;
      }
    },
    
    // 初始化签到数据
    async initSignInData() {
      if (!this.isAuthenticated) return;
      
      try {
        await Promise.all([
          this.checkSignIn(),
          this.getSignStats(),
          this.getMonthSignRecord()
        ]);
        return true;
      } catch (error) {
        console.error('初始化签到数据失败:', error);
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

    // 删除订单
    async deleteOrder(orderId) {
      try {
        const response = await apiClient.delete(`/orders/${orderId}`);
        if (response.data && response.data.code === 200) {
          return true;
        }
        return false;
      } catch (error) {
        console.error('删除订单失败:', error);
        ElMessage.error(error.response?.data?.msg || '删除订单失败，请稍后再试');
        return false;
      }
    }
  }
}) 