package com.santimattius.kvs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.santimattius.kvs.navigation.AppNavigation
import com.santimattius.kvs.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                Scaffold() {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(it)) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun AppNavigationPreview() {
    AppTheme {
        AppNavigation()
    }
}