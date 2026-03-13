package com.takatagit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.takatagit.app.AppContainer
import com.takatagit.app.ui.TakataGitApp
import com.takatagit.app.ui.theme.TakataGitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.initialize(applicationContext)
        setContent {
            TakataGitTheme {
                TakataGitApp()
            }
        }
    }
}
