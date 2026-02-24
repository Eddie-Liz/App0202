# Tag&Go Android 架構審合報告

> 生成日期：2026-02-24
> 審合對象：`docs/android-mvvm-example.md` + `docs/api.md`
> 專案版本：`main` 分支（commit `bddf332`）

---

## 總覽

| 面向 | 符合度 | 備註 |
|------|--------|------|
| MVVM 層次分離 | ✅ 符合 | View / ViewModel / Repository 職責明確 |
| API 端點實作 | ⚠️ 部分偏差 | 6/7 完整符合，API #6 主路徑有差異 |
| Room 資料庫 | ⚠️ 部分偏差 | Schema 符合，但版本與 migration 策略需注意 |
| 狀態管理 | ⚠️ 可接受偏差 | 使用 Compose State 取代 LiveData |
| 依賴注入 | ⚠️ 偏差 | Service Locator 取代架構文件建議的 Hilt |
| TypeConverter | ✅ 已修復 | `toIntList` nullable 問題已修正 |

---

## 1. MVVM 層次審合

### 1.1 層次分離 ✅ 符合

架構文件要求：

```
View → ViewModel → Repository → [Retrofit | Room]
```

實際結構：

```
ui/{screen}/
├── {Screen}Screen.kt      → View（Compose，不含業務邏輯）
└── {Screen}ViewModel.kt   → ViewModel（持有 UiState，呼叫 Repository）

data/
├── repository/RootiCareRepository.kt  → 統一資料存取入口
├── api/RootiCareApi.kt               → Retrofit 介面
└── local/                             → Room 資料庫
```

**符合點：**
- View（Compose Screen）只負責顯示 UI 和觸發 ViewModel 函數，無業務邏輯
- ViewModel 持有 `UiState` data class，不直接操作 UI 元件
- Repository 不持有任何 UI 引用，透過 `Result<T>` 回傳結果
- 所有 API 呼叫和 DB 操作皆集中在 Repository

### 1.2 狀態管理 ⚠️ 可接受偏差

**架構文件建議：** LiveData / StateFlow

**實際做法：** Compose `mutableStateOf`

```kotlin
// 架構文件示範
val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

// 實際實作
var uiState by mutableStateOf(LoginUiState())
    private set
```

**評估：** 這是 Jetpack Compose 的慣用模式（`State<T>` 取代 `LiveData`），技術上正確，但與架構文件範例不一致。若未來需要在非 Compose 環境（如 Fragment）共用 ViewModel，需改用 `StateFlow`。

### 1.3 ViewModel 取得 Repository 方式 ⚠️ 偏差

**架構文件建議：** 透過 constructor injection（Hilt 或 ViewModelFactory）

**實際做法：** 在 ViewModel 內直接存取 `ServiceLocator`

```kotlin
class LoginViewModel : ViewModel() {
    private val repository = ServiceLocator.repository  // 直接存取 singleton
```

**影響：** ViewModel 與 `ServiceLocator` 緊耦合，單元測試時無法替換 mock Repository。

---

## 2. 依賴注入審合

### 架構文件建議 vs 實際做法

| 項目 | 文件建議 | 實際實作 |
|------|---------|---------|
| DI 框架 | 隱含 Hilt（範例不含 DI 設定） | Service Locator（`AppModule.kt`） |
| ViewModel 注入 | `@HiltViewModel` / Factory | `ServiceLocator.repository` 直取 |
| 測試替換性 | 高（可 mock） | 低（需修改 ServiceLocator） |

**`di/AppModule.kt` 實作：**

```kotlin
object ServiceLocator {
    // Retrofit、Room、Repository 皆在 init() 中組裝
    // Application 啟動時呼叫 ServiceLocator.init(context)
}
```

**評估：** Service Locator 是可行的輕量方案，但若未來需要加入單元測試，建議引入 Hilt 或至少使用 `ViewModelProvider.Factory` 以支援 constructor injection。

---

## 3. API 端點審合

### 3.1 各 API 實作狀態

| API | 說明 | 路徑符合 | 方法符合 | 備註 |
|-----|------|---------|---------|------|
| #1 Get Token | OAuth Token | ✅ | ✅ POST | `AuthApi.kt` 完整符合 |
| #2 Auth Patient | 訂閱病患 | ✅ | ✅ GET | `subscribedBefore` 處理見下方 |
| #3 Current Measurement | 量測資訊 | ✅ | ✅ GET | 額外支援 Unix 秒/毫秒轉換 |
| #4 Total History Count | 歷史總筆數 | ✅ | ✅ GET | 完整符合 |
| #5 Event Tag History | 分頁歷史 | ✅ | ✅ GET | 分頁迴圈符合文件邏輯 |
| #6 Unsubscribe Patient | 登出 | ⚠️ | ✅ POST | **主路徑與文件不符** |
| #7 Add Virtual Event Tags | 上傳標註 | ✅ | ✅ POST | 完整符合 |

### 3.2 API #6 Unsubscribe — 路徑偏差 ⚠️

**文件規定路徑：**
```
POST /oauth/vendors/{institutionId}/patients/{patientId}/unsubscribe
```

**實際主路徑（Primary）：**
```kotlin
@POST("/oauth/vendors/{institutionId}/patients/{patientId}")
suspend fun unsubscribePatient(...)
```

**文件路徑被設為 Fallback：**
```kotlin
@POST("/oauth/vendors/{institutionId}/patients/{patientId}/unsubscribe")
suspend fun unsubscribePatientWithSuffix(...)
```

**評估：** 程式碼註解說明「已確認正確方法是 POST base path」，推測是後端實際行為與文件有落差，開發時透過實機測試調整。建議更新 `api.md` 以反映實際有效端點，或在程式碼中補充說明原因。

### 3.3 API #2 `subscribedBefore` 型別偏差 ⚠️

**文件規定：** 回傳 `String`（`"true"` / `"false"`）

**實際防禦處理：**
```kotlin
val isSubscribed = when (val sb = body?.subscribedBefore) {
    is Boolean -> sb
    is String -> sb.equals("true", ignoreCase = true)
    else -> false
}
```

**評估：** 防禦性寫法合理（API 實際行為可能與文件不一致），但 `AuthPatientResponse` 的型別宣告應明確為 `Any?` 或改用 `@JsonAdapter` 處理型別差異。

### 3.4 MeasurementInfo 欄位型別偏差 ⚠️

**文件規定（非 nullable）：**
```kotlin
data class MeasurementInfo(
    val measureRecordOid: Long,
    val state: Int,
    val expectedEndTime: Long,
    ...
)
```

**實際實作（全為 nullable）：**
```kotlin
data class MeasurementInfo(
    val measureRecordOid: Long? = null,
    val state: Int? = null,
    val expectedEndTime: Long? = null,
    ...
)
```

**評估：** Nullable 設計較為防禦性，可避免 API 回傳缺少欄位時 crash，但需確保所有使用端皆有 null 檢查。

### 3.5 `isMeasuring()` 邏輯擴充 ✅ 正向偏差

實作加入了文件未提及的 Unix 時間單位自動偵測：

```kotlin
// 文件版本
return state == STATE_MEASURING && expectedEndTime != 0L && now < expectedEndTime

// 實際版本（額外處理秒 vs 毫秒）
val endTimeMillis = if (et > 0 && et < 100000000000L) et * 1000 else et
return s == STATE_MEASURING && et != 0L && now < endTimeMillis
```

**評估：** 合理的防禦性擴充，處理後端時間戳單位不一致的實際問題。

---

## 4. Room 資料庫審合

### 4.1 Entity Schema ✅ 符合

| 欄位 | 文件 | 實作 | 符合 |
|------|------|------|------|
| `id` (PK) | String | String | ✅ |
| `tag_time` | Long | Long | ✅ |
| `tag_local_time` | String | String | ✅ |
| `measure_mode` | Int | Int | ✅ |
| `measure_record_id` | String | String | ✅ |
| `event_type` | List\<Int\> | List\<Int\> | ✅ |
| `others` | String? | String? | ✅ |
| `exercise_intensity` | Int | Int | ✅ |
| `is_read` | Boolean | Boolean | ✅ |
| `is_edit` | Boolean | Boolean | ✅ |

### 4.2 DAO 方法

**文件規定方法：**
- `insertAll()` ✅
- `getAllByMeasureMode()` ✅
- `getTotalCount()` ✅
- `clearAll()` ✅

**額外實作（非文件）：**
- `getAll()` — 取得全部紀錄（用於主頁顯示）
- `getUnsyncedCount()` — 取得未同步筆數（`is_edit = true`）

**評估：** 額外方法均為業務需要，無問題。

### 4.3 資料庫版本 ⚠️

**文件規定：** `version = 1`

**實際：** `version = 2, exportSchema = false`

且使用 `fallbackToDestructiveMigration()`：

```kotlin
Room.databaseBuilder(...)
    .fallbackToDestructiveMigration()  // 升版時清除所有資料
    .build()
```

**風險：** `fallbackToDestructiveMigration()` 在 Schema 版本不符時會直接清空資料庫，生產環境中會導致使用者資料遺失。建議改為提供明確的 `Migration` 策略。

**建議：**
```kotlin
// 替換為
.addMigrations(MIGRATION_1_2)
```

### 4.4 TypeConverter ✅ 已修復

文件範例（舊版，有 bug）：
```kotlin
fun toIntList(value: String?): List<Int>? =  // nullable 回傳 → crash
    value?.split(",")?.mapNotNull { it.toIntOrNull() }
```

目前實作（已修正）：
```kotlin
fun toIntList(value: String?): List<Int> =   // 非 nullable ✅
    value?.takeIf { it.isNotEmpty() }?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
```

**注意：** 文件中的 Room Example 仍使用舊版 nullable 寫法，應同步更新。

---

## 5. 文件未涵蓋的實作功能

以下功能在架構文件中未提及，但已在專案中實作：

| 功能 | 位置 | 說明 |
|------|------|------|
| 強制登入 | `LoginViewModel.forceLogin()` | 先解除其他裝置訂閱，再重新登入 |
| 錄製狀態輪詢 | `MainViewModel.checkRecordingStatus()` | 即時確認後端量測狀態 |
| 自動同步 | `MainViewModel.triggerAutoSync()` | 有未同步 tag 時自動觸發上傳 |
| 即時上傳 | `MainViewModel.confirmTag()` | 儲存 tag 後立即嘗試上傳 |
| Push token 管理 | `TokenManager.pushToken` | 登出時附帶 device token |
| 登出備援路徑 | `RootiCareRepository.unsubscribePatient()` | Primary 失敗後嘗試 /unsubscribe |

---

## 6. 待修正項目彙整

### P1 - 高優先（影響穩定性或資料安全）

| # | 問題 | 位置 | 修正方向 |
|---|------|------|---------|
| 1 | `fallbackToDestructiveMigration()` 於正式環境有資料遺失風險 | `AppModule.kt:89` | 提供 `Migration(1, 2)` 明確遷移腳本 |
| 2 | `exportSchema = false` 無法追蹤 schema 歷史 | `AppDatabase.kt:7` | 改為 `true`，將 schema JSON 加入版控 |

### P2 - 中優先（架構一致性）

| # | 問題 | 位置 | 修正方向 |
|---|------|------|---------|
| 3 | API #6 文件路徑與實作主路徑不符 | `RootiCareApi.kt:44` | 更新 `api.md` 反映實際有效端點 |
| 4 | ViewModel 直接存取 `ServiceLocator` | 所有 ViewModel | 引入 `ViewModelProvider.Factory` 或 Hilt |
| 5 | 文件 Room 範例仍有 `toIntList` nullable bug | `api.md:694` | 更新文件為已修復版本 |

### P3 - 低優先（品質改善）

| # | 問題 | 位置 | 修正方向 |
|---|------|------|---------|
| 6 | `MeasurementInfo` 全欄位 nullable 需各處 null 檢查 | `MeasurementInfo.kt` | 視 API 穩定性決定是否改為非 nullable |
| 7 | 狀態管理 `mutableStateOf` vs `StateFlow` | 所有 ViewModel | 若需跨框架共用考慮改用 `StateFlow` |

---

## 7. 結論

專案整體架構**符合** MVVM 核心精神：View / ViewModel / Repository 三層職責清晰，API 實作覆蓋率達 **6/7（85.7%）**，Room Schema 完整對齊文件規格。

主要偏差集中於：
1. **DI 模式**（Service Locator 取代 Hilt）- 可接受，但影響可測試性
2. **API #6 登出路徑**（文件與實際後端行為不符）- 文件需更新
3. **資料庫版本策略**（破壞性遷移）- 正式上線前需修正

文件本身的 Room 範例（`toIntList`）存在已知 bug（已在程式碼中修正），建議同步更新文件。
