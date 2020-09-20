package com.production.mylibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
import com.production.mylibrary.CustomModel.PostImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class mylibrary extends AppCompatActivity {
    ImageView imageView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALARY=2;
    static final int REQUEST_PERMISSION_ALL=0;
    SweetAlertDialog pDialog;

    public static final String APP_TAG = "MyCustomApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public static String photoFileName = "photo.jpg";
    static File photoFile;
    static Uri fileProvider;
    static String autheries="";
    public static void setAuher(String auther)
    {
        autheries=auther;
    }
    public static void setUri(Context context)
    {
        photoFile = getPhotoFileUri(context,photoFileName);
        fileProvider = FileProvider.getUriForFile(context, autheries, photoFile);
    }
    FloatingActionButton btn_camera, btn_pick;
    Button btn_result;
    String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET};
    static final String domain ="https://shrouded-brushlands-68077.herokuapp.com/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylibrary);
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        checkPermission();
        imageView= findViewById(R.id.img_face);
        btn_camera= findViewById(R.id.btn_camera);
        btn_pick= findViewById(R.id.btn_updfile);
        btn_result=findViewById(R.id.btn_result);

        pDialog= new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);

        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                bitmap= Bitmap.createScaledBitmap(bitmap, 640, 640, false);
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] image=stream.toByteArray();

                String img_str = Base64.encodeToString(image,Base64.NO_WRAP);
                String email="viethn.it@gmail.com";
                postImage(email,img_str);


            }
        });
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        btn_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPickPictureIntent();
            }
        });
    }

    private void postImage(String email, String img_str) {
        pDialog.show();
        String apiKey="NWY0N2FkMjg4ZjFiYmIwYWViZDBkNDdhXzU2Nzg5MTBfSG5mMlJRcDhMbkNuWWhBQw==";
        Log.d("IMAGE STRING",img_str);
        PostMethod(domain,apiKey,email,img_str);


    }

    // Check Permission
    private boolean checkPermission( )
    {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_PERMISSION_ALL);
            return false;
        }
        return true;
    }
    // Request Permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_IMAGE_CAPTURE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    dispatchTakePictureIntent();
                }
                break;
            case REQUEST_PERMISSION_ALL:
                if(grantResults.length>0)
                {
                    for(int i=0;i<permissions.length;i++)
                    {
                        if(grantResults[i]==PackageManager.PERMISSION_DENIED)
                        {
                            Toast.makeText(getApplicationContext(),"Not Permission",Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
        }
    }


    // Take Photo
    private void dispatchTakePictureIntent() {


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                fileProvider);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    // Pick Photo
    private void dispatchPickPictureIntent()
    {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPictureIntent,REQUEST_IMAGE_GALARY);
    }
    // Result from intent
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode)
            {
                case 1:
                    pDialog.show();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileProvider);
                        Bitmap rotatedBitmap = rotateImageIfRequired(getApplicationContext(),bitmap,fileProvider);
                        this.imageView.setImageDrawable(new BitmapDrawable(getResources(),rotatedBitmap));

                    } catch (IOException e) {
                        Log.d("Image:",e.toString());
                        e.printStackTrace();
                    }
                    pDialog.hide();
                    break;
                case 2:
                    pDialog.show();
                    if (resultCode == RESULT_OK && data != null) {
                        // Get selected gallery image
                        Uri selectedPicture = data.getData();
                        // Get and resize profile image
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getApplication().getContentResolver().query(selectedPicture, filePathColumn, null, null, null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();

                        Bitmap loadedBitmap = BitmapFactory.decodeFile(picturePath);

                        ExifInterface exif = null;
                        try {
                            File pictureFile = new File(picturePath);
                            exif = new ExifInterface(pictureFile.getAbsolutePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        int orientation = ExifInterface.ORIENTATION_NORMAL;

                        if (exif != null)
                            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                loadedBitmap = rotateBitmap(loadedBitmap, 90);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                loadedBitmap = rotateBitmap(loadedBitmap, 180);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_270:
                                loadedBitmap = rotateBitmap(loadedBitmap, 270);
                                break;
                        }
                        imageView.setImageDrawable(new BitmapDrawable(getResources(),loadedBitmap));
                        pDialog.hide();
                    }
                    break;
            }

        }
    }
    // Post method Api
    public void PostMethod(String realDomain,String apiKey,String email, String img )
    {
        if(realDomain==null)
        {
            realDomain=domain;
        }
        RetrofitInstance service = ApiUtils.getInstance(realDomain);
        PostImage user =new PostImage(email,img);
        service.createUser(apiKey,user).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful())
                {
                    try {
                        JsonObject object = response.body();
                        object = object.getAsJsonObject("data");
                        String id = String.valueOf(object.get("_id"));
                        //Intent intent = new Intent(getApplication(),ResultActivity.class);
                        //intent.putExtra("idImage",id);
                        //startActivity(intent);
                        Toast.makeText(getApplicationContext(),id,Toast.LENGTH_SHORT).show();

                    }
                    catch (Exception ex)
                    {
                        pDialog.hide();
                        Log.d("AAAAA",ex.toString());
                        Toast.makeText(getApplicationContext(),"Failure. Try Again".toString(),Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Log.d("AAAAA",response.toString());
                    Toast.makeText(getApplicationContext(),"Failure. Try Again".toString(),Toast.LENGTH_SHORT).show();

                }
                pDialog.hide();

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Failure. Try Again",Toast.LENGTH_SHORT).show();
                pDialog.hide();
            }

        });
    }
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    public static File getPhotoFileUri(Context context,String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateBitmap(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateBitmap(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateBitmap(img, 270);
            default:
                return img;
        }
    }
}
