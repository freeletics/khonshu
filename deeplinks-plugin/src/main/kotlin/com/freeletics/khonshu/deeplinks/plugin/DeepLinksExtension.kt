package com.freeletics.khonshu.deeplinks.plugin

import org.gradle.api.file.RegularFileProperty

public abstract class DeepLinksExtension() {
    public abstract val deepLinkDefinitionsFile: RegularFileProperty
}
