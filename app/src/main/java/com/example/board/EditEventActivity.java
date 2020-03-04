package com.example.board;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private Button btChoosePhoto;
    private Button btConfirmEdit;
    private EditText etPostName;
    private EditText etPostDate;
    private EditText etPostTime;
    private EditText etPostAddress;
    private EditText etPostDetails;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private StorageReference mStorageRef;
    private DocumentReference eventDocRef;
    private StorageReference eventImageRef;
    private ImageView ivUserPhoto;
    private ImageView editShadow;
    private ProgressBar editProgress;
    private boolean imageSelected;
    private boolean uploading = false;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int MAP_REQUEST_CODE = 103;

    private double eventLatDouble;
    private double eventLngDouble;
    private String eventAddress;
    private String placeId;
    private String eventName;
    private String eventDetails;
    private String eventDate;
    private String eventTime;
    private String eventImgRef;
    private String eventId;
    private String eventUserId;
    private Intent intent;
    private String eventLat;
    private String eventLng;

    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "Board" ;
    final Calendar c = Calendar.getInstance();


    @Override
    public void onBackPressed() {
        if(!uploading) {
            super.onBackPressed();
        }
        else
            Toast.makeText(getApplicationContext(),"Edit is Uploading",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        intent = getIntent();
        btChoosePhoto = findViewById(R.id.btEditChoosePhoto);
        btConfirmEdit = findViewById(R.id.btConfirmEdit);
        ivUserPhoto = findViewById(R.id.ivEditUserPhoto);
        etPostName = findViewById(R.id.etEditEventName);
        etPostDate = findViewById(R.id.etEditEventDate);
        etPostTime = findViewById(R.id.etEditEventTime);
        etPostAddress = findViewById(R.id.etEditEventAddress);
        etPostDetails = findViewById(R.id.etEditEventDetails);
        editShadow = findViewById(R.id.ivEditShadow);
        editProgress = findViewById(R.id.editProgressBar);



        eventName = intent.getStringExtra("eventName");
        eventAddress = intent.getStringExtra("eventAddress");
        eventDate = intent.getStringExtra("eventDate");
        eventTime = intent.getStringExtra("eventTime");
        eventDetails = intent.getStringExtra("eventDetails");
        eventImgRef = intent.getStringExtra("imageRef");
        eventId = intent.getStringExtra("eventId");
        eventUserId  = intent.getStringExtra("eventUserId");
        eventLat = intent.getStringExtra("eventLat");
        eventLng = intent.getStringExtra("eventLng");

        String date = eventDate + " " + eventTime;
        Log.i("EditEvent", date);
        SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
        try {
            Date newDate = format.parse(eventDate);
            c.setTime(newDate);
        } catch (ParseException p){
            Log.d("ParseException", p.toString());
        }


        etPostName.setText(eventName);
        etPostAddress.setText(eventAddress);
        etPostDate.setText(eventDate);
        etPostTime.setText(eventTime);
        etPostDetails.setText(eventDetails);

        user = FirebaseAuth.getInstance().getCurrentUser();
        Log.i("user",user.getUid());

        //Get user storage reference
        storage = FirebaseStorage.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        //Get user database reference
        db = FirebaseFirestore.getInstance();
        eventDocRef = db.collection("users").document(user.getUid()).collection("events").document(eventId);

        final StorageReference imageRef = storage.getReferenceFromUrl(eventImgRef);
        Glide.with(this)
                .asBitmap()
                .load(imageRef)
                .into(ivUserPhoto);

        /*
        if(savedInstanceState != null){
            etPostName.setText(savedInstanceState.getString("eventName"));
        }

         */

        etPostAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapActivity = new Intent(getApplicationContext(),MapsActivity.class);
                startActivityForResult(mapActivity , MAP_REQUEST_CODE);
                etPostAddress.setError(null);
            }
        });


        etPostDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(),"date picker");
                etPostDate.setError(null);

            }
        });

        etPostTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(),"time picker");
                etPostTime.setError(null);

            }
        });

        //Get User


        btChoosePhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                selectImage(EditEventActivity.this);
            }
        });

        //Todo: set timestamp for event removal from database
        btConfirmEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getUid().equals(eventUserId)){
                    if (isFilled() && user != null) {
                        uploading = true;
                        editProgress.setVisibility(View.VISIBLE);
                        editShadow.setVisibility(View.VISIBLE);
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);



                        Map<String, Object> event = new HashMap<String, Object>();
                        event.put("eventName", etPostName.getText().toString());
                        event.put("eventDate", etPostDate.getText().toString());
                        event.put("eventTime", etPostTime.getText().toString());
                        event.put("eventAddress",etPostAddress.getText().toString());
                        event.put("eventDetails", etPostDetails.getText().toString());
                        event.put("eventLat",eventLat);
                        event.put("eventLng",eventLng);

                        eventDocRef.update(event).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                eventImageRef = mStorageRef.child("images/" + user.getUid() + "/events/" + eventDocRef.getId() + ".jpg");
                                updateLabel();
                                if(imageSelected == true) {
                                    uploadPhoto(eventImageRef);
                                }else{
                                    Toast.makeText(getApplicationContext(),"Event Posted",Toast.LENGTH_LONG).show();
                                    //Todo: myevents
                                    Intent myEvents = new Intent(EditEventActivity.this, MyEventsActivity.class);
                                    startActivity(myEvents);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("dbupload","upload Failed ",e);
                            }
                        });
                    }
                }
            }
        });



    }

    private boolean isFilled(){
        boolean infoCheck = true;
        if(etPostName.getText().toString().isEmpty()){
            etPostName.setError("Event Name cannot be empty");
            infoCheck = false;
        }
        if(etPostDate.getText().toString().isEmpty()){
            etPostDate.setError("Please Choose Event Date");
            infoCheck = false;
        }

        if(etPostTime.getText().toString().isEmpty()){
            etPostTime.setError("Please Choose Event Time");
            infoCheck = false;
        }

        if(etPostAddress.getText().toString().isEmpty()){
            etPostAddress.setError("Please Choose Event Address");
            infoCheck = false;
        }

        if (etPostDetails.getText().toString().length() < 25){
            etPostDetails.setError("Please Provide a short description of at least 25 characters");
            infoCheck = false;
        }

        return infoCheck;
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        //Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,hourOfDay);
        c.set(Calendar.MINUTE,minute);
        c.set(Calendar.SECOND,0);
        String currentTimeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        etPostTime.setText(currentTimeString);
        eventTime = currentTimeString;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        //Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,month);
        c.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        String currentDateString = DateFormat.getDateInstance().format(c.getTime());
        etPostDate.setText(currentDateString);
        eventDate = currentDateString;
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
                        ivUserPhoto.setImageBitmap(selectedImage);
                        imageSelected = true;
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
                                ivUserPhoto.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                                imageSelected = true;
                            }
                        }

                    }
                    break;
                case MAP_REQUEST_CODE:
                    if (resultCode == Activity.RESULT_OK){
                        eventAddress = data.getStringExtra("address");
                        eventLatDouble = data.getDoubleExtra("lat" , -1);
                        eventLngDouble = data.getDoubleExtra("lng",-1);
                        eventLat = Double.toString(eventLatDouble);
                        eventLng = Double.toString(eventLngDouble);
                        placeId = data.getStringExtra("placeId");
                        Log.i("AddressInfo", eventAddress);
                        etPostAddress.setTextSize(12);
                        etPostAddress.setText(eventAddress);
                    }
            }
        }
    }

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(EditEventActivity.this,
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
                Toast.makeText(getApplicationContext(),
                        "Camera Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    private void uploadPhoto(StorageReference reference){
        // Get the data from an ImageView as bytes
        Bitmap bitmap = ((BitmapDrawable) ivUserPhoto.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = reference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i("Fail_Post", "Could not upload to storage: " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                editProgress.setVisibility(View.GONE);
                editShadow.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Log.i("Success_Post","Upload Successful : " + taskSnapshot.getMetadata());
                Toast.makeText(getApplicationContext(), "Upload Successful", Toast.LENGTH_LONG);
                eventDocRef.update("imageRef",eventImageRef.toString());
                //Todo:Take to event, Delete image from storage, change to service
                Toast.makeText(getApplicationContext(),"Event Updated",Toast.LENGTH_LONG).show();
                Intent myEvents = new Intent(EditEventActivity.this,MyEventsActivity.class);
                startActivity(myEvents);
            }
        });
    }

    private void scheduleNotification (Notification notification , long time) {
        Intent notificationIntent = new Intent( this, MyNotificationsPublisher.class ) ;
        notificationIntent.putExtra(MyNotificationsPublisher. NOTIFICATION_ID , 1 ) ;
        notificationIntent.putExtra(MyNotificationsPublisher. NOTIFICATION , notification) ;
        PendingIntent pendingIntent = PendingIntent. getBroadcast ( this, 0 , notificationIntent , PendingIntent. FLAG_UPDATE_CURRENT ) ;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context. ALARM_SERVICE ) ;
        assert alarmManager != null;

        Log.i("Notification", "notification scheduled for " + (time));
        alarmManager.set(AlarmManager.RTC_WAKEUP , time , pendingIntent) ;
    }
    private Notification getNotification () {
        NotificationCompat.Builder builder = new NotificationCompat.Builder( this, default_notification_channel_id ) ;
        builder.setContentTitle( "Board Event" ) ;
        builder.setContentText("Your event " + etPostName.getText().toString() + " is about to begin!") ;
        builder.setSmallIcon(R.drawable.logo ) ;
        builder.setAutoCancel( true ) ;
        builder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
        return builder.build() ;
    }
    private void updateLabel () {
        Date date = c.getTime() ;
        Log.i("Notification", Long.toString(date.getTime()));
        scheduleNotification(getNotification(), date.getTime());
    }
}
