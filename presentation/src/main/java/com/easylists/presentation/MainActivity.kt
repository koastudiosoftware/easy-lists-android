package com.easylists.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.about.AboutScreen
import com.easylists.presentation.ui.listdetails.ListDetailsScreen
import com.easylists.presentation.ui.listoflists.ListOfListsScreen
import com.easylists.presentation.ui.listoflists.ListOfListsViewModel
import com.easylists.presentation.ui.settings.SettingsScreen
import com.easylists.presentation.ui.theme.EasyListsTheme
import com.easylists.presentation.ui.theme.spaces
import dagger.hilt.android.AndroidEntryPoint
import dev.olshevski.navigation.reimagined.NavBackHandler
import dev.olshevski.navigation.reimagined.NavHost
import dev.olshevski.navigation.reimagined.rememberNavController
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by viewModels()
    private val startDestinationViewModel: ListOfListsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EasyListsTheme(dynamicColor = false) {
                val navController = rememberNavController<Screen>(
                    startDestination = Screen.ListOfLists
                )

                NavBackHandler(navController)

                val isBackStackEmpty by remember {
                    derivedStateOf {
                        navController.backstack.entries.size == 1
                    }
                }

                BackHandler(enabled = isBackStackEmpty) {
                    finish()
                }

                val currentDestination by remember {
                    derivedStateOf {
                        navController.backstack.entries.first().destination
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(MaterialTheme.spaces.none),
                    bottomBar = {}
                ) { innerPadding ->

                    Surface(modifier = Modifier.padding(innerPadding)) {
                        NavHost(controller = navController) { route ->
                            when (route) {
                                is Screen.ListOfLists -> {
                                    ListOfListsScreen(
                                        navController = navController,
                                        sharedViewModel = sharedViewModel,
                                        viewModel = startDestinationViewModel
                                    )
                                }

                                is Screen.ListDetails -> {
                                    ListDetailsScreen(
                                        navController = navController,
                                        sharedViewModel = sharedViewModel
                                    )
                                }

                                is Screen.Settings -> {
                                    SettingsScreen(
                                        navController = navController,
                                        sharedViewModel = sharedViewModel
                                    )
                                }

                                is Screen.About -> {
                                    AboutScreen(
                                        navController = navController,
                                        onLinkClick = { _ ->
//                                            this@MainActivity.openUrlInExternalBrowser(url = url)
                                        },
                                        onEmailClick = { _, _ ->
//                                            this@MainActivity.openEmailInExternalApp(
//                                                toEmailAddresses = setOf(email),
//                                                subject = subject
//                                            )
                                        },
                                        onPlayStoreClick = {
//                                            openAppInPlayStore(packageName = packageName)
                                        }
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
