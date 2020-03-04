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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private TextView tvDisplayName;
    private TextView tvEventCount;
    private TextView tvRSVPcount;
    private EditText tvArea;
    private EditText tvAbout;
    private ImageButton ibProfilePic;
    private ProgressBar postProgress;
    private Button btSave;
    private String name;
    private String eventCount;
    private String RSVPs;
    private String area;
    private String about;
    private Intent prevIntent;

    private Boolean imageSelected = false;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private DocumentReference docRef = db.collection("users").document(user.getUid());
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    final StorageReference profilePicRef = mStorageRef.child("images/" + user.getUid() + "/profilepicture.jpg");


    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        ibProfilePic = findViewById(R.id.ibProfilePic);
        btSave = findViewById(R.id.btSaveProfile);
        tvDisplayName = findViewById(R.id.tvDisplayName);
        tvEventCount = findViewById(R.id.tvEventsCount);
        tvRSVPcount = findViewById(R.id.tvRSVPCount);
        tvArea = findViewById(R.id.tvArea);
        tvAbout = findViewById(R.id.tvAbout);
        postProgress = findViewById(R.id.postProgressBar);

        prevIntent = getIntent();
        name = prevIntent.getStringExtra("name");
        eventCount = prevIntent.getStringExtra("events");
        RSVPs = prevIntent.getStringExtra("RSVPs");
        area = prevIntent.getStringExtra("area");
        about = prevIntent.getStringExtra("about");

        tvDisplayName.setText(name);
        tvEventCount.setText(eventCount);
        tvRSVPcount.setText(RSVPs);
        tvArea.setText(area);
        tvAbout.setText(about);


        //TODO:Change to Async
        fetchUser();


        ibProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(EditProfileActivity.this);
                imageSelected = true;
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postProgress.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                if(imageSelected) {
                    updateProfile();
                    uploadPhoto();
                }
                else{
                    updateProfile();
                    finish();
                }
                //TODO: CH
                //if(ibProfilePic.getDrawable() != null)

            }
        });


    }

    private void updateProfile(){
        HashMap<String,Object> profile = new HashMap<>();
        profile.put("area",tvArea.getText().toString());
        profile.put("about",tvAbout.getText().toString());
        docRef.update(profile).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ProfileEdit", "Failure Updating Profile");
            }
        });
    }

    public void fetchUser(){

        final long ONE_MEGABYTE = 1024*1024;
        profilePicRef.getBytes(5 * ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                // Resize the bitmap to 150x100 (width x height)
                Bitmap bMapScaled = Bitmap.createScaledBitmap(image, ibProfilePic.getWidth(), ibProfilePic.getHeight(), true);
                ibProfilePic.setImageBitmap(bMapScaled);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Get Pic Fail", e.getMessage());
            }
        });
    }


    private void selectImage(Context context) {
        final CharSequence[] options = { "Camera", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Upload Photo");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Camera")) {
                    checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        Bitmap bMapScaled = Bitmap.createScaledBitmap(selectedImage, ibProfilePic.getWidth(), ibProfilePic.getHeight(), true);
                        ibProfilePic.setImageBitmap(bMapScaled);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                Bitmap image = BitmapFactory.decodeFile(picturePath);
                                Bitmap bMapScaled = Bitmap.createScaledBitmap(image, ibProfilePic.getWidth(), ibProfilePic.getHeight(), true);
                                ibProfilePic.setImageBitmap(bMapScaled);

                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this,
                    new String[] { permission },
                    requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        "Camera Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    public void uploadPhoto(){
        // Get the data from an ImageView as bytes
        Bitmap bitmap = ((BitmapDrawable) ibProfilePic.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = profilePicRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("Fail_Post", "Could not upload to storage: " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                /*
                db.collection("users").document(user.getUid()).update("imageRef",profilePicRef).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

                 */
                postProgress.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                finish();
            }
        });
    }
}
