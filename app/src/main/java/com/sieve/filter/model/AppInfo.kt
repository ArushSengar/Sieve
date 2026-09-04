package com.sieve.filter.model

import android.graphics.drawable.Drawable

/**
 * Representation of an application for the App Rules screen.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val mode: AppRuleMode = AppRuleMode.AUTO
)
