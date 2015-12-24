package com.jecelyin.android.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class ImageUtils {

    public static Intent getImagePickerIntent() {
        Intent intent = new Intent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }

        // set MIME type for image
        intent.setType("image/*");
        return intent;
    }

    public static String getImageFileFromPickerResult(Context context, Intent intent) {
        String imageFile = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Uri selectedImage = intent.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageFile = cursor.getString(columnIndex);
                }

                cursor.close();
            }

        } else {
            String id = DocumentsContract.getDocumentId(intent.getData());
            String selection = "_id=?";
            String[] selectionArgs = new String[]{id.split(":")[1]};

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns.DATA}, selection, selectionArgs, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    imageFile = cursor.getString(0);
                }
                cursor.close();
            }

        }
        return imageFile;
    }

    public static Intent getImageCaptureIntent(Uri outputUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return intent;
    }

    public static Intent getImageCropIntent(File filePath, File outputUri) {
        String output = outputUri.getAbsolutePath();
        if((!output.startsWith("/sdcard") && !output.startsWith("/storage")) || output.contains("/Android/data/")) {
            L.e("*************************************\n可能无法访问"+output+"\n*****************************************");
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        //indicate image type and Uri
        intent.setDataAndType(Uri.fromFile(filePath), "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputUri));
        //set crop properties
        intent.putExtra("crop", "true");
        //indicate aspect of desired crop
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //indicate output X and Y
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("scale", true);
        //retrieve data on return
//        intent.putExtra("return-data", false);
        return intent;
    }

    /**
     * 缩放图片并处理某些机器上拍照图片旋转了90度的问题
     */
    public static boolean compressImage(String filePath, float maxWidth, float maxHeight, Bitmap.CompressFormat format) {

        Bitmap scaledBitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inTempStorage = new byte[16 * 1024];

        bmp = BitmapFactory.decodeFile(filePath, options);

        scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            L.e(e);
            return false;
        }

        FileOutputStream out;
        try {
            out = new FileOutputStream(filePath);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(format, 80, out);
            out.close();
        } catch (Exception e) {
            L.e(e);
            return false;
        }

        return true;

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    /**
     * 注意不能使用私有目录，不然剪切App可能访问不了
     * @return
     */
    public static File getTempFile(Context context) {
        File path = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
        File file;
        try {
            if (!path.isDirectory() && !path.mkdirs())
                return null;
            file = File.createTempFile(
                    "tk_image_temp",  /* prefix */
                    ".jpg",         /* suffix */
                    path      /* directory */
            );
            return file;
        } catch (Exception e) {
            L.e(e);
            return null;
        }
    }

    /**
     * {@link Activity#onDestroy()} 最好在Activity销毁时调用，避免有漏网之鱼
     *
     * @param context
     */
    public static void cleanTempFiles(Context context) {
        final File path = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
        if(!path.isDirectory())
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files = path.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith("tk_image_temp") && filename.endsWith(".jpg");
                    }
                });
                if(files != null && files.length > 0) {
                    for(File file : files)
                        file.delete();
                }
            }
        }).start();

    }


    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
                return true;
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
            return false;
        }

        return false;
    }
}
