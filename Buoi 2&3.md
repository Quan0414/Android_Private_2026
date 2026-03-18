# 📱 Buổi 2&3 — Thiết Kế Giao Diện (1)(2)

---

## 1. Khái Niệm Nền Tảng

### View vs ViewGroup

| | **View** | **ViewGroup** |
|---|---|---|
| **Là gì** | Thành phần UI cơ bản | Container chứa các View khác |
| **Ví dụ** | `TextView`, `Button`, `ImageView` | `LinearLayout`, `ConstraintLayout`, `FrameLayout` |
| **Vai trò** | Hiển thị nội dung | Sắp xếp bố cục |

```
ViewGroup (LinearLayout)
├── View (TextView)
├── View (EditText)
└── View (Button)
```

### Hai Thuộc Tính Bắt Buộc

Mọi View và ViewGroup đều phải có `layout_width` và `layout_height`:

| Giá trị | Ý nghĩa |
|---------|---------|
| `match_parent` | Bằng kích thước của ViewGroup cha |
| `wrap_content` | Vừa đủ với nội dung bên trong |
| `0dp` | Dùng trong ConstraintLayout — chiếm phần còn lại |
| `200dp` | Kích thước cố định |

### Đơn Vị Đo Lường

| Đơn vị | Dùng cho | Lý do |
|--------|---------|-------|
| `dp` (density-independent pixels) | Kích thước, margin, padding | Tự động scale theo mật độ màn hình |
| `sp` (scale-independent pixels) | Cỡ chữ (`textSize`) | Scale theo cả mật độ lẫn font size của người dùng |

> [!IMPORTANT]
> Luôn dùng `dp` cho kích thước layout và `sp` cho cỡ chữ. Không dùng `px` vì sẽ hiển thị sai trên các màn hình có mật độ khác nhau.

---

## 2. LinearLayout

**LinearLayout** sắp xếp các View con theo **một hàng ngang hoặc dọc**.

### 2.1 Thuộc Tính Chính

| Thuộc tính | Giá trị | Mô tả |
|-----------|---------|-------|
| `android:orientation` | `vertical` / `horizontal` | Hướng sắp xếp |
| `android:gravity` | `center`, `start`, `end`... | Căn chỉnh nội dung **bên trong** ViewGroup |
| `android:layout_gravity` | `center`, `start`, `end`... | Căn chỉnh View **trong** LinearLayout cha |
| `android:layout_weight` | số nguyên (vd: `1`, `2`) | Tỷ lệ chiếm không gian còn lại |
| `android:weightSum` | số nguyên (vd: `3`) | Tổng weight, thường đặt ở LinearLayout cha |

### 2.2 Ví Dụ — Xếp Dọc

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:gravity="center_horizontal">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Tiêu đề"
        android:textSize="20sp"
        android:layout_marginBottom="8dp" />

    <EditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Nhập họ tên..." />

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="Xác nhận" />

</LinearLayout>
```

### 2.3 Ví Dụ — Chia Đôi Màn Hình Bằng `weight`

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:weightSum="2">

    <!-- Chiếm 1/2 chiều rộng -->
    <Button
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Hủy" />

    <!-- Chiếm 1/2 chiều rộng -->
    <Button
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Đồng ý" />

</LinearLayout>
```

> [!TIP]
> Khi dùng `layout_weight`, hãy đặt `layout_width="0dp"` (horizontal) hoặc `layout_height="0dp"` (vertical) để LinearLayout tính toán lại kích thước theo weight.

---

## 3. ConstraintLayout

**ConstraintLayout** định vị View bằng **các ràng buộc (constraints)** nối đến View khác hoặc cạnh của màn hình. Đây là layout mạnh mẽ nhất và được khuyến nghị cho hầu hết UI phức tạp.

### 3.1 Ưu Điểm So Với LinearLayout

| | LinearLayout | ConstraintLayout |
|---|---|---|
| Bố cục phức tạp | Cần lồng nhiều cấp | Phẳng (flat), một cấp |
| Hiệu suất | Giảm khi lồng sâu | Cao hơn do hierarchy phẳng |
| Responsive | Hạn chế | Rất linh hoạt |
| Drag & drop trong IDE | Khó | Dễ dàng |

### 3.2 Các Constraint Cơ Bản

```xml
<!-- Nối cạnh Start của View này → cạnh Start của parent -->
app:layout_constraintStart_toStartOf="parent"

<!-- Nối cạnh End của View này → cạnh End của parent -->
app:layout_constraintEnd_toEndOf="parent"

<!-- Nối cạnh Top của View này → cạnh Bottom của View khác -->
app:layout_constraintTop_toBottomOf="@id/tvTitle"

<!-- Nối cạnh Bottom của View này → cạnh Bottom của parent -->
app:layout_constraintBottom_toBottomOf="parent"
```

> [!IMPORTANT]
> Mỗi View trong ConstraintLayout **phải có ít nhất 2 constraint** (1 theo chiều ngang, 1 theo chiều dọc). Nếu thiếu, View sẽ nằm ở góc trái-trên (0,0).

### 3.3 Ví Dụ — Màn Hình Đăng Nhập

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp">

    <!-- Logo -->
    <ImageView
        android:id="@+id/ivLogo"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:src="@drawable/ic_launcher_foreground"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="48dp" />

    <!-- Tiêu đề -->
    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Đăng Nhập"
        android:textSize="28sp"
        android:textStyle="bold"
        app:layout_constraintTop_toBottomOf="@id/ivLogo"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="16dp" />

    <!-- Email -->
    <EditText
        android:id="@+id/etEmail"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:hint="Email"
        android:inputType="textEmailAddress"
        app:layout_constraintTop_toBottomOf="@id/tvTitle"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="32dp" />

    <!-- Mật khẩu -->
    <EditText
        android:id="@+id/etPassword"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:hint="Mật khẩu"
        android:inputType="textPassword"
        app:layout_constraintTop_toBottomOf="@id/etEmail"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="12dp" />

    <!-- Nút đăng nhập -->
    <Button
        android:id="@+id/btnLogin"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Đăng Nhập"
        app:layout_constraintTop_toBottomOf="@id/etPassword"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="24dp" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### 3.4 Bias — Điều Chỉnh Vị Trí

Khi View có constraint ở cả hai phía (Start + End hoặc Top + Bottom), View sẽ được căn giữa. Dùng `bias` để lệch về một phía:

```xml
<!-- Lệch 30% từ trái sang (mặc định là 0.5 = giữa) -->
app:layout_constraintHorizontal_bias="0.3"

<!-- Lệch 20% từ trên xuống -->
app:layout_constraintVertical_bias="0.2"
```

---

## 4. TextView

**TextView** dùng để **hiển thị văn bản**. Đây là View được dùng nhiều nhất.

### 4.1 Thuộc Tính Thường Dùng

| Thuộc tính | Ví dụ | Mô tả |
|-----------|-------|-------|
| `android:text` | `"Xin chào"` | Nội dung hiển thị |
| `android:textSize` | `16sp` | Cỡ chữ |
| `android:textColor` | `#FF5722` / `@color/black` | Màu chữ |
| `android:textStyle` | `bold`, `italic`, `normal` | Kiểu chữ |
| `android:gravity` | `center`, `start`, `end` | Căn chỉnh nội dung bên trong TextView |
| `android:maxLines` | `2` | Số dòng tối đa |
| `android:ellipsize` | `end`, `start`, `middle` | Hiển thị `...` khi text bị cắt |
| `android:letterSpacing` | `0.05` | Khoảng cách giữa các chữ |
| `android:lineSpacingMultiplier` | `1.5` | Khoảng cách giữa các dòng |

```xml
<TextView
    android:id="@+id/tvTitle"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Tiêu đề bài viết rất dài có thể bị cắt bớt"
    android:textSize="18sp"
    android:textStyle="bold"
    android:textColor="@color/black"
    android:maxLines="2"
    android:ellipsize="end"
    android:gravity="center" />
```

### 4.2 Dùng String Resource (Khuyến Nghị)

Thay vì hardcode text trong XML, hãy đặt trong `res/values/strings.xml`:

```xml
<!-- res/values/strings.xml -->
<resources>
    <string name="app_name">MyApp</string>
    <string name="welcome_message">Chào mừng bạn đến với Android!</string>
</resources>
```

```xml
<!-- Dùng trong layout -->
<TextView
    android:text="@string/welcome_message" />
```

> [!NOTE]
> Dùng String Resource giúp dễ dàng **hỗ trợ đa ngôn ngữ** (i18n) sau này — chỉ cần thêm file `strings.xml` cho từng ngôn ngữ.

---

## 5. EditText

**EditText** là ô nhập liệu, được dùng để nhận **dữ liệu từ người dùng**.

### 5.1 Thuộc Tính Thường Dùng

| Thuộc tính | Ví dụ | Mô tả |
|-----------|-------|-------|
| `android:hint` | `"Nhập email..."` | Text gợi ý (mờ) khi chưa nhập |
| `android:inputType` | xem bảng dưới | Loại bàn phím hiển thị |
| `android:maxLength` | `50` | Giới hạn số ký tự |
| `android:imeOptions` | `actionDone`, `actionNext` | Nút action trên bàn phím |
| `android:textColorHint` | `@color/gray` | Màu của hint text |

### 5.2 `inputType` — Loại Bàn Phím

| `inputType` | Mô tả |
|-------------|-------|
| `text` | Bàn phím chữ thông thường |
| `textPassword` | Ẩn ký tự, bàn phím chữ |
| `textEmailAddress` | Bàn phím có ký tự `@` và `.` |
| `number` | Bàn phím số |
| `phone` | Bàn phím số điện thoại |
| `textMultiLine` | Cho phép nhập nhiều dòng |

```xml
<EditText
    android:id="@+id/etUsername"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Tên đăng nhập"
    android:inputType="text"
    android:maxLength="30"
    android:imeOptions="actionNext" />

<EditText
    android:id="@+id/etPassword"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Mật khẩu"
    android:inputType="textPassword"
    android:imeOptions="actionDone" />
```



---

## 6. ImageView

**ImageView** dùng để hiển thị hình ảnh từ drawable resource hoặc URL.

### 6.1 Thuộc Tính Thường Dùng

| Thuộc tính | Giá trị | Mô tả |
|-----------|---------|-------|
| `android:src` | `@drawable/image` | Ảnh hiển thị |
| `android:scaleType` | xem bảng dưới | Cách scale ảnh trong khung |
| `android:adjustViewBounds` | `true` / `false` | Tự điều chỉnh bounds theo tỷ lệ ảnh |
| `android:contentDescription` | `"Logo ứng dụng"` | Mô tả cho accessibility |

### 6.2 `scaleType` — Cách Hiển Thị Ảnh

| `scaleType` | Mô tả |
|-------------|-------|
| `centerCrop` | Cắt ảnh để lấp đầy khung, giữ tỷ lệ (hay dùng nhất) |
| `fitCenter` | Thu nhỏ ảnh vừa khung, giữ tỷ lệ, có thể có viền trống |
| `fitXY` | Kéo dãn ảnh vừa khung, không giữ tỷ lệ |
| `center` | Ảnh ở giữa, kích thước gốc, không scale |

```xml
<ImageView
    android:id="@+id/ivAvatar"
    android:layout_width="80dp"
    android:layout_height="80dp"
    android:src="@drawable/ic_launcher_foreground"
    android:scaleType="centerCrop"
    android:contentDescription="Ảnh đại diện" />
```

### 6.3 Load Ảnh Từ URL — Thư Viện Glide

Glide là thư viện phổ biến nhất để load ảnh từ Internet:

```kotlin
// build.gradle (app)
implementation("com.github.bumptech.glide:glide:4.16.0")
```

```kotlin
// Trong Activity/Fragment
Glide.with(this)
    .load("https://picsum.photos/200")
    .placeholder(R.drawable.ic_placeholder)  // Ảnh hiển thị khi đang tải
    .error(R.drawable.ic_error)              // Ảnh hiển thị khi lỗi
    .centerCrop()
    .into(ivAvatar)
```

---

## 7. FrameLayout

**FrameLayout** xếp chồng các View lên nhau theo **thứ tự trong XML** (View sau ở trên View trước). Thường dùng cho:
- Overlay (hiển thị badge, nhãn trên ảnh)
- Container cho Fragment
- Loading overlay

### 7.1 Thuộc Tính

FrameLayout ít thuộc tính riêng. View con dùng `layout_gravity` để định vị:

| `layout_gravity` | Vị trí |
|-----------------|--------|
| `top\|start` | Trên trái (mặc định) |
| `center` | Giữa |
| `bottom\|end` | Dưới phải |
| `bottom\|center_horizontal` | Giữa phía dưới |

### 7.2 Ví Dụ — Ảnh Có Badge

```xml
<FrameLayout
    android:layout_width="80dp"
    android:layout_height="80dp">

    <!-- Ảnh đại diện ở dưới -->
    <ImageView
        android:layout_width="80dp"
        android:layout_height="80dp"
        android:src="@drawable/avatar"
        android:scaleType="centerCrop" />

    <!-- Badge số thông báo ở góc trên phải -->
    <TextView
        android:layout_width="20dp"
        android:layout_height="20dp"
        android:text="3"
        android:textColor="#FFFFFF"
        android:textSize="10sp"
        android:gravity="center"
        android:background="@drawable/bg_badge_red"
        android:layout_gravity="top|end" />

</FrameLayout>
```

### 7.3 Ví Dụ — Loading Overlay

```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Nội dung chính -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">
        <!-- ... -->
    </LinearLayout>

    <!-- Overlay loading — hiển thị khi đang tải dữ liệu -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />

</FrameLayout>
```

---

## 8. Button

**Button** kế thừa từ `TextView` — vì vậy nó hỗ trợ hầu hết thuộc tính của TextView.

### 8.1 Các Loại Button

| Loại | Class | Mô tả |
|------|-------|-------|
| Filled Button | `<Button>` | Nền đặc (mặc định) — hành động chính |
| Outlined Button | `<com.google.android.material.button.MaterialButton` style `@style/Widget.Material3.Button.OutlinedButton` | Viền, không nền — hành động phụ |
| Text Button | style `@style/Widget.Material3.Button.TextButton` | Chỉ text — hành động ít quan trọng |
| Icon Button | `<com.google.android.material.button.MaterialButton` với `app:icon` | Button có icon |

```xml
<!-- Filled Button (mặc định) -->
<Button
    android:id="@+id/btnLogin"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Đăng Nhập" />

<!-- Outlined Button -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnRegister"
    style="@style/Widget.Material3.Button.OutlinedButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Đăng Ký" />

<!-- Text Button -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnForgotPassword"
    style="@style/Widget.Material3.Button.TextButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Quên mật khẩu?" />
```

---

## 9. Background Drawable

Drawable là tài nguyên đồ họa có thể vẽ lên View. Được đặt trong thư mục `res/drawable/`.

### 9.1 Các Loại Drawable Thường Dùng

| Loại | File | Mô tả |
|------|------|-------|
| Shape Drawable | `*.xml` | Vẽ hình học: hình chữ nhật, oval, đường thẳng |
| Selector Drawable | `*.xml` | Thay đổi hình theo trạng thái (pressed, enabled...) |
| PNG/WebP/SVG | `*.png`, `*.webp`, `*.xml` | Hình ảnh thực |
| Layer List | `*.xml` | Nhiều drawable xếp chồng |

### 9.2 Shape Drawable — Tạo Hình Nền Tùy Chỉnh

```xml
<!-- res/drawable/bg_button_primary.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <!-- Màu nền -->
    <solid android:color="#6200EE" />

    <!-- Bo góc -->
    <corners android:radius="12dp" />

    <!-- Viền -->
    <stroke
        android:width="2dp"
        android:color="#3700B3" />

    <!-- Padding bên trong shape (khác với android:padding của View) -->
    <padding
        android:left="16dp"
        android:top="12dp"
        android:right="16dp"
        android:bottom="12dp" />

</shape>
```

#### Thuộc tính `android:shape`

| Giá trị | Hình dạng |
|---------|-----------|
| `rectangle` | Hình chữ nhật (mặc định) |
| `oval` | Hình ellipse / tròn |
| `line` | Đường thẳng ngang |
| `ring` | Hình nhẫn / vòng tròn rỗng |

```xml
<!-- Hình tròn -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#6200EE" />
</shape>
```

#### Bo Góc Chi Tiết

```xml
<corners
    android:topLeftRadius="16dp"
    android:topRightRadius="16dp"
    android:bottomLeftRadius="0dp"
    android:bottomRightRadius="0dp" />
```

### 9.3 Gradient — Màu Chuyển Sắc

```xml
<!-- res/drawable/bg_gradient_header.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient
        android:type="linear"
        android:angle="135"
        android:startColor="#6200EE"
        android:endColor="#03DAC5" />
    <corners android:radius="16dp" />
</shape>
```

| `android:type` | Kiểu gradient |
|----------------|--------------|
| `linear` | Tuyến tính (theo góc `angle`) |
| `radial` | Tỏa tròn từ tâm |
| `sweep` | Xoay theo vòng tròn |

### 9.4 Selector Drawable — Đổi Style Theo Trạng Thái

Selector cho phép thay đổi giao diện View tùy theo trạng thái người dùng tương tác:

```xml
<!-- res/drawable/selector_button.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Khi đang nhấn -->
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="#3700B3" />
            <corners android:radius="12dp" />
        </shape>
    </item>

    <!-- Khi bị disabled -->
    <item android:state_enabled="false">
        <shape android:shape="rectangle">
            <solid android:color="#CCCCCC" />
            <corners android:radius="12dp" />
        </shape>
    </item>

    <!-- Mặc định (phải để cuối cùng, không có state) -->
    <item>
        <shape android:shape="rectangle">
            <solid android:color="#6200EE" />
            <corners android:radius="12dp" />
        </shape>
    </item>

</selector>
```

```xml
<!-- Áp dụng vào View -->
<Button
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@drawable/selector_button"
    android:text="Nhấn vào tôi" />
```

> [!IMPORTANT]
> Thứ tự item trong Selector **rất quan trọng**. Android duyệt từ **trên xuống dưới** và chọn item đầu tiên khớp với trạng thái hiện tại. Item không có `state_*` luôn đặt **cuối cùng** làm mặc định.

### 9.5 Dùng Color Resource

Thay vì hardcode màu trong drawable, hãy dùng color resource:

```xml
<!-- res/values/colors.xml -->
<resources>
    <color name="colorPrimary">#6200EE</color>
    <color name="colorPrimaryDark">#3700B3</color>
    <color name="colorAccent">#03DAC5</color>
    <color name="colorGray">#CCCCCC</color>
</resources>
```

```xml
<!-- Trong drawable -->
<solid android:color="@color/colorPrimary" />
```

### 9.6 Áp Dụng Background Lên View

```xml
<!-- Dùng file drawable -->
<View android:background="@drawable/bg_button_primary" />

<!-- Dùng màu trực tiếp -->
<View android:background="@color/colorPrimary" />

<!-- Bo góc nhanh (API 31+) -->
<View
    android:background="@color/colorPrimary"
    android:clipToOutline="true" />
```

```kotlin
// Trong Kotlin
view.setBackgroundResource(R.drawable.bg_button_primary)
view.setBackgroundColor(Color.parseColor("#6200EE"))
```

---

## 10. Giới Thiệu Material Design

**Material Design** là hệ thống thiết kế do Google phát triển. Nó định nghĩa các nguyên tắc về màu sắc, typography, component, và animation để tạo ra UI nhất quán, đẹp và dễ dùng.

### 10.1 Tại Sao Dùng Material?

- ✅ Component đẹp sẵn, không cần tự làm drawable phức tạp
- ✅ Hỗ trợ Dark Mode tự động
- ✅ Theo chuẩn UX của Google — người dùng Android đã quen
- ✅ Tích hợp sẵn trong Android Jetpack

### 10.2 Thêm Material Design Vào Project

```kotlin
// build.gradle (app)
implementation("com.google.android.material:material:1.12.0")
```

```xml
<!-- res/values/themes.xml — đổi theme sang Material3 -->
<style name="Theme.MyApp" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorOnPrimary">@color/white</item>
    <item name="colorSecondary">@color/colorAccent</item>
</style>
```

### 10.3 Một Số Component Material Hay Dùng

| Component | Class | Dùng khi |
|-----------|-------|---------|
| **MaterialButton** | `com.google.android.material.button.MaterialButton` | Thay thế `Button` thông thường |
| **TextInputLayout** | `com.google.android.material.textfield.TextInputLayout` | Bọc EditText — có floating label, error |
| **MaterialCardView** | `com.google.android.material.card.MaterialCardView` | Card có shadow, corner radius |
| **Snackbar** | `Snackbar` | Thông báo ngắn ở đáy màn hình |
| **MaterialToolbar** | `com.google.android.material.appbar.MaterialToolbar` | Thanh tiêu đề ứng dụng |

### 10.4 TextInputLayout — EditText Đẹp Hơn

```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Email"
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox">

    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="textEmailAddress" />

</com.google.android.material.textfield.TextInputLayout>
```

```kotlin
// Hiện thông báo lỗi
val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
tilEmail.error = "Email không hợp lệ"
tilEmail.error = null  // Xóa lỗi
```

### 10.5 MaterialCardView

```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp"
    app:strokeColor="@color/colorPrimary"
    app:strokeWidth="1dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Tiêu đề Card"
            android:textSize="16sp"
            android:textStyle="bold" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Nội dung mô tả của card..."
            android:layout_marginTop="4dp" />

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

---
