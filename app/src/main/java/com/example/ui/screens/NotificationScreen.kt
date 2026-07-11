package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AlertPreset(
    val title: String,
    val message: String,
    val subject: String
)

data class PushHistoryLog(
    val time: String,
    val channel: String,
    val title: String,
    val status: String,
    val statusColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToInventory: () -> Unit
) {
    val context = LocalContext.current

    // Collect profile data and existing preferences
    val ownerEmail by viewModel.ownerEmail.collectAsState()
    val ownerWhatsapp by viewModel.ownerWhatsapp.collectAsState()
    val ownerSms by viewModel.ownerSms.collectAsState()
    val isEmailEnabled by viewModel.isEmailNotificationEnabled.collectAsState()
    val isWhatsappEnabled by viewModel.isWhatsappNotificationEnabled.collectAsState()
    val isSmsEnabled by viewModel.isSmsNotificationEnabled.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()

    // Form inputs
    var emailInput by remember { mutableStateOf("") }
    var whatsappInput by remember { mutableStateOf("") }
    var smsInput by remember { mutableStateOf("") }
    var emailSwitch by remember { mutableStateOf(true) }
    var whatsappSwitch by remember { mutableStateOf(true) }
    var smsSwitch by remember { mutableStateOf(true) }

    // Synchronize inputs when values are first loaded
    LaunchedEffect(ownerEmail, ownerWhatsapp, ownerSms, isEmailEnabled, isWhatsappEnabled, isSmsEnabled) {
        emailInput = ownerEmail
        whatsappInput = ownerWhatsapp
        smsInput = ownerSms
        emailSwitch = isEmailEnabled
        whatsappSwitch = isWhatsappEnabled
        smsSwitch = isSmsEnabled
    }

    // Tab state (0: Alerts Inbox, 1: Gateway Configuration & Simulator)
    var selectedTab by remember { mutableStateOf(0) }

    // Push test parameters
    val alertPresets = listOf(
        AlertPreset(
            title = "Peringatan Stok Tipis",
            message = "[Seller Sphere] PERINGATAN: Stok barang 'Kopi Gayo Premium' tinggal 3 pcs. Harap segera lakukan restock barang!",
            subject = "Restock Alert: Kopi Gayo Premium Hampir Habis"
        ),
        AlertPreset(
            title = "Laporan Penjualan Harian",
            message = "[Seller Sphere] LAPORAN: Rekap harian sukses. Omzet terkumpul: Rp 2.450.000 dengan total 18 transaksi.",
            subject = "Laporan Penjualan Harian Seller Sphere"
        ),
        AlertPreset(
            title = "Notifikasi Kurir Kargo",
            message = "[Seller Sphere] LOGISTIK: Kurir J&T Express sedang menuju ke toko Anda untuk menjemput 4 paket pesanan. Harap siapkan barang Anda.",
            subject = "Informasi Penjemputan Paket Kurir"
        )
    )
    var selectedPresetIndex by remember { mutableStateOf(0) }
    val currentPreset = alertPresets[selectedPresetIndex]

    // Notification simulation history logs
    var pushLogs by remember {
        mutableStateOf(
            listOf(
                PushHistoryLog("08:14", "Email", "Laporan Keuangan", "Terkirim", SoftTeal),
                PushHistoryLog("07:30", "WhatsApp", "Stok Kopi Arabika", "Terkirim", SoftTeal),
                PushHistoryLog("Kemarin", "SMS", "Verifikasi OTP Sesi", "Terkirim", SoftTeal)
            )
        )
    }

    // Snackbar notification states
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    // Actual Intent actions
    fun triggerEmailIntent() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$emailInput")
                putExtra(Intent.EXTRA_SUBJECT, currentPreset.subject)
                putExtra(Intent.EXTRA_TEXT, currentPreset.message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Aplikasi Email tidak ditemukan di perangkat", Toast.LENGTH_LONG).show()
        }
    }

    fun triggerWhatsappIntent() {
        try {
            var formattedPhone = whatsappInput.trim().replace(" ", "").replace("-", "")
            if (formattedPhone.startsWith("0")) {
                formattedPhone = "62" + formattedPhone.substring(1)
            }
            if (!formattedPhone.startsWith("+") && !formattedPhone.startsWith("62")) {
                formattedPhone = "62$formattedPhone"
            }
            val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(currentPreset.message)}"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp tidak terpasang di perangkat", Toast.LENGTH_LONG).show()
        }
    }

    fun triggerSmsIntent() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$smsInput")
                putExtra("sms_body", currentPreset.message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Aplikasi SMS tidak ditemukan di perangkat", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Notifikasi & Alur Pesan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("notification_back_btn")
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
        ) {
            // Tab Header (Inbox vs Gateway Setup)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.testTag("tab_alerts_inbox")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = if (selectedTab == 0) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Kotak Masuk Alergi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("tab_gateway_settings")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsInputAntenna,
                            contentDescription = null,
                            tint = if (selectedTab == 1) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Gateway & Push Test",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tab Content Frame
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (selectedTab == 0) {
                    // TAB 1: System & Stock Alerts Inbox
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Stock Warnings Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "PERINGATAN STOK TIPIS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftTeal
                            )
                            Badge(containerColor = if (lowStockProducts.isNotEmpty()) WarmOrange else SoftTeal) {
                                Text(
                                    "${lowStockProducts.size} Produk Kritis",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (lowStockProducts.isEmpty()) {
                            // Beautiful secure safe icon
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Safe",
                                        tint = SoftTeal,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        "Semua Stok Barang Aman",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Tidak ada barang yang berada di bawah ambang batas minimum stok penjualan saat ini.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        } else {
                            // Render Low Stock list
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                lowStockProducts.forEach { product ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = product.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Kategori: ${product.category} • SKU: ${product.sku.ifBlank { "-" }}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Stok: ${product.stock} pcs",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AlertRed
                                            )
                                            Text(
                                                text = "Ambang: ${product.minStockThreshold} pcs",
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Link to Inventory
                            Button(
                                onClick = onNavigateToInventory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("alert_to_inventory_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Buka Manajemen Inventaris Toko", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        // Recent System Updates
                        Text(
                            "CATATAN SISTEM AUTOMATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftTeal,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SystemMessageRow(
                                    title = "Otomasi Kurir J&T Aktif",
                                    desc = "Request pickup otomatis diaktifkan apabila pesanan selesai di-packing.",
                                    time = "Hari ini"
                                )
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                SystemMessageRow(
                                    title = "Gateway Integrasi Siap",
                                    desc = "Aliran API notifikasi ke WA, SMS, & Email siap digunakan untuk pengujian.",
                                    time = "Hari ini"
                                )
                            }
                        }
                    }
                } else {
                    // TAB 2: Notification Channel Settings & Push Simulator
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // SECTION 2A: Gateway Settings Form
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
                                    Icon(Icons.Default.SettingsInputAntenna, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "KONFIGURASI SALURAN PENGIRIMAN",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }

                                // Email Input
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Email, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                            Text("Notifikasi Email", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Switch(
                                            checked = emailSwitch,
                                            onCheckedChange = { emailSwitch = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.3f)),
                                            modifier = Modifier.scale(0.8f).testTag("notif_switch_email")
                                        )
                                    }
                                    OutlinedTextField(
                                        value = emailInput,
                                        onValueChange = { emailInput = it },
                                        placeholder = { Text("Masukkan alamat email...", fontSize = 11.sp) },
                                        enabled = emailSwitch,
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("notif_input_email"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    )
                                }

                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                                // WhatsApp Input
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Sms, contentDescription = null, tint = SoftTeal, modifier = Modifier.size(16.dp))
                                            Text("Notifikasi WhatsApp (WA)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Switch(
                                            checked = whatsappSwitch,
                                            onCheckedChange = { whatsappSwitch = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = SoftTeal, checkedTrackColor = SoftTeal.copy(alpha = 0.3f)),
                                            modifier = Modifier.scale(0.8f).testTag("notif_switch_wa")
                                        )
                                    }
                                    OutlinedTextField(
                                        value = whatsappInput,
                                        onValueChange = { whatsappInput = it },
                                        placeholder = { Text("Contoh: 081234567890", fontSize = 11.sp) },
                                        enabled = whatsappSwitch,
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("notif_input_wa"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoftTeal, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    )
                                }

                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                                // SMS Input
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(16.dp))
                                            Text("Notifikasi SMS Seluler", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Switch(
                                            checked = smsSwitch,
                                            onCheckedChange = { smsSwitch = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = WarmOrange, checkedTrackColor = WarmOrange.copy(alpha = 0.3f)),
                                            modifier = Modifier.scale(0.8f).testTag("notif_switch_sms")
                                        )
                                    }
                                    OutlinedTextField(
                                        value = smsInput,
                                        onValueChange = { smsInput = it },
                                        placeholder = { Text("Contoh: 081234567890", fontSize = 11.sp) },
                                        enabled = smsSwitch,
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("notif_input_sms"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmOrange, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    )
                                }

                                // Save button
                                Button(
                                    onClick = {
                                        viewModel.updateNotificationPreferences(
                                            whatsapp = whatsappInput,
                                            sms = smsInput,
                                            emailEnabled = emailSwitch,
                                            whatsappEnabled = whatsappSwitch,
                                            smsEnabled = smsSwitch
                                        )
                                        snackbarMessage = "Preferensi & nomor gateway tersimpan!"
                                        showSuccessSnackbar = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("notif_save_prefs_btn")
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("Simpan Konfigurasi Saluran", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // SECTION 2B: Dynamic Simulator Console
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = SoftTeal, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "SIMULASI ENGINE & INTENT PUSH",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTeal
                                    )
                                }

                                Text(
                                    text = "Pilih jenis templat notifikasi di bawah untuk diuji coba. Anda bisa melakukan simulasi pengiriman cepat ke log internal atau meluncurkan Intent sistem guna memicu pengiriman pesan nyata.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )

                                // Presets selection tabs
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    alertPresets.forEachIndexed { index, preset ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selectedPresetIndex == index) SoftTeal else Color.Transparent)
                                                .clickable { selectedPresetIndex = index }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = preset.title.replace("Peringatan ", "").replace("Laporan ", "").replace("Notifikasi ", ""),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedPresetIndex == index) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Preview Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Isi Pesan Notifikasi:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTeal
                                        )
                                        Text(
                                            text = currentPreset.message,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                // Direct System Intent Buttons Row
                                Text(
                                    text = "KIRIM VIA INSTAN INTENT SISTEM (Picu Pesan Nyata):",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Launch WA
                                    Button(
                                        onClick = { triggerWhatsappIntent() },
                                        enabled = whatsappSwitch && whatsappInput.isNotBlank(),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("btn_intent_wa"),
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftTeal),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Sms, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                            Text("WA Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }

                                    // Launch SMS
                                    Button(
                                        onClick = { triggerSmsIntent() },
                                        enabled = smsSwitch && smsInput.isNotBlank(),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("btn_intent_sms"),
                                        colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Text("SMS Kirim", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }

                                    // Launch Email
                                    Button(
                                        onClick = { triggerEmailIntent() },
                                        enabled = emailSwitch && emailInput.isNotBlank(),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("btn_intent_email"),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Text("Email App", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                                // Simulated Console Push
                                Button(
                                    onClick = {
                                        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                        val channelsSent = mutableListOf<String>()
                                        if (emailSwitch && emailInput.isNotBlank()) channelsSent.add("Email")
                                        if (whatsappSwitch && whatsappInput.isNotBlank()) channelsSent.add("WhatsApp")
                                        if (smsSwitch && smsInput.isNotBlank()) channelsSent.add("SMS")

                                        if (channelsSent.isEmpty()) {
                                            snackbarMessage = "Harap aktifkan dan isi setidaknya 1 saluran pengiriman!"
                                            showSuccessSnackbar = true
                                        } else {
                                            val newLogs = channelsSent.map { ch ->
                                                PushHistoryLog(timeNow, ch, currentPreset.title, "Sukses Terkirim", SoftTeal)
                                            }
                                            pushLogs = newLogs + pushLogs
                                            snackbarMessage = "Notifikasi sukses dipush otomatis ke ${channelsSent.joinToString(", ")}!"
                                            showSuccessSnackbar = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .testTag("btn_simulated_push")
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.SettingsPower, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("Simulasikan Auto-Push ke Seluruh Saluran Aktif", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // SECTION 2C: Notification Push History Audit Log
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
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
                                    Icon(Icons.Default.History, contentDescription = null, tint = VividOrchid, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "RIWAYAT PENGIRIMAN & STATUS SERVER",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VividOrchid
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    pushLogs.take(5).forEach { log ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when (log.channel) {
                                                                "Email" -> NeonCyan.copy(alpha = 0.15f)
                                                                "WhatsApp" -> SoftTeal.copy(alpha = 0.15f)
                                                                else -> WarmOrange.copy(alpha = 0.15f)
                                                            }
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = when (log.channel) {
                                                            "Email" -> Icons.Default.Email
                                                            "WhatsApp" -> Icons.Default.Sms
                                                            else -> Icons.Default.PhoneAndroid
                                                        },
                                                        contentDescription = null,
                                                        tint = when (log.channel) {
                                                            "Email" -> NeonCyan
                                                            "WhatsApp" -> SoftTeal
                                                            else -> WarmOrange
                                                        },
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }

                                                Column {
                                                    Text(
                                                        text = log.title,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Via ${log.channel} • Waktu: ${log.time}",
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            // Status Badge
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(log.statusColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = log.status,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = log.statusColor
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
        }
    }

    // Custom bottom notification snackbar
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SystemMessageRow(
    title: String,
    desc: String,
    time: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(NeonCyan)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
        Text(
            text = time,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper extension for scaling
private fun Modifier.scale(scale: Float): Modifier = this
