package com.example.board;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;


import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyEventsActivity extends AppCompatActivity {


    private static final String TAG = "ListEvents";
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList <String> mImagesURL = new ArrayList<>();
    private ArrayList<String> mDetails = new ArrayList<>();
    private ArrayList<String> mDistances = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<QueryDocumentSnapshot> eventsQuery;
    public FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private final double RADIUS = 3958.8;

    private GeoLocation myLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    final private int LOCATION_PERMISSION_REQUEST_CODE = 103;

    private ProgressBar postProgress;

    private Intent prevIntent;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        eventsQuery = new ArrayList<>();
        TextView tvHeader = findViewById(R.id.textView6);
        postProgress = findViewById(R.id.postProgressBar);
        postProgress.setVisibility(View.VISIBLE);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();
        prevIntent = getIntent();

        if (prevIntent.getExtras() == null){
            userId = user.getUid();
        }else
            userId = prevIntent.getStringExtra("userId");

        if (!userId.equals(user.getUid())){
            tvHeader.setText(R.string.tvTheirEvents);
        }
    }



    private void getEvents(){
        //Todo: fix constraints
        db.collection("users").document(userId).collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document: task.getResult()){
                                Log.i("eventQuery", document.getId() + "=>" + document.getData());
                                eventsQuery.add(document);
                            }
                            initImageBitmaps();

                        } else {
                            Log.i("eventQuery", "Error getting documents: " + task.getException());
                        }
                    }
                });
    }

    private void initImageBitmaps()
    {
        for(int i = 0; i < eventsQuery.size(); i++){
            QueryDocumentSnapshot doc = eventsQuery.get(i);
            double eventLat = Double.parseDouble(doc.getData().get("eventLat").toString());
            double eventLng = Double.parseDouble(doc.getData().get("eventLng").toString());
            GeoLocation eventLocation = GeoLocation.fromDegrees(eventLat,eventLng);
            mDistances.add(String.format("%.2f",myLocation.distanceTo(eventLocation,RADIUS)));
            mNames.add(doc.get("eventName").toString());
            mDetails.add(doc.get("eventDetails").toString());
            mImagesURL.add(doc.get("imageRef").toString());
        }
        initRecyclerView();
    }

    private void getLocationPermission(){
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,permission,LOCATION_PERMISSION_REQUEST_CODE);
        }
        else getDeviceLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getDeviceLocation();
                }
            }
        }
    }

    private void getDeviceLocation(){
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null){
                                myLocation = GeoLocation.fromDegrees(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                                getEvents();
                            }
                            else {
                                Log.d("locationErr", "Cannot get location");
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("locationErr", e.toString());
            }
        });
    }

    private void initRecyclerView()
    {
        postProgress.setVisibility(View.GONE);
        Log.d(TAG,"initRecyclerView: inti recycler");
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.removeAllViewsInLayout();
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this,mNames,mImagesURL,mDetails,mDistances,eventsQuery);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        mLinearLayout.setReverseLayout(true);
        mLinearLayout.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLinearLayout);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent home = new Intent(MyEventsActivity.this,HomeActivity.class);
        startActivity(home);
    }
}
