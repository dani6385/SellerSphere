package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Product
import com.example.data.model.SaleItem
import com.example.data.model.SaleTransaction
import com.example.data.model.SalesTarget
import com.example.data.repository.AppRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    // --- Shopsphere Orders Tracking ---
    private val _shopsphereOrders = MutableStateFlow<List<ShopsphereOrder>>(emptyList())
    val shopsphereOrders: StateFlow<List<ShopsphereOrder>> = _shopsphereOrders.asStateFlow()

    // --- Buyer Chats Tracking ---
    private val _buyerChats = MutableStateFlow<List<BuyerChat>>(emptyList())
    val buyerChats: StateFlow<List<BuyerChat>> = _buyerChats.asStateFlow()

    val activeChatBuyerName = MutableStateFlow<String?>(null)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(
            database.productDao(),
            database.transactionDao(),
            database.targetDao()
        )
        // Initialize dummy data if empty to show beautiful analytics
        seedInitialData()
        initShopsphereOrders()
        initBuyerChats()
    }

    // --- State Observables ---
    val products: StateFlow<List<Product>> = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions: StateFlow<List<SaleTransaction>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSaleItems: StateFlow<List<SaleItem>> = repository.allSaleItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Today's Date String format YYYY-MM-DD
    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Sales Target Flow
    private val _todayTarget = MutableStateFlow<SalesTarget?>(null)
    val todayTarget: StateFlow<SalesTarget?> = _todayTarget.asStateFlow()

    // --- Theme Settings ---
    private val prefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    private val _isDarkTheme = MutableStateFlow<Boolean>(prefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        val newVal = !_isDarkTheme.value
        _isDarkTheme.value = newVal
        prefs.edit().putBoolean("is_dark_theme", newVal).apply()
    }

    // --- Default Payment Method ---
    private val _defaultPaymentMethod = MutableStateFlow(prefs.getString("default_payment_method", "Tunai") ?: "Tunai")
    val defaultPaymentMethod: StateFlow<String> = _defaultPaymentMethod.asStateFlow()

    fun updateDefaultPaymentMethod(method: String) {
        _defaultPaymentMethod.value = method
        prefs.edit().putString("default_payment_method", method).apply()
        addSyncLog("Metode pembayaran default diubah ke: $method")
    }

    // --- Safe Mode Search / Age Filtering ---
    private val _isSafeModeEnabled = MutableStateFlow(prefs.getBoolean("is_safe_mode_enabled", false))
    val isSafeModeEnabled: StateFlow<Boolean> = _isSafeModeEnabled.asStateFlow()

    private val _safeModeAgeLimit = MutableStateFlow(prefs.getInt("safe_mode_age_limit", 13))
    val safeModeAgeLimit: StateFlow<Int> = _safeModeAgeLimit.asStateFlow()

    fun toggleSafeMode(enabled: Boolean) {
        _isSafeModeEnabled.value = enabled
        prefs.edit().putBoolean("is_safe_mode_enabled", enabled).apply()
        addSyncLog("Safe Mode pencarian diubah ke: ${if (enabled) "Aktif" else "Nonaktif"}")
    }

    fun updateSafeModeAgeLimit(age: Int) {
        _safeModeAgeLimit.value = age
        prefs.edit().putInt("safe_mode_age_limit", age).apply()
        addSyncLog("Batas usia Safe Mode diatur ke: $age tahun")
    }

    // --- Firebase Realtime Database Configuration ---
    private val _rtdbUrl = MutableStateFlow(
        prefs.getString("firebase_rtdb_url", "https://matrixsphere-c3de9-default-rtdb.asia-southeast1.firebasedatabase.app") 
            ?: "https://matrixsphere-c3de9-default-rtdb.asia-southeast1.firebasedatabase.app"
    )
    val rtdbUrl = _rtdbUrl.asStateFlow()

    private val _customStoreName = MutableStateFlow(
        prefs.getString("custom_store_name", "SS Seller Sphere") ?: "SS Seller Sphere"
    )
    val customStoreName: StateFlow<String> = _customStoreName.asStateFlow()

    private val _ownerName = MutableStateFlow(
        prefs.getString("owner_name", "Dani") ?: "Dani"
    )
    val ownerName: StateFlow<String> = _ownerName.asStateFlow()

    private val _ownerEmail = MutableStateFlow(
        prefs.getString("owner_email", "dani6385@gmail.com") ?: "dani6385@gmail.com"
    )
    val ownerEmail: StateFlow<String> = _ownerEmail.asStateFlow()

    private val _pickupAddress = MutableStateFlow(
        prefs.getString("pickup_address", "Jl. Kebon Jeruk No. 88, Jakarta Barat") ?: "Jl. Kebon Jeruk No. 88, Jakarta Barat"
    )
    val pickupAddress: StateFlow<String> = _pickupAddress.asStateFlow()

    private val _pickupLatitude = MutableStateFlow(
        prefs.getFloat("pickup_latitude", -6.1751f)
    )
    val pickupLatitude: StateFlow<Float> = _pickupLatitude.asStateFlow()

    private val _pickupLongitude = MutableStateFlow(
        prefs.getFloat("pickup_longitude", 106.8272f)
    )
    val pickupLongitude: StateFlow<Float> = _pickupLongitude.asStateFlow()

    private val _pickupNotes = MutableStateFlow(
        prefs.getString("pickup_notes", "Pagar hitam, di depan minimarket") ?: "Pagar hitam, di depan minimarket"
    )
    val pickupNotes: StateFlow<String> = _pickupNotes.asStateFlow()

    // --- Notification Preferences for Email, WA, and SMS ---
    private val _ownerWhatsapp = MutableStateFlow(
        prefs.getString("owner_whatsapp", "081234567890") ?: "081234567890"
    )
    val ownerWhatsapp: StateFlow<String> = _ownerWhatsapp.asStateFlow()

    private val _ownerSms = MutableStateFlow(
        prefs.getString("owner_sms", "081234567890") ?: "081234567890"
    )
    val ownerSms: StateFlow<String> = _ownerSms.asStateFlow()

    private val _isEmailNotificationEnabled = MutableStateFlow(
        prefs.getBoolean("is_email_notification_enabled", true)
    )
    val isEmailNotificationEnabled: StateFlow<Boolean> = _isEmailNotificationEnabled.asStateFlow()

    private val _isWhatsappNotificationEnabled = MutableStateFlow(
        prefs.getBoolean("is_whatsapp_notification_enabled", true)
    )
    val isWhatsappNotificationEnabled: StateFlow<Boolean> = _isWhatsappNotificationEnabled.asStateFlow()

    private val _isSmsNotificationEnabled = MutableStateFlow(
        prefs.getBoolean("is_sms_notification_enabled", true)
    )
    val isSmsNotificationEnabled: StateFlow<Boolean> = _isSmsNotificationEnabled.asStateFlow()

    fun updateNotificationPreferences(
        whatsapp: String,
        sms: String,
        emailEnabled: Boolean,
        whatsappEnabled: Boolean,
        smsEnabled: Boolean
    ) {
        _ownerWhatsapp.value = whatsapp
        _ownerSms.value = sms
        _isEmailNotificationEnabled.value = emailEnabled
        _isWhatsappNotificationEnabled.value = whatsappEnabled
        _isSmsNotificationEnabled.value = smsEnabled

        prefs.edit()
            .putString("owner_whatsapp", whatsapp)
            .putString("owner_sms", sms)
            .putBoolean("is_email_notification_enabled", emailEnabled)
            .putBoolean("is_whatsapp_notification_enabled", whatsappEnabled)
            .putBoolean("is_sms_notification_enabled", smsEnabled)
            .apply()

        addSyncLog("Preferensi Notifikasi (Email, WA, SMS) diperbarui.")
    }

    val sanitizedStoreName: String
        get() = _customStoreName.value.trim().lowercase()
            .replace(Regex("[^a-z0-9_-]"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
            .ifBlank { "unknown-store" }

    private val _sellerSphereNode = MutableStateFlow(
        (_customStoreName.value.trim().lowercase()
            .replace(Regex("[^a-z0-9_-]"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
            .ifBlank { "unknown-store" })
    )
    val sellerSphereNode = _sellerSphereNode.asStateFlow()

    private val _shopSphereNode = MutableStateFlow(
        (_customStoreName.value.trim().lowercase()
            .replace(Regex("[^a-z0-9_-]"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
            .ifBlank { "unknown-store" })
    )
    val shopSphereNode = _shopSphereNode.asStateFlow()

    fun updateRtdbUrl(url: String) {
        var cleanUrl = url.trim()
        if (cleanUrl.endsWith("/")) {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length - 1)
        }
        _rtdbUrl.value = cleanUrl
        prefs.edit().putString("firebase_rtdb_url", cleanUrl).apply()
        addSyncLog("URL RTDB diperbarui ke: $cleanUrl")
        fetchOrdersFromRtdb()
    }

    fun updateSellerSphereNode(node: String) {
        val cleanNode = node.trim().lowercase()
        _sellerSphereNode.value = cleanNode
        prefs.edit().putString("seller_sphere_node", cleanNode).apply()
        addSyncLog("Node Seller Sphere diperbarui ke: $cleanNode")
    }

    fun updateShopSphereNode(node: String) {
        val cleanNode = node.trim().lowercase()
        _shopSphereNode.value = cleanNode
        prefs.edit().putString("shop_sphere_node", cleanNode).apply()
        addSyncLog("Node Shop Sphere diperbarui ke: $cleanNode")
        fetchOrdersFromRtdb()
    }

    // OkHttp Client & Moshi for REST operations
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val productListAdapter = moshi.adapter<List<Product>>(
        Types.newParameterizedType(List::class.java, Product::class.java)
    )
    private val transactionListAdapter = moshi.adapter<List<SaleTransaction>>(
        Types.newParameterizedType(List::class.java, SaleTransaction::class.java)
    )
    private val saleItemListAdapter = moshi.adapter<List<SaleItem>>(
        Types.newParameterizedType(List::class.java, SaleItem::class.java)
    )
    private val targetListAdapter = moshi.adapter<List<SalesTarget>>(
        Types.newParameterizedType(List::class.java, SalesTarget::class.java)
    )
    private val orderListAdapter = moshi.adapter<List<ShopsphereOrder>>(
        Types.newParameterizedType(List::class.java, ShopsphereOrder::class.java)
    )

    fun loadTodayTarget() {
        viewModelScope.launch {
            repository.getTargetForDate(getTodayDateString()).collect { target ->
                if (target == null) {
                    // Set default target of 1,000,000 IDR if not set
                    _todayTarget.value = SalesTarget(getTodayDateString(), 1000000.0)
                } else {
                    _todayTarget.value = target
                }
            }
        }
    }

    fun updateTodayTarget(amount: Double) {
        viewModelScope.launch {
            val target = SalesTarget(getTodayDateString(), amount)
            repository.insertTarget(target)
            _todayTarget.value = target
            triggerNotification("Target Penjualan Diperbarui", "Target hari ini diatur sebesar ${formatRupiah(amount)}")
        }
    }

    // --- Point of Sale (POS) Cart State ---
    private val _cart = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val cart: StateFlow<Map<Product, Int>> = _cart.asStateFlow()

    fun addToCart(product: Product) {
        if (product.stock <= 0) {
            triggerNotification("Stok Habis", "Produk ${product.name} tidak memiliki stok tersisa.")
            return
        }
        val currentMap = _cart.value.toMutableMap()
        val currentQty = currentMap[product] ?: 0
        if (currentQty + 1 > product.stock) {
            triggerNotification("Stok Tidak Mencukupi", "Hanya tersedia ${product.stock} unit untuk ${product.name}.")
            return
        }
        currentMap[product] = currentQty + 1
        _cart.value = currentMap
    }

    fun removeFromCart(product: Product) {
        val currentMap = _cart.value.toMutableMap()
        val currentQty = currentMap[product] ?: 0
        if (currentQty <= 1) {
            currentMap.remove(product)
        } else {
            currentMap[product] = currentQty - 1
        }
        _cart.value = currentMap
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    // --- POS Checkout ---
    fun checkout(paymentMethod: String) {
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return

        viewModelScope.launch {
            var totalAmount = 0.0
            var totalProfit = 0.0

            currentCart.forEach { (product, qty) ->
                totalAmount += product.sellingPrice * qty
                totalProfit += (product.sellingPrice - product.purchasePrice) * qty
            }

            // Create and insert SaleTransaction
            val transaction = SaleTransaction(
                timestamp = System.currentTimeMillis(),
                totalAmount = totalAmount,
                totalProfit = totalProfit,
                paymentMethod = paymentMethod
            )
            val transId = repository.insertTransaction(transaction).toInt()

            // Insert SaleItems & update stocks
            currentCart.forEach { (product, qty) ->
                val saleItem = SaleItem(
                    transactionId = transId,
                    productId = product.id,
                    productName = product.name,
                    quantity = qty,
                    purchasePrice = product.purchasePrice,
                    sellingPrice = product.sellingPrice
                )
                repository.insertSaleItem(saleItem)

                // Update product stock
                val updatedProduct = product.copy(stock = (product.stock - qty).coerceAtLeast(0))
                repository.updateProduct(updatedProduct)

                // Check low stock warning
                if (updatedProduct.isLowStock) {
                    triggerNotification(
                        "Stok Menipis!",
                        "Stok ${updatedProduct.name} tersisa ${updatedProduct.stock} unit (Batas minimum: ${updatedProduct.minStockThreshold} unit)."
                    )
                }
            }

            // Check if checkout hits sales target achievements
            checkTargetMilestoneAchievement(totalAmount)

            // Clear the cart
            clearCart()
            pushDataToRtdb()
            triggerNotification(
                "Penjualan Berhasil",
                "Transaksi #${transId} selesai. Total: ${formatRupiah(totalAmount)}"
            )
        }
    }

    private fun checkTargetMilestoneAchievement(newSaleAmount: Double) {
        viewModelScope.launch {
            val target = _todayTarget.value?.targetAmount ?: 1000000.0
            val todayTotalSales = getTodaySalesTotal() + newSaleAmount
            val previousSales = todayTotalSales - newSaleAmount

            val prevPercentage = (previousSales / target) * 100
            val newPercentage = (todayTotalSales / target) * 100

            if (prevPercentage < 50 && newPercentage >= 50) {
                triggerNotification(
                    "Target Penjualan 50% Tercapai! 🎉",
                    "Mantap! Anda sudah mencapai setengah target penjualan hari ini. Terus semangat!"
                )
            } else if (prevPercentage < 100 && newPercentage >= 100) {
                triggerNotification(
                    "Target Harian 100% Tercapai! 🏆🔥",
                    "Luar biasa! Target penjualan harian sebesar ${formatRupiah(target)} telah terpenuhi! Kinerja hebat!"
                )
            }
        }
    }

    fun getTodaySalesTotal(): Double {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfToday = calendar.timeInMillis

        val todayTrans = transactions.value.filter { it.timestamp in startOfToday..endOfToday }
        return todayTrans.sumOf { it.totalAmount }
    }

    // --- Product Operations ---
    fun addProduct(name: String, sku: String, stock: Int, purchasePrice: Double, sellingPrice: Double, category: String, threshold: Int, imageUrls: String = "", ageRating: Int = 0, videoUrl: String = "") {
        viewModelScope.launch {
            val product = Product(
                name = name,
                sku = sku,
                stock = stock,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                category = category,
                minStockThreshold = threshold,
                imageUrls = imageUrls,
                ageRating = ageRating,
                videoUrl = videoUrl
            )
            repository.insertProduct(product)
            pushDataToRtdb()
            triggerNotification("Produk Ditambahkan", "Produk $name berhasil dimasukkan ke inventaris.")
            if (product.isLowStock) {
                triggerNotification(
                    "Peringatan Stok Rendah!",
                    "Stok ${product.name} saat ini (${product.stock} unit) berada di bawah ambang batas minimum yang Anda tentukan (${product.minStockThreshold} unit)."
                )
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
            pushDataToRtdb()
            triggerNotification("Produk Diperbarui", "Data ${product.name} telah disimpan.")
            if (product.isLowStock) {
                triggerNotification(
                    "Peringatan Stok Rendah!",
                    "Stok ${product.name} saat ini (${product.stock} unit) berada di bawah ambang batas minimum yang Anda tentukan (${product.minStockThreshold} unit)."
                )
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            pushDataToRtdb()
            triggerNotification("Produk Dihapus", "Produk ${product.name} berhasil dihapus.")
        }
    }

    fun uploadImageToImgBB(
        uri: android.net.Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = withContext(Dispatchers.IO) {
                    inputStream?.readBytes()
                }
                inputStream?.close()

                if (bytes == null) {
                    onError("Gagal membaca file gambar.")
                    return@launch
                }

                _isSyncing.value = true
                val resultUrl = withContext(Dispatchers.IO) {
                    val mediaType = "image/*".toMediaTypeOrNull()
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "uploaded_image.jpg", RequestBody.create(mediaType, bytes))
                        .build()

                    val request = Request.Builder()
                        .url("https://api.imgbb.com/1/upload?key=f601727fed32cf7a175833d01d8a10ff")
                        .post(requestBody)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw Exception("Upload gagal dengan kode: ${response.code}")
                        }
                        val bodyString = response.body?.string() ?: throw Exception("Response body kosong")
                        val adapter = moshi.adapter(ImgBbResponse::class.java)
                        val res = adapter.fromJson(bodyString)
                        if (res?.success == true && res.data?.url != null) {
                            res.data.url
                        } else {
                            throw Exception("Respon API tidak sukses atau URL tidak ditemukan.")
                        }
                    }
                }
                _isSyncing.value = false
                onSuccess(resultUrl)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Upload image failed", e)
                _isSyncing.value = false
                onError(e.localizedMessage ?: "Terjadi kesalahan yang tidak diketahui.")
            }
        }
    }

    // --- Labels Generator ---
    private val _selectedProductForLabel = MutableStateFlow<Product?>(null)
    val selectedProductForLabel: StateFlow<Product?> = _selectedProductForLabel.asStateFlow()

    private val _selectedTemplate = MutableStateFlow("Minimalis Modern") // Minimalis Modern, Diskon/Promo, Grosir, Barcode Klasik
    val selectedTemplate: StateFlow<String> = _selectedTemplate.asStateFlow()

    private val _promoDiscountPercent = MutableStateFlow(10)
    val promoDiscountPercent: StateFlow<Int> = _promoDiscountPercent.asStateFlow()

    private val _labelSize = MutableStateFlow("50x30 mm")
    val labelSize: StateFlow<String> = _labelSize.asStateFlow()

    fun selectProductForLabel(product: Product) {
        _selectedProductForLabel.value = product
    }

    fun updateLabelTemplate(templateName: String) {
        _selectedTemplate.value = templateName
    }

    fun updateCustomStoreName(name: String) {
        _customStoreName.value = name
        prefs.edit().putString("custom_store_name", name).apply()
        
        val sanitized = sanitizedStoreName
        _sellerSphereNode.value = sanitized
        _shopSphereNode.value = sanitized
        
        addSyncLog("Nama Toko kustom diubah ke: $name (Node: $sanitized)")
        fetchOrdersFromRtdb()
    }

    fun updateProfile(
        name: String,
        email: String,
        storeName: String,
        address: String,
        latitude: Float,
        longitude: Float,
        notes: String
    ) {
        _ownerName.value = name
        _ownerEmail.value = email
        _pickupAddress.value = address
        _pickupLatitude.value = latitude
        _pickupLongitude.value = longitude
        _pickupNotes.value = notes

        prefs.edit()
            .putString("owner_name", name)
            .putString("owner_email", email)
            .putString("pickup_address", address)
            .putFloat("pickup_latitude", latitude)
            .putFloat("pickup_longitude", longitude)
            .putString("pickup_notes", notes)
            .apply()

        updateCustomStoreName(storeName)
        addSyncLog("Profil Akun dan Pinpoint Penjemputan diperbarui.")
    }

    fun updatePromoDiscount(percent: Int) {
        _promoDiscountPercent.value = percent
    }

    fun updateLabelSize(size: String) {
        _labelSize.value = size
    }

    // --- Simulated Label Printer Connection & Actions ---
    private val _printerConnectionState = MutableStateFlow("Terputus") // Terputus, Mencari..., Menghubungkan..., Terhubung (PT-210)
    val printerConnectionState: StateFlow<String> = _printerConnectionState.asStateFlow()

    private val _availablePrinters = MutableStateFlow<List<String>>(emptyList())
    val availablePrinters: StateFlow<List<String>> = _availablePrinters.asStateFlow()

    fun startPrinterDiscovery() {
        viewModelScope.launch {
            _printerConnectionState.value = "Mencari..."
            kotlinx.coroutines.delay(1500)
            _availablePrinters.value = listOf("PT-210 Thermal Printer", "RPP02N Mobile", "Zebra ZD410-Label", "Rongta RP326")
            _printerConnectionState.value = "Pilih Printer"
        }
    }

    fun connectToPrinter(printerName: String) {
        viewModelScope.launch {
            _printerConnectionState.value = "Menghubungkan..."
            kotlinx.coroutines.delay(1200)
            _printerConnectionState.value = "Terhubung ($printerName)"
            triggerNotification("Printer Terhubung", "Siap mencetak ke $printerName.")
        }
    }

    fun disconnectPrinter() {
        _printerConnectionState.value = "Terputus"
        _availablePrinters.value = emptyList()
    }

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting: StateFlow<Boolean> = _isPrinting.asStateFlow()

    fun simulatePrintLabel() {
        val prod = _selectedProductForLabel.value
        if (prod == null) {
            triggerNotification("Gagal Mencetak", "Pilih produk terlebih dahulu!")
            return
        }
        viewModelScope.launch {
            _isPrinting.value = true
            kotlinx.coroutines.delay(2000)
            _isPrinting.value = false
            triggerNotification("Cetak Selesai 🖨️", "Label untuk ${prod.name} sukses dikirim ke printer.")
        }
    }

    // --- Live Real-time Multi-Device Sync (Firebase Realtime Database) ---
    private val _syncCode = MutableStateFlow("SPHERE-SELLER-7F2A")
    val syncCode: StateFlow<String> = _syncCode.asStateFlow()

    private val _syncStatus = MutableStateFlow("Terhubung (Otomatis)") // Terputus, Menghubungkan, Terhubung, Sinkronisasi Aktif (Otomatis), Koneksi Bermasalah
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<String>>(
        listOf(
            "Sesi sinkronisasi Firebase RTDB aktif",
            "Mendengarkan perubahan real-time dari cloud...",
            "Gunakan layar Pengaturan Sinkronisasi untuk konfigurasi node."
        )
    )
    val syncLogs: StateFlow<List<String>> = _syncLogs.asStateFlow()

    fun generateNewSyncCode() {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val codeBuilder = StringBuilder("SPHERE-")
        for (i in 0..3) {
            codeBuilder.append(chars.random())
        }
        codeBuilder.append("-")
        for (i in 0..3) {
            codeBuilder.append(chars.random())
        }
        _syncCode.value = codeBuilder.toString()
        addSyncLog("Kode sinkronisasi baru dihasilkan: ${_syncCode.value}")
    }

    fun fetchOrdersFromRtdb() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = "${rtdbUrl.value}/shop-sphere/${shopSphereNode.value}/orders.json"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (!bodyString.isNullOrBlank() && bodyString != "null") {
                            val orders = orderListAdapter.fromJson(bodyString)
                            if (orders != null) {
                                _shopsphereOrders.value = orders
                                addSyncLog("Berhasil mengunduh ${orders.size} pesanan dari RTDB.")
                            }
                        } else {
                            addSyncLog("Node pesanan di RTDB kosong (${shopSphereNode.value}).")
                        }
                    } else {
                        addSyncLog("Gagal mengunduh pesanan dari RTDB. Kode: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error fetching orders", e)
                addSyncLog("Gagal memuat pesanan RTDB: ${e.localizedMessage}")
            }
        }
    }

    fun uploadOrdersToRtdb(ordersList: List<ShopsphereOrder> = _shopsphereOrders.value) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = "${rtdbUrl.value}/shop-sphere/${shopSphereNode.value}/orders.json"
                val json = orderListAdapter.toJson(ordersList)
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = RequestBody.create(mediaType, json)
                val request = Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        addSyncLog("Perubahan status pesanan berhasil diunggah ke RTDB.")
                    } else {
                        addSyncLog("Gagal mengunggah status pesanan ke RTDB. Kode: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error uploading orders", e)
                addSyncLog("Gagal mengunggah pesanan ke RTDB: ${e.localizedMessage}")
            }
        }
    }

    fun pushDataToRtdb() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                _isSyncing.value = true
                _syncStatus.value = "Mengunggah..."
                addSyncLog("Memulai unggah data lokal ke Seller Sphere RTDB...")

                val localProducts = repository.allProducts.first()
                val localTransactions = repository.allTransactions.first()
                val localSaleItems = repository.allSaleItems.first()
                val localTargets = repository.allTargets.first()

                val baseNodeUrl = "${rtdbUrl.value}/seller-sphere/${sellerSphereNode.value}"

                addSyncLog("Mengunggah ${localProducts.size} produk...")
                val prodJson = productListAdapter.toJson(localProducts)
                uploadJsonNode("$baseNodeUrl/products.json", prodJson)

                addSyncLog("Mengunggah ${localTransactions.size} transaksi...")
                val transJson = transactionListAdapter.toJson(localTransactions)
                uploadJsonNode("$baseNodeUrl/transactions.json", transJson)

                addSyncLog("Mengunggah ${localSaleItems.size} detail item penjualan...")
                val itemsJson = saleItemListAdapter.toJson(localSaleItems)
                uploadJsonNode("$baseNodeUrl/sale_items.json", itemsJson)

                addSyncLog("Mengunggah ${localTargets.size} target penjualan...")
                val targetsJson = targetListAdapter.toJson(localTargets)
                uploadJsonNode("$baseNodeUrl/targets.json", targetsJson)

                addSyncLog("Semua data lokal berhasil diunggah ke cloud RTDB!")
                _syncStatus.value = "Terhubung (Otomatis)"
                triggerNotification("Unggah Sukses ☁️", "Data toko berhasil disimpan di RTDB.")
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error pushing data", e)
                addSyncLog("Gagal mengunggah data: ${e.localizedMessage}")
                _syncStatus.value = "Koneksi Bermasalah"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun pullDataFromRtdb() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                _isSyncing.value = true
                _syncStatus.value = "Mengunduh..."
                addSyncLog("Memulai unduh data dari Seller Sphere RTDB...")

                val baseNodeUrl = "${rtdbUrl.value}/seller-sphere/${sellerSphereNode.value}"

                addSyncLog("Mengunduh katalog produk...")
                val productsJson = downloadJsonNode("$baseNodeUrl/products.json")
                val productsList = if (!productsJson.isNullOrBlank() && productsJson != "null") {
                    productListAdapter.fromJson(productsJson)
                } else null

                addSyncLog("Mengunduh rekap transaksi...")
                val transJson = downloadJsonNode("$baseNodeUrl/transactions.json")
                val transList = if (!transJson.isNullOrBlank() && transJson != "null") {
                    transactionListAdapter.fromJson(transJson)
                } else null

                addSyncLog("Mengunduh rincian barang terjual...")
                val itemsJson = downloadJsonNode("$baseNodeUrl/sale_items.json")
                val itemsList = if (!itemsJson.isNullOrBlank() && itemsJson != "null") {
                    saleItemListAdapter.fromJson(itemsJson)
                } else null

                addSyncLog("Mengunduh data target penjualan...")
                val targetsJson = downloadJsonNode("$baseNodeUrl/targets.json")
                val targetsList = if (!targetsJson.isNullOrBlank() && targetsJson != "null") {
                    targetListAdapter.fromJson(targetsJson)
                } else null

                if (productsList == null && transList == null && itemsList == null && targetsList == null) {
                    addSyncLog("Node RTDB kosong atau tidak ditemukan data sinkronisasi.")
                    return@launch
                }

                addSyncLog("Pembersihan database lokal...")
                val db = AppDatabase.getDatabase(getApplication())
                db.clearAllTables()

                addSyncLog("Mengisi database lokal dengan data cloud...")
                productsList?.forEach { repository.insertProduct(it) }
                transList?.forEach { repository.insertTransaction(it) }
                itemsList?.forEach { repository.insertSaleItem(it) }
                targetsList?.forEach { repository.insertTarget(it) }

                addSyncLog("Pemuatan ulang data lokal...")
                loadTodayTarget()

                addSyncLog("Unduh dan Sinkronisasi Lokal sukses!")
                _syncStatus.value = "Terhubung (Otomatis)"
                triggerNotification("Unduh Sukses ☁️", "Database lokal berhasil dipulihkan dari RTDB.")
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error pulling data", e)
                addSyncLog("Gagal mengunduh data: ${e.localizedMessage}")
                _syncStatus.value = "Koneksi Bermasalah"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private fun uploadJsonNode(url: String, json: String) {
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, json)
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Gagal menulis ke $url. Kode: ${response.code}")
            }
        }
    }

    private fun downloadJsonNode(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                return response.body?.string()
            } else if (response.code == 404) {
                return null
            } else {
                throw Exception("Gagal membaca dari $url. Kode: ${response.code}")
            }
        }
    }

    fun initSampleDataInRtdb() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                _isSyncing.value = true
                addSyncLog("Menginisialisasi data contoh ke RTDB...")

                // Make sure we have local products first
                val localProducts = repository.allProducts.first()
                if (localProducts.isEmpty()) {
                    seedInitialData()
                    kotlinx.coroutines.delay(1000)
                }

                val products = repository.allProducts.first()
                val transactions = repository.allTransactions.first()
                val saleItems = repository.allSaleItems.first()
                val targets = repository.allTargets.first()

                val baseNodeUrl = "${rtdbUrl.value}/seller-sphere/${sellerSphereNode.value}"
                uploadJsonNode("$baseNodeUrl/products.json", productListAdapter.toJson(products))
                uploadJsonNode("$baseNodeUrl/transactions.json", transactionListAdapter.toJson(transactions))
                uploadJsonNode("$baseNodeUrl/sale_items.json", saleItemListAdapter.toJson(saleItems))
                uploadJsonNode("$baseNodeUrl/targets.json", targetListAdapter.toJson(targets))

                addSyncLog("Membuat data pesanan contoh untuk Shop Sphere...")
                val ordersList = mutableListOf<ShopsphereOrder>()
                val productsNameList = listOf("Kemeja Flanel Slimfit", "Jeans Denim Premium", "Botol Minum Tumbler", "Sepatu Sneakers Klasik", "Kaos Polos Cotton 30s")
                val customersList = listOf("Andi", "Siti", "Budi", "Dewi", "Eko", "Rina", "Doni", "Rian", "Yusuf", "Hendra")
                val couriersList = listOf("Ahmad (Shopsphere Express)", "Yanto (J&T Express)", "Budiman (Shopsphere Express)", "Agus (SiCepat)", "Husein (Shopsphere Express)")
                
                val calendar = Calendar.getInstance()
                for (day in 0..6) {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek + day)
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    val ordersCount = if (day == 6) 4 else (1..3).random()

                    for (o in 0 until ordersCount) {
                        val orderId = "ORD-${10000 + (day * 100) + o}"
                        val prodName = productsNameList.random()
                        val qty = (1..3).random()
                        val custName = customersList.random()
                        val courName = couriersList.random()
                        val amount = qty * 45000.0
                        val status = if (day < 6) "Selesai Diambil" else {
                            when (o) {
                                0 -> "Perlu Dipacking"
                                1 -> "Siap Diambil"
                                else -> "Perlu Dipacking"
                            }
                        }
                        val verCode = (100000 + (orderId.hashCode() % 900000).let { if (it < 0) -it else it }).toString()

                        ordersList.add(
                            ShopsphereOrder(
                                id = orderId,
                                dateString = dateStr,
                                dayIndex = day,
                                productName = prodName,
                                quantity = qty,
                                customerName = custName,
                                courierName = courName,
                                courierPhone = "0812${(10000000..99999999).random()}",
                                totalAmount = amount,
                                status = status,
                                verificationCode = verCode
                            )
                        )
                    }
                }

                val ordersUrl = "${rtdbUrl.value}/shop-sphere/${shopSphereNode.value}/orders.json"
                uploadJsonNode(ordersUrl, orderListAdapter.toJson(ordersList))

                _shopsphereOrders.value = ordersList
                addSyncLog("Inisialisasi Data Contoh Sukses! RTDB Anda sekarang memiliki ${ordersList.size} pesanan dan data stok.")
                triggerNotification("Inisialisasi Sukses 🟢", "RTDB berhasil diisi dengan data contoh.")
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error seeding RTDB", e)
                addSyncLog("Inisialisasi RTDB Gagal: ${e.localizedMessage}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Menghubungkan..."
            addSyncLog("Menghubungkan ke Realtime Database: ${rtdbUrl.value}...")
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val baseNodeUrl = "${rtdbUrl.value}/seller-sphere/${sellerSphereNode.value}"
                    
                    addSyncLog("Mengunduh data toko dari node ${sellerSphereNode.value}...")
                    val productsJson = downloadJsonNode("$baseNodeUrl/products.json")
                    val productsList = if (!productsJson.isNullOrBlank() && productsJson != "null") {
                        productListAdapter.fromJson(productsJson)
                    } else null

                    val transJson = downloadJsonNode("$baseNodeUrl/transactions.json")
                    val transList = if (!transJson.isNullOrBlank() && transJson != "null") {
                        transactionListAdapter.fromJson(transJson)
                    } else null

                    val itemsJson = downloadJsonNode("$baseNodeUrl/sale_items.json")
                    val itemsList = if (!itemsJson.isNullOrBlank() && itemsJson != "null") {
                        saleItemListAdapter.fromJson(itemsJson)
                    } else null

                    val targetsJson = downloadJsonNode("$baseNodeUrl/targets.json")
                    val targetsList = if (!targetsJson.isNullOrBlank() && targetsJson != "null") {
                        targetListAdapter.fromJson(targetsJson)
                    } else null

                    if (productsList != null || transList != null || itemsList != null || targetsList != null) {
                        addSyncLog("Membaca data cloud. Sinkronisasi database lokal...")
                        val db = AppDatabase.getDatabase(getApplication())
                        db.clearAllTables()
                        
                        productsList?.forEach { repository.insertProduct(it) }
                        transList?.forEach { repository.insertTransaction(it) }
                        itemsList?.forEach { repository.insertSaleItem(it) }
                        targetsList?.forEach { repository.insertTarget(it) }
                        
                        addSyncLog("Database lokal sinkron dengan cloud!")
                    } else {
                        addSyncLog("Node cloud kosong. Menginisialisasi data lokal ke cloud...")
                        val localProducts = repository.allProducts.first()
                        val localTransactions = repository.allTransactions.first()
                        val localSaleItems = repository.allSaleItems.first()
                        val localTargets = repository.allTargets.first()

                        if (localProducts.isNotEmpty()) {
                            uploadJsonNode("$baseNodeUrl/products.json", productListAdapter.toJson(localProducts))
                            uploadJsonNode("$baseNodeUrl/transactions.json", transactionListAdapter.toJson(localTransactions))
                            uploadJsonNode("$baseNodeUrl/sale_items.json", saleItemListAdapter.toJson(localSaleItems))
                            uploadJsonNode("$baseNodeUrl/targets.json", targetListAdapter.toJson(localTargets))
                            addSyncLog("Berhasil mengunggah data lokal ke cloud.")
                        }
                    }

                    addSyncLog("Mengunduh data pesanan Shop Sphere...")
                    val ordersUrl = "${rtdbUrl.value}/shop-sphere/${shopSphereNode.value}/orders.json"
                    val ordersJson = downloadJsonNode(ordersUrl)
                    if (!ordersJson.isNullOrBlank() && ordersJson != "null") {
                        val orders = orderListAdapter.fromJson(ordersJson)
                        if (orders != null) {
                            _shopsphereOrders.value = orders
                            addSyncLog("Berhasil memuat ${orders.size} pesanan.")
                        }
                    }

                    _syncStatus.value = "Terhubung (Otomatis)"
                    addSyncLog("Sinkronisasi dua arah selesai. Data sinkron.")
                    triggerNotification("Sinkronisasi Selesai", "Semua data inventaris, transaksi, dan pesanan telah sinkron.")
                } catch (e: Exception) {
                    Log.e("AppViewModel", "Sync failed", e)
                    addSyncLog("Koneksi gagal: ${e.localizedMessage}")
                    _syncStatus.value = "Koneksi Bermasalah"
                }
            }
            _isSyncing.value = false
        }
    }

    fun addSyncLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val current = _syncLogs.value.toMutableList()
        current.add(0, "[$time] $message")
        _syncLogs.value = current
    }

    // --- Import / Export CSV (Excel Compatible) & Print layout representation ---
    fun exportProductsToCsv(): String {
        val sb = StringBuilder()
        sb.append("ID,Nama Produk,SKU,Stok,Harga Beli,Harga Jual,Kategori,Ambang Minimum\n")
        products.value.forEach { p ->
            sb.append("${p.id},\"${p.name.replace("\"", "\"\"")}\",${p.sku},${p.stock},${p.purchasePrice},${p.sellingPrice},\"${p.category}\",${p.minStockThreshold}\n")
        }
        return sb.toString()
    }

    fun exportTransactionsToCsv(): String {
        val sb = StringBuilder()
        sb.append("ID,Tanggal,Total Omzet,Total Keuntungan,Metode Pembayaran\n")
        transactions.value.forEach { t ->
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(t.timestamp))
            sb.append("${t.id},${dateStr},${t.totalAmount},${t.totalProfit},${t.paymentMethod}\n")
        }
        return sb.toString()
    }

    fun importProductsFromCsv(csvContent: String): Boolean {
        try {
            val lines = csvContent.lines()
            if (lines.size <= 1) return false
            viewModelScope.launch {
                val lowStockNames = mutableListOf<String>()
                lines.forEachIndexed { index, line ->
                    if (index == 0 || line.isBlank()) return@forEachIndexed
                    val tokens = parseCsvLine(line)
                    if (tokens.size >= 7) {
                        val name = tokens[1]
                        val sku = tokens[2]
                        val stock = tokens[3].toIntOrNull() ?: 0
                        val purchasePrice = tokens[4].toDoubleOrNull() ?: 0.0
                        val sellingPrice = tokens[5].toDoubleOrNull() ?: 0.0
                        val category = tokens[6]
                        val threshold = if (tokens.size > 7) tokens[7].toIntOrNull() ?: 5 else 5

                        val p = Product(
                            name = name,
                            sku = sku,
                            stock = stock,
                            purchasePrice = purchasePrice,
                            sellingPrice = sellingPrice,
                            category = category,
                            minStockThreshold = threshold
                        )
                        repository.insertProduct(p)
                        if (p.isLowStock) {
                            lowStockNames.add(p.name)
                        }
                    }
                }
                triggerNotification("Impor Sukses", "Data produk berhasil diimpor dari file CSV Excel.")
                if (lowStockNames.isNotEmpty()) {
                    triggerNotification(
                        "Peringatan Stok Rendah!",
                        "${lowStockNames.size} produk yang diimpor memiliki stok di bawah batas minimum: ${lowStockNames.joinToString(", ")}"
                    )
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("AppViewModel", "Gagal mengimpor CSV", e)
            return false
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    cur.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString().trim())
                cur = StringBuilder()
            } else {
                cur.append(c)
            }
            i++
        }
        result.add(cur.toString().trim())
        return result
    }

    // --- Notification & Reminders Hub ---
    data class NotificationItem(val id: Long, val title: String, val message: String, val timestamp: Long)

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _notificationFlow = MutableSharedFlow<NotificationItem>()
    val notificationFlow = _notificationFlow.asSharedFlow()

    fun triggerNotification(title: String, message: String) {
        val newItem = NotificationItem(
            id = System.nanoTime(),
            title = title,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        val list = _notifications.value.toMutableList()
        list.add(0, newItem)
        _notifications.value = list
        viewModelScope.launch {
            _notificationFlow.emit(newItem)
        }
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    fun deleteNotification(id: Long) {
        _notifications.value = _notifications.value.filterNot { it.id == id }
    }

    // --- Seed Demo Data (Interactive Analytics) ---
    private fun seedInitialData() {
        viewModelScope.launch {
            val count = repository.allProducts.first().size
            if (count == 0) {
                // Prepopulate products
                val sampleProducts = listOf(
                    Product(name = "Kemeja Flanel Slimfit", sku = "BJU-01", stock = 3, purchasePrice = 85000.0, sellingPrice = 135000.0, category = "Pakaian", minStockThreshold = 4, ageRating = 13),
                    Product(name = "Jeans Denim Premium", sku = "BJU-02", stock = 12, purchasePrice = 120000.0, sellingPrice = 199000.0, category = "Pakaian", minStockThreshold = 5, ageRating = 18),
                    Product(name = "Botol Minum Tumbler", sku = "ACC-01", stock = 20, purchasePrice = 25000.0, sellingPrice = 45000.0, category = "Aksesoris", minStockThreshold = 6, ageRating = 0),
                    Product(name = "Sepatu Sneakers Klasik", sku = "SPT-01", stock = 2, purchasePrice = 150000.0, sellingPrice = 250000.0, category = "Sepatu", minStockThreshold = 3, ageRating = 13),
                    Product(name = "Kaos Polos Cotton 30s", sku = "BJU-03", stock = 45, purchasePrice = 18000.0, sellingPrice = 35000.0, category = "Pakaian", minStockThreshold = 8, ageRating = 0),
                    Product(name = "Rokok Marlboro Merah", sku = "RKK-01", stock = 50, purchasePrice = 32000.0, sellingPrice = 38000.0, category = "Tembakau", minStockThreshold = 5, ageRating = 18),
                    Product(name = "Kopi Hitam Espresso", sku = "KPI-01", stock = 15, purchasePrice = 12000.0, sellingPrice = 18000.0, category = "Minuman", minStockThreshold = 5, ageRating = 13)
                )

                sampleProducts.forEach { repository.insertProduct(it) }

                // Seed historical sales to populate the last 7 days of weekly sales trends
                val calendar = Calendar.getInstance()
                val currentMilli = calendar.timeInMillis

                val randomSales = listOf(
                    450000.0,  // 6 days ago
                    680000.0,  // 5 days ago
                    540000.0,  // 4 days ago
                    920000.0,  // 3 days ago
                    1200000.0, // 2 days ago
                    850000.0,  // 1 day ago
                    750000.0   // Today
                )

                val randomProfits = listOf(
                    170000.0,
                    240000.0,
                    210000.0,
                    360000.0,
                    480000.0,
                    310000.0,
                    290000.0
                )

                for (i in 0..6) {
                    calendar.timeInMillis = currentMilli
                    calendar.add(Calendar.DAY_OF_YEAR, - (6 - i))
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

                    // Target
                    repository.insertTarget(SalesTarget(dateStr, 1000000.0))

                    // Transaction
                    val transaction = SaleTransaction(
                        timestamp = calendar.timeInMillis,
                        totalAmount = randomSales[i],
                        totalProfit = randomProfits[i],
                        paymentMethod = if (i % 2 == 0) "Tunai" else "QRIS"
                    )
                    repository.insertTransaction(transaction)
                }

                loadTodayTarget()
            } else {
                loadTodayTarget()
            }
        }
    }

    // Helper functions
    fun formatRupiah(amount: Double): String {
        val format = java.text.NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }

    fun initBuyerChats() {
        val now = System.currentTimeMillis()
        _buyerChats.value = listOf(
            BuyerChat(
                customerName = "Andi",
                messages = listOf(
                    BuyerMessage(senderName = "Andi", text = "Halo min, kemeja flanel slimfit size L ready?", timestamp = now - 3 * 3600 * 1000, isFromBuyer = true),
                    BuyerMessage(senderName = "Seller", text = "Halo kak, ready ya. Silakan diorder.", timestamp = now - 2 * 3600 * 1000, isFromBuyer = false),
                    BuyerMessage(senderName = "Andi", text = "Apakah barangnya sudah dikirim kak?", timestamp = now - 45 * 60 * 1000, isFromBuyer = true),
                    BuyerMessage(senderName = "Andi", text = "Soalnya saya butuh cepat untuk acara besok.", timestamp = now - 44 * 60 * 1000, isFromBuyer = true)
                ),
                unreadCount = 2,
                lastMessageTimestamp = now - 44 * 60 * 1000
            ),
            BuyerChat(
                customerName = "Dewi",
                messages = listOf(
                    BuyerMessage(senderName = "Dewi", text = "Kak, botol minum tumbler saya bocor dikit. Apakah bisa ditukar?", timestamp = now - 15 * 60 * 1000, isFromBuyer = true)
                ),
                unreadCount = 1,
                lastMessageTimestamp = now - 15 * 60 * 1000
            ),
            BuyerChat(
                customerName = "Budi",
                messages = listOf(
                    BuyerMessage(senderName = "Budi", text = "Untuk sepatu sneakers klasik warnanya apa aja ya?", timestamp = now - 2 * 3600 * 1000, isFromBuyer = true),
                    BuyerMessage(senderName = "Budi", text = "Bisa minta tolong fotokan aslinya?", timestamp = now - 2 * 3600 * 1000 + 30000, isFromBuyer = true)
                ),
                unreadCount = 2,
                lastMessageTimestamp = now - 2 * 3600 * 1000 + 30000
            ),
            BuyerChat(
                customerName = "Siti",
                messages = listOf(
                    BuyerMessage(senderName = "Siti", text = "Pagi sis, mau tanya jeans denim premium bahannya melar gak?", timestamp = now - 24 * 3600 * 1000, isFromBuyer = true),
                    BuyerMessage(senderName = "Seller", text = "Sore kak, jeans kami semi-stretch nyaman dipakai sehari-hari.", timestamp = now - 20 * 3600 * 1000, isFromBuyer = false)
                ),
                unreadCount = 0,
                lastMessageTimestamp = now - 20 * 3600 * 1000
            )
        )
    }

    fun markChatAsRead(customerName: String) {
        _buyerChats.value = _buyerChats.value.map { chat ->
            if (chat.customerName.equals(customerName, ignoreCase = true)) {
                chat.copy(unreadCount = 0)
            } else {
                chat
            }
        }
    }

    fun sendMessageToBuyer(customerName: String, text: String) {
        val now = System.currentTimeMillis()
        val newMessage = BuyerMessage(senderName = "Seller", text = text, timestamp = now, isFromBuyer = false)
        
        _buyerChats.value = _buyerChats.value.map { chat ->
            if (chat.customerName.equals(customerName, ignoreCase = true)) {
                chat.copy(
                    messages = chat.messages + newMessage,
                    lastMessageTimestamp = now
                )
            } else {
                chat
            }
        }

        // Auto-reply simulation from buyer
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500L)
            val buyerReplies = listOf(
                "Baik kak, terima kasih banyak atas responnya! 😊",
                "Siap kak, terima kasih infonya ya.",
                "Ok min, nanti kalau senggang saya mampir.",
                "Mantap! Terima kasih respon cepatnya min.",
                "Ditunggu ya kak kabarnya."
            )
            val randomReply = buyerReplies.random()
            val replyTime = System.currentTimeMillis()
            val replyMsg = BuyerMessage(senderName = customerName, text = randomReply, timestamp = replyTime, isFromBuyer = true)

            _buyerChats.value = _buyerChats.value.map { chat ->
                if (chat.customerName.equals(customerName, ignoreCase = true)) {
                    val isCurrentlyActive = activeChatBuyerName.value?.equals(customerName, ignoreCase = true) == true
                    val newUnread = if (isCurrentlyActive) 0 else chat.unreadCount + 1
                    
                    if (!isCurrentlyActive) {
                        triggerNotification("Pesan Baru: $customerName", randomReply)
                    }
                    
                    chat.copy(
                        messages = chat.messages + replyMsg,
                        unreadCount = newUnread,
                        lastMessageTimestamp = replyTime
                    )
                } else {
                    chat
                }
            }
        }
    }

    private fun initShopsphereOrders() {
        val calendar = Calendar.getInstance()
        val sdfDate = SimpleDateFormat("dd/MM", Locale.getDefault())
        val ordersList = mutableListOf<ShopsphereOrder>()

        val productNames = listOf(
            "Kemeja Flanel Slimfit",
            "Jeans Denim Premium",
            "Botol Minum Tumbler",
            "Sepatu Sneakers Klasik",
            "Kaos Polos Cotton 30s"
        )
        val customerNames = listOf(
            "Budi Santoso", "Siti Rahma", "Dian Pratama", "Rian Wijaya", "Novianti",
            "Adi Hidayat", "Eka Putri", "Fajar Nugraha", "Gita Lestari", "Hendra Wijaya"
        )
        val courierNames = listOf(
            "Ahmad (Shopsphere Express)", "Yanto (J&T Express)", "Budiman (Shopsphere Express)",
            "Agus (SiCepat)", "Husein (Shopsphere Express)", "Dedi (Anteraja)"
        )

        // Seed orders for the last 7 days
        for (day in 0..6) {
            val checkCalendar = Calendar.getInstance()
            checkCalendar.add(Calendar.DAY_OF_YEAR, - (6 - day))
            val dateStr = sdfDate.format(checkCalendar.time)

            // Number of orders for this day
            val orderCount = when(day) {
                0 -> 4
                1 -> 5
                2 -> 3
                3 -> 6
                4 -> 4
                5 -> 7
                else -> 5 // Today
            }

            for (o in 0 until orderCount) {
                val orderId = "SS-${100000 + day * 1000 + o * 10}"
                val prodName = productNames.random()
                val qty = (1..3).random()
                val custName = customerNames.random()
                val courName = courierNames.random()
                val phone = "081${(10000000..99999999).random()}"
                val amount = qty * 50000.0 // average price estimation

                val status = if (day < 6) {
                    "Selesai Diambil"
                } else {
                    // Today's orders have mixed status
                    when (o) {
                        0 -> "Perlu Dipacking"
                        1 -> "Siap Diambil"
                        2 -> "Perlu Dipacking"
                        3 -> "Selesai Diambil"
                        else -> "Perlu Dipacking"
                    }
                }

                val verCode = (100000 + (orderId.hashCode() % 900000).let { if (it < 0) -it else it }).toString()

                ordersList.add(
                    ShopsphereOrder(
                        id = orderId,
                        dateString = dateStr,
                        dayIndex = day,
                        productName = prodName,
                        quantity = qty,
                        customerName = custName,
                        courierName = courName,
                        courierPhone = phone,
                        totalAmount = amount,
                        status = status,
                        verificationCode = verCode
                    )
                )
            }
        }
        _shopsphereOrders.value = ordersList
    }

    fun finishPacking(orderId: String) {
        val currentList = _shopsphereOrders.value.map { order ->
            if (order.id == orderId) {
                order.copy(status = "Siap Diambil")
            } else {
                order
            }
        }
        _shopsphereOrders.value = currentList
        uploadOrdersToRtdb(currentList)
        val order = currentList.find { order -> order.id == orderId }
        order?.let {
            triggerNotification(
                "Packing Selesai 📦",
                "Pesanan ${it.id} selesai dipacking. Pembeli telah mendapatkan notifikasi untuk mengambil barang."
            )
        }
    }

    fun confirmOrderPickup(orderId: String) {
        val currentList = _shopsphereOrders.value.map { order ->
            if (order.id == orderId) {
                order.copy(status = "Selesai Diambil")
            } else {
                order
            }
        }
        _shopsphereOrders.value = currentList
        uploadOrdersToRtdb(currentList)
        val order = currentList.find { order -> order.id == orderId }
        order?.let {
            triggerNotification(
                "Pengambilan Selesai 📦",
                "Pesanan ${it.id} (${it.productName}) telah berhasil diambil oleh pembeli (${it.customerName})."
            )
        }
    }

    fun callCourier(orderId: String) {
        val order = _shopsphereOrders.value.find { order -> order.id == orderId }
        order?.let {
            triggerNotification(
                "Hubungi Pembeli 📞",
                "Menghubungi pembeli ${it.customerName} (${it.courierPhone}) terkait pengambilan pesanan ${it.id}."
            )
        }
    }

    fun printOrderLabel(orderId: String) {
        val order = _shopsphereOrders.value.find { order -> order.id == orderId }
        order?.let {
            // Find matched product if possible to set selection
            val matchedProd = products.value.find { p -> p.name.contains(it.productName, ignoreCase = true) }
            if (matchedProd != null) {
                selectProductForLabel(matchedProd)
            }
            triggerNotification(
                "Cetak Nota Sukses 🖨️",
                "Nota belanja/struk untuk pesanan ${it.id} berhasil dicetak menggunakan printer thermal."
            )
        }
    }
}

data class ShopsphereOrder(
    val id: String,
    val dateString: String,
    val dayIndex: Int, // 0..6
    val productName: String,
    val quantity: Int,
    val customerName: String,
    val courierName: String,
    val courierPhone: String,
    val totalAmount: Double,
    val status: String, // "Perlu Dipacking", "Siap Diambil", "Selesai Diambil"
    val verificationCode: String
)

data class ImgBbResponse(
    val data: ImgBbData?,
    val success: Boolean,
    val status: Int
)

data class ImgBbData(
    val url: String?,
    val display_url: String?
)

data class BuyerMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderName: String, // "Seller" or customerName
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromBuyer: Boolean
)

data class BuyerChat(
    val customerName: String,
    val messages: List<BuyerMessage>,
    val unreadCount: Int,
    val lastMessageTimestamp: Long
)
