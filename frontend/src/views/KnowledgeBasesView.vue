<template>
  <div class="page">
    <div class="toolbar">
      <h2>知识库管理</h2>
      <el-button type="primary" @click="dialogVisible = true">新建知识库</el-button>
    </div>

    <el-collapse v-model="activeCollapse">
      <el-collapse-item v-for="kb in knowledgeBases" :key="kb.id" :name="kb.id">
        <template #title>
          <div class="kb-title">
            <span>{{ kb.name }}</span>
            <el-tag size="small" type="success">{{ kb.status }}</el-tag>
          </div>
        </template>

        <div class="doc-actions">
          <input
            :ref="(el) => (fileInputs[kb.id] = el)"
            type="file"
            accept=".pdf,.docx,.md,.txt"
            style="display: none"
            @change="(event) => handleUpload(kb, event)"
          />
          <el-button size="small" type="primary" plain @click="fileInputs[kb.id]?.click()">
            上传文档
          </el-button>
          <el-button size="small" type="danger" plain @click="handleDeleteKnowledgeBase(kb)">
            删除知识库
          </el-button>
        </div>

        <el-table :data="documents[kb.id] || []" size="small" @expand-change="() => loadDocuments(kb)">
          <el-table-column prop="filename" label="文件名" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="chunkCount" label="切片数" width="100" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="danger" size="small" @click="handleDeleteDocument(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>

    <el-dialog v-model="dialogVisible" title="新建知识库" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createKnowledgeBase,
  deleteDocument,
  deleteKnowledgeBase,
  listDocuments,
  listKnowledgeBases,
  uploadDocument
} from '../api/knowledge'

const knowledgeBases = ref([])
const documents = ref({})
const fileInputs = ref({})
const activeCollapse = ref([])
const dialogVisible = ref(false)
const form = reactive({ name: '', description: '' })
const MAX_FILE_SIZE = 20 * 1024 * 1024

onMounted(load)

async function load() {
  knowledgeBases.value = await listKnowledgeBases()
}

async function loadDocuments(kb) {
  documents.value[kb.id] = await listDocuments(kb.id)
}

async function handleCreate() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入名称')
    return
  }
  await createKnowledgeBase({ name: form.name, description: form.description })
  ElMessage.success('创建成功')
  dialogVisible.value = false
  form.name = ''
  form.description = ''
  await load()
}

async function handleDeleteKnowledgeBase(kb) {
  await ElMessageBox.confirm(`确认删除知识库「${kb.name}」？`, '提示', { type: 'warning' })
  await deleteKnowledgeBase(kb.id)
  ElMessage.success('已删除')
  await load()
}

async function handleUpload(kb, event) {
  const file = event.target.files[0]
  if (!file) return
  if (file.size > MAX_FILE_SIZE) {
    ElMessage.error('文件大小不能超过 20MB')
    event.target.value = ''
    return
  }
  try {
    await uploadDocument(kb.id, file)
    ElMessage.success('上传成功，正在解析')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '上传失败，请重试')
  } finally {
    event.target.value = ''
    await loadDocuments(kb)
  }
}

async function handleDeleteDocument(document) {
  await ElMessageBox.confirm(`确认删除文档「${document.filename}」？`, '提示', { type: 'warning' })
  await deleteDocument(document.id)
  ElMessage.success('已删除')
  await loadDocuments({ id: document.knowledgeBaseId })
}

function statusType(status) {
  if (status === 'READY') return 'success'
  if (status === 'FAILED') return 'danger'
  return 'warning'
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.kb-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.doc-actions {
  margin-bottom: 8px;
}
</style>
