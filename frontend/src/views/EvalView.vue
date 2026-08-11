<template>
  <div class="page">
    <div class="toolbar">
      <h2>评估中心</h2>
      <div>
        <el-button type="primary" @click="openCreateDialog">新建评估集</el-button>
        <el-button type="success" @click="openRunDialog">运行评估</el-button>
      </div>
    </div>

    <el-card class="section">
      <template #header>评估集</template>
      <el-table :data="evalSets" size="small">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="questionCount" label="题目数" width="100" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewSet(row)">查看</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteSet(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="section">
      <template #header>评估运行记录</template>
      <el-table :data="evalRuns" size="small">
        <el-table-column prop="id" label="运行 ID" width="100" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="metrics" label="指标" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="200" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleExport(row)">导出 CSV</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteRun(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createDialogVisible" title="新建评估集" width="640px">
      <el-form :model="createForm" label-width="90px">
        <el-form-item label="名称">
          <el-input v-model="createForm.name" placeholder="例如：RAG 基础问题集" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="题目">
          <div v-for="(question, index) in createForm.questions" :key="index" class="question-row">
            <el-input v-model="question.question" placeholder="问题" />
            <el-input v-model="question.expectedAnswer" placeholder="期望答案" />
            <el-input v-model="question.expectedChunkIds" placeholder="期望 chunk id，逗号分隔" />
            <el-button link type="danger" @click="createForm.questions.splice(index, 1)">移除</el-button>
          </div>
          <el-button size="small" @click="addQuestion">添加题目</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSet">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="runDialogVisible" title="运行评估" width="480px">
      <el-form label-width="100px">
        <el-form-item label="评估集">
          <el-select v-model="runForm.evalSetId" style="width: 100%">
            <el-option v-for="set in evalSets" :key="set.id" :label="set.name" :value="set.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识库">
          <el-select v-model="runForm.knowledgeBaseId" style="width: 100%">
            <el-option v-for="kb in knowledgeBases" :key="kb.id" :label="kb.name" :value="kb.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="TopK">
          <el-input-number v-model="runForm.topK" :min="1" :max="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="runDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="running" @click="handleRunEval">运行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listKnowledgeBases } from '../api/knowledge'
import {
  createEvalSet,
  deleteEvalSet,
  deleteEvalRun,
  downloadEvalRun,
  listEvalRuns,
  listEvalSets,
  runEval
} from '../api/eval'

const evalSets = ref([])
const evalRuns = ref([])
const knowledgeBases = ref([])
const createDialogVisible = ref(false)
const runDialogVisible = ref(false)
const running = ref(false)
const createForm = reactive({
  name: '',
  description: '',
  questions: []
})
const runForm = reactive({
  evalSetId: null,
  knowledgeBaseId: null,
  topK: 10
})

onMounted(async () => {
  await loadAll()
})

async function loadAll() {
  evalSets.value = await listEvalSets()
  evalRuns.value = await listEvalRuns()
  knowledgeBases.value = await listKnowledgeBases()
}

function openCreateDialog() {
  createForm.name = ''
  createForm.description = ''
  createForm.questions = []
  addQuestion()
  createDialogVisible.value = true
}

function addQuestion() {
  createForm.questions.push({ question: '', expectedAnswer: '', expectedChunkIds: '' })
}

async function handleCreateSet() {
  if (!createForm.name || !createForm.questions.length) {
    ElMessage.warning('请填写名称和至少一个题目')
    return
  }
  const payload = {
    name: createForm.name,
    description: createForm.description,
    questions: createForm.questions.map((q) => ({
      question: q.question,
      expectedAnswer: q.expectedAnswer,
      expectedChunkIds: q.expectedChunkIds
        ? q.expectedChunkIds.split(',').map((v) => Number(v.trim())).filter(Boolean)
        : []
    }))
  }
  await createEvalSet(payload)
  ElMessage.success('创建成功')
  createDialogVisible.value = false
  await loadAll()
}

async function handleDeleteSet(row) {
  await ElMessageBox.confirm(`确认删除评估集「${row.name}」？`, '提示', { type: 'warning' })
  await deleteEvalSet(row.id)
  await loadAll()
}

function openRunDialog() {
  runDialogVisible.value = true
}

async function handleRunEval() {
  if (!runForm.evalSetId || !runForm.knowledgeBaseId) {
    ElMessage.warning('请选择评估集和知识库')
    return
  }
  running.value = true
  try {
    await runEval(runForm)
    ElMessage.success('评估已运行')
    runDialogVisible.value = false
    await loadAll()
  } finally {
    running.value = false
  }
}

async function handleViewSet(row) {
  ElMessage.info(`评估集 ${row.name}：${row.questionCount} 道题`)
}

async function handleExport(row) {
  await downloadEvalRun(row.id)
}

async function handleDeleteRun(row) {
  await ElMessageBox.confirm(`确认删除评估运行 ${row.id}？`, '提示', { type: 'warning' })
  await deleteEvalRun(row.id)
  await loadAll()
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section {
  margin-top: 16px;
}

.question-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
</style>
