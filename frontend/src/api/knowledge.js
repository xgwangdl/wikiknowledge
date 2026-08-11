import http from './http'

export const listKnowledgeBases = () =>
  http.get('/knowledge-bases').then((res) => res.data)

export const createKnowledgeBase = (data) =>
  http.post('/knowledge-bases', data).then((res) => res.data)

export const updateKnowledgeBase = (id, data) =>
  http.put(`/knowledge-bases/${id}`, data).then((res) => res.data)

export const deleteKnowledgeBase = (id) =>
  http.delete(`/knowledge-bases/${id}`)

export const listDocuments = (knowledgeBaseId) =>
  http.get(`/knowledge-bases/${knowledgeBaseId}/documents`).then((res) => res.data)

export const uploadDocument = (knowledgeBaseId, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post(`/knowledge-bases/${knowledgeBaseId}/documents`, formData).then((res) => res.data)
}

export const deleteDocument = (id) =>
  http.delete(`/documents/${id}`)

export const getSuggestions = (knowledgeBaseId, query) =>
  http.get(`/knowledge-bases/${knowledgeBaseId}/suggestions`, { params: { query } })
    .then((res) => res.data.questions || [])
