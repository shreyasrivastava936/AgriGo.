package com.ggoogle.cropidentifier;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class GraphActivity extends AppCompatActivity {

    HashMap<String, Integer> map;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceForAnalytics;
    DatabaseReference parentDatabaseReference;
    DatabaseReference AnalysisDatabaseReference;
    BarDataSet bardataset;
    BarChart barChart;
    String TAG = "GraphActivity";
    Map<String, Long> cropVsUser;
    LocationListener locationListener;
    LocationManager locationManager;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_screen);
        
        barChart = (BarChart) findViewById(R.id.barchart);
        map = new HashMap<>();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        getDatabaseData();

        map.put("Carrot", 0);
        map.put("Cofee", 1);
        map.put("Corn", 2);
        map.put("Cotton", 3);
        map.put("Mint", 4);
        map.put("Rice", 5);
        map.put("Sugarcane", 6);
        map.put("Tobbaco", 7);
        map.put("Tomato", 8);
        map.put("Wheat", 9);

        findViewById(R.id.switchScreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });

        findViewById(R.id.showAnalysis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: analysis button");
                showAnalyticalData();
            }
        });

    }

    private void showAnalyticalData() {
        //new page
        //numbering by color & sort in descending order (top --> most demand)
        //dialog box
        startAnalyticsActivity();
    }

    private void startMainActivity() {
        Bundle extras= getIntent().getExtras();
        if(extras == null)
            return;
        String userId = extras.getString("id", null);
        Intent myIntent = new Intent(GraphActivity.this, MainActivity.class);
        myIntent.putExtra("id", userId);
        myIntent.putExtra("location", location);
        GraphActivity.this.startActivity(myIntent);
    }

    private void startAnalyticsActivity() {
        Log.i(TAG, "startAnalyticsActivity: ");
        Intent myIntent = new Intent(GraphActivity.this, AnalysticsActivity.class);
        getLocation();
        Log.i(TAG, "startAnalyticsActivity: location "+location);
        myIntent.putExtra("location", location);
        String month = LocalDate.now().getMonth().toString();
        myIntent.putExtra("month", month);
        GraphActivity.this.startActivity(myIntent);
    }

    private void changeGraph(ArrayList<BarEntry> entries) {
        for (BarEntry entry: entries) {
            Log.i(TAG, "changeGraph: "+entry.getVal()+" "+entry);
        }
        bardataset = new BarDataSet(entries, "Cells");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Carrot"); 
        labels.add("Cofee"); 
        labels.add("Corn"); 
        labels.add("Cotton"); 
        labels.add("Mint"); 
        labels.add("Rice");
        labels.add("Sugarcane"); 
        labels.add("Tobbaco"); 
        labels.add("Tomato"); 
        labels.add("Wheat");

        BarData data = new BarData(labels, bardataset);
        barChart.setData(data); // set the data and list of labels into chart
        barChart.setDescription("Set Bar Chart Description Here");  // set the description
        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
        barChart.animateY(5000);
    }

    private void getDatabaseData() {
        firebaseDatabase = FirebaseDatabase.getInstance();

        // below line is used to get
        // reference for our database.
//        String location = "Gautam Buddh Nagar";
        getLocation();
        String month = LocalDate.now().getMonth().toString();

//        checkForChangeinParentDatabase(firebaseDatabase, location);



//        databaseReference = firebaseDatabase.getReference(location);


    }

    private void checkForChangeinParentDatabase(FirebaseDatabase firebaseDatabase, String location) {
        Log.i(TAG, "checkForChangeinParentDatabase: started");
        Log.i(TAG, "checkForChangeinParentDatabase: location "+location);
        String month = LocalDate.now().getMonth().toString();

        cropVsUser = new HashMap<>();
        parentDatabaseReference = firebaseDatabase.getReference("CropsInfo/"+location+"/"+month);
        parentDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // this method is call to get the realtime
                // updates in the data.
                // this method is called when the data is
                // changed in our Firebase console.
                // below line is for getting the data from
                // snapshot of our database.
                Log.i(TAG, "onDataChange: parentDatabaseReference "+snapshot.getChildrenCount());
//                String value = snapshot.getValue(String.class);

//                Log.i(TAG, "onDataChange: value: parentDatabaseReference "+value);

                ArrayList<BarEntry> entries = new ArrayList<>();
                for(String crop: map.keySet()) {
                    Log.i(TAG, "onDataChange: crop "+crop);
                    databaseReference = firebaseDatabase.getReference("CropsInfo/"+location+"/"+month+"/"+crop);
                    long userCount = getUserCount(databaseReference);
                    Log.i(TAG, "onDataChange: usercount "+userCount);
                    if(userCount != -1) cropVsUser.put(crop, userCount);
                }
                
//                for (String crop: map.keySet()) {
//                    float f = (float)cropVsUser.getOrDefault(crop, 0L);
//                    Log.i(TAG, "onDataChange: float: "+f+" "+crop);
//                    entries.add(new BarEntry(f, map.get(crop)));     //usercount, index
//                }
//                changeGraph(entries);
                // after getting the value we are setting
                // our value to our text view in below line.
//                retrieveTV.setText(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // calling on cancelled method when we receive
                // any error or we are not able to get the data.
                Log.e(TAG, "onCancelled: kk: "+ error);
                Toast.makeText(GraphActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private long getUserCount(DatabaseReference databaseReference) {
        Log.i(TAG, "getUserCount: started");
        final long[] count = {-1};
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                count[0] = snapshot.getChildrenCount();
                Log.i(TAG, "onDataChange: databaseReference "+snapshot.getChildrenCount()+" "+snapshot);

                String crop = snapshot.getKey();
                    Log.i(TAG, "onDataChange: crop2 "+crop);
                    Log.i(TAG, "onDataChange: usercount2 "+count[0]);
                    if(count[0] != -1) cropVsUser.put(crop, count[0]);

                ArrayList<BarEntry> entries = new ArrayList<>();
                for (String crop2: map.keySet()) {
                    float f = (float)cropVsUser.getOrDefault(crop2, 0L);
                    Log.i(TAG, "onDataChange: float2: "+f+" "+crop2);
                    entries.add(new BarEntry(f, map.get(crop2)));     //usercount, index
                }
                changeGraph(entries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // calling on cancelled method when we receive
                // any error or we are not able to get the data.
                Toast.makeText(GraphActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
        return count[0];
    }

    private void getLocation() {
        locationListener = new LocationListener()
        {

            @Override
            public void onLocationChanged(Location location2) {
//                   Toast.makeText(getApplicationContext(),"Location: "+ location2.getLatitude() + "longitude:   "+ location2.getLongitude(),Toast.LENGTH_SHORT).show();
                double lat = location2.getLatitude();
                double lon = location2.getLongitude();
                Log.i(TAG, "onLocationChanged: lat: "+lat);
                getAddress(lat, lon);
                locationManager.removeUpdates(locationListener);

                checkForChangeinParentDatabase(firebaseDatabase, location);
                //locationManager = null;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i(TAG, "onStatusChanged: ");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.i(TAG, "onProviderEnabled: ");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.i(TAG, "onProviderDisabled: ");
            }
        };

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(GraphActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        }
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            if(obj != null) {
                location = obj.getSubAdminArea();
            }

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
}
