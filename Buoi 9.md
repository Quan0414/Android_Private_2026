# Buổi 9: Kotlin Flow, StateFlow và Quản Lý UI State

---

## 1. Vì sao cần Flow?

Ở các buổi trước, ta đã dùng `LiveData` để đưa dữ liệu từ `ViewModel` xuống `Activity` / `Fragment`. Cách này phù hợp với nhiều màn hình đơn giản, nhưng khi app bắt đầu xử lý dữ liệu bất đồng bộ liên tục thì `LiveData` không còn đủ linh hoạt.

Ví dụ các tình huống cần nhận **nhiều giá trị theo thời gian**:

- Đếm ngược từng giây.
- Theo dõi dữ liệu từ Room Database.
- Nhận dữ liệu real-time từ server.
- Search khi người dùng gõ từng ký tự.
- Theo dõi trạng thái GPS, sensor, kết nối mạng.

`suspend fun` chỉ trả về **một kết quả** sau khi chạy xong:

```kotlin
suspend fun getUser(): User {
    return api.getUser()
}
```

Nếu cần phát ra nhiều kết quả liên tục, ta dùng `Flow`:

```kotlin
fun countDown(): Flow<Int> = flow {
    emit(3)
    delay(1000)
    emit(2)
    delay(1000)
    emit(1)
}
```

---

## 2. Flow là gì?

**Flow** là một luồng dữ liệu bất đồng bộ trong Kotlin Coroutines. Flow có thể `emit` nhiều giá trị theo thời gian, còn bên nhận dùng `collect` để lắng nghe từng giá trị đó.

Flow hoạt động theo mô hình:

```
Producer                         Consumer
flow { emit(value) }   ---->     collect { value -> ... }
```

| Thành phần | Vai trò |
|---|---|
| `flow {}` | Tạo một Flow |
| `emit(value)` | Phát ra một giá trị |
| `collect {}` | Nhận từng giá trị được phát ra |
| `delay()` | Tạm dừng coroutine, không block thread |

Ví dụ cơ bản:

```kotlin
fun numberFlow(): Flow<Int> = flow {
    emit(1)
    emit(2)
    emit(3)
}

viewModelScope.launch {
    numberFlow().collect { number ->
        Log.d("FlowDemo", "Nhận được: $number")
    }
}
```

Kết quả:

```text
Nhận được: 1
Nhận được: 2
Nhận được: 3
```

> [!IMPORTANT]
> Flow thường là **cold stream**. Nghĩa là Flow chưa chạy khi được tạo ra. Nó chỉ bắt đầu chạy khi có người gọi `.collect()`.

---

## 3. Cold Flow và Hot Flow

### 3.1 Cold Flow

Cold Flow chỉ chạy khi có collector. Mỗi lần collect là một lần chạy lại từ đầu.

```kotlin
fun demoFlow(): Flow<Int> = flow {
    Log.d("FlowDemo", "Flow bắt đầu chạy")
    emit(1)
    emit(2)
}

viewModelScope.launch {
    demoFlow().collect { value ->
        Log.d("FlowDemo", "Collector A: $value")
    }
}

viewModelScope.launch {
    demoFlow().collect { value ->
        Log.d("FlowDemo", "Collector B: $value")
    }
}
```

Trong ví dụ trên, `demoFlow()` sẽ chạy **2 lần**, vì có 2 collector khác nhau.

### 3.2 Hot Flow

Hot Flow vẫn tồn tại và giữ dữ liệu ngay cả khi chưa có collector. Các loại Hot Flow thường gặp:

| Loại | Dùng để làm gì |
|---|---|
| `StateFlow` | Giữ trạng thái hiện tại của UI |
| `SharedFlow` | Phát sự kiện một lần như Toast, Snackbar, Navigation |

---

## 4. Các operator thường dùng trong Flow

Flow mạnh hơn `LiveData` vì có nhiều operator để biến đổi, lọc và kết hợp dữ liệu.

### 4.1 `map` - biến đổi dữ liệu

```kotlin
val nameFlow: Flow<String> = userFlow.map { user ->
    user.name
}
```

`map` dùng khi muốn đổi dữ liệu từ dạng này sang dạng khác.

### 4.2 `filter` - lọc dữ liệu

```kotlin
val adultUsers = userFlow.filter { user ->
    user.age >= 18
}
```

`filter` chỉ cho những giá trị thỏa điều kiện đi tiếp.

### 4.3 `onStart` - chạy trước khi Flow emit dữ liệu

```kotlin
repository.getUsers()
    .onStart {
        _uiState.value = UiState.Loading
    }
    .collect { users ->
        _uiState.value = UiState.Success(users)
    }
```

Thường dùng để bật loading trước khi bắt đầu lấy dữ liệu.

### 4.4 `catch` - bắt lỗi trong Flow

```kotlin
repository.getUsers()
    .catch { throwable ->
        _uiState.value = UiState.Error(throwable.message ?: "Lỗi không xác định")
    }
    .collect { users ->
        _uiState.value = UiState.Success(users)
    }
```

`catch` chỉ bắt lỗi xảy ra ở các bước phía trên nó trong chain Flow.

### 4.5 `debounce` - chờ người dùng ngừng gõ

```kotlin
searchQueryFlow
    .debounce(500)
    .filter { keyword -> keyword.isNotBlank() }
    .collect { keyword ->
        searchUser(keyword)
    }
```

`debounce(500)` nghĩa là chờ 500ms. Nếu người dùng tiếp tục gõ, Flow bỏ giá trị cũ và chờ giá trị mới.

### 4.6 `flatMapLatest` - chỉ lấy request mới nhất

```kotlin
searchQueryFlow
    .debounce(500)
    .flatMapLatest { keyword ->
        repository.searchUsers(keyword)
    }
    .collect { users ->
        _uiState.value = UiState.Success(users)
    }
```

Nếu keyword mới xuất hiện trong khi request cũ chưa xong, `flatMapLatest` sẽ hủy request cũ và chỉ giữ request mới nhất.

---

## 5. StateFlow

### 5.1 StateFlow là gì?

`StateFlow` là một loại Flow đặc biệt dùng để giữ **một trạng thái hiện tại**. Nó rất phù hợp để thay thế `LiveData` trong mô hình MVVM hiện đại.

Đặc điểm của `StateFlow`:

- Là **hot stream**.
- Luôn có **giá trị ban đầu**.
- Luôn giữ **giá trị mới nhất**.
- Collector mới sẽ nhận ngay giá trị hiện tại.
- Chỉ emit lại khi giá trị mới **khác** giá trị cũ.

### 5.2 MutableStateFlow và StateFlow

Giống quy tắc `MutableLiveData` / `LiveData`, trong `ViewModel` ta nên tách:

| Biến | Phạm vi | Vai trò |
|---|---|---|
| `MutableStateFlow` | `private` | ViewModel được quyền ghi |
| `StateFlow` | `public` | View chỉ được quyền đọc |

```kotlin
class LoginViewModel(
    private val repository: DemoRepository
) : ViewModel() {

    private val _loginResult = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val loginResult: StateFlow<UiState<Boolean>> = _loginResult.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = UiState.Loading

            val isSuccess = repository.login(username, password)

            _loginResult.value = if (isSuccess) {
                UiState.Success(true)
            } else {
                UiState.Error("Invalid credentials")
            }
        }
    }
}
```

Trong ví dụ trên:

- `_loginResult` là biến private để ViewModel tự cập nhật state.
- `loginResult` là biến public để View quan sát.
- UI chỉ nhận state, không được tự sửa state.

---

## 6. UI State

### 6.1 Vì sao cần UI State?

Không nên quản lý màn hình bằng nhiều biến rời rạc như:

```kotlin
val isLoading = MutableStateFlow(false)
val errorMessage = MutableStateFlow("")
val user = MutableStateFlow<User?>(null)
```

Cách này dễ tạo ra trạng thái mâu thuẫn. Ví dụ: `isLoading = true` nhưng `user` vẫn có dữ liệu cũ, hoặc vừa hiển thị lỗi vừa hiển thị success.

Thay vào đó, nên gom toàn bộ trạng thái màn hình vào một kiểu duy nhất:

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

Đây cũng là format đang có trong project:

```text
MVVM/app/src/main/java/com/example/myapplication/core/ui/UiState.kt
```

### 6.2 Ý nghĩa từng state

| State | Ý nghĩa | UI thường làm |
|---|---|---|
| `Idle` | Chưa có hành động nào | Hiển thị trạng thái ban đầu |
| `Loading` | Đang xử lý | Bật progress bar, disable button |
| `Success(data)` | Thành công, có dữ liệu | Hiển thị dữ liệu |
| `Error(message)` | Thất bại, có thông báo lỗi | Hiển thị Toast, Snackbar hoặc Text lỗi |

### 6.3 Luồng state chuẩn

```
User click button
      |
      v
View gọi viewModel.login(...)
      |
      v
ViewModel set UiState.Loading
      |
      v
Repository xử lý login
      |
      v
Success --------------> UiState.Success(data)
Error ----------------> UiState.Error(message)
      |
      v
View collect state và render UI
```

---

## 7. Collect StateFlow trong Activity / Fragment

Khác với `LiveData`, `StateFlow` không tự lifecycle-aware. Vì vậy trong Android View, ta cần collect bên trong `repeatOnLifecycle`.

### 7.1 Activity

```kotlin
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginResult.collect { state ->
                    renderLoginState(state)
                }
            }
        }
    }

    private fun renderLoginState(state: UiState<Boolean>) {
        when (state) {
            is UiState.Idle -> {
                binding.tvResult.text = "Idle"
                binding.btnLogin.isEnabled = true
            }

            is UiState.Loading -> {
                binding.tvResult.text = "Loading..."
                binding.btnLogin.isEnabled = false
            }

            is UiState.Success -> {
                binding.tvResult.text = "Login successful!"
                binding.btnLogin.isEnabled = true
            }

            is UiState.Error -> {
                binding.tvResult.text = "Login failed: ${state.message}"
                binding.btnLogin.isEnabled = true
            }
        }
    }
}
```

### 7.2 Fragment

Với Fragment, nên dùng `viewLifecycleOwner.lifecycleScope`, không dùng trực tiếp `lifecycleScope` của Fragment.

```kotlin
class LoginFragment : Fragment() {

    private val viewModel by viewModels<LoginViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginResult.collect { state ->
                    renderLoginState(state)
                }
            }
        }
    }
}
```

> [!IMPORTANT]
> Trong Fragment, View có thể bị hủy tại `onDestroyView()` nhưng Fragment object vẫn còn. Vì vậy collect UI state phải bám theo `viewLifecycleOwner`.

---

## 8. SharedFlow

### 8.1 SharedFlow dùng để làm gì?

`StateFlow` dùng cho **state lâu dài** của màn hình. Nhưng có những thứ chỉ nên chạy **một lần**, ví dụ:

- Toast.
- Snackbar.
- Điều hướng sang màn hình khác.
- Mở dialog.
- Gửi event báo login thành công.

Những thứ này gọi là **one-time event**. Không nên nhét chúng vào `StateFlow`, vì khi xoay màn hình collector mới có thể nhận lại state cũ và làm Toast / Navigation chạy lại.

Với one-time event, dùng `SharedFlow`.

### 8.2 Ví dụ SharedFlow cho event

```kotlin
sealed class LoginEvent {
    data class ShowMessage(val message: String) : LoginEvent()
    object NavigateHome : LoginEvent()
}

class LoginViewModel(
    private val repository: DemoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val uiState: StateFlow<UiState<Boolean>> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<LoginEvent>()
    val event: SharedFlow<LoginEvent> = _event.asSharedFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            if (repository.login(username, password)) {
                _uiState.value = UiState.Success(true)
                _event.emit(LoginEvent.NavigateHome)
            } else {
                _uiState.value = UiState.Error("Invalid credentials")
                _event.emit(LoginEvent.ShowMessage("Sai tài khoản hoặc mật khẩu"))
            }
        }
    }
}
```

Collect event trong Activity:

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.event.collect { event ->
            when (event) {
                is LoginEvent.ShowMessage -> {
                    Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
                }

                is LoginEvent.NavigateHome -> {
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                }
            }
        }
    }
}
```

---

## 9. So sánh LiveData, Flow, StateFlow và SharedFlow

| Tiêu chí | `LiveData` | `Flow` | `StateFlow` | `SharedFlow` |
|---|---|---|---|---|
| Loại stream | Hot | Cold | Hot | Hot |
| Giá trị ban đầu | Không bắt buộc | Không có | Bắt buộc | Không bắt buộc |
| Giữ giá trị mới nhất | Có | Không | Có | Tùy cấu hình `replay` |
| Lifecycle-aware | Có sẵn | Không | Không | Không |
| Dùng với coroutine | Trung bình | Rất tốt | Rất tốt | Rất tốt |
| Use case chính | UI state đơn giản | Stream dữ liệu | UI state hiện tại | One-time event |
| Ví dụ | Observe login result | Room query, search stream | Loading/Success/Error | Toast, Navigation |

### Khi nào dùng cái nào?

| Nhu cầu | Nên dùng |
|---|---|
| Project cũ, đang dùng LiveData ổn định | `LiveData` |
| Repository trả nhiều giá trị theo thời gian | `Flow` |
| ViewModel giữ trạng thái màn hình | `StateFlow` |
| Gửi event chỉ chạy một lần | `SharedFlow` |

---

## 10. Best Practice

### 10.1 ViewModel chỉ expose read-only Flow

```kotlin
private val _uiState = MutableStateFlow<UiState<User>>(UiState.Idle)
val uiState: StateFlow<UiState<User>> = _uiState.asStateFlow()
```

Không expose trực tiếp `MutableStateFlow` ra View:

```kotlin
// Không nên
val uiState = MutableStateFlow<UiState<User>>(UiState.Idle)
```

Nếu expose mutable state, Activity / Fragment có thể tự ý sửa state và phá vỡ luồng dữ liệu một chiều.

### 10.2 View chỉ render state, không xử lý business logic

```kotlin
binding.btnLogin.setOnClickListener {
    viewModel.login(
        username = binding.edtUsername.text.toString(),
        password = binding.edtPassword.text.toString()
    )
}
```

View chỉ lấy input và gọi ViewModel. Việc kiểm tra login, gọi API, đọc database phải nằm ở ViewModel / Repository.

### 10.3 Luôn collect Flow theo lifecycle

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            render(state)
        }
    }
}
```

Không nên collect trực tiếp mà không gắn lifecycle:

```kotlin
// Không nên trong Activity / Fragment
lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        render(state)
    }
}
```

Vì collector có thể tiếp tục chạy khi màn hình đã vào background.

### 10.4 State và Event nên tách riêng

```kotlin
val uiState: StateFlow<UiState<User>>
val event: SharedFlow<UserEvent>
```

- `uiState`: dữ liệu để vẽ màn hình.
- `event`: hành động chỉ chạy một lần.

Không nên dùng `StateFlow` để bắn Toast / Navigation.

---

## 11. Ví dụ hoàn chỉnh theo MVVM

### 11.1 Repository

```kotlin
class DemoRepository {
    suspend fun login(username: String, password: String): Boolean {
        delay(1000)
        return username == "admin" && password == "123456"
    }
}
```

### 11.2 UiState

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

### 11.3 ViewModel

```kotlin
class MainViewModel(
    private val repo: DemoRepository
) : ViewModel() {

    private val _loginResult = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val loginResult: StateFlow<UiState<Boolean>> = _loginResult.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = UiState.Loading

            try {
                val success = repo.login(username, password)

                _loginResult.value = if (success) {
                    UiState.Success(true)
                } else {
                    UiState.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _loginResult.value = UiState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }
}
```

### 11.4 Activity

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(DemoRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                username = binding.edtUsername.text.toString(),
                password = binding.edtPassword.text.toString()
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginResult.collect { state ->
                    renderLoginState(state)
                }
            }
        }
    }

    private fun renderLoginState(state: UiState<Boolean>) {
        when (state) {
            is UiState.Idle -> {
                binding.tvResult.text = "Idle"
                binding.btnLogin.isEnabled = true
            }

            is UiState.Loading -> {
                binding.tvResult.text = "Loading..."
                binding.btnLogin.isEnabled = false
            }

            is UiState.Success -> {
                binding.tvResult.text = "Login successful!"
                binding.btnLogin.isEnabled = true
            }

            is UiState.Error -> {
                binding.tvResult.text = "Login failed: ${state.message}"
                binding.btnLogin.isEnabled = true
            }
        }
    }
}
```

---

## 12. Tóm tắt

| Khái niệm | Ghi nhớ |
|---|---|
| `Flow` | Stream bất đồng bộ, phát nhiều giá trị theo thời gian |
| `collect` | Lắng nghe dữ liệu từ Flow |
| `emit` | Phát dữ liệu từ Flow |
| Cold Flow | Chỉ chạy khi có collector |
| Hot Flow | Tồn tại độc lập với collector |
| `StateFlow` | Giữ state hiện tại của màn hình |
| `SharedFlow` | Gửi event một lần |
| `UiState` | Đóng gói trạng thái UI: Idle, Loading, Success, Error |
| `repeatOnLifecycle` | Collect Flow an toàn theo lifecycle Android |

Luồng chuẩn trong MVVM khi dùng StateFlow:

```
User Action
   ↓
View gọi hàm trong ViewModel
   ↓
ViewModel cập nhật MutableStateFlow
   ↓
StateFlow emit state mới
   ↓
View collect state
   ↓
View render UI
```

Quy tắc quan trọng nhất:

> ViewModel quản lý state. View chỉ quan sát state và vẽ UI.
