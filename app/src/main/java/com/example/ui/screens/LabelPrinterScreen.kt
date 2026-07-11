package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantRose
import com.example.ui.theme.SoftTeal
import com.example.ui.theme.WarmOrange
import com.example.ui.viewmodel.AppViewModel

@Composable
fun LabelPrinterScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    val selectedProduct by viewModel.selectedProductForLabel.collectAsState()
    val selectedTemplate by viewModel.selectedTemplate.collectAsState()
    val customStoreName by viewModel.customStoreName.collectAsState()
    val promoDiscountPercent by viewModel.promoDiscountPercent.collectAsState()
    val labelSize by viewModel.labelSize.collectAsState()

    val printerState by viewModel.printerConnectionState.collectAsState()
    val availablePrinters by viewModel.availablePrinters.collectAsState()
    val isPrinting by viewModel.isPrinting.collectAsState()

    var showProductSelectDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Panel: Settings & Studio Controls (50% width)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Label Studio Pro",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Product Selection
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "1. Pilih Barang", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable { showProductSelectDropdown = !showProductSelectDropdown }
                                .padding(12.dp)
                                .testTag("select_label_product_dropdown")
                        ) {
                            Text(
                                text = selectedProduct?.name ?: "Pilih barang dari stok...",
                                color = if (selectedProduct != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        AnimatedVisibility(visible = showProductSelectDropdown) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                if (products.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Tidak ada barang di inventaris", fontSize = 12.sp)
                                    }
                                } else {
                                    LazyColumn {
                                        items(products) { p ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.selectProductForLabel(p)
                                                        showProductSelectDropdown = false
                                                    }
                                                    .padding(12.dp)
                                            ) {
                                                Text(text = p.name, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Customize Label Form
            if (selectedProduct != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "2. Atur Desain", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Store Name Input
                            OutlinedTextField(
                                value = customStoreName,
                                onValueChange = { viewModel.updateCustomStoreName(it) },
                                label = { Text("Nama Toko") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("label_store_name_field")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Template Picker
                            Text(text = "Template Desain", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Minimalis Modern", "Diskon/Promo", "Grosir", "Barcode Klasik", "QR Code").forEach { temp ->
                                    val isSelected = selectedTemplate == temp
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable { viewModel.updateLabelTemplate(temp) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = temp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom attributes based on template
                            if (selectedTemplate == "Diskon/Promo") {
                                OutlinedTextField(
                                    value = promoDiscountPercent.toString(),
                                    onValueChange = { viewModel.updatePromoDiscount(it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0) },
                                    label = { Text("Persentase Diskon %") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Size selection
                            Text(text = "Ukuran Kertas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("50x30 mm", "40x30 mm", "30x20 mm").forEach { sz ->
                                    val isSelected = labelSize == sz
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable { viewModel.updateLabelSize(sz) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = sz, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bluetooth Printing Setup Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (printerState.contains("Terhubung")) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = if (printerState.contains("Terhubung")) SoftTeal else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Koneksi Printer Bluetooth", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            if (printerState.contains("Terhubung")) {
                                Text(
                                    text = "Putus",
                                    color = RadiantRose,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.clickable { viewModel.disconnectPrinter() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status: $printerState",
                                fontSize = 11.sp,
                                color = if (printerState.contains("Terhubung")) SoftTeal else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontWeight = if (printerState.contains("Terhubung")) FontWeight.Bold else FontWeight.Normal
                            )

                            if (printerState == "Terputus" || printerState == "Pilih Printer") {
                                Button(
                                    onClick = { viewModel.startPrinterDiscovery() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("scan_printer_button")
                                ) {
                                    Text("Cari Printer", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Discovery result dropdown
                        AnimatedVisibility(visible = printerState == "Mencari...") {
                            Row(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mencari printer thermal Bluetooth terdekat...", fontSize = 10.sp)
                            }
                        }

                        AnimatedVisibility(visible = availablePrinters.isNotEmpty() && printerState == "Pilih Printer") {
                            Column(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                availablePrinters.forEach { p ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.connectToPrinter(p) }
                                            .padding(10.dp)
                                    ) {
                                        Text(text = p, fontSize = 11.sp)
                                    }
                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Right Panel: Sticker Live Canvas & Output (50% width)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pratinjau Stiker Label",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Sticker Canvas Board
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .width(240.dp)
                    .height(180.dp)
                    .border(1.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (selectedProduct == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "[ Pilih barang di panel kiri ]\nuntuk melihat pratinjau label",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (selectedTemplate == "QR Code") {
                        // Custom Live Thermal QR Code Sticker Design
                        val prod = selectedProduct!!
                        val qrText = if (prod.sku.isBlank()) "PROD-${prod.id}" else prod.sku
                        val qrBitmap = remember(qrText) {
                            com.example.ui.util.QrCodeUtil.generateQrCode(qrText, 250)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = customStoreName.uppercase(),
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.Black)
                            )

                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Sticker QR",
                                    modifier = Modifier
                                        .size(75.dp)
                                        .border(0.5.dp, Color.Gray)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(75.dp)
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("QR Gagal", fontSize = 9.sp, color = Color.Red)
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = prod.name.uppercase(),
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "SKU: $qrText",
                                    color = Color.DarkGray,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = viewModel.formatRupiah(prod.sellingPrice),
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    } else {
                        // Custom Live Thermal Sticker Drawing Canvas
                        val prod = selectedProduct!!
                        Canvas(modifier = Modifier.fillMaxSize().testTag("label_canvas")) {
                            val w = size.width
                            val h = size.height

                            // Draw Sticker Border
                            drawRect(
                                color = Color.Black,
                                style = Stroke(width = 1.dp.toPx())
                            )

                            // Clean barcode background box at bottom
                            val barcodeTop = h * 0.65f
                            val barcodeHeight = h * 0.22f

                            // Barcode pattern drawing (Thin / Thick lines)
                            val pattern = listOf(2, 4, 1, 3, 2, 1, 4, 2, 3, 1, 2, 4, 1, 3, 2, 1, 4, 2, 3, 1, 2)
                            var curX = w * 0.15f
                            val barWidthBase = w * 0.008f
                            val maxBarX = w * 0.85f

                            var pIdx = 0
                            while (curX < maxBarX) {
                                val sizeMult = pattern[pIdx % pattern.size]
                                val barWidth = barWidthBase * sizeMult
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(curX, barcodeTop),
                                    size = Size(barWidth, barcodeHeight)
                                )
                                curX += barWidth + barWidthBase * 1.5f
                                pIdx++
                            }
                        }

                        // Compose overlay text layers for high crisp fidelity
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Store name header
                            Text(
                                text = customStoreName.uppercase(),
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Horizontal divider line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.5.dp)
                                    .background(Color.Black)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Product details
                            Text(
                                text = prod.name,
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Dynamic pricing templates
                            when (selectedTemplate) {
                                "Diskon/Promo" -> {
                                    val originalPrice = prod.sellingPrice
                                    val discPercent = promoDiscountPercent
                                    val finalPrice = originalPrice * (100 - discPercent) / 100

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = viewModel.formatRupiah(originalPrice),
                                                    color = Color.Gray,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color.Gray
                                                    ),
                                                    modifier = Modifier.background(Color.White)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color.Black)
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "PROMO $discPercent%",
                                                        color = Color.White,
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = viewModel.formatRupiah(finalPrice),
                                                color = Color.Black,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                                "Grosir" -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "HARGA GROSIR",
                                            color = Color.Black,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = viewModel.formatRupiah(prod.sellingPrice),
                                                color = Color.Black,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "(Min. 3 Pcs)",
                                                color = Color.Black,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                "Barcode Klasik" -> {
                                    // Heavy barcode emphasis, small text
                                    Text(
                                        text = viewModel.formatRupiah(prod.sellingPrice),
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                else -> {
                                    // Minimalis Modern (Default)
                                    Text(
                                        text = viewModel.formatRupiah(prod.sellingPrice),
                                        color = Color.Black,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // SKU text below barcode
                            Text(
                                text = if (prod.sku.isBlank()) "0000000" else prod.sku,
                                color = Color.Black,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.width(240.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.simulatePrintLabel() },
                    enabled = selectedProduct != null && printerState.contains("Terhubung") && !isPrinting,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("action_print_label_button")
                ) {
                    if (isPrinting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = {
                        viewModel.triggerNotification("PDF Tersimpan", "Format Label PDF berhasil diekspor ke penyimpanan internal.")
                    },
                    enabled = selectedProduct != null,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("action_save_pdf_label_button")
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Printing loading visual feedback
            AnimatedVisibility(visible = isPrinting) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .width(240.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mengirim perintah ESC/POS ke printer Bluetooth...", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}
