package com.kuraiji.speedyplaylistcreator.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun TrackItemPreview() {
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
    SpeedyPlaylistCreatorTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
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

@Composable
fun TrackItem(
        track: PlaylistData.Track,
        modifier: Modifier
) {
    Text(
        text = track.title,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .fillMaxWidth()
            //.background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp),
        textAlign = TextAlign.Center,
    )
}