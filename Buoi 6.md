# 📱 Buổi 6 — Fragment trong Android

---

## 1. Fragment Là Gì?

**Fragment** là một thành phần UI có vòng đời riêng, được nhúng bên trong một `Activity`.  
Có thể hiểu Fragment như một "màn hình con" nằm trong màn hình lớn (Activity).

### Tại sao cần Fragment?

| Lý do | Ý nghĩa thực tế |
|---|---|
| **Tái sử dụng** | Cùng một Fragment dùng được ở nhiều Activity |
| **Linh hoạt layout** | Phone 1 cột, tablet 2 cột — cùng code |
| **Điều hướng hiện đại** | Tích hợp tốt với Navigation Component |
| **Dễ bảo trì** | Chia nhỏ màn hình lớn thành các module độc lập |

### Activity vs Fragment

| | Activity | Fragment |
|---|---|---|
| Tồn tại độc lập | ✅ Có | ❌ Phải nằm trong Activity |
| Chứa UI | ✅ Có | ✅ Có |
| Vòng đời riêng | ✅ | ✅ (phụ thuộc vào Activity cha) |
| Vai trò | Container cấp màn hình | Thành phần UI con |

---

## 2. Vòng Đời Fragment

### 2.1 Sơ Đồ Vòng Đời

```
        onAttach(context)
              ↓
        onCreate(savedInstanceState)
              ↓
        onCreateView(inflater, container, savedInstanceState)
              ↓
        onViewCreated(view, savedInstanceState)
              ↓
           onStart()
              ↓
           onResume()
              ↓
     ┌────────┴───────────┐
     ↓                    ↓
  onPause()      [Fragment hoạt động]
     ↓
  onStop()
     ↓
  onDestroyView()   ← View bị hủy, Fragment object vẫn còn
     ↓
  onDestroy()
     ↓
  onDetach()
```

### 2.2 Ý Nghĩa Từng Callback

| Callback | Khi nào được gọi | Nên làm gì |
|---|---|---|
| `onAttach(context)` | Fragment gắn vào Activity | Lấy interface callback từ Activity |
| `onCreate(...)` | Tạo Fragment (chưa có View) | Khởi tạo dữ liệu, đọc `arguments` |
| `onCreateView(...)` | Tạo giao diện (inflate XML) | Inflate layout, trả về View |
| `onViewCreated(...)` | View đã sẵn sàng | Bind view, set listener, observe data |
| `onDestroyView()` | View bị hủy | **Set `_binding = null`** để tránh memory leak |
| `onDetach()` | Tách khỏi Activity | Clear callback/reference đến Activity |

> [!IMPORTANT]
> Vòng đời **View** của Fragment ngắn hơn vòng đời của Fragment object.  
> View bị hủy tại `onDestroyView()`, nhưng Fragment object vẫn còn.  
> → Phải clear binding ở `onDestroyView()`, **không phải** `onDestroy()`.

### 2.3 Ví Dụ Fragment Cơ Bản Với ViewBinding

```kotlin
class DemoFragment : Fragment() {

    private var _binding: FragmentDemoBinding? = null
    private val binding get() = _binding!!

    // ① Inflate layout → trả về View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ② View đã sẵn sàng → bắt đầu thao tác UI
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = "Hello Fragment"
        binding.btnAction.setOnClickListener {
            // xử lý click
        }
    }

    // ③ Hủy binding để tránh memory leak
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

> [!TIP]
> Chỉ truy cập `binding` trong khoảng từ `onCreateView()` đến trước `onDestroyView()`.  
> Nếu dùng ngoài khoảng đó sẽ ném `NullPointerException`.

---

## 3. Thêm Fragment Vào Activity

### 3.1 Cách Tĩnh — Khai Báo Trong XML

Thêm Fragment trực tiếp trong layout XML của Activity.  
**Không thể thay thế hay xóa ở runtime.**

```xml
<!-- activity_main.xml -->
<LinearLayout ...>

    <fragment
        android:id="@+id/fragmentHeader"
        android:name="com.example.app.HeaderFragment"
        android:layout_width="match_parent"
        android:layout_height="60dp" />

</LinearLayout>
```

### 3.2 Cách Động — Dùng FragmentManager (Phổ Biến Hơn)

Khai báo một container trong XML:

```xml
<!-- activity_main.xml -->
<FrameLayout
    android:id="@+id/fragmentContainer"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

Rồi thêm Fragment bằng code:

```kotlin
// Trong Activity
supportFragmentManager.beginTransaction()
    .replace(R.id.fragmentContainer, HomeFragment())
    .addToBackStack(null)   // cho phép nhấn Back quay lại
    .commit()
```

---

## 4. FragmentManager & Transaction

### 4.1 FragmentManager

`FragmentManager` quản lý toàn bộ Fragment trong một Activity hoặc Fragment cha.

| Truy cập từ | API |
|---|---|
| Activity | `supportFragmentManager` |
| Fragment (quản lý fragment con) | `childFragmentManager` |
| Fragment (lên Activity cha) | `parentFragmentManager` |

### 4.2 Các Phương Thức Transaction

| Phương thức | Tác dụng |
|---|---|
| `add(containerId, fragment)` | Thêm Fragment vào container (chồng lên trên) |
| `replace(containerId, fragment)` | Thay thế Fragment hiện tại |
| `remove(fragment)` | Xóa Fragment khỏi container |
| `hide(fragment)` | Ẩn Fragment (vẫn còn trong bộ nhớ) |
| `show(fragment)` | Hiện lại Fragment đã ẩn |

### 4.3 Back Stack

```kotlin
supportFragmentManager.beginTransaction()
    .replace(R.id.container, DetailFragment())
    .addToBackStack("detail")   // tên tùy chọn để popBackStack theo tên
    .commit()
```

| API | Tác dụng |
|---|---|
| `addToBackStack(name)` | Thêm transaction vào back stack |
| `popBackStack()` | Quay về transaction trước (giống nhấn Back) |
| `popBackStack(name, flags)` | Quay về đến transaction có tên `name` |

> [!WARNING]
> Nếu **không** gọi `addToBackStack()`, nhấn Back sẽ đóng Activity thay vì quay lại Fragment trước.

---

## 5. Giao Tiếp Activity ↔ Fragment

### 5.1 Activity → Fragment

#### Cách 1: Truyền Dữ Liệu Qua `arguments` (Khi Khởi Tạo)

```kotlin
class ProfileFragment : Fragment() {

    companion object {
        private const val ARG_USER_NAME = "arg_user_name"
        private const val ARG_USER_ID   = "arg_user_id"

        // Factory method — cách chuẩn để tạo Fragment có tham số
        fun newInstance(userId: Int, userName: String): ProfileFragment {
            return ProfileFragment().apply {
                arguments = bundleOf(
                    ARG_USER_ID   to userId,
                    ARG_USER_NAME to userName
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId   = arguments?.getInt(ARG_USER_ID)
        val userName = arguments?.getString(ARG_USER_NAME)
    }
}
```

**Trong Activity khởi tạo:**

```kotlin
val fragment = ProfileFragment.newInstance(userId = 42, userName = "Minh")
supportFragmentManager.beginTransaction()
    .replace(R.id.container, fragment)
    .commit()
```

> [!IMPORTANT]
> **Không nên** tạo constructor có tham số cho Fragment.  
> Khi hệ thống khôi phục Fragment (xoay màn hình), nó luôn gọi constructor không tham số.  
> → Dùng `arguments` + `newInstance()` là cách chuẩn.

#### Cách 2: Gọi Public Function Của Fragment

```kotlin
// Trong Fragment
class DetailFragment : Fragment() {
    fun updateTitle(title: String) {
        binding.tvTitle.text = title
    }
}

// Trong Activity
val fragment = supportFragmentManager
    .findFragmentById(R.id.container) as? DetailFragment
fragment?.updateTitle("Dữ liệu từ Activity")
```

---

### 5.2 Fragment → Activity

#### Cách Chuẩn: Interface Callback

**Bước 1 — Khai báo interface trong Fragment:**

```kotlin
class InputFragment : Fragment() {

    // ① Định nghĩa interface
    interface OnSubmitListener {
        fun onSubmit(data: String)
    }

    private var listener: OnSubmitListener? = null

    // ② Lấy listener từ Activity cha khi Fragment gắn vào
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnSubmitListener
            ?: throw RuntimeException("${context::class.simpleName} phải implement OnSubmitListener")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSubmit.setOnClickListener {
            listener?.onSubmit(binding.etInput.text.toString())
        }
    }

    // ③ Clear listener khi Fragment tách khỏi Activity
    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
```

**Bước 2 — Activity implement interface:**

```kotlin
class MainActivity : AppCompatActivity(), InputFragment.OnSubmitListener {

    override fun onSubmit(data: String) {
        binding.tvResult.text = "Nhận được: $data"
    }
}
```

---

### 5.3 Fragment ↔ Fragment

Không nên cho 2 Fragment gọi trực tiếp nhau.  
Nên dùng một trong các cách trung gian:

| Cách | Khi nào dùng |
|---|---|
| Thông qua Activity cha | Fragment cùng cấp, đơn giản |
| `SharedViewModel` | Khuyến nghị khi dùng MVVM |
| `FragmentResultListener` | Giao tiếp ngắn gọn, không cần ViewModel |

#### Ví Dụ — Thông Qua Activity Cha

**Kịch bản:** Fragment A có một ô nhập văn bản, nhấn nút gửi → Fragment B hiển thị nội dung đó.

```
[Fragment A] ──onSend()──▶ [MainActivity] ──showInB()──▶ [Fragment B]
```

**Bước 1 — Fragment A khai báo interface và gọi lên Activity:**

```kotlin
class FragmentA : Fragment() {

    interface OnSendToB {
        fun onSendToB(message: String)
    }

    private var listener: OnSendToB? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnSendToB
            ?: throw RuntimeException("Activity phải implement OnSendToB")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSend.setOnClickListener {
            val msg = binding.etMessage.text.toString()
            listener?.onSendToB(msg)        // ① báo lên Activity
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
```

**Bước 2 — Fragment B cung cấp hàm public để nhận dữ liệu:**

```kotlin
class FragmentB : Fragment() {

    fun displayMessage(message: String) {   // ③ Activity gọi hàm này
        binding.tvResult.text = message
    }
}
```

**Bước 3 — Activity làm cầu nối:**

```kotlin
class MainActivity : AppCompatActivity(), FragmentA.OnSendToB {

    override fun onSendToB(message: String) {   // ② nhận từ Fragment A
        val fragmentB = supportFragmentManager
            .findFragmentById(R.id.containerB) as? FragmentB
        fragmentB?.displayMessage(message)       // ③ chuyển sang Fragment B
    }
}
```

> [!NOTE]
> Luồng dữ liệu luôn đi qua Activity:  
> **Fragment A → Activity → Fragment B**  
> Hai Fragment không bao giờ biết sự tồn tại của nhau.

---

#### Ví Dụ — `FragmentResultListener`

**Fragment A gửi dữ liệu:**

```kotlin
// Fragment A
val bundle = bundleOf("result_key" to "Dữ liệu từ Fragment A")
parentFragmentManager.setFragmentResult("my_request", bundle)
```

**Fragment B lắng nghe:**

```kotlin
// Fragment B — trong onViewCreated
parentFragmentManager.setFragmentResultListener("my_request", viewLifecycleOwner) { _, bundle ->
    val result = bundle.getString("result_key")
    binding.tvResult.text = result
}
```

> [!TIP]
> `setFragmentResultListener` tự động hủy khi vòng đời Fragment kết thúc (nhờ `viewLifecycleOwner`).  
> Không cần cleanup thủ công.

---

## 6. Lỗi Thường Gặp & Cách Xử Lý

| Lỗi | Nguyên nhân | Cách xử lý |
|---|---|---|
| `NullPointerException` với binding | Truy cập `binding` sau `onDestroyView()` | Set `_binding = null` đúng chỗ; chỉ dùng binding khi View còn sống |
| `ClassCastException` ở `onAttach` | Activity chưa implement interface | Cho Activity implement interface, hoặc chuyển sang `FragmentResult` / `SharedViewModel` |
| Mất dữ liệu khi xoay màn hình | Lưu state trong biến thường | Dùng `ViewModel`, `savedInstanceState`, hoặc `arguments` |
| Memory leak | Giữ reference Context/Activity lâu dài | Clear ở `onDestroyView()` / `onDetach()` |
| Fragment trùng lặp sau xoay màn hình | Add Fragment mà không kiểm tra | Kiểm tra `savedInstanceState == null` trước khi thêm Fragment |

```kotlin
// Chống Fragment bị thêm 2 lần khi xoay màn hình
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (savedInstanceState == null) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment())
            .commit()
    }
}
```

---

## 7. Best Practices

- ✅ Dùng `newInstance()` + `arguments` để truyền dữ liệu khởi tạo cho Fragment.
- ✅ Luôn dùng `viewLifecycleOwner` khi observe `LiveData` trong Fragment.
- ✅ Clear `_binding = null` trong `onDestroyView()`.
- ✅ Kiểm tra `savedInstanceState == null` trước khi add Fragment trong `onCreate`.
- ✅ Ưu tiên `SharedViewModel` hoặc `FragmentResultListener` cho giao tiếp Fragment ↔ Fragment.
- ❌ Không tạo constructor có tham số cho Fragment.
- ❌ Không truyền object phức tạp (không serializable) qua `Bundle`.
- ❌ Không giữ reference trực tiếp đến Activity/View lâu dài.

---

## 8. Tóm Tắt

```
Fragment
 ├─ Là UI component có vòng đời riêng, sống trong Activity
 ├─ Vòng đời View: onCreateView → onViewCreated → onDestroyView
 │
 ├─ Thêm vào Activity
 │   ├─ Tĩnh: khai báo trong XML (không linh hoạt)
 │   └─ Động: FragmentManager.beginTransaction()
 │
 ├─ Giao tiếp
 │   ├─ Activity → Fragment : arguments, gọi hàm public
 │   ├─ Fragment → Activity : Interface callback
 │   └─ Fragment ↔ Fragment : SharedViewModel, FragmentResultListener
 │
 └─ Lỗi phổ biến
     ├─ Binding null   → clear đúng chỗ
     ├─ Fragment trùng → kiểm tra savedInstanceState
     └─ Memory leak    → clear reference ở onDestroyView / onDetach
```
