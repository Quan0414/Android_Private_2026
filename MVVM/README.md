# 📁 MVVM Android Project — Cấu Trúc Thư Mục

Dự án Android được tổ chức theo mô hình kiến trúc **MVVM (Model - View - ViewModel)**, kết hợp với cách phân chia theo **Feature-based** giúp dễ mở rộng và bảo trì. (tất cả chỉ là tham khảo, không nhất thiết phải như này)

---

## 🗂️ Tổng Quan Cấu Trúc

```
MVVM/
├── app/
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/myapplication/
│       │   │   ├── MainActivity.kt
│       │   │   │
│       │   │   ├── core/                          # Thành phần dùng chung toàn app
│       │   │   │   ├── database/                  # Room Database, DAO
│       │   │   │   ├── di/                        # Dependency Injection
│       │   │   │   ├── network/                   # Retrofit, API Client
│       │   │   │   └── utils/                     # Tiện ích, Extension functions
│       │   │   │
│       │   │   └── features/                      # Phân chia theo feature
│       │   │       ├── auth/                      # feature auth
│       │   │       │   ├── data/
│       │   │       │   │   ├── api/
│       │   │       │   │   │   └── AuthService.kt
│       │   │       │   │   ├── model/
│       │   │       │   │   │   ├── LoginRequest.kt
│       │   │       │   │   │   ├── LoginResponse.kt
│       │   │       │   │   │   └── UserDto.kt
│       │   │       │   │   └── repository/
│       │   │       │   │       └── AuthRepository.kt
│       │   │       │   └── ui/
│       │   │       │       ├── login/             # có thể chia theo feature nhỏ hơn như này
│       │   │       │       └── signup/
│       │   │       │
│       │   │       └── home/                      # feature home (hoặc feature gì đó)
│       │   │           ├── data/
│       │   │           │   ├── api/
│       │   │           │   ├── model/
│       │   │           │   └── repository/
│       │   │           │       └── DemoRepository.kt
│       │   │           └── ui/
│       │   │               ├── adapter/
│       │   │               ├── view/              # hoặc có thể chia theo view/viewmodel
│       │   │               └── viewmodel/
│       │   │                   ├── MainViewModel.kt
│       │   │                   └── MainVMFactory.kt
│       │   │
│       │   └── res/                               
│       │       ├── drawable/
│       │       ├── layout/
│       │       │   └── activity_main.xml
│       │       ├── mipmap-*/                      
│       │       ├── values/
│       │       │   ├── colors.xml
│       │       │   ├── strings.xml
│       │       │   └── themes.xml
│       │       └── xml/
│       │
│       ├── androidTest/                           
│       └── test/                                  
│
├── build.gradle.kts                               
├── settings.gradle.kts
├── gradle.properties
└── gradlew / gradlew.bat
```

---

## 🏛️ Các Tầng Kiến Trúc MVVM

### 1.  Model (Tầng Dữ Liệu — `data/`)
> Chịu trách nhiệm **cung cấp và quản lý dữ liệu** cho ứng dụng.

| Thư mục | Mô tả |
|---|---|
| `data/api/` | Interface Retrofit — định nghĩa các endpoint API |
| `data/model/` | Data class: Request/Response/DTO — ánh xạ dữ liệu JSON |
| `data/repository/` | Repository — trung gian giữa data source và ViewModel |

**Ví dụ:**
```kotlin
// data/model/LoginRequest.kt
data class LoginRequest(
    val email: String,
    val password: String
)

// data/repository/AuthRepository.kt
class AuthRepository(private val authService: AuthService) {
    suspend fun login(request: LoginRequest) = authService.login(request)
}
```

---

### 2. View (Tầng Giao Diện — `ui/view/` & `res/layout/`)
> Chịu trách nhiệm **hiển thị dữ liệu** và **nhận tương tác người dùng**.

| Thư mục | Mô tả |
|---|---|
| `ui/view/` | Activity / Fragment — lắng nghe LiveData từ ViewModel |
| `ui/adapter/` | RecyclerView Adapter — hiển thị danh sách |
| `res/layout/` | File XML định nghĩa giao diện |

**Nguyên tắc:**
- View **không chứa logic nghiệp vụ**.
- View chỉ **quan sát (observe)** LiveData và cập nhật UI.

```kotlin
// Trong Activity/Fragment — observe LiveData
viewModel.userData.observe(this) { user ->
    binding.tvUsername.text = user.name
}
```

---

### 3. ViewModel (Tầng Trung Gian — `ui/viewmodel/`)
> Cầu nối giữa **View** và **Model**, giữ trạng thái UI sống sót qua vòng đời.

| File | Mô tả |
|---|---|
| `MainViewModel.kt` | Chứa LiveData, xử lý logic UI, gọi Repository |
| `MainVMFactory.kt` | Factory tạo ViewModel với tham số tùy chỉnh |

```kotlin
// ui/viewmodel/MainViewModel.kt
class MainViewModel(private val repository: DemoRepository) : ViewModel() {
    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data

    fun loadData() {
        viewModelScope.launch {
            _data.value = repository.fetchData()
        }
    }
}
```

---

##  Thư Mục `core/` — Dùng Chung Toàn App

| Thư mục | Mô tả |
|---|---|
| `core/network/` | Cấu hình Retrofit, OkHttp interceptor |
| `core/database/` | Room Database, DAO interface |
| `core/di/` | Dependency Injection (Hilt / manual DI) |
| `core/utils/` | Extension functions, Constants, Helper classes |

---

##  Luồng Dữ Liệu (Data Flow)

```
[ User Action ]
      │
      ▼
   [ View ]          observe →      [ LiveData ]
  Activity/Fragment                        │
      │                                    │
      │ gọi method                         │
      ▼                                    │
 [ ViewModel ] ──────────────────────────►─┘
      │
      │ gọi repository
      ▼
 [ Repository ]
      │
      ├──► [ Remote: Retrofit API ]
      │
      └──► [ Local: Room Database ]
```

---

##  Phân Chia Theo Feature (Feature-based)

Mỗi tính năng (`auth`, `home`, ...) đều có cấu trúc **3 tầng độc lập**:

```
features/
└── <ten_tinh_nang>/
    ├── data/
    │   ├── api/           ← API Service Interface
    │   ├── model/         ← Data Classes
    │   └── repository/    ← Repository Implementation
    └── ui/
        ├── view/          ← Activity / Fragment
        ├── adapter/       ← RecyclerView Adapter
        └── viewmodel/     ← ViewModel + VMFactory
```

> ✅ **Lợi ích:** Dễ thêm tính năng mới mà không ảnh hưởng đến các module khác.

---

##  Công Nghệ Sử Dụng

| Thư viện | Mục đích |
|---|---|
| **ViewModel** | Lưu trữ trạng thái UI, tồn tại qua configuration change |
| **LiveData** | Observable data holder, tự động cập nhật UI |
| **Coroutines** | Xử lý bất đồng bộ (async/await) |
| **Retrofit** | Gọi REST API |
| **Room** | Cơ sở dữ liệu local (SQLite wrapper) |
| **Kotlin** | Ngôn ngữ lập trình chính |

---

##  Quy Ước Đặt Tên

| Loại | Quy ước | Ví dụ |
|---|---|---|
| Activity | `<Tên>Activity.kt` | `LoginActivity.kt` |
| Fragment | `<Tên>Fragment.kt` | `HomeFragment.kt` |
| ViewModel | `<Tên>ViewModel.kt` | `MainViewModel.kt` |
| Repository | `<Tên>Repository.kt` | `AuthRepository.kt` |
| Model/DTO | `<Tên>Dto.kt` | `UserDto.kt` |
| API Service | `<Tên>Service.kt` | `AuthService.kt` |
| Adapter | `<Tên>Adapter.kt` | `UserListAdapter.kt` |
| Layout XML | `activity_<tên>.xml` | `activity_login.xml` |
| Layout XML | `fragment_<tên>.xml` | `fragment_home.xml` |
| Layout XML | `item_<tên>.xml` | `item_user.xml` |
