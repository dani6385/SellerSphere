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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.WarmOrange
import com.example.ui.theme.SoftTeal
import com.example.ui.viewmodel.AppViewModel
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

    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Initialize with a beautiful welcome message
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Halo! Saya adalah **Asisten AI Seller Sphere**. Saya siap membantu Anda menganalisis inventaris, memantau stok barang, penjualan, dan memberikan saran bisnis untuk toko **$storeName**.\n\nApa yang bisa saya bantu hari ini?",
                isUser = false
            )
        )
    }

    var isLoading by remember { mutableStateOf(false) }

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
                    "Luar biasa! Saat ini tidak ada produk di toko **$storeName** yang berada di bawah batas minimum stok. Semua stok barang Anda aman dan mencukupi untuk melayani transaksi pelanggan."
                } else {
                    val listBuilder = StringBuilder()
                    listBuilder.append("Saat ini terdapat **${lowStockProducts.size} produk** yang hampir habis dan memerlukan perhatian Anda:\n\n")
                    lowStockProducts.forEach { prod ->
                        listBuilder.append("🔴 **${prod.name}**\n")
                        listBuilder.append("   • Sisa Stok: **${prod.stock} Unit**\n")
                        listBuilder.append("   • Batas Min: ${prod.minStockThreshold} Unit\n")
                        listBuilder.append("   • Estimasi Modal Re-stock: Rp ${formatNumber(prod.purchasePrice * (prod.minStockThreshold * 2))}\n\n")
                    }
                    listBuilder.append("💡 **Saran Asisten:** Segera hubungi pemasok Anda untuk melakukan pemesanan ulang agar menghindari potensi kehilangan peluang penjualan ketika pelanggan berbelanja.")
                    listBuilder.toString()
                }
            }
            q.contains("penjualan") || q.contains("transaksi") || q.contains("omset") || q.contains("untung") || q.contains("laba") || q.contains("dasbor") || q.contains("target") -> {
                val totalRevenue = transactions.sumOf { it.totalAmount }
                val totalProfit = transactions.sumOf { it.totalProfit }
                val targetText = todayTarget?.let { "Target penjualan hari ini adalah **Rp ${formatNumber(it.targetAmount)}**." } ?: "Anda belum menetapkan target nominal penjualan untuk hari ini."
                
                """
                    Berikut adalah ikhtisar ringkas kinerja toko **$storeName**:
                    
                    📊 **Statistik Penjualan:**
                    • Total Transaksi: **${transactions.size} transaksi**
                    • Estimasi Total Pendapatan: **Rp ${formatNumber(totalRevenue)}**
                    • Estimasi Total Keuntungan Bersih: **Rp ${formatNumber(totalProfit)}**
                    
                    🎯 **Status Target:**
                    • $targetText
                    
                    💡 **Saran Asisten:** Anda dapat memantau grafik perkembangan dan trend transaksi mingguan secara real-time pada tab **Dasbor** utama aplikasi.
                """.trimIndent()
            }
            q.contains("bantu") || q.contains("fitur") || q.contains("aplikasi") || q.contains("bisa apa") || q.contains("panduan") -> {
                """
                    Sebagai **Asisten AI Seller Sphere**, saya dapat membantu Anda mengelola operasional toko dengan beberapa fitur utama:
                    
                    1. 📦 **Analisis Stok & Inventaris:** Menanyakan stok produk tertentu atau meminta laporan produk yang hampir habis.
                    2. 📈 **Laporan Keuangan & Penjualan:** Menganalisis estimasi keuntungan, pendapatan, serta pelacakan target penjualan toko Anda.
                    3. 🏷️ **Pencetakan Label QR:** Memberikan rekomendasi atau cara mencetak label SKU produk di menu *Label*.
                    4. 🔄 **Sinkronisasi Multi-Device:** Memandu Anda menyambungkan database toko Anda ke cloud / perangkat lain di menu *Laporan*.
                    
                    Silakan ketik pertanyaan Anda, misalnya: *"produk apa saja yang stoknya mau habis?"* atau *"berapa estimasi keuntungan toko saya?"*
                """.trimIndent()
            }
            q.contains("halo") || q.contains("hai") || q.contains("pagi") || q.contains("siang") || q.contains("sore") || q.contains("malam") || q.contains("halo") -> {
                "Halo! Selamat datang kembali di asisten personal Anda. Ada yang bisa saya bantu hari ini untuk memajukan bisnis toko **$storeName** Anda?"
            }
            else -> {
                """
                    Saya memahami pertanyaan Anda mengenai *" $query "*. 
                    
                    Sebagai asisten toko Anda, saya mengonfirmasi status toko **$storeName** dalam kondisi prima:
                    • Jumlah produk aktif: **${products.size} barang**
                    • Produk perlu restok: **${lowStockProducts.size} barang**
                    • Total transaksi tercatat: **${transactions.size} penjualan**
                    
                    Apakah Anda ingin saya memberikan detail spesifik mengenai produk, transaksi, atau bantuan teknis operasional lainnya?
                """.trimIndent()
            }
        }
    }

    // Call Gemini API utilizing Direct REST implementation with proper error boundary & timeout
    fun callGeminiApi(prompt: String, onResult: (String) -> Unit) {
        coroutineScope.launch {
            isLoading = true
            val responseText = withContext(Dispatchers.IO) {
                // Read API key safely
                val apiKey = try {
                    com.example.BuildConfig.GEMINI_API_KEY
                } catch (e: Exception) {
                    ""
                }

                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    // Fallback instantly if API key is not configured or is the default placeholder
                    return@withContext generateSmartFallback(prompt)
                }

                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()

                    val systemInstruction = """
                        You are "Seller Sphere AI Assistant", a smart personal retail manager for the merchant store.
                        Respond warmly, politely, and highly professionally in Indonesian language.
                        Use Markdown formatting for headings, bullet points, and bold text to make it extremely clean and readable.
                        Here is the REAL-TIME context of the merchant's store inventory and transactions. 
                        Do NOT invent fictional products unless asked for simulations. Answer questions strictly based on this data:
                        
                        ${getStoreContext()}
                    """.trimIndent()

                    val jsonRequest = JSONObject().apply {
                        val contentsArray = JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", prompt)
                                    })
                                })
                            })
                        }
                        put("contents", contentsArray)

                        // Add system instruction for personality coaching
                        put("systemInstruction", JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", systemInstruction)
                                })
                            })
                        })
                    }

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = jsonRequest.toString().toRequestBody(mediaType)

                    // Use gemini-3.5-flash as default model per gemini-api skill instructions
                    val request = Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                        .post(body)
                        .build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        val jsonResponse = JSONObject(responseBody)
                        val candidates = jsonResponse.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                parts.getJSONObject(0).optString("text", "Maaf, saya tidak dapat merumuskan jawaban.")
                            } else {
                                "Maaf, terjadi kesalahan parsing format respons."
                            }
                        } else {
                            generateSmartFallback(prompt)
                        }
                    } else {
                        // Fallback on HTTP errors (e.g. Rate limits, invalid key)
                        generateSmartFallback(prompt)
                    }
                } catch (e: Exception) {
                    // Fallback on Network timeout/No internet
                    generateSmartFallback(prompt)
                }
            }

            onResult(responseText)
            isLoading = false
            
            // Scroll to the latest message smoothly
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
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
                                .background(SoftTeal) // Online indicator dot
                        )
                        Column {
                            Text(
                                text = "Asisten AI Seller Sphere",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Respons instan • Hub Bantuan Bisnis",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Suggestion quick-action chips at the top
            val suggestions = listOf(
                "⚠️ Analisis Stok Tipis",
                "📈 Omset & Keuntungan",
                "💡 Tips Promosi Toko",
                "❓ Panduan Fitur"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { label ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clickable(enabled = !isLoading) {
                                val cleanPrompt = when (label) {
                                    "⚠️ Analisis Stok Tipis" -> "Bagaimana kondisi stok barang saya yang hampir habis? Berikan detail dan saran re-stock."
                                    "📈 Omset & Keuntungan" -> "Berapa total transaksi dan omset toko saya saat ini? Tolong berikan analisis singkat."
                                    "💡 Tips Promosi Toko" -> "Berikan ide promosi taktis untuk meningkatkan penjualan barang di toko saya."
                                    else -> "Apa saja fitur aplikasi ini dan bagaimana cara menggunakannya?"
                                }
                                inputMessage = ""
                                messages.add(ChatMessage(text = cleanPrompt, isUser = true))
                                callGeminiApi(cleanPrompt) { response ->
                                    messages.add(ChatMessage(text = response, isUser = false))
                                }
                            }
                            .weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }

            // Messages chat feed
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("chat_message_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = NeonCyan
                            )
                            Text(
                                text = "Asisten AI sedang berpikir...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Input bar at the bottom
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
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = {
                            Text(
                                text = "Tanya asisten atau ketik pesan...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputMessage.isNotBlank() && !isLoading) {
                                    val userText = inputMessage.trim()
                                    inputMessage = ""
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    messages.add(ChatMessage(text = userText, isUser = true))
                                    callGeminiApi(userText) { response ->
                                        messages.add(ChatMessage(text = response, isUser = false))
                                    }
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
                            .background(if (inputMessage.isNotBlank() && !isLoading) NeonCyan else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = inputMessage.isNotBlank() && !isLoading) {
                                val userText = inputMessage.trim()
                                inputMessage = ""
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                messages.add(ChatMessage(text = userText, isUser = true))
                                callGeminiApi(userText) { response ->
                                    messages.add(ChatMessage(text = response, isUser = false))
                                }
                            }
                            .testTag("chat_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim",
                            tint = if (inputMessage.isNotBlank() && !isLoading) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
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

private fun formatNumber(number: Double): String {
    return try {
        val format = NumberFormat.getNumberInstance(Locale("in", "ID"))
        format.format(number)
    } catch (e: Exception) {
        number.toInt().toString()
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
