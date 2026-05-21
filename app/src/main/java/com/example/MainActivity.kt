package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.example.ui.theme.*
import com.example.ui.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EnclaveApp()
            }
        }
    }
}

enum class Screen {
    Chats, Spaces, Channels, Profile, ChatDetail, Subscription
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnclaveApp(viewModel: ChatViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Chats.name
    val isTopLevel = currentRoute in listOf(Screen.Chats.name, Screen.Spaces.name, Screen.Channels.name, Screen.Profile.name)
    var showBottomSheet by remember { mutableStateOf(false) }

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
                    Row(
                        modifier = Modifier.fillMaxWidth().background(SurfaceVariant).padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("END-TO-END ENCRYPTED (TWEETNACL)", color = OnSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                    }
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
                            onClick = { navController.navigate(Screen.Chats.name) { popUpTo(Screen.Chats.name) { inclusive = true } } },
                            icon = { Icon(Icons.Filled.ChatBubble, contentDescription = "Chats") },
                            label = { Text("Chats", fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = OnPrimaryContainer, selectedTextColor = OnSurface, indicatorColor = PrimaryContainer, unselectedIconColor = OnSurfaceVariant, unselectedTextColor = OnSurfaceVariant)
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Spaces.name,
                            onClick = { navController.navigate(Screen.Spaces.name) },
                            icon = { Icon(Icons.Outlined.Folder, contentDescription = "Spaces") },
                            label = { Text("Spaces") },
                            colors = NavigationBarItemDefaults.colors(unselectedIconColor = OnSurfaceVariant, unselectedTextColor = OnSurfaceVariant)
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Channels.name,
                            onClick = { navController.navigate(Screen.Channels.name) },
                            icon = { Icon(Icons.Outlined.Campaign, contentDescription = "Channels") },
                            label = { Text("Channels") },
                            colors = NavigationBarItemDefaults.colors(unselectedIconColor = OnSurfaceVariant, unselectedTextColor = OnSurfaceVariant)
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Profile.name,
                            onClick = { navController.navigate(Screen.Profile.name) },
                            icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            colors = NavigationBarItemDefaults.colors(unselectedIconColor = OnSurfaceVariant, unselectedTextColor = OnSurfaceVariant)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.Chats.name, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Chats.name) {
                com.example.ui.ChatsListScreen(viewModel, showBottomSheet, { showBottomSheet = false }, { navController.navigate(Screen.ChatDetail.name) })
            }
            composable(Screen.Spaces.name) { com.example.ui.PlaceholderScreen("Spaces Area - Collaborate Offline") }
            composable(Screen.Channels.name) { com.example.ui.PlaceholderScreen("Channels - Broadcast Securely") }
            composable(Screen.Profile.name) { com.example.ui.ProfileScreen(onNavigateToSubscription = { navController.navigate(Screen.Subscription.name) }) }
            composable(Screen.ChatDetail.name) {
                com.example.ui.ChatDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Subscription.name) {
                com.example.ui.SubscriptionScreen(onBack = { navController.popBackStack() })
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
