<template>
  <div id="app-container">
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <h2>人才绩效管理系统</h2>
        </div>
        <div class="header-right">
          <span>admin</span>
          <el-button type="danger" text @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="app-aside">
          <el-menu :default-active="currentRoute" router background-color="#f5f7fa" :default-openeds="['1','2','3']">
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
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const currentRoute = computed(() => route.path)

function logout() {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
html, body, #app, #app-container { height: 100%; }
.app-header {
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
}
.app-header h2 { font-size: 18px; }
.header-right { display: flex; align-items: center; gap: 12px; }
.app-aside { border-right: 1px solid #e4e7ed; min-height: calc(100vh - 56px); }
.app-main { background: #f0f2f5; padding: 20px; }
</style>
