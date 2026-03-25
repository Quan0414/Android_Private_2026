# 📱 Buổi 4 — Glide, Xử Lý Sự Kiện, Activity & Điều Hướng

---

## 1. Thư Viện Glide

**Glide** là thư viện load ảnh phổ biến nhất trên Android, do Google phát triển. Glide giúp load ảnh từ URL, file, resource một cách nhanh, hiệu quả và có caching tự động.

### 1.1 Thêm Dependency

```kotlin
// build.gradle (app)
dependencies {
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
```

> [!IMPORTANT]
> Thêm permission Internet vào `AndroidManifest.xml` nếu load ảnh từ URL:
> ```xml
> <uses-permission android:name="android.permission.INTERNET" />
> ```

### 1.2 Context Là Gì?

**Context** là một đối tượng đặc biệt trong Android, đóng vai trò như **"cửa ngõ"** để code truy cập vào tài nguyên và hệ thống của ứng dụng.

Hiểu đơn giản: nếu Android là một toà nhà, thì **Context là thẻ từ** — bạn cần có nó mới vào được phòng (load ảnh, hiện Toast, mở Activity, đọc String resource...).

```
Context cho phép bạn:
├── Load ảnh, âm thanh, string từ res/
├── Hiện Toast, Dialog, Snackbar
├── Mở Activity mới
├── Truy cập SharedPreferences, Database
└── Gọi các Service hệ thống (GPS, Camera...)
```

**Ai là Context?**

| Đối tượng | Là Context không? | Ghi chú |
|-----------|:-----------------:|---------|
| `Activity` | ✅ | Phổ biến nhất, dùng được mọi nơi |
| `Fragment` | ❌ (bản thân) | Dùng `requireContext()` hoặc `context` |
| `Application` | ✅ | Tồn tại suốt vòng đời app |
| `View` | ✅ | Dùng `view.context` |
| `this` (trong Activity) | ✅ | Chính là Activity đó |

**Dùng `this` hay `applicationContext`?**

```kotlin
// ✅ Dùng "this" (Activity context) — cho UI, Toast, Dialog, Glide
Toast.makeText(this, "Xin chào!", Toast.LENGTH_SHORT).show()
Glide.with(this).load(url).into(imageView)

// ✅ Dùng "applicationContext" — cho những thứ không gắn với UI
// ví dụ: Room Database, SharedPreferences, WorkManager
val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "mydb").build()
```

> [!WARNING]
> Không dùng `applicationContext` cho Glide hoặc Dialog — chúng cần lifecycle của Activity để hoạt động đúng và tự huỷ khi cần thiết.

---

### 1.3 Cú Pháp Cơ Bản

```kotlin
Glide.with(context)       // Context: Activity, Fragment, View
    .load(url)            // Nguồn ảnh: URL, File, Uri, Resource ID
    .into(imageView)      // ImageView đích
```

### 1.3 Các Tuỳ Chọn Thường Dùng

```kotlin
Glide.with(this)
    .load("https://picsum.photos/400/300")
    .placeholder(R.drawable.ic_placeholder)   // Ảnh hiển thị khi đang tải
    .error(R.drawable.ic_error)               // Ảnh hiển thị khi lỗi
    .centerCrop()                             // Scale kiểu centerCrop
    .override(200, 200)                       // Giới hạn kích thước decode
    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache cả ảnh gốc và đã scale
    .into(ivAvatar)
```

### 1.4 Load Từ Nhiều Nguồn Khác Nhau

| Nguồn | Code |
|-------|------|
| URL | `.load("https://example.com/image.jpg")` |
| File | `.load(File("/sdcard/photo.jpg"))` |
| Resource | `.load(R.drawable.my_image)` |
| Uri | `.load(uri)` |
| Bitmap | `.load(bitmap)` |

### 1.5 Transform — Biến Đổi Ảnh

```kotlin
// Ảnh tròn
Glide.with(this)
    .load(url)
    .circleCrop()
    .into(ivAvatar)

// Bo góc tuỳ chỉnh (cần thêm dependency glide-transformations hoặc dùng RoundedCorners)
Glide.with(this)
    .load(url)
    .transform(RoundedCorners(24))
    .into(ivPhoto)
```

### 1.6 Xoá Cache

---

## 2. Xử Lý Sự Kiện (Event Handling)

Sự kiện là các hành động của người dùng: chạm, nhấn, nhập liệu, v.v. Android sử dụng cơ chế **Listener** để lắng nghe và xử lý sự kiện.

### 2.1 Click Event — Sự Kiện Nhấn

**Cách 1: Dùng lambda (khuyến nghị)**

```kotlin
// Trong Activity
btnLogin.setOnClickListener {
    // Xử lý khi người dùng nhấn nút
    Toast.makeText(this, "Đã nhấn Đăng Nhập!", Toast.LENGTH_SHORT).show()
}
```

**Cách 2: Implement interface**

```kotlin
class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnLogin.setOnClickListener(this)
        btnRegister.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogin    -> handleLogin()
            R.id.btnRegister -> handleRegister()
        }
    }
}
```

**Cách 3: Khai báo trong XML (ít dùng)**

```xml
<Button
    android:onClick="handleLogin"
    ... />
```

```kotlin
// Trong Activity — tên hàm phải khớp với XML
fun handleLogin(view: View) {
    // ...
}
```

### 2.2 Long Click Event — Nhấn Giữ

```kotlin
btnDelete.setOnLongClickListener {
    Toast.makeText(this, "Nhấn giữ để xoá", Toast.LENGTH_SHORT).show()
    true  // true = đã xử lý, không tiếp tục truyền sự kiện
}
```

### 2.3 Touch Event — Sự Kiện Chạm

```kotlin
view.setOnTouchListener { v, event ->
    when (event.action) {
        MotionEvent.ACTION_DOWN -> { /* Bắt đầu chạm */ }
        MotionEvent.ACTION_MOVE -> { /* Đang di chuyển */ }
        MotionEvent.ACTION_UP   -> { /* Nhấc tay lên */ }
    }
    true
}
```

### 2.4 Text Change — Theo Dõi Thay Đổi Text

```kotlin
etSearch.addTextChangedListener(object : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Trước khi text thay đổi
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Trong khi text đang thay đổi — dùng nhiều nhất
        performSearch(s.toString())
    }

    override fun afterTextChanged(s: Editable?) {
        // Sau khi text đã thay đổi
    }
})
```

### 2.5 Focus Change — Thay Đổi Focus

```kotlin
etEmail.setOnFocusChangeListener { view, hasFocus ->
    if (hasFocus) {
        tilEmail.error = null  // Xoá lỗi khi người dùng bắt đầu nhập
    } else {
        // Validate khi rời focus
        if (etEmail.text.isNullOrEmpty()) {
            tilEmail.error = "Email không được trống"
        }
    }
}
```

---

## 3. Activity & Vòng Đời Activity

### 3.1 Activity Là Gì?

**Activity** là một màn hình trong ứng dụng Android. Mỗi Activity tương ứng với một giao diện người dùng. Một ứng dụng thường có nhiều Activity (màn đăng nhập, màn danh sách, màn chi tiết...).

```
App
├── LoginActivity       ← Màn đăng nhập
├── HomeActivity        ← Màn chính
└── DetailActivity      ← Màn chi tiết
```

### 3.2 Tạo Activity

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Gắn file XML layout
        
        // Khởi tạo View, xử lý logic ban đầu tại đây
    }
}
```

```xml
<!-- AndroidManifest.xml — phải khai báo mọi Activity -->
<activity android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <!-- Đây là Activity mở đầu tiên khi khởi động app -->
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<activity android:name=".DetailActivity" android:exported="false" />
```

### 3.3 Vòng Đời Activity (Activity Lifecycle)

```
                  ┌──────────────┐
                  │   onCreate() │  ← Activity được tạo lần đầu
                  └──────┬───────┘
                         ↓
                  ┌──────────────┐
                  │   onStart()  │  ← Activity hiển thị nhưng chưa tương tác được
                  └──────┬───────┘
                         ↓
                  ┌──────────────┐
                  │  onResume()  │  ← Activity chạy bình thường, người dùng tương tác
                  └──────┬───────┘
                         │
           ┌─────────────┴─────────────┐
           ↓                           ↓
    ┌─────────────┐             ┌─────────────┐
    │  onPause()  │             │  App bình   │
    │  Mất focus  │             │  thường     │
    └──────┬──────┘             └─────────────┘
           ↓
    ┌─────────────┐
    │  onStop()   │  ← Activity không còn hiển thị
    └──────┬──────┘
           ↓
    ┌─────────────┐
    │ onDestroy() │  ← Activity bị huỷ hoàn toàn
    └─────────────┘
```

### 3.4 Mô Tả Từng Callback

| Callback | Khi nào được gọi | Dùng để |
|----------|-----------------|---------|
| `onCreate()` | Activity được tạo lần đầu | Khởi tạo UI, bind View, thiết lập dữ liệu ban đầu |
| `onStart()` | Activity trở nên visible | Đăng ký BroadcastReceiver |
| `onResume()` | Activity có focus, người dùng tương tác được | Bắt đầu animation, camera, GPS |
| `onPause()` | Activity mất focus (chuyển app, incoming call...) | Lưu trạng thái tạm, dừng animation |
| `onStop()` | Activity không còn hiển thị | Giải phóng tài nguyên nặng, lưu dữ liệu |
| `onDestroy()` | Activity bị huỷ hoàn toàn | Cleanup, giải phóng tài nguyên cuối cùng |
| `onRestart()` | Activity quay lại từ `onStop()` | Refresh dữ liệu |

### 3.5 Ví Dụ Thực Tế

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // ✅ Khởi tạo UI, load dữ liệu ban đầu
    }

    override fun onResume() {
        super.onResume()
        // ✅ Refresh dữ liệu mỗi khi quay lại màn hình này
        loadLatestData()
    }

    override fun onPause() {
        super.onPause()
        // ✅ Lưu trạng thái người dùng đang nhập dở
        saveDraft()
    }

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Giải phóng tài nguyên
    }
}
```

> [!IMPORTANT]
> Luôn gọi `super.onXxx()` ở đầu mỗi callback để Android xử lý logic nội bộ trước.

### 3.6 Lưu & Khôi Phục Trạng Thái

Khi màn hình xoay ngang/dọc, Activity bị **destroy rồi tạo lại**. Dùng `onSaveInstanceState` để lưu dữ liệu:

```kotlin
override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString("USER_INPUT", etName.text.toString())
    outState.putInt("SCROLL_POSITION", currentPosition)
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Khôi phục dữ liệu sau khi xoay màn hình
    savedInstanceState?.let {
        etName.setText(it.getString("USER_INPUT"))
        currentPosition = it.getInt("SCROLL_POSITION")
    }
}
```

---

## 4. Chuyển Màn & Truyền Dữ Liệu

### 4.1 Intent Là Gì?

**Intent** là đối tượng dùng để:
- **Chuyển màn** (mở Activity mới)
- **Truyền dữ liệu** giữa các Activity
- **Gọi component** hệ thống (mở camera, gọi điện...)

```kotlin
// Cú pháp cơ bản
val intent = Intent(context, TargetActivity::class.java)
startActivity(intent)
```

### 4.2 Chuyển Màn Đơn Giản

```kotlin
// Cách 1: Thông thường
btnGoToDetail.setOnClickListener {
    val intent = Intent(this, DetailActivity::class.java)
    startActivity(intent)
}

// Cách 2: Dùng extension (ngắn hơn)
btnGoToDetail.setOnClickListener {
    startActivity<DetailActivity>()  // Cần import androidx.core.content.ContextCompat
}
```

### 4.3 Truyền Dữ Liệu Với `putExtra`

**Bên gửi (Sender)**

```kotlin
val intent = Intent(this, DetailActivity::class.java).apply {
    putExtra("USER_ID", 42)
    putExtra("USER_NAME", "Nguyễn Văn A")
    putExtra("IS_PREMIUM", true)
}
startActivity(intent)
```

**Bên nhận (Receiver) — trong `onCreate()`**

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_detail)

    val userId   = intent.getIntExtra("USER_ID", -1)       // -1 là giá trị mặc định
    val userName = intent.getStringExtra("USER_NAME") ?: ""
    val isPremium = intent.getBooleanExtra("IS_PREMIUM", false)

    tvName.text = userName
}
```

### 4.4 Các Kiểu Dữ Liệu Được Hỗ Trợ

| Loại | Gửi | Nhận |
|------|-----|------|
| Int | `putExtra("key", 123)` | `getIntExtra("key", 0)` |
| String | `putExtra("key", "text")` | `getStringExtra("key")` |
| Boolean | `putExtra("key", true)` | `getBooleanExtra("key", false)` |
| Float | `putExtra("key", 3.14f)` | `getFloatExtra("key", 0f)` |
| Double | `putExtra("key", 3.14)` | `getDoubleExtra("key", 0.0)` |
| Long | `putExtra("key", 123L)` | `getLongExtra("key", 0L)` |
| Serializable | `putExtra("key", obj)` | `getSerializableExtra("key")` |
| Parcelable | `putExtra("key", obj)` | `getParcelableExtra("key")` |

### 4.5 Truyền Object — Parcelable

Để truyền object phức tạp, class phải implement **Parcelable** (hoặc dùng annotation `@Parcelize` cho tiện):

```kotlin
// Thêm plugin vào build.gradle
plugins {
    id("kotlin-parcelize")
}
```

```kotlin
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int,
    val name: String,
    val email: String
) : Parcelable
```

```kotlin
// Gửi
val user = User(id = 1, name = "Minh", email = "minh@gmail.com")
val intent = Intent(this, ProfileActivity::class.java)
intent.putExtra("USER", user)
startActivity(intent)

// Nhận
val user = intent.getParcelableExtra<User>("USER")
tvName.text = user?.name
```

### 4.6 Nhận Kết Quả Từ Activity Con — `ActivityResultLauncher`

Khi cần Activity con trả về kết quả (ví dụ: chọn ảnh, nhập dữ liệu), dùng `ActivityResultLauncher`:

```kotlin
class MainActivity : AppCompatActivity() {

    // Khai báo launcher
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val updatedName = result.data?.getStringExtra("NEW_NAME")
            tvUserName.text = updatedName
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)  // Mở và chờ kết quả
        }
    }
}
```

```kotlin
// EditProfileActivity — trả kết quả về
class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        btnSave.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("NEW_NAME", etName.text.toString())
            setResult(RESULT_OK, resultIntent)  // Trả kết quả
            finish()                            // Đóng Activity
        }
    }
}
```

### 4.7 Điều Hướng Nâng Cao — Back Stack & Flags

**Back Stack** là ngăn xếp lưu lịch sử các Activity. Nhấn nút Back sẽ pop Activity trên cùng.

```
Back Stack sau khi mở 3 màn:
┌─────────────────┐
│  DetailActivity │  ← Màn đang hiện
├─────────────────┤
│   HomeActivity  │
├─────────────────┤
│  LoginActivity  │  ← Đáy stack
└─────────────────┘
```

**Flags thường dùng:**

```kotlin
// Xoá toàn bộ back stack, HomeActivity là gốc mới
// Dùng sau khi đăng nhập xong
val intent = Intent(this, HomeActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
}
startActivity(intent)

// Không tạo Activity mới nếu đã tồn tại trong back stack
intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

// Đưa Activity đã tồn tại lên trên cùng
intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
```

### 4.8 Đóng Activity

```kotlin
// Đóng Activity hiện tại, quay về Activity trước
finish()

// Đóng và trả về kết quả
setResult(RESULT_OK, resultIntent)
finish()

// Đóng và trả về kết quả huỷ
setResult(RESULT_CANCELED)
finish()
```

> [!TIP]
> Khi chuyển sang màn Đăng nhập sau khi Đăng xuất, luôn dùng `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK` để người dùng không nhấn Back quay lại màn cần đăng nhập.

---

## 5. Tổng Kết

| Chủ đề | Điểm quan trọng |
|--------|----------------|
| **Glide** | `.with()` → `.load()` → `.into()` — tự quản lý lifecycle |
| **Event** | Dùng lambda `setOnClickListener { }` — đơn giản và ngắn gọn nhất |
| **Activity Lifecycle** | `onCreate` → `onResume` → `onPause` → `onStop` → `onDestroy` |
| **Intent** | Dùng `putExtra` / `getXxxExtra` để truyền dữ liệu |
| **Parcelable** | Dùng `@Parcelize` để truyền object dễ dàng |
| **ActivityResult** | `registerForActivityResult` thay cho `startActivityForResult` (đã deprecated) |
| **Flags** | `CLEAR_TASK` để xoá back stack sau đăng nhập/đăng xuất |
