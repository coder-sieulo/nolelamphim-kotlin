package nolelamphim.vercel.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import nolelamphim.vercel.app.navigation.AppNavHost
import nolelamphim.vercel.app.ui.theme.CinemaTheme

@Composable
@Preview
fun App() {
    CinemaTheme {
        AppNavHost()
    }
}
