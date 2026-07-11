package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()

    // Mock Trend keywords
    val trendKeywords = listOf(
        Pair("Kopi Gula Aren", "+142%"),
        Pair("Camilan Pedas Daun Jeruk", "+98%"),
        Pair("Susu Almond Organik", "+75%"),
        Pair("Madu Hutan Murni", "+54%"),
        Pair("Teh Matcha Bubuk", "+42%")
    )

    // Competitor Benchmark State
    var selectedProductIndex by remember { mutableStateOf(0) }
    val selectedProduct = if (products.isNotEmpty() && selectedProductIndex in products.indices) products[selectedProductIndex] else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SoftTeal)
                        Text(
                            "Tren Pasar & Rekomendasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: Dynamic Search Trends Visualizer
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "KATA KUNCI TERPOPULER MINGGU INI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    Text(
                        text = "Data pencarian real-time pelanggan di wilayah Jakarta Barat menunjukkan minat yang tinggi pada komoditas berikut.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    // Draw clean bar list representing trending keywords search volume indices
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        trendKeywords.forEachIndexed { i, keyword ->
                            val volumeFactor = 1f - (i * 0.15f)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(keyword.first, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(keyword.second, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SoftTeal)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(volumeFactor)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(NeonCyan, SoftTeal)
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: AI Smart Stock Recommendations
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VividOrchid, modifier = Modifier.size(18.dp))
                        Text(
                            text = "REKOMENDASI STOK AI SMART",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividOrchid
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VividOrchid.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .border(0.5.dp, VividOrchid.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = VividOrchid, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Prediksi Lonjakan Permintaan:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VividOrchid)
                            }
                            Text(
                                text = "Berdasarkan tren cuaca panas ekstrem saat ini, produk kategori camilan dingin, es kopi gula aren, dan teh matcha diproyeksikan melonjak permintaannya hingga +35% dalam dua minggu mendatang. Disarankan menambah stok bahan baku pendukung sebesar 20%.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // SECTION 3: Competitor Benchmark Tool
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Compare, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(18.dp))
                        Text(
                            text = "PERBANDINGAN HARGA PASAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmOrange
                        )
                    }

                    Text(
                        text = "Gunakan alat ini untuk melihat posisi harga produk Anda dibandingkan dengan rata-rata harga pasar kompetitor online.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    if (products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tambahkan produk Anda terlebih dahulu di Kelola Stok.", fontSize = 11.sp)
                        }
                    } else {
                        // Product selector dropdown
                        var expandedDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedDropdown = true },
                                modifier = Modifier.fillMaxWidth().testTag("benchmark_product_dropdown"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedProduct?.name ?: "Pilih Produk...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            DropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                products.forEachIndexed { idx, p ->
                                    DropdownMenuItem(
                                        text = { Text("${p.name} (${viewModel.formatRupiah(p.sellingPrice)})", fontSize = 12.sp) },
                                        onClick = {
                                            selectedProductIndex = idx
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        if (selectedProduct != null) {
                            val yourPrice = selectedProduct.sellingPrice
                            // Emulate competitor low, average, and high market rates
                            val marketAvg = yourPrice * 1.05
                            val marketLow = yourPrice * 0.90
                            val marketHigh = yourPrice * 1.25

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Harga Anda:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(viewModel.formatRupiah(yourPrice), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Harga Kompetitor Terendah:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(viewModel.formatRupiah(marketLow), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AlertRed)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Rata-Rata Pasar:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(viewModel.formatRupiah(marketAvg), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Harga Kompetitor Tertinggi:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(viewModel.formatRupiah(marketHigh), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SoftTeal)
                                }

                                Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                // Dynamic analysis badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SoftTeal.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Icon(Icons.Default.TrendingDown, contentDescription = null, tint = SoftTeal, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "Harga Anda 5% lebih murah dari rata-rata kompetitor. Ini adalah posisi kompetitif yang menguntungkan!",
                                        fontSize = 10.sp,
                                        color = SoftTeal,
                                        lineHeight = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
