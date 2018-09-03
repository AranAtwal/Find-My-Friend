package com.example.aranatwal.fypv3;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.aranatwal.fypv3.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    private EditText mEmail_signup, mPassword_signup, mPassword_signup_confirm, mFirstname_signup, mLastname_signup;
    private Button mSignup_signup;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mSignup_signup = (Button) findViewById(R.id.signup_login);

        mFirstname_signup = (EditText) findViewById(R.id.signup_firstname);
        mLastname_signup = (EditText) findViewById(R.id.signup_lastname);
        mEmail_signup = (EditText) findViewById(R.id.signup_email);
        mPassword_signup = (EditText) findViewById(R.id.signup_password);
        mPassword_signup_confirm = (EditText) findViewById(R.id.signup_password_confirm) ;

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // checks user isnt logged in and on wrong page
                if (user != null) {
                    Intent intent = new Intent(SignUpActivity.this, UserMapActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        };



        mSignup_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//pulls fields and checks possible problems with sign up in order and displays appropriate message
                final String firstname_signup = mFirstname_signup.getText().toString();//new
                final String lastname_signup = mLastname_signup.getText().toString();//new
                final String email_signup = mEmail_signup.getText().toString();
                final String password_signup = mPassword_signup.getText().toString();
                final String password_signup_confirm = mPassword_signup_confirm.getText().toString();//new

                if (TextUtils.isEmpty(firstname_signup)) {
                    mFirstname_signup.setError("Firstname is required");
                }
                else if (TextUtils.isEmpty(lastname_signup)) {
                    mLastname_signup.setError("Lastname is required");
                }
                else if (TextUtils.isEmpty(email_signup)) {
                    mEmail_signup.setError("Email is required");
                }
                else if (TextUtils.isEmpty(password_signup)) {
                    mPassword_signup.setError("Password is required");
                }
                else if (password_signup.length()<6) {
                    mPassword_signup.setError("Password must be 6 or more character");
                }
                else if (TextUtils.isEmpty(password_signup_confirm)) {
                    mPassword_signup_confirm.setError("Password Confirm is required");
                }
                else if (!password_signup.equals(password_signup_confirm)) {
                    mPassword_signup_confirm.setError("Passwords do not match");
                } else {

                    mAuth.createUserWithEmailAndPassword(email_signup, password_signup).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {//creates user

                            while (!task.isComplete()) {

                            }

                            if (task.isComplete()) {
                                String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                writeNewUser(user_id, firstname_signup, lastname_signup, email_signup);
                            }


                        }
                    });


                }



            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent =  new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
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

    private void writeNewUser(String user_id, String firstname, String lastname, String email) { // writes new user to map and then adds to database
        User user = new User(firstname, lastname, email);

        CollectionReference collection = db.collection("Users");
        user.toMap();
        collection.document(user_id).set(user);



    }
}
