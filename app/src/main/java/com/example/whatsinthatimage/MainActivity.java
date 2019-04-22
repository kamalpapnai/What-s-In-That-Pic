package com.example.whatsinthatimage;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.DIRECTORY_PICTURES;

public class MainActivity extends Activity {

    public static final String TAG_NAME = MainActivity.class.getSimpleName();
    static final int REQUEST_IMAGE_CAPTURE=1;
    static final String GET_IMAGE="getimage";
    String currentPhotoPath;
    private ImageView imageView;
    private Uri getPhotoURI;
    private Bitmap bitmapImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.imageView);

        dispatchTakePictureIntent();

    }

    private void sendBitmap(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmapImage.compress(Bitmap.CompressFormat.JPEG,100,stream);
                byte[] byteArray = stream.toByteArray();
                Log.d(TAG_NAME,"mainintent");

                Intent goToLabel = new Intent(MainActivity.this,LabelActivity.class);
                goToLabel.putExtra(GET_IMAGE,byteArray);
                startActivity(goToLabel);
            }
        }).start();
    }




    private void dispatchTakePictureIntent()
    {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //checking if any other app can handle this take picture intent
        if(takePicture.resolveActivity(getPackageManager())!=null)
        {
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }
            catch(IOException ex){
                Log.d(TAG_NAME,ex.toString());
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);

                getPhotoURI = photoURI;
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


            if (resultCode == RESULT_OK && requestCode==REQUEST_IMAGE_CAPTURE)
            {
                Log.d(TAG_NAME,"Got Data");
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),getPhotoURI);
                    if(bitmap!=null){
                        Log.d(TAG_NAME,"Got Bitmap");
                        setPic();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG_NAME,"Image not found from uri");
                }
            }

//            Bundle extras = null;
//            if (data != null) {
//                Log.d("key","Got Data");
//                extras = data.getExtras();
//                Bitmap image = (Bitmap) extras.get("data");
//                imageView.setImageBitmap(image);
//            }
//            Log.d("key","Got No data");
//
//
////            galleryAddPic();
//        }
    }

    private File createImageFile()throws IOException
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
//        int g = photoW/targetW;
//        int h = photoH/targetH;
//
//        int scaleFactor = Math.min(g,h);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize =4;
        bmOptions.inPurgeable = true;

        bitmapImage = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmapImage);

        sendBitmap();
    }


}
