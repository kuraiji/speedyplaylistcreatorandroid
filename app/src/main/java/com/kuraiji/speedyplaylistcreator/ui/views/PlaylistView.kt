package com.kuraiji.speedyplaylistcreator.ui.views

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuraiji.speedyplaylistcreator.R
import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.domain.MainViewModel
import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun MainAlbumPreview() {
    val tracks = arrayOf<PlaylistData.Track>(
        PlaylistData.Track(0, "Metal Gear Solid Main Theme", 0, 0, "Metal Gear Solid Main Theme", "Konami", ""),
        PlaylistData.Track(0, "Main Theme", 0, 0, "Metal Gear Solid", "Konami", ""),
        PlaylistData.Track(0, "Main Theme", 0, 0, "Metal Gear Solid", "Konami", ""),
        PlaylistData.Track(0, "Main Theme", 0, 0, "Metal Gear Solid", "Konami", ""),
        PlaylistData.Track(0, "Main Theme", 0, 0, "Metal Gear Solid", "Konami", ""),
        PlaylistData.Track(0, "Main Theme", 0, 0, "Metal Gear Solid", "Konami", ""),
        PlaylistData.Track(0, "Main Theme", 0, 0, "Metal Gear Solid", "Konami", ""),
        PlaylistData.Track(0, "Main Theme", 0, 0, "Metal Gear Solid", "Konami", ""),
    )

    SpeedyPlaylistCreatorTheme(dynamicColor = false) {
        val vw = LocalConfiguration.current.screenWidthDp
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Playlist Manager",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(10.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(30.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = {  }) {
                        Text(text = "SAVE")
                    }
                    Button(onClick = {  }) {
                        Text(text = "LOAD")
                    }
                }
                LazyColumn(
                    Modifier.padding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(tracks) { track ->
                        ElevatedCard(
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                Modifier.padding(5.dp),

                            ) {
                                Text(
                                    text = "${track.title} - ${track.album}",
                                    modifier = Modifier.width((vw * .8).dp),
                                    textAlign = TextAlign.Center
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_remove_24),
                                    contentDescription = "Remove from playlist",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.padding(vertical = 1.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistView(
    navigator: DestinationsNavigator?,
    viewModel: MainViewModel = viewModel(),
    saveCallback: () -> Unit,
    loadCallback: () -> Unit,
    removeTrackCallback: (PlaylistData.Track) -> Unit
) {
    val tracks = viewModel.playlist.values.toMutableList().sortedBy { it.second }
    val vw = LocalConfiguration.current.screenWidthDp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Playlist Manager",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(10.dp)
        )
        Row(
            modifier = Modifier
                .padding(30.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { saveCallback() }) {
                Text(text = "SAVE")
            }
            Button(onClick = { loadCallback() }) {
                Text(text = "LOAD")
            }
        }
        LazyColumn(
            Modifier.padding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(tracks.toTypedArray()) { track ->
                ElevatedCard(
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        Modifier.padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${track.first.title} - ${track.first.album}",
                            modifier = Modifier.width((vw * .8).dp),
                            textAlign = TextAlign.Center
                            )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_remove_24),
                            contentDescription = "Remove from playlist",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.clickable { removeTrackCallback(track.first) }
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 1.dp))
            }
        }
    }
}