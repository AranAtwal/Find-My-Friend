package com.example.aranatwal.fypv3;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {//first screen shown, used for login

    private EditText mLogin_email, mLogin_password;
    private Button mLogin, mSignup;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {//if the user is already logged in on the device they pass through as they shouldnt need to log in again
                    Intent intent = new Intent(MainActivity.this, UserMapActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        };

        mLogin_email = (EditText) findViewById(R.id.login_email);
        mLogin_password = (EditText) findViewById(R.id.login_password);


        mLogin = (Button) findViewById(R.id.login);
        mSignup = (Button) findViewById(R.id.signup);

        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//takes user to signup page
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email_login = mLogin_email.getText().toString();
                final String password_login = mLogin_password.getText().toString();


                if (TextUtils.isEmpty(email_login)) {
                    mLogin_email.setError("Email is required");
                }
                else if (TextUtils.isEmpty(password_login)) {
                    mLogin_password.setError("Password is required");
                } else {

                    //authenticating user

                    mAuth.signInWithEmailAndPassword(email_login, password_login).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Log in error, Sorry!", Toast.LENGTH_SHORT).show();
                                }
                            }
                    });

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);

    }
}

