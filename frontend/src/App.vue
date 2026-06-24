<template>
  <div id="app-container">
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <h2>人才绩效管理系统</h2>
        </div>
        <div class="header-right">
          <el-button text @click="toggleTheme" style="color: var(--text-secondary);">
            <el-icon :size="18">
              <component :is="isDark ? Sunny : Moon" />
            </el-icon>
          </el-button>
          <span>admin</span>
          <el-button type="danger" text @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="app-aside">
          <el-menu :default-active="currentRoute" router :default-openeds="['1','2','3']">
            <el-sub-menu index="1">
              <template #title><el-icon><UserFilled /></el-icon> 人才管理</template>
              <el-menu-item index="/talent/list">人才列表</el-menu-item>
              <el-menu-item index="/talent/add">添加人才</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="2">
              <template #title><el-icon><SetUp /></el-icon> 指标配置</template>
              <el-menu-item index="/attribute/list">指标管理</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="3">
              <template #title><el-icon><Filter /></el-icon> 人才选拔</template>
              <el-menu-item index="/screening/plans">筛选方案</el-menu-item>
              <el-menu-item index="/scoring/plans">评分管理</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Sunny, Moon } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const currentRoute = computed(() => route.path)

const isDark = ref(true)

function initTheme() {
  const saved = localStorage.getItem('theme')
  if (saved === 'light') {
    isDark.value = false
    document.documentElement.dataset.theme = 'light'
  } else {
    isDark.value = true
    document.documentElement.dataset.theme = ''
  }
}

function toggleTheme() {
  isDark.value = !isDark.value
  if (isDark.value) {
    document.documentElement.dataset.theme = ''
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.dataset.theme = 'light'
    localStorage.setItem('theme', 'light')
  }
}

onMounted(() => {
  initTheme()
})

function logout() {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style>
/* 仅保留 theme.css 未覆盖的布局样式 */
html, body, #app, #app-container { height: 100%; }
.el-container { height: 100%; }
</style>
