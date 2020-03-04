package com.example.board;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ListEventsActivity extends AppCompatActivity {
    //private TextView tvLat;
    //private TextView tvLng;
    private TextView tvDistance;
    private SeekBar sbDistance;
    private Button btnUpdateDist;
    private ProgressBar postProgress;


    private double distance;
    //3.6 = 5 mile radius, distance * 0.72
    private GeoLocation myLocation;
    private GeoLocation[] boundingCoords;
    private double distanceTo;
    private final double RADIUS = 3958.8;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;

    final private int LOCATION_PERMISSION_REQUEST_CODE = 103;

    private static final String TAG = "ListEvents";
    private ArrayList<String> mDistances = new ArrayList<>();
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList <String> mImagesURL = new ArrayList<>();
    private ArrayList<String> mDetails = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<QueryDocumentSnapshot> eventsQuery;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_events);
        //tvLat = findViewById(R.id.tvLat);
        //tvLng = findViewById(R.id.tvLng);
        tvDistance = findViewById(R.id.tvDistance);
        sbDistance = findViewById(R.id.sbDistance);
        btnUpdateDist = findViewById(R.id.btnUpdateDist);
        postProgress = findViewById(R.id.postProgressBar);


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();
        distance = 7.2;


        eventsQuery = new ArrayList<>();


        sbDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvDistance.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnUpdateDist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNames.clear();
                mDetails.clear();
                mImagesURL.clear();
                eventsQuery.clear();
                mDistances.clear();
                distance = Double.parseDouble(tvDistance.getText().toString());
                getBoundingCoord();
            }
        });


    }

    private void getEvents(){
        //Todo: fix constraints
        postProgress.setVisibility(View.VISIBLE);
        db.collectionGroup("events")
                .whereGreaterThanOrEqualTo("eventLng",boundingCoords[0].getLongitudeInDegrees())
                .whereLessThanOrEqualTo("eventLng",boundingCoords[1].getLongitudeInDegrees())
                .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document: task.getResult()){
                        double eventLat = Double.parseDouble(document.getData().get("eventLat").toString());
                        double eventLng = Double.parseDouble(document.getData().get("eventLng").toString());
                        GeoLocation eventLocation = GeoLocation.fromDegrees(eventLat,eventLng);
                        distanceTo = myLocation.distanceTo(eventLocation,RADIUS);
                        Log.i("eventQuery",document.getData().get("eventName").toString() + " Distance:" + distanceTo);
                        /*
                        if(eventLat > boundingCoords[0].getLatitudeInDegrees() &&
                        eventLat < boundingCoords[1].getLatitudeInDegrees())

                         */

                        if(distanceTo < distance){
                            Log.i("eventQuery", document.getId() + "=>" + document.getData());
                            eventsQuery.add(document);
                        }
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

    private void initRecyclerView()
    {
        postProgress.setVisibility(View.GONE);
        Log.d(TAG,"initRecyclerView: inti recycler");
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.removeAllViewsInLayout();
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this,mNames,mImagesURL,mDetails,mDistances,eventsQuery);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        mLinearLayout.setReverseLayout(false);
        mLinearLayout.setStackFromEnd(false);
        recyclerView.setLayoutManager(mLinearLayout);
    }



    private void getBoundingCoord(){
        boundingCoords = myLocation.boundingCoordinates(distance,RADIUS);
        String bCoord1 = Double.toString(boundingCoords[0].getLatitudeInDegrees());
        String bCoord2 = Double.toString(boundingCoords[0].getLongitudeInDegrees());
        String bCoord3 = Double.toString(boundingCoords[1].getLatitudeInDegrees());
        String bCoord4 = Double.toString(boundingCoords[1].getLongitudeInDegrees());

        Log.i("Bounding Cordinates",bCoord1 + " " + bCoord2 + " " + bCoord3 + " " + bCoord4);

        getEvents();

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
                                //tvLat.setText(Double.toString(mLastKnownLocation.getLatitude()));
                                //tvLng.setText(Double.toString(mLastKnownLocation.getLongitude()));
                                myLocation = GeoLocation.fromDegrees(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                                getBoundingCoord();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent home = new Intent(ListEventsActivity.this,HomeActivity.class);
        startActivity(home);
    }
}
