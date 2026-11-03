@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package nolelamphim.vercel.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@Composable
actual fun VideoPlayer(url: String, modifier: Modifier) {
    UIKitView(
        modifier = modifier,
        factory = {
            val webView = WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = WKWebViewConfiguration(),
            )
            val nsUrl = NSURL(string = url)
            if (nsUrl != null) {
                webView.loadRequest(NSURLRequest(uRL = nsUrl))
            }
            webView
        },
    )
}
