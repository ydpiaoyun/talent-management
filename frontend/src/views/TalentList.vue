<template>
  <div>
    <div class="page-toolbar">
      <h3>人才列表</h3>
      <div style="display:flex;gap:12px;">
        <el-input v-model="keyword" placeholder="搜索姓名/部门" clearable style="width:220px" @clear="load" @keyup.enter="load" />
        <el-button type="primary" @click="$router.push('/talent/add')">添加人才</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" width="80" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="dept" label="部门" width="100" />
        <el-table-column prop="position" label="职位" min-width="120" />
        <el-table-column v-for="col in attrColumns" :key="col.code" :prop="col.code" :label="col.name" :width="col.width || 100">
          <template #default="{ row }">
            <span v-if="col.type === 'NUMBER' && row[col.code]">
              {{ row[col.code] }}{{ col.unit || '' }}
            </span>
            <span v-else>{{ row[col.code] || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/talent/edit/${row.id}`)">编辑</el-button>
            <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { talentApi, attrApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const keyword = ref('')
const tableData = ref([])
const attrColumns = ref([])

async function load() {
  loading.value = true
  try {
    const attrs = await attrApi.list()
    attrColumns.value = attrs.data.map(a => ({
      code: a.code, name: a.name, type: a.type, unit: a.unit, width: a.type === 'NUMBER' ? 90 : 100
    }))
    const res = await talentApi.listWithAttrs()
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

async function del(id) {
  await ElMessageBox.confirm('确认删除该人才？', '提示', { type: 'warning' })
  await talentApi.delete(id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
</style>
