package com.example.hujin.cropimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author hujin
 * @package com.example.hujin.cropimageview
 * @description: 工具类
 * @email xiaoqingxu0502@gamil.com
 * @since 2018/7/22 下午6:50
 */
public class FileUtil {
    public static boolean saveOutput(Bitmap croppedImage, File saveFile) {
        if(croppedImage == null) {
            return false;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(saveFile);
            if (fos != null) {
                croppedImage.compress(Bitmap.CompressFormat.JPEG, 75, fos);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            croppedImage.recycle();
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        croppedImage.recycle();
        return true;
    }

    public  static File createImageFile(Context context,String prefix,String suffix) {
        String root = cachePath(context, "crop$$image");
        File studentF = new File(root);
        if (!studentF.exists()) {
            studentF.mkdirs();
        }
        File file = new File(studentF, prefix + System.currentTimeMillis() + suffix);
        return file;
    }

    private static String cachePath(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        if (TextUtils.isEmpty(cachePath)) {
            return null;
        }
        return cachePath + File.separator + uniqueName;
    }
}
