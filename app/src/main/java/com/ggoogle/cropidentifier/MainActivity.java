package com.ggoogle.cropidentifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.CAMERA;
import static android.provider.Telephony.Mms.Part.FILENAME;


public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_LOCATION_PERMISSION_CODE = 1;
    LocationManager locationManager;
    LocationListener locationListener;
    TextView LocationDetails, plantDetails;
    String add;
    String cropDetails;
    StringBuffer stringBuffer;
    FirebaseCustomRemoteModel remoteModel;
    FirebaseCustomLocalModel localModel;
    FirebaseModelInterpreter interpreter;
    FirebaseModelInterpreterOptions options;
    SimpleDateFormat adf = new SimpleDateFormat("yyMMdd_HHmmss");
    String time = adf.format(new Date());
    String userChoosenTask;
    int REQUEST_CAMERA = 0;
    int SELECT_FILE = 1;
   // ImageView imageView;
    Button select;
    //rough work/////////////////////////////////////////////////////////////////////////////////////////
//    private static ViewPager mPager;
//    private static int currentPage = 0;
//    private static int NUM_PAGES = 0;
//    private ArrayList<ImageModel> imageModelArrayList;
//    View retView;
//
//    private int[] myImageList = new int[]{R.drawable.iron, R.drawable.ironman,
//            R.drawable.ironman2,R.drawable.prey
//            ,R.drawable.tiger2,R.drawable.wall2};
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    Button dataCrop;

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            add = "\nCrop Location : "+obj.getAddressLine(0) ;
            LocationDetails.setText(add);
            writeToInternalStorage();
            readcropData();
//            add = add + "\n" + obj.getCountryName();
//            add = add + "\n" + obj.getCountryCode();
//            add = add + "\n" + obj.getAdminArea();
//            add = add + "\n" + obj.getPostalCode();
//            add = add + "\n" + obj.getSubAdminArea();
//            add = add + "\n" + obj.getLocality();
//            add = add + "\n" + obj.getSubThoroughfare();

           // Log.v("IGA", "Address" + add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void replaceFragment(Fragment destFragment)
    {
        final int CONTENT_VIEW_ID = 10101010;
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CONTENT_VIEW_ID);

        // First get FragmentManager object.
        FragmentManager fragmentManager = this.getFragmentManager();

        // Begin Fragment transaction.
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the layout holder with the required Fragment object.
        fragmentTransaction.replace(R.id.dynamic_fragment_frame_layout, destFragment);

        // Commit the Fragment replace action.
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationDetails = (TextView) findViewById(R.id.locationDetails);
        plantDetails = (TextView) findViewById(R.id.plantDetails);
        this.imageView = (ImageView)this.findViewById(R.id.imageView);
        dataCrop = (Button) findViewById(R.id.CropData);

        dataCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment CropDataFragment = new cropDataFragment();
                replaceFragment(CropDataFragment);
            }
        });


//        FirebaseModelDownloadConditions.Builder conditionsBuilder =
//                new FirebaseModelDownloadConditions.Builder().requireWifi();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            // Enable advanced conditions on Android Nougat and newer.
//            conditionsBuilder = conditionsBuilder
//                    .requireCharging()
//                    .requireDeviceIdle();
//        }
//        FirebaseModelDownloadConditions conditions = conditionsBuilder.build();
//
//// Build a remote model source object by specifying the name you assigned the model
//// when you uploaded it in the Firebase console.
//        FirebaseRemoteModel cloudSource = new FirebaseRemoteModel.Builder("Crop_identification")
//                .enableModelUpdates(true)
//                .setInitialDownloadConditions(conditions)
//                .setUpdatesDownloadConditions(conditions)
//                .build();
//        FirebaseModelManager.getInstance().registerRemoteModel(cloudSource);

        //model work

      //  tensorflowFileWork();

        //app work
        readcropData();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Button button = (Button) this.findViewById(R.id.export);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(stringBuffer.toString());
            }
        });




        Button photoButton = (Button) this.findViewById(R.id.imageView2);
        photoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
//                {
//                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
//                }
//                else
//                {
//                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
//                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
//                }
                selectImage();


                locationListener = new LocationListener()
                {

                    @Override
                    public void onLocationChanged(Location location) {
                       //   Toast.makeText(getApplicationContext(),"Location: "+ location.getLatitude() + "longitude:   "+ location.getLongitude(),Toast.LENGTH_SHORT).show();
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        getAddress(lat, lon);
                        locationManager.removeUpdates(locationListener);
                        //locationManager = null;
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                };
                    if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_PERMISSION_CODE);
                    }
                    else {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    }
            }
        });
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //camera permission check
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            grantResults[0] = PackageManager.PERMISSION_GRANTED;
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }

        //location permission check
        if( requestCode == MY_LOCATION_PERMISSION_CODE)
        {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    }
                }
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
//        {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(photo);
//            String path = saveToInternalStorage(photo);
//           // loadImageFromStorage(path);
//           // loadInternalImages();
//            tensorflowFileWork(photo);
//            Toast.makeText(getApplicationContext(), path,Toast.LENGTH_SHORT).show();
//        }
//    }

    private void writeToInternalStorage() {
        String textFile_Name = "crop_details";
        try {
            FileOutputStream fileOutputStream = openFileOutput(textFile_Name, MODE_APPEND);
            add = add + cropDetails;
            fileOutputStream.write(add.getBytes());
            fileOutputStream.close();
         //   Toast.makeText(getApplicationContext(),"Message Saved", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readcropData() {
        String Details;
        try {
            FileInputStream fileInputStream = openFileInput("crop_details");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            stringBuffer = new StringBuffer();
            while((Details=bufferedReader.readLine()) != null)
            {
               stringBuffer.append("\n"+Details + "\n");
            }

          //  plantDetails.setText(stringBuffer.toString());                                            //to do here
            //send mail
           // sendEmail(stringBuffer.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    protected void sendEmail( String s) {
        Log.i("Send email", "crop_detail");

        String[] TO = {"shreyasrivastava936@gmail.com"};
        String[] CC = {"shreyasrivastava936@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        File imageFile = getFileStreamPath("crop_detail");
        Uri imageUri = Uri.fromFile(imageFile);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, s);
       // emailIntent.putExtra(android.content.Intent.EXTRA_STREAM,imageUri);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_APPEND);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg"+time);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path)
    {

        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
           // ImageView img=(ImageView)findViewById(R.id.imgPicker);
            imageView.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    private File [] loadInternalImages(){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File[] imageList = directory.listFiles();
        if(imageList == null){
            imageList = new File[0];
        }
        Log.i("My","ImageList Size = "+imageList.length);
        return imageList;
    }

    private void tensorflowFileWork(final Bitmap photo) {
        remoteModel =
                new FirebaseCustomRemoteModel.Builder("Crop_identification").build();
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Success.
                    }
                });//to do

        localModel = new FirebaseCustomLocalModel.Builder()
                .setAssetFilePath("mobile_model.tflite")
                .build();



        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isDownloaded) {
//                        FirebaseModelInterpreterOptions options;
                        if (isDownloaded) {
                            options = new FirebaseModelInterpreterOptions.Builder(remoteModel).build();
                        } else {
                            options = new FirebaseModelInterpreterOptions.Builder(localModel).build();
                        }
                      //  Toast.makeText(getApplicationContext(), "errordgfgv     \n"+ options, Toast.LENGTH_LONG).show();
//                        try {
//                             interpreter = FirebaseModelInterpreter.getInstance(options);
//                        } catch (FirebaseMLException e) {
//                            e.printStackTrace();
//                        }
                        // ...

       // Toast.makeText(getApplicationContext(), "errordgfgv     \n"+ options, Toast.LENGTH_LONG).show();
                        if(options != null) {
                            try {
                                interpreter = FirebaseModelInterpreter.getInstance(options);
                //                Toast.makeText(getApplicationContext(), interpreter+"    hello\n", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                            }
                        }


      //  Toast.makeText(getApplicationContext(), interpreter+"    hello\n", Toast.LENGTH_LONG).show();
        Bitmap bitmap = photo;
        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        int batchNum = 0;
        float[][][][] input = new float[1][224][224][3];
        for (int x = 0; x < 224; x++) {
            for (int y = 0; y < 224; y++) {
                int pixel = bitmap.getPixel(x, y);
                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [0.0, 1.0] instead.
//                input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
//                input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
//                input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;

                input[batchNum][x][y][0] = (Color.red(pixel) ) / 255.0f;
                input[batchNum][x][y][1] = (Color.green(pixel)) / 255.0f;
                input[batchNum][x][y][2] = (Color.blue(pixel)) / 255.0f;
            }
        }

        FirebaseModelInputs inputs = null;
        try {
            inputs = new FirebaseModelInputs.Builder()
                    .add(input)  // add() as many input arrays as your model requires
                    .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

        try {
            FirebaseModelInputOutputOptions inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 224, 224, 3})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 10})
                            .build();

            if (interpreter != null) {
                interpreter.run(inputs, inputOutputOptions)
                        .addOnSuccessListener(
                                new OnSuccessListener<FirebaseModelOutputs>() {
                                    @Override
                                    public void onSuccess(FirebaseModelOutputs result) {
                                        // ...
                                        float[][] output = result.getOutput(0);
                                        int row = output.length;
                                        int column = output[0].length;
                                        String b = "";
                                        for(int j = 0;j<row;j++)
                                        {
                                            for(int k = 0;k<column;k++)
                                            {
                                                b = b+ "  "+ output[j][k];
                                            }
                                        }
                                        float[] probabilities = output[0];
                                        int max = 0;
                                        for(int i = 0;i<probabilities.length;i++)
                                        {
                                            if(probabilities[i] > probabilities[max])
                                            {
                                                max = i;
                                            }
                                        }
                                       // plantDetails.setText(b+"\n "+ a);

                                        String[] cropName = {"carrot", "coffee", "corn", "cotton", "mint", "rice", "sugarcane", "tobbaco", "tomato", "wheat"};
                                        plantDetails.setText(cropName[max]+" with "+ (probabilities[max]*100)+ "% accuracy \n");
                                        LocationDetails.setText("loading...");
                                        cropDetails = "\nCropName : "+ cropName[max];
                                        //  Toast.makeText(getApplicationContext(), "success"+ output, Toast.LENGTH_LONG).show();
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
            }
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
                    }
                });


    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                // boolean result=Utility.checkPermission(MainActivity.this);
                if (items[item].equals("Take Photo")) {
                    userChoosenTask="Take Photo";
                    //  if(result)
                    cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask="Choose from Library";
                    // if(result)
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        imageView.setImageBitmap(bm);
        LocationDetails.setText("loading...");
        tensorflowFileWork(bm);
    }
    //copytextpop-up

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        imageView.setImageBitmap(thumbnail);
        tensorflowFileWork(thumbnail);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        imageView.setImageBitmap(thumbnail);
    }
}



