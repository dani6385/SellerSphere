package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.QrCode
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.Product
import com.example.data.model.SaleTransaction
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SoftTeal
import com.example.ui.theme.WarmOrange
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.ShopsphereOrder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToInventory: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToSlides: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val lowStockList by viewModel.lowStockProducts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val todayTarget by viewModel.todayTarget.collectAsState()
    val shopsphereOrders by viewModel.shopsphereOrders.collectAsState()

    var showTargetDialog by remember { mutableStateOf(false) }
    var targetInputString by remember { mutableStateOf("") }

    // Calculate today's sales
    val todaySalesTotal = remember(transactions) { viewModel.getTodaySalesTotal() }
    val todayProfitTotal = remember(transactions) {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        transactions.filter { it.timestamp in todayStart..todayEnd }.sumOf { it.totalProfit }
    }

    val targetValue = todayTarget?.targetAmount ?: 1000000.0
    val targetProgress = if (targetValue > 0) (todaySalesTotal / targetValue).coerceIn(0.0, 1.0) else 1.0
    val targetPercentage = (targetProgress * 100).toInt()

    LaunchedEffect(Unit) {
        viewModel.loadTodayTarget()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToSlides() }
                    .testTag("home_hero_banner_box")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Seller Sphere Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC090D1A)),
                                startY = 100f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "SS Seller Sphere",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time Store Intelligence Pro",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Low stock notification bar
        if (lowStockList.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToInventory() }
                        .testTag("low_stock_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peringatan",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Peringatan Stok Menipis! (${lowStockList.size} Produk)",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Ketuk untuk melihat detail barang di inventaris.",
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Daily Target Progress Indicator Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Reminder",
                                tint = WarmOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Target Penjualan Harian",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "Ubah",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable {
                                    targetInputString = targetValue.toInt().toString()
                                    showTargetDialog = true
                                }
                                .testTag("edit_target_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar
                    val animatedProgress by animateFloatAsState(
                        targetValue = targetProgress.toFloat(),
                        animationSpec = tween(durationMillis = 800)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E3E66))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(16.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(WarmOrange, NeonCyan)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${viewModel.formatRupiah(todaySalesTotal)} / ${viewModel.formatRupiah(targetValue)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$targetPercentage%",
                            fontWeight = FontWeight.Bold,
                            color = if (targetPercentage >= 100) SoftTeal else WarmOrange,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Motivation Quote
                    val motivationText = when {
                        todaySalesTotal == 0.0 -> "Semangat! Mulai hari ini dengan menambahkan penjualan pertama Anda. Target Anda hari ini adalah ${viewModel.formatRupiah(targetValue)}."
                        targetPercentage < 50 -> "Anda sudah mencapai $targetPercentage% dari target hari ini. Terus maju, sisa ${viewModel.formatRupiah(targetValue - todaySalesTotal)} lagi!"
                        targetPercentage < 100 -> "Hampir sampai! $targetPercentage% target tercapai. Tambah beberapa transaksi lagi untuk mencapai sukses hari ini!"
                        else -> "Luar biasa! Target penjualan hari ini TELAH TERCAPAI ($targetPercentage%). Pertahankan kinerja hebat ini! 🎉"
                    }

                    Text(
                        text = motivationText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Daily Financial Summary Indicators (Adapted to SS Shopsphere Order Pickups)
        item {
            val todayOrders = remember(shopsphereOrders) { shopsphereOrders.filter { it.dayIndex == 6 } }
            val awaitingPickupCount = remember(todayOrders) { todayOrders.count { it.status != "Selesai Diambil" } }
            val pickedUpCount = remember(todayOrders) { todayOrders.count { it.status == "Selesai Diambil" } }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(WarmOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Belum Diambil",
                                    tint = WarmOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Belum Diambil", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$awaitingPickupCount Paket",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = WarmOrange
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SoftTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selesai Diambil",
                                    tint = SoftTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Selesai Diambil", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$pickedUpCount Paket",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SoftTeal
                        )
                    }
                }
            }
        }

        // Interactive Weekly Order Graph Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Statistik Pengambilan Pesanan Toko",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Ketuk hari untuk detail paket masuk & pengambilan oleh pembeli",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw Interactive Chart
                    ShopsphereWeeklyOrderChart(orders = shopsphereOrders, viewModel = viewModel, onNavigateToChat = onNavigateToChat)
                }
            }
        }

        // Main Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToTransactions,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("nav_pos_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Kasir (POS)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }

                Button(
                    onClick = onNavigateToInventory,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("nav_inventory_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ShowChart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Kelola Barang", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        }
    }

    // Daily Sales Target Editing Dialog
    if (showTargetDialog) {
        Dialog(onDismissRequest = { showTargetDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Atur Target Penjualan Harian",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = targetInputString,
                        onValueChange = { targetInputString = it.filter { c -> c.isDigit() } },
                        label = { Text("Target Rp") },
                        prefix = { Text("Rp ") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_input_field")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Batal",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showTargetDialog = false }
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Simpan",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    val amt = targetInputString.toDoubleOrNull() ?: 0.0
                                    viewModel.updateTodayTarget(amt)
                                    showTargetDialog = false
                                }
                                .padding(8.dp)
                                .testTag("save_target_dialog_button")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShopsphereWeeklyOrderChart(orders: List<ShopsphereOrder>, viewModel: AppViewModel, onNavigateToChat: (String) -> Unit) {
    val daysData = remember(orders) {
        val sdfLabel = SimpleDateFormat("E", Locale("in", "ID")) // Mon, Tue...
        val sdfDate = SimpleDateFormat("dd/MM", Locale.getDefault())

        val list = mutableListOf<Triple<String, String, DayOrderStats>>()

        for (i in 0..6) {
            val checkCalendar = Calendar.getInstance()
            checkCalendar.add(Calendar.DAY_OF_YEAR, - (6 - i))
            val dateStr = sdfDate.format(checkCalendar.time)
            val dayLabel = sdfLabel.format(checkCalendar.time)

            val dayOrders = orders.filter { it.dayIndex == i }
            val completed = dayOrders.count { it.status == "Selesai Diambil" }
            val awaiting = dayOrders.count { it.status != "Selesai Diambil" }

            list.add(Triple(dayLabel, dateStr, DayOrderStats(completed, awaiting)))
        }
        list
    }

    var selectedIndex by remember { mutableStateOf(6) } // Default select today

    val maxOrders = remember(daysData) {
        val maxVal = daysData.maxOfOrNull { it.third.total } ?: 5
        if (maxVal == 0) 5 else maxVal
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(SoftTeal, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Selesai Diambil", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.size(10.dp).background(WarmOrange, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Belum Diambil", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            val selectedDayStats = daysData[selectedIndex].third
            Text(
                text = "${selectedDayStats.total} Pesanan",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(daysData) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val colWidth = width / 7f
                            val tappedCol = (offset.x / colWidth).toInt().coerceIn(0, 6)
                            selectedIndex = tappedCol
                        }
                    }
                    .testTag("shopsphere_weekly_order_chart")
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val colWidth = canvasWidth / 7f
                val topPadding = 20f
                val bottomPadding = 20f
                val chartHeight = canvasHeight - topPadding - bottomPadding
                val barWidth = 16.dp.toPx()

                // Draw horizontal grid lines
                for (gridIdx in 0..3) {
                    val yLine = topPadding + (gridIdx / 3f) * chartHeight
                    drawLine(
                        color = Color(0xFF2E3E66).copy(alpha = 0.3f),
                        start = Offset(0f, yLine),
                        end = Offset(canvasWidth, yLine),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw stacked bars
                for (i in 0..6) {
                    val stats = daysData[i].third
                    val x = colWidth * i + colWidth / 2f
                    
                    val isSelected = i == selectedIndex

                    if (stats.total > 0) {
                        val compHeight = (stats.completed.toFloat() / maxOrders) * chartHeight
                        val awatHeight = (stats.awaiting.toFloat() / maxOrders) * chartHeight

                        // Completed bar (SoftTeal) at the bottom
                        val compTopY = canvasHeight - bottomPadding - compHeight
                        drawRect(
                            color = if (isSelected) SoftTeal else SoftTeal.copy(alpha = 0.7f),
                            topLeft = Offset(x - barWidth / 2f, compTopY),
                            size = Size(barWidth, compHeight)
                        )

                        // Awaiting bar (WarmOrange) stacked on top of completed bar
                        val awatTopY = compTopY - awatHeight
                        drawRect(
                            color = if (isSelected) WarmOrange else WarmOrange.copy(alpha = 0.7f),
                            topLeft = Offset(x - barWidth / 2f, awatTopY),
                            size = Size(barWidth, awatHeight)
                        )
                    }

                    // Draw halo/indicator on top or around the selected bar
                    if (isSelected) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.15f),
                            topLeft = Offset(colWidth * i + 2.dp.toPx(), topPadding - 10f),
                            size = Size(colWidth - 4.dp.toPx(), chartHeight + 20f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days labels below chart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0..6) {
                val isSelected = i == selectedIndex
                Column(
                    modifier = Modifier
                        .width(42.dp)
                        .clickable { selectedIndex = i },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = daysData[i].first,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = daysData[i].second,
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detail list of orders for the selected day
        val selectedDateStr = daysData[selectedIndex].second
        val selectedDayName = daysData[selectedIndex].first
        val dayOrders = remember(orders, selectedIndex) {
            orders.filter { it.dayIndex == selectedIndex }
        }

        Text(
            text = "Daftar Paket Hari $selectedDayName ($selectedDateStr)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        if (dayOrders.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada orderan masuk untuk tanggal ini.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dayOrders.forEach { order ->
                    OrderPickupItem(order = order, viewModel = viewModel, onNavigateToChat = onNavigateToChat)
                }
            }
        }
    }
}

data class DayOrderStats(val completed: Int, val awaiting: Int) {
    val total: Int get() = completed + awaiting
}

@Composable
fun OrderPickupItem(order: ShopsphereOrder, viewModel: AppViewModel, onNavigateToChat: (String) -> Unit) {
    val isPickedUp = order.status == "Selesai Diambil"
    var showVerificationDialog by remember { mutableStateOf(false) }

    if (showVerificationDialog) {
        OrderVerificationDialog(
            order = order,
            onDismiss = { showVerificationDialog = false },
            onVerifySuccess = {
                viewModel.confirmOrderPickup(order.id)
                showVerificationDialog = false
            }
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPickedUp) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.id,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isPickedUp) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Pembeli: ${order.customerName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = {
                                viewModel.activeChatBuyerName.value = order.customerName
                                onNavigateToChat(order.customerName)
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("chat_buyer_button_${order.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat Pembeli ${order.customerName}",
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Status Badge
                Surface(
                    color = when (order.status) {
                        "Selesai Diambil" -> SoftTeal.copy(alpha = 0.15f)
                        "Siap Diambil" -> NeonCyan.copy(alpha = 0.15f)
                        else -> WarmOrange.copy(alpha = 0.15f)
                    },
                    contentColor = when (order.status) {
                        "Selesai Diambil" -> SoftTeal
                        "Siap Diambil" -> NeonCyan
                        else -> WarmOrange
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${order.productName} x${order.quantity}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = viewModel.formatRupiah(order.totalAmount),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "No. HP Pembeli: ${order.courierPhone}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            if (order.status == "Siap Diambil") {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SoftTeal,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Verifikasi Barcode & PIN Aman Aktif",
                        fontSize = 10.sp,
                        color = SoftTeal,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Packing Instruction Prompt
            if (order.status == "Perlu Dipacking") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmOrange.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarmOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Silakan lakukan packing untuk pesanan ini.",
                        fontSize = 11.sp,
                        color = WarmOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action Buttons
            if (!isPickedUp) {
                if (order.status == "Perlu Dipacking") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.finishPacking(order.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Barang Selesai, Silakan Ambil",
                            fontSize = 12.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (order.status == "Siap Diambil") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Call Courier
                        Button(
                            onClick = { viewModel.callCourier(order.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.3f).height(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hubungi Pembeli", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Print Bill / Receipt / Nota
                        Button(
                            onClick = { viewModel.printOrderLabel(order.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f).height(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cetak Nota", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Confirm pickup
                        Button(
                            onClick = { showVerificationDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTeal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.8f).height(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Konfirmasi Diambil", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderVerificationDialog(
    order: ShopsphereOrder,
    onDismiss: () -> Unit,
    onVerifySuccess: () -> Unit
) {
    var activeTab by remember { mutableStateOf(1) } // Default to real camera scan
    var inputCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Verifikasi Pengambilan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Pesanan: ${order.id}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "Pelanggan: ${order.customerName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Produk: ${order.productName} x${order.quantity}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = { activeTab = 1 },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (activeTab == 1) Color(0xFF334155) else Color.Transparent
                        ),
                        modifier = Modifier.weight(1.2f).height(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Kamera Real-time 📷",
                            color = if (activeTab == 1) NeonCyan else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = { activeTab = 0 },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (activeTab == 0) Color(0xFF334155) else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f).height(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Manual & Simulasi",
                            color = if (activeTab == 0) NeonCyan else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (activeTab == 0) {
                    // Manual UI
                    Text(
                        text = "Gunakan barcode pembeli atau ketik kode verifikasi 6-digit untuk memastikan penyerahan pesanan yang sah.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Simulated Barcode Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Drawing realistic vertical lines for Barcode
                            Row(
                                modifier = Modifier
                                    .height(50.dp)
                                    .fillMaxWidth(0.85f),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val widths = listOf(2, 4, 1, 3, 2, 5, 1, 3, 2, 4, 1, 2, 4, 1, 3, 2, 3, 1, 4, 2)
                                widths.forEach { w ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(w.dp)
                                            .background(Color.Black)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "* ${order.verificationCode} *",
                                color = Color.Black,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }

                        // Scan Laser Animation overlay
                        if (isScanning) {
                            val infiniteTransition = rememberInfiniteTransition()
                            val laserOffset by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 80f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(2.dp)
                                    .background(Color.Red)
                                    .offset(y = (-40 + laserOffset.toInt()).dp)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Red.copy(alpha = 0.08f))
                            )
                        }
                    }

                    // Scanning status or verification code field
                    if (isScanning) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Memindai Barcode Pembeli...",
                                fontSize = 12.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = {
                                if (it.length <= 6) {
                                    inputCode = it
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Kode Verifikasi (6 Digit)") },
                            placeholder = { Text("Contoh: ${order.verificationCode}") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = errorMessage.isNotEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    // Quick Scan helper
                    Button(
                        onClick = {
                            isScanning = true
                            errorMessage = ""
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(1800)
                                isScanning = false
                                inputCode = order.verificationCode
                            }
                        },
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simulasi Scan Barcode 📸",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                } else {
                    // Camera Live UI
                    val context = LocalContext.current
                    var hasCameraPermission by remember {
                        mutableStateOf(
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        )
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        hasCameraPermission = isGranted
                        if (!isGranted) {
                            errorMessage = "Izin Kamera diperlukan untuk memindai QR code."
                        }
                    }

                    if (!hasCameraPermission) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Yellow,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Izin Kamera Dibutuhkan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Aktifkan kamera untuk memindai QR / Barcode secara langsung.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTeal),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Berikan Izin Kamera", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Live Scanner Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            CameraPreview(
                                modifier = Modifier.fillMaxSize(),
                                onBarcodeDetected = { scannedCode ->
                                    val cleaned = scannedCode.trim()
                                    if (cleaned == order.verificationCode) {
                                        onVerifySuccess()
                                    } else {
                                        errorMessage = "Terdeteksi kode: '$cleaned'. (Pesanan ini memerlukan: '${order.verificationCode}')"
                                    }
                                }
                            )

                            // Scanning overlay borders
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 3.dp.toPx()
                                val cornerLength = 20.dp.toPx()
                                val padding = 15.dp.toPx()
                                val rectSize = size.height - (padding * 2)
                                val left = (size.width - rectSize) / 2
                                val top = padding
                                val right = left + rectSize
                                val bottom = top + rectSize

                                // Top-Left
                                drawLine(color = NeonCyan, start = Offset(left, top), end = Offset(left + cornerLength, top), strokeWidth = strokeWidth)
                                drawLine(color = NeonCyan, start = Offset(left, top), end = Offset(left, top + cornerLength), strokeWidth = strokeWidth)

                                // Top-Right
                                drawLine(color = NeonCyan, start = Offset(right, top), end = Offset(right - cornerLength, top), strokeWidth = strokeWidth)
                                drawLine(color = NeonCyan, start = Offset(right, top), end = Offset(right, top + cornerLength), strokeWidth = strokeWidth)

                                // Bottom-Left
                                drawLine(color = NeonCyan, start = Offset(left, bottom), end = Offset(left + cornerLength, bottom), strokeWidth = strokeWidth)
                                drawLine(color = NeonCyan, start = Offset(left, bottom), end = Offset(left, bottom - cornerLength), strokeWidth = strokeWidth)

                                // Bottom-Right
                                drawLine(color = NeonCyan, start = Offset(right, bottom), end = Offset(right - cornerLength, bottom), strokeWidth = strokeWidth)
                                drawLine(color = NeonCyan, start = Offset(right, bottom), end = Offset(right, bottom - cornerLength), strokeWidth = strokeWidth)
                            }

                            // Moving scan line animation
                            val infiniteTransition = rememberInfiniteTransition()
                            val laserOffset by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 160f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(2.dp)
                                    .background(Color.Red)
                                    .offset(y = (-80 + laserOffset.toInt()).dp)
                            )
                        }

                        Text(
                            text = "Arahkan kamera ke barcode/QR Code pembeli untuk verifikasi otomatis.",
                            fontSize = 11.sp,
                            color = NeonCyan,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            if (activeTab == 0) {
                Button(
                    onClick = {
                        if (inputCode == order.verificationCode) {
                            onVerifySuccess()
                        } else {
                            errorMessage = "Kode verifikasi salah! Cocokkan barcode atau ketik 6-digit kode pembeli."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTeal),
                    enabled = !isScanning && inputCode.length == 6
                ) {
                    Text("Konfirmasi Penyerahan", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isScanning) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, BarcodeAnalyzer { code ->
                            onBarcodeDetected(code)
                        })
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Failed to bind camera use cases", e)
                }
            }, executor)
            previewView
        },
        modifier = modifier
    )
}

class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            onBarcodeDetected(rawValue)
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    // Suppress error logs in frame-by-frame analysis
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
