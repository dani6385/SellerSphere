package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SoftTeal
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.RadiantRose
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.ShopsphereOrder

@Composable
fun TransactionScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val shopsphereOrders by viewModel.shopsphereOrders.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Kasir POS, 1: Orderan Masuk

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Custom Segmented Pill Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val pendingPackingCount = shopsphereOrders.count { it.status == "Perlu Dipacking" }
            val tabs = listOf(
                "Kasir POS",
                if (pendingPackingCount > 0) "Orderan Masuk ($pendingPackingCount)" else "Orderan Masuk"
            )
            tabs.forEachIndexed { index, title ->
                val isSelected = activeTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonCyan else Color.Transparent)
                        .clickable { activeTab = index }
                        .padding(vertical = 10.dp)
                        .testTag("transaction_tab_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) Color(0xFF090D1A) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (activeTab == 0) {
            var searchQuery by remember { mutableStateOf("") }
            var selectedPaymentMethod by remember { mutableStateOf("Tunai") } // Tunai, QRIS, Transfer
            var showBottomSheet by remember { mutableStateOf(false) }

            // Filter available products
            val filteredProducts = remember(products, searchQuery) {
                products.filter { p ->
                    p.name.contains(searchQuery, ignoreCase = true) || p.sku.contains(searchQuery, ignoreCase = true)
                }
            }

            val cartTotal = remember(cart) {
                cart.entries.sumOf { (product, qty) -> product.sellingPrice * qty }
            }
            val totalItems = remember(cart) { cart.values.sum() }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari barang untuk dijual...", fontSize = 12.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("pos_search_field")
                    )

                    // Products Grid (Spans full width now!)
                    if (filteredProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Produk tidak ditemukan",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp), // Extra padding at bottom to make sure products aren't blocked by the floating bar
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(filteredProducts) { product ->
                                PosProductCard(
                                    product = product,
                                    cartQty = cart[product] ?: 0,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }

                // Beautiful, Interactive, Floating Bottom Cart Bar (with spring pop/bounce animation on content changes!)
                val cartButtonScale = remember { androidx.compose.animation.core.Animatable(1f) }

                LaunchedEffect(totalItems) {
                    if (totalItems > 0) {
                        cartButtonScale.animateTo(
                            targetValue = 1.08f,
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                            )
                        )
                        cartButtonScale.animateTo(
                            targetValue = 1.0f,
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                            )
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = cart.isNotEmpty(),
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = androidx.compose.animation.core.spring()
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = androidx.compose.animation.core.spring()
                    ) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .graphicsLayer(
                            scaleX = cartButtonScale.value,
                            scaleY = cartButtonScale.value
                        )
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2E)),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable { showBottomSheet = true }
                            .testTag("floating_cart_bar")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Dynamic badge showing item count
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = totalItems.toString(),
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Keranjang Belanja",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = viewModel.formatRupiah(cartTotal),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = NeonCyan
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SoftTeal)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Transaksi",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Beautiful Modal Bottom Sheet for Transaction Checkout Details
            if (showBottomSheet) {
                @OptIn(ExperimentalMaterial3Api::class)
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = Color(0xFF0F172A),
                    dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Keranjang Belanja (${totalItems} Barang)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Hapus Semua",
                                color = RadiantRose,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.clearCart()
                                        showBottomSheet = false
                                    }
                                    .testTag("clear_cart_button")
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                        ) {
                            items(cart.entries.toList()) { (product, qty) ->
                                CartItemRow(product = product, quantity = qty, viewModel = viewModel)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        Text(
                            text = "Metode Pembayaran",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val methods = listOf("Tunai", "QRIS", "Transfer")
                            methods.forEach { method ->
                                val isSelected = selectedPaymentMethod == method
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedPaymentMethod = method }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = method,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Bayar",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = viewModel.formatRupiah(cartTotal),
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = NeonCyan
                            )
                        }

                        Button(
                            onClick = {
                                showBottomSheet = false
                                viewModel.checkout(selectedPaymentMethod)
                            },
                            enabled = cart.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftTeal,
                                contentColor = Color.Black,
                                disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("checkout_button")
                        ) {
                            Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bayar & Cetak Nota", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        } else {
            // OrdersTabContent
            OrdersTabContent(viewModel = viewModel, orders = shopsphereOrders)
        }
    }
}

@Composable
fun PosProductCard(
    product: Product,
    cartQty: Int,
    viewModel: AppViewModel
) {
    val isOutOfStock = product.stock <= 0
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(cartQty) {
        if (cartQty > 0) {
            scale.animateTo(
                targetValue = 1.08f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            )
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                )
            )
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2E)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value
            )
            .clickable(enabled = !isOutOfStock) { viewModel.addToCart(product) }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isOutOfStock) Color.White.copy(alpha = 0.5f) else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "SKU: ${product.sku}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                if (cartQty > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cartQty.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.formatRupiah(product.sellingPrice),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = NeonCyan
                )

                Text(
                    text = if (isOutOfStock) "Habis" else "Stok: ${product.stock}",
                    fontSize = 10.sp,
                    color = if (isOutOfStock) RadiantRose else if (product.isLowStock) WarmOrange else SoftTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    product: Product,
    quantity: Int,
    viewModel: AppViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131A2E), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = viewModel.formatRupiah(product.sellingPrice * quantity),
                fontSize = 10.sp,
                color = NeonCyan,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { viewModel.removeFromCart(product) },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Kurang", tint = Color.White, modifier = Modifier.size(12.dp))
            }

            Text(
                text = quantity.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { viewModel.addToCart(product) },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah", tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun OrdersTabContent(viewModel: AppViewModel, orders: List<ShopsphereOrder>) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") } // Semua, Perlu Dipacking, Siap Diambil, Selesai Diambil

    // Filtered orders list
    val filteredOrders = remember(orders, searchQuery, selectedFilter) {
        orders.filter { order ->
            val matchesSearch = order.id.contains(searchQuery, ignoreCase = true) ||
                    order.customerName.contains(searchQuery, ignoreCase = true) ||
                    order.productName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = selectedFilter == "Semua" || order.status == selectedFilter

            matchesSearch && matchesFilter
        }
    }

    // Statistics
    val totalCount = orders.size
    val packingCount = orders.count { it.status == "Perlu Dipacking" }
    val completedCount = orders.count { it.status == "Selesai Diambil" }

    Column(modifier = Modifier.fillMaxSize()) {
        // Stats Cards Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Total Orders Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Total Order", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$totalCount Pesanan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NeonCyan)
                }
            }

            // Awaiting Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Perlu Packing", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$packingCount Paket", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmOrange)
                }
            }

            // Completed Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Selesai Diambil", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$completedCount Paket", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SoftTeal)
                }
            }
        }

        // Search Bar & Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari order ID, nama pembeli...", fontSize = 12.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("orders_search_field")
            )
        }

        // Filter chips list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf("Semua", "Perlu Dipacking", "Siap Diambil", "Selesai Diambil")
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Orders list
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada orderan ditemukan",
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
                items(filteredOrders) { order ->
                    OrderPickupItem(order = order, viewModel = viewModel)
                }
            }
        }
    }
}
