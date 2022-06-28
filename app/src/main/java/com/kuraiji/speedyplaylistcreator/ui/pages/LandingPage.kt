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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.work.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuraiji.speedyplaylistcreator.common.debugLog
import com.kuraiji.speedyplaylistcreator.data.PlaylistManager

import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

import com.kuraiji.speedyplaylistcreator.ui.theme.AppTypography
import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme
import com.kuraiji.speedyplaylistcreator.domain.DirectoryScanWorker
import com.kuraiji.speedyplaylistcreator.domain.LandingViewModel
import com.kuraiji.speedyplaylistcreator.domain.WorkerKeys
import com.kuraiji.speedyplaylistcreator.ui.MainActivity
import com.kuraiji.speedyplaylistcreator.ui.pages.destinations.MainDestination

internal const val WORKNAME = "scan"

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LandingPreview() {
    SpeedyPlaylistCreatorTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
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
                Button(
                    onClick = {}
                ) {
                    Text(text = "Select Directory")
                }
            }
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

//TODO: Rework Landing Page to be way less buggy and retain db data when closing and opening

@Composable
fun LandingPage(
    navigator: DestinationsNavigator? = null,
    viewModel: LandingViewModel = viewModel()
) {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    var scanRequest: OneTimeWorkRequest
    val (state, setState) = remember { mutableStateOf(WorkInfo.State.BLOCKED)}
    val (trackAmt, setTrackAmt) = remember { mutableStateOf(0) }
    LaunchedEffect(key1 = null) {
        workManager.pruneWork()
    }
    workManager.getWorkInfosForUniqueWorkLiveData(WORKNAME).observe(context as MainActivity) { workInfoList ->
        if(workInfoList == null || workInfoList.size < 1) return@observe
        setState(workInfoList[0].state)
        if(!viewModel.notEntered.value || !viewModel.started.value || workInfoList[0].state == WorkInfo.State.RUNNING) return@observe
        viewModel.notEntered.value = false
        setTrackAmt(workInfoList[0].outputData.getInt(WorkerKeys.TRACK_AMT, 0))
        navigator?.navigate(MainDestination())
    }
    val openDirectoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode != RESULT_OK) {
            return@rememberLauncherForActivityResult
        }
        it.data?.also { uri ->
            uri.data?.let { iUri ->
                DocumentFile.fromTreeUri(context, iUri)
                ?.let { dFile ->
                    PlaylistManager.saveBaseDir(context, dFile.uri)
                    scanRequest = OneTimeWorkRequestBuilder<DirectoryScanWorker>()
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiresStorageNotLow(true)
                                .setRequiresBatteryNotLow(true)
                                .build()
                        ).setInputData(
                            Data.Builder().putString(WorkerKeys.DIR_URI, dFile.uri.toString()).build()
                        ).build()
                    workManager.beginUniqueWork(WORKNAME, ExistingWorkPolicy.REPLACE, scanRequest).enqueue()
                }
            }
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
        Button(
            onClick = {
                viewModel.started.value = true
                openDirectoryLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
                      },
            enabled = state != WorkInfo.State.RUNNING && trackAmt < 1
            ) {
            Text(text = "Select Directory")
        }
    }
}