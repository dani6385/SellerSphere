package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.SoftTeal
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.BuyerChat
import com.example.ui.viewmodel.BuyerMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val products by viewModel.products.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val storeName by viewModel.customStoreName.collectAsState()
    val todayTarget by viewModel.todayTarget.collectAsState()
    
    val buyerChats by viewModel.buyerChats.collectAsState()
    val activeChatBuyerName by viewModel.activeChatBuyerName.collectAsState()
    
    var aiInputMessage by remember { mutableStateOf("") }
    var buyerInputMessage by remember { mutableStateOf("") }
    
    val aiListState = rememberLazyListState()
    val buyerListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Initialize AI Assistant chat history
    val aiMessages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Halo! Saya adalah **Asisten AI Seller Sphere**. Saya siap membantu Anda menganalisis inventaris, memantau stok barang, penjualan, dan memberikan saran bisnis untuk toko **$storeName**.\n\nApa yang bisa saya bantu hari ini?",
                isUser = false
            )
        )
    }

    var isAiLoading by remember { mutableStateOf(false) }

    // Helper functions to generate local analytics context for Gemini or Fallback
    fun getStoreContext(): String {
        val lowStockNames = lowStockProducts.joinToString(", ") { "${it.name} (Sisa ${it.stock} unit, batasan minimum ${it.minStockThreshold})" }
        val allProductsSummary = products.joinToString("\n") { "- ${it.name}: ${it.stock} Unit - Harga Jual: Rp ${formatNumber(it.sellingPrice)}" }
        val targetText = todayTarget?.let { "Target Penjualan Hari Ini: Rp ${formatNumber(it.targetAmount)}" } ?: "Belum ada target penjualan hari ini."

        return """
            Nama Toko: $storeName
            $targetText
            Jumlah Produk Terdaftar: ${products.size}
            Produk Hampir Habis: ${if (lowStockProducts.isEmpty()) "Tidak ada" else lowStockNames}
            Rincian Inventaris:
            $allProductsSummary
        """.trimIndent()
    }

    // High performance fallback smart response logic
    fun generateSmartFallback(query: String): String {
        val q = query.lowercase(Locale.ROOT)
        return when {
            q.contains("stok") || q.contains("habis") || q.contains("hampir") || q.contains("kurang") || q.contains("restock") || q.contains("re-stock") -> {
                if (lowStockProducts.isEmpty()) {
                    "Luar biasa! Saat ini tidak ada produk di toko **$storeName** yang berada di bawah batas minimum stok. Semua stok barang Anda aman dan mencukupi."
                } else {
                    val listBuilder = StringBuilder()
                    listBuilder.append("Saat ini terdapat **${lowStockProducts.size} produk** yang hampir habis:\n\n")
                    lowStockProducts.forEach { prod ->
                        listBuilder.append("🔴 **${prod.name}**\n")
                        listBuilder.append("   • Sisa Stok: **${prod.stock} Unit**\n")
                        listBuilder.append("   • Batas Min: ${prod.minStockThreshold} Unit\n\n")
                    }
                    listBuilder.append("💡 **Saran:** Segera hubungi pemasok Anda untuk melakukan pemesanan ulang.")
                    listBuilder.toString()
                }
            }
            q.contains("penjualan") || q.contains("transaksi") || q.contains("omset") || q.contains("untung") || q.contains("laba") || q.contains("dasbor") || q.contains("target") -> {
                val totalRevenue = transactions.sumOf { it.totalAmount }
                val totalProfit = transactions.sumOf { it.totalProfit }
                val targetText = todayTarget?.let { "Target penjualan hari ini adalah **Rp ${formatNumber(it.targetAmount)}**." } ?: "Anda belum menetapkan target nominal penjualan untuk hari ini."
                
                """
                    Berikut adalah ringkasan kinerja toko **$storeName**:
                    
                    📊 **Statistik Penjualan:**
                    • Total Transaksi: **${transactions.size} transaksi**
                    • Estimasi Pendapatan: **Rp ${formatNumber(totalRevenue)}**
                    • Estimasi Keuntungan: **Rp ${formatNumber(totalProfit)}**
                    
                    🎯 **Status Target:**
                    • $targetText
                """.trimIndent()
            }
            else -> {
                """
                    Asisten mengonfirmasi status toko **$storeName** dalam kondisi prima:
                    • Jumlah produk aktif: **${products.size} barang**
                    • Produk perlu restok: **${lowStockProducts.size} barang**
                    • Total transaksi tercatat: **${transactions.size} penjualan**
                """.trimIndent()
            }
        }
    }

    

    val totalUnread = buyerChats.sumOf { it.unreadCount }

    // Sorting chats: Priority to unread, then reverse chronological order of the last message timestamp
    val sortedChats = remember(buyerChats) {
        buyerChats.sortedWith(
            compareByDescending<BuyerChat> { it.unreadCount > 0 }
                .thenByDescending { it.lastMessageTimestamp }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(SoftTeal)
                        )
                        Column {
                            Text(
                                text = if (activeChatBuyerName == null) "Hub Obrolan Toko" else "Obrolan Pembeli",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (activeChatBuyerName == null) "Asisten AI & Pesan Pelanggan" else "Sedang terhubung dengan $activeChatBuyerName",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (activeChatBuyerName != null) {
                                viewModel.activeChatBuyerName.value = null
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("chat_top_bar")
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (activeChatBuyerName == null) {
                // SINGLE COMBINED PAGE
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // SECTION 1: ASISTEN AI (At the very top)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.25f)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                // AI Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SmartToy,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Asisten AI Pintar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Tanya stok, omset, & saran penjualan",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SoftTeal.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ONLINE",
                                            color = SoftTeal,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // AI Message History Area (bounded box height)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    LazyColumn(
                                        state = aiListState,
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(aiMessages) { msg ->
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                                            ) {
                                                Surface(
                                                    color = if (msg.isUser) NeonCyan else MaterialTheme.colorScheme.surfaceVariant,
                                                    contentColor = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = msg.text,
                                                        fontSize = 12.sp,
                                                        lineHeight = 16.sp,
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = formatTime(msg.timestamp),
                                                    fontSize = 8.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }

                                        if (isAiLoading) {
                                            item {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(12.dp),
                                                        strokeWidth = 1.5.dp,
                                                        color = NeonCyan
                                                    )
                                                    Text(
                                                        text = "AI sedang mengetik...",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Action Chips
                                val suggestions = listOf(
                                    "⚠️ Stok Habis",
                                    "📊 Omset & Untung",
                                    "❓ Panduan Fitur"
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    suggestions.forEach { label ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                                .clickable(enabled = !isAiLoading) {
                                                    val prompt = when (label) {
                                                        "⚠️ Stok Habis" -> "Apakah ada produk saya yang stoknya mau habis? Berikan analisa."
                                                        "📊 Omset & Untung" -> "Berapa omset dan perkiraan laba toko saya?"
                                                        else -> "Bagaimana cara menggunakan asisten ini?"
                                                    }
                                                    aiMessages.add(ChatMessage(text = prompt, isUser = true))
                                                    callGeminiApi(prompt) { response ->
                                                        aiMessages.add(ChatMessage(text = response, isUser = false))
                                                    }
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NeonCyan
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // AI Message Input Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = aiInputMessage,
                                        onValueChange = { aiInputMessage = it },
                                        placeholder = {
                                            Text(
                                                text = "Ketik pesan untuk AI...",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        maxLines = 1,
                                        keyboardOptions = KeyboardOptions(
                                            capitalization = KeyboardCapitalization.Sentences,
                                            imeAction = ImeAction.Send
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                if (aiInputMessage.isNotBlank() && !isAiLoading) {
                                                    val prompt = aiInputMessage.trim()
                                                    aiInputMessage = ""
                                                    focusManager.clearFocus()
                                                    keyboardController?.hide()
                                                    aiMessages.add(ChatMessage(text = prompt, isUser = true))
                                                    callGeminiApi(prompt) { response ->
                                                        aiMessages.add(ChatMessage(text = response, isUser = false))
                                                    }
                                                }
                                            }
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (aiInputMessage.isNotBlank() && !isAiLoading) NeonCyan else MaterialTheme.colorScheme.surface)
                                            .clickable(enabled = aiInputMessage.isNotBlank() && !isAiLoading) {
                                                val prompt = aiInputMessage.trim()
                                                aiInputMessage = ""
                                                focusManager.clearFocus()
                                                keyboardController?.hide()
                                                aiMessages.add(ChatMessage(text = prompt, isUser = true))
                                                callGeminiApi(prompt) { response ->
                                                    aiMessages.add(ChatMessage(text = response, isUser = false))
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Kirim",
                                            tint = if (aiInputMessage.isNotBlank() && !isAiLoading) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 2: SECTION HEADER FOR BUYER MESSAGES
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = SoftTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Pesan Masuk Pembeli",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (totalUnread > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Red)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "$totalUnread Belum Dibaca",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 3: LIST OF CUSTOMERS
                    if (sortedChats.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text("Tidak ada pesan dari pembeli", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        items(sortedChats) { buyerChat ->
                            val lastMsg = buyerChat.messages.lastOrNull()?.text ?: "Tidak ada pesan"
                            val hasUnread = buyerChat.unreadCount > 0

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        viewModel.activeChatBuyerName.value = buyerChat.customerName
                                        viewModel.markChatAsRead(buyerChat.customerName)
                                    }
                                    .testTag("buyer_chat_item_${buyerChat.customerName.lowercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (hasUnread) NeonCyan.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = if (hasUnread) BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Customer Initials Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(if (hasUnread) NeonCyan else SoftTeal.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = buyerChat.customerName.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasUnread) Color.Black else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                    }

                                    // Content Texts
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = buyerChat.customerName,
                                                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = formatTimeAndDate(buyerChat.lastMessageTimestamp),
                                                fontSize = 10.sp,
                                                color = if (hasUnread) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(3.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = lastMsg,
                                                fontSize = 12.sp,
                                                color = if (hasUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            
                                            if (hasUnread) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(start = 8.dp)
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Red),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = buyerChat.unreadCount.toString(),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
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
                }
            } else {
                // NESTED ACTIVE CHAT SCREEN WITH BUYER
                val currentBuyerName = activeChatBuyerName ?: ""
                val currentChat = buyerChats.find { it.customerName.equals(currentBuyerName, ignoreCase = true) }
                val currentMessages = currentChat?.messages ?: emptyList()

                // Mark messages read & auto-scroll
                LaunchedEffect(currentMessages.size) {
                    viewModel.markChatAsRead(currentBuyerName)
                }

                LaunchedEffect(currentMessages.size) {
                    if (currentMessages.isNotEmpty()) {
                        buyerListState.animateScrollToItem(currentMessages.size - 1)
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat header panel
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.activeChatBuyerName.value = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali ke Daftar",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SoftTeal.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentBuyerName.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                            }

                            Column {
                                Text(
                                    text = currentBuyerName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Pelanggan • Online",
                                    fontSize = 11.sp,
                                    color = SoftTeal
                                )
                            }
                        }
                    }

                    // Messages LazyColumn
                    LazyColumn(
                        state = buyerListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
                    ) {
                        items(currentMessages) { bMsg ->
                            BuyerChatBubble(message = bMsg)
                        }
                    }

                    // Input footer panel
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                                .fillMaxWidth()
                                .imePadding(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = buyerInputMessage,
                                onValueChange = { buyerInputMessage = it },
                                placeholder = {
                                    Text(
                                        text = "Ketik balasan untuk $currentBuyerName...",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("buyer_chat_input_field"),
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Send
                                ),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (buyerInputMessage.isNotBlank()) {
                                            val textToSend = buyerInputMessage.trim()
                                            buyerInputMessage = ""
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            viewModel.sendMessageToBuyer(currentBuyerName, textToSend)
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    focusedContainerColor = MaterialTheme.colorScheme.background,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                                )
                            )

                            // Send Button
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (buyerInputMessage.isNotBlank()) NeonCyan else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable(enabled = buyerInputMessage.isNotBlank()) {
                                        val textToSend = buyerInputMessage.trim()
                                        buyerInputMessage = ""
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        viewModel.sendMessageToBuyer(currentBuyerName, textToSend)
                                    }
                                    .testTag("buyer_chat_send_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Kirim",
                                    tint = if (buyerInputMessage.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiChatBubble(message: ChatMessage) {
    val bubbleColor = if (message.isUser) NeonCyan else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val shape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp, top = 4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Surface(
                color = bubbleColor,
                contentColor = contentColor,
                shape = shape,
                border = if (!message.isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)) else null
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = formatTime(message.timestamp),
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(
                start = if (message.isUser) 0.dp else 36.dp,
                end = if (message.isUser) 4.dp else 0.dp
            )
        )
    }
}

@Composable
fun BuyerChatBubble(message: BuyerMessage) {
    val bubbleColor = if (!message.isFromBuyer) NeonCyan else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (!message.isFromBuyer) Color.White else MaterialTheme.colorScheme.onSurface
    val alignment = if (!message.isFromBuyer) Alignment.End else Alignment.Start
    val shape = if (!message.isFromBuyer) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            horizontalArrangement = if (!message.isFromBuyer) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (message.isFromBuyer) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp, top = 4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SoftTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = SoftTeal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Surface(
                color = bubbleColor,
                contentColor = contentColor,
                shape = shape,
                border = if (message.isFromBuyer) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)) else null
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = formatTime(message.timestamp),
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(
                start = if (!message.isFromBuyer) 0.dp else 36.dp,
                end = if (!message.isFromBuyer) 4.dp else 0.dp
            )
        )
    }
}

private fun formatNumber(number: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("in", "ID"))
    return format.format(number)
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTimeAndDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
