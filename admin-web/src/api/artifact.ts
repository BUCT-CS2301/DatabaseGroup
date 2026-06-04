import request from '@/utils/request'

export interface RelicObject {
  objectId: string
  title: string
  period: string
  type: string
  material: string
  description: string
  dimensions: string
  museum: string
  museumId: string
  location: string
  detailUrl: string
  imageUrl: string
  imageUrls: string[]
  imagePath: string
  creditLine: string
  accessionNumber: string
  crawlDate: string
  createTime: string
  updateTime: string
  isDeleted: number
  hot: number
}

export interface MuseumObject {
  objectId: string
  name: string
  nameCn: string
  location: string
  website: string
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
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

export function getRelicList(params: {
  page?: number
  size?: number
  keyword?: string
  period?: string
  type?: string
  material?: string
  museum?: string
  sort?: 'hot' | 'name' | 'period'
}): Promise<PageResult<RelicObject>> {
  return request({
    url: '/api/v1/artifacts',
    method: 'get',
    params
  })
}

export function getRelicDetail(objectId: string): Promise<RelicObject> {
  return request({
    url: `/v1/artifacts/${objectId}`,
    method: 'get'
  })
}

export function createRelic(data: CreateRelicRequest): Promise<RelicObject> {
  return request({
    url: '/api/v1/artifacts',
    method: 'post',
    data
  })
}

export function updateRelic(objectId: string, data: UpdateRelicRequest): Promise<RelicObject> {
  return request({
    url: `/v1/artifacts/${objectId}`,
    method: 'put',
    data
  })
}

export function deleteRelic(objectId: string): Promise<{ objectId: string; isDeleted: number }> {
  return request({
    url: `/v1/artifacts/${objectId}`,
    method: 'delete'
  })
}

export function uploadRelicImage(objectId: string, file: File): Promise<UploadImageResponse> {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/v1/artifacts/${objectId}/image`,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function importCsv(file: File): Promise<ImportCsvResponse> {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/v1/artifacts/import-csv',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportCsv(params: {
  keyword?: string
  museum?: string
}): Promise<Blob> {
  return request({
    url: '/api/v1/artifacts/export-csv',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function getMuseumList(params: {
  page?: number
  size?: number
  keyword?: string
}): Promise<PageResult<MuseumObject>> {
  return request({
    url: '/api/v1/data/museums',
    method: 'get',
    params
  })
}

export function getAllMuseums(): Promise<MuseumObject[]> {
  return request({
    url: '/api/v1/data/museums',
    method: 'get',
    params: { page: 1, size: 100 }
  }).then((res: any) => res.items || res.records || [])
}

export function getArtifactFilters(): Promise<ArtifactFilters> {
  return request({
    url: '/api/v1/artifacts/filters',
    method: 'get'
  })
}

export function getInteractionSummary(objectId: string): Promise<InteractionSummary> {
  return request({
    url: `/api/v1/artifacts/${objectId}/interaction-summary`,
    method: 'get'
  })
}

export function getRelatedArtifacts(objectId: string, count?: number): Promise<{ items: RelatedArtifact[] }> {
  return request({
    url: `/api/v1/artifacts/${objectId}/related`,
    method: 'get',
    params: { count }
  })
}