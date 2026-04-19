# Buổi 7: Kiến trúc MVVM và Lưu trữ dữ liệu với ViewModel, LiveData

## 1. Mô hình MVVM (Model-View-ViewModel)

### 1.1 Khái niệm
MVVM là một mẫu kiến trúc phần mềm phổ biến trong phát triển ứng dụng di động, đặc biệt là Android (được Google khuyến nghị trong bộ Android Architecture Components). MVVM giúp tách biệt logic giao diện (UI) và logic nghiệp vụ (Business logic), giúp code dễ bảo trì, dễ kiểm thử và linh hoạt hơn so với các mô hình cũ như MVC hay MVP.

### 1.2 Các thành phần chính

- **Model (Mô hình Dữ liệu):** 
  - Đại diện cho tầng dữ liệu (Data Layer) và logic nghiệp vụ.
  - Chịu trách nhiệm lấy dữ liệu (từ API mạng ở xa, Database cục bộ như Room, hoặc SharedPreferences) và xử lý dữ liệu.
  - Model không biết gì về View hay ViewModel. Nó chỉ cung cấp dữ liệu cho ViewModel khi được yêu cầu.

- **View (Giao diện):**
  - Đại diện cho tầng giao diện người dùng (UI), bao gồm Activity hoặc Fragment trong Android.
  - Chịu trách nhiệm thiết lập UI, hiển thị dữ liệu lên màn hình và nhận tương tác từ người dùng (như click nút, vuốt, nhập chữ).
  - View quan sát (Observe) các sự thay đổi dữ liệu từ ViewModel và tự cập nhật giao diện tương ứng. View không nên chứa các logic nghiệp vụ (tính toán, xử lý dữ liệu).

- **ViewModel (Cầu nối giữa View và Model):**
  - Đóng vai trò là cầu nối nhận và truyền tải giữ View và Model.
  - Nhận yêu cầu từ View, gọi Model để lấy/xử lý dữ liệu, sau đó chuẩn bị dữ liệu dưới dạng có thể quan sát được (như LiveData hoặc StateFlow) để View tự động lấy xuống và cập nhật vào UI.
  - **Quan trọng:** ViewModel không chứa các tham chiếu trực tiếp đến View hay Context để tránh lỗi rò rỉ bộ nhớ (memory leak). Nó tồn tại độc lập với vòng đời của View.

### 1.3 Luồng tương tác giữa các thành phần (Interaction Flow)
Luồng dữ liệu trong MVVM được thiết kế theo hướng **một chiều (Unidirectional Data Flow)**, quy trình diễn ra như sau:

1. **Người dùng Action:** Người dùng tương tác với **View** (ví dụ: gõ văn bản, bấm nút "Đăng nhập").
2. **View -> ViewModel:** **View** không tự xử lý mà chuyển tiếp sự kiện đó cho **ViewModel** thông qua việc gọi một hàm (method) hoặc cập nhật một Data Binding.
3. **ViewModel -> Model:** **ViewModel** nhận yêu cầu, xử lý ban đầu (nếu cần) rồi gọi xuống **Model** (như Repository/Database/API/Room) để thao tác dữ liệu.
4. **Model -> ViewModel:** **Model** thực thi tác vụ (lấy hoặc lưu dữ liệu) và trả kết quả về cho **ViewModel**.
5. **ViewModel Update State:** **ViewModel** nhận kết quả, sau đó cập nhật dữ liệu này vào các biến trạng thái có thể quan sát (ví dụ như `LiveData`, `StateFlow`).
6. **ViewModel -> View (Tự động):** **View** - lúc này hiểu là một Observer, đang âm thầm lắng nghe các biến trạng thái kia. Khi biến thay đổi giá trị, **View** tự động bắt được thông báo và tự cập nhật hiển thị lại UI. **ViewModel không hề biết View là ai và không bao giờ gọi trực tiếp View.**

### 1.4 So sánh mô hình MVC và MVVM trong Android

| Tiêu chí | MVC (Model - View - Controller) | MVVM (Model - View - ViewModel) |
| --- | --- | --- |
| **Vai trò của Activity/Fragment** | Bị bắt làm chung 2 việc: Vừa tạo View (hiển thị UI) vừa kiêm luôn **Controller** (như nghe sự kiện click, vòng đời Activity, gọi hàm Model). | Chỉ đóng vai trò thuần túy là **View** (thiết lập UI, truyền action click cho ViewModel và đăng ký Observer). |
| **Độ cồng kềnh của Code** | **"Massive View Controller"** - Activity/Fragment thường bị phình to hàng ngàn dòng chứa rác logic lẫn UI dẫn đến cực kỳ khó bảo trì. | ViewModel chia sẻ gánh vác sạch phần logic nghiệp vụ, giúp file code Activity/Fragment rất "gọn gàng nhẹ nhàng". |
| **Sự phụ thuộc (Coupling)** | Controller thao tác đổi giao diện **trực tiếp** với View (Code Controller gõ rõ chữ `textView.setText(...)`). Ràng buộc 2 bên quá chặt chẽ (Tight coupling). | ViewModel **không biết** View nào đang dùng mình. View phải chủ động đi "quan sát" ViewModel. Giúp thiết kế lỏng lẻo dễ tách rời (Loose coupling). |
| **Bảo toàn dữ liệu khi xoay máy** | Máy xoay là Activity load lại từ đầu, mất sạch dữ liệu nếu dev không viết code rườm rà tại hàm `onSaveInstanceState`. | **ViewModel** sinh ra để duy trì sự sống sót khi Activity bị Destroy tạm thời do đổi màn hình, giữ nguyên vẹn dữ liệu. |
| **Kiểm thử (Unit Test)** | Rất vất vả khó khăn vì class Controller có dính dáng trực tiếp với class Android Context, View của hệ điều hành. | Cực kì dễ (Easy to Test) vì ViewModel là class logic Kotlin thuần túy (Plain classes), không bị cột chặt với nền tảng Android. |

---

## 2. Lưu trữ dữ liệu: ViewModel, LiveData, Observer pattern

Đây là các thành phần cốt lõi của thư viện Android Architecture Components được thiết kế đặc biệt cho kiến trúc MVVM.

### 2.1 Observer Pattern
- Observer pattern là cơ sở của giao tiếp một chiều giữa ViewModel và View trong MVVM.
- Mẫu này gồm 2 thành phần chính:
  - **Subject (Chủ thể):** Lớp chứa dữ liệu gốc, duy trì một danh sách các Observer. Khi trạng thái hoặc dữ liệu của Subject thay đổi, nó sẽ gọi và thông báo cho danh sách Observer của nó.
  - **Observer (Người quan sát):** Theo dõi Subject. Khi Subject phát ra thông báo thay đổi, Observer sẽ thực hiện các hành động cập nhật tương ứng.
- Trong Android: `LiveData` đóng vai trò là Subject (Chủ thể), và các Activity/Fragment (View) đóng vai trò là Observer (Người quan sát).

### 2.2 ViewModel trong Android
- **Mục đích:** Lưu trữ và quản lý dữ liệu liên quan đến UI một cách thông minh, có thể nhận thức được vòng đời (lifecycle-aware).
- **Vấn đề nó giải quyết:** Bình thường, khi cấu hình điện thoại thay đổi (như xoay màn hình từ dọc sang ngang, thay đổi ngôn ngữ, ...), Activity hiện tại sẽ bị hệ thống Android hủy và tạo lại (destroy and recreate). Lúc này, dữ liệu lưu trong các biến bình thường của Activity sẽ bị mất trắng. ViewModel được sinh ra với vòng đời dài hơn để bảo toàn trạng thái qua các lần thay đổi cấu hình này.
- **Đặc điểm:**
  - Vòng đời của ViewModel dài hơn Activity/Fragment. Nó chỉ thực sự bị hệ thống hủy (gọi hàm `onCleared()`) khi Activity hoàn toàn kết thúc (ví dụ người dùng bấm Back thoát ứng dụng).
  - Tuyệt đối **KHÔNG lưu trữ** `Context`, tham chiếu của các `View` (như Button, TextView), hoặc `Activity` bên trong ViewModel. Nếu làm vậy sẽ gây Memory Leak do ViewModel giữ lại Activity cũ không cho tệp rác hệ thống (Garbage Collector) dọn dẹp.

**Cách khởi tạo và sử dụng cơ bản:**
```kotlin
class MyViewModel : ViewModel() {
    // Chứa logic và lưu trữ dữ liệu tại đây
}
```
Khởi tạo tự động trong Activity/Fragment bằng thư viện KTX (Tự động cấp phát ViewModel theo Scope):
```kotlin
// Cần thêm thư viện vào build.gradle.kts (Module: app)
// implementation("androidx.activity:activity-ktx:1.8.0")
// implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

val viewModel: MyViewModel by viewModels() 
```

### 2.3 LiveData
- **Mục đích:** Là một lớp bao bọc lấy một kiểu dữ liệu để tạo ra một biến chứa dữ liệu có thể theo dõi/quan sát được (observable data holder) và có khả năng nhận thức được vòng đời (lifecycle-aware components).
- **Đặc trưng Ưu việt:**
  - **Auto Nhận thức vòng đời (Lifecycle-aware):** LiveData "biết" về vòng đời của View đang theo dõi nó (Activity/Fragment). Nó chỉ thông báo bản cập nhật dữ liệu cho các Observer khi UI của chúng đang hiển thị hoặc sắp hiển thị trên màn hình (trạng thái `STARTED` hoặc `RESUMED`). Nếu UI đang bị ẩn dưới Background (trạng thái `STOPPED`), LiveData sẽ không gửi bản cập nhật.
  - **An toàn chống Crash:** Việc dừng gửi cập nhật khi rơi xuống Background giúp ngăn chặn 100% các lỗi crash ứng dụng phổ biến như NullPointerException do cố sức lôi giao diện đã bị hủy ra để thay thế nội dung.
  - **Chống rò rỉ bộ nhớ hoàn chỉnh:** Observer (View) tự động bị hủy theo dõi LiveData khi vòng đời của nó bị hệ điều hành tiêu hủy (`DESTROYED`). Code dev sẽ không còn phải nhớ để xóa observer khi thoát đóng màn hình.

**Phân biệt MutableLiveData vs LiveData:**
Để bảo vệ kiến trúc an toàn 1 chiều, Google cung cấp 2 phân loại LiveData:
- `MutableLiveData`: Là lớp mở rộng (subclass) của `LiveData`. Cung cấp khả năng ghi, thay đổi dữ liệu đè lên thông qua 2 hàm `setValue()` và `postValue()`.
  - `setValue(...)` hay gán `.value =`: Bắt buộc dùng khi muốn đổi giá trị ở luồng UI chính (Main-Thread).
  - `postValue(...)`: Bắt buộc đi dùng khi thay đổi giá trị từ một Luồng nền (Background-Thread). 
- **Quy tắc vàng (Best Practice):** Trong class ViewModel, dev luôn khởi tạo 2 biến đồng hành:
  - Một `MutableLiveData` với phạm vi truy cập là `private`, chỉ để ViewModel toàn quyền tự thay đổi giá trị.
  - Một `LiveData` là `public`, trỏ thẳng vào biến `private` kia. Và để hở nó ra cho View (Activity) nhìn vào để đăng ký quan sát. Từ ngoài Activity thay vì có thể làm bừa thay đổi bằng `.value` thì sẽ không thể biên dịch do biến trả ra bị khóa thành "Read-only" (Chỉ đọc).

### 2.4 Ví dụ Demo triển khai kết hợp (View + ViewModel + LiveData)

*Bước 1: Trong file MyViewModel (Tầng ViewModel)*
```kotlin
class UserViewModel : ViewModel() {
    
    // 1. Khởi tạo một kiểu Mutable để có thể thao tác ghi (Private)
    private val _userName = MutableLiveData<String>()
    
    // 2. Trả ra ngoài một LiveData loại chỉ đọc (Public) để view quan sát 
    val userName: LiveData<String> get() = _userName
    
    // 3. Khối lệnh xử lý tác vụ logic
    fun loadData() {
        // Giả lập xử lý load dữ liệu
        // Nếu load thành công thì gán giá trị mới vào biến LiveData, LiveData sẽ tự thông báo Observer
        _userName.value = "Hứa Văn Khương" 
        // Luôn nhớ nếu đang dùng luồng nền (Coroutines Dispatchers.IO) thì phải đổi dùng _userName.postValue()
    }
}
```

*Bước 2: Trong file UserActivity (Tầng UI/View)*
```kotlin
class UserActivity : AppCompatActivity() {
    // Lấy instance của viewmodel dùng ktx delegation
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val tvName = findViewById<TextView>(R.id.tvName)
        val btnLoad = findViewById<Button>(R.id.btnLoad)

        // 1. Phân công Lắng nghe (Observe) từ trước:
        // Đăng ký nhận thông báo mỗi khi biến public `userName` bị gán nội dung mới
        // Chuyền `this` (LifecycleOwner) để biến trở nên lifecycle-aware
        viewModel.userName.observe(this, Observer { updatedStringName ->
            // Update the UI
            tvName.text = updatedStringName
        })

        // 2. Truyền lệnh cho ViewModel thực hiện logic của nó khi có va chạm với UI
        btnLoad.setOnClickListener {
            viewModel.loadData() 
        }
    }
}
```

### 3. Quản lý trạng thái giao diện (UI State Handling) trong MVVM
Thay vì dùng nhiều biến `LiveData` rời rạc cho từng thành phần UI (ví dụ: một biến `isLoading`, một biến `errorMessage`, một biến `userName`), thực tế trong các dự án Android người ta thường đóng gói tất cả vào một trạng thái chung gọi là **UI State**. Điều này đảm bảo giao diện luôn chỉ phản chiếu một trạng thái thống nhất ở mọi thời điểm, tránh các lỗi logic hiển thị chắp vá.

Kỹ thuật được dùng phổ biến nhất là **Sealed Class** (hoặc Sealed Interface) trong Kotlin để định nghĩa giới hạn các trạng thái của màn hình:

```kotlin
// Định nghĩa chuẩn các trạng thái có thể xảy ra của màn hình User
sealed class UserUiState {
    object Loading : UserUiState()                        // Trạng thái đang tải dữ liệu
    data class Success(val name: String) : UserUiState()  // Trạng thái thành công, mang theo Data
    data class Error(val message: String) : UserUiState() // Trạng thái lỗi, mang theo lời báo lỗi
}
```

*Cập nhật lại trong ViewModel:*
```kotlin
class UserViewModel : ViewModel() {
    private val _uiState = MutableLiveData<UserUiState>()
    val uiState: LiveData<UserUiState> get() = _uiState

    fun loadData() {
        _uiState.value = UserUiState.Loading // 1. Báo UI bật vòng xoay loading

        // Giả lập đang xử lý / gọi API HTTP...
        try {
            val resultName = "Hứa Văn Khương"
            _uiState.value = UserUiState.Success(resultName) // 2. Thành công -> Đẩy data lên UI
        } catch (e: Exception) {
            _uiState.value = UserUiState.Error("Lỗi kết nối mạng!") // 3. Thất bại -> Đẩy lỗi lên UI
        }
    }
}
```

*Cập nhật lại trong View (Activity/Fragment):*
```kotlin
// Lắng nghe duy nhất 1 luồng biến State
viewModel.uiState.observe(this, Observer { state ->
    when (state) {
        is UserUiState.Loading -> {
            progressBar.visibility = View.VISIBLE
            tvName.visibility = View.GONE
        }
        is UserUiState.Success -> {
            progressBar.visibility = View.GONE
            tvName.visibility = View.VISIBLE
            tvName.text = state.name // Bóc data an toàn
        }
        is UserUiState.Error -> {
            progressBar.visibility = View.GONE
            Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
        }
    }
})
```

### 4. Tóm tắt
- Cấu trúc **MVVM** chia rẽ nhiệm vụ của các mảnh Component (Tránh code nhồi nhét Massive View Controller), tạo luồng chảy **chỉ 1 chiều** rất rành mạch.
- **ViewModel** tách rời UI, sống sót an toàn qua biến động vòng đời Activity (xoay màn hình).
- **LiveData** trung thành vận chuyển dữ liệu một cách an toàn tới Observer nhờ tự nhận biết và bám theo vòng đời Lifecycle (Lifecycle-aware), chống Memory Leak.
- **UI State** bọc mọi tình huống của 1 màn hình vào 1 class trạng thái (Loading/Success/Error) duy nhất giúp UI có phản ứng nhất quán (Đã xoay Loading thì tắt Data), không xảy ra xung đột giao diện.

