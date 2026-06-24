<template>
  <div>
    <div class="page-toolbar">
      <h3>{{ isEdit ? '编辑人才' : '添加人才' }}</h3>
    </div>

    <el-card v-loading="loading">
      <el-form :model="form" ref="formRef" label-width="100px" style="max-width:700px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name" :rules="[{required:true,message:'请输入姓名'}]">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" style="width:100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="form.birthDate" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">
              <el-input v-model="form.dept" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位">
              <el-input v-model="form.position" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电话">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">指标信息</el-divider>

        <el-row :gutter="20" v-for="attr in attrs" :key="attr.id">
          <el-col :span="24">
            <el-form-item :label="attr.name" :required="attr.isRequired === 1">
              <template v-if="attr.type === 'ENUM' && attr.optionsJson">
                <el-select v-model="attrValues[attr.id]" style="width:100%" clearable>
                  <el-option v-for="opt in parseOptions(attr.optionsJson)" :key="opt" :label="opt" :value="opt" />
                </el-select>
              </template>
              <template v-else-if="attr.type === 'NUMBER' || attr.type === 'RANGE'">
                <el-input v-model="attrValues[attr.id]" type="number" :placeholder="`请输入${attr.name}`">
                  <template #append v-if="attr.unit">{{ attr.unit }}</template>
                </el-input>
              </template>
              <template v-else>
                <el-input v-model="attrValues[attr.id]" :placeholder="`请输入${attr.name}`" />
              </template>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="submit" :loading="saving">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { talentApi, attrApi } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const saving = ref(false)
const formRef = ref()
const attrs = ref([])
const form = reactive({
  id: null, name: '', gender: '男', birthDate: '', dept: '', position: '', email: '', phone: '', remark: '',
})
const attrValues = reactive({})

function parseOptions(json) {
  try { return JSON.parse(json) } catch { return [] }
}

async function load() {
  loading.value = true
  try {
    const attrRes = await attrApi.list()
    attrs.value = attrRes.data
    if (isEdit.value) {
      const res = await talentApi.getById(route.params.id)
      const d = res.data
      Object.assign(form, {
        id: d.id, name: d.name, gender: d.gender, birthDate: d.birthDate || '',
        dept: d.dept, position: d.position, email: d.email, phone: d.phone, remark: d.remark || '',
      })
      if (d.attributes) {
        for (const av of d.attributes) {
          attrValues[av.attrId] = av.value || ''
        }
      }
    }
  } finally {
    loading.value = false
  }
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const data = { ...form, attrValues: { ...attrValues } }
    if (isEdit.value) {
      await talentApi.update(data)
    } else {
      await talentApi.save(data)
    }
    ElMessage.success('保存成功')
    router.push('/talent/list')
  } finally {
    saving.value = false
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
