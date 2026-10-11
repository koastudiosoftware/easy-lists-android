package com.easylists.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylists.domain.common.Capitalization
import com.easylists.presentation.common.LocalCapitalization
import com.easylists.presentation.common.SharedViewModel
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.about.AboutScreen
import com.easylists.presentation.ui.categories.CategoriesScreen
import com.easylists.presentation.ui.tags.TagsScreen
import com.easylists.presentation.ui.listitems.ListItemsScreen
import com.easylists.presentation.ui.lists.ListsViewModel
import com.easylists.presentation.ui.lists.ListsScreen
import com.easylists.presentation.ui.settings.SettingsScreen
import com.easylists.presentation.ui.theme.EasyListsTheme
import com.easylists.presentation.ui.theme.spaces
//import com.easylists.presentation.ui.theme.EasyListsTheme
import dagger.hilt.android.AndroidEntryPoint
import dev.olshevski.navigation.reimagined.NavBackHandler
import dev.olshevski.navigation.reimagined.NavHost
import dev.olshevski.navigation.reimagined.rememberNavController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private val startDestinationViewModel: ListsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalCapitalization provides (settings?.capitalization ?: Capitalization.NoCapitalization)
            ) {
                EasyListsTheme() {
                    val navController = rememberNavController<Screen>(
                        startDestination = Screen.MasterLists
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
                                    is Screen.MasterLists -> {
                                        ListsScreen(
                                            navController = navController,
                                            sharedViewModel = sharedViewModel,
                                            viewModel = startDestinationViewModel
                                        )
                                    }

                                    is Screen.ListDetails -> {
                                        ListItemsScreen(
                                            navController = navController,
                                            sharedViewModel = sharedViewModel
                                        )
                                    }

                                    is Screen.EditCategories -> {
                                        CategoriesScreen(navController = navController)
                                    }

                                    is Screen.EditTags -> {
                                        TagsScreen(navController = navController)
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

}
