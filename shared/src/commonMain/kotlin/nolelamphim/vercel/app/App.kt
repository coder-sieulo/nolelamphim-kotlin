package nolelamphim.vercel.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import nolelamphim.vercel.app.navigation.AppNavHost

@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(),
    ) {
        AppNavHost()
    }
}
