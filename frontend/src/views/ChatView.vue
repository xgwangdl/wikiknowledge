<template>
  <div class="chat-page">
    <el-card class="chat-card">
      <div class="chat-toolbar">
        <el-select v-model="activeKbId" placeholder="选择知识库" style="width: 220px" @change="handleKbChange">
          <el-option v-for="kb in knowledgeBases" :key="kb.id" :label="kb.name" :value="kb.id" />
        </el-select>
        <el-button type="primary" @click="handleNewSession">新会话</el-button>
      </div>

      <div class="chat-body">
        <div class="session-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: session.id === activeSessionId }"
            @click="handleSelectSession(session.id)"
          >
            <span>{{ session.title }}</span>
            <el-button link type="danger" size="small" @click.stop="handleDeleteSession(session.id)">删除</el-button>
          </div>
        </div>

        <div class="message-area" ref="messageArea">
          <div v-for="message in messages" :key="message.id" class="message" :class="message.role">
            <div class="bubble markdown-body">{{ message.content || '正在思考...' }}</div>
            <div v-if="message.citations && message.citations.length" class="citations">
              引用：{{ message.citations.map((c) => `文档 ${c.documentId} #${c.seqNo}`).join('、') }}
            </div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            placeholder="输入你的问题，Enter 发送"
            @keydown.enter.exact.prevent="handleSend"
          />
          <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listKnowledgeBases } from '../api/knowledge'
import { deleteSession, getSession, listSessions, streamChat } from '../api/chat'

const knowledgeBases = ref([])
const sessions = ref([])
const activeKbId = ref(null)
const activeSessionId = ref(null)
const messages = ref([])
const input = ref('')
const sending = ref(false)
const messageArea = ref(null)

onMounted(async () => {
  knowledgeBases.value = await listKnowledgeBases()
  sessions.value = await listSessions()
})

function handleKbChange() {
  activeSessionId.value = null
  messages.value = []
}

async function handleNewSession() {
  if (!activeKbId.value) {
    ElMessage.warning('请先选择知识库')
    return
  }
  activeSessionId.value = null
  messages.value = []
}

async function handleSelectSession(id) {
  activeSessionId.value = id
  const detail = await getSession(id)
  messages.value = detail.messages.map((message) => ({
    id: message.id,
    role: message.role,
    content: message.content,
    citations: message.citations ? JSON.parse(message.citations) : []
  }))
  await scrollToBottom()
}

async function handleDeleteSession(id) {
  await ElMessageBox.confirm('确认删除该会话？', '提示', { type: 'warning' })
  await deleteSession(id)
  if (activeSessionId.value === id) {
    activeSessionId.value = null
    messages.value = []
  }
  sessions.value = await listSessions()
}

async function handleSend() {
  const question = input.value.trim()
  if (!question) return
  if (!activeKbId.value) {
    ElMessage.warning('请先选择知识库')
    return
  }

  messages.value.push({ id: `user-${Date.now()}`, role: 'user', content: question, citations: [] })
  const assistant = { id: `assistant-${Date.now()}`, role: 'assistant', content: '', citations: [] }
  messages.value.push(assistant)
  input.value = ''
  sending.value = true
  await scrollToBottom()

  try {
    await streamChat({
      knowledgeBaseId: activeKbId.value,
      question,
      sessionId: activeSessionId.value,
      title: activeSessionId.value ? null : '新会话',
      onEvent: (event) => {
        if (event.type === 'start' && event.data?.sessionId) {
          activeSessionId.value = event.data.sessionId
        } else if (event.type === 'delta') {
          assistant.content += event.data?.content || ''
        } else if (event.type === 'done') {
          assistant.citations = event.data?.citations || []
        } else if (event.type === 'error') {
          assistant.content = event.data?.message || '出错了'
        }
        scrollToBottom()
      }
    })
  } catch (error) {
    assistant.content = '请求失败，请稍后重试'
    ElMessage.error('请求失败')
  } finally {
    sending.value = false
    sessions.value = await listSessions()
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (messageArea.value) {
    messageArea.value.scrollTop = messageArea.value.scrollHeight
  }
}
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 120px);
}

.chat-card {
  height: 100%;
}

.chat-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.chat-body {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 12px;
  height: calc(100% - 70px);
}

.session-list {
  overflow: auto;
  border-right: 1px solid #e5e7eb;
  padding-right: 8px;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
}

.session-item.active {
  background: #eef4ff;
}

.message-area {
  overflow: auto;
  padding: 8px;
}

.message {
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
}

.message.user {
  align-items: flex-end;
}

.bubble {
  max-width: 80%;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
}

.message.user .bubble {
  background: #1d4ed8;
  color: #fff;
  border: none;
}

.citations {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
}

.chat-input {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
</style>
