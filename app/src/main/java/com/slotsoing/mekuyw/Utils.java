package com.slotsoing.mekuyw;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;

public class Utils {
    private static final String TAG = "Constants.TAG" + "utils";

    public static boolean extractAssets(Context context, String filePath, String pluginName) {
        AssetManager assetManager = context.getAssets();

        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {

            Log.i(TAG, "save apk file path is " + filePath);

            fileOutputStream = new FileOutputStream(filePath);
            //获取assets目录下的插件apk输入流
            inputStream = assetManager.open(pluginName);

            byte[] bytes = new byte[1024];
            int length = -1;
            //将apk文件复制到对应到文件目录下
            while ((length = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, length);
            }
            fileOutputStream.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "copy file failed " + e.getMessage());
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
                if (null != fileOutputStream) {
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                Log.i(TAG, "extractAssets: " + e.getMessage());
            }
        }
        return false;
    }

    public static void requestPermissions(Activity activity, String[] permissions) {
        try {
            PackageManager pm = activity.getPackageManager();
            String pkgName = activity.getPackageName();
            String[] needRequst = new String[permissions.length];

            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (PackageManager.PERMISSION_DENIED == pm.checkPermission(permission, pkgName)) {
                    needRequst[i] = permission;
                }
            }

            ActivityCompat.requestPermissions(activity, needRequst, 1);
        } catch (Exception e) {
            Log.i(TAG, "requestPermissions: " + e.getMessage());
        }
    }

    /**
     * @param context
     * @param pluginName 插件名
     */
    public static void copyApk(Context context, String pluginName) {
        DePluginSP sp = DePluginSP.getInstance(context);
        //获取插件apk保存路径
        String filePath = sp.getString(Constants.COPY_FILE_PATH, "");
        if (TextUtils.isEmpty(filePath)) {
            //如果插件apk保存路径为空，说明没有copy插件apk到对应目录成功
            File saveApkFile = context.getFileStreamPath(pluginName);
            if (null == saveApkFile) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    saveApkFile = context.getDataDir();
                    filePath = saveApkFile.getAbsolutePath() + pluginName;
                } else {
                    filePath = "data/user/0/" + context.getPackageName() + "/" + pluginName;
                }
            } else {
                filePath = saveApkFile.getAbsolutePath();
            }
            boolean result = extractAssets(context, filePath, pluginName);
            if (result) {
                sp.setString(Constants.COPY_FILE_PATH, filePath);
            }
            Log.i(TAG, "copy " + result);
        } else {
            //如果插件apk保存路径不为空，并且本地存在了apk则不在进行二次copy，否则可能已经被删除则重新复制一份到对应到目录下
            //当然在实际到开发中这里到情况会复杂的多，比如与服务器插件版本进行对比判断是否需要重新下载等
            File file = new File(filePath);
            if (file.exists()) {
                Log.i(TAG, "had copy apk before,so no need copy again");
            } else {
                Log.i(TAG, "althogh save apk file path success,but file not exists");
                extractAssets(context, filePath, pluginName);
            }
        }
    }

    public static ApplicationInfo generateApplicationInfo(String pluginPath) {
        try {
            ApplicationInfo applicationInfo = getApplicationInfoByPackageArchiveInfo(pluginPath);
            if (null == applicationInfo) {
                Log.i(TAG, "get applicationInfo failed");
                return null;
            }
            applicationInfo.sourceDir = pluginPath;
            applicationInfo.publicSourceDir = pluginPath;
            applicationInfo.uid = Process.myUid();
            return applicationInfo;
        } catch (Exception e) {
            Log.i(TAG, "generateApplicationzInfo failed " + e.getMessage());
        }
        return null;
    }

    private static ApplicationInfo getApplicationInfoByReflect(String pluginPath) throws Exception {
        Class<?> packageParserCls = RefInvoke.getClass("android.content.pm.PackageParser");
        Object packageParserObj = RefInvoke.createObject(packageParserCls, new Class[]{}, new Class[]{});

        File file = new File(pluginPath);
        Log.i(TAG, "plugin path is " + pluginPath);
        if (!file.exists()) {
            Log.i(TAG, "file non-exist");
            return null;
        }
        Object packageObj = RefInvoke.on(packageParserObj, "parseMonolithicPackage", File.class, int.class).invoke(file, 0);
        if (null == packageObj) {
            Log.i(TAG, "get PackageParse$Package obj failed");
            return null;
        }

        Class<?> packageUserStateCls = RefInvoke.getClass("android.content.pm.PackageUserState");
        ApplicationInfo applicationInfo = RefInvoke.on(packageParserObj, "generateApplicationInfo", packageObj.getClass(), int
                .class, packageUserStateCls).invoke(packageObj, 0, RefInvoke.createObject(packageUserStateCls, new Class[]{}, new Class[]{}));
        Log.i(TAG, pluginPath + " package name is " + applicationInfo.packageName);
        return applicationInfo;
    }

    private static ApplicationInfo getApplicationInfoByPackageArchiveInfo(String pluginPath) {
        PackageManager packageManager = MyApp.Companion.getMContext().getPackageManager();
        if (null == packageManager) {
            Log.i(TAG, "get PackageManager failed");
            return null;
        }
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(pluginPath, 0);
        if (null == packageInfo) {
            Log.i(TAG, "get packageInfo failed");
            return null;
        }
        return packageInfo.applicationInfo;
    }

    public static void mergeDex(ClassLoader classLoader, File apkFile, File optDexfile) {
        try {

            Object pathListObj = RefInvoke.getFieldValue(RefInvoke.getField(BaseDexClassLoader.class, "pathList"), classLoader);
            if (null == pathListObj) {
                Log.i(TAG, "get path list failed");
                return;
            }

            Field dexElementsField = RefInvoke.getField(pathListObj.getClass(), "dexElements");
            Object[] elements = (Object[]) RefInvoke.getFieldValue(dexElementsField, pathListObj);
            if (null == elements) {
                Log.i(TAG, "get elements failed");
                return;
            }

            int length = elements.length;
            Class<?> elementCls = elements.getClass().getComponentType();
            Object[] newElemets = (Object[]) Array.newInstance(elementCls, length + 1);


            Object elementObj = RefInvoke.createObject(elementCls, new Class[]{DexFile.class, File.class}, new Object[]{DexFile.loadDex(apkFile.getCanonicalPath(), optDexfile.getAbsolutePath(), 0), apkFile});
            newElemets[0] = elementObj;
            System.arraycopy(elements, 0, newElemets, 1, length);
            RefInvoke.setFieldValue(dexElementsField, pathListObj, newElemets);

            Log.i(TAG, "merge dex success");
        } catch (Exception e) {
            Log.e(TAG, "mergeDex failed " + e);
        }
    }

    public static void mapping() {
        HostToPluginMapping.putActivityMapping("StandardStubActivity.class.getName()", "org.cocos2dx.javascript.AppActivity");
    }
}
