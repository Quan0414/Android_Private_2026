# Buổi 8: Xử lý bất đồng bộ với Kotlin Coroutine

---

## 1. Nền tảng: Process, Thread và Coroutine

### 1.1 Process (Tiến trình)

**Process** là một chương trình đang được thực thi. Khi mở một ứng dụng Android, hệ điều hành tạo ra một **Process riêng biệt** cho app đó, với bộ nhớ hoàn toàn cách ly.

```
Hệ điều hành Android
├── Process: com.google.maps        ← App Google Maps
├── Process: com.facebook.android   ← App Facebook
└── Process: com.yourapp.example    ← App của bạn
     ├── Bộ nhớ riêng (Heap, Stack)
     └── Ít nhất 1 Thread (Main Thread)
```

### 1.2 Thread (Luồng)

**Thread** là đơn vị thực thi bên trong Process. Một Process có thể có **nhiều Thread chạy song song**, dùng chung vùng nhớ Heap.

```
Process: com.yourapp.example
├── Main Thread (UI Thread)    ← vẽ UI & nhận input — BẮT BUỘC phải có
├── Background Thread 1        ← gọi API
└── Background Thread 2        ← xử lý ảnh
```

Khi Thread gặp tác vụ chờ (network, I/O), nó **bị BLOCK hoàn toàn** — đứng yên chờ đến khi xong:

```
Thread-1: [==Gọi API==|BLOCK...CHỜCHỜCHỜ...|==Xử lý kết quả==]
Thread-2: [==Gọi DB== |BLOCK...CHỜCHỜCHỜ...|==Xử lý kết quả==]
```

> ⚠️ Nếu **Main Thread bị block** quá **5 giây**, Android sẽ hiện hộp thoại **ANR (Application Not Responding)** và buộc người dùng đóng app.

Vì vậy, mọi tác vụ nặng (gọi API, truy vấn DB...) **bắt buộc phải chạy trên Background Thread**, sau đó trả kết quả về Main Thread để cập nhật UI.

### 1.3 Coroutine — Giải pháp hiện đại

**Coroutine** là đơn vị thực thi **siêu nhẹ** chạy bên trên Thread. Thay vì BLOCK Thread khi chờ, Coroutine chỉ **SUSPEND** (tạm nhường Thread) để Thread đó xử lý việc khác, rồi tự **RESUME** khi có kết quả.

```
Thread-1: [==Coroutine A gọi API==|suspend|==Coroutine B gọi DB==|suspend|==Resume A==|==Resume B==]
          ↑ 1 Thread xử lý được nhiều tác vụ, không lãng phí khi chờ
```

**Dispatcher** phân phối Coroutine xuống đúng Thread:

| Dispatcher | Thread | Dùng khi nào |
|---|---|---|
| `Dispatchers.Main` | Main Thread | Cập nhật UI, đọc/ghi LiveData |
| `Dispatchers.IO` | Pool IO Thread | Gọi API, truy vấn DB, đọc/ghi file |
| `Dispatchers.Default` | Pool CPU Thread | Tính toán nặng, sắp xếp dữ liệu lớn |

---

## 2. Cài đặt thư viện

Thêm vào `build.gradle.kts` (Module: app):

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")  // viewModelScope
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")    // lifecycleScope
}
```

---

## 3. Ba khái niệm cốt lõi

### 3.1 `suspend` — Hàm có thể tạm dừng

Từ khóa `suspend` đánh dấu một hàm **có thể tạm dừng giữa chừng** mà không block Thread.

- Chỉ gọi được từ **bên trong Coroutine** hoặc từ một `suspend fun` khác.
- Khi gặp tác vụ chờ, hàm **nhường Thread** cho Coroutine khác chạy.
- Tự **tiếp tục** từ điểm bị dừng khi tác vụ hoàn thành.

```kotlin
suspend fun getUserFromNetwork(): User {
    return apiService.getUser()  // apiService.getUser() cũng là suspend fun
}
```

### 3.2 CoroutineScope — Phạm vi sống

**CoroutineScope** kiểm soát vòng đời của Coroutine. Khi Scope bị hủy, **tất cả Coroutine bên trong cũng tự hủy theo** — đây là cơ chế chống Memory Leak.

| Scope | Nơi dùng | Tự hủy khi |
|---|---|---|
| `viewModelScope` | Bên trong `ViewModel` | ViewModel bị destroy |
| `lifecycleScope` | Bên trong `Activity` / `Fragment` | Activity/Fragment bị DESTROYED |

```kotlin
class MyViewModel : ViewModel() {
    fun loadData() {
        viewModelScope.launch {
            // Tự hủy khi ViewModel bị xóa — không cần lo Memory Leak
        }
    }
}
```

### 3.3 Coroutine Builder — Cách khởi động Coroutine

#### `launch` — Chạy và không cần kết quả

Dùng khi muốn chạy một tác vụ mà **không cần lấy kết quả** trả về (lưu DB, gửi log...).

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    repository.saveUser(user)
    Log.d("TAG", "Lưu thành công")
}
```

#### `async` / `await` — Chạy song song và lấy kết quả

Dùng khi cần **chạy song song nhiều tác vụ** rồi ghép kết quả lại.

```kotlin
viewModelScope.launch {
    val deferredUser  = async(Dispatchers.IO) { repository.getUser() }
    val deferredPosts = async(Dispatchers.IO) { repository.getPosts() }

    // Cả hai bắt đầu chạy song song ngay lập tức
    val user  = deferredUser.await()   // chờ kết quả
    val posts = deferredPosts.await()  // chờ kết quả

    _uiState.value = UiState.Success(user, posts)
}
```

#### `withContext` — Chuyển Dispatcher tạm thời

Dùng để **đổi luồng thực thi** trong cùng một Coroutine, sau đó tự quay về Dispatcher ban đầu.

```kotlin
viewModelScope.launch {                         // bắt đầu trên Main Thread
    _uiState.value = UiState.Loading

    val result = withContext(Dispatchers.IO) {   // chuyển sang IO Thread
        repository.fetchData()
    }                                            // tự động quay về Main Thread

    _uiState.value = UiState.Success(result)    // cập nhật UI — an toàn
}
```

---

## 4. Xử lý lỗi

### 4.1 Try-Catch thông thường

```kotlin
viewModelScope.launch {
    try {
        _uiState.value = UiState.Loading
        val data = withContext(Dispatchers.IO) { repository.fetchData() }
        _uiState.value = UiState.Success(data)
    } catch (e: IOException) {
        _uiState.value = UiState.Error("Lỗi mạng: ${e.message}")
    } catch (e: Exception) {
        _uiState.value = UiState.Error("Lỗi không xác định: ${e.message}")
    }
}
```

### 4.2 CoroutineExceptionHandler — Bắt lỗi tập trung

Dùng khi muốn một nơi xử lý lỗi chung cho cả Scope (tương tự global error handler).

```kotlin
val handler = CoroutineExceptionHandler { _, throwable ->
    _uiState.postValue(UiState.Error(throwable.message ?: "Lỗi không xác định"))
}

viewModelScope.launch(Dispatchers.IO + handler) {
    val data = repository.fetchData()
    withContext(Dispatchers.Main) {
        _uiState.value = UiState.Success(data)
    }
}
```

---

## 5. Tích hợp vào MVVM

Luồng dữ liệu trong MVVM với Coroutine:

```
View (Activity) → ViewModel (viewModelScope) → Repository (suspend fun) → API / DB
```

### Repository — suspend fun

```kotlin
class UserRepository {
    suspend fun getUser(id: Int): User {
        return apiService.getUserById(id)  // Retrofit hỗ trợ suspend tự động
    }
}
```

### ViewModel — khởi động Coroutine

```kotlin
class UserViewModel(private val repo: UserRepository) : ViewModel() {

    private val _uiState = MutableLiveData<UserUiState>()
    val uiState: LiveData<UserUiState> get() = _uiState

    fun loadUser(id: Int) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            try {
                val user = withContext(Dispatchers.IO) { repo.getUser(id) }
                _uiState.value = UserUiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error("Không thể tải: ${e.message}")
            }
        }
    }
}
```

### Activity — quan sát kết quả

```kotlin
class UserActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UserUiState.Loading -> progressBar.visibility = View.VISIBLE
                is UserUiState.Success -> {
                    progressBar.visibility = View.GONE
                    tvUserName.text = state.user.name
                }
                is UserUiState.Error -> Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }
        }

        btnLoad.setOnClickListener { viewModel.loadUser(id = 1) }
    }
}
```

---

## 6. Flow — Nhiều giá trị theo thời gian

### 6.1 Tại sao cần Flow?

Ở Buổi 7 ta dùng **LiveData** để truyền dữ liệu từ ViewModel xuống View. LiveData hoạt động tốt, nhưng nó có một giới hạn: mỗi lần chỉ giữ **một giá trị duy nhất** — giá trị hiện tại.

Tương tự, `suspend fun` cũng chỉ trả về **một kết quả** sau khi hoàn thành.

Vậy nếu cần nhận **nhiều giá trị liên tục theo thời gian**?
- Đếm ngược (3... 2... 1...)
- Stream dữ liệu real-time từ server
- Theo dõi trạng thái liên tục (GPS, sensor...)
- Tìm kiếm theo từng ký tự người dùng gõ

→ Đây là lúc **Kotlin Flow** ra đời.

### 6.2 Flow là gì?

**Flow** là một **luồng dữ liệu bất đồng bộ** có thể phát ra (emit) **nhiều giá trị tuần tự theo thời gian**.

Flow hoạt động theo mô hình **Producer - Consumer**:
- **Producer (Nhà phát):** khối `flow { ... }` tạo và emit từng giá trị.
- **Consumer (Người nhận):** `.collect { ... }` nhận và xử lý từng giá trị khi nó đến.

```kotlin
// PRODUCER: Tạo một Flow đếm ngược 3...2...1
fun countDownFlow(): Flow<Int> = flow {
    for (i in 3 downTo 1) {
        emit(i)         // phát ra giá trị
        delay(1000L)    // chờ 1 giây (suspend, không block thread)
    }
}

// CONSUMER: Thu nhận từng giá trị trong ViewModel
viewModelScope.launch {
    countDownFlow().collect { value ->
        _countDown.value = value    // cập nhật UI mỗi giây
    }
}
```

> 💡 **Lưu ý quan trọng:** Flow là **cold stream** — Flow **không chạy** cho đến khi có người `.collect()`. Nếu không có ai collect, Flow không làm gì cả. Mỗi lần collect là một lần chạy mới từ đầu.

---

### 6.3 StateFlow — Thay thế LiveData

**StateFlow** là một loại Flow đặc biệt, được thiết kế để **giữ và phát lại một trạng thái hiện tại** — rất giống LiveData nhưng tích hợp tự nhiên vào hệ sinh thái Coroutine.

StateFlow là **hot stream** — nó luôn chạy và giữ giá trị ngay cả khi không có ai đang collect.

#### Cách dùng StateFlow trong ViewModel

```kotlin
class UserViewModel(private val repo: UserRepository) : ViewModel() {

    // Khai báo MutableStateFlow (private - chỉ ViewModel được ghi)
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)

    // Expose StateFlow read-only ra ngoài (public - View chỉ đọc)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun loadUser(id: Int) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            try {
                val user = withContext(Dispatchers.IO) { repo.getUser(id) }
                _uiState.value = UserUiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error("Lỗi: ${e.message}")
            }
        }
    }
}
```

#### Cách collect StateFlow trong Activity

```kotlin
class UserActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Cách đúng: dùng repeatOnLifecycle để an toàn theo lifecycle
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UserUiState.Loading  -> progressBar.visibility = View.VISIBLE
                        is UserUiState.Success  -> {
                            progressBar.visibility = View.GONE
                            tvName.text = state.user.name
                        }
                        is UserUiState.Error    -> Toast.makeText(this@UserActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        // repeatOnLifecycle(STARTED) → tự pause collect khi app vào background
        //                            → tự resume collect khi app trở lại foreground
    }
}
```

---

### 6.4 So sánh LiveData vs StateFlow

Ở Buổi 7, ta dùng **LiveData** vì nó đơn giản và tích hợp sẵn với vòng đời Android. Sang Buổi 8 với Coroutine, **StateFlow** là lựa chọn hiện đại hơn. Đây là sự khác biệt:

| Tiêu chí | `LiveData` (Buổi 7) | `StateFlow` (Buổi 8) |
|---|---|---|
| **Nguồn gốc** | Android Jetpack | Kotlin Coroutines |
| **Lifecycle-aware** | ✅ Có sẵn, tự động | ⚠️ Cần bọc trong `repeatOnLifecycle` |
| **Giá trị ban đầu** | Không bắt buộc | **Bắt buộc** phải có |
| **Cập nhật UI từ background** | `.postValue()` | `.value =` (từ coroutine bất kỳ) |
| **Tích hợp với Flow** | Hạn chế | Tự nhiên — StateFlow **là** một Flow |
| **Xử lý dữ liệu (map, filter...)** | Hạn chế, cần LiveData operators | Dùng trực tiếp Flow operators |
| **Phát cùng giá trị 2 lần** | ✅ Vẫn notify Observer | ❌ Bỏ qua — StateFlow chỉ emit khi giá trị **thay đổi** |
| **Dùng ngoài Android** | ❌ Chỉ dùng trong Android | ✅ Dùng được cả Kotlin Multiplatform |

#### Khi nào dùng cái nào?

```
Dự án cũ / đơn giản / chỉ cần hiển thị data   →  LiveData (quen thuộc, ít boilerplate)

Dự án mới / dùng Coroutine / cần xử lý stream  →  StateFlow (tích hợp tự nhiên, mạnh hơn)
```

> 🔑 **Quy tắc thực tế:** Nếu đã dùng Coroutine trong ViewModel (như Buổi 8), hãy dùng **StateFlow**. Nếu chưa có Coroutine (như Buổi 7), **LiveData** vẫn là lựa chọn tốt.

---

### 6.5 So sánh LiveData vs Flow vs StateFlow — Toàn cảnh

| | `LiveData` | `Flow` | `StateFlow` |
|---|---|---|---|
| **Số giá trị** | Một (hiện tại) | Nhiều (stream) | Một (hiện tại) |
| **Hot / Cold** | Hot | Cold | Hot |
| **Lifecycle-aware** | ✅ Tự động | ❌ Không | ❌ Cần thêm |
| **Giá trị ban đầu** | Không bắt buộc | Không có | **Bắt buộc** |
| **Thường dùng ở** | ViewModel → View | Repository → ViewModel | ViewModel → View |
| **Thay thế cho** | — | `suspend fun` khi cần nhiều giá trị | LiveData |

---

## 7. Tóm tắt

| Khái niệm | Vai trò |
|---|---|
| `suspend fun` | Hàm có thể tạm dừng, không block Thread |
| `CoroutineScope` | Kiểm soát vòng đời, tự hủy coroutine khi không cần |
| `Dispatcher` | Xác định coroutine chạy trên Thread nào |
| `launch` | Khởi động coroutine, không cần kết quả |
| `async` / `await` | Chạy song song, lấy kết quả |
| `withContext` | Chuyển Dispatcher tạm thời trong coroutine |
| `Flow` | Stream nhiều giá trị bất đồng bộ theo thời gian (cold) |
| `StateFlow` | Giữ một trạng thái hiện tại (hot), thay thế LiveData trong MVVM hiện đại |

