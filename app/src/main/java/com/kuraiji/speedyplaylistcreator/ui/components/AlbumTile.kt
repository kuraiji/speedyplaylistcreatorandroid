package com.kuraiji.speedyplaylistcreator.ui.components

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.data.PlaylistManager
import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme
import kotlinx.coroutines.launch

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun AlbumTilePreview() {
    SpeedyPlaylistCreatorTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AlbumTile(albumArtist = PlaylistData.AlbumArtist("Shin Megami Tensei IV: Original Soundtrack", "Atlus"), modifier = Modifier)
        }
    }
}

@Composable
fun AlbumTile(
    albumArtist: PlaylistData.AlbumArtist,
    modifier: Modifier
){
    val configuration = LocalConfiguration.current
    val vw = configuration.screenWidthDp.dp
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val (bitmap, setBitmap) = remember { mutableStateOf<Bitmap?>(null)}
    LaunchedEffect(key1 = null) {
        coroutineScope.launch {
            setBitmap(PlaylistManager.getAlbumCover(context, albumArtist))
        }
    }
    
    Box(
        modifier = modifier
        //.requiredSize(vw * .3F)
        .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            if(bitmap == null) {
                Text(
                    modifier = Modifier.padding(15.dp),
                    text = albumArtist.album,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    modifier = Modifier.padding(15.dp),
                    text = albumArtist.artist,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if(bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Cover Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                    )
            }
        }
    }
}