package com.rakuishi.rrr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rakuishi.rrr.ui.MainScreen
import com.rakuishi.rrr.ui.theme.RRRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RRRTheme {
                MainScreen()
            }
        }
    }
}
