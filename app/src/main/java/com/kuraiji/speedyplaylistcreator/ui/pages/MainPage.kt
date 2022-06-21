package com.kuraiji.speedyplaylistcreator.ui.pages

import android.content.res.Configuration
import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kuraiji.speedyplaylistcreator.common.debugLog
import com.kuraiji.speedyplaylistcreator.domain.MainViewModel
import com.kuraiji.speedyplaylistcreator.ui.MainActivity

import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.annotation.Destination

import com.kuraiji.speedyplaylistcreator.ui.theme.SpeedyPlaylistCreatorTheme


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun MainPreview() {
    SpeedyPlaylistCreatorTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomePage()
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

    Text(text = "Konichiwa")
    Button(onClick = {viewModel.test()}) {}
}