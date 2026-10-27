package com.easylists.presentation

//import com.easylists.presentation.ui.theme.EasyListsTheme
import android.content.Intent
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
import com.easylists.data.share.PendingInviteStore
import com.easylists.domain.models.InviteLinks
import com.easylists.presentation.common.LocalCapitalization
import com.easylists.presentation.models.Screen
import com.easylists.presentation.ui.about.AboutScreen
import com.easylists.presentation.ui.categories.CategoriesScreen
import com.easylists.presentation.ui.invite.InviteJoinHost
import com.easylists.presentation.ui.invite.InviteJoinViewModel
import com.easylists.presentation.ui.listitems.ListItemsScreen
import com.easylists.presentation.ui.lists.ListsScreen
import com.easylists.presentation.ui.lists.ListsViewModel
import com.easylists.presentation.ui.settings.SettingsScreen
import com.easylists.presentation.ui.tags.TagsScreen
import com.easylists.presentation.ui.theme.EasyListsTheme
import com.easylists.presentation.ui.theme.spaces
import dagger.hilt.android.AndroidEntryPoint
import dev.olshevski.navigation.reimagined.NavBackHandler
import dev.olshevski.navigation.reimagined.NavHost
import dev.olshevski.navigation.reimagined.rememberNavController
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private val startDestinationViewModel: ListsViewModel by viewModels()
    private val inviteViewModel: InviteJoinViewModel by viewModels()

    @Inject
    lateinit var pendingInvites: PendingInviteStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // A link that started the app; not handled again after a rotation
        if (savedInstanceState == null) {
            handleInviteLink(intent)
        }

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalCapitalization provides (
                        settings?.capitalization ?: Capitalization.NoCapitalization
                )
            ) {
                EasyListsTheme() {
                    val navController = rememberNavController<Screen>(
                        startDestination = Screen.Lists
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
                                    is Screen.Lists -> {
                                        ListsScreen(
                                            navController = navController,
                                            viewModel = startDestinationViewModel
                                        )
                                    }

                                    is Screen.ListItems -> {
                                        ListItemsScreen(
                                            navController = navController,
                                            listId = route.listId,
                                            listName = route.listName
                                        )
                                    }

                                    is Screen.Categories -> {
                                        CategoriesScreen(navController = navController)
                                    }

                                    is Screen.Tags -> {
                                        TagsScreen(navController = navController)
                                    }

                                    is Screen.Settings -> {
                                        SettingsScreen(navController = navController)
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

                            InviteJoinHost(
                                viewModel = inviteViewModel,
                                navController = navController,
                            )

                        }
                    }
                }
            }
        }
    }


    // A link that arrives while the app is already open
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleInviteLink(intent)
    }


    private fun handleInviteLink(intent: Intent?) {
        val link = intent?.data?.toString() ?: return
        InviteLinks.tokenFrom(link)?.let { pendingInvites.set(it) }
    }

}
