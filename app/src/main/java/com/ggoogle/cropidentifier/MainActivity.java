package com.ggoogle.cropidentifier;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.*;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.DebugUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.*;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG";
    private Uri imgUri;
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_LOCATION_PERMISSION_CODE = 1;
    public static final String IMAGE_DIRECTORY_NAME = "image_from_app";
    public static final String SELECT_GALLERY_IMAGE = "select image from gallery";
    public static final String TAKE_PHOTO_USING_CAMERA = "take photo using camera";
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 1;
    private static final int SELECT_FILE = 0;
    private String district = "District Not Found";

    //    private static Context mContext;
//
//    public static Context getContext() {
//        return mContext;
//    }
    LocationManager locationManager;
    TextView LocationDetails, plantDetails;
    String cropDetails;
    StringBuffer stringBuffer;
    FirebaseCustomRemoteModel remoteModel;
    FirebaseCustomLocalModel localModel;
    FirebaseModelInterpreter interpreter;
    FirebaseModelInterpreterOptions options;
    FusedLocationProviderClient locationClient;
    String userId;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();


        // below line is used to get reference for our database.
//        databaseReference = firebaseDatabase.getReference("CropsInfo");


        LocationDetails = (TextView) findViewById(R.id.locationDetails);
        plantDetails = (TextView) findViewById(R.id.plantDetails);
        this.imageView = (ImageView) this.findViewById(R.id.imageView);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationClient = getFusedLocationProviderClient(MainActivity.this);
        locationUpdate();

        Button exportDataToFirebase = (Button) this.findViewById(R.id.export);
        exportDataToFirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(stringBuffer.toString());         //wrong method
            }
        });

        Button pictureClickButton = (Button) this.findViewById(R.id.imageView2);
        pictureClickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                district = getLastLocation2();
                 district = find_Location();
                Log.i(TAG, "onClick: district " + district);

                String userAction = getUserActionForCamera();
                Log.i("ONClickAction", "onClick: userAction" + userAction);
//                getUserLocation();
//                getLastLocation();


            }
        });
    }

    public String find_Location() {
        Log.d("Find Location", "in find_location");
        String location_context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) MainActivity.this.getSystemService(location_context);
        List<String> providers = locationManager.getProviders(true);
        Log.i(TAG, "find_Location: "+providers);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.i(TAG, "getLastLocation2: permission not granted");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            locationManager.requestLocationUpdates(provider, 1000, 0,
                    new LocationListener() {

                        public void onLocationChanged(Location location) {
                            Log.i(TAG, "onLocationChanged: "+location);
                        }

                        public void onProviderDisabled(String provider) {
                            Log.i(TAG, "onProviderDisabled: "+provider);
                        }

                        public void onProviderEnabled(String provider) {
                            Log.i(TAG, "onProviderEnabled: "+provider);
                        }

                        public void onStatusChanged(String provider, int status,
                                                    Bundle extras) {
                            Log.i(TAG, "onStatusChanged: "+provider);
                        }
                    });
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.i(TAG, "onClickLocation: "+location+" "+latitude+" "+longitude);
                district = getAddress(latitude, longitude);

                Log.i(TAG, "getLastLocation2: "+district);
                if(district == null) {
                    district = "address not found";
                }
                return district;
            }
        }
        return null;
    }

    private String getLastLocation2() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i(TAG, "getLastLocation2: permission not granted");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        LocationServices.getFusedLocationProviderClient(MainActivity.this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location == null) {
                    Toast.makeText(MainActivity.this, "null location", Toast.LENGTH_SHORT).show();
                    return;
                }
                //TODO: UI updates.
//                Toast.makeText(getApplicationContext(),"Hello Javatpoint"+location,Toast.LENGTH_SHORT).show();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.i(TAG, "onClickLocation: "+location+" "+latitude+" "+longitude);
                district = getAddress(latitude, longitude);
            }
        });

        Log.i(TAG, "getLastLocation2: "+district);
        if(district == null) {
            district = "address not found";
        }
        return district;
    }

    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            Log.e(TAG, "getAddress: "+e.getMessage());
            return "address not found";
        }

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String district = addresses.get(0).getSubAdminArea();
        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
        Log.i(TAG, "getAddress: "+address+" "+" ::"+city+" ::"+country+" ::"+postalCode+" ::"+knownName+" :: "+district);
        return district;
    }

    private void locationUpdate() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        //TODO: UI updates.
//                        Toast.makeText(getApplicationContext(),"LocationUpdate oncreate"+location,Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onCreateLocation: "+location);
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    private void performUserActionForCamera(String userAction) {
        switch (userAction) {
            case TAKE_PHOTO_USING_CAMERA:
                captureImageUsingCamera();
                break;
            case SELECT_GALLERY_IMAGE:
                chooseFromGallery();
                break;
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
                                                        LocationDetails.setText(district);
                                                        saveDataTofirebase(cropName[max], district);
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

    //sdk version >= 26
    private void saveDataTofirebase(String cropName, String district) {
        Log.i(TAG, "saveDataTofirebase: started "+cropName+" "+district);

//        databaseReference = firebaseDatabase.getReference("CropsInfo/"+district);

        String month = LocalDate.now().getMonth().toString();
        Bundle extras= getIntent().getExtras();
        if(extras == null)
            return;
        String userId = extras.getString("id", null);
        if(userId == null)
            return;

//        CropDetails cropDetails = new CropDetails();
//        MonthWiseDetails monthWiseDetails = new MonthWiseDetails();
//        monthWiseDetails.setMonth(month);
//        monthWiseDetails.setCropName(cropName);
//        cropDetails.setDistrict(district);
//        cropDetails.setUserId(userId);
//        cropDetails.setMonthWiseDetails(monthWiseDetails);

        databaseReference =
//                firebaseDatabase.getReference("CropsInfo/"+district+"/"+userId+"/Month/"+month);
                firebaseDatabase.getReference("CropsInfo/"+district+"/"+month+"/"+userId);

        Log.i(TAG, "saveDataTofirebase2: ");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // inside the method of on Data change we are setting
                // our object class to our database reference.
                // data base reference will sends data to firebase.
                databaseReference.setValue(cropName);

                // after adding this data we are showing toast message.
                Toast.makeText(MainActivity.this, "data added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // if the data is not added or it is cancelled then
                // we are displaying a failure toast message.
                Toast.makeText(MainActivity.this, "Fail to add data " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUserActionForCamera() {
        Log.i("TAG", "getUserActionForCamera: started");
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        final String[] response = new String[1];
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    response[0] = TAKE_PHOTO_USING_CAMERA;
                    performUserActionForCamera(response[0]);
                } else if (items[item].equals("Choose from Library")) {
                    response[0] = SELECT_GALLERY_IMAGE;
                    performUserActionForCamera(response[0]);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
        if(response[0] == null) response[0] = "Choose from Library";
        Log.i(TAG, "getUserActionForCamera: "+response[0]);
        return response[0];
    }

    public void onCaptureImageResultFromGallery(Intent data) throws IOException {

        Uri imageUri = data.getData();
        Bitmap thumbNail = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        Log.i("call for on capture: ", "onCaptureImageResult: uri"+ " "+imageUri);
        Log.i(TAG, "onCaptureImageResult: data: "+thumbNail);
        processImage(thumbNail);
    }

    private void processImage(Bitmap thumbNail) {
        imageView.setImageBitmap(thumbNail);
        tensorflowFileWork(thumbNail);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbNail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
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
    }
//    private Uri imgUri;

    /** Here goes your code where you handle the way the user chooses between getting from gallery or opening camera **/

    /**
     * Opens gallery to choose image.
     */
    public void chooseFromGallery() {
        Log.i(TAG, "chooseFromGallery: ");
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select File"),
                SELECT_FILE);
    }

    /**
     * Creates empty image file. Launches camera app to capture image.
     */
    private void captureImageUsingCamera() {

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);

//        File image = createImageFile();
////        imgUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", image);
//        imgUri = Uri.fromFile(image);
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
//        // start the image capture Intent
//        Log.i(TAG, "captureImageUsingCamera: ");
//        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: ");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
//                chooseFromGallery();
                try {
                    onCaptureImageResultFromGallery(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
//                captureImage();

                Log.i(TAG, "onActivityResult: CAMERA_CAPTURE");
                onCaptureImageResultForCamera(data);

            }
        }
    }

    private void onCaptureImageResultForCamera(Intent data) {
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        processImage(photo);
    }
}



