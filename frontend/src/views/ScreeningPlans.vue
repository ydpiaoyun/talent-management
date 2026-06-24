<template>
  <div>
    <div class="page-toolbar">
      <h3>筛选方案管理</h3>
      <el-button type="primary" @click="openPlanDialog()">新增方案</el-button>
    </div>

    <el-card>
      <el-table :data="plans" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="方案名" width="180" />
        <el-table-column prop="description" label="描述" min-width="250" />
        <el-table-column prop="logicType" label="逻辑" width="70" />
        <el-table-column label="操作" width="280">
          <template #default="{row}">
            <el-button size="small" @click="manageConditions(row)">条件</el-button>
            <el-button size="small" type="success" @click="execute(row.id)">执行筛选</el-button>
            <el-button size="small" @click="openPlanDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="delPlan(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 筛选结果弹窗 -->
    <el-dialog v-model="resultVisible" title="筛选结果" width="80%">
      <el-table :data="results" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="name" label="姓名" width="80" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="dept" label="部门" width="100" />
        <el-table-column prop="position" label="职位" min-width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="电话" width="130" />
      </el-table>
      <p style="margin-top:12px;color:#666;">共匹配 <b>{{ results.length }}</b> 人</p>
    </el-dialog>

    <!-- 方案编辑弹窗 -->
    <el-dialog v-model="planDialogVisible" :title="planEditing.id ? '编辑方案' : '新增方案'" width="500px">
      <el-form :model="planEditing" label-width="80px">
        <el-form-item label="方案名">
          <el-input v-model="planEditing.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="planEditing.description" type="textarea" />
        </el-form-item>
        <el-form-item label="逻辑">
          <el-radio-group v-model="planEditing.logicType">
            <el-radio value="AND">AND（满足所有条件）</el-radio>
            <el-radio value="OR">OR（满足任一条件）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="高级公式">
          <el-input
            v-model="planEditing.expression"
            type="textarea"
            :rows="3"
            placeholder="可选，输入高级筛选公式（如：score_math > 80 && score_english > 70）"
          />
          <div style="font-size:12px;color:#909399;margin-top:4px;">
            留空则使用下方条件组合，填写后优先使用公式筛选
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePlan" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 条件管理弹窗 -->
    <el-dialog v-model="condDialogVisible" :title="`条件管理 - ${currentPlan?.name || ''}`" width="900px">
      <div style="margin-bottom:12px;">
        <el-button type="primary" size="small" @click="addCondition">添加条件</el-button>
        <el-button size="small" @click="condDialogVisible = false">返回</el-button>
      </div>
      <el-table :data="conditions" border stripe>
        <el-table-column prop="name" label="条件名" width="140">
          <template #default="{row,$index}">
            <el-input v-model="row.name" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="指标" width="130">
          <template #default="{row}">
            <el-select v-model="row.attrId" size="small" style="width:100%">
              <el-option v-for="a in allAttrs" :key="a.id" :label="a.name" :value="a.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作符" width="100">
          <template #default="{row}">
            <el-select v-model="row.operator" size="small" style="width:100%">
              <el-option label="等于" value="EQ" />
              <el-option label="不等于" value="NE" />
              <el-option label="大于" value="GT" />
              <el-option label="大于等于" value="GTE" />
              <el-option label="小于" value="LT" />
              <el-option label="小于等于" value="LTE" />
              <el-option label="包含" value="IN" />
              <el-option label="模糊匹配" value="LIKE" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="比较值" min-width="160">
          <template #default="{row}">
            <el-input v-model="row.value" size="small" placeholder="如: 硕士,博士 (IN时逗号分隔)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="60">
          <template #default="{row,$index}">
            <el-button size="small" type="danger" @click="conditions.splice($index,1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="condDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConditions" :loading="saving">保存条件</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { screeningApi, attrApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const plans = ref([])
const allAttrs = ref([])

// 方案编辑
const planDialogVisible = ref(false)
const planEditing = reactive({ id: null, name: '', description: '', logicType: 'AND' })

// 条件编辑
const condDialogVisible = ref(false)
const currentPlan = ref(null)
const conditions = ref([])

// 结果
const resultVisible = ref(false)
const results = ref([])

async function load() {
  loading.value = true
  try {
    plans.value = (await screeningApi.planList()).data
    allAttrs.value = (await attrApi.list()).data
  } finally {
    loading.value = false
  }
}

function openPlanDialog(row) {
  if (row) Object.assign(planEditing, row)
  else Object.assign(planEditing, { id: null, name: '', description: '', logicType: 'AND' })
  planDialogVisible.value = true
}

async function savePlan() {
  saving.value = true
  try {
    if (planEditing.id) {
      await screeningApi.planUpdate({ ...planEditing })
    } else {
      await screeningApi.planSave({ ...planEditing })
    }
    ElMessage.success('保存成功')
    planDialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function delPlan(id) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await screeningApi.planDelete(id)
  ElMessage.success('删除成功')
  load()
}

async function manageConditions(plan) {
  currentPlan.value = plan
  const res = await screeningApi.condList(plan.id)
  conditions.value = res.data.map(c => ({ ...c }))
  condDialogVisible.value = true
}

function addCondition() {
  conditions.value.push({
    planId: currentPlan.value.id, name: '', attrId: allAttrs.value[0]?.id || null,
    operator: 'EQ', value: '', sortOrder: conditions.value.length + 1,
  })
}

async function saveConditions() {
  saving.value = true
  try {
    await screeningApi.condBatchSave(currentPlan.value.id, conditions.value)
    ElMessage.success('条件保存成功')
    condDialogVisible.value = false
  } finally {
    saving.value = false
  }
}

async function execute(planId) {
  loading.value = true
  try {
    const res = await screeningApi.execute(planId)
    results.value = res.data
    resultVisible.value = true
  } finally {
    loading.value = false
  }
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
