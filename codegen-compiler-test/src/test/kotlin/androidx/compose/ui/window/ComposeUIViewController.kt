@file:Suppress("ktlint:standard:function-naming")
package androidx.compose.ui.window

import androidx.compose.runtime.Composable
import platform.UIKit.UIViewController

fun ComposeUIViewController(content: @Composable () -> Unit): UIViewController = UIViewController()
