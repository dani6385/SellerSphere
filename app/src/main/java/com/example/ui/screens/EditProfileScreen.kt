package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val ownerNameState by viewModel.ownerName.collectAsState()
    val ownerEmailState by viewModel.ownerEmail.collectAsState()
    val storeNameState by viewModel.customStoreName.collectAsState()
    val addressState by viewModel.pickupAddress.collectAsState()
    val latitudeState by viewModel.pickupLatitude.collectAsState()
    val longitudeState by viewModel.pickupLongitude.collectAsState()
    val notesState by viewModel.pickupNotes.collectAsState()

    var ownerName by remember { mutableStateOf(ownerNameState) }
    var ownerEmail by remember { mutableStateOf(ownerEmailState) }
    var storeName by remember { mutableStateOf(storeNameState) }
    var address by remember { mutableStateOf(addressState) }
    var notes by remember { mutableStateOf(notesState) }

    // Map pin offsets based on lat/lng mapped relative to an interactive center
    // Base center of Jakarta is -6.1751, 106.8272
    var pinLatitude by remember { mutableStateOf(latitudeState) }
    var pinLongitude by remember { mutableStateOf(longitudeState) }

    // For map display bounds mapping (zoom & pan emulation)
    var mapZoom by remember { mutableStateOf(15f) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profil & Lokasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("edit_profile_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = NeonCyan
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
            // SECTION 1: Personal Profile
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
                        Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        Text(
                            text = "INFORMASI PROFIL TOKO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Nama Pemilik") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_owner_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = ownerEmail,
                        onValueChange = { ownerEmail = it },
                        label = { Text("Email Kontak") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Nama Toko") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_store_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // SECTION 2: Map Pinpoint Selection
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = SoftTeal, modifier = Modifier.size(20.dp))
                            Text(
                                text = "PINPOINT LOKASI PENJEMPUTAN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftTeal
                            )
                        }

                        // GPS Trigger button
                        IconButton(
                            onClick = {
                                // Simulate retrieving exact high-accuracy GPS coordinates for Jakarta Central
                                pinLatitude = -6.175392f
                                pinLongitude = 106.827153f
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Berhasil sinkronisasi dengan GPS internal!")
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(SoftTeal.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "Dapatkan GPS",
                                tint = SoftTeal,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "Ketuk pada peta futuristik di bawah untuk memindahkan pinpoint koordinat toko Anda.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    // Interactive Custom Map Drawing Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .background(GuardianDarkBackground)
                    ) {
                        val mapOutlineColor = MaterialTheme.colorScheme.outline

                        // Interactive pointer tapping input
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        // Map X, Y coordinate offset to Lat/Lng offsets around Jakarta base center
                                        // Width center: 106.8272, Height center: -6.1751
                                        val width = size.width
                                        val height = size.height

                                        val dx = (offset.x - width / 2f) / (width / 2f)
                                        val dy = (offset.y - height / 2f) / (height / 2f)

                                        // Map normalized values (-1 to 1) to realistic coordinates span
                                        pinLongitude = 106.8272f + dx * 0.015f
                                        pinLatitude = -6.1751f - dy * 0.015f // lat is inverted in screen space
                                    }
                                }
                        ) {
                            val w = size.width
                            val h = size.height

                            // Draw Radar grid lines (Cyber Synth grid aesthetic)
                            val gridSpacing = 40.dp.toPx()
                            val lineStroke = Stroke(width = 1.dp.toPx())

                            // Horizontal Grid Lines
                            var y = 0f
                            while (y < h) {
                                drawLine(
                                    color = mapOutlineColor.copy(alpha = 0.12f),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                                y += gridSpacing
                            }

                            // Vertical Grid Lines
                            var x = 0f
                            while (x < w) {
                                drawLine(
                                    color = mapOutlineColor.copy(alpha = 0.12f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, h),
                                    strokeWidth = 1.dp.toPx()
                                )
                                x += gridSpacing
                            }

                            // Draw simulated Cyber Roads & Water bodies
                            val roadStroke = Stroke(width = 3.dp.toPx())
                            val mainRoadStroke = Stroke(width = 6.dp.toPx())

                            // Diagonal major avenue
                            drawLine(
                                color = NeonCyan.copy(alpha = 0.18f),
                                start = Offset(0f, h * 0.2f),
                                end = Offset(w, h * 0.8f),
                                strokeWidth = mainRoadStroke.width
                            )

                            // Cross ring highway
                            drawCircle(
                                color = SoftTeal.copy(alpha = 0.15f),
                                radius = h * 0.4f,
                                center = Offset(w / 2f, h / 2f),
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Mock city blocks/buildings
                            drawRect(
                                color = VividOrchid.copy(alpha = 0.1f),
                                topLeft = Offset(w * 0.15f, h * 0.45f),
                                size = androidx.compose.ui.geometry.Size(60.dp.toPx(), 40.dp.toPx())
                            )
                            drawRect(
                                color = NeonCyan.copy(alpha = 0.1f),
                                topLeft = Offset(w * 0.65f, h * 0.25f),
                                size = androidx.compose.ui.geometry.Size(50.dp.toPx(), 60.dp.toPx())
                            )

                            // Map the current pinLatitude/pinLongitude to X, Y screen coordinates
                            // pinLongitude range: [106.8122 to 106.8422] mapped to [0 to w]
                            // pinLatitude range: [-6.1901 to -6.1601] mapped to [h to 0]
                            val pinX = w / 2f + ((pinLongitude - 106.8272f) / 0.015f) * (w / 2f)
                            val pinY = h / 2f - ((pinLatitude - (-6.1751f)) / 0.015f) * (h / 2f)

                            // Draw glowing area circle around Pin
                            drawCircle(
                                color = SoftTeal.copy(alpha = 0.25f),
                                radius = 24.dp.toPx(),
                                center = Offset(pinX, pinY)
                            )

                            // Draw pinpoint crosshair
                            drawLine(
                                color = SoftTeal,
                                start = Offset(pinX - 15.dp.toPx(), pinY),
                                end = Offset(pinX + 15.dp.toPx(), pinY),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = SoftTeal,
                                start = Offset(pinX, pinY - 15.dp.toPx()),
                                end = Offset(pinX, pinY + 15.dp.toPx()),
                                strokeWidth = 2.dp.toPx()
                            )

                            // Draw central neon pinpoint indicator marker
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = Offset(pinX, pinY)
                            )
                        }

                        // Floating coordinate card overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "Lat: %.6f, Lng: %.6f", pinLatitude, pinLongitude),
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Latitude & Longitude read-only display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = String.format(Locale.US, "%.6f", pinLatitude),
                            onValueChange = {
                                val value = it.toFloatOrNull()
                                if (value != null) pinLatitude = value
                            },
                            label = { Text("Latitude") },
                            leadingIcon = { Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        OutlinedTextField(
                            value = String.format(Locale.US, "%.6f", pinLongitude),
                            onValueChange = {
                                val value = it.toFloatOrNull()
                                if (value != null) pinLongitude = value
                            },
                            label = { Text("Longitude") },
                            leadingIcon = { Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat Penjemputan Lengkap") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SoftTeal) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_address_input"),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Petunjuk Tambahan Penjemputan") },
                        placeholder = { Text("Contoh: Pagar hitam, dekat pos satpam") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_notes_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // SECTION 3: Save Button
            Button(
                onClick = {
                    if (ownerName.isBlank() || storeName.isBlank() || address.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Silakan lengkapi semua kolom yang wajib diisi.")
                        }
                    } else {
                        viewModel.updateProfile(
                            name = ownerName,
                            email = ownerEmail,
                            storeName = storeName,
                            address = address,
                            latitude = pinLatitude,
                            longitude = pinLongitude,
                            notes = notes
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Profil dan Pinpoint berhasil disimpan! ✨")
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Simpan Perubahan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}
