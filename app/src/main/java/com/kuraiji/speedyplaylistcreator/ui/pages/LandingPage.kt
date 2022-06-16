package com.kuraiji.speedyplaylistcreator.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

import com.kuraiji.speedyplaylistcreator.common.findActivity
import com.kuraiji.speedyplaylistcreator.ui.theme.AppTypography
import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun Preview() {
    SpeedyPlaylistCreatorTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LandingPage()
        }
    }
}

@RootNavGraph(start = true)
@Destination
@Composable
fun Destination(
    navigator: DestinationsNavigator
) {
    SpeedyPlaylistCreatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LandingPage(navigator)
        }
    }
}

@Composable
fun LandingPage(
    navigator: DestinationsNavigator? = null
) {
    val activity = LocalContext.current.findActivity();

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(
            text = "Welcome to Speedy Playlist Creator!",
            style = AppTypography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Please select the top-level directory of your local music files to begin.",
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = {activity?.openDirectory()}) {
            Text(text = "Select Directory")
        }
    }
}