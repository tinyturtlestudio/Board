package com.example.board;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    public static final String DB_ADD = "DB_Add";
    public static final String DB_FAIL = "DB_Fail";
    private FirebaseAuth mAuth;
    public FirebaseFirestore db;
    private Button signUpBtn;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordRe;
    private EditText etFirstName;
    private EditText etLastName;
    private ImageButton btViewPass;
    private ImageButton btViewPassRe;
    boolean showingPass = false;
    boolean showingRe = false;
    //Intent signIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signUpBtn = findViewById(R.id.btSignIn);
        btViewPass = findViewById(R.id.btViewPassword);
        btViewPassRe = findViewById(R.id.btViewPasswordRe);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordRe = findViewById(R.id.etPasswordRe);
        etFirstName = findViewById(R.id.etSignFirstName);
        etLastName = findViewById(R.id.etSignLastName);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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

        btViewPassRe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!showingRe){
                    etPasswordRe.setInputType(0x00000091);
                    showingRe = true;
                }
                else{
                    etPasswordRe.setInputType(0x00000081);
                    showingRe = false;
                }

            }
        });


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String firstName = etFirstName.getText().toString();
                final String lastName = etLastName.getText().toString();
                final String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();
                final String passwordRe = etPasswordRe.getText().toString();
                boolean infoError = false;


                if (firstName.isEmpty()){
                    etFirstName.setError("First Name cannot be empty.");
                    infoError = true;
                }

                if(lastName.isEmpty()){
                    etLastName.setError("Last Name cannot be empty");
                    infoError = true;
                }

                if (email.isEmpty()){
                    etEmail.setError("Email cannot be empty");
                    infoError = true;
                }

                if(!password.equals(passwordRe)){
                    etPasswordRe.setError("Passwords do not match");
                    infoError = true;
                }

                if(!isValidPassword(password)){
                    etPassword.setError("Password must contain 8 or more characters with a mix of one uppercase, one lowercase, numbers & symbols");
                    infoError = true;
                }

                if(infoError){return;}

                if(password.equals(passwordRe) && isValidPassword(password)){
                    Log.d("Accepted Password", password);
                    mAuth.createUserWithEmailAndPassword(email,password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Log.d("Sign Up", "createUserWithEmailAndPassword:success");
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(firstName + " " + lastName).build();

                                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Log.d("Sign Up","User profile created");
                                                }
                                            }
                                        });

                                        Map<String,String> userName = new HashMap<>();
                                        userName.put("firstName" , firstName);
                                        userName.put("lastName", lastName);
                                        userName.put("about","Nothing Yet!");
                                        userName.put("area","None Yet!");
                                        userName.put("imageRef","gs://boardapphht.appspot.com/images/party-hat.png");
                                        db.collection("users").document(user.getUid()).set(userName)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(DB_ADD, "User added to database.");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                    Log.d(DB_FAIL,"User was not added to database.");
                                                    }
                                        });

                                        user.sendEmailVerification()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(SignUpActivity.this,"Email sent to: " + email, Toast.LENGTH_LONG).show();
                                                            Log.d("Sign Up","Email sent.");
                                                        }
                                                        Intent login = new Intent(SignUpActivity.this, SignInActivity.class);
                                                        startActivity(login);
                                                    }
                                                });
                                    }
                                    else{
                                        Log.w("Sign Up", "createUserWithEmail:failure", task.getException());
                                    }
                                }
                            });
                }
            }
        });

        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent loginIntent = new Intent(v.getContext(), SignInActivity.class);
                startActivity(loginIntent);
            }
        });

    }

    public static boolean isValidPassword(final String password) {
        //8 characters, 1 alphabet, 1 number, 1 special character
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }
}
