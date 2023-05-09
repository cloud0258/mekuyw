package com.slotsoing.mekuyw

import android.content.ComponentName
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

//    private val activityName = "org.cocos2dx.javascript.AppActivity"
    private val activityName = "org.cocos2dx.javascript.AppActivity"
    private var path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadResource(DePluginSP.getInstance(this).getString(Constants.COPY_FILE_PATH, ""))
        setContentView(R.layout.activity_main)
        init()
        mergeDex()
        findViewById<Button>(R.id.btn).setOnClickListener {
            load()
        }
    }

    private fun mergeDex() {
//        setSO()
        if (path.isEmpty()) {
            Log.i("sky", "path is empty")
            return
        }
        val dir = path.substring(0, path.lastIndexOf("/"))
        Log.i("sky", "dir is $dir")
        val apkFile = File(path)
        val optDexFile = File("$dir/out.dex")
        "path: $path, dir: $dir, apkPath: ${apkFile.absolutePath}, dexPath: ${optDexFile.absolutePath}".logI()
        if (!optDexFile.exists()) {
            val result = optDexFile.createNewFile()
            "create dex file result is $result".logI()
        }
        Utils.mergeDex(classLoader, apkFile, optDexFile)
    }

    private fun init() {
        path = DePluginSP.getInstance(this).getString(Constants.COPY_FILE_PATH, "")
    }

    private fun loadResource(dexPath: String) {
        val mAssetManager = assets
        val result =
            RefInvoke.on(mAssetManager, "addAssetPath", String::class.java).invoke<Int>(dexPath)
        Log.i("sky", "add asset path result is $result")
        val superRes = super.getResources()
        val mResources = Resources(mAssetManager, superRes.displayMetrics, superRes.configuration)
        val mTheme = mResources.newTheme()
        mTheme.setTo(super.getTheme())
    }

    private fun load() {
        val newIntent = Intent()
        newIntent.component = ComponentName(packageName, activityName)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(newIntent)
    }
}