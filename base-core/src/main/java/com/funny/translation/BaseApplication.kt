package com.funny.translation

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.tencent.mmkv.MMKV
import kotlin.properties.Delegates


open class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ctx = this
        MMKV.initialize(this)
    }

    companion object {
        var ctx : Application by Delegates.notNull()
        val resources: Resources get() = ctx.resources
    }
}