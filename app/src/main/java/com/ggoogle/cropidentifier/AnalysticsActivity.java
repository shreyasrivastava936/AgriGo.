package com.ggoogle.cropidentifier;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.database.*;

import java.time.LocalDate;

public class AnalysticsActivity extends AppCompatActivity {

    private static final String TAG = "AnalyticsActivity";
    DatabaseReference databaseReferenceForAnalytics;
    FirebaseDatabase firebaseDatabase;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("TAG", "onCreate: started for analytics" );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_layout);
        firebaseDatabase = FirebaseDatabase.getInstance();
        linearLayout = this.findViewById(R.id.linearLayout);
        onAnalyticsDataAdd();
    }

    private void onAnalyticsDataAdd() {
        Bundle extras= getIntent().getExtras();
        String location = extras.getString("location", null);

        Log.i(TAG, "onAnalyticsDataAdd: location "+location);
        String month = LocalDate.now().getMonth().toString();
        databaseReferenceForAnalytics =
                firebaseDatabase.getReference("CropsAnalysis/"+location+"/"+month);

        Log.i(TAG, "saveDataTofirebase2: ");
        databaseReferenceForAnalytics.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                linearLayout.removeAllViews();
                for(DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                    String values[] = dataSnapshot1.getValue(String.class).replace(" ", "").split(",");
                    String rank = dataSnapshot1.getKey();

                    for (String value: values) {
                        Button button = new Button(AnalysticsActivity.this);
                        button.setText(value);
                        button.setBackgroundColor(getColorForCrops(Integer.valueOf(rank)));
                        linearLayout.addView(button);
                        Log.i(TAG, "onDataChange: for analytics: "+value+" "+dataSnapshot1.getKey());
                    }

                }

//                AssignCropsToLayout(value);
                // after adding this data we are showing toast message.
//                Toast.makeText(AnalysticsActivity.this, "data changed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // if the data is not added or it is cancelled then
                // we are displaying a failure toast message.
                Toast.makeText(AnalysticsActivity.this, "Fail to add data " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void AssignCropsToLayout(Object value) {
        if(value == null)   return;
        String cropList[] = value.toString().replace(" ", "").split(",");


    }

    private int getColorForCrops(int rank) {
        switch (rank) {
            case 1: return Color.parseColor("#33D4FF");
            case 2: return Color.parseColor("#338DFF");
            case 3: return Color.parseColor("#335EFF");
            case 4: return Color.parseColor("#6E6B9E");
            case 5: return Color.parseColor("#43385B");
            case 6: return Color.parseColor("#636161");
            case 7: return Color.parseColor("#A97BDC");
            case 8: return Color.parseColor("#C07BDC");
            case 9: return Color.parseColor("#600E64");
            case 10: return Color.parseColor("#B4207C");
            default: return Color.parseColor("#B4AD20");
        }
    }
}
