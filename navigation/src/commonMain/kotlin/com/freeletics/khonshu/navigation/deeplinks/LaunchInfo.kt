package com.freeletics.khonshu.navigation.deeplinks

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri
import dev.drewhamilton.poko.Poko

/**
 * Represents information on how the app was launched in the form of a uri. This information can then be used
 * for deep linking.
 */
@Poko
public class LaunchInfo(
    public val uri: Uri?,
)

@Composable
internal expect fun obtainLaunchInfo(): LaunchInfo
