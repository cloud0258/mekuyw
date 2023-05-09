package com.slotsoing.mekuyw

import android.app.Application
import android.content.Context

class MyApp: Application() {

    companion object {
        var mContext: Context? = null
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
        Utils.copyApk(this,Constants.PLUGIN_NAME_ONE)
        Utils.mapping()
    }
}