package com.ggoogle.cropidentifier;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraphActivity extends AppCompatActivity {

    HashMap<String, Integer> map;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference parentDatabaseReference;
    BarDataSet bardataset;
    BarChart barChart;
    String TAG = "GraphActivity";
    Map<String, Long> cropVsUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_screen);
        
        barChart = (BarChart) findViewById(R.id.barchart);
        map = new HashMap<>();
        getDatabaseData();

        map.put("Carrot", 0);
        map.put("Coffee", 1);
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

    }

    private void startMainActivity() {
        Bundle extras= getIntent().getExtras();
        if(extras == null)
            return;
        String userId = extras.getString("id", null);
        Intent myIntent = new Intent(GraphActivity.this, MainActivity.class);
        myIntent.putExtra("id", userId);
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
        String location = "Gautam Buddh Nagar";
        String month = LocalDate.now().getMonth().toString();


        checkForChangeinParentDatabase(firebaseDatabase, location);



//        databaseReference = firebaseDatabase.getReference(location);


    }

    private void checkForChangeinParentDatabase(FirebaseDatabase firebaseDatabase, String location) {
        Log.i(TAG, "checkForChangeinParentDatabase: started");
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
}
