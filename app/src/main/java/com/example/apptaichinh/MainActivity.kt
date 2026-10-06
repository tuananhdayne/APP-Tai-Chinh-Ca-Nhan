package com.example.apptaichinh

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.apptaichinh.theme.AppTaiChinhTheme
import com.example.apptaichinh.ui.MainApp
import com.example.apptaichinh.ui.viewmodel.FinanceViewModel
import com.example.apptaichinh.widget.FinanceAppWidgetProvider

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            AppTaiChinhTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val targetTab = intent?.getIntExtra(FinanceAppWidgetProvider.EXTRA_TARGET_TAB, -1) ?: -1
        if (targetTab in 0..4) {
            viewModel.setCurrentTab(targetTab)
        }
    }
}
