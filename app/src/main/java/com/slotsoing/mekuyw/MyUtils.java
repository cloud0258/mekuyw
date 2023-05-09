package com.slotsoing.mekuyw;

import java.lang.reflect.Field;

public class MyUtils {

    private static Object getField(Object obj, Class<?> clazz, String field) {
        Field localField;
        try {
            localField = clazz.getDeclaredField(field);
            localField.setAccessible(true);
            return localField.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getPathList(Object baseDexClassLoader) throws Exception {
        return getField(baseDexClassLoader,Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getDexElements(Object obj) throws Exception {
        return getField(obj,obj.getClass(),"dexElements");
    }

//    private static void dexInject(final Context appContext, File filesDir, HashSet<File> loadedDex) {
//        String optimizeDir = filesDir.getAbsolutePath()+File.separator+"opt_dex";
//        File optimizedFile = new File(optimizeDir);
//        if(!optimizedFile.exists()){
//            boolean hasDir = optimizedFile.mkdirs();
//            if(!hasDir){
//                Log.e("sky", "make directory fail...");
//                return;
//            }
//        }
//        //1.加载应用程序的dex
//        try {
//            PathClassLoader pathLoader = (PathClassLoader) appContext.getClassLoader();
//
//            for (File dex : loadedDex) {
//                //2.加载指定的修复的dex文件。
//                DexClassLoader classLoader = new DexClassLoader(dex.getAbsolutePath(),
//                        optimizedFile.getAbsolutePath(), null, pathLoader);
//                Object dexObj = getPathList(classLoader);
//                Object pathObj = getPathList(pathLoader);
//                Object mDexElementsList = getDexElements(dexObj);
//                Object pathDexElementsList = getDexElements(pathObj);
//                //3.合并dex
//                Object dexElements = combineArray(mDexElementsList,pathDexElementsList);
//                //4.重新赋值给PathList里面的dexElements
//                Object pathList = getPathList(pathLoader);
//                setField(pathList,pathList.getClass(),"dexElements",dexElements);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
