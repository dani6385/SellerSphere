package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SoftTeal
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.RadiantRose
import com.example.ui.util.QrCodeUtil
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: AppViewModel,
    onNavigateToLabelPrinter: (Product) -> Unit
) {
    val products by viewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }

    // Dialog state
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var qrProduct by remember { mutableStateOf<Product?>(null) }

    // CSV state
    var showCsvDialog by remember { mutableStateOf(false) }
    var csvInputText by remember { mutableStateOf("") }

    val categories = remember(products) {
        listOf("Semua") + products.map { it.category }.distinct().filter { it.isNotBlank() }
    }

    var selectedSortKey by remember { mutableStateOf("Alphabetical") }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) || p.sku.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "Semua" || p.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    val sortedProducts = remember(filteredProducts, selectedSortKey) {
        when (selectedSortKey) {
            "Stock Level (Low to High)" -> filteredProducts.sortedBy { p -> p.stock }
            "Price" -> filteredProducts.sortedBy { p -> p.sellingPrice }
            "Alphabetical" -> filteredProducts.sortedBy { p -> p.name.lowercase() }
            else -> filteredProducts
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        var sortMenuExpanded by remember { mutableStateOf(false) }
        val sortOptions = listOf(
            "Alphabetical" to "Alphabetical",
            "Stock Level (Low to High)" to "Stock Level (Low to High)",
            "Price" to "Price"
        )

        // 1. Search Box at the very top
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari berdasarkan nama atau SKU...", fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("inventory_search_field")
        )

        // 2. Categories Horizontally Scrollable next to Sort Dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Horizontal scroll for categories
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sort Dropdown Button
            Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                OutlinedButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("inventory_sort_button"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Urutkan",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val shortActiveLabel = when (selectedSortKey) {
                        "Stock Level (Low to High)" -> "Stok"
                        "Price" -> "Harga"
                        else -> "A-Z"
                    }
                    Text(
                        text = shortActiveLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    modifier = Modifier.testTag("inventory_sort_menu")
                ) {
                    sortOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label, fontSize = 13.sp) },
                            onClick = {
                                selectedSortKey = key
                                sortMenuExpanded = false
                            },
                            leadingIcon = {
                                val icon = when (key) {
                                    "Stock Level (Low to High)" -> Icons.Default.ImportExport
                                    "Price" -> Icons.Default.MonetizationOn
                                    else -> Icons.Default.Sort
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("sort_option_$key")
                        )
                    }
                }
            }
        }

        // 3. Main Body Scrollable Area (Unified LazyColumn)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header 1: Quick actions and Info Cards
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Action Buttons Row (Add Product & Import CSV)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("add_product_trigger")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Barang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { showCsvDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ImportExport, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Impor/Ekspor", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Stats Ringkasan & Estimasi Side-by-Side Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Ringkasan Stok Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Ringkasan Stok",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Jenis", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${products.size} Item", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = NeonCyan)
                                }
                                val totalStock = products.sumOf { it.stock }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$totalStock Unit", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = SoftTeal)
                                }
                                val lowStockCount = products.count { it.isLowStock }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Kritis", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "$lowStockCount Item",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (lowStockCount > 0) WarmOrange else SoftTeal
                                    )
                                }
                            }
                        }

                        // Estimasi Nilai Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Estimasi Nilai",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val totalRevenue = products.sumOf { it.sellingPrice * it.stock }
                                val totalCost = products.sumOf { it.purchasePrice * it.stock }
                                val potentialProfit = totalRevenue - totalCost

                                Column {
                                    Text(
                                        text = "Pendapatan",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = viewModel.formatRupiah(totalRevenue),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = NeonCyan,
                                        modifier = Modifier.testTag("total_estimated_revenue_text")
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Modal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = viewModel.formatRupiah(totalCost),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Untung", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = viewModel.formatRupiah(potentialProfit),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (potentialProfit >= 0) SoftTeal else RadiantRose
                                    )
                                }
                            }
                        }
                    }

                    val lowStockCount = products.count { it.isLowStock }
                    if (lowStockCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WarmOrange.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ada $lowStockCount produk perlu re-stock segera!",
                                    fontSize = 11.sp,
                                    color = WarmOrange,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Header 2: Product List Label
            item {
                Text(
                    text = "Daftar Produk (${filteredProducts.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            // List of Products
            if (sortedProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada produk di dalam inventaris",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(sortedProducts, key = { it.id }) { product ->
                    ProductItemCard(
                        product = product,
                        viewModel = viewModel,
                        onEdit = { editingProduct = product },
                        onDelete = { productToDelete = product },
                        onPrintLabel = {
                            viewModel.selectProductForLabel(product)
                            onNavigateToLabelPrinter(product)
                        },
                        onShowQr = { qrProduct = product }
                    )
                }
            }
        }
    }

    // Add Product Dialog
    if (showAddDialog) {
        ProductFormDialog(
            title = "Tambah Produk Baru",
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onSave = { name, sku, stock, purchase, sell, cat, threshold, images ->
                viewModel.addProduct(name, sku, stock, purchase, sell, cat, threshold, images)
                showAddDialog = false
            }
        )
    }

    // Edit Product Dialog
    editingProduct?.let { product ->
        ProductFormDialog(
            title = "Ubah Produk",
            product = product,
            viewModel = viewModel,
            onDismiss = { editingProduct = null },
            onSave = { name, sku, stock, purchase, sell, cat, threshold, images ->
                viewModel.updateProduct(
                    product.copy(
                        name = name,
                        sku = sku,
                        stock = stock,
                        purchasePrice = purchase,
                        sellingPrice = sell,
                        category = cat,
                        minStockThreshold = threshold,
                        imageUrls = images
                    )
                )
                editingProduct = null
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = RadiantRose) },
            title = { Text("Hapus Produk?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Apakah Anda yakin ingin menghapus '${product.name}'? Tindakan ini tidak dapat dibatalkan.", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RadiantRose)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // QR Code Dialog
    qrProduct?.let { product ->
        ProductQrDialog(
            product = product,
            viewModel = viewModel,
            onDismiss = { qrProduct = null }
        )
    }

    // CSV Import / Export Dialog
    if (showCsvDialog) {
        AlertDialog(
            onDismissRequest = { showCsvDialog = false },
            title = { Text("Impor / Ekspor CSV Produk", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Format: Nama,SKU,Stok,HargaBeli,HargaJual,Kategori,Threshold",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = csvInputText,
                        onValueChange = { csvInputText = it },
                        placeholder = { Text("Tempel data CSV di sini untuk impor...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val csv = viewModel.exportProductsToCsv()
                                csvInputText = csv
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = NeonCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin Ekspor", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (csvInputText.isNotBlank()) {
                                    val success = viewModel.importProductsFromCsv(csvInputText)
                                    if (success) {
                                        showCsvDialog = false
                                        csvInputText = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Impor CSV", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCsvDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    viewModel: AppViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPrintLabel: () -> Unit,
    onShowQr: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
            width = 1.dp,
            color = if (product.isLowStock) WarmOrange.copy(alpha = 0.5f) else Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val firstImageUrl = remember(product.imageUrls) {
                        product.imageUrls.split(",").firstOrNull { it.isNotBlank() }
                    }
                    var showGalleryDialog by remember { mutableStateOf(false) }

                    if (firstImageUrl != null) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable { showGalleryDialog = true }
                        ) {
                            AsyncImage(
                                model = firstImageUrl,
                                contentDescription = product.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ImageNotSupported,
                                contentDescription = "Tidak ada foto",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (showGalleryDialog && firstImageUrl != null) {
                        ProductGalleryDialog(
                            product = product,
                            onDismiss = { showGalleryDialog = false }
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            if (product.isLowStock) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Peringatan Stok Menipis",
                                    tint = RadiantRose,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .testTag("low_stock_warning_icon_${product.id}")
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = NeonCyan.copy(alpha = 0.1f),
                                contentColor = NeonCyan,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SKU: ${product.sku}",
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = product.category,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Action Menu Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onShowQr,
                        modifier = Modifier.size(32.dp).testTag("product_qr_button_${product.id}")
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = "QR Code", tint = NeonCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onPrintLabel,
                        modifier = Modifier.size(32.dp).testTag("product_print_button_${product.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = "Cetak Label", tint = NeonCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp).testTag("product_edit_button_${product.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Ubah", tint = SoftTeal, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("product_delete_button_${product.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = RadiantRose, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price & Profit
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = viewModel.formatRupiah(product.sellingPrice),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jual",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Modal: ${viewModel.formatRupiah(product.purchasePrice)} • Untung: ${viewModel.formatRupiah(product.profitPerUnit)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // Stock status
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (product.isLowStock) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Stok Kritis",
                                tint = RadiantRose,
                                modifier = Modifier
                                    .size(14.dp)
                                    .testTag("low_stock_status_warning_icon_${product.id}")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(SoftTeal)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "${product.stock} Unit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (product.isLowStock) WarmOrange else SoftTeal
                        )
                    }
                    if (product.isLowStock) {
                        Text(
                            text = "Min: ${product.minStockThreshold} Unit",
                            fontSize = 9.sp,
                            color = WarmOrange
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    title: String,
    product: Product? = null,
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Double, Double, String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var stockText by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var purchaseText by remember { mutableStateOf(product?.purchasePrice?.toString() ?: "") }
    var sellText by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Umum") }
    var thresholdText by remember { mutableStateOf(product?.minStockThreshold?.toString() ?: "5") }

    val initialUrls = remember(product) {
        if (product?.imageUrls?.isNotBlank() == true) {
            product.imageUrls.split(",").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }
    var imageUrlsList by remember { mutableStateOf(initialUrls) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            isUploadingImage = true
            uploadError = null
            viewModel.uploadImageToImgBB(
                uri = it,
                onSuccess = { uploadedUrl ->
                    isUploadingImage = false
                    if (imageUrlsList.size < 10) {
                        imageUrlsList = imageUrlsList + uploadedUrl
                    }
                },
                onError = { errorMsg ->
                    isUploadingImage = false
                    uploadError = errorMsg
                }
            )
        }
    }

    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Barang") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU / Barcode") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            label = { Text("Stok") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = thresholdText,
                            onValueChange = { thresholdText = it },
                            label = { Text("Min. Threshold") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = purchaseText,
                            onValueChange = { purchaseText = it },
                            label = { Text("Harga Beli") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sellText,
                            onValueChange = { sellText = it },
                            label = { Text("Harga Jual") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Add up to 10 product images/photos section
                item {
                    Text(
                        text = "Foto Produk (Maksimal 10)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Render existing images
                        imageUrlsList.forEachIndexed { index, url ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Foto Produk $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                // Delete Button Overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable {
                                            imageUrlsList = imageUrlsList.filterIndexed { i, _ -> i != index }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus Foto",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        // Uploading placeholder
                        if (isUploadingImage) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        // Add Photo Button (only if count is less than 10 and not currently uploading)
                        if (imageUrlsList.size < 10 && !isUploadingImage) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { launcher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Tambah Foto",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${imageUrlsList.size}/10",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (uploadError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gagal mengunggah foto: $uploadError",
                            color = RadiantRose,
                            fontSize = 11.sp
                        )
                    }
                }

                if (hasError) {
                    item {
                        Text(
                            text = "Silakan lengkapi semua data dengan format angka yang valid.",
                            color = RadiantRose,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val stockVal = stockText.toIntOrNull()
                    val purchaseVal = purchaseText.toDoubleOrNull()
                    val sellVal = sellText.toDoubleOrNull()
                    val thresholdVal = thresholdText.toIntOrNull() ?: 5

                    if (name.isNotBlank() && stockVal != null && purchaseVal != null && sellVal != null) {
                        val imageUrlsJoined = imageUrlsList.joinToString(",")
                        onSave(name, sku, stockVal, purchaseVal, sellVal, category, thresholdVal, imageUrlsJoined)
                    } else {
                        hasError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ProductQrDialog(
    product: Product,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var labelSize by remember { mutableStateOf("50x30 mm") }
    var highContrast by remember { mutableStateOf(false) }
    var isSimulatingPrint by remember { mutableStateOf(false) }
    var printSuccess by remember { mutableStateOf(false) }

    val qrText = if (product.sku.isBlank()) "PROD-${product.id}" else product.sku
    val qrBitmap = remember(qrText) {
        QrCodeUtil.generateQrCode(qrText, 300)
    }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Cetak Label & QR Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Pratinjau Stiker Thermal (Siap Cetak)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // The printable physical label simulator (White Background card representing actual paper)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (highContrast) Color.White else Color(0xFFF1F5F9)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, Color.Black.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Store Name Banner
                        Text(
                            text = "SS SELLER SPHERE",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.Black)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // QR Code image
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Product QR Code",
                                modifier = Modifier
                                    .size(130.dp)
                                    .border(1.dp, Color.LightGray)
                                    .padding(4.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Gagal memuat QR", color = Color.Red, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Product Name
                        Text(
                            text = product.name.uppercase(),
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )

                        // SKU Text
                        Text(
                            text = "SKU: $qrText",
                            color = Color.DarkGray,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Price Text
                        Text(
                            text = viewModel.formatRupiah(product.sellingPrice),
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Configuration Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ukuran Kertas:", fontSize = 12.sp, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("50x30 mm", "40x40 mm", "30x30 mm").forEach { size ->
                            val isSel = labelSize == size
                            Surface(
                                modifier = Modifier
                                    .clickable { labelSize = size }
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isSel) NeonCyan.copy(alpha = 0.2f) else Color.Transparent,
                                border = BorderStroke(1.dp, if (isSel) NeonCyan else Color.Gray.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = size,
                                    fontSize = 10.sp,
                                    color = if (isSel) NeonCyan else Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Kontras Tinggi (Maksimal)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Direkomendasikan untuk printer thermal", fontSize = 9.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = highContrast,
                        onCheckedChange = { highContrast = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyan.copy(alpha = 0.3f)
                        )
                    )
                }

                if (isSimulatingPrint) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menghubungkan & mencetak via Bluetooth...", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                } else if (printSuccess) {
                    Text(
                        text = "✓ Sukses mencetak ke printer thermal Bluetooth!",
                        color = SoftTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSimulatingPrint = true
                    printSuccess = false
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(1500)
                        isSimulatingPrint = false
                        printSuccess = true
                        viewModel.triggerNotification(
                            "Cetak QR Selesai 🖨️",
                            "Label QR Code untuk ${product.name} berhasil dicetak ukuran $labelSize."
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftTeal),
                enabled = !isSimulatingPrint
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                    Text("Cetak QR Label", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSimulatingPrint) {
                Text("Tutup", color = Color.White)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFF0B0F19)
    )
}

@Composable
fun ProductGalleryDialog(
    product: Product,
    onDismiss: () -> Unit
) {
    val urls = remember(product) {
        product.imageUrls.split(",").filter { it.isNotBlank() }
    }
    var activeIndex by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Foto Produk - ${product.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Image Display
                if (urls.isNotEmpty() && activeIndex in urls.indices) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = urls[activeIndex],
                            contentDescription = "Active image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                }

                // Thumbnail row for navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    urls.forEachIndexed { index, url ->
                        val isSelected = index == activeIndex
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { activeIndex = index }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Thumbnail $index",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", fontWeight = FontWeight.Bold, color = NeonCyan)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFF0B0F19)
    )
}
