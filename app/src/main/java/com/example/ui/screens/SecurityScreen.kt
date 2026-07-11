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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    // Interactive states
    var appPin by remember { mutableStateOf("1234") }
    var pinInput by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var isBiometricEnabled by remember { mutableStateOf(true) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    // Active sessions state list
    var activeSessions by remember {
        mutableStateOf(
            listOf(
                ConnectedDevice("Samsung Galaxy S24 Ultra (Sesi Ini)", "DKI Jakarta, ID", "Aktif Sekarang", true),
                ConnectedDevice("Web Merchant Console (Chrome)", "Bandung, Jawa Barat, ID", "10 Menit Lalu", false),
                ConnectedDevice("Tablet Kasir iPad Pro", "Surabaya, Jawa Timur, ID", "Kemarin", false)
            )
        )
    }

    // Dynamic security logs based on user action
    val initialLogs = remember {
        mutableListOf(
            SecurityLog("07:48", "Pendaftaran Lokasi Logistik sukses", "Berhasil mendeteksi koordinat"),
            SecurityLog("06:12", "Masuk dengan kode OTP", "Berhasil login dari Samsung S24"),
            SecurityLog("Kemarin", "Sandi Toko berhasil diperbarui", "Kekuatan sandi: Sangat Tinggi"),
            SecurityLog("08 Juli", "Enkripsi Database lokal aktif", "Algoritma AES-256 terverifikasi")
        )
    }
    var securityLogs by remember { mutableStateOf(initialLogs.toList()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Keamanan Akun",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("security_back_btn")
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
            // SECTION 1: Security Health Status Indicator
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SoftTeal.copy(alpha = 0.15f))
                            .border(1.dp, SoftTeal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SoftTeal,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STATUS KEAMANAN: AMAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftTeal
                        )
                        Text(
                            text = "Tingkat perlindungan akun Anda sangat tinggi. Enkripsi lokal aktif dan diproteksi sidik jari.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // SECTION 2: PIN and Biometrics Settings
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
                        Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "OTENTIKASI & AKSES MASUK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    // Row 1: PIN Code setup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                            .clickable { showPinDialog = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Kode PIN Pengaman",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Kode PIN Aktif: **** (Ubah Kode)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Row 2: Biometrics Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Sidik Jari / Biometrik",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Buka kunci konsol kasir via sidik jari",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = {
                                isBiometricEnabled = it
                                snackbarMessage = if (it) "Otentikasi Sidik Jari diaktifkan" else "Otentikasi Sidik Jari dinonaktifkan"
                                showSuccessSnackbar = true
                                val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                securityLogs = listOf(
                                    SecurityLog(timeNow, "Ubah status biometrik", if (it) "Sidik jari diaktifkan" else "Sidik jari dinonaktifkan")
                                ) + securityLogs
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = NeonCyan.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("security_biometric_switch")
                        )
                    }
                }
            }

            // SECTION 3: Connected Sessions / Devices
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(18.dp))
                            Text(
                                text = "PERANGKAT TERHUBUNG (${activeSessions.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmOrange
                            )
                        }

                        if (activeSessions.size > 1) {
                            TextButton(
                                onClick = {
                                    activeSessions = listOf(activeSessions[0])
                                    snackbarMessage = "Berhasil mengeluarkan semua sesi perangkat lain."
                                    showSuccessSnackbar = true
                                    val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                    securityLogs = listOf(
                                        SecurityLog(timeNow, "Sesi perangkat lain dicabut", "Mengeluarkan 2 perangkat luar")
                                    ) + securityLogs
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Keluar Sesi Lain", fontSize = 11.sp, color = WarmOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        activeSessions.forEach { session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (session.isCurrent) NeonCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (session.isCurrent) Icons.Default.PhoneAndroid else Icons.Default.Laptop,
                                        contentDescription = null,
                                        tint = if (session.isCurrent) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = session.deviceName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${session.location} • ${session.lastActive}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (!session.isCurrent) {
                                    IconButton(
                                        onClick = {
                                            activeSessions = activeSessions.filter { it != session }
                                            snackbarMessage = "Sesi ${session.deviceName} dicabut."
                                            showSuccessSnackbar = true
                                            val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                            securityLogs = listOf(
                                                SecurityLog(timeNow, "Akses perangkat dicabut", session.deviceName)
                                            ) + securityLogs
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Sesi",
                                            tint = AlertRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4: Security Audit Log Activity
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
                        Icon(Icons.Default.History, contentDescription = null, tint = VividOrchid, modifier = Modifier.size(18.dp))
                        Text(
                            text = "RIWAYAT AKTIVITAS KEAMANAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividOrchid
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        securityLogs.forEach { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = log.time,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VividOrchid,
                                    modifier = Modifier.width(55.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.action,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = log.detail,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Status Badge Disclaimer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonCyan.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .border(0.5.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Aplikasi Anda berjalan pada database SQLite terenkripsi. Sesi kasir dikontrol oleh kunci PIN lokal untuk mencegah penyalahgunaan saat tablet ditinggalkan.",
                    fontSize = 10.sp,
                    color = NeonCyan,
                    lineHeight = 14.sp
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

    // Interactive custom PIN changer Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = {
                Text(
                    "Ubah PIN Keamanan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Masukkan 4 digit PIN baru Anda untuk mengamankan konsol Kasir POS dan analisis keuangan.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                pinInput = input
                            }
                        },
                        label = { Text("PIN Baru (4 Digit)", fontSize = 11.sp) },
                        placeholder = { Text("Contoh: 8888") },
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("security_pin_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            appPin = pinInput
                            showPinDialog = false
                            snackbarMessage = "PIN Keamanan sukses diperbarui!"
                            showSuccessSnackbar = true
                            pinInput = ""
                            val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            securityLogs = listOf(
                                SecurityLog(timeNow, "Kode PIN diubah", "Proteksi sandi level 2 aktif")
                            ) + securityLogs
                        } else {
                            snackbarMessage = "PIN harus tepat 4 digit angka!"
                            showSuccessSnackbar = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Simpan PIN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinDialog = false
                    pinInput = ""
                }) {
                    Text("Batal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

data class ConnectedDevice(
    val deviceName: String,
    val location: String,
    val lastActive: String,
    val isCurrent: Boolean
)

data class SecurityLog(
    val time: String,
    val action: String,
    val detail: String
)
