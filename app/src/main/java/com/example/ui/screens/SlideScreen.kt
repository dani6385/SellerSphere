package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    // Current active page state (0 for Slide 1, 1 for Slide 2)
    var activePage by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Slide Navigator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("slide_back_btn")
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Description Header
            Text(
                text = if (activePage == 0) "GESER KE KANAN ➡️" else "KETUK GAMBAR UNTUK MASUK AKUN 👇",
                color = if (activePage == 0) NeonCyan else VividOrchid,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            // Dynamic Slide content container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        var dragAmountAccumulated = 0f
                        detectDragGestures(
                            onDragStart = { dragAmountAccumulated = 0f },
                            onDragEnd = {
                                if (dragAmountAccumulated < -100f && activePage == 0) {
                                    activePage = 1 // Swiped left -> Go next (right page)
                                } else if (dragAmountAccumulated > 100f && activePage == 1) {
                                    activePage = 0 // Swiped right -> Go back (left page)
                                }
                            },
                            onDragCancel = { dragAmountAccumulated = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAmountAccumulated += dragAmount.x
                            }
                        )
                    }
                    .padding(16.dp)
            ) {
                AnimatedContent(
                    targetState = activePage,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "SlideTransition"
                ) { page ->
                    if (page == 0) {
                        // Slide 1
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Langkah 1: Geser ke Kanan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            // Image 1
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(vertical = 16.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_hero_banner),
                                    contentDescription = "Slide 1 Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                            )
                                        )
                                )
                                Text(
                                    text = "SS Seller Sphere Hub",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp)
                                )
                            }

                            // Instruction description
                            Text(
                                text = "Ini adalah slide pertama Anda. Silakan geser atau tekan tombol panah kanan di bawah ini untuk melihat slide berikutnya.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        // Slide 2
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Langkah 2: Ketuk Gambar Profil",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            // Image 2 (Clickable profile image that navigates to Account Screen)
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .weight(1f)
                                    .padding(vertical = 16.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, VividOrchid, CircleShape)
                                    .background(Color.Black)
                                    .clickable { onNavigateToAccount() }
                                    .testTag("slide_2_avatar_image"),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_profile_avatar),
                                    contentDescription = "Avatar Akun",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.2f))
                                )
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "Tap here",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .align(Alignment.Center)
                                )
                            }

                            // Instruction description
                            Text(
                                text = "Kerja bagus! Sekarang KETUK gambar profil di atas untuk membuka Halaman Akun Bisnis Anda secara langsung.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            // Slide Navigation Indicators and Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev Button
                IconButton(
                    onClick = { if (activePage > 0) activePage-- },
                    enabled = activePage > 0,
                    modifier = Modifier.testTag("slide_prev_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Sebelumnya",
                        tint = if (activePage > 0) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }

                // Dot Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (activePage == 0) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (activePage == 0) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            .clickable { activePage = 0 }
                    )
                    Box(
                        modifier = Modifier
                            .size(if (activePage == 1) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (activePage == 1) VividOrchid else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            .clickable { activePage = 1 }
                    )
                }

                // Next Button
                IconButton(
                    onClick = { if (activePage < 1) activePage++ },
                    enabled = activePage < 1,
                    modifier = Modifier.testTag("slide_next_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Berikutnya",
                        tint = if (activePage < 1) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
