package com.kuraiji.speedyplaylistcreator.ui.pages

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.DocumentsContract
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kuraiji.speedyplaylistcreator.common.debugLog

import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.domain.MainViewModel
import com.kuraiji.speedyplaylistcreator.domain.MainViewModel.ViewKeys
import com.kuraiji.speedyplaylistcreator.ui.components.AlbumTile
import com.kuraiji.speedyplaylistcreator.ui.components.BottomNavBar
import com.kuraiji.speedyplaylistcreator.ui.components.TrackItem
import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme
import com.kuraiji.speedyplaylistcreator.ui.views.AlbumView
import com.kuraiji.speedyplaylistcreator.ui.views.PlaylistView
import com.kuraiji.speedyplaylistcreator.ui.views.TrackView

import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.annotation.Destination

val MIME_TYPE = "audio/mpegurl"
val MIME_TYPES = arrayOf("audio/mpegurl", "audio/x-mpegurl")

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Album Dark Mode")
@Preview(showBackground = true, name = "Album Light Mode")
@Composable
fun MainAlbumPreview() {
    val sampleAlbums: Array<PlaylistData.AlbumArtist> = arrayOf(
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
    )

    SpeedyPlaylistCreatorTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyVerticalGrid(GridCells.Fixed(3)) { 
                items(sampleAlbums.size) { index ->
                    AlbumTile(albumArtist = sampleAlbums[index],
                        modifier = Modifier
                            .padding(0.dp)
                            .aspectRatio(1F)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun Main(
    navigator: DestinationsNavigator,
    viewModel: MainViewModel = viewModel(),
) {
    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if(result.resultCode != RESULT_OK) return@rememberLauncherForActivityResult
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        viewModel.savePlaylist(uri)
    }
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if(result.resultCode != RESULT_OK) return@rememberLauncherForActivityResult
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        viewModel.loadPlaylist(uri)
    }
    val (selectedAlbum, setSelectedAlbum) = remember {
        mutableStateOf(PlaylistData.AlbumArtist("",""))
    }
    val currentView = viewModel.currentView
    val albumViewCallback: (PlaylistData.AlbumArtist, Pair<Int, Int>) -> Unit = { albumArtist, index ->
        viewModel.setAlbumIndex(index.first, index.second)
        setSelectedAlbum(albumArtist)
        viewModel.goToView(ViewKeys.VIEW_KEY_TRACK)
    }
    val trackViewCallback: (PlaylistData.Track) -> Unit = { track ->
        viewModel.toggleAddToPlaylist(track)
    }

    val savePlaylistCallback: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createFileLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType(MIME_TYPE)
                .putExtra(Intent.EXTRA_MIME_TYPES, MIME_TYPES)
                .putExtra(DocumentsContract.EXTRA_INITIAL_URI, viewModel.baseDirUri.toString())
            )
        }
        else {
            createFileLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType(MIME_TYPE)
                .putExtra(Intent.EXTRA_MIME_TYPES, MIME_TYPES)
            )
        }
    }
    val loadPlaylistCallback: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            openFileLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT)
                .putExtra(DocumentsContract.EXTRA_INITIAL_URI, viewModel.baseDirUri.toString())
                .setType("*/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, MIME_TYPES)
            )
        }
        else {
            openFileLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("*/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, MIME_TYPES)
            )
        }
    }
    val removeTrackCallback: (PlaylistData.Track) -> Unit = { track ->
        viewModel.removeFromPlaylist(track)
    }
    BackHandler(enabled = true) {
        viewModel.goBack()
    }
    SpeedyPlaylistCreatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = { BottomNavBar(
                    albumCallback = { viewModel.goToView(ViewKeys.VIEW_KEY_ALBUM) },
                    playlistCallback = { viewModel.goToView(ViewKeys.VIEW_KEY_PLAYLIST) },
                    settingsCallback = { viewModel.goToView(ViewKeys.VIEW_KEY_SETTINGS) })
                }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when(currentView.value) {
                        0 -> AlbumView(navigator, viewModel, albumViewCallback)
                        1 -> TrackView(navigator, viewModel, selectedAlbum, trackViewCallback)
                        2 -> PlaylistView(navigator, viewModel, savePlaylistCallback, loadPlaylistCallback, removeTrackCallback)
                    }
                }
            }
        }
    }
}



