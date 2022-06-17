package com.kuraiji.speedyplaylistcreator.ui.pages

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuraiji.speedyplaylistcreator.common.debugLog

import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

import com.kuraiji.speedyplaylistcreator.domain.LandingViewModel
import com.kuraiji.speedyplaylistcreator.ui.theme.AppTypography
import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme
import kotlinx.coroutines.launch


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LandingPreview() {
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
fun LandingDestination(
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
    navigator: DestinationsNavigator? = null,
    viewModel: LandingViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val openDirectoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode != RESULT_OK) {
            return@rememberLauncherForActivityResult
        }
        it.data?.also { uri ->
            uri.data?.let { iUri -> DocumentFile.fromTreeUri(context, iUri)
                ?.let { dFile -> coroutineScope.launch {
                    if(viewModel.pushDocFile(dFile) < 1) return@launch
                    debugLog("Next Screen")
                } } }
        }
    }

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
        Button(onClick = {openDirectoryLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))}) {
            Text(text = "Select Directory")
        }
    }
}