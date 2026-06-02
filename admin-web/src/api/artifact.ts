import request from '@/utils/request'

export interface RelicObject {
  objectId: string
  title: string
  period: string
  type: string
  material: string
  description: string
  dimensions: string
  museumId: string
  detailUrl: string
  imageUrl: string
  imagePath: string
  creditLine: string
  accessionNumber: string
  crawlDate: string
  createTime: string
  updateTime: string
  isDeleted: number
}

export interface MuseumObject {
  objectId: string
  name: string
  nameCn: string
  location: string
  website: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface CreateRelicRequest {
  title: string
  period?: string
  type?: string
  material?: string
  description?: string
  dimensions?: string
  museumId: string
  detailUrl: string
  creditLine?: string
  accessionNumber?: string
  crawlDate: string
}

export interface UpdateRelicRequest {
  title?: string
  period?: string
  type?: string
  material?: string
  description?: string
  dimensions?: string
  museumId?: string
  detailUrl?: string
  creditLine?: string
  accessionNumber?: string
  crawlDate?: string
}

export interface UploadImageResponse {
  objectId: string
  imagePath: string
  imageUrl: string
}

export interface ImportCsvResponse {
  objectIds: string[]
}

export interface ArtifactFilters {
  periods: string[]
  types: string[]
  materials: string[]
  museums: string[]
}

export interface InteractionSummary {
  artifactId: string
  likeCount: number
  favoriteCount: number
  commentCount: number
  viewCount: number
}

export interface RelatedArtifact {
  objectId: string
  title: string
  period: string
  type: string
  museum: string
  imageUrl: string
}

export async function getRelicList(params: {
  page?: number
  pageSize?: number
  keyword?: string
  period?: string
  type?: string
  material?: string
  museumId?: string
}): Promise<PageResult<RelicObject>> {
  const res = await request.get('/v1/data/relics', { params })
  return res.data
}

export async function getRelicDetail(objectId: string): Promise<RelicObject> {
  const res = await request.get(`/v1/data/relics/${objectId}`)
  return res.data
}

export async function createRelic(data: CreateRelicRequest): Promise<RelicObject> {
  const res = await request.post('/v1/data/relics', data)
  return res.data
}

export async function updateRelic(objectId: string, data: UpdateRelicRequest): Promise<RelicObject> {
  const res = await request.put(`/v1/data/relics/${objectId}`, data)
  return res.data
}

export async function deleteRelic(objectId: string): Promise<{ objectId: string; isDeleted: number }> {
  const res = await request.delete(`/v1/data/relics/${objectId}`)
  return res.data
}

export async function uploadRelicImage(objectId: string, file: File): Promise<UploadImageResponse> {
  const formData = new FormData()
  formData.append('file', file)
  const res = await request.post(`/v1/data/relics/${objectId}/image`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return res.data
}

export async function importCsv(file: File): Promise<ImportCsvResponse> {
  const formData = new FormData()
  formData.append('file', file)
  const res = await request.post('/v1/data/relics/import-csv', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return res.data
}

export async function exportCsv(params: {
  keyword?: string
  museumId?: string
}): Promise<Blob> {
  const res = await request.get('/v1/data/relics/export-csv', {
    params,
    responseType: 'blob'
  })
  return res.data
}

export async function getMuseumList(params: {
  page?: number
  pageSize?: number
  keyword?: string
}): Promise<PageResult<MuseumObject>> {
  const res = await request.get('/v1/data/museums', { params })
  return res.data
}

export async function getAllMuseums(): Promise<MuseumObject[]> {
  const res = await request.get('/v1/data/museums', { params: { page: 1, pageSize: 100 } })
  return res.data.records
}

export async function getArtifactFilters(): Promise<ArtifactFilters> {
  const res = await request.get('/v1/artifacts/filters')
  return res.data
}

export async function getInteractionSummary(objectId: string): Promise<InteractionSummary> {
  const res = await request.get(`/v1/artifacts/${objectId}/interaction-summary`)
  return res.data
}

export async function getRelatedArtifacts(objectId: string, count?: number): Promise<{ items: RelatedArtifact[] }> {
  const res = await request.get(`/v1/artifacts/${objectId}/related`, { params: { count } })
  return res.data
}