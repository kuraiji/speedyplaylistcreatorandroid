package com.kuraiji.speedyplaylistcreator.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuraiji.speedyplaylistcreator.common.debugLog
import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.domain.MainViewModel
import com.kuraiji.speedyplaylistcreator.ui.components.AlbumTile
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

//TODO: Make Album Update on another Thread

@Composable
fun AlbumView(
    navigator: DestinationsNavigator? = null,
    viewModel: MainViewModel = viewModel(),
    callback: (PlaylistData.AlbumArtist, Pair<Int, Int>) -> Unit
) {
    val albumState = viewModel.albums.observeAsState()
    val gridState = rememberLazyGridState()
    val show = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = null) {
        launch {
            gridState.scrollToItem(viewModel.albumIndex.value, viewModel.albumoffset.value)
        }
        show.value = true
    }
    AnimatedVisibility(
        visible = albumState.value != null && show.value,
        enter = fadeIn()
    ) {
        LazyVerticalGrid(GridCells.Fixed(3), state = gridState) {
            items(albumState.value!!,
                key = {item: PlaylistData.AlbumArtist ->  "${item.album}${item.artist}"})
            { item: PlaylistData.AlbumArtist ->
                AlbumTile(
                    albumArtist = item,
                    modifier = Modifier
                        .padding(0.dp)
                        .aspectRatio(1F)
                        .clickable {
                            callback(
                                item,
                                Pair(
                                    gridState.firstVisibleItemIndex,
                                    gridState.firstVisibleItemScrollOffset
                                )
                            )
                        }
                )
            }
        }
    }
}