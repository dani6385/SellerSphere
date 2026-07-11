package com.example.ui.screens

import android.content.Context
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

data class IntellectualPropertyAsset(
    val id: String,
    val type: String, // "Merek Dagang", "Hak Cipta", "Paten", "Desain Industri"
    val name: String,
    val regNumber: String,
    val registeredDate: String,
    val status: String // "Disetujui", "Proses", "Ditolak"
)

data class InfringementReport(
    val reporterName: String,
    val infringedAsset: String,
    val infringerStoreUrl: String,
    val evidenceLink: String,
    val extraNotes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntellectualPropertyScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Navigation and tab states
    var selectedTab by remember { mutableStateOf(0) } // 0: Pedoman & Edukasi, 1: Aset HAKI Saya, 2: Aduan Pelanggaran

    // Local IP Assets List
    var ipAssets by remember {
        mutableStateOf(
            listOf(
                IntellectualPropertyAsset("1", "Merek Dagang", "Seller Sphere™", "IDM000987654", "12 Nov 2025", "Disetujui"),
                IntellectualPropertyAsset("2", "Hak Cipta", "Desain Maskot Kopi Gayo", "EC002026123", "05 Jan 2026", "Disetujui"),
                IntellectualPropertyAsset("3", "Desain Industri", "Botol Kemasan Ramah Lingkungan", "IDD000045611", "22 Feb 2026", "Proses")
            )
        )
    }

    // Modal forms & dialogs state
    var showAddAssetDialog by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    // Add Asset Form States
    var newAssetType by remember { mutableStateOf("Merek Dagang") }
    var newAssetName by remember { mutableStateOf("") }
    var newAssetRegNumber by remember { mutableStateOf("") }
    var assetTypeExpanded by remember { mutableStateOf(false) }

    // Infringement Claim Form States
    var infringerName by remember { mutableStateOf("") }
    var infringedAssetName by remember { mutableStateOf("") }
    var infringingLink by remember { mutableStateOf("") }
    var evidenceUrl by remember { mutableStateOf("") }
    var statementChecked by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Hak Kekayaan Intelektual",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ipr_back_btn")
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
            // Section 1: HAKI Top Tabs
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
                    modifier = Modifier.testTag("tab_ipr_guidelines")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = if (selectedTab == 0) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Pedoman",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("tab_ipr_assets")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (selectedTab == 1) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Aset HAKI Saya",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.testTag("tab_ipr_infringement")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = if (selectedTab == 2) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Aduan Hak Cipta",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 2) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Scrollable Content Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 0: Education & Guidance
                        IprGuidelinesTabContent()
                    }
                    1 -> {
                        // TAB 1: Registered Intellectual Property Assets & Add Flow
                        IprAssetsTabContent(
                            assets = ipAssets,
                            onAddClick = { showAddAssetDialog = true },
                            onDeleteClick = { assetId ->
                                ipAssets = ipAssets.filter { it.id != assetId }
                                snackbarMessage = "Aset perlindungan berhasil dihapus."
                                showSuccessSnackbar = true
                            }
                        )
                    }
                    2 -> {
                        // TAB 2: Infringement takedown report claim form
                        IprInfringementTabContent(
                            infringerName = infringerName,
                            onInfringerChange = { infringerName = it },
                            infringedAssetName = infringedAssetName,
                            onInfringedAssetChange = { infringedAssetName = it },
                            infringingLink = infringingLink,
                            onLinkChange = { infringingLink = it },
                            evidenceUrl = evidenceUrl,
                            onEvidenceChange = { evidenceUrl = it },
                            statementChecked = statementChecked,
                            onCheckedChange = { statementChecked = it },
                            onSubmit = {
                                if (infringerName.isBlank() || infringedAssetName.isBlank() || infringingLink.isBlank()) {
                                    Toast.makeText(context, "Harap lengkapi semua kolom wajib!", Toast.LENGTH_SHORT).show()
                                } else if (!statementChecked) {
                                    Toast.makeText(context, "Harap setujui pernyataan kebenaran informasi!", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Submit simulated claim successfully
                                    snackbarMessage = "Laporan berhasil terkirim ke Tim Legalitas DJKI & Seller Sphere!"
                                    showSuccessSnackbar = true
                                    // Reset fields
                                    infringerName = ""
                                    infringedAssetName = ""
                                    infringingLink = ""
                                    evidenceUrl = ""
                                    statementChecked = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Success notification bar
    if (showSuccessSnackbar) {
        LaunchedEffect(showSuccessSnackbar) {
            kotlinx.coroutines.delay(3500)
            showSuccessSnackbar = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
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
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                    Text(
                        text = snackbarMessage,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Dialog: Add intellectual property asset
    if (showAddAssetDialog) {
        AlertDialog(
            onDismissRequest = { showAddAssetDialog = false },
            title = {
                Text(
                    "Daftarkan Perlindungan Aset",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Catat perlindungan Kekayaan Intelektual Anda di bawah payung hukum untuk dipantau secara otomatis.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    // Type Selector Dropdown Box
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tipe Kekayaan Intelektual", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { assetTypeExpanded = true }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(newAssetType, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = assetTypeExpanded,
                            onDismissRequest = { assetTypeExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            listOf("Merek Dagang", "Hak Cipta", "Paten", "Desain Industri").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, fontSize = 12.sp) },
                                    onClick = {
                                        newAssetType = type
                                        assetTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Asset Name input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Nama Karya / Merek", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = newAssetName,
                            onValueChange = { newAssetName = it },
                            placeholder = { Text("Contoh: Kopi Gayo Signature", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth().testTag("add_ipr_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
                        )
                    }

                    // Registration number
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Nomor Permohonan / Sertifikat (Opsional)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = newAssetRegNumber,
                            onValueChange = { newAssetRegNumber = it },
                            placeholder = { Text("Mulai dengan IDM / EC / IDD...", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth().testTag("add_ipr_reg_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAssetName.isBlank()) {
                            Toast.makeText(context, "Nama aset HAKI tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                        } else {
                            val cleanRegNumber = if (newAssetRegNumber.isBlank()) "PENDING-${(100000..999999).random()}" else newAssetRegNumber
                            val newAsset = IntellectualPropertyAsset(
                                id = (ipAssets.size + 1).toString(),
                                type = newAssetType,
                                name = newAssetName,
                                regNumber = cleanRegNumber,
                                registeredDate = "Hari ini",
                                status = if (newAssetRegNumber.isBlank()) "Proses" else "Disetujui"
                            )
                            ipAssets = ipAssets + newAsset
                            showAddAssetDialog = false
                            snackbarMessage = "Sukses mendaftarkan perlindungan '${newAssetName}'!"
                            showSuccessSnackbar = true

                            // Reset
                            newAssetName = ""
                            newAssetRegNumber = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Daftarkan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAssetDialog = false }) {
                    Text("Batal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun IprGuidelinesTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hero Image/Illustration Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SoftTeal.copy(alpha = 0.2f), MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                )
                .border(1.dp, SoftTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SoftTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = SoftTeal,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gerbang Pelindung HAKI",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Seller Sphere berkomitmen penuh melindungi hak cipta, merek dagang, dan karya asli para mitra merchant dari pembajakan serta plagiarisme.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Text(
            "MENGENAL PILAR KEKAYAAN INTELEKTUAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SoftTeal
        )

        // Column of explanation cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            IprConceptCard(
                icon = Icons.Default.Label,
                iconTint = NeonCyan,
                title = "Merek Dagang (Trademark)",
                description = "Melindungi nama toko, kata penjelas, logo merek, slogan, atau kombinasi visual unik yang membedakan produk Anda dari kompetitor.",
                guideline = "Hak merek diakui setelah terdaftar di DJKI. Melarang pihak lain menjual dengan nama identik."
            )

            IprConceptCard(
                icon = Icons.Default.Copyright,
                iconTint = SoftTeal,
                title = "Hak Cipta (Copyright)",
                description = "Melindungi ekspresi orisinal berupa karya visual (foto produk asli), deskripsi puitis tulisan, manual tata cara, maupun desain grafis spanduk.",
                guideline = "Hak cipta muncul otomatis sejak dideklarasikan. Mengkloning foto produk asli merchant lain dilarang keras."
            )

            IprConceptCard(
                icon = Icons.Default.PrecisionManufacturing,
                iconTint = WarmOrange,
                title = "Desain Industri (Industrial Design)",
                description = "Melindungi bentuk estetika 3 dimensi, konfigurasi ergonomis wadah, atau pola hiasan kemasan luar dari peniruan presisi.",
                guideline = "Melindungi tampilan visual fungsional kemasan yang diciptakan merchant agar tidak dicontek."
            )
        }

        // DJKI Alignment note
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftTeal.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .border(0.5.dp, SoftTeal.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = SoftTeal,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Pendaftaran resmi HAKI dapat diurus mandiri secara online melalui sistem e-Hak Cipta / e-Merek Direktorat Jenderal Kekayaan Intelektual Republik Indonesia (DJKI Kemenkumham).",
                fontSize = 10.sp,
                color = SoftTeal,
                lineHeight = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun IprAssetsTabContent(
    assets: List<IntellectualPropertyAsset>,
    onAddClick: () -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "DAFTAR PORTOPOLIO HAKI ANDA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftTeal
                )
                Text(
                    "Sertifikasi perlindungan hukum aktif",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("add_ip_asset_trigger_btn"),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Tambah", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (assets.isEmpty()) {
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
                        imageVector = Icons.Default.ShieldMoon,
                        contentDescription = "Empty IP",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Belum Ada Portofolio HAKI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Gunakan tombol 'Tambah' di atas untuk mencatat merek dagang, hak cipta foto produk, atau perlindungan kemasan yang Anda miliki.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                assets.forEach { asset ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // IP Type Badge Row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when (asset.type) {
                                                        "Merek Dagang" -> NeonCyan.copy(alpha = 0.15f)
                                                        "Hak Cipta" -> SoftTeal.copy(alpha = 0.15f)
                                                        "Desain Industri" -> WarmOrange.copy(alpha = 0.15f)
                                                        else -> VividOrchid.copy(alpha = 0.15f)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = asset.type,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (asset.type) {
                                                    "Merek Dagang" -> NeonCyan
                                                    "Hak Cipta" -> SoftTeal
                                                    "Desain Industri" -> WarmOrange
                                                    else -> VividOrchid
                                                }
                                            )
                                        }

                                        // Status Badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (asset.status == "Disetujui") SoftTeal.copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (asset.status == "Disetujui") SoftTeal else Color.Gray)
                                                )
                                                Text(
                                                    text = asset.status,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (asset.status == "Disetujui") SoftTeal else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = asset.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Delete option
                                IconButton(
                                    onClick = { onDeleteClick(asset.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = AlertRed.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "NOMOR PERMOHONAN",
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = asset.regNumber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "TANGGAL DICATAT",
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = asset.registeredDate,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
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

@Composable
fun IprInfringementTabContent(
    infringerName: String,
    onInfringerChange: (String) -> Unit,
    infringedAssetName: String,
    onInfringedAssetChange: (String) -> Unit,
    infringingLink: String,
    onLinkChange: (String) -> Unit,
    evidenceUrl: String,
    onEvidenceChange: (String) -> Unit,
    statementChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
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
                    Icon(Icons.Default.Security, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
                    Text(
                        text = "FORMULIR TAKEDOWN PELANGGARAN HAK CIPTA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlertRed
                    )
                }

                Text(
                    text = "Jika Anda menemukan merchant lain di platform Seller Sphere mengunggah foto asli Anda tanpa izin atau menjiplak deskripsi dagang berhak cipta milik Anda, kirimkan form laporan resmi untuk proses takedown cepat.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // Input 1: Infringer Store/Product Name
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Nama Toko / Nama Produk Pelanggar *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = infringerName,
                        onValueChange = onInfringerChange,
                        placeholder = { Text("Contoh: Kedai Kopi Tiruan", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth().testTag("claim_infringer_name"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AlertRed)
                    )
                }

                // Input 2: Infringed Work Name (Merek/Karya yang Dilanggar)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Karya Terlindungi / Merek yang Dilanggar *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = infringedAssetName,
                        onValueChange = onInfringedAssetChange,
                        placeholder = { Text("Contoh: Foto Produk Kopi Kemasan Gayo Seller Sphere", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth().testTag("claim_infringed_asset"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AlertRed)
                    )
                }

                // Input 3: URL/Infringing Link
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tautan / Link Lapak Produk Pelanggar *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = infringingLink,
                        onValueChange = onLinkChange,
                        placeholder = { Text("Contoh: https://sellersphere.com/barang/tiruan-123", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth().testTag("claim_infringing_link"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AlertRed)
                    )
                }

                // Input 4: Evidence Link (Opsional)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tautan Dokumen / Bukti Kepemilikan (Sertifikat / File Asli)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = evidenceUrl,
                        onValueChange = onEvidenceChange,
                        placeholder = { Text("Contoh: Tautan Google Drive sertifikat HAKI", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth().testTag("claim_evidence_link"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AlertRed)
                    )
                }

                // Agreement Statement Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = statementChecked,
                        onCheckedChange = onCheckedChange,
                        modifier = Modifier.testTag("claim_checkbox_verify")
                    )
                    Text(
                        text = "Saya menyatakan dengan sungguh-sungguh bahwa saya adalah pemilik sah atas hak kekayaan intelektual yang dilanggar tersebut di atas dan bersedia menerima konsekuensi hukum jika data palsu.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Submit button
                Button(
                    onClick = onSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("claim_submit_btn")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Kirim Permohonan Takedown Resmi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun IprConceptCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    guideline: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            // Guideline pill block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconTint.copy(alpha = 0.05f))
                    .padding(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(12.dp).padding(top = 1.dp)
                    )
                    Text(
                        text = guideline,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
