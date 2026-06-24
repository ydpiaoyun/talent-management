<template>
  <div>
    <div class="page-toolbar">
      <h3>指标管理</h3>
      <el-button type="primary" @click="openDialog()">新增指标</el-button>
    </div>

    <el-card>
      <el-table :data="attrs" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="指标名" width="120" />
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="type" label="类型" width="90" />
        <el-table-column prop="weight" label="权重" width="70">
          <template #default="{row}">{{ row.weight }}%</template>
        </el-table-column>
        <el-table-column prop="direction" label="方向" width="70">
          <template #default="{row}">{{ row.direction === 1 ? '↑越高越好' : '↓越低越好' }}</template>
        </el-table-column>
        <el-table-column prop="groupName" label="分组" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-switch :model-value="row.status === 1" @change="toggle(row.id)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{row}">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editing.id ? '编辑指标' : '新增指标'" width="560px">
      <el-form :model="editing" label-width="100px">
        <el-form-item label="指标名" required>
          <el-input v-model="editing.name" />
        </el-form-item>
        <el-form-item label="编码" required>
          <el-input v-model="editing.code" placeholder="英文标识，如 work_years" />
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="editing.type" style="width:100%">
            <el-option label="文本 TEXT" value="TEXT" />
            <el-option label="数字 NUMBER" value="NUMBER" />
            <el-option label="枚举 ENUM" value="ENUM" />
            <el-option label="范围 RANGE" value="RANGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位" v-if="editing.type === 'NUMBER' || editing.type === 'RANGE'">
          <el-input v-model="editing.unit" placeholder="如 cm、年" />
        </el-form-item>
        <el-form-item label="最小值" v-if="editing.type === 'RANGE'">
          <el-input-number v-model="editing.minValue" :precision="2" />
        </el-form-item>
        <el-form-item label="最大值" v-if="editing.type === 'RANGE'">
          <el-input-number v-model="editing.maxValue" :precision="2" />
        </el-form-item>
        <el-form-item label="枚举选项" v-if="editing.type === 'ENUM'">
          <el-input v-model="editing.optionsJson" placeholder='JSON数组，如 ["博士","硕士","本科"]' />
        </el-form-item>
        <el-form-item label="分值映射" v-if="editing.type === 'ENUM'">
          <el-input v-model="editing.scoreMapping" placeholder='JSON对象，如 {"博士":100,"硕士":80}' />
        </el-form-item>
        <el-form-item label="权重">
          <el-input-number v-model="editing.weight" :min="0" :max="100" />
          <span style="margin-left:8px;color:#999;">%（所有指标权重之和不必为100）</span>
        </el-form-item>
        <el-form-item label="方向">
          <el-radio-group v-model="editing.direction">
            <el-radio :value="1">越高越好 ↑</el-radio>
            <el-radio :value="-1">越低越好 ↓</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分组">
          <el-select
            v-model="editing.groupName"
            allow-create
            filterable
            placeholder="选择或输入分组名称"
            style="width: 100%;"
          >
            <el-option
              v-for="g in allGroups"
              :key="g"
              :label="g"
              :value="g"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="必填">
          <el-switch v-model="editing.isRequiredBool" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editing.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { attrApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const attrs = ref([])
const allGroups = ref([])
const dialogVisible = ref(false)
const editing = reactive(emptyAttr())

function emptyAttr() {
  return {
    id: null, name: '', code: '', type: 'TEXT', unit: '', minValue: null, maxValue: null,
    optionsJson: '', scoreMapping: '', weight: 0, direction: 1,
    groupName: '', isRequired: 0, isRequiredBool: false, sortOrder: 0, status: 1,
  }
}

async function load() {
  loading.value = true
  try {
    const [listRes, groupsRes] = await Promise.all([
      attrApi.list(),
      attrApi.groups(),
    ])
    attrs.value = listRes.data
    allGroups.value = groupsRes.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  if (row) {
    Object.assign(editing, row, { isRequiredBool: row.isRequired === 1 })
  } else {
    Object.assign(editing, emptyAttr())
  }
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    const data = {
      ...editing,
      isRequired: editing.isRequiredBool ? 1 : 0,
    }
    if (data.id) {
      await attrApi.update(data)
    } else {
      await attrApi.save(data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function toggle(id) {
  await attrApi.toggle(id)
  load()
}

async function del(id) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await attrApi.delete(id)
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
