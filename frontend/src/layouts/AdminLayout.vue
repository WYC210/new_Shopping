<template>
  <div class="admin-layout">
    <div class="admin-sidebar">
      <div class="logo">ShopSphere</div>
      <ul class="menu">
        <li>
          <router-link to="/dashboard" active-class="active">
            <el-icon><DataLine /></el-icon>
            <span>数据大屏</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/products" active-class="active">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/orders" active-class="active">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/users" active-class="active">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/settings" active-class="active">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </router-link>
        </li>
      </ul>
    </div>
    <div class="admin-content">
      <div class="admin-header">
        <div class="header-left">
          <el-icon class="menu-toggle" @click="toggleSidebar"><Menu /></el-icon>
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin/products' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar size="small" :src="avatarUrl" />
              <span>管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人设置</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <div class="admin-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Menu, DataLine, Goods, List, User, Setting, ArrowDown } from '@element-plus/icons-vue'

const avatarUrl = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')
const sidebarCollapsed = ref(false)

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background-color: #f0f2f5;
  color: #fff;
  
  .admin-sidebar {
    width: 220px;
    background-color: #001529;
    height: 100%;
    transition: width 0.3s;
    overflow-y: auto;
    
    .logo {
      height: 64px;
      line-height: 64px;
      padding-left: 24px;
      font-size: 20px;
      font-weight: bold;
      color: #fff;
      background-color: #002140;
      overflow: hidden;
    }
    
    .menu {
      padding: 0;
      margin: 0;
      list-style: none;
      
      li {
        a {
          display: flex;
          align-items: center;
          padding: 12px 24px;
          color: rgba(255, 255, 255, 0.65);
          text-decoration: none;
          transition: all 0.3s;
          
          &:hover, &.active {
            color: #fff;
            background-color: #1890ff;
          }
          
          .el-icon {
            margin-right: 10px;
            font-size: 16px;
          }
        }
      }
    }
  }
  
  .admin-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    
    .admin-header {
      height: 64px;
      background-color: #fff;
      box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 20px;
      
      .header-left {
        display: flex;
        align-items: center;
        
        .menu-toggle {
          font-size: 20px;
          cursor: pointer;
          color: #666;
          margin-right: 20px;
        }
        
        .breadcrumb {
          color: #666;
        }
      }
      
      .header-right {
        .user-info {
          display: flex;
          align-items: center;
          color: #666;
          cursor: pointer;
          
          span {
            margin: 0 8px;
          }
        }
      }
    }
    
    .admin-main {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .admin-sidebar {
    width: 80px;
    
    .logo {
      padding-left: 0;
      text-align: center;
    }
    
    .menu li a span {
      display: none;
    }
    
    .menu li a .el-icon {
      margin-right: 0;
    }
  }
}
</style> 