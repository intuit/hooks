package com.intuit.hooks.docs

import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.resources.resourcesource.DelegatingResourceSource
import com.eden.orchid.api.resources.resourcesource.OrchidResourceSource
import com.eden.orchid.api.resources.resourcesource.ThemeResourceSource
import com.eden.orchid.api.theme.Theme
import com.eden.orchid.api.theme.assets.AssetManagerDelegate
import com.eden.orchid.api.theme.models.Social
import com.eden.orchid.copper.CopperTheme
import com.eden.orchid.utilities.OrchidUtils
import javax.inject.Inject

class HooksTheme @Inject constructor(context: OrchidContext) : Theme(context, "HooksTheme", OrchidUtils.DEFAULT_PRIORITY + 1) {

    private val delegateTheme = CopperTheme(context)

    @Option
    lateinit var social: Social

    override fun loadAssets(delegate: AssetManagerDelegate) {
        delegate.addCss("assets/css/bulma.min.css")
        delegate.addCss("assets/css/bulma-tooltip.css")
        delegate.addCss("assets/css/bulma-accordion.min.css")

        delegate.addJs("https://use.fontawesome.com/releases/v5.4.0/js/all.js").apply { defer = true }
        delegate.addJs("assets/js/bulma.js")
        delegate.addJs("assets/js/bulma-accordion.min.js")
        delegate.addJs("assets/js/bulma-tabs.js")
    }

    override fun getResourceSource(): OrchidResourceSource =
        DelegatingResourceSource(
            listOfNotNull(super.getResourceSource(), delegateTheme.resourceSource),
            emptyList(),
            priority,
            ThemeResourceSource
        )
}
