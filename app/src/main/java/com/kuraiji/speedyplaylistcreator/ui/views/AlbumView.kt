package com.kuraiji.speedyplaylistcreator.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.domain.MainViewModel
import com.kuraiji.speedyplaylistcreator.ui.components.AlbumTile
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun AlbumView(
    navigator: DestinationsNavigator? = null,
    viewModel: MainViewModel = viewModel(),
    callback: (PlaylistData.AlbumArtist) -> Unit
) {
    val albumState = viewModel.albums.observeAsState()
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