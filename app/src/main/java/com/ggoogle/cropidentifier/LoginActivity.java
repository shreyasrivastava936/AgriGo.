package com.ggoogle.cropidentifier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.ui_screen);
//
//        Button login = this.findViewById(R.id.login);
//        login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(LoginActivity.this, "login", Toast.LENGTH_SHORT).show();
//                startMainActivity();
//            }
//        });
//    }

    String TAG = "loginActivity";
    private void startMainActivity() {
        Intent myIntent = new Intent(LoginActivity.this, GraphActivity.class);
        myIntent.putExtra("id", mobile);
        LoginActivity.this.startActivity(myIntent);
    }

    /////////////////////

    private String mVerificationId;

    //The edittext to input the code
    private EditText editTextCode;

    private EditText editTextMobile;

    private String mobile;

    //firebase auth object
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_screen);

        //initializing objects
        mAuth = FirebaseAuth.getInstance();

        //if the automatic sms detection did not work, user can also enter the code manually
        //so adding a click listener to the button
        findViewById(R.id.getOTP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobile = getPhoneNumber();
                sendVerificationCode(mobile);
                Log.i(TAG, "onClick: getOTP verification code sent");
                editTextMobile.setVisibility(View.INVISIBLE);
                editTextCode = findViewById(R.id.code);
                editTextCode.setVisibility(View.VISIBLE);
                Button getOTP = findViewById(R.id.getOTP);
                getOTP.setVisibility(View.INVISIBLE);
                Button login = findViewById(R.id.login);
                login.setVisibility(View.VISIBLE);
            }
        });
        
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextCode = findViewById(R.id.code); //TODO: set on click
                String code = editTextCode.getText().toString().trim();
                Log.i(TAG, "onClick: login code: "+code);
                if (code.isEmpty() || code.length() < 6) {
                    editTextCode.setError("Enter valid code");
                    editTextCode.requestFocus();
                    return;
                }
//
//                //verifying the code entered manually
                verifyVerificationCode(code);
            }
        });

    }

    private String getPhoneNumber() {
        editTextMobile = findViewById(R.id.number);
        Log.i(TAG, "getPhoneNumber: "+editTextMobile+" "+editTextMobile.getText());
        String mobile = editTextMobile.getText().toString().trim();
        if(mobile.isEmpty() || mobile.length() < 10){
            editTextMobile.setError("Enter a valid mobile");
            editTextMobile.requestFocus();
            return "";
        }
        return mobile;
    }

    //the method is sending verification code
    //the country id is concatenated
    //you can take the country id as user input as well
    private void sendVerificationCode(String mobile) {
        Log.i("tag", "sendVerificationCode: "+mobile);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91"+mobile,
                60,
                TimeUnit.SECONDS,
                LoginActivity.this,
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            Log.i(TAG, "onVerificationCompleted: "+code);
            Toast.makeText(LoginActivity.this, "code "+code, Toast.LENGTH_SHORT).show();
            if (code != null) {
                editTextCode.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.i(TAG, "onVerificationCompleted2: "+e.getMessage());
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            Log.i(TAG, "onVerificationCompleted2: "+s);
            //storing the verification id that is sent to the user
            mVerificationId = s;
        }
    };


    private void verifyVerificationCode(String code) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            Log.i(TAG, "onComplete: verification code verified");
                            startMainActivity();

                        } else {

                            //verification unsuccessful.. display an error message

                            String message = "Somthing is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }

                            Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_LONG);
                            snackbar.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            snackbar.show();
                        }
                    }
                });
    }


}
