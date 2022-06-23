package com.kuraiji.speedyplaylistcreator.ui.pages

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kuraiji.speedyplaylistcreator.common.debugLog
import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.domain.MainViewModel
import com.kuraiji.speedyplaylistcreator.ui.MainActivity
import com.kuraiji.speedyplaylistcreator.ui.components.AlbumTile

import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.annotation.Destination

import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme
import kotlin.collections.ArrayList


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun MainPreview() {
    val sampleAlbums: Array<PlaylistData.AlbumArtist> = arrayOf(
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
        PlaylistData.AlbumArtist("Shin Megami Tensei IV", "Atlus"),
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
                        Modifier
                            .padding(0.dp)
                            .aspectRatio(1F)
                    )
                }
            }
        }
    }
}

@Destination
@Composable
fun Main(
    navigator: DestinationsNavigator,
) {
    SpeedyPlaylistCreatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomePage(navigator = navigator)
        }
    }
}

@Composable
fun HomePage(
    navigator: DestinationsNavigator? = null,
    viewModel: MainViewModel = viewModel()
) {
    val albumState = viewModel.albums.observeAsState()
    val trackState = viewModel.tracks.observeAsState()

    LazyVerticalGrid(GridCells.Fixed(3)) {
        items(albumState.value?.size ?: 0) { index ->
            AlbumTile(albumArtist = albumState.value!![index],
                Modifier
                    .padding(0.dp)
                    .aspectRatio(1F)
            )
        }
    }
}