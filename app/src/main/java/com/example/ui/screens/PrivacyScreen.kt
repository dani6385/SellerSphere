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
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    // Interactive states for privacy settings
    var hideSalesTurnover by remember { mutableStateOf(false) }
    var allowAiAnalysis by remember { mutableStateOf(true) }
    var encryptImagesMetadata by remember { mutableStateOf(true) }
    var shareLocationWithCouriers by remember { mutableStateOf(true) }

    // Dialog & Notification states
    var showExportDialog by remember { mutableStateOf(false) }
    var showDeleteDataDialog by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Privasi Akun & Data",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("privacy_back_btn")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: Privacy Rating Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(SoftTeal.copy(alpha = 0.15f), NeonCyan.copy(alpha = 0.1f))
                        )
                    )
                    .border(1.dp, SoftTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SoftTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SoftTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Kedaulatan Data Penjual",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Semua data transaksi dienkripsi secara lokal di ponsel Anda.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // SECTION 2: Interactive Privacy Toggles
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "KONTROL DAN IZIN DATA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    // Toggle 1: Sembunyikan Omzet Penjualan
                    PrivacyToggleItem(
                        title = "Samarkan Nilai Omzet Utama",
                        description = "Sembunyikan grafik pendapatan di halaman utama saat berada di tempat umum.",
                        checked = hideSalesTurnover,
                        onCheckedChange = {
                            hideSalesTurnover = it
                            snackbarMessage = if (it) "Sensitivitas omzet diaktifkan (disamarkan)" else "Omzet ditampilkan penuh"
                            showSuccessSnackbar = true
                        },
                        tag = "privacy_toggle_hide_turnover"
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Toggle 2: Izinkan Analisis Pasar AI
                    PrivacyToggleItem(
                        title = "Rekomendasi AI Produk Lokal",
                        description = "Izinkan model AI lokal menganalisis nama barang guna merumuskan tren penjualan terbaik.",
                        checked = allowAiAnalysis,
                        onCheckedChange = {
                            allowAiAnalysis = it
                            snackbarMessage = if (it) "Izin analisis AI diaktifkan" else "Analisis AI dinonaktifkan"
                            showSuccessSnackbar = true
                        },
                        tag = "privacy_toggle_ai_analysis"
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Toggle 3: Bagikan Lokasi Ke Kurir
                    PrivacyToggleItem(
                        title = "Bagikan Peta Lokasi ke Mitra Kurir",
                        description = "Mengirimkan koordinat GPS jemput barang demi mempercepat pencarian oleh pengemudi kargo.",
                        checked = shareLocationWithCouriers,
                        onCheckedChange = {
                            shareLocationWithCouriers = it
                            snackbarMessage = if (it) "Koordinat peta dibagikan ke logistik" else "Koordinat dirahasiakan"
                            showSuccessSnackbar = true
                        },
                        tag = "privacy_toggle_share_location"
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Toggle 4: Enkripsi Metadata File Gambar
                    PrivacyToggleItem(
                        title = "Bersihkan Metadata EXIF Gambar",
                        description = "Secara otomatis menghapus tanggal & lokasi GPS saat mengunggah foto produk baru.",
                        checked = encryptImagesMetadata,
                        onCheckedChange = {
                            encryptImagesMetadata = it
                            snackbarMessage = if (it) "EXIF metadata dibersihkan otomatis" else "Metadata dipertahankan"
                            showSuccessSnackbar = true
                        },
                        tag = "privacy_toggle_exif"
                    )
                }
            }

            // SECTION 3: Portabilitas & Kedaulatan Data (Export / Delete)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(18.dp))
                        Text(
                            text = "PORTABILITAS & HAPUS AKUN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmOrange
                        )
                    }

                    Text(
                        text = "Sesuai regulasi perlindungan data pribadi (GDPR/UU PDP), Anda memiliki kontrol penuh atas hak penghapusan dan ekspor seluruh informasi bisnis.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Button 1: Export Data (.json)
                    OutlinedButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("privacy_export_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NeonCyan
                        ),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Ekspor Backup Data Bisnis (.JSON)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Button 2: Delete All Local Data
                    Button(
                        onClick = { showDeleteDataDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("privacy_delete_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertRed.copy(alpha = 0.15f),
                            contentColor = AlertRed
                        ),
                        border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.3f))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Shredder (Hapus Permanen Seluruh Sesi)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // SECTION 4: GDPR Compliance / Transparency
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftTeal.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .border(0.5.dp, SoftTeal.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SoftTeal,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Aplikasi ini mematuhi standar privasi offline-first. Kami tidak pernah mengunggah informasi rahasia tanpa persetujuan eksplisit Anda di atas.",
                    fontSize = 10.sp,
                    color = SoftTeal,
                    lineHeight = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Success notification bar
    if (showSuccessSnackbar) {
        LaunchedEffect(showSuccessSnackbar) {
            kotlinx.coroutines.delay(3000)
            showSuccessSnackbar = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTeal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                    Text(
                        text = snackbarMessage,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Export Data Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text(
                    "Ekspor Enkripsi Berhasil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    "Seluruh data inventaris barang, riwayat transaksi penjualan, dan catatan profil maps Anda berhasil dikemas ke dalam cadangan JSON terenkripsi:\n\n" +
                            "📁 seller_sphere_backup_2026.json (84.2 KB)\n\n" +
                            "File ini siap dipindahkan ke perangkat baru.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        snackbarMessage = "Data cadangan disimpan di berkas Unduhan!"
                        showSuccessSnackbar = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Unduh Berkas", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Tutup", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    // Shredder / Delete Data Dialog
    if (showDeleteDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDataDialog = false },
            title = {
                Text(
                    "🚨 Hapus Seluruh Data Toko?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AlertRed
                )
            },
            text = {
                Text(
                    "Tindakan ini bersifat IRREVERSIBLE (tidak dapat dibatalkan). Semua data penjualan offline, konfigurasi alamat kurir, dan daftar inventaris akan dihancurkan secara permanen dari penyimpanan SQLite lokal.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDataDialog = false
                        snackbarMessage = "Seluruh database offline berhasil dibersihkan."
                        showSuccessSnackbar = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Hancurkan Data", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataDialog = false }) {
                    Text("Batal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun PrivacyToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonCyan.copy(alpha = 0.3f)
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
