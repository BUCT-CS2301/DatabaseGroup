import request from '@/utils/request'

export interface GraphNode {
  id: string
  name: string
  type: 'museum' | 'relic' | 'type' | 'period' | 'material'
  category: number
  symbolSize: number
  value: number
  itemStyle?: {
    color: string
  }
  x?: number
  y?: number
}

export interface GraphEdge {
  source: string
  target: string
  name: string
  lineStyle?: {
    color: string
    width: number
    type: 'solid' | 'dashed'
  }
}

export interface GraphData {
  nodes: GraphNode[]
  edges: GraphEdge[]
  stats: {
    nodeCount: number
    edgeCount: number
    museumCount: number
    relicCount: number
    typeCount: number
    periodCount: number
    materialCount: number
  }
}

export function getGraphData(): Promise<GraphData> {
  return request({
    url: '/api/v1/knowledge-graph',
    method: 'get'
  })
}