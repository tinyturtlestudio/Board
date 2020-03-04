package com.example.board;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ListRSVPActivity extends AppCompatActivity {

    private ArrayList<String> mNames;
    private ArrayList<String> mImagesURL;
    private ArrayList<String> userIds;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();


    private String eventId;
    Intent prevIntent;

    private ProgressBar postProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        TextView tvRSVPL = findViewById(R.id.textView6);
        tvRSVPL.setText(getString(R.string.tvRSVPs));
        postProgress = findViewById(R.id.postProgressBar);
        postProgress.setVisibility(View.VISIBLE);
        prevIntent = getIntent();
        eventId = prevIntent.getStringExtra("eventId");
        getRSVP();
    }

    private void getRSVP(){
        userIds = new ArrayList<>();
        mImagesURL = new ArrayList<>();
        mNames = new ArrayList<>();
        Log.i("RSVPs","Getting RSVPs for " + eventId);
        db.collectionGroup("RSVP").whereEqualTo("eventId",eventId)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(final DocumentSnapshot doc: queryDocumentSnapshots){
                    Log.i("RSVPs",doc.getData().toString());
                    db.collection("users").document(doc.get("userId").toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String name = documentSnapshot.get("firstName") + " " + documentSnapshot.get("lastName");
                            userIds.add(documentSnapshot.getId());
                            mNames.add(name);
                            mImagesURL.add(documentSnapshot.get("imageRef").toString());
                            initRecyclerView();
                        }
                    });

                }
                postProgress.setVisibility(View.GONE);

            }
        });
    }

    private void initRecyclerView()
    {
        postProgress.setVisibility(View.GONE);
        Log.d("RSVPs","initRecyclerView: inti recycler");
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.removeAllViewsInLayout();
        RecyclerViewAdapterUsers adapter = new RecyclerViewAdapterUsers(this,mNames,mImagesURL,userIds);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
