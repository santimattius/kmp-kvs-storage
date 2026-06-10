package com.santimattius.kmp.context

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Service
import android.app.backup.BackupAgent
import android.content.Context
import android.content.ContextWrapper

fun Context.canLeakMemory(): Boolean = when (this) {
    is Application -> false
    is Activity, is Service, is BackupAgent -> true
    is ContextWrapper -> if (baseContext === this) true else baseContext.canLeakMemory()
    else -> applicationContext === null
}

@SuppressLint("StaticFieldLeak")
private var applicationContext: Context? = null

internal fun injectContext(context: Context) {
    require(!context.canLeakMemory()) { "The passed $context would leak memory!" }
    applicationContext = context
}

fun getApplicationContext(): Context = applicationContext ?: error("Context not injected!")
