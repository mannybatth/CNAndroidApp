package com.thecn.app.stores;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import com.thecn.app.AppSession;
import com.thecn.app.tools.BitmapUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class ImageStore {
    private static final String TAG = ImageStore.class.getSimpleName();

    public static interface ImageCallback {
        public void response(JSONObject json);

        public void error(Exception e);
    }

    public static void uploadImage(String filePath, ImageCallback callback) {
        HttpClient httpclient = new DefaultHttpClient();

        String query = BaseStore.BASE_URL + "/attachment_picture/?TOKEN=" + AppSession.getInstance().getToken();
        HttpPost httppost = new HttpPost(query);

        try {
            Bitmap bm = BitmapUtil.decodeSampledBitmapFromFilePath(filePath, 1024, 768);

            ExifInterface ei = new ExifInterface(filePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bm = rotateImage(bm, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bm = rotateImage(bm, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bm = rotateImage(bm, 270);
                    break;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            bm.recycle();

            byte[] data = baos.toByteArray();
            baos.close();
            ByteArrayBody bab = new ByteArrayBody(data, "image.jpeg");

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

            //FileBody fileBody = new FileBody(new File(filePath));
            entityBuilder.addPart("upload_file", bab);
           // entityBuilder.addPart("upload_file", fileBody);
            httppost.setEntity(entityBuilder.build());

            Log.d(TAG, "API CALL => " + query);
            HttpResponse httpResponse = httpclient.execute(httppost);
            JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
            Log.d(TAG, "RESPONSE: " + response.toString());

            callback.response(response);

        } catch (ClientProtocolException e) {
            callback.error(e);
        } catch (IOException e) {
            callback.error(e);
        } catch (JSONException e) {
            callback.error(e);
        }
    }

    private static Bitmap rotateImage(Bitmap sourceBitmap, float rotation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
    }
}
