package io.simplelogin.android.ui.home.topbar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.data.models.preferences.Theme
import io.simplelogin.android.data.models.ui.AliasFilterMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalTopAppBar(
    theme: Theme,
    isPremium: Boolean,
    selectedAliasFilterMode: AliasFilterMode,
    scrollBehavior: TopAppBarScrollBehavior,
    onOpenDrawer: () -> Unit,
    onSearchClick: () -> Unit,
    onSelectAliasFilterMode: (AliasFilterMode) -> Unit
) {
    val context = LocalContext.current
    var showFilterOptions by rememberSaveable { mutableStateOf(false) }
    val isDark = when (theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.MATCH_SYSTEM -> isSystemInDarkTheme()
    }
    MediumTopAppBar(
        title = {
            Text(selectedAliasFilterMode.title(context))
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                if (isPremium) {
                    Icon(
                        painter = painterResource(
                            if (isDark) R.drawable.ic_premium_menu_night else R.drawable.ic_premium_menu
                        ),
                        contentDescription = stringResource(R.string.open_menu),
                        tint = Color.Unspecified
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.open_menu)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }

            Box {
                IconButton(onClick = { showFilterOptions = true }) {
                    Icon(
                        imageVector = if (selectedAliasFilterMode == AliasFilterMode.ALL)
                            Icons.Outlined.FilterAlt else Icons.Default.FilterAlt,
                        contentDescription = stringResource(R.string.filter_options),
                        tint = if (selectedAliasFilterMode == AliasFilterMode.ALL)
                            LocalContentColor.current else MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showFilterOptions,
                    onDismissRequest = { showFilterOptions = false }
                ) {
                    AliasFilterMode.entries.toTypedArray().forEach { mode ->
                        DropdownMenuItem(
                            trailingIcon = {
                                if (selectedAliasFilterMode == mode) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null
                                    )
                                }
                            },
                            text = {
                                Text(text = mode.title(context))
                            },
                            onClick = {
                                onSelectAliasFilterMode(mode)
                                showFilterOptions = false
                            }
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}