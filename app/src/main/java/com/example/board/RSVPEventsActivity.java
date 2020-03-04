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
import com.google.android.gms.tasks.OnSuccessListener;
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

import org.w3c.dom.Text;

public class RSVPEventsActivity extends AppCompatActivity {


    private static final String TAG = "ListEvents";
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList <String> mImagesURL = new ArrayList<>();
    private ArrayList<String> mDetails = new ArrayList<>();
    private ArrayList<String> mDistances = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<DocumentSnapshot> eventsQuery;
    public FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private final double RADIUS = 3958.8;

    private GeoLocation myLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    final private int LOCATION_PERMISSION_REQUEST_CODE = 103;

    private ProgressBar postProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        TextView tvRSVPEvents = findViewById(R.id.textView6);
        tvRSVPEvents.setText(getString(R.string.btn_rsvp_events));
        postProgress = findViewById(R.id.postProgressBar);
        postProgress.setVisibility(View.VISIBLE);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();

    }

    private void getEvents(){
        eventsQuery = new ArrayList<>();
        //Todo: fix constraints
        db.collection("users").document(user.getUid()).collection("RSVP")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (final QueryDocumentSnapshot doc: task.getResult()){
                        String eventId = doc.getId();
                        String ownerId = doc.get("eventOwnerId").toString();
                        db.collection("users").document(ownerId).collection("events").document(eventId)
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot document) {
                                if(document.exists()) {
                                    Log.i("eventQuery", document.getId() + "=>" + document.getData());
                                    eventsQuery.add(document);
                                    initImageBitmaps();

                                }
                                else{
                                    Log.i("eventQuery","event has been deleted");
                                    doc.getReference().delete();

                                }
                            }

                        });
                    }
                    postProgress.setVisibility(View.GONE);
                }
                else {
                    Log.i("eventQuery", "Error getting documents: " + task.getException());
                }

            }
        });
    }

    private void initImageBitmaps()
    {
        mNames = new ArrayList<>();
        mImagesURL = new ArrayList<>();
        mDetails = new ArrayList<>();
        mDistances = new ArrayList<>();
        for(int i = 0; i < eventsQuery.size(); i++){
            DocumentSnapshot doc = eventsQuery.get(i);
            Log.d(TAG,doc.getData().toString());
            double eventLat = Double.parseDouble(doc.get("eventLat").toString());
            double eventLng = Double.parseDouble(doc.get("eventLng").toString());
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
        RecyclerViewAdapterRSVP rsvpAdapter = new RecyclerViewAdapterRSVP(this,mNames,mImagesURL,mDetails,mDistances,eventsQuery);
        recyclerView.setAdapter(rsvpAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        getEvents();
    }
}