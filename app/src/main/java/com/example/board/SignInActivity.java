package com.example.board;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    public static final String SIGN_IN = "Sign In";
    private FirebaseAuth mAuth;
    private Button btSignIn;
    private EditText etEmail;
    private EditText etPassword;
    private ImageButton btViewPass;
    private FirebaseAuth.AuthStateListener mAuthListner;
    Intent loginIntent;
    Intent signUpIntent;
    boolean showingPass = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        btSignIn = findViewById(R.id.btSignIn);
        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btViewPass = findViewById(R.id.btViewPasswordIn);
        TextView t2 = findViewById(R.id.tvLinkSignUp);


        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    String name = user.getDisplayName();
                    loginIntent = new Intent(SignInActivity.this, HomeActivity.class);
                    loginIntent.putExtra("displayName",name);
                    startActivity(loginIntent);
                }
            }
        };


        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();

                if(email.isEmpty()){
                    etEmail.setError("Email required.");
                    return;
                }

                if(password.isEmpty()){
                    etPassword.setError("Password required.");
                    return;
                }

                mAuth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(SIGN_IN, "signInWithEmail:success");

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(SIGN_IN, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    //updateUI(null);
                                }
                            }
                        });
            }
        });


        btViewPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!showingPass) {
                    etPassword.setInputType(0x00000091);
                    showingPass = true;
                }
                else{
                    etPassword.setInputType( 0x00000081);
                    showingPass = false;
                }
            }
        });

        t2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signUpIntent = new Intent(v.getContext(), SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

    }

    protected void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListner);
    }

    protected void onStop(){
        super.onStop();
        if(mAuthListner != null){
            mAuth.removeAuthStateListener(mAuthListner);
        }
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
