package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.LabelPrinterScreen
import com.example.ui.screens.NotificationScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SyncSettingsScreen
import com.example.ui.screens.TransactionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SoftTeal
import com.example.ui.theme.WarmOrange
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.delay

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.R
import java.util.Locale
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainShell(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(viewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // UI Toast Notification Overlay State
    var toastTitle by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    // Listen to ViewModel Notifications
    LaunchedEffect(Unit) {
        viewModel.notificationFlow.collect { notif ->
            toastTitle = notif.title
            toastMessage = notif.message
            showToast = true
            delay(4000)
            showToast = false
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Drawer Header with nice glowing cyber-synth vertical gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        NeonCyan.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_profile_avatar),
                                    contentDescription = "Profile Avatar",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, NeonCyan, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(NeonCyan.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "PRO SELLER",
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                drawerState.close()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("close_drawer_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Tutup Menu",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Dani",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "dani6385@gmail.com",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Pemilik Toko",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftTeal
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Account Settings Menu Section
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PENGATURAN AKUN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )

                        // Menu Item 0: Kembali ke Dasbor Utama
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        drawerState.close()
                                    }
                                    if (currentRoute != "dasbor") {
                                        navController.navigate("dasbor") {
                                            popUpTo(navController.graph.findStartDestination()?.id ?: 0) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Kembali ke Dasbor Utama",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        var isAccountInfoExpanded by remember { mutableStateOf(false) }
                        
                        // Menu Item 1: Detail Profil (Expandable)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isAccountInfoExpanded = !isAccountInfoExpanded },
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Detail Profil Saya",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isAccountInfoExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (isAccountInfoExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ProfileDetailRow("Peran", "Owner & Administrator")
                                    ProfileDetailRow("Wilayah", "Jakarta, Indonesia")
                                    ProfileDetailRow("ID Pengguna", "USR-6385-DANI")
                                    ProfileDetailRow("Status Akun", "Premium Aktif \u2728")
                                }
                            }
                        }

                        // Menu Item 2: Statistik Bisnis (Live data!)
                        var isStatsExpanded by remember { mutableStateOf(false) }
                        val productsState by viewModel.products.collectAsState()
                        val transactionsState by viewModel.transactions.collectAsState()
                        val lowStockState by viewModel.lowStockProducts.collectAsState()
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isStatsExpanded = !isStatsExpanded },
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = SoftTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Ringkasan Toko",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isStatsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (isStatsExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val totalProducts = productsState.size
                                    val totalTransactions = transactionsState.size
                                    val totalSalesOmzet = transactionsState.sumOf { it.totalAmount }
                                    
                                    ProfileDetailRow("Total Produk", "$totalProducts item")
                                    ProfileDetailRow("Transaksi", "$totalTransactions transaksi")
                                    ProfileDetailRow("Total Omzet", "Rp " + String.format(Locale.GERMAN, "%,.0f", totalSalesOmzet.toDouble()))
                                    
                                    if (lowStockState.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = WarmOrange,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${lowStockState.size} produk stok menipis!",
                                                color = WarmOrange,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Theme and Sync Section
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PREFERENSI & KONEKSI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )

                        // Theme switch row
                        val isDarkTheme by viewModel.isDarkTheme.collectAsState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) NeonCyan else WarmOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Mode Gelap",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { viewModel.toggleDarkTheme() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonCyan,
                                    checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }

                        // Sync Status Row
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("laporan_sync") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    scope.launch { drawerState.close() }
                                },
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Status Sinkronisasi",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Cloud RTDB Aktif",
                                        fontSize = 11.sp,
                                        color = SoftTeal
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Help & Developer Section
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "BANTUAN & INFORMASI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )

                        // Help Item
                        var isHelpExpanded by remember { mutableStateOf(false) }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isHelpExpanded = !isHelpExpanded },
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Pusat Bantuan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isHelpExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (isHelpExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "• Kasir Elektronik: Kelola penjualan Anda secara instan dan cetak struk digital.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "• Cetak Label QR: Hasilkan kode QR produk langsung untuk scan cepat di menu label.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "• Sinkronisasi Awan: Cadangkan data Anda ke Firebase Realtime Database kapan saja.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Drawer Footer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Seller Sphere Pro",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NeonCyan
                            )
                            Text(
                                text = "Versi 1.2.0 • Build Premium",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) {
        var showSettingsSheet by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
            topBar = {
                if (currentRoute != "notifikasi") {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_profile_avatar),
                                    contentDescription = "Profil Akun",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, NeonCyan, CircleShape)
                                        .clickable {
                                            scope.launch {
                                                drawerState.open()
                                            }
                                        },
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = "Seller Sphere",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        actions = {
                            // Small mock notification hub button showing alerts count
                            val lowStockList by viewModel.lowStockProducts.collectAsState()
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clickable {
                                        navController.navigate("notifikasi") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Alerts",
                                    tint = if (lowStockList.isNotEmpty()) WarmOrange else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (lowStockList.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(WarmOrange)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }

                            // Chat Button right next to notification bell
                            val buyerChats by viewModel.buyerChats.collectAsState()
                            val totalUnreadChats = buyerChats.sumOf { it.unreadCount }

                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                                    .clickable {
                                        navController.navigate("chat") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .testTag("app_chat_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Chat,
                                    contentDescription = "Chat Asisten AI",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                if (totalUnreadChats > 0) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp, end = 4.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }

                            // Settings Button next to notification bell
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                                    .clickable {
                                        showSettingsSheet = true
                                    }
                                    .testTag("settings_toggle_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Pengaturan",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier.testTag("app_top_bar")
                    )
                }
            },
            bottomBar = {
                if (currentRoute != "notifikasi" && currentRoute != "chat") {
                    val items = listOf(
                        CustomNavigationItem("dasbor", "Dasbor", Icons.Default.Home, "nav_item_dasbor"),
                        CustomNavigationItem("barang", "Stok", Icons.Default.Category, "nav_item_barang"),
                        CustomNavigationItem("kasir", "Transaksi", Icons.Default.Receipt, "nav_item_kasir", isCentral = true),
                        CustomNavigationItem("label", "Label", Icons.Default.QrCode, "nav_item_label"),
                        CustomNavigationItem("laporan_sync", "Laporan", Icons.Default.LocalShipping, "nav_item_laporan")
                    )

                    Box(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            .fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("app_bottom_nav_bar")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items.forEach { item ->
                                    val isSelected = currentRoute == item.route

                                    if (item.isCentral) {
                                        // Special highlighted circular button (Kasir)
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) NeonCyan else NeonCyan.copy(alpha = 0.12f)
                                                )
                                                .clickable {
                                                    if (currentRoute != item.route) {
                                                        navController.navigate(item.route) {
                                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                }
                                                .testTag(item.testTag)
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label,
                                                modifier = Modifier.size(26.dp),
                                                tint = if (isSelected) Color(0xFF020617) else NeonCyan
                                            )
                                        }
                                    } else {
                                        // Normal items (Dashboard, Inventory, Label, Reports)
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    if (currentRoute != item.route) {
                                                        navController.navigate(item.route) {
                                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                }
                                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                                .testTag(item.testTag)
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label,
                                                tint = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = item.label,
                                                color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main NavHost Setup
                NavHost(
                    navController = navController,
                    startDestination = "dasbor",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("dasbor") {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToInventory = {
                                navController.navigate("barang") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToTransactions = {
                                navController.navigate("kasir") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    composable("barang") {
                        InventoryScreen(
                            viewModel = viewModel,
                            onNavigateToLabelPrinter = { product ->
                                navController.navigate("label") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    composable("kasir") {
                        TransactionScreen(viewModel = viewModel)
                    }

                    composable("label") {
                        LabelPrinterScreen(viewModel = viewModel)
                    }

                    composable("laporan_sync") {
                        LaporanSyncCombinedTabScreen(viewModel = viewModel)
                    }

                    composable("notifikasi") {
                        NotificationScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToInventory = {
                                navController.navigate("barang") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    composable("chat") {
                        ChatScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }

                // Real-time Sliding In-App Toast Notification
                AnimatedVisibility(
                    visible = showToast,
                    enter = slideInVertically(initialOffsetY = { -it }),
                    exit = slideOutVertically(targetOffsetY = { -it }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .testTag("toast_overlay")
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = toastTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NeonCyan
                                )
                                Text(
                                    text = toastMessage,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }

            // Sliding Settings Overlay!
            AnimatedVisibility(
                visible = showSettingsSheet,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                // Semi-transparent dim background
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left empty click-to-dismiss zone
                        Box(
                            modifier = Modifier
                                .weight(0.15f)
                                .fillMaxHeight()
                                .clickable { showSettingsSheet = false }
                        )
                        
                        // Main settings content on the right (85% width)
                        Surface(
                            modifier = Modifier
                                .weight(0.85f)
                                .fillMaxHeight(),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                // Header
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(
                                                    NeonCyan.copy(alpha = 0.15f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                        .padding(top = 48.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = null,
                                                tint = NeonCyan,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Pengaturan",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                                                .clickable { showSettingsSheet = false },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                                
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                
                                // Scrollable settings body
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    // Section 1: Identitas Toko
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "IDENTITAS TOKO",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                        
                                        val currentStoreName by viewModel.customStoreName.collectAsState()
                                        var tempStoreName by remember(currentStoreName) { mutableStateOf(currentStoreName) }
                                        
                                        OutlinedTextField(
                                            value = tempStoreName,
                                            onValueChange = { tempStoreName = it },
                                            label = { Text("Nama Toko") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NeonCyan,
                                                focusedLabelColor = NeonCyan,
                                                cursorColor = NeonCyan
                                            ),
                                            trailingIcon = {
                                                if (tempStoreName != currentStoreName) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Save",
                                                        tint = SoftTeal,
                                                        modifier = Modifier.clickable {
                                                            viewModel.updateCustomStoreName(tempStoreName)
                                                            viewModel.addSyncLog("Nama Toko kustom diubah ke: $tempStoreName")
                                                        }
                                                    )
                                                }
                                            }
                                        )
                                    }
                                    
                                    // Section 2: Metode Pembayaran Default (Recommended)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "METODE PEMBAYARAN UTAMA",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                        Text(
                                            text = "Rekomendasi metode pembayaran default saat membuka kasir penjualan.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        val defaultPayment by viewModel.defaultPaymentMethod.collectAsState()
                                        val paymentOptions = listOf("Tunai", "QRIS", "Transfer")
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            paymentOptions.forEach { method ->
                                                val isSelected = defaultPayment == method
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (isSelected) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) NeonCyan else Color.Transparent,
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable {
                                                            viewModel.updateDefaultPaymentMethod(method)
                                                        }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = method,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    // Section 3: Koneksi Cloud RTDB
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "INTEGRASI CLOUD & FIREBASE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                        Text(
                                            text = "Kelola server real-time toko Anda agar stok & penjualan selalu tersinkronisasi otomatis.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        val dbUrl by viewModel.rtdbUrl.collectAsState()
                                        var tempDbUrl by remember(dbUrl) { mutableStateOf(dbUrl) }
                                        
                                        OutlinedTextField(
                                            value = tempDbUrl,
                                            onValueChange = { tempDbUrl = it },
                                            label = { Text("URL Firebase RTDB") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NeonCyan,
                                                focusedLabelColor = NeonCyan,
                                                cursorColor = NeonCyan
                                            )
                                        )
                                        
                                        val hasChanges = tempDbUrl != dbUrl
                                        
                                        Button(
                                            onClick = {
                                                viewModel.updateRtdbUrl(tempDbUrl)
                                                viewModel.addSyncLog("Koneksi Cloud diperbarui!")
                                            },
                                            enabled = hasChanges,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = SoftTeal,
                                                contentColor = Color.Black,
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Simpan & Hubungkan Database", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    // Section 4: Fitur Rekomendasi Pintar AI (Highly recommended)
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "REKOMENDASI FITUR PINTAR AI",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                        
                                        // Switch 1: AI Auto-Description
                                        var autoAiDesc by remember { mutableStateOf(true) }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(0.8f)) {
                                                Text(
                                                    text = "Auto-Generate Deskripsi Produk (Gemini AI)",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Buat deskripsi promosi otomatis menggunakan AI saat menambah stok.",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = autoAiDesc,
                                                onCheckedChange = { autoAiDesc = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.3f))
                                            )
                                        }
                                        
                                        // Switch 2: Notifikasi otomatis stok rendah
                                        var autoStockAlert by remember { mutableStateOf(true) }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(0.8f)) {
                                                Text(
                                                    text = "Notifikasi Otomatis Stok Rendah",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Berikan peringatan berkala di bar atas saat stok produk menipis di bawah limit.",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = autoStockAlert,
                                                onCheckedChange = { autoStockAlert = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.3f))
                                            )
                                        }
                                        
                                        // Decorative Recommendation Card
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(NeonCyan.copy(alpha = 0.05f))
                                                .border(1.dp, NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                .padding(14.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(SoftTeal)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Tips Optimalisasi Bisnis",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SoftTeal
                                                    )
                                                }
                                                Text(
                                                    text = "Aktifkan sinkronisasi otomatis awan untuk membagikan katalog produk ke customer Anda secara real-time via scan barcode/QR.",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Footer version
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Seller Sphere Settings v1.2.0",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
fun LaporanSyncCombinedTabScreen(viewModel: AppViewModel) {
    var activeSubTab by remember { mutableStateOf(0) } // 0: Laporan Keuangan, 1: Sinkronisasi Awan

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                    color = NeonCyan
                )
            }
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Rekap Keuangan", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                selectedContentColor = NeonCyan,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Sinkronisasi Awan", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                selectedContentColor = NeonCyan,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (activeSubTab == 0) {
                ReportScreen(viewModel = viewModel)
            } else {
                SyncSettingsScreen(viewModel = viewModel)
            }
        }
    }
}

private data class CustomNavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String,
    val isCentral: Boolean = false
)

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

