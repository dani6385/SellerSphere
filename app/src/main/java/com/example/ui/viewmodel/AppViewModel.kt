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
    fun addProduct(name: String, sku: String, stock: Int, purchasePrice: Double, sellingPrice: Double, category: String, threshold: Int) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                sku = sku,
                stock = stock,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                category = category,
                minStockThreshold = threshold
            )
            repository.insertProduct(product)
            triggerNotification("Produk Ditambahkan", "Produk $name berhasil dimasukkan ke inventaris.")
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
            triggerNotification("Produk Diperbarui", "Data ${product.name} telah disimpan.")
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            triggerNotification("Produk Dihapus", "Produk ${product.name} berhasil dihapus.")
        }
    }

    // --- Labels Generator ---
    private val _selectedProductForLabel = MutableStateFlow<Product?>(null)
    val selectedProductForLabel: StateFlow<Product?> = _selectedProductForLabel.asStateFlow()

    private val _selectedTemplate = MutableStateFlow("Minimalis Modern") // Minimalis Modern, Diskon/Promo, Grosir, Barcode Klasik
    val selectedTemplate: StateFlow<String> = _selectedTemplate.asStateFlow()

    private val _customStoreName = MutableStateFlow("SS Seller Sphere")
    val customStoreName: StateFlow<String> = _customStoreName.asStateFlow()

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

    // --- Simulated Real-time Multi-Device Sync ---
    private val _syncCode = MutableStateFlow("SPHERE-SELLER-7F2A")
    val syncCode: StateFlow<String> = _syncCode.asStateFlow()

    private val _syncStatus = MutableStateFlow("Sinkronisasi Aktif (Otomatis)") // Terputus, Menghubungkan, Terhubung, Sinkronisasi Aktif (Otomatis)
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<String>>(
        listOf(
            "Sesi sinkronisasi dimulai pada ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}",
            "Perangkat induk terhubung dengan cloud",
            "Data 5 produk terunggah ke penyimpanan cloud aman",
            "Mendengarkan perubahan real-time dari perangkat lain..."
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

    fun triggerManualSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Menghubungkan ke Cloud..."
            addSyncLog("Menghubungkan ke server Seller Sphere Cloud...")
            kotlinx.coroutines.delay(1000)

            _syncStatus.value = "Mengunduh Perubahan..."
            addSyncLog("Membandingkan data lokal dengan cloud...")
            kotlinx.coroutines.delay(1000)

            // Let's add a random sample product from cloud if list is short to demonstrate real-time data flow
            if (products.value.size < 10) {
                val names = listOf("Kopi Robusta Premium", "Teh Hijau Organik", "Cokelat Susu 100g", "Minyak Goreng 1L", "Gula Pasir 1kg")
                val category = listOf("Minuman", "Minuman", "Makanan", "Bahan Pokok", "Bahan Pokok")
                val randIndex = (names.indices).random()
                val newProd = Product(
                    name = names[randIndex] + " (Cloud)",
                    sku = "SKU-CLOUD-${(100..999).random()}",
                    stock = (10..50).random(),
                    purchasePrice = 12000.0,
                    sellingPrice = 16500.0,
                    category = category[randIndex],
                    minStockThreshold = 5
                )
                repository.insertProduct(newProd)
                addSyncLog("Data produk baru disinkronkan: ${newProd.name}")
            }

            _isSyncing.value = false
            _syncStatus.value = "Sinkronisasi Aktif (Otomatis)"
            addSyncLog("Sinkronisasi real-time berhasil diselesaikan. Semua data konsisten!")
            triggerNotification("Sinkronisasi Selesai", "Data inventaris dan penjualan berhasil disinkronkan.")
        }
    }

    private fun addSyncLog(message: String) {
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
                    }
                }
                triggerNotification("Impor Sukses", "Data produk berhasil diimpor dari file CSV Excel.")
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

    // --- Seed Demo Data (Interactive Analytics) ---
    private fun seedInitialData() {
        viewModelScope.launch {
            val count = repository.allProducts.first().size
            if (count == 0) {
                // Prepopulate products
                val sampleProducts = listOf(
                    Product(name = "Kemeja Flanel Slimfit", sku = "BJU-01", stock = 3, purchasePrice = 85000.0, sellingPrice = 135000.0, category = "Pakaian", minStockThreshold = 4),
                    Product(name = "Jeans Denim Premium", sku = "BJU-02", stock = 12, purchasePrice = 120000.0, sellingPrice = 199000.0, category = "Pakaian", minStockThreshold = 5),
                    Product(name = "Botol Minum Tumbler", sku = "ACC-01", stock = 20, purchasePrice = 25000.0, sellingPrice = 45000.0, category = "Aksesoris", minStockThreshold = 6),
                    Product(name = "Sepatu Sneakers Klasik", sku = "SPT-01", stock = 2, purchasePrice = 150000.0, sellingPrice = 250000.0, category = "Sepatu", minStockThreshold = 3),
                    Product(name = "Kaos Polos Cotton 30s", sku = "BJU-03", stock = 45, purchasePrice = 18000.0, sellingPrice = 35000.0, category = "Pakaian", minStockThreshold = 8)
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
                        status = status
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
    val status: String // "Perlu Dipacking", "Siap Diambil", "Selesai Diambil"
)
