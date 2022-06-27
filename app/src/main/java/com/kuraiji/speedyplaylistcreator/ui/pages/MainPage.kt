package com.kuraiji.speedyplaylistcreator.ui.pages

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kuraiji.speedyplaylistcreator.R
import com.kuraiji.speedyplaylistcreator.common.debugLog
import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.data.PlaylistManager
import com.kuraiji.speedyplaylistcreator.domain.MainViewModel
import com.kuraiji.speedyplaylistcreator.domain.MainViewModel.ViewKeys
import com.kuraiji.speedyplaylistcreator.ui.MainActivity
import com.kuraiji.speedyplaylistcreator.ui.components.AlbumTile
import com.kuraiji.speedyplaylistcreator.ui.components.TrackItem

import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.annotation.Destination

import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme
import kotlinx.coroutines.launch


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
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Track Dark Mode")
@Preview(showBackground = true, name = "Track Light Mode")
@Composable
fun MainTrackPreview() {
    val tracks: Array<PlaylistData.Track> = arrayOf(
        PlaylistData.Track(0, "Title Theme", 0, 0, "", "", ""),
        PlaylistData.Track(0, "Title Theme", 0, 0, "", "", ""),
        PlaylistData.Track(0, "Title Theme", 0, 0, "", "", ""),
        PlaylistData.Track(0, "Title Theme", 0, 0, "", "", ""),
        PlaylistData.Track(0, "Title Theme", 0, 0, "", "", ""),
        PlaylistData.Track(0, "Title Theme", 0, 0, "", "", ""),
        PlaylistData.Track(0, "Title Theme", 0, 0, "", "", ""),
        PlaylistData.Track(0, "Title Theme", 0, 0, "", "", ""),
        PlaylistData.Track(0, "Title Theme", 0, 0, "", "", ""),
        PlaylistData.Track(0, "Title Theme", 0, 0, "", "", ""),
    )
    val vw = LocalConfiguration.current.screenWidthDp
    val vh = LocalConfiguration.current.screenHeightDp
    SpeedyPlaylistCreatorTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(vw.dp)
                        .padding(vertical = (vh * .05).dp)
                ) {
                    Box(modifier = Modifier
                        .requiredSize((vw * .50).dp)
                        .background(MaterialTheme.colorScheme.error))
                    Text(
                        text = "Shin Megami Tensei IV",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                    Text(
                        text = "ATLUS",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 0.dp)
                    )
                }
                Spacer(modifier = Modifier.padding())
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(tracks) { track: PlaylistData.Track ->
                        TrackItem(track = track, modifier = Modifier)
                    }
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
    val (selectedAlbum, setSelectedAlbum) = remember {
        mutableStateOf(PlaylistData.AlbumArtist("",""))
    }
    val currentView = viewModel.currentView.observeAsState()
    val albumViewCallback: (PlaylistData.AlbumArtist) -> Unit = { albumArtist ->
        setSelectedAlbum(albumArtist)
        viewModel.currentView.value = ViewKeys.VIEW_KEY_TRACK
    }
    val trackViewCallback: (PlaylistData.Track) -> Unit = { track ->
        viewModel.toggleAddToPlaylist(track)
    }
    BackHandler(enabled = true) {viewModel.currentView.value = ViewKeys.VIEW_KEY_ALBUM}
    SpeedyPlaylistCreatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = {BottomAppBar() {
                        IconButton(onClick = {viewModel.savePlaylist()}) {
                            Icon(painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "Test")
                        }
                        IconButton(onClick = {viewModel.loadPlaylist()}) {
                            Icon(painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "Test")
                        }
                    }}
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when(currentView.value) {
                        0 -> AlbumView(navigator, viewModel, albumViewCallback)
                        1 -> TrackView(navigator, viewModel, selectedAlbum, trackViewCallback)
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumView(
    navigator: DestinationsNavigator? = null,
    viewModel: MainViewModel = viewModel(),
    callback: (PlaylistData.AlbumArtist) -> Unit
) {
    //val context = LocalContext.current
    val albumState = viewModel.albums.observeAsState()
    //val trackState = viewModel.tracks.observeAsState()

    if(albumState.value != null) {
        LazyVerticalGrid(GridCells.Fixed(3)) {
            items(albumState.value!!,
                key = {item: PlaylistData.AlbumArtist ->  "${item.album}${item.artist}"})
            { item: PlaylistData.AlbumArtist ->
                AlbumTile(
                    albumArtist = item,
                    modifier = Modifier
                        .padding(0.dp)
                        .aspectRatio(1F)
                        .clickable { callback(item) }
                )
            }
        }
    }
}

@Composable
fun TrackView(
    navigator: DestinationsNavigator?,
    viewModel: MainViewModel = viewModel(),
    albumArtist: PlaylistData.AlbumArtist,
    callback: (PlaylistData.Track) -> Unit
){
    val context = LocalContext.current
    val vw = LocalConfiguration.current.screenWidthDp
    val vh = LocalConfiguration.current.screenHeightDp
    val (tracks, setTracks) = remember { mutableStateOf<Array<PlaylistData.Track>?>(null) }
    val (bitmap, setBitmap) = remember { mutableStateOf<Bitmap?>(null)}
    LaunchedEffect(key1 = albumArtist) {
        launch {
            PlaylistManager.getAlbumTracks(context, albumArtist).observe(context as MainActivity) { tracks ->
                setTracks(tracks)
            }
            setBitmap(PlaylistManager.getAlbumCover(context, albumArtist))
        }
    }

    Column {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(vw.dp)
                .padding(vertical = (vh * .05).dp)
        ) {
            if(bitmap == null) {
                Box(modifier = Modifier
                    .requiredSize((vw * .50).dp)
                    .background(MaterialTheme.colorScheme.error)
                )
            }
            if(bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Cover Art",
                    modifier = Modifier.requiredSize((vw * .50).dp),
                    contentScale = ContentScale.FillBounds
                )
            }
            Text(
                text = albumArtist.album,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 5.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = albumArtist.artist,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 0.dp),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.padding())
        if(tracks != null) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tracks, key = {track -> track.uri}) { track: PlaylistData.Track ->
                    TrackItem(track = track, modifier = Modifier
                        .clickable { callback(track) }
                        .background(
                            if(viewModel.playlist.containsKey(track.uri))
                                MaterialTheme.colorScheme.surfaceTint
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }

}