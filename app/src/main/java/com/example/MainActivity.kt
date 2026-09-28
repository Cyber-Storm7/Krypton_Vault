package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.crypto.CryptoManager
import com.example.crypto.DuressVaultManager
import com.example.ui.components.KryptonFloatingNavBar
import com.example.ui.components.KryptonNavTab
import com.example.ui.navigation.Screen
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.ItemDetailEditScreen
import com.example.ui.screens.SecurityAuditScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UnlockScreen
import com.example.ui.screens.VaultListScreen
import com.example.ui.theme.KryptonVaultTheme
import com.example.ui.viewmodel.VaultViewModel
import com.example.util.LifecycleManager
import com.example.util.SecurityPreferencesManager
import kotlinx.coroutines.delay

import androidx.activity.SystemBarStyle

class MainActivity : FragmentActivity() {

    private val viewModel: VaultViewModel by viewModels()
    private var isFlagSecureActive by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // Initialize ProcessLifecycleOwner-based auto-lock
        LifecycleManager.initialize(this)
        LifecycleManager.onAutoLock = {
            viewModel.lockVault()
        }

        // Anti-Screen capture protection: strictly enforce FLAG_SECURE
        applyFlagSecure(true)

        setContent {
            KryptonVaultTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KryptonAppNavigation(
                        viewModel = viewModel,
                        isFlagSecureEnabled = isFlagSecureActive,
                        onToggleFlagSecure = { enabled ->
                            applyFlagSecure(enabled)
                        }
                    )
                }
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        LifecycleManager.recordInteraction()
    }

    private fun applyFlagSecure(enable: Boolean) {
        isFlagSecureActive = enable
        if (enable) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

@Composable
fun KryptonAppNavigation(
    viewModel: VaultViewModel,
    isFlagSecureEnabled: Boolean,
    onToggleFlagSecure: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val uiState by viewModel.uiState.collectAsState()

    // --- ISSUE #5: NEARBY DEVICES RUNTIME PERMISSIONS ON STARTUP ---
    val neededPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ).filter {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }
        } else {
            emptyList()
        }
    }

    var showPermissionRationale by remember { mutableStateOf(false) }
    var hasRequestedPermissions by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val anyDenied = results.values.any { !it }
        if (anyDenied) {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(neededPermissions) {
        if (neededPermissions.isNotEmpty() && !hasRequestedPermissions) {
            hasRequestedPermissions = true
            permissionLauncher.launch(neededPermissions.toTypedArray())
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = {
                Text("Nearby Devices Permission", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Krypton Vault uses offline Bluetooth for local peer-to-peer sync. No internet connection is used or required."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_ADVERTISE
                                )
                            )
                        }
                    }
                ) {
                    Text("Grant Permission", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    // Note: Vault auto-lock applies strictly on app background exit, not while the user is actively using the app.

    // Reactive navigation to Unlock screen if vault gets locked
    LaunchedEffect(uiState.isUnlocked) {
        if (!uiState.isUnlocked && currentRoute != null && currentRoute != Screen.Unlock.route) {
            navController.navigate(Screen.Unlock.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val startDestination = if (CryptoManager.isUnlocked()) {
        Screen.VaultList.route
    } else {
        Screen.Unlock.route
    }

    // Determine current navigation tab for floating bar
    val currentTab = when (currentRoute) {
        Screen.VaultList.route -> KryptonNavTab.VAULT
        Screen.Generator.route -> KryptonNavTab.GENERATOR
        Screen.SecurityAudit.route -> KryptonNavTab.HEALTH
        Screen.Settings.route -> KryptonNavTab.SETTINGS
        else -> null
    }

    var pendingSyncItems by remember { mutableStateOf<List<com.example.model.VaultItemDecrypted>>(emptyList()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fast, fluid transitions for Minimalist theme
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = { fadeIn(tween(100)) },
                exitTransition = { fadeOut(tween(100)) },
                popEnterTransition = { fadeIn(tween(100)) },
                popExitTransition = { fadeOut(tween(100)) }
            ) {
                composable(Screen.Unlock.route) {
                    UnlockScreen(
                        viewModel = viewModel,
                        onUnlocked = {
                            SecurityPreferencesManager.setLastBackgroundTimestamp(context, 0L)
                            navController.navigate(Screen.VaultList.route) {
                                popUpTo(Screen.Unlock.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.VaultList.route) {
                    VaultListScreen(
                        viewModel = viewModel,
                        onNavigateToDetail = { itemId ->
                            navController.navigate(Screen.ItemDetail.createRoute(itemId))
                        },
                        onNavigateToAudit = {
                            navController.navigate(Screen.SecurityAudit.route) {
                                popUpTo(Screen.VaultList.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(Screen.VaultList.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onLockVault = {
                            viewModel.lockVault()
                            navController.navigate(Screen.Unlock.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Generator.route) {
                    GeneratorScreen(
                        onNavigateBack = {
                            navController.navigate(Screen.VaultList.route)
                        }
                    )
                }

                composable(Screen.SecurityAudit.route) {
                    SecurityAuditScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.navigate(Screen.VaultList.route) {
                                popUpTo(Screen.VaultList.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToEdit = { itemId ->
                            navController.navigate(Screen.ItemDetail.createRoute(itemId))
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        isFlagSecureEnabled = isFlagSecureEnabled,
                        onToggleFlagSecure = onToggleFlagSecure,
                        onOpenSelectiveSync = { items ->
                            pendingSyncItems = items
                            navController.navigate(Screen.SelectiveSync.route)
                        },
                        onNavigateBack = {
                            navController.navigate(Screen.VaultList.route) {
                                popUpTo(Screen.VaultList.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onLockVault = {
                            viewModel.lockVault()
                            navController.navigate(Screen.Unlock.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = Screen.SelectiveSync.route,
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(200)) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(200)) },
                    popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(200)) },
                    popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(200)) }
                ) {
                    com.example.ui.screens.SelectiveSyncScreen(
                        remoteItems = pendingSyncItems,
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.ItemDetail.route,
                    arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(200)) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(200)) },
                    popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(200)) },
                    popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(200)) }
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId") ?: "new"
                    ItemDetailEditScreen(
                        itemId = itemId,
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Minimalist Floating Navigation Bar
        if (currentTab != null) {
            KryptonFloatingNavBar(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    val targetRoute = when (tab) {
                        KryptonNavTab.VAULT -> Screen.VaultList.route
                        KryptonNavTab.GENERATOR -> Screen.Generator.route
                        KryptonNavTab.HEALTH -> Screen.SecurityAudit.route
                        KryptonNavTab.SETTINGS -> Screen.Settings.route
                    }
                    if (tab == KryptonNavTab.VAULT) {
                        navController.navigate(Screen.VaultList.route) {
                            popUpTo(Screen.VaultList.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.VaultList.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
