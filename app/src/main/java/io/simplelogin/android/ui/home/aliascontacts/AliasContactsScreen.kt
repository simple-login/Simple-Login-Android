package io.simplelogin.android.ui.home.aliascontacts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.ui.home.dialog.EditEmailDialog
import io.simplelogin.android.ui.home.aliaslist.AliasEmailText
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.util.TitledFAB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasContactsScreen(
    alias: Alias,
    onGoBack: () -> Unit
) {
    var fabExpanded by rememberSaveable { mutableStateOf(false) }
    Surface(color = SlColor.BackgroundColor) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { AliasEmailText(alias = alias) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = onGoBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent,
            floatingActionButton = {
                ContactsScreenFAB(
                    expanded = fabExpanded,
                    onClick = { fabExpanded = !fabExpanded },
                    onContactPicked = {},
                    onCreate = {}
                )
            }
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                item {
                    Text(text = alias.email)
                }
            }
        }
    }
}

@Composable
private fun ContactsScreenFAB(
    expanded: Boolean,
    onClick: () -> Unit,
    onContactPicked: (Uri) -> Unit,
    onCreate: (String) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "fab_rotation"
    )

    val pickContactsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri ->
        contactUri?.let { onContactPicked(it) }
    }

    var showEmailDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                TitledFAB(
                    title = stringResource(R.string.pick_from_contacts),
                    imageVector = Icons.Default.Contacts,
                    onClick = {
                        onClick()
                        pickContactsLauncher.launch(null)
                    }
                )

                TitledFAB(
                    title = stringResource(R.string.enter_contact_email),
                    imageVector = Icons.Default.Edit,
                    onClick = {
                        onClick()
                        showEmailDialog = true
                    }
                )
            }
        }

        FloatingActionButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.create_new_contact),
                modifier = Modifier.rotate(rotation)
            )
        }
    }

    if (showEmailDialog) {
        EditEmailDialog(
            title = stringResource(R.string.enter_contact_email),
            ctaTitle = stringResource(R.string.create),
            onAdd = {
                showEmailDialog = false
                onCreate(it)
            },
            onDismiss = { showEmailDialog = false }
        )
    }
}