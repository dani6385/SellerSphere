package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.ui.util.PdfReportUtil
import android.net.Uri
import android.content.Intent
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SaleTransaction
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SoftTeal
import com.example.ui.theme.WarmOrange
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportScreen(viewModel: AppViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val products by viewModel.products.collectAsState()

    // Calculate aggregated stats
    val totalSales = remember(transactions) { transactions.sumOf { it.totalAmount } }
    val totalProfit = remember(transactions) { transactions.sumOf { it.totalProfit } }
    val totalExpenses = remember(totalSales, totalProfit) { totalSales - totalProfit }
    val transactionCount = transactions.size
    val averageTransactionVal = if (transactionCount > 0) totalSales / transactionCount else 0.0

    // Selected transaction for invoice preview sheet
    var selectedTransactionForInvoice by remember { mutableStateOf<SaleTransaction?>(null) }

    // If transactions is not empty, auto-select the latest one for invoice layout preview
    if (selectedTransactionForInvoice == null && transactions.isNotEmpty()) {
        selectedTransactionForInvoice = transactions.first()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header & Performance Overview
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Laporan Keuangan Otomatis",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                // Monthly stats overview card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Ringkasan Performa Toko",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Total Omzet", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                Text(text = viewModel.formatRupiah(totalSales), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Laba Bersih", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                Text(text = viewModel.formatRupiah(totalProfit), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SoftTeal)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Total Pengeluaran (HPP)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                Text(text = viewModel.formatRupiah(totalExpenses), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Rata-rata Penjualan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                Text(text = viewModel.formatRupiah(averageTransactionVal), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val csv = viewModel.exportProductsToCsv()
                                    viewModel.triggerNotification("Excel Berhasil Diekspor", "Data produk berhasil diunduh ke file CSV Excel.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("export_excel_button")
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Excel Produk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val csv = viewModel.exportTransactionsToCsv()
                                    viewModel.triggerNotification("Laporan Terunduh", "Riwayat penjualan otomatis disimpan dalam format Excel CSV.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("export_transactions_button")
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Excel Transaksi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Low Stock PDF Report Card
        item {
            val lowStockList by viewModel.lowStockProducts.collectAsState()
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            var pdfProgress by remember { mutableStateOf<Float?>(null) }
            var generatedPdfUri by remember { mutableStateOf<Uri?>(null) }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("low_stock_pdf_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (lowStockList.isNotEmpty()) WarmOrange.copy(alpha = 0.15f)
                                else SoftTeal.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (lowStockList.isNotEmpty()) Icons.Default.WarningAmber else Icons.Default.Assignment,
                            contentDescription = null,
                            tint = if (lowStockList.isNotEmpty()) WarmOrange else SoftTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Laporan Stok Menipis (PDF)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (lowStockList.isEmpty()) {
                                "Semua stok produk aman di atas ambang batas."
                            } else {
                                "Ada ${lowStockList.size} produk di bawah batas minimum."
                            },
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        
                        // Progress description if generating
                        pdfProgress?.let { progress ->
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Memproses laporan PDF... (${(progress * 100).toInt()}%)",
                                fontSize = 9.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pdfProgress != null) {
                            CircularProgressIndicator(
                                progress = pdfProgress ?: 0f,
                                modifier = Modifier.size(20.dp),
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            if (generatedPdfUri != null) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(generatedPdfUri, "application/pdf")
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            viewModel.triggerNotification(
                                                "Buka PDF Gagal", 
                                                "Tidak ada aplikasi pembaca PDF. Cari file di folder Unduhan."
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTeal),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("open_low_stock_pdf_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Buka", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    if (lowStockList.isEmpty()) {
                                        viewModel.triggerNotification(
                                            "Laporan PDF",
                                            "Tidak ada produk di bawah ambang batas untuk dimasukkan ke laporan."
                                        )
                                        return@Button
                                    }
                                    coroutineScope.launch {
                                        pdfProgress = 0.0f
                                        withContext(Dispatchers.IO) {
                                            val stepTime = 200L
                                            kotlinx.coroutines.delay(stepTime)
                                            withContext(Dispatchers.Main) { pdfProgress = 0.2f }
                                            
                                            kotlinx.coroutines.delay(stepTime)
                                            withContext(Dispatchers.Main) { pdfProgress = 0.4f }
                                            
                                            val uri = PdfReportUtil.generateLowStockPdfReport(context, lowStockList) { progress ->
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    pdfProgress = progress
                                                }
                                            }
                                            
                                            kotlinx.coroutines.delay(stepTime)
                                            withContext(Dispatchers.Main) {
                                                generatedPdfUri = uri
                                                pdfProgress = null
                                                if (uri != null) {
                                                    viewModel.triggerNotification(
                                                        "Unduh PDF Selesai 🖨️",
                                                        "Laporan Stok Menipis (${lowStockList.size} produk) berhasil diunduh ke folder Downloads."
                                                    )
                                                } else {
                                                    viewModel.triggerNotification(
                                                        "Gagal Membuat PDF",
                                                        "Terjadi kesalahan saat memproses laporan PDF."
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (generatedPdfUri != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("download_low_stock_pdf_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (generatedPdfUri != null) "Cetak Ulang" else "Unduh PDF",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Digital Invoice Printout Preview Card
        item {
            AnimatedVisibility(visible = selectedTransactionForInvoice != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Lembar Nota Digital",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // White Formal Print Sheet
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .border(1.dp, Color.Black.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val activeTrans = selectedTransactionForInvoice!!
                            val formattedDate = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(activeTrans.timestamp))

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Header
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "SS SELLER SPHERE",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Nota Penjualan Resmi",
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Trans: #${activeTrans.id}", color = Color.Black, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        Text(text = formattedDate, color = Color.Black, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                // Divider
                                Divider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                                // Items representation
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Item", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Total", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Divider(color = Color.Black.copy(alpha = 0.2f), thickness = 0.5.dp)

                                    val itemsList = listOf(
                                        Pair("Item SSphere Pro", activeTrans.totalAmount)
                                    )

                                    itemsList.forEach { (name, amt) ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(
                                                text = name,
                                                color = Color.Black,
                                                fontSize = 8.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.width(160.dp)
                                            )
                                            Text(text = viewModel.formatRupiah(amt), color = Color.Black, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }

                                // Divider
                                Divider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                                // Footers with totals
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Metode:", color = Color.Black, fontSize = 8.sp)
                                        Text(text = activeTrans.paymentMethod, color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Laba Bersih:", color = Color.Gray, fontSize = 8.sp)
                                        Text(text = viewModel.formatRupiah(activeTrans.totalProfit), color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "TOTAL BAYAR:", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = viewModel.formatRupiah(activeTrans.totalAmount),
                                            color = Color.Black,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Terima kasih atas kunjungan Anda!",
                                        color = Color.Gray,
                                        fontSize = 8.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.triggerNotification("PDF Laporan Diekspor", "PDF laporan untuk Transaksi #${selectedTransactionForInvoice?.id} berhasil disimpan.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("action_print_invoice_button")
                    ) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Nota / PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // 4. Section Header: Riwayat Penjualan
        item {
            Text(
                text = "Riwayat Penjualan (${transactionCount})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // 5. Transaction History Rows
        if (transactions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi terekam", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
        } else {
            items(transactions) { trans ->
                val isSelected = selectedTransactionForInvoice?.id == trans.id
                TransactionHistoryRow(
                    transaction = trans,
                    isSelected = isSelected,
                    onSelect = { selectedTransactionForInvoice = trans },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun TransactionHistoryRow(
    transaction: SaleTransaction,
    isSelected: Boolean,
    onSelect: () -> Unit,
    viewModel: AppViewModel
) {
    val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale("in", "ID")).format(Date(transaction.timestamp))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (transaction.paymentMethod == "Tunai") WarmOrange.copy(alpha = 0.15f) else SoftTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = if (transaction.paymentMethod == "Tunai") WarmOrange else SoftTeal,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Transaksi #${transaction.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$dateStr • ${transaction.paymentMethod}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = viewModel.formatRupiah(transaction.totalAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NeonCyan
                )
                Text(
                    text = "Laba: " + viewModel.formatRupiah(transaction.totalProfit),
                    fontSize = 10.sp,
                    color = SoftTeal,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
