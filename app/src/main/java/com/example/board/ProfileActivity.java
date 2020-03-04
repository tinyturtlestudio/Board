package com.example.board;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvDisplayName;
    private TextView tvAbout;
    private TextView tvArea;
    private TextView tvEventsCount;
    private TextView tvRSVPCount;
    private ImageView ivProfilePic;
    private Button btEditProfile;
    private String uid;
    private Intent prevIntent;
    private Bitmap bMapScaled;
    private ProgressBar postProgress;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        tvDisplayName = findViewById(R.id.tvDisplayName);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btEditProfile = findViewById(R.id.btEditProfile);
        tvAbout = findViewById(R.id.tvAbout);
        tvArea = findViewById(R.id.tvArea);
        tvEventsCount = findViewById(R.id.tvEventsCount);
        tvRSVPCount = findViewById(R.id.tvRSVPCount);
        prevIntent = getIntent();
        uid = prevIntent.getStringExtra("uid");
        postProgress = findViewById(R.id.postProgressBar);
        postProgress.setVisibility(View.VISIBLE);

        DocumentReference docRef = db.collection("users").document(uid);
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profilePicRef = mStorageRef.child("images/" + uid + "/profilepicture.jpg");

        if (uid.equals(user.getUid())){
           btEditProfile.setVisibility(View.VISIBLE);
        }



        //Todo: Async
        fetchUser(docRef,profilePicRef);

        btEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfile = new Intent(getApplicationContext(),EditProfileActivity.class);
                editProfile.putExtra("name",tvDisplayName.getText());
                editProfile.putExtra("events",tvEventsCount.getText());
                editProfile.putExtra("RSVPs",tvRSVPCount.getText());
                editProfile.putExtra("area",tvArea.getText());
                editProfile.putExtra("about",tvAbout.getText());
                startActivity(editProfile);
            }
        });

        tvEventsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventsIntent = new Intent(getApplicationContext(),MyEventsActivity.class);
                eventsIntent.putExtra("userId",uid);
                startActivity(eventsIntent);
            }
        });

    }

    public void fetchUser(DocumentReference docRef, StorageReference profilePicRef){
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Map<String,Object> profileData = documentSnapshot.getData();
                    tvDisplayName.setText(profileData.get("firstName").toString() + " " + profileData.get("lastName").toString());
                    tvAbout.setText(profileData.get("about").toString());
                    tvArea.setText(profileData.get("area").toString());
                }
            }
        });

        docRef.collection("events").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                tvEventsCount.setText(Integer.toString(queryDocumentSnapshots.size()));
            }
        });

        docRef.collection("RSVP").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                tvRSVPCount.setText(Integer.toString(queryDocumentSnapshots.size()));
            }
        });

        final long ONE_MEGABYTE = 1024*1024;
        profilePicRef.getBytes(5 * ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                // Resize the bitmap to 150x100 (width x height)
                bMapScaled = Bitmap.createScaledBitmap(image, ivProfilePic.getWidth(), ivProfilePic.getHeight(), true);
                ivProfilePic.setImageBitmap(bMapScaled);
                postProgress.setVisibility(View.GONE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Get Pic Fail", e.getMessage());
                postProgress.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        DocumentReference docRef = db.collection("users").document(uid);
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profilePicRef = mStorageRef.child("images/" + uid + "/profilepicture.jpg");
        fetchUser(docRef,profilePicRef);
    }
}
