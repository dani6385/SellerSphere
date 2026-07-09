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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Panel: Operations & Quick Add Button (30% width)
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Manajemen Inventaris",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Kelola data produk, stok, harga, serta cetak label harga dan promo barcode secara mandiri.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Add Product Button
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("add_product_trigger")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Barang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Import/Export CSV Button
                    OutlinedButton(
                        onClick = { showCsvDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ImportExport, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Impor / Ekspor CSV", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Statistics Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ringkasan Stok",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Jenis Produk", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${products.size} Item", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NeonCyan)
                    }

                    val totalStock = products.sumOf { it.stock }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Unit Tersedia", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalStock Unit", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SoftTeal)
                    }

                    val lowStockCount = products.count { it.isLowStock }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Barang Stok Menipis", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$lowStockCount Item",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (lowStockCount > 0) WarmOrange else SoftTeal
                        )
                    }

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
                                    fontSize = 10.sp,
                                    color = WarmOrange,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Estimated Revenue Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("revenue_summary_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SoftTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = SoftTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Estimasi Nilai Inventaris",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val totalRevenue = products.sumOf { it.sellingPrice * it.stock }
                    val totalCost = products.sumOf { it.purchasePrice * it.stock }
                    val potentialProfit = totalRevenue - totalCost

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Total Estimasi Pendapatan",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = viewModel.formatRupiah(totalRevenue),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = NeonCyan,
                            modifier = Modifier.testTag("total_estimated_revenue_text")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimasi Modal (Cost)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = viewModel.formatRupiah(totalCost),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Potensi Keuntungan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = viewModel.formatRupiah(potentialProfit),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = if (potentialProfit >= 0) SoftTeal else RadiantRose
                            )
                        }
                    }

                    if (products.any { it.stock > 0 && it.sellingPrice == 0.0 }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WarmOrange.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarmOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Beberapa barang aktif belum diatur harga jualnya.",
                                    fontSize = 9.sp,
                                    color = WarmOrange,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right Panel: Search, Category Filters, and Products List (70% width)
        Column(
            modifier = Modifier
                .weight(2.5f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            var sortMenuExpanded by remember { mutableStateOf(false) }
            val sortOptions = listOf(
                "Alphabetical" to "Alphabetical",
                "Stock Level (Low to High)" to "Stock Level (Low to High)",
                "Price" to "Price"
            )

            // Search and Category Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari berdasarkan nama atau SKU...", fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("inventory_search_field")
                )

                // Sort Dropdown Button
                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    OutlinedButton(
                        onClick = { sortMenuExpanded = true },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("inventory_sort_button"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Urutkan",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val activeLabel = sortOptions.firstOrNull { it.first == selectedSortKey }?.second ?: "Alphabetical"
                        Text(
                            text = activeLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
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

            // Category filter row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(5).forEach { category ->
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
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Products List
            if (sortedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada produk di dalam inventaris",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
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
    }

    // Add Product Dialog
    if (showAddDialog) {
        ProductFormDialog(
            title = "Tambah Produk Baru",
            onDismiss = { showAddDialog = false },
            onSave = { name, sku, stock, purchase, sell, cat, threshold ->
                viewModel.addProduct(name, sku, stock, purchase, sell, cat, threshold)
                showAddDialog = false
            }
        )
    }

    // Edit Product Dialog
    editingProduct?.let { product ->
        ProductFormDialog(
            title = "Ubah Produk",
            product = product,
            onDismiss = { editingProduct = null },
            onSave = { name, sku, stock, purchase, sell, cat, threshold ->
                viewModel.updateProduct(
                    product.copy(
                        name = name,
                        sku = sku,
                        stock = stock,
                        purchasePrice = purchase,
                        sellingPrice = sell,
                        category = cat,
                        minStockThreshold = threshold
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
                Column(modifier = Modifier.weight(1f)) {
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
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Double, Double, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var stockText by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var purchaseText by remember { mutableStateOf(product?.purchasePrice?.toString() ?: "") }
    var sellText by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Umum") }
    var thresholdText by remember { mutableStateOf(product?.minStockThreshold?.toString() ?: "5") }

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
                        onSave(name, sku, stockVal, purchaseVal, sellVal, category, thresholdVal)
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
