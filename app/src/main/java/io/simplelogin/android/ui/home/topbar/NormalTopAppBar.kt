package io.simplelogin.android.ui.home.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.data.models.ui.AliasFilterMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalTopAppBar(
    selectedAliasFilterMode: AliasFilterMode,
    scrollBehavior: TopAppBarScrollBehavior,
    onOpenDrawer: () -> Unit,
    onSearchClick: () -> Unit,
    onSelectAliasFilterMode: (AliasFilterMode) -> Unit
) {
    val context = LocalContext.current
    var showFilterOptions by rememberSaveable { mutableStateOf(false) }
    MediumTopAppBar(
        title = {
            Text(selectedAliasFilterMode.title(context))
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.open_menu)
                )
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
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = stringResource(R.string.filter_options)
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