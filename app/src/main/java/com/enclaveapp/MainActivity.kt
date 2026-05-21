package com.enclaveapp

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.enclaveapp.ui.theme.*
import com.enclaveapp.ui.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

import android.view.WindowManager
import com.enclaveapp.crypto.LocalKeyStore
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import com.enclaveapp.data.MessagePollWorker

class MainActivity : FragmentActivity(), PaymentResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!LocalKeyStore.hasKeypair(applicationContext)) {
            LocalKeyStore.generateAndSave(applicationContext)
        }
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "message_poll",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<MessagePollWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        )
        
        Checkout.preload(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EnclaveApp(onStartCheckout = { amountInPaise -> startPayment(amountInPaise) })
            }
        }
    }
    
    private fun startPayment(amountInPaise: Int) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_mock_enclave")
        try {
            val options = JSONObject()
            options.put("name", "Enclave Premium")
            options.put("description", "Secure Chat Subscription")
            options.put("currency", "INR")
            options.put("amount", amountInPaise.toString())
            options.put("prefill.email", "android.designer@example.com")
            options.put("prefill.contact", "9876543210")
            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        Toast.makeText(this, "Payment Successful: $razorpayPaymentID", Toast.LENGTH_LONG).show()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment failed: $response", Toast.LENGTH_LONG).show()
    }
}

enum class Screen {
    PinLock, Chats, Spaces, Channels, Profile, ChatDetail, Subscription
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnclaveApp(viewModel: ChatViewModel = viewModel(), onStartCheckout: (Int) -> Unit = {}) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.PinLock.name
    val isTopLevel = currentRoute in listOf(Screen.Chats.name, Screen.Spaces.name, Screen.Channels.name, Screen.Profile.name)
    var showBottomSheet by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                navController.navigate(Screen.PinLock.name) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Surface,
        topBar = {
            if (isTopLevel) {
                Column {
                    TopAppBar(
                        title = { Text("Enclave", color = Primary, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp) },
                        actions = {
                            IconButton(onClick = {}) { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = OnSurfaceVariant) }
                            IconButton(onClick = {}) { Icon(Icons.Outlined.MoreVert, contentDescription = "More", tint = OnSurfaceVariant) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
                    )
                    HorizontalDivider(color = Outline)
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Screen.Chats.name) {
                FloatingActionButton(
                    onClick = { showBottomSheet = true },
                    containerColor = PrimaryContainer,
                    contentColor = OnPrimaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "New Chat")
                }
            }
        },
        bottomBar = {
            if (isTopLevel) {
                Column {
                    HorizontalDivider(color = Outline)
                    NavigationBar(containerColor = Color(0xFFF3F4F9), tonalElevation = 0.dp) {
                        NavigationBarItem(
                            selected = currentRoute == Screen.Chats.name,
                            onClick = { 
                                navController.navigate(Screen.Chats.name) { 
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                } 
                            },
                            icon = { Icon(Icons.Filled.ChatBubble, contentDescription = "Chats") },
                            label = { Text("Chats", fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = OnPrimaryContainer, selectedTextColor = OnSurface, indicatorColor = PrimaryContainer, unselectedIconColor = OnSurfaceVariant, unselectedTextColor = OnSurfaceVariant)
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Spaces.name,
                            onClick = { 
                                navController.navigate(Screen.Spaces.name) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Outlined.Folder, contentDescription = "Spaces") },
                            label = { Text("Spaces") },
                            colors = NavigationBarItemDefaults.colors(unselectedIconColor = OnSurfaceVariant, unselectedTextColor = OnSurfaceVariant)
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Channels.name,
                            onClick = { 
                                navController.navigate(Screen.Channels.name) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Outlined.Campaign, contentDescription = "Channels") },
                            label = { Text("Channels") },
                            colors = NavigationBarItemDefaults.colors(unselectedIconColor = OnSurfaceVariant, unselectedTextColor = OnSurfaceVariant)
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Profile.name,
                            onClick = { 
                                navController.navigate(Screen.Profile.name) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            colors = NavigationBarItemDefaults.colors(unselectedIconColor = OnSurfaceVariant, unselectedTextColor = OnSurfaceVariant)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.PinLock.name, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.PinLock.name) {
                com.enclaveapp.ui.PinLockScreen(onUnlocked = {
                    navController.navigate(Screen.Chats.name) { popUpTo(Screen.PinLock.name) { inclusive = true } }
                })
            }
            composable(Screen.Chats.name) {
                com.enclaveapp.ui.ChatsListScreen(viewModel, showBottomSheet, { showBottomSheet = false }, { convId -> navController.navigate("${Screen.ChatDetail.name}/$convId") })
            }
            composable(Screen.Spaces.name) { com.enclaveapp.ui.PlaceholderScreen("Spaces Area - Collaborate Offline") }
            composable(Screen.Channels.name) { com.enclaveapp.ui.PlaceholderScreen("Channels - Broadcast Securely") }
            composable(Screen.Profile.name) { com.enclaveapp.ui.ProfileScreen(onNavigateToSubscription = { navController.navigate(Screen.Subscription.name) }) }
            composable("${Screen.ChatDetail.name}/{conversationId}") { backStackEntry ->
                val convId = backStackEntry.arguments?.getString("conversationId") ?: ""
                com.enclaveapp.ui.ChatDetailScreen(conversationId = convId, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Subscription.name) {
                com.enclaveapp.ui.SubscriptionScreen(onBack = { navController.popBackStack() }, onCheckout = onStartCheckout)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name! Preserved existing component.", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
