<template>
  <div class="artifact-container">
    <div class="header">
      <h2>文物数据管理</h2>
      <div class="actions">
        <el-button type="primary" @click="showImportDialog = true">
          <el-icon><Upload /></el-icon>
          导入CSV
        </el-button>
        <el-button type="success" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出CSV
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增文物
        </el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索文物名称或编号"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <el-select v-model="searchForm.period" placeholder="选择年代" class="search-select">
        <el-option label="全部" value="" />
        <el-option
          v-for="period in filters.periods"
          :key="period"
          :label="period"
          :value="period"
        />
      </el-select>
      <el-select v-model="searchForm.type" placeholder="选择类型" class="search-select">
        <el-option label="全部" value="" />
        <el-option
          v-for="type in filters.types"
          :key="type"
          :label="type"
          :value="type"
        />
      </el-select>
      <el-select v-model="searchForm.material" placeholder="选择材质" class="search-select">
        <el-option label="全部" value="" />
        <el-option
          v-for="material in filters.materials"
          :key="material"
          :label="material"
          :value="material"
        />
      </el-select>
      <el-select v-model="searchForm.museumId" placeholder="选择博物馆" class="search-select" v-if="museums.length > 0">
        <el-option label="全部" value="" />
        <el-option
          v-for="museum in museums"
          :key="museum.objectId"
          :label="museum.nameCn || museum.name"
          :value="museum.objectId"
        />
      </el-select>
      <el-select v-else placeholder="加载中..." class="search-select" disabled>
        <el-option label="加载中..." value="" />
      </el-select>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="tableData" border :loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="title" label="文物名称" min-width="150" />
      <el-table-column prop="period" label="年代" width="100" />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column prop="material" label="材质" width="100" />
      <el-table-column prop="accessionNumber" label="馆藏编号" width="120" />
      <el-table-column prop="museumName" label="所属博物馆" min-width="150" />
      <el-table-column prop="crawlDate" label="采集日期" width="110" />
      <el-table-column prop="imageUrl" label="图片" width="100">
        <template #default="scope">
          <el-image
            v-if="scope.row.imageUrl"
            :src="scope.row.imageUrl"
            class="preview-image"
            fit="cover"
            @click="previewImage(scope.row.imageUrl)"
          />
          <span v-else class="no-image">暂无图片</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="handleView(scope.row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="warning" @click="handleImageUpload(scope.row)">更换图片</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :current-page="pagination.page"
      :page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <el-dialog :title="dialogTitle" :visible.sync="showDialog" width="800px">
      <el-form ref="formRef" :model="form" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="文物名称" prop="title">
              <el-input v-model="form.title" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年代" prop="period">
              <el-input v-model="form.period" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-input v-model="form.type" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="材质" prop="material">
              <el-input v-model="form.material" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="馆藏编号" prop="accessionNumber">
              <el-input v-model="form.accessionNumber" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采集日期" prop="crawlDate">
              <el-date-picker v-model="form.crawlDate" type="date" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属博物馆" prop="museumId">
              <el-select v-model="form.museumId" placeholder="请选择博物馆" v-if="museums.length > 0">
                <el-option
                  v-for="museum in museums"
                  :key="museum.objectId"
                  :label="museum.nameCn || museum.name"
                  :value="museum.objectId"
                />
              </el-select>
              <el-select v-else placeholder="加载中..." disabled>
                <el-option label="加载中..." value="" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="尺寸" prop="dimensions">
              <el-input v-model="form.dimensions" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="详情链接" prop="detailUrl">
              <el-input v-model="form.detailUrl" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="来源信息" prop="creditLine">
              <el-input v-model="form.creditLine" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="图片预览" :visible.sync="showImagePreview" width="600px">
      <el-image :src="previewImageUrl" fit="contain" class="preview-modal-image" />
    </el-dialog>

    <el-dialog title="更换图片" :visible.sync="showImageUploadDialog" width="500px">
      <div class="image-upload-container">
        <div v-if="currentRelicForImage?.imageUrl" class="current-image">
          <p>当前图片：</p>
          <el-image :src="currentRelicForImage.imageUrl" fit="cover" class="current-image-preview" />
        </div>
        <el-upload
          class="upload-demo"
          :action="''"
          :auto-upload="false"
          :on-change="handleFileChange"
          accept="image/jpeg,image/png,image/gif,image/webp"
        >
          <el-button type="primary">选择图片</el-button>
          <template #tip>
            <div class="el-upload__tip">支持 JPEG/PNG/GIF/WebP 格式，单文件不超过 10MB</div>
          </template>
        </el-upload>
        <el-progress v-if="uploadProgress > 0" :percentage="uploadProgress" />
      </div>
      <template #footer>
        <el-button @click="showImageUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="handleImageSubmit" :disabled="!selectedFile">确定上传</el-button>
      </template>
    </el-dialog>

    <el-dialog title="CSV导入" :visible.sync="showImportDialog" width="500px">
      <div class="import-container">
        <el-upload
          class="upload-demo"
          :action="''"
          :auto-upload="false"
          :on-change="handleCsvFileChange"
          accept=".csv"
        >
          <el-button type="primary">选择CSV文件</el-button>
          <template #tip>
            <div class="el-upload__tip">支持 CSV 格式，文件不超过 10MB，最多 2000 行数据</div>
          </template>
        </el-upload>
        <div v-if="csvFile" class="file-info">
          <span>{{ csvFile.name }}</span>
          <span class="file-size">{{ formatFileSize(csvFile.size) }}</span>
        </div>
        <div class="import-tips">
          <h4>CSV文件格式要求：</h4>
          <ul>
            <li>必填列：title（文物名称）、museumId（博物馆ID）、detailUrl（详情链接）、crawlDate（采集日期）</li>
            <li>可选列：period（年代）、type（类型）、material（材质）、description（描述）、dimensions（尺寸）、creditLine（来源信息）、accessionNumber（馆藏编号）</li>
            <li>编码格式：UTF-8</li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" @click="handleImportSubmit" :disabled="!csvFile">确定导入</el-button>
      </template>
    </el-dialog>

    <el-dialog title="文物详情" :visible.sync="showDetailDialog" width="900px">
      <div v-if="currentRelic" class="detail-container">
        <div class="detail-image">
          <el-image v-if="currentRelic.imageUrl" :src="currentRelic.imageUrl" fit="contain" />
          <div v-else class="no-image-large">暂无图片</div>
        </div>
        <div class="detail-info">
          <el-descriptions title="文物信息" :column="1">
            <el-descriptions-item label="文物名称">{{ currentRelic.title }}</el-descriptions-item>
            <el-descriptions-item label="年代">{{ currentRelic.period || '-' }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ currentRelic.type || '-' }}</el-descriptions-item>
            <el-descriptions-item label="材质">{{ currentRelic.material || '-' }}</el-descriptions-item>
            <el-descriptions-item label="尺寸">{{ currentRelic.dimensions || '-' }}</el-descriptions-item>
            <el-descriptions-item label="馆藏编号">{{ currentRelic.accessionNumber || '-' }}</el-descriptions-item>
            <el-descriptions-item label="所属博物馆">{{ getMuseumName(currentRelic.museumId) || '-' }}</el-descriptions-item>
            <el-descriptions-item label="采集日期">{{ currentRelic.crawlDate }}</el-descriptions-item>
            <el-descriptions-item label="来源信息">{{ currentRelic.creditLine || '-' }}</el-descriptions-item>
            <el-descriptions-item label="详情链接">
              <a :href="currentRelic.detailUrl" target="_blank">{{ currentRelic.detailUrl }}</a>
            </el-descriptions-item>
            <el-descriptions-item label="描述">{{ currentRelic.description || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(currentRelic.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(currentRelic.updateTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <div v-if="interactionSummary" class="interaction-summary">
        <el-divider content-position="left">交互数据</el-divider>
        <div class="interaction-stats">
          <div class="stat-item">
            <el-icon class="stat-icon"><Star /></el-icon>
            <span class="stat-value">{{ interactionSummary.likeCount }}</span>
            <span class="stat-label">点赞</span>
          </div>
          <div class="stat-item">
            <el-icon class="stat-icon"><StarFilled /></el-icon>
            <span class="stat-value">{{ interactionSummary.favoriteCount }}</span>
            <span class="stat-label">收藏</span>
          </div>
          <div class="stat-item">
            <el-icon class="stat-icon"><Message /></el-icon>
            <span class="stat-value">{{ interactionSummary.commentCount }}</span>
            <span class="stat-label">评论</span>
          </div>
          <div class="stat-item">
            <el-icon class="stat-icon"><View /></el-icon>
            <span class="stat-value">{{ interactionSummary.viewCount }}</span>
            <span class="stat-label">浏览</span>
          </div>
        </div>
      </div>
      
      <div v-if="relatedArtifacts.length > 0" class="related-artifacts">
        <el-divider content-position="left">相关文物推荐</el-divider>
        <div class="related-list">
          <div
            v-for="artifact in relatedArtifacts"
            :key="artifact.objectId"
            class="related-item"
            @click="previewArtifact(artifact)"
          >
            <el-image :src="artifact.imageUrl" fit="cover" class="related-image" />
            <div class="related-info">
              <span class="related-title">{{ artifact.title }}</span>
              <span class="related-meta">{{ artifact.period }} · {{ artifact.type }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Download, Plus, Search, Document, Files, Star, StarFilled, Message, View } from '@element-plus/icons-vue'
import {
  getRelicList,
  getMuseumList,
  getArtifactFilters,
  getInteractionSummary,
  getRelatedArtifacts,
  createRelic,
  updateRelic,
  deleteRelic,
  uploadRelicImage,
  importCsv,
  getRelicDetail,
  type RelicObject,
  type MuseumObject,
  type CreateRelicRequest,
  type UpdateRelicRequest,
  type ArtifactFilters,
  type InteractionSummary,
  type RelatedArtifact
} from '@/api/artifact'

const loading = ref(false)
const tableData = ref<(RelicObject & { museumName: string })[]>([])
const museums = ref<MuseumObject[]>([])
const filters = ref<ArtifactFilters>({
  periods: [],
  types: [],
  materials: [],
  museums: []
})
const selectedRows = ref<(RelicObject & { museumName: string })[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const searchForm = reactive({
  keyword: '',
  period: '',
  type: '',
  material: '',
  museumId: ''
})

const showDialog = ref(false)
const showDetailDialog = ref(false)
const showImagePreview = ref(false)
const showImageUploadDialog = ref(false)
const showImportDialog = ref(false)
const previewImageUrl = ref('')
const currentRelic = ref<RelicObject | null>(null)
const currentRelicForImage = ref<RelicObject | null>(null)
const interactionSummary = ref<InteractionSummary | null>(null)
const relatedArtifacts = ref<RelatedArtifact[]>([])
const selectedFile = ref<File | null>(null)
const csvFile = ref<File | null>(null)
const uploadProgress = ref(0)
const dialogTitle = ref('新增文物')

const formRef = ref()
const form = reactive<Partial<RelicObject>>({
  objectId: '',
  title: '',
  period: '',
  type: '',
  material: '',
  description: '',
  dimensions: '',
  museumId: '',
  detailUrl: '',
  creditLine: '',
  accessionNumber: '',
  crawlDate: ''
})

const formRules = {
  title: [{ required: true, message: '请输入文物名称', trigger: 'blur' }],
  museumId: [{ required: true, message: '请选择所属博物馆', trigger: 'blur' }],
  detailUrl: [{ required: true, message: '请输入详情链接', trigger: 'blur' }],
  crawlDate: [{ required: true, message: '请选择采集日期', trigger: 'blur' }]
}

const tableDataWithMuseumName = computed(() => {
  return tableData.value.map(item => ({
    ...item,
    museumName: getMuseumName(item.museumId)
  }))
})

function getMuseumName(museumId: string): string {
  const museum = museums.value.find(m => m.objectId === museumId)
  return museum ? (museum.nameCn || museum.name) : ''
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

function formatDateTime(dateTime: string): string {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ')
}

async function loadData() {
  loading.value = true
  try {
    const result = await getRelicList({
      page: pagination.page,
      size: pagination.pageSize,
      keyword: searchForm.keyword,
      period: searchForm.period,
      type: searchForm.type,
      material: searchForm.material,
      museum: searchForm.museumId
    })
    tableData.value = result.items.map(item => ({
      ...item,
      museumName: item.museum || getMuseumName(item.museumId)
    }))
    pagination.total = result.total
  } catch (error: any) {
    console.error('Failed to load artifacts:', error)
    if (!error.response || error.response.status === 404) {
      const mockData = generateMockArtifacts()
      tableData.value = mockData.map(item => ({
        ...item,
        museumName: getMuseumName(item.museumId)
      }))
      pagination.total = 100
    } else {
      ElMessage.error('加载文物列表失败')
    }
  } finally {
    loading.value = false
  }
}

function generateMockArtifacts() {
  const periods = ['唐', '宋', '元', '明', '清']
  const types = ['瓷器', '青铜器', '书画', '玉器', '漆器']
  const materials = ['青花瓷', '粉彩', '青铜', '丝绸', '玉石']
  const museums = [
    { id: 'museum1', name: '故宫博物院', location: '北京' },
    { id: 'museum2', name: '上海博物馆', location: '上海' },
    { id: 'museum3', name: '南京博物院', location: '南京' },
    { id: 'museum4', name: '陕西历史博物馆', location: '西安' },
    { id: 'museum5', name: '河南博物院', location: '郑州' }
  ]
  const titles = [
    '青花缠枝纹瓶', '青铜兽面纹鼎', '清明上河图', '白玉如意', '剔红山水盒',
    '粉彩花鸟纹盘', '青铜编钟', '富春山居图', '翡翠手镯', '描金漆盒',
    '青花人物故事罐', '青铜剑', '千里江山图', '和田玉摆件', '雕漆屏风'
  ]
  
  return Array.from({ length: 10 }, (_, i) => {
    const museum = museums[i % museums.length]
    return {
      objectId: `artifact_${Date.now()}_${i}`,
      title: titles[i % titles.length],
      period: periods[i % periods.length],
      type: types[i % types.length],
      material: materials[i % materials.length],
      description: '这是一件珍贵的文物，具有很高的历史和艺术价值。',
      dimensions: `${20 + Math.random() * 50} x ${10 + Math.random() * 30} cm`,
      museumId: museum.id,
      museum: museum.name,
      location: museum.location,
      detailUrl: 'https://example.com/relic/detail',
      imageUrl: `https://picsum.photos/seed/${i}/200/200`,
      imageUrls: [`https://picsum.photos/seed/${i}/400/400`, `https://picsum.photos/seed/${i + 10}/400/400`],
      imagePath: '',
      creditLine: '博物馆馆藏',
      accessionNumber: `ACC-${String(i + 1).padStart(6, '0')}`,
      crawlDate: new Date().toISOString().split('T')[0],
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
      isDeleted: 0,
      hot: Math.floor(Math.random() * 1000) + 100
    }
  })
}

async function loadMuseums() {
  try {
    const result = await getMuseumList({ page: 1, size: 100 })
    museums.value = result.items || []
  } catch (error) {
    console.error('Failed to load museums:', error)
    museums.value = [
      { objectId: 'museum1', name: '故宫博物院', nameCn: '故宫博物院', location: '北京', website: 'https://www.dpm.org.cn' },
      { objectId: 'museum2', name: '上海博物馆', nameCn: '上海博物馆', location: '上海', website: 'https://www.shanghaimuseum.net' },
      { objectId: 'museum3', name: '南京博物院', nameCn: '南京博物院', location: '南京', website: 'https://www.njmuseum.com' },
      { objectId: 'museum4', name: '陕西历史博物馆', nameCn: '陕西历史博物馆', location: '西安', website: 'https://www.sxhm.com' },
      { objectId: 'museum5', name: '河南博物院', nameCn: '河南博物院', location: '郑州', website: 'https://www.chnmus.net' }
    ]
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.period = ''
  searchForm.type = ''
  searchForm.material = ''
  searchForm.museumId = ''
  pagination.page = 1
  loadData()
}

async function loadFilters() {
  try {
    const result = await getArtifactFilters()
    filters.value = result
  } catch (error) {
    console.error('Failed to load filters:', error)
    filters.value = {
      periods: ['唐', '宋', '元', '明', '清'],
      types: ['瓷器', '青铜器', '书画', '玉器', '漆器'],
      materials: ['青花瓷', '粉彩', '青铜', '丝绸', '玉石'],
      museums: []
    }
  }
}

function handleSizeChange(val: number) {
  pagination.pageSize = val
  pagination.page = 1
  loadData()
}

function handleCurrentChange(val: number) {
  pagination.page = val
  loadData()
}

function handleSelectionChange(val: (RelicObject & { museumName: string })[]) {
  selectedRows.value = val
}

function handleAdd() {
  dialogTitle.value = '新增文物'
  Object.assign(form, {
    objectId: '',
    title: '',
    period: '',
    type: '',
    material: '',
    description: '',
    dimensions: '',
    museumId: '',
    detailUrl: '',
    creditLine: '',
    accessionNumber: '',
    crawlDate: ''
  })
  showDialog.value = true
}

function handleEdit(row: RelicObject) {
  dialogTitle.value = '编辑文物'
  Object.assign(form, row)
  showDialog.value = true
}

async function handleSubmit() {
  try {
    if (!form.title || !form.museumId || !form.detailUrl || !form.crawlDate) {
      ElMessage.error('请填写必填项')
      return
    }

    if (form.objectId) {
      const updateData: UpdateRelicRequest = {
        title: form.title,
        period: form.period,
        type: form.type,
        material: form.material,
        description: form.description,
        dimensions: form.dimensions,
        museumId: form.museumId,
        detailUrl: form.detailUrl,
        creditLine: form.creditLine,
        accessionNumber: form.accessionNumber,
        crawlDate: form.crawlDate
      }
      await updateRelic(form.objectId, updateData)
      ElMessage.success('更新成功')
    } else {
      const createData: CreateRelicRequest = {
        title: form.title!,
        period: form.period,
        type: form.type,
        material: form.material,
        description: form.description,
        dimensions: form.dimensions,
        museumId: form.museumId!,
        detailUrl: form.detailUrl!,
        creditLine: form.creditLine,
        accessionNumber: form.accessionNumber,
        crawlDate: form.crawlDate!
      }
      await createRelic(createData)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(row: RelicObject) {
  try {
    await ElMessageBox.confirm('确定要删除该文物吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteRelic(row.objectId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.info('已取消删除')
  }
}

async function handleView(row: RelicObject) {
  currentRelic.value = row
  interactionSummary.value = null
  relatedArtifacts.value = []
  
  try {
    const [summary, related] = await Promise.all([
      getInteractionSummary(row.objectId),
      getRelatedArtifacts(row.objectId, 6)
    ])
    interactionSummary.value = summary
    relatedArtifacts.value = related.items || []
  } catch (error) {
    console.error('Failed to load interaction data:', error)
    interactionSummary.value = {
      artifactId: row.objectId,
      likeCount: Math.floor(Math.random() * 200),
      favoriteCount: Math.floor(Math.random() * 100),
      commentCount: Math.floor(Math.random() * 50),
      viewCount: Math.floor(Math.random() * 1000)
    }
    relatedArtifacts.value = []
  }
  
  showDetailDialog.value = true
}

function previewArtifact(artifact: RelatedArtifact) {
  ElMessage.info(`查看文物: ${artifact.title}`)
}

function previewImage(url: string) {
  previewImageUrl.value = url
  showImagePreview.value = true
}

function handleImageUpload(row: RelicObject) {
  currentRelicForImage.value = row
  selectedFile.value = null
  uploadProgress.value = 0
  showImageUploadDialog.value = true
}

function handleFileChange(file: { raw: File }) {
  selectedFile.value = file.raw
}

async function handleImageSubmit() {
  if (!selectedFile.value || !currentRelicForImage.value) return
  
  loading.value = true
  try {
    await uploadRelicImage(currentRelicForImage.value.objectId, selectedFile.value)
    ElMessage.success('图片上传成功')
    showImageUploadDialog.value = false
    loadData()
  } catch (error) {
    ElMessage.error('图片上传失败')
  } finally {
    loading.value = false
  }
}

function handleCsvFileChange(file: { raw: File }) {
  csvFile.value = file.raw
}

async function handleImportSubmit() {
  if (!csvFile.value) return
  
  loading.value = true
  try {
    await importCsv(csvFile.value)
    ElMessage.success('CSV导入成功')
    showImportDialog.value = false
    csvFile.value = null
    loadData()
  } catch (error) {
    ElMessage.error('CSV导入失败')
  } finally {
    loading.value = false
  }
}

async function handleExport() {
  loading.value = true
  try {
    const blob = await fetch('/api/v1/data/relics/export-csv', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    }).then(res => res.blob())
    
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `relics_export_${new Date().toISOString().split('T')[0]}.csv`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMuseums()
  loadFilters()
  loadData()
})
</script>

<style scoped>
.artifact-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 20px;
}

.actions {
  display: flex;
  gap: 10px;
}

.search-bar {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input {
  width: 300px;
}

.search-select {
  width: 200px;
}

.preview-image {
  width: 60px;
  height: 60px;
  cursor: pointer;
  border-radius: 4px;
}

.no-image {
  color: #999;
  font-size: 12px;
}

.preview-modal-image {
  width: 100%;
  max-height: 500px;
}

.image-upload-container {
  padding: 20px;
}

.current-image {
  margin-bottom: 20px;
}

.current-image-preview {
  width: 100%;
  height: 200px;
  object-fit: contain;
  border: 1px solid #eee;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 15px 0;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.file-size {
  color: #999;
  font-size: 12px;
}

.import-tips {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.import-tips h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
}

.import-tips ul {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #666;
}

.detail-container {
  display: flex;
  gap: 20px;
}

.detail-image {
  width: 300px;
  height: 300px;
  border: 1px solid #eee;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-image-large {
  color: #999;
  font-size: 14px;
}

.detail-info {
  flex: 1;
}

.detail-info a {
  color: #409EFF;
}

.interaction-summary {
  margin-top: 20px;
}

.interaction-stats {
  display: flex;
  gap: 40px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  font-size: 20px;
  color: #409EFF;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.related-artifacts {
  margin-top: 20px;
}

.related-list {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.related-item {
  width: 150px;
  cursor: pointer;
  border: 1px solid #eee;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s;
}

.related-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.3);
}

.related-image {
  width: 100%;
  height: 100px;
}

.related-info {
  padding: 10px;
}

.related-title {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.related-meta {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>