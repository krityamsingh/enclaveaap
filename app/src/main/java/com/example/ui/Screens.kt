package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(viewModel: ChatViewModel, showBottomSheet: Boolean, onDismissBottomSheet: () -> Unit, onNavigateToChat: () -> Unit) {
    val offlineQueue by viewModel.offlineQueue.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    var recipientText by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(offlineQueue, key = { it.id }) { message ->
            Box(modifier = Modifier.background(Surface)) {
                ChatItem(
                    icon = {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Outline), contentAlignment = Alignment.Center) {
                            Text(message.recipient.take(2).uppercase(), color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    },
                    title = message.recipient,
                    titleIcon = { Icon(Icons.Filled.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp).padding(start = 4.dp)) },
                    titleBadge = "QUEUED",
                    time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                    timeColor = TextSecondary,
                    message = message.messageText,
                    messageColor = OnSurfaceVariant,
                    trailingAction = {
                        IconButton(onClick = { viewModel.deleteMessage(message) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedBadge)
                        }
                    },
                    onClick = onNavigateToChat
                )
            }
            HorizontalDivider(color = Outline, modifier = Modifier.padding(start = 88.dp))
        }

        item {
            ChatItem(
                icon = {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Primary), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null, tint = Color.White)
                    }
                },
                title = "Saved Messages", time = "8:42 AM", message = "Secret Key: enclave_x25519_...", messageColor = TextSecondary, onClick = onNavigateToChat
            )
            HorizontalDivider(color = Outline, modifier = Modifier.padding(start = 88.dp))
        }
        item {
            ChatItem(
                icon = {
                    Box {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Outline), contentAlignment = Alignment.Center) {
                            Text("VS", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Box(modifier = Modifier.size(16.dp).align(Alignment.BottomEnd).clip(CircleShape).background(GreenOnline).border(2.dp, Surface, CircleShape))
                    }
                },
                title = "Vikram Sharma",
                titleIcon = { Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp).padding(start = 4.dp)) },
                time = "10m", timeColor = Primary, message = "Client-side decryption verified.",
                messageIcon = { Icon(Icons.Filled.DoneAll, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp).padding(end = 4.dp)) },
                messageColor = OnSurfaceVariant, onClick = onNavigateToChat
            )
            HorizontalDivider(color = Outline, modifier = Modifier.padding(start = 88.dp))
        }
        item {
            ChatItem(
                icon = {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(SurfaceVariant), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Group, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(32.dp))
                    }
                },
                title = "Dev Ops Production", time = "Yesterday", messagePrefix = "Arjun: ", message = "Razorpay HMAC signature valid.",
                messageColor = TextSecondary, badgeCount = "12", onClick = onNavigateToChat
            )
            HorizontalDivider(color = Outline, modifier = Modifier.padding(start = 88.dp))
        }
        item {
            ChatItem(
                icon = {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFFFF9C4)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Stars, contentDescription = null, tint = Color(0xFFF9A825), modifier = Modifier.size(32.dp))
                    }
                },
                title = "Premium Billing", titleBadge = "VIP", time = "Monday", message = "Subscription ₹1,999/year active.",
                messageColor = TextSecondary, onClick = onNavigateToChat
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = onDismissBottomSheet, sheetState = sheetState) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                Text("Queue Offline Message", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = recipientText, onValueChange = { recipientText = it }, label = { Text("Recipient") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = messageText, onValueChange = { messageText = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (recipientText.isNotBlank() && messageText.isNotBlank()) {
                        viewModel.queueMessage(recipientText, messageText)
                        recipientText = ""
                        messageText = ""
                        onDismissBottomSheet()
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Save to Queue")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().background(Surface)) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Outline), contentAlignment = Alignment.Center) {
                        Text("VS", color = Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Vikram Sharma", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Online", fontSize = 12.sp, color = GreenOnline, fontWeight = FontWeight.Medium)
                    }
                }
            },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
            actions = {
                IconButton(onClick = {}) { Icon(Icons.Outlined.Call, contentDescription = "Call") }
                IconButton(onClick = {}) { Icon(Icons.Outlined.MoreVert, contentDescription = "More") }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
        )
        HorizontalDivider(color = Outline)
        
        // Chat Area
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp), reverseLayout = true) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    Box(modifier = Modifier.background(PrimaryContainer, RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)).padding(12.dp)) {
                        Text("Client-side decryption verified.", color = OnPrimaryContainer, fontSize = 15.sp)
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Start) {
                    Box(modifier = Modifier.background(SurfaceVariant, RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)).padding(12.dp)) {
                        Text("Did you test the new TweetNaCl wrapper?", color = OnSurfaceVariant, fontSize = 15.sp)
                    }
                }
            }
        }

        // Input Area
        Row(modifier = Modifier.fillMaxWidth().background(Surface).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { Icon(Icons.Outlined.Add, contentDescription = "Add") }
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Message...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Outline,
                    unfocusedBorderColor = Outline,
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant
                )
            )
            IconButton(onClick = { messageText = "" }, enabled = messageText.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = if (messageText.isNotBlank()) Primary else TextSecondary)
            }
        }
    }
}

@Composable
fun ProfileScreen(onNavigateToSubscription: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().background(Surface), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(PrimaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = OnPrimaryContainer)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Android Designer", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        Text("+91 98765 43210", fontSize = 16.sp, color = TextSecondary)
        
        Spacer(modifier = Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceVariant)) {
            Column {
                ProfileOption(Icons.Outlined.Security, "Privacy & Security")
                HorizontalDivider(color = Outline)
                ProfileOption(Icons.Outlined.Settings, "App Settings")
                HorizontalDivider(color = Outline)
                ProfileOption(Icons.Outlined.Notifications, "Notifications")
                HorizontalDivider(color = Outline)
                ProfileOption(Icons.Outlined.ShoppingCart, "Premium Subscription", badge = "VIP", onClick = onNavigateToSubscription)
            }
        }
    }
}

@Composable
fun ProfileOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, badge: String? = null, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = OnSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = OnSurface, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (badge != null) {
            Box(modifier = Modifier.background(Color(0xFFF9A825), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text(text = badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize().background(Surface), contentAlignment = Alignment.Center) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(onBack: () -> Unit) {
    var selectedPlan by remember { mutableStateOf(1) } // 0 = monthly, 1 = yearly
    Column(modifier = Modifier.fillMaxSize().background(Surface)) {
        TopAppBar(
            title = { Text("Enclave Premium", color = OnSurface, fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
        )
        HorizontalDivider(color = Outline)
        
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFFFF9C4)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Stars, contentDescription = null, tint = Color(0xFFF9A825), modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Upgrade to Premium", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Unlock end-to-end encrypted voice calls, priority support, and infinite message history.", fontSize = 14.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(32.dp))
            
            // Monthly Plan
            Row(modifier = Modifier.fillMaxWidth().border(2.dp, if (selectedPlan == 0) Primary else Outline, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).clickable { selectedPlan = 0 }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedPlan == 0, onClick = { selectedPlan = 0 })
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Monthly Plan", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("Billed every month", fontSize = 12.sp, color = TextSecondary)
                }
                Text("₹230", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Yearly Plan
            Row(modifier = Modifier.fillMaxWidth().background(if (selectedPlan == 1) PrimaryContainer else Color.Transparent, RoundedCornerShape(12.dp)).border(2.dp, if (selectedPlan == 1) Primary else Outline, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).clickable { selectedPlan = 1 }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedPlan == 1, onClick = { selectedPlan = 1 })
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Yearly Plan", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.background(Primary, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("SAVE 20%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("Billed annually", fontSize = 12.sp, color = TextSecondary)
                }
                Text("₹1,999", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
            }
        }
        
        // Checkout Section
        Surface(shadowElevation = 8.dp, color = Surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.Security, contentDescription = "Secure", tint = GreenOnline, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secured by Razorpay", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(25.dp)) {
                    Text("Pay via UPI / Cards", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ChatItem(
    icon: @Composable () -> Unit, title: String, titleIcon: (@Composable () -> Unit)? = null, titleBadge: String? = null,
    time: String, timeColor: Color = TextSecondary, messagePrefix: String? = null, messageIcon: (@Composable () -> Unit)? = null,
    message: String, messageColor: Color, badgeCount: String? = null, trailingAction: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    titleIcon?.invoke()
                    if (titleBadge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.background(Color(0xFFF9A825), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                            Text(text = titleBadge, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Text(text = time, color = timeColor, fontSize = 12.sp, fontWeight = if (timeColor == Primary) FontWeight.SemiBold else FontWeight.Normal)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    messageIcon?.invoke()
                    if (messagePrefix != null) { Text(text = messagePrefix, color = Primary, fontSize = 14.sp) }
                    Text(text = message, color = messageColor, fontSize = 14.sp, fontWeight = if (messageColor == OnSurfaceVariant) FontWeight.Medium else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (badgeCount != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.defaultMinSize(minWidth = 20.dp, minHeight = 20.dp).background(RedBadge, CircleShape).padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                            Text(text = badgeCount, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (trailingAction != null) { Spacer(modifier = Modifier.width(8.dp)); trailingAction() }
                }
            }
        }
    }
}
