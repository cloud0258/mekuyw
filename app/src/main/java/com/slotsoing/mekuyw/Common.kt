package com.slotsoing.mekuyw

import android.content.Context
import android.util.Log
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexClassLoader
import java.io.File

fun String?.logI() {
    Log.i("sky", this ?: "logIEmpty")
}

fun Int?.logI() {
    this?.toString().logI()
}

fun Boolean?.logI() {
    this?.toString().logI()
}

fun mergePluginDex() {

}

fun mergePluginDex(context: Context, pluginFile: File) {
    val dexPathListField = BaseDexClassLoader::class.java.getDeclaredField("pathList")
    dexPathListField.isAccessible = true
    val dexElementsField =
        Class.forName("dalvik.system.DexPathList").getDeclaredField("dexElements")
    dexElementsField.isAccessible = true

    val pluginDexClassLoader = DexClassLoader(
        pluginFile.absolutePath,
        context.filesDir.absolutePath,
        pluginFile.absolutePath,
        context.classLoader.parent
    )

    val pluginPathListField = pluginDexClassLoader.javaClass.getField("pathList")
    pluginPathListField.isAccessible = true
    val pluginElementsField = pluginDexClassLoader.javaClass.getField("dexElements")
    pluginElementsField.isAccessible = true
    val pluginPathList = pluginPathListField.get(pluginDexClassLoader)
    val pluginElements = pluginElementsField.get(pluginPathList) as Array<Any>

    val classLoader = context.classLoader as BaseDexClassLoader
    val pathList = dexPathListField.get(classLoader)
    val dexElements = dexElementsField.get(pathList) as Array<Any>


    val newList = mutableListOf<Any>()
    newList.addAll(dexElements)
    newList.addAll(pluginElements)

    // 更新宿主应用程序的 dexElements 属性
    dexElementsField.set(pathList, newList.toTypedArray())
}