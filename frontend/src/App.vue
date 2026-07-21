<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { request } from "./api";

interface Game {
  id: number;
  name: string;
  platform: string;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}

interface GamePage {
  items: Game[];
  total: number;
  page: number;
  size: number;
}

interface TestScene {
  id: number;
  gameId: number;
  name: string;
  method: string;
  durationSeconds: number | null;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}

interface ConfigTemplate {
  id: number;
  gameId: number;
  name: string;
  resolution: string | null;
  graphicsPreset: string | null;
  upscalingTech: string | null;
  upscalingQuality: string | null;
  vsyncEnabled: boolean | null;
  frameGenerationEnabled: boolean | null;
  gpuCoreClockMhz: number | null;
  gpuVoltageMv: number | null;
  gpuMemoryClockMhz: number | null;
  gpuPowerLimitPercent: number | null;
  driverVersion: string | null;
  customDescription: string | null;
  createdAt: string;
  updatedAt: string;
}

interface BenchmarkRecord {
  id: number;
  gameId: number;
  sceneId: number | null;
  templateId: number | null;
  recordedAt: string | null;
  avgFps: number;
  minFps: number;
  gpuTempCelsius: number | null;
  cpuTempCelsius: number | null;
  gpuPowerWatt: number | null;
  cpuUsagePercent: number | null;
  frameTimeMs: number;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

const view = ref<"games" | "scenes" | "templates" | "records">("games");
const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const keyword = ref("");
const games = ref<Game[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const form = reactive({ name: "", platform: "", remark: "" });

const selectedGame = ref<Game | null>(null);
const sceneLoading = ref(false);
const sceneSubmitting = ref(false);
const sceneDialogVisible = ref(false);
const editingSceneId = ref<number | null>(null);
const scenes = ref<TestScene[]>([]);
const sceneForm = reactive<{ name: string; method: string; durationSeconds: number | null; remark: string }>({
  name: "",
  method: "",
  durationSeconds: null,
  remark: ""
});

const templateLoading = ref(false);
const templateSubmitting = ref(false);
const templateDialogVisible = ref(false);
const editingTemplateId = ref<number | null>(null);
const templates = ref<ConfigTemplate[]>([]);
const templateForm = reactive<{
  name: string;
  resolution: string;
  graphicsPreset: string;
  upscalingTech: string;
  upscalingQuality: string;
  vsyncEnabled: boolean;
  frameGenerationEnabled: boolean;
  gpuCoreClockMhz: number | null;
  gpuVoltageMv: number | null;
  gpuMemoryClockMhz: number | null;
  gpuPowerLimitPercent: number | null;
  driverVersion: string;
  customDescription: string;
}>({
  name: "",
  resolution: "",
  graphicsPreset: "",
  upscalingTech: "",
  upscalingQuality: "",
  vsyncEnabled: false,
  frameGenerationEnabled: false,
  gpuCoreClockMhz: null,
  gpuVoltageMv: null,
  gpuMemoryClockMhz: null,
  gpuPowerLimitPercent: null,
  driverVersion: "",
  customDescription: ""
});

const recordLoading = ref(false);
const recordSubmitting = ref(false);
const recordDialogVisible = ref(false);
const editingRecordId = ref<number | null>(null);
const records = ref<BenchmarkRecord[]>([]);
const recordScenes = ref<TestScene[]>([]);
const recordTemplates = ref<ConfigTemplate[]>([]);
const recordForm = reactive<{
  sceneId: number | null;
  templateId: number | null;
  recordedAt: string;
  avgFps: number | null;
  minFps: number | null;
  gpuTempCelsius: number | null;
  cpuTempCelsius: number | null;
  gpuPowerWatt: number | null;
  cpuUsagePercent: number | null;
  frameTimeMs: number | null;
  notes: string;
}>({
  sceneId: null,
  templateId: null,
  recordedAt: "",
  avgFps: null,
  minFps: null,
  gpuTempCelsius: null,
  cpuTempCelsius: null,
  gpuPowerWatt: null,
  cpuUsagePercent: null,
  frameTimeMs: null,
  notes: ""
});

const dialogTitle = computed(() => editingId.value === null ? "新增游戏" : "编辑游戏");
const sceneDialogTitle = computed(() => editingSceneId.value === null ? "新增测试场景" : "编辑测试场景");
const templateDialogTitle = computed(() => editingTemplateId.value === null ? "新增配置模板" : "编辑配置模板");
const recordDialogTitle = computed(() => editingRecordId.value === null ? "新增测试记录" : "编辑测试记录");
const isEditingRecord = computed(() => editingRecordId.value !== null);

function nowLocalString(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

async function loadGames(): Promise<void> {
  loading.value = true;
  try {
    const query = new URLSearchParams({ page: String(page.value), size: String(size.value) });
    if (keyword.value.trim()) query.set("keyword", keyword.value.trim());
    const data = await request<GamePage>(`/api/games?${query.toString()}`);
    games.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "加载游戏失败");
  } finally {
    loading.value = false;
  }
}

function openCreate(): void {
  editingId.value = null;
  Object.assign(form, { name: "", platform: "", remark: "" });
  dialogVisible.value = true;
}

function openEdit(game: Game): void {
  editingId.value = game.id;
  Object.assign(form, { name: game.name, platform: game.platform, remark: game.remark ?? "" });
  dialogVisible.value = true;
}

async function saveGame(): Promise<void> {
  if (!form.name.trim()) {
    ElMessage.warning("请填写游戏名称");
    return;
  }
  submitting.value = true;
  try {
    const body = JSON.stringify({ name: form.name, platform: form.platform, remark: form.remark || null });
    const url = editingId.value === null ? "/api/games" : `/api/games/${editingId.value}`;
    await request<Game>(url, { method: editingId.value === null ? "POST" : "PUT", body });
    ElMessage.success(editingId.value === null ? "游戏已保存" : "游戏已更新");
    dialogVisible.value = false;
    await loadGames();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "保存失败");
  } finally {
    submitting.value = false;
  }
}

async function removeGame(game: Game): Promise<void> {
  try {
    await ElMessageBox.confirm(`删除“${game.name}”会同时删除其场景、模板和记录。`, "确认删除", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消"
    });
    await request<void>(`/api/games/${game.id}`, { method: "DELETE" });
    ElMessage.success("游戏已删除");
    if (games.value.length === 1 && page.value > 1) page.value -= 1;
    await loadGames();
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(error instanceof Error ? error.message : "删除失败");
    }
  }
}

async function openScenes(game: Game): Promise<void> {
  selectedGame.value = game;
  view.value = "scenes";
  await loadScenes();
}

async function openTemplates(game: Game): Promise<void> {
  selectedGame.value = game;
  view.value = "templates";
  await loadTemplates();
}

async function openRecords(game: Game): Promise<void> {
  selectedGame.value = game;
  view.value = "records";
  await Promise.all([loadRecords(), loadRecordScenes(), loadRecordTemplates()]);
}

function returnToGames(): void {
  view.value = "games";
  selectedGame.value = null;
  scenes.value = [];
  templates.value = [];
  records.value = [];
  recordScenes.value = [];
  recordTemplates.value = [];
}

async function loadScenes(): Promise<void> {
  if (selectedGame.value === null) return;
  sceneLoading.value = true;
  try {
    scenes.value = await request<TestScene[]>(`/api/games/${selectedGame.value.id}/scenes`);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "加载测试场景失败");
  } finally {
    sceneLoading.value = false;
  }
}

function openSceneCreate(): void {
  editingSceneId.value = null;
  Object.assign(sceneForm, { name: "", method: "", durationSeconds: null, remark: "" });
  sceneDialogVisible.value = true;
}

function openSceneEdit(scene: TestScene): void {
  editingSceneId.value = scene.id;
  Object.assign(sceneForm, {
    name: scene.name,
    method: scene.method,
    durationSeconds: scene.durationSeconds,
    remark: scene.remark ?? ""
  });
  sceneDialogVisible.value = true;
}

async function saveScene(): Promise<void> {
  if (!sceneForm.name.trim() || !sceneForm.method.trim()) {
    ElMessage.warning("请填写场景名称和测试方法");
    return;
  }
  if (sceneForm.durationSeconds !== null && sceneForm.durationSeconds <= 0) {
    ElMessage.warning("测试时长必须大于 0");
    return;
  }
  if (selectedGame.value === null) return;

  sceneSubmitting.value = true;
  try {
    const body = JSON.stringify({
      name: sceneForm.name,
      method: sceneForm.method,
      durationSeconds: sceneForm.durationSeconds,
      remark: sceneForm.remark || null
    });
    const url = editingSceneId.value === null
      ? `/api/games/${selectedGame.value.id}/scenes`
      : `/api/scenes/${editingSceneId.value}`;
    await request<TestScene>(url, { method: editingSceneId.value === null ? "POST" : "PUT", body });
    ElMessage.success(editingSceneId.value === null ? "测试场景已保存" : "测试场景已更新");
    sceneDialogVisible.value = false;
    await loadScenes();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "保存测试场景失败");
  } finally {
    sceneSubmitting.value = false;
  }
}

async function removeScene(scene: TestScene): Promise<void> {
  try {
    await ElMessageBox.confirm(`删除“${scene.name}”后，后续历史记录会保留场景快照。`, "确认删除", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消"
    });
    await request<void>(`/api/scenes/${scene.id}`, { method: "DELETE" });
    ElMessage.success("测试场景已删除");
    await loadScenes();
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(error instanceof Error ? error.message : "删除测试场景失败");
    }
  }
}

async function loadTemplates(): Promise<void> {
  if (selectedGame.value === null) return;
  templateLoading.value = true;
  try {
    templates.value = await request<ConfigTemplate[]>(`/api/games/${selectedGame.value.id}/config-templates`);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "加载配置模板失败");
  } finally {
    templateLoading.value = false;
  }
}

function resetTemplateForm(): void {
  Object.assign(templateForm, {
    name: "",
    resolution: "",
    graphicsPreset: "",
    upscalingTech: "",
    upscalingQuality: "",
    vsyncEnabled: false,
    frameGenerationEnabled: false,
    gpuCoreClockMhz: null,
    gpuVoltageMv: null,
    gpuMemoryClockMhz: null,
    gpuPowerLimitPercent: null,
    driverVersion: "",
    customDescription: ""
  });
}

function openTemplateCreate(): void {
  editingTemplateId.value = null;
  resetTemplateForm();
  templateDialogVisible.value = true;
}

function openTemplateEdit(template: ConfigTemplate): void {
  editingTemplateId.value = template.id;
  Object.assign(templateForm, {
    name: template.name,
    resolution: template.resolution ?? "",
    graphicsPreset: template.graphicsPreset ?? "",
    upscalingTech: template.upscalingTech ?? "",
    upscalingQuality: template.upscalingQuality ?? "",
    vsyncEnabled: template.vsyncEnabled ?? false,
    frameGenerationEnabled: template.frameGenerationEnabled ?? false,
    gpuCoreClockMhz: template.gpuCoreClockMhz,
    gpuVoltageMv: template.gpuVoltageMv,
    gpuMemoryClockMhz: template.gpuMemoryClockMhz,
    gpuPowerLimitPercent: template.gpuPowerLimitPercent,
    driverVersion: template.driverVersion ?? "",
    customDescription: template.customDescription ?? ""
  });
  templateDialogVisible.value = true;
}

function formatEmpty(value: string | null | undefined): string {
  return value && value.trim() ? value : "-";
}

async function saveTemplate(): Promise<void> {
  if (!templateForm.name.trim()) {
    ElMessage.warning("请填写模板名称");
    return;
  }
  if (templateForm.gpuPowerLimitPercent !== null
    && (templateForm.gpuPowerLimitPercent < -100 || templateForm.gpuPowerLimitPercent > 100)) {
    ElMessage.warning("GPU 功耗限制必须在 -100 到 100 之间");
    return;
  }
  if (selectedGame.value === null) return;

  templateSubmitting.value = true;
  try {
    const body = JSON.stringify({
      name: templateForm.name,
      resolution: templateForm.resolution || null,
      graphicsPreset: templateForm.graphicsPreset || null,
      upscalingTech: templateForm.upscalingTech || null,
      upscalingQuality: templateForm.upscalingQuality || null,
      vsyncEnabled: templateForm.vsyncEnabled,
      frameGenerationEnabled: templateForm.frameGenerationEnabled,
      gpuCoreClockMhz: templateForm.gpuCoreClockMhz,
      gpuVoltageMv: templateForm.gpuVoltageMv,
      gpuMemoryClockMhz: templateForm.gpuMemoryClockMhz,
      gpuPowerLimitPercent: templateForm.gpuPowerLimitPercent,
      driverVersion: templateForm.driverVersion || null,
      customDescription: templateForm.customDescription || null
    });
    const url = editingTemplateId.value === null
      ? `/api/games/${selectedGame.value.id}/config-templates`
      : `/api/config-templates/${editingTemplateId.value}`;
    await request<ConfigTemplate>(url, { method: editingTemplateId.value === null ? "POST" : "PUT", body });
    ElMessage.success(editingTemplateId.value === null ? "配置模板已保存" : "配置模板已更新");
    templateDialogVisible.value = false;
    await loadTemplates();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "保存配置模板失败");
  } finally {
    templateSubmitting.value = false;
  }
}

async function removeTemplate(template: ConfigTemplate): Promise<void> {
  try {
    await ElMessageBox.confirm(`删除“${template.name}”不会改变已有历史记录，模板仅作为记录来源。`, "确认删除", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消"
    });
    await request<void>(`/api/config-templates/${template.id}`, { method: "DELETE" });
    ElMessage.success("配置模板已删除");
    await loadTemplates();
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(error instanceof Error ? error.message : "删除配置模板失败");
    }
  }
}

async function loadRecords(): Promise<void> {
  if (selectedGame.value === null) return;
  recordLoading.value = true;
  try {
    records.value = await request<BenchmarkRecord[]>(`/api/games/${selectedGame.value.id}/records`);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "加载测试记录失败");
  } finally {
    recordLoading.value = false;
  }
}

async function loadRecordScenes(): Promise<void> {
  if (selectedGame.value === null) return;
  try {
    recordScenes.value = await request<TestScene[]>(`/api/games/${selectedGame.value.id}/scenes`);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "加载场景失败");
  }
}

async function loadRecordTemplates(): Promise<void> {
  if (selectedGame.value === null) return;
  try {
    recordTemplates.value = await request<ConfigTemplate[]>(`/api/games/${selectedGame.value.id}/config-templates`);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "加载模板失败");
  }
}

function sceneName(id: number | null): string {
  if (id === null) return "-";
  const found = recordScenes.value.find((s) => s.id === id);
  return found ? found.name : `场景#${id}`;
}

function templateName(id: number | null): string {
  if (id === null) return "-";
  const found = recordTemplates.value.find((t) => t.id === id);
  return found ? found.name : `模板#${id}`;
}

function resetRecordForm(): void {
  Object.assign(recordForm, {
    sceneId: null,
    templateId: null,
    recordedAt: nowLocalString(),
    avgFps: null,
    minFps: null,
    gpuTempCelsius: null,
    cpuTempCelsius: null,
    gpuPowerWatt: null,
    cpuUsagePercent: null,
    frameTimeMs: null,
    notes: ""
  });
}

function openRecordCreate(): void {
  editingRecordId.value = null;
  resetRecordForm();
  recordDialogVisible.value = true;
}

function openRecordEdit(record: BenchmarkRecord): void {
  editingRecordId.value = record.id;
  Object.assign(recordForm, {
    sceneId: record.sceneId,
    templateId: record.templateId,
    recordedAt: record.recordedAt ?? nowLocalString(),
    avgFps: record.avgFps,
    minFps: record.minFps,
    gpuTempCelsius: record.gpuTempCelsius,
    cpuTempCelsius: record.cpuTempCelsius,
    gpuPowerWatt: record.gpuPowerWatt,
    cpuUsagePercent: record.cpuUsagePercent,
    frameTimeMs: record.frameTimeMs,
    notes: record.notes ?? ""
  });
  recordDialogVisible.value = true;
}

async function saveRecord(): Promise<void> {
  if (selectedGame.value === null) return;
  if (recordForm.sceneId === null) {
    ElMessage.warning("请选择测试场景");
    return;
  }
  if (recordForm.avgFps === null || recordForm.avgFps <= 0) {
    ElMessage.warning("平均帧率必须大于 0");
    return;
  }
  if (recordForm.minFps === null || recordForm.minFps <= 0) {
    ElMessage.warning("最低帧率必须大于 0");
    return;
  }
  if (recordForm.frameTimeMs === null || recordForm.frameTimeMs <= 0) {
    ElMessage.warning("平均帧时间必须大于 0");
    return;
  }
  if (recordForm.gpuTempCelsius !== null && recordForm.gpuTempCelsius < -273.15) {
    ElMessage.warning("GPU 温度不能低于绝对零度");
    return;
  }
  if (recordForm.cpuTempCelsius !== null && recordForm.cpuTempCelsius < -273.15) {
    ElMessage.warning("CPU 温度不能低于绝对零度");
    return;
  }
  if (recordForm.gpuPowerWatt !== null && recordForm.gpuPowerWatt < 0) {
    ElMessage.warning("GPU 功耗不能为负数");
    return;
  }
  if (recordForm.cpuUsagePercent !== null && (recordForm.cpuUsagePercent < 0 || recordForm.cpuUsagePercent > 100)) {
    ElMessage.warning("CPU 占用必须在 0 到 100 之间");
    return;
  }

  recordSubmitting.value = true;
  try {
    const body = JSON.stringify({
      sceneId: recordForm.sceneId,
      templateId: recordForm.templateId,
      recordedAt: recordForm.recordedAt || null,
      avgFps: recordForm.avgFps,
      minFps: recordForm.minFps,
      gpuTempCelsius: recordForm.gpuTempCelsius,
      cpuTempCelsius: recordForm.cpuTempCelsius,
      gpuPowerWatt: recordForm.gpuPowerWatt,
      cpuUsagePercent: recordForm.cpuUsagePercent,
      frameTimeMs: recordForm.frameTimeMs,
      notes: recordForm.notes || null
    });
    const url = editingRecordId.value === null
      ? `/api/games/${selectedGame.value.id}/records`
      : `/api/records/${editingRecordId.value}`;
    await request<BenchmarkRecord>(url, { method: editingRecordId.value === null ? "POST" : "PUT", body });
    ElMessage.success(editingRecordId.value === null ? "测试记录已保存" : "测试记录已更新");
    recordDialogVisible.value = false;
    await loadRecords();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "保存测试记录失败");
  } finally {
    recordSubmitting.value = false;
  }
}

async function removeRecord(record: BenchmarkRecord): Promise<void> {
  try {
    await ElMessageBox.confirm(`删除该测试记录后不可恢复。`, "确认删除", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消"
    });
    await request<void>(`/api/records/${record.id}`, { method: "DELETE" });
    ElMessage.success("测试记录已删除");
    await loadRecords();
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(error instanceof Error ? error.message : "删除测试记录失败");
    }
  }
}

function formatTime(value: string): string {
  return new Date(value).toLocaleString();
}

function formatValue(value: number | null | undefined): string {
  return value === null || value === undefined ? "-" : String(value);
}

onMounted(loadGames);
</script>

<template>
  <main class="page-shell">
    <template v-if="view === 'games'">
      <section class="hero">
        <div>
          <p class="eyebrow">LOCAL BENCHMARK LIBRARY</p>
          <h1>GameBench Tracker</h1>
          <p>管理游戏、测试场景、配置与性能测试记录。</p>
        </div>
        <el-button type="primary" size="large" :disabled="submitting" @click="openCreate">新增游戏</el-button>
      </section>

      <section class="panel">
        <div class="toolbar">
          <el-input v-model="keyword" clearable placeholder="按游戏名称搜索" @keyup.enter="page = 1; loadGames()" />
          <el-button :loading="loading" @click="page = 1; loadGames()">搜索</el-button>
        </div>

        <el-table v-loading="loading" :data="games" empty-text="还没有游戏，先新增一个。">
          <el-table-column prop="name" label="游戏" min-width="180" />
          <el-table-column prop="platform" label="平台" min-width="130">
            <template #default="scope">{{ scope.row.platform || "未填写" }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="220">
            <template #default="scope">{{ scope.row.remark || "-" }}</template>
          </el-table-column>
          <el-table-column label="更新时间" min-width="180">
            <template #default="scope">{{ formatTime(scope.row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="360" fixed="right">
            <template #default="scope">
              <el-button link type="primary" :disabled="submitting" @click="openScenes(scope.row)">场景</el-button>
              <el-button link type="primary" :disabled="submitting" @click="openTemplates(scope.row)">模板</el-button>
              <el-button link type="primary" :disabled="submitting" @click="openRecords(scope.row)">记录</el-button>
              <el-button link type="primary" :disabled="submitting" @click="openEdit(scope.row)">编辑</el-button>
              <el-button link type="danger" :disabled="submitting" @click="removeGame(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="total > size"
          v-model:current-page="page"
          v-model:page-size="size"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="loadGames"
        />
      </section>
    </template>

    <template v-else-if="view === 'scenes' && selectedGame !== null">
      <section class="hero">
        <div>
          <p class="eyebrow">TEST SCENE LIBRARY</p>
          <h1>{{ selectedGame.name }}</h1>
          <p>管理 {{ selectedGame.platform || "未填写平台" }} 下可重复使用的测试场景。</p>
        </div>
        <div class="hero-actions">
          <el-button :disabled="sceneSubmitting" @click="returnToGames">返回游戏</el-button>
          <el-button type="primary" size="large" :disabled="sceneSubmitting" @click="openSceneCreate">新增场景</el-button>
        </div>
      </section>

      <section class="panel">
        <el-table v-loading="sceneLoading" :data="scenes" empty-text="还没有测试场景，先新增一个。">
          <el-table-column prop="name" label="场景" min-width="180" />
          <el-table-column prop="method" label="测试方法" min-width="180" />
          <el-table-column label="测试时长" width="120">
            <template #default="scope">{{ scope.row.durationSeconds === null ? "-" : `${scope.row.durationSeconds} 秒` }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="220">
            <template #default="scope">{{ scope.row.remark || "-" }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button link type="primary" :disabled="sceneSubmitting" @click="openSceneEdit(scope.row)">编辑</el-button>
              <el-button link type="danger" :disabled="sceneSubmitting" @click="removeScene(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </template>

    <template v-else-if="view === 'templates' && selectedGame !== null">
      <section class="hero">
        <div>
          <p class="eyebrow">CONFIG TEMPLATE LIBRARY</p>
          <h1>{{ selectedGame.name }}</h1>
          <p>管理 {{ selectedGame.platform || "未填写平台" }} 下可复用的显卡配置模板。</p>
        </div>
        <div class="hero-actions">
          <el-button :disabled="templateSubmitting" @click="returnToGames">返回游戏</el-button>
          <el-button type="primary" size="large" :disabled="templateSubmitting" @click="openTemplateCreate">新增模板</el-button>
        </div>
      </section>

      <section class="panel">
        <el-table v-loading="templateLoading" :data="templates" empty-text="还没有配置模板，先新增一个。">
          <el-table-column prop="name" label="模板" min-width="180" />
          <el-table-column prop="resolution" label="分辨率" min-width="120">
            <template #default="scope">{{ formatEmpty(scope.row.resolution) }}</template>
          </el-table-column>
          <el-table-column prop="graphicsPreset" label="图形预设" min-width="120">
            <template #default="scope">{{ formatEmpty(scope.row.graphicsPreset) }}</template>
          </el-table-column>
          <el-table-column label="功耗限制" min-width="110">
            <template #default="scope">{{ scope.row.gpuPowerLimitPercent === null ? "-" : `${scope.row.gpuPowerLimitPercent}%` }}</template>
          </el-table-column>
          <el-table-column prop="driverVersion" label="驱动版本" min-width="140">
            <template #default="scope">{{ formatEmpty(scope.row.driverVersion) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button link type="primary" :disabled="templateSubmitting" @click="openTemplateEdit(scope.row)">编辑</el-button>
              <el-button link type="danger" :disabled="templateSubmitting" @click="removeTemplate(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </template>

    <template v-else-if="view === 'records' && selectedGame !== null">
      <section class="hero">
        <div>
          <p class="eyebrow">BENCHMARK RECORD LIBRARY</p>
          <h1>{{ selectedGame.name }}</h1>
          <p>记录并回顾 {{ selectedGame.platform || "未填写平台" }} 的实测性能数据。</p>
        </div>
        <div class="hero-actions">
          <el-button :disabled="recordSubmitting" @click="returnToGames">返回游戏</el-button>
          <el-button type="primary" size="large" :disabled="recordSubmitting" @click="openRecordCreate">新增记录</el-button>
        </div>
      </section>

      <section class="panel">
        <el-table v-loading="recordLoading" :data="records" empty-text="还没有测试记录，先新增一条。">
          <el-table-column label="测试时间" min-width="170">
            <template #default="scope">{{ scope.row.recordedAt || "-" }}</template>
          </el-table-column>
          <el-table-column label="场景" min-width="160">
            <template #default="scope">{{ sceneName(scope.row.sceneId) }}</template>
          </el-table-column>
          <el-table-column label="模板" min-width="140">
            <template #default="scope">{{ templateName(scope.row.templateId) }}</template>
          </el-table-column>
          <el-table-column label="平均FPS" width="100">
            <template #default="scope">{{ formatValue(scope.row.avgFps) }}</template>
          </el-table-column>
          <el-table-column label="最低FPS" width="100">
            <template #default="scope">{{ formatValue(scope.row.minFps) }}</template>
          </el-table-column>
          <el-table-column label="GPU温度" width="110">
            <template #default="scope">{{ scope.row.gpuTempCelsius === null ? "-" : `${scope.row.gpuTempCelsius}℃` }}</template>
          </el-table-column>
          <el-table-column label="GPU功耗" width="110">
            <template #default="scope">{{ scope.row.gpuPowerWatt === null ? "-" : `${scope.row.gpuPowerWatt}W` }}</template>
          </el-table-column>
          <el-table-column label="CPU占用" width="100">
            <template #default="scope">{{ scope.row.cpuUsagePercent === null ? "-" : `${scope.row.cpuUsagePercent}%` }}</template>
          </el-table-column>
          <el-table-column prop="notes" label="备注" min-width="180">
            <template #default="scope">{{ scope.row.notes || "-" }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button link type="primary" :disabled="recordSubmitting" @click="openRecordEdit(scope.row)">编辑</el-button>
              <el-button link type="danger" :disabled="recordSubmitting" @click="removeRecord(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </template>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px" :close-on-click-modal="!submitting">
      <el-form label-position="top">
        <el-form-item label="游戏名称" required>
          <el-input v-model="form.name" :disabled="submitting" maxlength="120" />
        </el-form-item>
        <el-form-item label="平台">
          <el-input v-model="form.platform" :disabled="submitting" placeholder="例如 Steam、GOG、Epic" maxlength="120" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" :disabled="submitting" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveGame">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="sceneDialogVisible" :title="sceneDialogTitle" width="460px" :close-on-click-modal="!sceneSubmitting">
      <el-form label-position="top">
        <el-form-item label="场景名称" required>
          <el-input v-model="sceneForm.name" :disabled="sceneSubmitting" maxlength="120" />
        </el-form-item>
        <el-form-item label="测试方法" required>
          <el-input v-model="sceneForm.method" :disabled="sceneSubmitting" placeholder="例如固定路线、内置基准测试" maxlength="240" />
        </el-form-item>
        <el-form-item label="测试时长（秒）">
          <el-input-number v-model="sceneForm.durationSeconds" :disabled="sceneSubmitting" :min="1" :precision="0" class="duration-input" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="sceneForm.remark" :disabled="sceneSubmitting" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="sceneSubmitting" @click="sceneDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sceneSubmitting" @click="saveScene">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="templateDialogVisible" :title="templateDialogTitle" width="560px" :close-on-click-modal="!templateSubmitting">
      <el-form label-position="top">
        <el-form-item label="模板名称" required>
          <el-input v-model="templateForm.name" :disabled="templateSubmitting" maxlength="120" />
        </el-form-item>
        <el-form-item label="分辨率">
          <el-input v-model="templateForm.resolution" :disabled="templateSubmitting" placeholder="例如 2560x1440" maxlength="40" />
        </el-form-item>
        <el-form-item label="图形预设">
          <el-input v-model="templateForm.graphicsPreset" :disabled="templateSubmitting" placeholder="例如 高、极致、自定义" maxlength="80" />
        </el-form-item>
        <el-form-item label="超分技术">
          <el-input v-model="templateForm.upscalingTech" :disabled="templateSubmitting" placeholder="例如 DLSS、FSR、XeSS" maxlength="40" />
        </el-form-item>
        <el-form-item label="超分质量">
          <el-input v-model="templateForm.upscalingQuality" :disabled="templateSubmitting" placeholder="例如 质量、平衡、性能" maxlength="40" />
        </el-form-item>
        <el-form-item label="垂直同步">
          <el-switch v-model="templateForm.vsyncEnabled" :disabled="templateSubmitting" />
        </el-form-item>
        <el-form-item label="帧生成">
          <el-switch v-model="templateForm.frameGenerationEnabled" :disabled="templateSubmitting" />
        </el-form-item>
        <el-form-item label="GPU 核心频率（MHz）">
          <el-input-number v-model="templateForm.gpuCoreClockMhz" :disabled="templateSubmitting" :min="0" :precision="0" class="duration-input" />
        </el-form-item>
        <el-form-item label="GPU 电压（mV）">
          <el-input-number v-model="templateForm.gpuVoltageMv" :disabled="templateSubmitting" :min="0" :precision="0" class="duration-input" />
        </el-form-item>
        <el-form-item label="GPU 显存频率（MHz）">
          <el-input-number v-model="templateForm.gpuMemoryClockMhz" :disabled="templateSubmitting" :min="0" :precision="0" class="duration-input" />
        </el-form-item>
        <el-form-item label="GPU 功耗限制（%，-100~100）">
          <el-input-number v-model="templateForm.gpuPowerLimitPercent" :disabled="templateSubmitting" :min="-100" :max="100" :precision="0" class="duration-input" />
        </el-form-item>
        <el-form-item label="驱动版本">
          <el-input v-model="templateForm.driverVersion" :disabled="templateSubmitting" placeholder="例如 566.36" maxlength="60" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="templateForm.customDescription" :disabled="templateSubmitting" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="templateSubmitting" @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="templateSubmitting" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordDialogVisible" :title="recordDialogTitle" width="600px" :close-on-click-modal="!recordSubmitting">
      <el-alert
        v-if="isEditingRecord"
        type="warning"
        :closable="false"
        show-icon
        title="正在修改历史测试数据"
        description="保存后记录的更新时间会刷新，用于修正录入错误。"
        style="margin-bottom: 16px"
      />
      <el-form label-position="top">
        <el-form-item label="测试场景" required>
          <el-select v-model="recordForm.sceneId" :disabled="recordSubmitting" placeholder="选择所属场景" style="width: 100%">
            <el-option v-for="s in recordScenes" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置模板">
          <el-select v-model="recordForm.templateId" :disabled="recordSubmitting" placeholder="可选，记录模板来源" style="width: 100%" clearable>
            <el-option v-for="t in recordTemplates" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="测试时间">
          <el-date-picker
            v-model="recordForm.recordedAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            :disabled="recordSubmitting"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="平均帧率 FPS（必填 > 0）" required>
          <el-input-number v-model="recordForm.avgFps" :disabled="recordSubmitting" :min="0.01" :precision="2" :step="0.1" class="duration-input" />
        </el-form-item>
        <el-form-item label="最低帧率 FPS（必填 > 0）" required>
          <el-input-number v-model="recordForm.minFps" :disabled="recordSubmitting" :min="0.01" :precision="2" :step="0.1" class="duration-input" />
        </el-form-item>
        <el-form-item label="平均帧时间 ms（必填 > 0）" required>
          <el-input-number v-model="recordForm.frameTimeMs" :disabled="recordSubmitting" :min="0.01" :precision="2" :step="0.1" class="duration-input" />
        </el-form-item>
        <el-form-item label="GPU 温度 ℃（≥ -273.15）">
          <el-input-number v-model="recordForm.gpuTempCelsius" :disabled="recordSubmitting" :min="-273.15" :precision="1" :step="1" class="duration-input" />
        </el-form-item>
        <el-form-item label="CPU 温度 ℃（≥ -273.15）">
          <el-input-number v-model="recordForm.cpuTempCelsius" :disabled="recordSubmitting" :min="-273.15" :precision="1" :step="1" class="duration-input" />
        </el-form-item>
        <el-form-item label="GPU 功耗 W（≥ 0）">
          <el-input-number v-model="recordForm.gpuPowerWatt" :disabled="recordSubmitting" :min="0" :precision="1" :step="1" class="duration-input" />
        </el-form-item>
        <el-form-item label="CPU 占用 %（0~100）">
          <el-input-number v-model="recordForm.cpuUsagePercent" :disabled="recordSubmitting" :min="0" :max="100" :precision="1" :step="1" class="duration-input" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recordForm.notes" :disabled="recordSubmitting" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="recordSubmitting" @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="recordSubmitting" @click="saveRecord">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>
