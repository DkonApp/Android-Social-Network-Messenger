package id.dkon.app.util;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import id.dkon.app.R;
import id.dkon.app.app.App;

public class Helper extends Application {

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    Context ctx;

    public Helper() {

    }

    public Helper(Context ctx) {

        this.ctx = ctx;
    }

    private Bitmap resizeImg(Uri filename) throws IOException {

        int maxWidth = 1200;
        int maxHeight = 1200;

        // create the options
        BitmapFactory.Options opts = new BitmapFactory.Options();

        BitmapFactory.decodeFile(getRealPath(filename), opts);

        //get the original size
        int orignalHeight = opts.outHeight;
        int orignalWidth = opts.outWidth;

        //opts = new BitmapFactory.Options();

        //just decode the file
        opts.inJustDecodeBounds = true;

        //initialization of the scale
        int resizeScale = 1;

        Log.e("qascript orignalWidth", Integer.toString(orignalWidth));
        Log.e("qascript orignalHeight", Integer.toString(orignalHeight));

        //get the good scale
        if (orignalWidth > maxWidth || orignalHeight > maxHeight) {

            resizeScale = 2;
        }

        //put the scale instruction (1 -> scale to (1/1); 8-> scale to 1/8)
        opts.inSampleSize = resizeScale;
        opts.inJustDecodeBounds = false;

        //get the future size of the bitmap
        int bmSize = 6000;

        //check if it's possible to store into the vm java the picture
        if (Runtime.getRuntime().freeMemory() > bmSize) {

            //decode the file

            InputStream is = this.ctx.getContentResolver().openInputStream(filename);
            Bitmap bp = BitmapFactory.decodeStream(is, new Rect(0, 0, 512, 512), opts);
            is.close();

            return bp;

        } else {

            Log.e("qascript", "not resize image");

            return null;
        }
    }

    public void saveImg(Uri filename, String newFilename) {

        String mimeType = "image/jpeg";
        String directory = Environment.DIRECTORY_PICTURES;
        Uri mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        try {

            Bitmap bmp = this.resizeImg(filename);

            if (bmp == null) {

                try {

                    bmp = MediaStore.Images.Media.getBitmap(App.getInstance().getApplicationContext().getContentResolver(), filename);

                }  catch (Exception e) {

                    //handle exception

                    Log.e("qascript", "MediaStore error");
                }
            }

            int orientation = 1;

            OutputStream imageOutStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, newFilename);
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, directory);

                ContentResolver contentResolver = this.ctx.getContentResolver();

                imageOutStream = contentResolver.openOutputStream(contentResolver.insert(mediaContentUri, values));

                try (InputStream inputStream = ctx.getContentResolver().openInputStream(filename)) {

                    ExifInterface exif = new ExifInterface(inputStream);

                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                } catch (IOException e) {

                    e.printStackTrace();
                }

            } else {

                // File file = new File(Environment.DIRECTORY_PICTURES, inFile);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newFilename);
                imageOutStream = new FileOutputStream(file);

                ExifInterface exif = new ExifInterface(getRealPath(filename));
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            }

            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:

                    bmp = rotateImage(bmp, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:

                    bmp = rotateImage(bmp, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:

                    bmp = rotateImage(bmp, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:

                default:

                    bmp = bmp;
            }

            bmp.compress(Bitmap.CompressFormat.JPEG, 90, imageOutStream);
            imageOutStream.flush();
            imageOutStream.close();

        } catch (Exception ex) {

            Log.e("qascript saveImg()", ex.getMessage());
        }
    }

    public void saveBmp(Bitmap bmp, String newFilename) {

        String mimeType = "image/jpeg";
        String directory = Environment.DIRECTORY_PICTURES;
        Uri mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        try {

            OutputStream imageOutStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, newFilename);
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, directory);

                ContentResolver contentResolver = this.ctx.getContentResolver();

                imageOutStream = contentResolver.openOutputStream(contentResolver.insert(mediaContentUri, values));

            } else {

                // File file = new File(Environment.DIRECTORY_PICTURES, inFile);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newFilename);
                imageOutStream = new FileOutputStream(file);
            }

            bmp.compress(Bitmap.CompressFormat.JPEG, 90, imageOutStream);
            imageOutStream.flush();
            imageOutStream.close();

        } catch (Exception ex) {

            Log.e("qascript saveBmp()", ex.getMessage());

        } finally {

            Log.e("qascript saveBmp()", "success");
        }
    }

    public static String getRealPath(Uri uri) {

        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        Uri contentUri;

        switch (type) {

            case "image":

                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;

            case "video":

                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;

            case "audio":

                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;

            default:

                contentUri = MediaStore.Files.getContentUri("external");
        }

        String selection = "_id=?";
        String[] selectionArgs = new String[]{split[1]};

        return getDataColumn(App.getInstance().getApplicationContext(), contentUri, selection, selectionArgs);
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        String column = "_data";
        String[] projection = {column};

        try {

            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {

                int column_index = cursor.getColumnIndexOrThrow(column);
                String value = cursor.getString(column_index);

                if (value.startsWith("content://") || !value.startsWith("/") && !value.startsWith("file://")) {

                    return null;
                }

                return value;
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (cursor != null) {

                cursor.close();
            }
        }

        return null;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {

        Matrix matrix = new Matrix();

        matrix.postRotate(angle);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static void deleteFile(final Context context, final File file) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {

            final String where = MediaStore.MediaColumns.DATA + "=?";

            final String[] selectionArgs = new String[] {
                    file.getAbsolutePath()
            };

            final ContentResolver contentResolver = context.getContentResolver();
            final Uri filesUri = MediaStore.Files.getContentUri("external");

            contentResolver.delete(filesUri, where, selectionArgs);

            if (file.exists()) {

                contentResolver.delete(filesUri, where, selectionArgs);
            }

        } else {

            if (file.exists()) {

                file.delete();
            }
        }
    }

    public static String md5(final String s) {

        final String MD5 = "MD5";

        try {

            // Create MD5 Hash

            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());

            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();

            for (byte aMessageDigest : messageDigest) {

                String h = Integer.toHexString(0xFF & aMessageDigest);

                while (h.length() < 2) h = "0" + h;

                hexString.append(h);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }

        return "";
    }

    public static String getGenderTitle(Context ctx, int gender) {

        switch (gender) {

            case 1: {

                return ctx.getString(R.string.label_male);
            }

            case 2: {

                return ctx.getString(R.string.label_female);
            }

            default: {

                return ctx.getString(R.string.label_unknown);
            }
        }
    }


    public String getFormatedAmount(int amount){

        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    public static int getGridSpanCount(Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth  = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.item_size);
        return Math.round(screenWidth / cellWidth);
    }

    public static int getStickersGridSpanCount(Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth  = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.sticker_item_size);
        return Math.round(screenWidth / cellWidth);
    }

    public boolean isValidEmail(String email) {

    	if (TextUtils.isEmpty(email)) {

    		return false;

    	} else {

    		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    	}
    }
    
    public boolean isValidLogin(String login) {

        String regExpn = "^([a-zA-Z]{4,24})?([a-zA-Z][a-zA-Z0-9_]{4,24})$";
        CharSequence inputStr = login;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {

            return true;

        } else {

            return false;
        }
    }

    public boolean isValidSearchQuery(String query) {

        String regExpn = "^([a-zA-Z]{1,24})?([a-zA-Z][a-zA-Z0-9_]{1,24})$";
        CharSequence inputStr = query;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {

            return true;

        } else {

            return false;
        }
    }
    
    public boolean isValidPassword(String password) {

        String regExpn = "^[a-z0-9_$@$!%*?&]{6,24}$";
        CharSequence inputStr = password;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {

            return true;

        } else {

            return false;
        }
    }

    public static String randomString(int len) {

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));

        return sb.toString();
    }
}
