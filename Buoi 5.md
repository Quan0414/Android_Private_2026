# 📱 Buổi 5 — ListView & RecyclerView

---

## 1. Tổng Quan — Tại Sao Cần List View?

Trong ứng dụng di động, danh sách là thành phần xuất hiện ở hầu hết mọi nơi: danh sách sản phẩm, tin nhắn, bài đăng, liên hệ... Android cung cấp hai cách chính để hiển thị danh sách:

| | **ListView** | **RecyclerView** |
|---|---|---|
| Xuất hiện từ | API 1 (rất lâu đời) | API 21 (Android 5.0) |
| Hiệu năng | Trung bình | ⚡ Cao hơn (tái sử dụng View) |
| Tính linh hoạt | Giới hạn | Rất cao (nhiều layout) |
| Animation | Không có sẵn | Có sẵn |
| Khuyến nghị | Dự án cũ / học cơ bản | ✅ Dùng cho dự án thật |

---

## 2. ListView

### 2.1 ListView Là Gì?

**ListView** là một ViewGroup hiển thị danh sách các item theo chiều dọc, có khả năng cuộn. Mỗi item trong danh sách được tạo ra bởi một **Adapter**.

```
ListView
├── Item 0  ← Adapter tạo ra View này
├── Item 1
├── Item 2
├── ...
└── Item N
```

### 2.2 Các Thành Phần

```
┌──────────────────────────────────────────┐
│              Data (List<T>)              │  ← Dữ liệu thô: List<String>, List<User>...
└──────────────────┬───────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────┐
│              Adapter                     │  ← Cầu nối: chuyển Data thành View
└──────────────────┬───────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────┐
│              ListView                    │  ← Hiển thị danh sách lên màn hình
└──────────────────────────────────────────┘
```

### 2.3 Ví Dụ Đơn Giản — ArrayAdapter

**Bước 1: Thêm ListView vào XML**

```xml
<!-- activity_main.xml -->
<ListView
    android:id="@+id/lvFruits"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

**Bước 2: Dùng ArrayAdapter trong Activity**

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fruits = listOf("Táo", "Chuối", "Xoài", "Cam", "Dưa hấu")

        // ArrayAdapter dựng sẵn — dùng cho danh sách text đơn giản
        val adapter = ArrayAdapter(
            this,                           // Context
            android.R.layout.simple_list_item_1,  // Layout mặc định của Android
            fruits                          // Dữ liệu
        )

        val lvFruits = findViewById<ListView>(R.id.lvFruits)
        lvFruits.adapter = adapter
    }
}
```

> [!TIP]
> `android.R.layout.simple_list_item_1` là layout có sẵn của Android, chỉ hiển thị một dòng text. Dùng khi chỉ cần danh sách text đơn giản.

### 2.4 Custom Adapter — Adapter Tuỳ Chỉnh

Khi mỗi item cần hiển thị nhiều thông tin hơn (ảnh + tên + mô tả), ta cần tự tạo Adapter.

**Bước 1: Tạo Data Class**

```kotlin
data class Product(
    val name: String,
    val price: String,
    val imageRes: Int  // Resource ID của ảnh
)
```

**Bước 2: Tạo Layout Cho Item**

```xml
<!-- res/layout/item_product.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="12dp">

    <ImageView
        android:id="@+id/ivProductImage"
        android:layout_width="60dp"
        android:layout_height="60dp"
        android:scaleType="centerCrop" />

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginStart="12dp"
        android:orientation="vertical">

        <TextView
            android:id="@+id/tvProductName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="16sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvProductPrice"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            android:textColor="#E53935" />
    </LinearLayout>
</LinearLayout>
```

**Bước 3: Tạo Custom Adapter**

```kotlin
class ProductAdapter(
    context: Context,
    private val products: List<Product>
) : BaseAdapter() {            // Kế thừa BaseAdapter

    private val inflater = LayoutInflater.from(context)

    // Trả về số lượng item
    override fun getCount(): Int = products.size

    // Trả về item tại vị trí position
    override fun getItem(position: Int): Any = products[position]

    // Trả về ID của item (thường dùng position)
    override fun getItemId(position: Int): Long = position.toLong()

    // 🔑 Hàm quan trọng nhất: tạo và trả về View cho mỗi item
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // convertView: View cũ có thể tái sử dụng (null nếu chưa có)
        val view = convertView ?: inflater.inflate(R.layout.item_product, parent, false)

        val product = products[position]

        val ivImage = view.findViewById<ImageView>(R.id.ivProductImage)
        val tvName  = view.findViewById<TextView>(R.id.tvProductName)
        val tvPrice = view.findViewById<TextView>(R.id.tvProductPrice)

        ivImage.setImageResource(product.imageRes)
        tvName.text  = product.name
        tvPrice.text = product.price

        return view
    }
}
```

**Bước 4: Gán Adapter Vào ListView**

```kotlin
val products = listOf(
    Product("iPhone 15", "25.990.000đ", R.drawable.ic_phone),
    Product("Galaxy S24", "22.990.000đ", R.drawable.ic_phone),
    Product("Pixel 8", "18.990.000đ", R.drawable.ic_phone)
)

val adapter = ProductAdapter(this, products)
lvProducts.adapter = adapter
```

### 2.5 Xử Lý Sự Kiện Click Item

```kotlin
lvProducts.setOnItemClickListener { parent, view, position, id ->
    val selectedProduct = products[position]
    Toast.makeText(this, "Chọn: ${selectedProduct.name}", Toast.LENGTH_SHORT).show()

    // Chuyển màn chi tiết
    val intent = Intent(this, DetailActivity::class.java)
    intent.putExtra("PRODUCT_NAME", selectedProduct.name)
    startActivity(intent)
}

// Long click
lvProducts.setOnItemLongClickListener { parent, view, position, id ->
    Toast.makeText(this, "Giữ: ${products[position].name}", Toast.LENGTH_SHORT).show()
    true
}
```

### 2.6 ViewHolder Pattern — Tối Ưu ListView

Vấn đề: mỗi lần `getView()` được gọi, `findViewById()` phải duyệt cây View lại từ đầu — **rất chậm** khi có nhiều item.

**Giải pháp: ViewHolder Pattern** — lưu tham chiếu View vào tag để tái sử dụng.

```kotlin
override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val holder: ViewHolder
    val view: View

    if (convertView == null) {
        // Lần đầu: inflate view và tạo holder mới
        view = inflater.inflate(R.layout.item_product, parent, false)
        holder = ViewHolder(
            ivImage = view.findViewById(R.id.ivProductImage),
            tvName  = view.findViewById(R.id.tvProductName),
            tvPrice = view.findViewById(R.id.tvProductPrice)
        )
        view.tag = holder  // Gắn holder vào view
    } else {
        // Lần sau: lấy holder đã lưu — không cần findViewById nữa
        view = convertView
        holder = convertView.tag as ViewHolder
    }

    val product = products[position]
    holder.ivImage.setImageResource(product.imageRes)
    holder.tvName.text  = product.name
    holder.tvPrice.text = product.price

    return view
}

// Data class đơn giản giữ tham chiếu View
private data class ViewHolder(
    val ivImage: ImageView,
    val tvName: TextView,
    val tvPrice: TextView
)
```

> [!IMPORTANT]
> RecyclerView được thiết kế tích hợp sẵn ViewHolder Pattern, buộc lập trình viên phải dùng — đó là lý do RecyclerView hiệu quả hơn ListView.

---

## 3. RecyclerView

### 3.1 RecyclerView Là Gì?

**RecyclerView** là thành phần hiển thị danh sách thế hệ mới, được thiết kế để **tái sử dụng (recycle)** các View item nhằm tối ưu hiệu năng. Thay vì tạo số lượng View bằng số item (có thể hàng nghìn), RecyclerView chỉ tạo số View vừa đủ để hiển thị trên màn hình, và **tái sử dụng** chúng khi người dùng cuộn.

```
Màn hình (chỉ hiển thị 7 item)
┌─────────────────────────────┐
│  [View A] Item 0            │
│  [View B] Item 1            │
│  [View C] Item 2            │
│  [View D] Item 3            │
│  [View E] Item 4            │
│  [View F] Item 5            │
│  [View G] Item 6            │
└─────────────────────────────┘
         ↓ Người dùng cuộn xuống
[View A] được tái sử dụng → hiển thị Item 7
[View B] được tái sử dụng → hiển thị Item 8
```

### 3.2 Các Thành Phần Của RecyclerView

```
RecyclerView
├── LayoutManager     ← Quyết định cách sắp xếp item (dọc, ngang, lưới...)
├── Adapter           ← Cầu nối: chuyển Data thành ViewHolder
│   └── ViewHolder    ← Giữ tham chiếu View của một item
└── ItemDecoration    ← Đường kẻ ngăn cách, padding... (tuỳ chọn)
```

### 3.3 Thêm Dependency

```kotlin
// build.gradle (app)
dependencies {
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
```

### 3.4 Xây Dựng RecyclerView — Từng Bước

#### Bước 1: Thêm RecyclerView Vào XML Layout

```xml
<!-- activity_main.xml -->
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rvProducts"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

#### Bước 2: Tạo Layout Cho Item

```xml
<!-- res/layout/item_product_rv.xml -->
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp">

        <ImageView
            android:id="@+id/ivImage"
            android:layout_width="72dp"
            android:layout_height="72dp"
            android:scaleType="centerCrop" />

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="12dp"
            android:orientation="vertical"
            android:gravity="center_vertical">

            <TextView
                android:id="@+id/tvName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textSize="16sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/tvPrice"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textSize="14sp"
                android:textColor="#E53935"
                android:layout_marginTop="4dp" />
        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

#### Bước 3: Tạo Adapter Cho RecyclerView

```kotlin
class ProductAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit   // Lambda xử lý click từ bên ngoài
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    // 1. ViewHolder — giữ tham chiếu View
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        val tvName: TextView   = itemView.findViewById(R.id.tvName)
        val tvPrice: TextView  = itemView.findViewById(R.id.tvPrice)
    }

    // 2. Tạo ViewHolder mới — chỉ gọi khi chưa có View để tái sử dụng
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_rv, parent, false)
        return ViewHolder(view)
    }

    // 3. Gán dữ liệu vào ViewHolder — gọi mỗi khi cần hiển thị item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.tvName.text  = product.name
        holder.tvPrice.text = product.price
        holder.ivImage.setImageResource(product.imageRes)

        // Xử lý click
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    // 4. Tổng số item
    override fun getItemCount(): Int = products.size
}
```

#### Bước 4: Thiết Lập RecyclerView Trong Activity

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val products = listOf(
            Product("iPhone 15", "25.990.000đ", R.drawable.ic_phone),
            Product("Galaxy S24", "22.990.000đ", R.drawable.ic_phone),
            Product("Pixel 8", "18.990.000đ", R.drawable.ic_phone)
        )

        val adapter = ProductAdapter(products) { product ->
            // Xử lý khi người dùng click
            Toast.makeText(this, "Chọn: ${product.name}", Toast.LENGTH_SHORT).show()
        }

        val rvProducts = findViewById<RecyclerView>(R.id.rvProducts)
        rvProducts.layoutManager = LinearLayoutManager(this)   // Danh sách dọc
        rvProducts.adapter = adapter
    }
}
```

### 3.5 LayoutManager — Chọn Kiểu Hiển Thị

LayoutManager quyết định cách các item được sắp xếp trong RecyclerView.

```kotlin
// Danh sách dọc (mặc định)
rvProducts.layoutManager = LinearLayoutManager(this)

// Danh sách ngang
rvProducts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

// Lưới 2 cột
rvProducts.layoutManager = GridLayoutManager(this, 2)

// Lưới 3 cột
rvProducts.layoutManager = GridLayoutManager(this, 3)

// Lưới so le (như Pinterest)
rvProducts.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
```

> [!TIP]
> Tham số thứ 3 của `LinearLayoutManager` là `reverseLayout`. Truyền `true` để đảo ngược thứ tự (item mới nhất lên đầu).

### 3.6 Cập Nhật Dữ Liệu — DiffUtil & notifyDataSetChanged

#### Cách cơ bản: `notifyDataSetChanged()`

```kotlin
class ProductAdapter(
    private val products: MutableList<Product>,
    ...
) : RecyclerView.Adapter<...>() {

    fun addProduct(product: Product) {
        products.add(product)
        notifyItemInserted(products.size - 1)  // Animation đẹp hơn notifyDataSetChanged
    }

    fun removeProduct(position: Int) {
        products.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateAll(newList: List<Product>) {
        products.clear()
        products.addAll(newList)
        notifyDataSetChanged()  // Tải lại toàn bộ — không có animation
    }
}
```

**Các hàm notify quan trọng:**

| Hàm | Tác dụng |
|-----|---------|
| `notifyDataSetChanged()` | Tải lại toàn bộ danh sách (không animation) |
| `notifyItemInserted(pos)` | Thêm item mới tại `pos` với animation |
| `notifyItemRemoved(pos)` | Xoá item tại `pos` với animation |
| `notifyItemChanged(pos)` | Cập nhật item tại `pos` |
| `notifyItemRangeChanged(start, count)` | Cập nhật nhiều item liên tiếp |

#### Cách tốt hơn: `ListAdapter` + `DiffUtil`

**DiffUtil** tự động tính toán sự khác biệt giữa danh sách cũ và mới, chỉ cập nhật những item thay đổi — hiệu quả và có animation đẹp.

```kotlin
// Bước 1: Tạo DiffUtil.ItemCallback
class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {

    // Hai item có phải là cùng một đối tượng không? (so sánh ID)
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    // Nội dung của hai item có giống nhau không?
    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem  // data class tự so sánh tất cả field
    }
}

// Bước 2: Kế thừa ListAdapter thay vì RecyclerView.Adapter
class ProductAdapter(
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ViewHolder>(ProductDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView  = itemView.findViewById(R.id.tvName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_rv, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = getItem(position)  // Dùng getItem() thay vì list[position]
        holder.tvName.text  = product.name
        holder.tvPrice.text = product.price
        holder.itemView.setOnClickListener { onItemClick(product) }
    }
}

// Bước 3: Cập nhật dữ liệu với submitList()
adapter.submitList(newProducts)  // DiffUtil tự tính toán, UI tự cập nhật
```

> [!IMPORTANT]
> Khi dùng `ListAdapter`, **không cần** gọi `notify*()` thủ công. Chỉ cần gọi `submitList(newList)`.

### 3.7 ItemDecoration — Thêm Khoảng Cách Giữa Các Item

```kotlin
// Cách 1: Thêm divider mặc định
rvProducts.addItemDecoration(
    DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
)

// Cách 2: Tự tạo ItemDecoration với khoảng cách tuỳ chỉnh
class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = space  // Thêm khoảng cách phía dưới mỗi item
        outRect.left   = space
        outRect.right  = space
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = space  // Chỉ thêm top cho item đầu tiên
        }
    }
}

// Sử dụng
rvProducts.addItemDecoration(SpaceItemDecoration(16))
```

### 3.8 Swipe To Delete — Vuốt Để Xoá

```kotlin
val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
    0,                                          // dragDirs: không cho kéo thả
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT  // swipeDirs: vuốt trái/phải
) {
    override fun onMove(...) = false  // Không dùng kéo thả

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.removeAt(position)  // Xoá item khỏi adapter
    }
})

itemTouchHelper.attachToRecyclerView(rvProducts)
```

### 3.9 Multiple ViewType — Nhiều Kiểu Item

Dùng khi danh sách có nhiều loại item khác nhau (ví dụ: header + item thường).

```kotlin
class MixedAdapter(private val items: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM   = 1
    }

    // Xác định loại item tại position
    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_product_rv, parent, false)
                ProductViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder  -> holder.bind(items[position] as String)
            is ProductViewHolder -> holder.bind(items[position] as Product)
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvHeader = view.findViewById<TextView>(R.id.tvHeader)
        fun bind(title: String) { tvHeader.text = title }
    }

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName = view.findViewById<TextView>(R.id.tvName)
        fun bind(product: Product) { tvName.text = product.name }
    }
}
```

---


## 4. Tổng Kết

| Chủ đề | Điểm quan trọng |
|--------|----------------|
| **Adapter** | Cầu nối giữa dữ liệu và View — bắt buộc cho cả ListView và RecyclerView |
| **ViewHolder** | Lưu tham chiếu View để tránh `findViewById()` nhiều lần — tối ưu hiệu năng |
| **ListView** | Đơn giản, phù hợp học cơ bản; dùng ViewHolder Pattern khi tối ưu |
| **RecyclerView** | Chuẩn thực tế; bắt buộc có `LayoutManager` và `Adapter extends RecyclerView.Adapter` |
| **LayoutManager** | `LinearLayoutManager` (dọc/ngang), `GridLayoutManager` (lưới) |
| **notifyDataSetChanged** | Dùng khi cập nhật toàn bộ; ưu tiên `notifyItemInserted/Removed` cho animation |
| **ListAdapter + DiffUtil** | Cách cập nhật chuẩn nhất — tự động so sánh và chỉ render phần thay đổi |
| **ItemTouchHelper** | Thêm swipe/drag dễ dàng vào RecyclerView |
