package blazern.lexisoup.privacy_policy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lexisoup.feature.privacy_policy.generated.resources.Res

@Composable
fun PrivacyPolicyScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            val coroutineScope = rememberCoroutineScope()
            var textFromFile by remember { mutableStateOf("") }

            // Trigger file reading asynchronously
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    textFromFile = Res.readPrivacyPolicy()
                }
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp)
                        .verticalScroll(rememberScrollState())
            ) {
                Text(text = textFromFile)
            }
        }
    }
}

private suspend fun Res.readPrivacyPolicy(): String = withContext(Dispatchers.Default) {
    readBytes("files/privacy_policy.md").decodeToString()
}
