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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Side: Financial Aggregations & Logs (55% width)
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Laporan Keuangan Otomatis",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
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
                            Text(text = "Total Omzet", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Text(text = viewModel.formatRupiah(totalSales), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Laba Bersih", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Text(text = viewModel.formatRupiah(totalProfit), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SoftTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Pengeluaran (HPP)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Text(text = viewModel.formatRupiah(totalExpenses), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Rata-rata Penjualan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Text(text = viewModel.formatRupiah(averageTransactionVal), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                                .height(32.dp)
                                .testTag("export_excel_button")
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel Produk", fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                                .height(32.dp)
                                .testTag("export_transactions_button")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel Transaksi", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Transaction History list
            Text(
                text = "Riwayat Penjualan (${transactionCount})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi terekam", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
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

        // Right Side: Digital Invoice Printout Preview Sheet (45% width)
        Column(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Lembar Nota Digital",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // White Formal Print Sheet
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .width(220.dp)
                    .weight(1f)
                    .border(1.dp, Color.Black.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (selectedTransactionForInvoice == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Pilih transaksi\nuntuk melihat lembar nota",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
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
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Nota Penjualan Resmi",
                                    color = Color.Gray,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Trans: #${activeTrans.id}", color = Color.Black, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                    Text(text = formattedDate, color = Color.Black, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                }
                            }

                            // Divider
                            Divider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                            // Items representation (we draw mock item details visually)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Item", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Total", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }

                                Divider(color = Color.Black.copy(alpha = 0.2f), thickness = 0.5.dp)

                                // Mock visual list of transaction items (since details are derived or standard)
                                val itemsList = listOf(
                                    Pair("Item SSphere Pro", activeTrans.totalAmount)
                                )

                                itemsList.forEach { (name, amt) ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            text = name,
                                            color = Color.Black,
                                            fontSize = 7.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.width(110.dp)
                                        )
                                        Text(text = viewModel.formatRupiah(amt), color = Color.Black, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
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
                                    Text(text = "TOTAL BAYAR:", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = viewModel.formatRupiah(activeTrans.totalAmount),
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Terima kasih atas kunjungan Anda!",
                                    color = Color.Gray,
                                    fontSize = 7.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.triggerNotification("PDF Laporan Diekspor", "PDF laporan untuk Transaksi #${selectedTransactionForInvoice?.id} berhasil disimpan.")
                },
                enabled = selectedTransactionForInvoice != null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(40.dp)
                    .testTag("action_print_invoice_button")
            ) {
                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cetak Nota / PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
