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

const view = ref<"games" | "scenes">("games");
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

const dialogTitle = computed(() => editingId.value === null ? "新增游戏" : "编辑游戏");
const sceneDialogTitle = computed(() => editingSceneId.value === null ? "新增测试场景" : "编辑测试场景");

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

function returnToGames(): void {
  view.value = "games";
  selectedGame.value = null;
  scenes.value = [];
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

function formatTime(value: string): string {
  return new Date(value).toLocaleString();
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
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button link type="primary" :disabled="submitting" @click="openScenes(scope.row)">场景</el-button>
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

    <template v-else-if="selectedGame !== null">
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
  </main>
</template>
