# SCRM

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Compile and Minify for Production

```sh
npm run build
```

## 性能优化

为了提高应用性能，减少不必要的请求和重新渲染，我们实施了以下优化：

### 布局优化

1. **布局组件化**：创建了 `MainLayout` 和 `FullscreenLayout` 两种布局组件，将固定内容（如导航栏和页脚）放在布局组件中，避免每次路由切换时重新加载。

2. **局部更新**：使用 `<router-view>` 只更新变化的内容，固定内容保持不变。

3. **页面缓存**：通过 `<keep-alive>` 缓存页面组件状态，避免重复创建和销毁组件。

### 数据加载优化

1. **全局状态管理**：使用 Pinia 存储全局数据（如商品分类），避免重复请求。

2. **按需加载**：只在数据不存在时才发送请求，利用缓存数据。

3. **预加载**：在布局组件中预加载通用数据，如商品分类。

### 过渡动画

1. **统一过渡效果**：创建 `PageTransitionWrapper` 组件，提供统一的页面过渡动画。

2. **平滑切换**：使用 CSS 过渡效果，提升用户体验。

### 路由配置优化

1. **布局指定**：为不同路由指定适合的布局组件。

2. **缓存控制**：通过路由元数据控制页面是否需要缓存。
