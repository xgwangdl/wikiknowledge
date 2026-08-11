import http from './http'

export const listEvalSets = () =>
  http.get('/admin/evals/sets').then((res) => res.data)

export const getEvalSet = (id) =>
  http.get(`/admin/evals/sets/${id}`).then((res) => res.data)

export const createEvalSet = (data) =>
  http.post('/admin/evals/sets', data).then((res) => res.data)

export const deleteEvalSet = (id) =>
  http.delete(`/admin/evals/sets/${id}`)

export const listEvalRuns = (evalSetId) =>
  http.get('/admin/evals/runs', { params: { evalSetId } }).then((res) => res.data)

export const runEval = (data) =>
  http.post('/admin/evals/runs', data).then((res) => res.data)

export const getEvalRun = (id) =>
  http.get(`/admin/evals/runs/${id}`).then((res) => res.data)

export const deleteEvalRun = (id) =>
  http.delete(`/admin/evals/runs/${id}`)

export async function downloadEvalRun(id) {
  const response = await http.get(`/admin/evals/runs/${id}/export`, { responseType: 'blob' })
  const url = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = url
  link.download = `eval-run-${id}.csv`
  link.click()
  URL.revokeObjectURL(url)
}
