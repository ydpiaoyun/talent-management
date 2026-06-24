<template>
  <div>
    <div class="page-toolbar">
      <h3>评分管理</h3>
      <el-button type="primary" @click="openPlanDialog()">新增评分方案</el-button>
    </div>

    <el-card>
      <el-table :data="plans" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="方案名" width="180" />
        <el-table-column prop="remark" label="说明" min-width="250" />
        <el-table-column label="公式" min-width="200">
          <template #default="{row}">
            <code style="font-size:12px;color:#409eff;">{{ row.expression || '-' }}</code>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{row}">
            <el-button size="small" type="success" @click="calc(row.id)">计算评分</el-button>
            <el-button size="small" @click="showRanking(row.id)">查看排名</el-button>
            <el-button size="small" @click="openPlanDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="delPlan(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 权重说明 -->
    <el-card style="margin-top:16px;">
      <h4 style="margin-bottom:12px;">当前指标权重配置</h4>
      <el-table :data="allAttrs" border stripe size="small">
        <el-table-column prop="name" label="指标" width="120" />
        <el-table-column prop="type" label="类型" width="80" />
        <el-table-column prop="weight" label="权重(%)" width="100" />
        <el-table-column label="方向" width="100">
          <template #default="{row}">{{ row.direction === 1 ? '越高越好' : '越低越好' }}</template>
        </el-table-column>
        <el-table-column label="分值映射" min-width="200">
          <template #default="{row}">{{ row.scoreMapping || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 排名结果弹窗 -->
    <el-dialog v-model="rankingVisible" title="评分排名" width="85%">
      <el-table :data="rankingResult" border stripe v-loading="rankLoading" max-height="500">
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column prop="name" label="姓名" width="80" />
        <el-table-column prop="dept" label="部门" width="100" />
        <el-table-column prop="position" label="职位" min-width="130" />
        <el-table-column prop="totalScore" label="综合评分" width="100" sortable>
          <template #default="{row}">
            <el-tag :type="row.totalScore >= 70 ? 'success' : row.totalScore >= 50 ? 'warning' : 'danger'">
              {{ row.totalScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-for="col in rankAttrCols" :key="col" :prop="`DETAIL_${col}`" :label="col" width="90" />
      </el-table>
    </el-dialog>

    <!-- 方案编辑弹窗 -->
    <el-dialog v-model="planDialogVisible" :title="planEditing.id ? '编辑方案' : '新增方案'" width="900px">
      <el-form :model="planEditing" label-width="80px">
        <el-form-item label="方案名">
          <el-input v-model="planEditing.name" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="planEditing.remark" type="textarea" />
        </el-form-item>
        <el-form-item label="评分公式">
          <FormulaEditor
            v-model="planEditing.expression"
            :attrs="allAttrs"
            ref="formulaEditorRef"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePlan" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { scoringApi, attrApi } from '../api'
import FormulaEditor from '../components/FormulaEditor.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const rankLoading = ref(false)
const plans = ref([])
const allAttrs = ref([])
const rankingResult = ref([])
const rankAttrCols = ref([])
const rankingVisible = ref(false)

// 方案编辑
const planDialogVisible = ref(false)
const planEditing = reactive({ id: null, name: '', remark: '', expression: '' })
const formulaEditorRef = ref(null)

async function load() {
  plans.value = (await scoringApi.planList()).data
  allAttrs.value = (await attrApi.list()).data
}

async function calc(planId) {
  loading.value = true
  try {
    const res = await scoringApi.calculate(planId)
    ElMessage.success(`评分计算完成，共 ${res.data.length} 人`)
    await showRanking(planId)
  } finally {
    loading.value = false
  }
}

async function showRanking(planId) {
  rankLoading.value = true
  rankingVisible.value = true
  try {
    const res = await scoringApi.ranking(planId)
    rankingResult.value = res.data.map(r => {
      let detail = {}
      try { detail = JSON.parse(r.DETAIL_JSON) } catch {}
      const row = { ...r }
      for (const [k, v] of Object.entries(detail)) {
        row[`DETAIL_${k}`] = typeof v === 'number' ? Math.round(v * 10) / 10 : v
      }
      return row
    })
    if (rankingResult.value.length > 0) {
      const first = rankingResult.value[0]
      rankAttrCols.value = Object.keys(first).filter(k => k.startsWith('DETAIL_')).map(k => k.replace('DETAIL_', ''))
    }
  } finally {
    rankLoading.value = false
  }
}

function openPlanDialog(row) {
  if (row) Object.assign(planEditing, row)
  else Object.assign(planEditing, { id: null, name: '', remark: '', expression: '' })
  planDialogVisible.value = true
}

async function savePlan() {
  saving.value = true
  try {
    // 如果输入了公式，先调用组件内的校验方法
    if (planEditing.expression) {
      const res = await formulaEditorRef.value?.validateFormula()
      if (!res?.valid) {
        saving.value = false
        return
      }
    }
    if (planEditing.id) {
      await scoringApi.planUpdate({ ...planEditing })
    } else {
      await scoringApi.planSave({ ...planEditing })
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
  await scoringApi.planDelete(id)
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
