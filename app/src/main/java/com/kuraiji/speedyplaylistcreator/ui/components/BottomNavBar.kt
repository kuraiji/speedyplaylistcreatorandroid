package com.kuraiji.speedyplaylistcreator.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kuraiji.speedyplaylistcreator.R
import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, name = "Light Mode")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBarPreview() {
    SpeedyPlaylistCreatorTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = { BottomNavBar({}, {}, {}) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues))
            }
        }
    }
}

@Composable
fun BottomNavBar(
    albumCallback: () -> Unit,
    playlistCallback: () -> Unit,
    settingsCallback: () -> Unit
) {
    val vh = LocalConfiguration.current.screenHeightDp
    val modifier: Modifier = Modifier.size( (vh * .1).dp ).aspectRatio(1F)

    BottomAppBar(modifier = Modifier.height((vh * .08).dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {albumCallback()}
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_album_24),
                    contentDescription = "Test",
                    modifier = modifier
                )
            }
            IconButton(
                onClick = {playlistCallback()}
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_list_alt_24),
                    contentDescription = "Test",
                    modifier = modifier
                )
            }
            IconButton(
                onClick = {settingsCallback()},
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_baseline_settings_24),
                    contentDescription = "Test",
                    modifier = modifier
                )
            }
        }
    }
}