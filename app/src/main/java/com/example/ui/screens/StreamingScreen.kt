package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamingScreen(viewModel: AppViewModel) {
    var isLive by remember { mutableStateOf(false) }
    var viewerCount by remember { mutableStateOf(0) }
    var liveDurationSec by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Mock Live Chat messages
    val mockChatUsers = listOf("Randi", "Siska", "Budi", "Dewi", "Amir", "Vina", "Andi", "Lia", "Aris", "Mega")
    val mockChatTexts = listOf(
        "Kualitasnya bagus banget kak!",
        "Masih ada diskon promonya?",
        "Spill keranjang kuning nomor 1 dong",
        "Sisa warna apa aja ya?",
        "Bisa bayar COD ga?",
        "Ukuran XL ready kak?",
        "Udah aku checkout ya, tolong segera kirim",
        "Recommended seller mantap!",
        "Bahan bajunya adem ga kak?",
        "Ongkir ke Surabaya berapa ya?"
    )

    var chatMessages by remember { mutableStateOf(listOf<LiveChatMessage>()) }
    var sellerMessageText by remember { mutableStateOf("") }
    
    // Tab State: 0 for Live Chat, 1 for Pin Products
    var selectedTabIdx by remember { mutableStateOf(0) }

    // Selected products for live show
    val products by viewModel.products.collectAsState()
    var pinnedProductIndex by remember { mutableStateOf(-1) }
    val pinnedProduct = if (pinnedProductIndex in products.indices) products[pinnedProductIndex] else null

    // Lazy list state for scrolling chat
    val chatListState = rememberLazyListState()

    // Run live stream timer and simulation
    LaunchedEffect(isLive) {
        if (isLive) {
            viewerCount = 42
            liveDurationSec = 0
            chatMessages = listOf(
                LiveChatMessage("Sistem Live", "Mulai menyiarkan secara langsung! ✨", isSystem = true)
            )

            while (isLive) {
                delay(1000)
                liveDurationSec++
                // Random viewer fluctuating
                viewerCount += (-4..6).random().coerceAtLeast(10).coerceAtMost(350)

                // Simulated comments coming in randomly
                if ((1..10).random() > 6) {
                    val user = mockChatUsers.random()
                    val msg = mockChatTexts.random()
                    chatMessages = chatMessages + LiveChatMessage(user, msg)
                    if (chatMessages.size > 100) chatMessages = chatMessages.drop(1)
                }
            }
        } else {
            viewerCount = 0
            liveDurationSec = 0
        }
    }

    // Auto scroll chat to bottom when message list changes
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LiveTv, contentDescription = null, tint = NeonCyan)
                        Text(
                            "Live Streaming Console",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Stream Camera View Simulation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, if (isLive) AlertRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                if (isLive) {
                    // Futuristic glowing camera static lines / radar gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    ) {
                        // Drawing static scanning wave lines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = NeonCyan.copy(alpha = 0.04f),
                                radius = size.height * 0.4f,
                                center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                            )
                        }
                    }

                    // Floating Badges inside Live Simulator
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LIVE indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AlertRed)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Viewer counter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$viewerCount",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Duration timer
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = String.format("%02d:%02d", liveDurationSec / 60, liveDurationSec % 60),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    // Floating PINNED Product Overlay
                    if (pinnedProduct != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .width(135.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = pinnedProduct.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = viewModel.formatRupiah(pinnedProduct.sellingPrice),
                                    fontSize = 9.sp,
                                    color = SoftTeal,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(NeonCyan, RoundedCornerShape(4.dp))
                                        .padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("TERSEMAT", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Overlaid text to indicate camera output simulation
                    Text(
                        text = "📡 Kamera Aktif Menyiar...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                } else {
                    // Broadcast ready interface
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideocamOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Siaran Belum Dimulai",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gunakan fitur ini untuk mempromosikan produk secara langsung (live) kepada pelanggan Anda.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // SECTION 2: Interactive Tabs & Console Controls
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.weight(1.2f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header Area with Broadcast Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KONTROL INTERAKTIF",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Start/Stop Broadcast Toggle Button
                        Button(
                            onClick = { isLive = !isLive },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLive) AlertRed else SoftTeal
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("live_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isLive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isLive) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLive) "Matikan Live" else "Mulai Live",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLive) Color.White else Color.Black
                            )
                        }
                    }

                    // Navigation Tab Row between Live Chat and Products
                    TabRow(
                        selectedTabIndex = selectedTabIdx,
                        containerColor = Color.Transparent,
                        contentColor = NeonCyan,
                        divider = {
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedTabIdx == 0,
                            onClick = { selectedTabIdx = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Live Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                        Tab(
                            selected = selectedTabIdx == 1,
                            onClick = { selectedTabIdx = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Sematkan Produk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    // Content based on selected Tab
                    when (selectedTabIdx) {
                        0 -> {
                            // TAB 1: Live Chat Console
                            if (!isLive) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Nyalakan Live untuk membuka interaksi chat dengan pelanggan.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Chat Message Board
                                    LazyColumn(
                                        state = chatListState,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(chatMessages) { msg ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (msg.isSeller) NeonCyan.copy(alpha = 0.12f)
                                                            else if (msg.isSystem) MaterialTheme.colorScheme.surfaceVariant
                                                            else MaterialTheme.colorScheme.surface
                                                        )
                                                        .border(
                                                            width = 0.5.dp,
                                                            color = if (msg.isSeller) NeonCyan.copy(alpha = 0.4f) else Color.Transparent,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (msg.isSeller) "Penjual 👑: " else "${msg.sender}: ",
                                                        color = if (msg.isSeller) NeonCyan else if (msg.isSystem) SoftTeal else VividOrchid,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                    Text(
                                                        text = msg.message,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Quick replies section for quick interaction
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val quickReplies = listOf(
                                            "Ready Kak! Silakan di-co",
                                            "Bisa COD seluruh wilayah!",
                                            "Lagi ada diskon 10% ya",
                                            "Kualitas dijamin original!",
                                            "Langsung dikirim hari ini!"
                                        )

                                        quickReplies.forEach { text ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        chatMessages = chatMessages + LiveChatMessage(
                                                            sender = "Penjual",
                                                            message = text,
                                                            isSeller = true
                                                        )
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = text,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // Custom Message Input Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = sellerMessageText,
                                            onValueChange = { sellerMessageText = it },
                                            placeholder = { Text("Tulis balasan chat...", fontSize = 11.sp) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("live_chat_input"),
                                            singleLine = true,
                                            shape = RoundedCornerShape(22.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                focusedBorderColor = NeonCyan
                                            ),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                                        )

                                        IconButton(
                                            onClick = {
                                                if (sellerMessageText.isNotBlank()) {
                                                    chatMessages = chatMessages + LiveChatMessage(
                                                        sender = "Penjual",
                                                        message = sellerMessageText.trim(),
                                                        isSeller = true
                                                    )
                                                    sellerMessageText = ""
                                                }
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(NeonCyan, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Kirim",
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // TAB 2: Product Pinning Console
                            if (products.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Tidak ada barang di stok untuk ditawarkan.", fontSize = 12.sp)
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "Ketuk 'Sematkan' untuk menampilkan widget harga produk di layar siaran pembeli.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )

                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(products.indices.toList()) { index ->
                                            val prod = products[index]
                                            val isPinned = pinnedProductIndex == index

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                    .border(0.5.dp, if (isPinned) NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = prod.name,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = viewModel.formatRupiah(prod.sellingPrice),
                                                            fontSize = 11.sp,
                                                            color = SoftTeal,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "Stok: ${prod.stock}",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Button(
                                                    onClick = {
                                                        pinnedProductIndex = if (isPinned) -1 else index
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isPinned) NeonCyan else MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.height(28.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                                ) {
                                                    Text(
                                                        text = if (isPinned) "Tersemat" else "Sematkan",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isPinned) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
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
    }
}

data class LiveChatMessage(
    val sender: String,
    val message: String,
    val isSystem: Boolean = false,
    val isSeller: Boolean = false
)
