package com.yara.raco.ui.components

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yara.raco.R
import com.yara.raco.ui.theme.RacoTheme

@Composable
fun RacoLoginScreen(
    modifier: Modifier = Modifier,
    loginIntent: Intent,
    onLoginSuccess: () -> Unit,
    onLoginFailed: () -> Unit
) {
    val launchLogin =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                if (activityResult.data?.getBooleanExtra("loggedIn", false) == true) {
                    onLoginSuccess()
                    return@rememberLauncherForActivityResult
                }
            }
            onLoginFailed()
            return@rememberLauncherForActivityResult
        }

    RacoTheme {
        Scaffold(
            contentWindowInsets = WindowInsets.systemBars
        ) { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Center
            ) {
                Card {
                    Column(
                        modifier = Modifier.padding(all = 20.dp),
                    ) {
                        Image(
                            modifier = Modifier
                                .padding(20.dp)
                                .height(70.dp)
                                .width(70.dp)
                                .clip(CircleShape)
                                .align(CenterHorizontally),
                            painter = painterResource(id = R.drawable.app_icon),
                            contentDescription = stringResource(id = R.string.app_name)
                        )

                        Button(
                            onClick = { launchLogin.launch(loginIntent) },
                            modifier = Modifier.align(CenterHorizontally)
                        ) {
                            Text(text = stringResource(id = R.string.login))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RacoLoginWebScreen(
    onLoginUrl: (String) -> Unit,
    url: String
) {
    val (loading, setLoading) = remember { mutableStateOf(false) }

    RacoTheme {
        Scaffold(
            contentWindowInsets = WindowInsets.systemBars
        ) { paddingValues ->
            Box {
                RacoLoginWebView(
                    onLoginUrl = onLoginUrl,
                    onLoadingChange = setLoading,
                    url = url,
                    modifier = Modifier.padding(paddingValues)
                )

                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Center)
                    )
                }
            }
        }
    }
}

@Composable
fun RacoLoginWebView(
    onLoginUrl: (String) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    url: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val apply = WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                val webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        if (newProgress == 100) onLoadingChange(false)
                        else onLoadingChange(true)
                        super.onProgressChanged(view, newProgress)
                    }
                }

                this.webChromeClient = webChromeClient

                val webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        request?.url.toString().let { url ->
                            if (url.startsWith("apifib://yara")) {
                                visibility = View.GONE
                                WebStorage.getInstance().deleteAllData()
                                CookieManager.getInstance().removeAllCookies {
                                    CookieManager.getInstance().flush()
                                }
                                clearHistory()
                                clearFormData()
                                clearCache(true)
                                view?.webChromeClient = null
                                onLoadingChange(true)
                                onLoginUrl(url)
                            } else {
                                view?.loadUrl(url)
                            }
                        }
                        return super.shouldOverrideUrlLoading(view, request)
                    }
                }

                this.webViewClient = webViewClient

                settings.domStorageEnabled
                settings.javaScriptEnabled
                settings.userAgentString = context.getString(R.string.app_name)

                loadUrl(url)
            }
            apply
        },
        update = { webView ->
            webView.loadUrl(url)
        },
    )

}