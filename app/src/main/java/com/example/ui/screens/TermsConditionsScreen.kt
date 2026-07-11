package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConditionsScreen(
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isAgreed by remember { mutableStateOf(true) }

    // Hardcoded high-quality professional terms and conditions clauses matching seller needs
    val clauses = remember {
        listOf(
            TermClause(
                id = 1,
                title = "1. Ketentuan Umum & Penggunaan Layanan",
                summary = "Mengatur aturan dasar pendaftaran akun toko, tanggung jawab merchant, dan keamanan kredensial.",
                details = "Aplikasi Seller Sphere Hub menyediakan platform manajemen stok, transaksi kasir digital (POS), analisis tren pasar, dan sistem siaran langsung (Live Streaming Console) untuk memudahkan pelaku usaha mikro, kecil, dan menengah (UMKM).\n\n" +
                        "Setiap pengguna wajib menjaga kerahasiaan informasi masuk akun (username & password). Segala aktivitas yang terjadi di bawah akun Anda sepenuhnya menjadi tanggung jawab Anda sebagai pemilik toko sah."
            ),
            TermClause(
                id = 2,
                title = "2. Pengelolaan Inventori & Stok Barang",
                summary = "Mengatur pencatatan barang dagang, ambang batas minimum stok, dan akurasi data.",
                details = "Pengguna diwajibkan melakukan pembaruan (update) jumlah persediaan secara berkala guna menjamin sinkronisasi data yang akurat dengan sistem penjualan.\n\n" +
                        "Sistem menyediakan notifikasi otomatis ketika stok menyentuh batas kritis (Min Stock Threshold). Kelalaian dalam mengisi ulang stok yang menyebabkan kegagalan pemesanan sepenuhnya di luar tanggung jawab platform."
            ),
            TermClause(
                id = 3,
                title = "3. Transaksi, Pembayaran & Kasir POS",
                summary = "Kebijakan pencatatan transaksi kasir, metode pembayaran tunai maupun non-tunai, serta perpajakan.",
                details = "Semua transaksi penjualan yang dicatatkan melalui modul Kasir POS dianggap sah dan final. Sistem kami menyimpan catatan transaksi secara offline-first dengan enkripsi data lokal.\n\n" +
                        "Harga jual yang ditentukan oleh merchant harus mengikuti regulasi perlindungan konsumen dan tidak diperkenankan melakukan manipulasi harga ekstrim yang merugikan pembeli secara ilegal."
            ),
            TermClause(
                id = 4,
                title = "4. Fitur Live Streaming & Penjualan Interaktif",
                summary = "Pedoman penyiaran langsung (Live Stream Console), penyematan produk, dan etika komunikasi chat.",
                details = "Fitur Live Streaming Console dirancang eksklusif untuk promosi interaktif produk toko Anda. Merchant dilarang keras menyiarkan konten yang melanggar hukum, SARA, pornografi, atau hak kekayaan intelektual pihak lain.\n\n" +
                        "Pesan chat yang dikirimkan oleh seller/penjual melalui fitur Live Chat harus senantiasa mematuhi etika sopan santun. Kami berhak membatasi akses streaming jika ditemukan pelanggaran laporan dari pelanggan."
            ),
            TermClause(
                id = 5,
                title = "5. Logistik, Lokasi, & Penjemputan Kurir",
                summary = "Pengaturan alamat penjemputan paket (pickup address) dan integrasi peta maps koordinat.",
                details = "Alamat penjemputan paket yang diisi pada menu edit profil harus sesuai dengan lokasi fisik gudang atau toko Anda agar proses integrasi armada ekspedisi kurir dapat terlaksana tanpa kendala.\n\n" +
                        "Kurir logistik pihak ketiga akan mendatangi lokasi sesuai koordinat maps yang tersimpan. Keterlambatan penjemputan akibat ketidakakuratan alamat menjadi tanggung jawab pihak pemilik toko."
            ),
            TermClause(
                id = 6,
                title = "6. Keamanan Data & Privasi Enkripsi",
                summary = "Komitmen kami untuk melindungi data personal, riwayat keuangan, dan kerahasiaan operasional.",
                details = "Data operasional Anda, termasuk omzet penjualan, daftar supplier, dan database pelanggan, dienkripsi menggunakan standar keamanan lokal end-to-end.\n\n" +
                "Kami tidak pernah menjual, mendistribusikan, atau menyalahgunakan data toko Anda kepada pihak ketiga mana pun tanpa persetujuan eksplisit Anda terlebih dahulu."
            )
        )
    }

    // Filter clauses based on search query
    val filteredClauses = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            clauses
        } else {
            clauses.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.summary.contains(searchQuery, ignoreCase = true) ||
                        it.details.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Syarat & Ketentuan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("terms_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Legal Disclaimer Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.15f), SoftTeal.copy(alpha = 0.15f))
                        )
                    )
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Persetujuan Layanan Hukum",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Terakhir diperbarui: 11 Juli 2026 • Versi 2.4",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Search Bar for Terms
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari poin syarat / pasal...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("terms_search_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedBorderColor = NeonCyan
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
            )

            // Scrollable Terms Clauses List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredClauses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Syarat atau pasal dengan kata kunci tersebut tidak ditemukan.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    filteredClauses.forEach { clause ->
                        TermItemCard(clause = clause)
                    }
                }
            }

            // Interactive Confirmation switch & button at the bottom
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = isAgreed,
                                onCheckedChange = { isAgreed = it },
                                colors = CheckboxDefaults.colors(checkedColor = NeonCyan),
                                modifier = Modifier.testTag("terms_checkbox")
                            )
                            Text(
                                text = "Saya menyatakan tunduk dan menyetujui seluruh ketentuan operasional di atas.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Button(
                        onClick = onNavigateBack,
                        enabled = isAgreed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftTeal,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("terms_agree_button")
                    ) {
                        Text(
                            text = if (isAgreed) "Lanjutkan Operasional Toko" else "Harap Setujui Ketentuan",
                            color = if (isAgreed) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TermItemCard(clause: TermClause) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isExpanded) 1.dp else 0.5.dp,
            color = if (isExpanded) NeonCyan.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("term_item_card_${clause.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = clause.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpanded) NeonCyan else MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (isExpanded) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = clause.summary,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 8.dp))
                    Text(
                        text = clause.details,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

data class TermClause(
    val id: Int,
    val title: String,
    val summary: String,
    val details: String
)
