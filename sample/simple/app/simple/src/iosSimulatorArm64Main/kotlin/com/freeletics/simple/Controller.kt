package com.freeletics.simple

import com.freeletics.khonshu.navigation.deeplinks.LaunchInfo
import com.freeletics.sample.main.KhonshuMainScreenViewController
import platform.UIKit.UIViewController

actual fun createViewController(): UIViewController {
    return KhonshuMainScreenViewController(App, LaunchInfo(null)).viewController()
}
