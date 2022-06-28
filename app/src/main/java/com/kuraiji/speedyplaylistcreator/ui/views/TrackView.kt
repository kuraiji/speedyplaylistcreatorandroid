package com.kuraiji.speedyplaylistcreator.ui.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.data.PlaylistManager
import com.kuraiji.speedyplaylistcreator.domain.MainViewModel
import com.kuraiji.speedyplaylistcreator.ui.MainActivity
import com.kuraiji.speedyplaylistcreator.ui.components.TrackItem
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

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
    val (bitmap, setBitmap) = remember { mutableStateOf<Bitmap?>(null) }
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
                            if (viewModel.playlist.containsKey(track.uri))
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