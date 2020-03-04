package com.example.board;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
//Todo: Add LogOut
public class HomeActivity extends AppCompatActivity {

    private ImageButton btGoEvents;
    private ImageButton btFindEvents;
    private ImageButton btPostEvents;
    private ImageButton btProfile;
    private ImageButton btSignOut;
    private ImageButton btRSVPEvents;

    private TextView tvHomeScreen;

    private Intent myEvents;
    private Intent listEvents;
    private Intent postEvents;
    private Intent profile;

    public FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Intent intent = getIntent();

        btGoEvents = findViewById(R.id.bt_go_events);
        btFindEvents = findViewById(R.id.bt_find_events);
        btPostEvents = findViewById(R.id.bt_post_events);
        btProfile = findViewById(R.id.btProfile);
        btSignOut = findViewById(R.id.ibSignout);
        btRSVPEvents = findViewById(R.id.bt_rsvp_events);

        tvHomeScreen = findViewById(R.id.tvHomeScreen);


        String displayName = user.getDisplayName();
        if(user.getDisplayName() != null) {
            tvHomeScreen.setText("Hello " + displayName + "!");
        }
        else
            tvHomeScreen.setText("Hello!");
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        btGoEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myEvents = new Intent(HomeActivity.this, MyEventsActivity.class);
                startActivity(myEvents);
            }
        });

        btRSVPEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rsvpEvents = new Intent(HomeActivity.this,RSVPEventsActivity.class);
                startActivity(rsvpEvents);
            }
        });

        btProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             profile = new Intent(HomeActivity.this,ProfileActivity.class);
             profile.putExtra("uid",user.getUid());
             startActivity(profile);
            }
        });

        btFindEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listEvents = new Intent(HomeActivity.this, ListEventsActivity.class);
                startActivity(listEvents);
            }
        });

        btPostEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postEvents = new Intent(HomeActivity.this, PostEventActivity.class);
                startActivity(postEvents);
            }
        });

        btSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signOut = new Intent(HomeActivity.this,SignInActivity.class);
                FirebaseAuth.getInstance().signOut();
                startActivity(signOut);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
