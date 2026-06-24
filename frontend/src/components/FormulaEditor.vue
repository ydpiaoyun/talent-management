<template>
  <div class="formula-editor">
    <!-- 左侧：分组选择 + 指标列表 + 运算符 -->
    <div class="fe-sidebar">
      <div class="fe-section">
        <div class="fe-label">指标分组</div>
        <el-select
          v-model="selectedGroup"
          placeholder="全部分组"
          clearable
          style="width: 100%;"
          @change="onGroupChange"
        >
          <el-option
            v-for="g in groups"
            :key="g"
            :label="g"
            :value="g"
          />
        </el-select>
      </div>

      <div class="fe-section">
        <div class="fe-label">指标列表（拖拽到公式框）</div>
        <div class="fe-attr-list">
          <div
            v-for="attr in filteredAttrs"
            :key="attr.id"
            class="fe-attr-item"
            draggable="true"
            @dragstart="onDragAttr($event, attr)"
          >
            <span class="fe-attr-name">{{ attr.name }}</span>
            <span class="fe-attr-code">{{ attr.code }}</span>
          </div>
          <div v-if="filteredAttrs.length === 0" class="fe-empty">暂无指标</div>
        </div>
      </div>

      <div class="fe-section">
        <div class="fe-label">运算符（点击插入）</div>
        <div class="fe-operator-row">
          <button
            v-for="op in operators"
            :key="op"
            class="fe-op-btn"
            @click="insertText(op)"
          >{{ op }}</button>
        </div>
      </div>

      <div class="fe-section">
        <div class="fe-label">常用函数（点击插入）</div>
        <div class="fe-operator-row">
          <button
            v-for="fn in functions"
            :key="fn.label"
            class="fe-fn-btn"
            @click="insertText(fn.value)"
          >{{ fn.label }}</button>
        </div>
      </div>
    </div>

    <!-- 右侧：公式文本框 + 校验 -->
    <div class="fe-main">
      <div class="fe-toolbar">
        <span class="fe-toolbar-title">公式编辑区</span>
        <div class="fe-toolbar-actions">
          <el-button size="small" @click="clearFormula">清空</el-button>
          <el-button size="small" type="primary" @click="validateFormula" :loading="validating">
            校验公式
          </el-button>
        </div>
      </div>

      <div
        class="fe-textarea-wrapper"
        @dragover.prevent
        @drop="onDrop"
      >
        <textarea
          ref="textareaRef"
          v-model="localFormula"
          class="fe-textarea"
          placeholder="请拖拽左侧指标或运算符到此处，也可直接输入公式..."
          @input="onInput"
        ></textarea>
      </div>

      <div v-if="validateResult" class="fe-validate-msg" :class="validateResult.valid ? 'fe-success' : 'fe-error'">
        {{ validateResult.valid ? '✅' : '❌' }} {{ validateResult.message }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { expressionApi } from '../api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: String, default: '' },
  attrs: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:modelValue'])

// 运算符
const operators = ['+', '-', '*', '/', '(', ')', '>', '<', '>=', '<=', '==', '!=', '&&', '||']

// 常用函数
const functions = [
  { label: 'MAX(a,b)', value: 'MAX(,)' },
  { label: 'MIN(a,b)', value: 'MIN(,)' },
  { label: 'IF(cond,a,b)', value: 'IF(,,)' },
  { label: 'ROUND(x)', value: 'ROUND()' },
  { label: 'ABS(x)', value: 'ABS()' },
]

// 本地状态
const localFormula = ref(props.modelValue)
const textareaRef = ref(null)
const validating = ref(false)
const validateResult = ref(null)
const selectedGroup = ref('')
const groups = ref([])

// 计算分组列表
function computeGroups() {
  const set = new Set()
  ;(props.attrs || []).forEach(a => {
    set.add(a.groupName || '未分组')
  })
  groups.value = [...set]
}

// 按分组过滤指标
const filteredAttrs = computed(() => {
  if (!selectedGroup.value) return props.attrs || []
  return (props.attrs || []).filter(a => (a.groupName || '未分组') === selectedGroup.value)
})

function onGroupChange() {
  // 分组切换时无需额外操作，filteredAttrs 会自动更新
}

// 同步外部 modelValue → 内部
watch(() => props.modelValue, (v) => {
  if (v !== localFormula.value) localFormula.value = v || ''
})
// 同步内部 → 外部
watch(localFormula, (v) => {
  emit('update:modelValue', v || '')
})

onMounted(() => {
  computeGroups()
})

watch(() => props.attrs, () => computeGroups())

// 拖拽指标
function onDragAttr(event, attr) {
  event.dataTransfer.setData('text/plain', String(attr.code || ''))
  event.dataTransfer.effectAllowed = 'copy'
}

// 在光标位置插入文本
function insertText(text) {
  const ta = textareaRef.value
  if (!ta) {
    localFormula.value = (localFormula.value || '') + text
    return
  }
  const start = ta.selectionStart
  const end = ta.selectionEnd
  const before = localFormula.value.substring(0, start)
  const after = localFormula.value.substring(end)
  localFormula.value = before + text + after
  nextTick(() => {
    const pos = start + text.length
    ta.selectionStart = ta.selectionEnd = pos
    ta.focus()
  })
}

// 点击运算符按钮 → 插入文本
function insertOperator(op) {
  insertText(op)
}

// 拖放处理
function onDrop(event) {
  event.preventDefault()
  const data = event.dataTransfer.getData('text/plain')
  if (data) insertText(data)
}

function onInput() {
  validateResult.value = null
}

function clearFormula() {
  localFormula.value = ''
  validateResult.value = null
}

async function validateFormula() {
  const expr = localFormula.value
  if (!expr || !expr.trim()) {
    ElMessage.warning('请先输入公式')
    return
  }
  validating.value = true
  validateResult.value = null
  try {
    const res = await expressionApi.validate(expr)
    validateResult.value = res.data
    if (res.data.valid) {
      ElMessage.success('公式校验通过')
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    validateResult.value = { valid: false, message: '校验请求失败：' + (e.message || '') }
    ElMessage.error('校验请求失败')
  } finally {
    validating.value = false
  }
}

// 暴露方法供父组件调用
defineExpose({ validateFormula, validateResult })
</script>

<style scoped>
.formula-editor {
  display: flex;
  gap: 16px;
  min-height: 360px;
}

/* 左侧面板 */
.fe-sidebar {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  border-right: 1px solid #e4e7ed;
  padding-right: 16px;
}

.fe-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.fe-label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.fe-attr-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.fe-attr-item {
  padding: 6px 8px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: grab;
  user-select: none;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.fe-attr-item:hover {
  background: #ecf5ff;
  border-color: #409eff;
}
.fe-attr-name {
  font-size: 13px;
  color: #303133;
}
.fe-attr-code {
  font-size: 11px;
  color: #909399;
  font-family: monospace;
}

.fe-empty {
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
  padding: 12px 0;
}

.fe-operator-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.fe-op-btn {
  width: 36px;
  height: 32px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  transition: all 0.15s;
}
.fe-op-btn:hover {
  background: #f0f9eb;
  border-color: #67c23a;
  color: #67c23a;
}

.fe-fn-btn {
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #f5efff;
  cursor: pointer;
  font-size: 12px;
  color: #8b5cf6;
  transition: all 0.15s;
  white-space: nowrap;
}
.fe-fn-btn:hover {
  background: #ede9fe;
  border-color: #8b5cf6;
}

/* 右侧主区域 */
.fe-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.fe-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.fe-toolbar-title {
  font-size: 13px;
  color: #606266;
}

.fe-toolbar-actions {
  display: flex;
  gap: 8px;
}

.fe-textarea-wrapper {
  flex: 1;
  min-height: 180px;
}

.fe-textarea {
  width: 100%;
  height: 100%;
  min-height: 180px;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  line-height: 1.6;
  resize: vertical;
  box-sizing: border-box;
  transition: border-color 0.15s;
}
.fe-textarea:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64,158,255,0.15);
}

.fe-validate-msg {
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 13px;
}
.fe-success {
  background: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}
.fe-error {
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
}
</style>
