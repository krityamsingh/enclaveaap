package com.enclaveapp.ui

import androidx.compose.foundation.background
import com.enclaveapp.crypto.CryptoManager
import com.enclaveapp.crypto.LocalKeyStore
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enclaveapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.Backspace

import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.decode.GifDecoder
import android.os.Build
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

import android.view.WindowManager

@Composable
fun AntiScreenshot() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? FragmentActivity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

@Composable
fun PinLockScreen(onUnlocked: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isPinSet = remember { com.enclaveapp.crypto.PinManager.isPinSet(context) }
    var confirmPin by remember { mutableStateOf<String?>(null) }
    
    val biometricLockManager = remember { com.enclaveapp.crypto.BiometricLockManager(context) }

    val shake by animateFloatAsState(targetValue = if (isError) 10f else 0f, label = "shake")

    // Biometric Check
    LaunchedEffect(Unit) {
        if (isPinSet && context is FragmentActivity && biometricLockManager.isBiometricAvailable()) {
            biometricLockManager.authenticate(
                activity = context,
                onSuccess = { onUnlocked() },
                onFallbackToPIN = { /* fallback directly done by prompt closing */ },
                onError = { /* optional error handling */ }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(PrimaryContainer), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = OnPrimaryContainer, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (!isPinSet) {
                if (confirmPin == null) "Set New Passcode" else "Confirm Passcode"
            } else "Enter Passcode", 
            fontSize = 20.sp, color = OnPrimaryContainer, fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        // PIN Dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.offset(x = shake.dp)) {
            for (i in 0 until 4) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (i < pin.length) OnPrimaryContainer else OnPrimaryContainer.copy(alpha = 0.3f))
                )
            }
        }
        Spacer(modifier = Modifier.height(64.dp))
        
        // Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "\u232B")
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            for (row in keys) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    for (key in row) {
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(72.dp))
                        } else if (key == "\u232B") {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = pin.isNotEmpty()) {
                                        pin = pin.dropLast(1)
                                        isError = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", tint = OnPrimaryContainer)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(OnPrimaryContainer.copy(alpha = 0.1f))
                                    .clickable {
                                        if (pin.length < 4) {
                                            pin += key
                                            if (pin.length == 4) {
                                                if (!isPinSet) {
                                                    if (confirmPin == null) {
                                                        confirmPin = pin
                                                        pin = ""
                                                    } else {
                                                        if (confirmPin == pin) {
                                                            com.enclaveapp.crypto.PinManager.setPin(context, pin)
                                                            onUnlocked()
                                                        } else {
                                                            isError = true
                                                            scope.launch {
                                                                delay(400)
                                                                pin = ""
                                                                confirmPin = null
                                                                isError = false
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    if (com.enclaveapp.crypto.PinManager.verifyPin(context, pin)) {
                                                        scope.launch {
                                                            delay(300)
                                                            onUnlocked()
                                                        }
                                                    } else {
                                                        isError = true
                                                        scope.launch {
                                                            delay(400)
                                                            pin = ""
                                                            isError = false
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(key, fontSize = 28.sp, color = OnPrimaryContainer, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(viewModel: ChatViewModel, showBottomSheet: Boolean, onDismissBottomSheet: () -> Unit, onNavigateToChat: (String) -> Unit) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val offlineQueue by viewModel.offlineQueue.collectAsStateWithLifecycle()
    
    val sheetState = rememberModalBottomSheetState()
    var recipientText by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var conversationToShare by remember { mutableStateOf<com.enclaveapp.data.ConversationEntity?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(conversations, key = { it.id }) { conversation ->
            val unreadCount = offlineQueue.count { it.conversationId == conversation.id && !it.isSentByMe && it.status != com.enclaveapp.data.MessageStatus.READ }
            ChatItem(
                icon = {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Outline), contentAlignment = Alignment.Center) {
                        Text(conversation.name.take(2).uppercase(), color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                title = conversation.name,
                time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(conversation.timestamp)),
                timeColor = TextSecondary,
                message = conversation.lastMessage,
                messageColor = OnSurfaceVariant,
                badgeCount = if (unreadCount > 0) unreadCount.toString() else null,
                isMuted = conversation.isMuted,
                titleIcon = if (conversation.isMuted) ({ Icon(Icons.Outlined.NotificationsOff, contentDescription = "Muted", modifier = Modifier.size(16.dp).padding(start = 4.dp), tint = TextSecondary) }) else null,
                onBadgeClick = { viewModel.markConversationAsRead(conversation.id) },
                onBadgeLongClick = { viewModel.markConversationAsRead(conversation.id) },
                onSwipeDelete = { viewModel.deleteConversation(conversation.id) },
                onSwipeMute = { viewModel.toggleMuteConversation(conversation.id) },
                onArchive = { viewModel.archiveConversation(conversation.id) },
                onMute = { viewModel.toggleMuteConversation(conversation.id) },
                trailingAction = {
                    IconButton(onClick = { conversationToShare = conversation }) {
                        Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share", tint = TextSecondary)
                    }
                },
                onClick = { onNavigateToChat(conversation.id) }
            )
            HorizontalDivider(color = Outline, modifier = Modifier.padding(start = 88.dp))
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = onDismissBottomSheet, sheetState = sheetState) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                Text("Start Secure Chat", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = recipientText, onValueChange = { recipientText = it }, label = { Text("Recipient Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = messageText, onValueChange = { messageText = it }, label = { Text("Initial Message") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (recipientText.isNotBlank() && messageText.isNotBlank()) {
                        val convId = java.util.UUID.randomUUID().toString()
                        viewModel.sendMessage(convId, messageText, recipientText)
                        recipientText = ""
                        messageText = ""
                        onDismissBottomSheet()
                        onNavigateToChat(convId)
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Send & Start")
                }
            }
        }
    }

    if (conversationToShare != null) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { conversationToShare = null },
            title = { Text("Share Conversation") },
            text = { Text("Do you want to share the details of the conversation with ${conversationToShare?.name}?\n\nLink: https://enclave.app/chat/${conversationToShare?.id}") },
            confirmButton = {
                TextButton(onClick = { 
                    val sendIntent: android.content.Intent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, "Join my secure chat on Enclave: https://enclave.app/chat/${conversationToShare?.id}")
                        type = "text/plain"
                    }
                    val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                    conversationToShare = null 
                }) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToShare = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HighlightedText(text: String, query: String, color: Color, modifier: Modifier = Modifier, fontSize: androidx.compose.ui.unit.TextUnit = 15.sp) {
    if (query.isBlank()) {
        Text(text, color = color, fontSize = fontSize, modifier = modifier)
        return
    }
    val startMatch = text.indexOf(query, ignoreCase = true)
    if (startMatch == -1) {
        Text(text, color = color, fontSize = fontSize, modifier = modifier)
        return
    }
    val endMatch = startMatch + query.length
    val annotatedString = buildAnnotatedString {
        append(text.substring(0, startMatch))
        withStyle(style = SpanStyle(background = Color.Yellow.copy(alpha = 0.4f))) {
            append(text.substring(startMatch, endMatch))
        }
        append(text.substring(endMatch))
    }
    Text(annotatedString, color = color, fontSize = fontSize, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(conversationId: String, viewModel: ChatViewModel, onBack: () -> Unit) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val conversation = conversations.find { it.id == conversationId }
    val messages by viewModel.getMessages(conversationId).collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var previousMessages by remember { mutableStateOf<List<com.enclaveapp.data.MessageEntity>?>(null) }
    
    val gifResults by viewModel.gifResults.collectAsStateWithLifecycle()
    val isSearchGifLoading by viewModel.isSearchGifLoading.collectAsStateWithLifecycle()
    var isGifMode by remember { mutableStateOf(false) }
    var gifSearchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val gifImageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    LaunchedEffect(messages) {
        if (previousMessages != null) {
            val statusUpdated = messages.any { currentMsg ->
                if (currentMsg.isSentByMe) {
                    val prevMsg = previousMessages!!.find { it.id == currentMsg.id }
                    prevMsg != null && currentMsg.status != prevMsg.status && 
                    (currentMsg.status == com.enclaveapp.data.MessageStatus.DELIVERED || currentMsg.status == com.enclaveapp.data.MessageStatus.READ)
                } else {
                    false
                }
            }
            if (statusUpdated) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            }
        }
        previousMessages = messages
    }

    LaunchedEffect(conversationId) {
        viewModel.markConversationAsRead(conversationId)
    }

    Column(modifier = Modifier.fillMaxSize().background(Surface)) {
        TopAppBar(
            title = {
                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search messages...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = SurfaceVariant,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Outline), contentAlignment = Alignment.Center) {
                            Text(conversation?.name?.take(2)?.uppercase() ?: "??", color = Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(conversation?.name ?: "Unknown", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Text("Online", fontSize = 12.sp, color = GreenOnline, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            navigationIcon = { 
                IconButton(onClick = {
                    if (isSearchActive) {
                        isSearchActive = false
                        viewModel.setSearchQuery("")
                    } else {
                        onBack()
                    }
                }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } 
            },
            actions = {
                if (!isSearchActive) {
                    IconButton(onClick = { isSearchActive = true }) { Icon(Icons.Outlined.Search, contentDescription = "Search") }
                }
                IconButton(onClick = {}) { Icon(Icons.Outlined.Call, contentDescription = "Call") }
                
                var showTimerMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showTimerMenu = true }) { 
                        Icon(Icons.Outlined.Timer, contentDescription = "Disappearing Messages", tint = if (conversation?.disappearAfterMs != null) Primary else TextSecondary) 
                    }
                    DropdownMenu(
                        expanded = showTimerMenu,
                        onDismissRequest = { showTimerMenu = false }
                    ) {
                        val options = listOf(
                            "Off" to null,
                            "30 seconds" to 30_000L,
                            "5 minutes" to 300_000L,
                            "1 hour" to 3_600_000L,
                            "1 day" to 86_400_000L
                        )
                        options.forEach { (label, duration) ->
                            DropdownMenuItem(
                                text = { Text(label, color = if (conversation?.disappearAfterMs == duration) Primary else OnSurface) },
                                onClick = { 
                                    viewModel.updateConversationTimer(conversationId, duration)
                                    showTimerMenu = false 
                                }
                            )
                        }
                    }
                }
                
                IconButton(onClick = {}) { Icon(Icons.Outlined.MoreVert, contentDescription = "More") }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
        )
        HorizontalDivider(color = Outline)
        
        // Chat Area
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp), reverseLayout = false) {
            items(messages, key = { it.id }) { message ->
                val timeString = remember(message.timestamp) {
                    java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start) {
                    Column(horizontalAlignment = if (message.isSentByMe) Alignment.End else Alignment.Start) {
                        Box(modifier = Modifier.background(if (message.isSentByMe) PrimaryContainer else SurfaceVariant, RoundedCornerShape(16.dp, 16.dp, if (message.isSentByMe) 4.dp else 16.dp, if (message.isSentByMe) 16.dp else 4.dp)).padding(12.dp)) {
                            if (message.plaintext.startsWith("[GIF] ")) {
                                AsyncImage(
                                    model = message.plaintext.removePrefix("[GIF] ").trim(),
                                    contentDescription = "Animated GIF",
                                    imageLoader = gifImageLoader,
                                    modifier = Modifier.height(150.dp).clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                HighlightedText(message.plaintext, searchQuery, color = if (message.isSentByMe) OnPrimaryContainer else OnSurfaceVariant, fontSize = 15.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp, end = if (message.isSentByMe) 4.dp else 0.dp, start = if (message.isSentByMe) 0.dp else 4.dp)) {
                            Text(timeString, fontSize = 10.sp, color = TextSecondary)
                            if (message.isSentByMe) {
                                Spacer(modifier = Modifier.width(4.dp))
                                AnimatedContent(
                                    targetState = message.status,
                                    transitionSpec = {
                                        if (targetState == com.enclaveapp.data.MessageStatus.READ && initialState == com.enclaveapp.data.MessageStatus.DELIVERED) {
                                            (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.5f, animationSpec = tween(300))) togetherWith
                                            (fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.5f, animationSpec = tween(300)))
                                        } else {
                                            fadeIn(animationSpec = tween(0)) togetherWith fadeOut(animationSpec = tween(0))
                                        }
                                    },
                                    label = "statusAnimation"
                                ) { status ->
                                    when (status) {
                                        com.enclaveapp.data.MessageStatus.QUEUED,
                                        com.enclaveapp.data.MessageStatus.SENDING -> Icon(Icons.Outlined.Schedule, contentDescription = "Sending", modifier = Modifier.size(12.dp), tint = TextSecondary)
                                        com.enclaveapp.data.MessageStatus.DELIVERED -> Icon(Icons.Outlined.Check, contentDescription = "Delivered", modifier = Modifier.size(12.dp), tint = TextSecondary)
                                        com.enclaveapp.data.MessageStatus.READ -> Icon(Icons.Outlined.DoneAll, contentDescription = "Read", modifier = Modifier.size(14.dp), tint = Verified)
                                        com.enclaveapp.data.MessageStatus.FAILED -> Icon(Icons.Outlined.ErrorOutline, contentDescription = "Failed", modifier = Modifier.size(12.dp), tint = RedBadge)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Input Area
        if (isGifMode) {
            Column(modifier = Modifier.fillMaxWidth().background(Surface).padding(8.dp)) {
                if (isSearchGifLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                androidx.compose.foundation.lazy.LazyRow(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    items(gifResults, key = { it.id }) { gif ->
                        AsyncImage(
                            model = gif.images.fixed_height.url,
                            contentDescription = "GIF",
                            imageLoader = gifImageLoader,
                            modifier = Modifier
                                .height(100.dp)
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    viewModel.sendMessage(conversationId, "[GIF] ${gif.images.fixed_height.url}", conversation?.name ?: "Unknown")
                                    isGifMode = false
                                    gifSearchQuery = ""
                                    viewModel.clearGifs()
                                }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { 
                        isGifMode = false 
                        gifSearchQuery = ""
                        viewModel.clearGifs()
                    }) { Icon(Icons.Outlined.Close, contentDescription = "Close GIF Search") }
                    OutlinedTextField(
                        value = gifSearchQuery,
                        onValueChange = { 
                            gifSearchQuery = it
                            viewModel.searchGifs(it)
                        },
                        placeholder = { Text("Search GIFs...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Outline,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = SurfaceVariant
                        )
                    )
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth().background(Surface).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isGifMode = true }) { Icon(Icons.Outlined.GifBox, contentDescription = "Send GIF") }
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
                IconButton(onClick = {
                    if (messageText.isNotBlank()) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.sendMessage(conversationId, messageText, conversation?.name ?: "Unknown")
                        messageText = ""
                    }
                }, enabled = messageText.isNotBlank()) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = if (messageText.isNotBlank()) Primary else TextSecondary)
                }
            }
        }
    }
}

@Composable
fun KeyVerificationScreen(contact: com.enclaveapp.data.ContactEntity, viewModel: ChatViewModel, onVerified: () -> Unit) {
    val context = LocalContext.current
    AntiScreenshot()
    val myFingerprint = remember { CryptoManager.fingerprint(LocalKeyStore.getPublicKey(context)) }
    val theirFingerprint = remember { 
        CryptoManager.fingerprint(
            android.util.Base64.decode(contact.publicKeyBase64, android.util.Base64.NO_WRAP)
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().background(Surface).padding(16.dp)) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Verify ${contact.displayName}'s identity", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Compare these codes in person or on a call", color = TextSecondary)
        Spacer(modifier = Modifier.height(32.dp))

        // Your fingerprint
        Text("Your safety code", fontWeight = FontWeight.SemiBold)
        Text(myFingerprint, color = Primary, fontSize = 18.sp, modifier = Modifier.padding(16.dp))

        Spacer(modifier = Modifier.height(24.dp))
        // Their fingerprint
        Text("${contact.displayName}'s safety code", fontWeight = FontWeight.SemiBold)
        Text(theirFingerprint, color = Primary, fontSize = 18.sp, modifier = Modifier.padding(16.dp))

        Spacer(modifier = Modifier.height(32.dp))
        Text("If both codes match on both phones, your conversation is secure.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = {
            viewModel.markContactVerified(contact.userId)
            onVerified()
        }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("Codes match — mark as verified")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileScreen(onNavigateToSubscription: () -> Unit = {}) {
    val context = LocalContext.current
    AntiScreenshot()
    var showSecurityDialog by remember { mutableStateOf(false) }

    if (showSecurityDialog) {
        val fingerprint = remember { CryptoManager.fingerprint(LocalKeyStore.getPublicKey(context)) }
        val qrBitmap = remember(fingerprint) {
            try {
                com.journeyapps.barcodescanner.BarcodeEncoder().encodeBitmap(fingerprint, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400)?.asImageBitmap()
            } catch(e: Exception) {
                null
            }
        }
        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = GreenOnline)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Security verification")
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your TweetNaCl Public Key Fingerprint:", fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "QR Code for Public Key Fingerprint",
                            modifier = Modifier.size(200.dp).padding(8.dp)
                        )
                    }

                    Box(modifier = Modifier.background(SurfaceVariant, RoundedCornerShape(8.dp)).padding(16.dp)) {
                        Text(fingerprint, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, letterSpacing = 2.sp, fontSize = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Only share this via a secure channel to verify your identity.", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = { showSecurityDialog = false }) {
                    Text("Dismiss", color = Primary)
                }
            },
            containerColor = Surface
        )
    }

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
                ProfileOption(Icons.Outlined.Security, "Privacy & Security", onClick = { showSecurityDialog = true })
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Disabled in True P2P Mode", fontSize = 14.sp, color = TextSecondary)
            Text("We removed centralized groups to ensure", fontSize = 12.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text("every message is strictly 1-to-1 encrypted.", fontSize = 12.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(onBack: () -> Unit, onCheckout: (Int) -> Unit = {}) {
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
                Button(onClick = { onCheckout(if (selectedPlan == 0) 23000 else 199900) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(25.dp)) {
                    Text("Pay via UPI / Cards", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatItem(
    icon: @Composable () -> Unit, title: String, titleIcon: (@Composable () -> Unit)? = null, titleBadge: String? = null,
    time: String, timeColor: Color = TextSecondary, messagePrefix: String? = null, messageIcon: (@Composable () -> Unit)? = null,
    message: String, messageColor: Color, badgeCount: String? = null, trailingAction: (@Composable () -> Unit)? = null,
    isMuted: Boolean = false,
    onBadgeClick: (() -> Unit)? = null,
    onBadgeLongClick: (() -> Unit)? = null,
    onSwipeDelete: (() -> Unit)? = null,
    onArchive: (() -> Unit)? = null,
    onSwipeMute: (() -> Unit)? = null,
    onMute: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart && onSwipeDelete != null) {
                onSwipeDelete()
                return@rememberSwipeToDismissBoxState true
            }
            if (it == SwipeToDismissBoxValue.StartToEnd && onSwipeMute != null) {
                onSwipeMute()
                return@rememberSwipeToDismissBoxState true
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = onSwipeMute != null,
        enableDismissFromEndToStart = onSwipeDelete != null,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFE53935)
                    SwipeToDismissBoxValue.StartToEnd -> if (isMuted) Color(0xFF42A5F5) else Color(0xFFFFA726)
                    else -> Color.Transparent
                },
                label = "color"
            )
            val alignment = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.White)
                } else if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(if (isMuted) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff, contentDescription = "Toggle Mute", tint = Color.White)
                }
            }
        },
        content = {
            Row(modifier = Modifier.fillMaxWidth().background(Surface).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = title, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            titleIcon?.invoke()
                            if (titleBadge != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.shadow(elevation = 2.dp, shape = RoundedCornerShape(4.dp)).background(Color(0xFFF9A825), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
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
                            val interactionSource = remember { MutableInteractionSource() }
                            val isHovered by interactionSource.collectIsHoveredAsState()
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val hoverScale by animateFloatAsState(
                                targetValue = if (isHovered && !isPressed) 1.2f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "hoverScale"
                            )
                            val pressScale by animateFloatAsState(
                                targetValue = if (isPressed) 0.85f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "pressScale"
                            )
                            val badgeScale by animateFloatAsState(
                                targetValue = if (badgeCount != null) 1f else 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "badgeScale"
                            )
                            var lastBadgeCount by remember { mutableStateOf(badgeCount ?: "") }
                            val popScale = remember { Animatable(1f) }
                            val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = if (badgeCount != null) 1.15f else 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = EaseInOut),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseScale"
                            )
                            LaunchedEffect(badgeCount) {
                                if (badgeCount != null) {
                                    if (lastBadgeCount.isNotEmpty() && lastBadgeCount != badgeCount) {
                                        popScale.snapTo(1.4f)
                                        popScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                                    }
                                    lastBadgeCount = badgeCount
                                }
                            }
                            
                            if (badgeScale > 0f) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box {
                                        var showBadgeMenu by remember { mutableStateOf(false) }
                                        var isExpanded by remember { mutableStateOf(false) }
                                        val isBadgeExpanded = isHovered || isPressed || showBadgeMenu || isExpanded
                                        val badgeMaxWidth by animateDpAsState(
                                            targetValue = if (isBadgeExpanded) 100.dp else 24.dp,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                            label = "badgeMaxWidth"
                                        )
                                        Box(modifier = Modifier.scale(badgeScale * hoverScale * pressScale * popScale.value * pulseScale).widthIn(max = badgeMaxWidth).combinedClickable(interactionSource = interactionSource, indication = ripple(), onClick = { isExpanded = !isExpanded; onBadgeClick?.invoke() }, onLongClick = { onBadgeLongClick?.invoke(); showBadgeMenu = true }).shadow(elevation = 2.dp, shape = CircleShape).defaultMinSize(minWidth = 20.dp, minHeight = 20.dp).background(RedBadge, CircleShape).padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                            Text(text = lastBadgeCount, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        DropdownMenu(expanded = showBadgeMenu, onDismissRequest = { showBadgeMenu = false; isExpanded = false }) {
                                            DropdownMenuItem(text = { Text("Delete conversation") }, onClick = { showBadgeMenu = false; isExpanded = false; onSwipeDelete?.invoke() })
                                            DropdownMenuItem(text = { Text("Archive") }, onClick = { showBadgeMenu = false; isExpanded = false; onArchive?.invoke() })
                                            DropdownMenuItem(text = { Text("Mute") }, onClick = { showBadgeMenu = false; isExpanded = false; onMute?.invoke() })
                                        }
                                    }
                                }
                            }
                            if (trailingAction != null) { Spacer(modifier = Modifier.width(8.dp)); trailingAction() }
                        }
                    }
                }
            }
        }
    )
}
