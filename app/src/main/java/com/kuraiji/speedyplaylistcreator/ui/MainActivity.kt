package com.kuraiji.speedyplaylistcreator.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import com.kuraiji.speedyplaylistcreator.ui.pages.NavGraphs
import com.ramcosta.composedestinations.DestinationsNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DestinationsNavHost(navGraph = NavGraphs.root)
        }
    }
    fun openDirectory() {
        val result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
                if(result.resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, result.data.toString(), Toast.LENGTH_LONG).show()
                }
        }
        result.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
    }
}

//context.startActivity(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))