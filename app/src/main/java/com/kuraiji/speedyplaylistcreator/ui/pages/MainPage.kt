package com.kuraiji.speedyplaylistcreator.ui.pages

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.annotation.Destination

import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun MainPreview() {
    SpeedyPlaylistCreatorTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomePage(uris = ArrayList())
        }
    }
}

@Destination
@Composable
fun MainDestination(
    navigator: DestinationsNavigator,
) {
    SpeedyPlaylistCreatorTheme() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomePage(navigator = navigator, uris = ArrayList())
        }
    }
}

@Composable
fun HomePage(
    navigator: DestinationsNavigator? = null,
    uris: ArrayList<Uri>
) {
    Text(text = "Konichiwa")
}