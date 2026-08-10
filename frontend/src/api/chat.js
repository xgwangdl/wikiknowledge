import http from './http'

export const listSessions = () =>
  http.get('/sessions').then((res) => res.data)

export const getSession = (id) =>
  http.get(`/sessions/${id}`).then((res) => res.data)

export const createSession = (data) =>
  http.post('/sessions', data).then((res) => res.data)

export const deleteSession = (id) =>
  http.delete(`/sessions/${id}`)

export async function streamChat({ knowledgeBaseId, question, sessionId, title, onEvent, signal }) {
  const response = await fetch('/api/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${localStorage.getItem('accessToken')}`
    },
    body: JSON.stringify({ knowledgeBaseId, question, sessionId, title }),
    signal
  })

  if (!response.ok || !response.body) {
    throw new Error('请求失败')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop()
    for (const line of lines) {
      if (line.startsWith('data:')) {
        const payload = line.slice(5).trim()
        if (payload) {
          onEvent(JSON.parse(payload))
        }
      }
    }
  }
}
