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
actual fun VideoPlayer(
    url: String,
    fallbackUrl: String?,
    modifier: Modifier,
) {
    // WebKit plays HLS itself but cannot strip the provider's segment wrapper, so iOS keeps
    // using the original embed page rather than the resolved playlist.
    val target = fallbackUrl ?: url
    UIKitView(
        modifier = modifier,
        factory = {
            val webView = WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = WKWebViewConfiguration(),
            )
            val nsUrl = NSURL(string = target)
            if (nsUrl != null) {
                webView.loadRequest(NSURLRequest(uRL = nsUrl))
            }
            webView
        },
    )
}
