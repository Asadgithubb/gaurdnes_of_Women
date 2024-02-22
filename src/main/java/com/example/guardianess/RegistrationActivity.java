package com.example.guardianess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {

    EditText inputname, inputemail, inputcontact, inputpassword, inputconpassword;
    CountryCodePicker countrypicker;
    Button signupbtn;
    TextView logintext;
    ProgressBar progressbar;
    FirebaseAuth signupAuth = FirebaseAuth.getInstance();
    private static final String TAG = "SignupActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        inputname = findViewById(R.id.inputname);
        inputemail = findViewById(R.id.inputemail);
        inputcontact = findViewById(R.id.inputcontact);
        inputpassword = findViewById(R.id.inputpassword);
        inputconpassword = findViewById(R.id.inputconpassword);
        countrypicker = findViewById(R.id.countrypicker);
        signupbtn = findViewById(R.id.signupbtn);
        logintext = findViewById(R.id.logintext);
        progressbar = findViewById(R.id.progressbar);


        logintext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputname.getText().toString();
                String email = inputemail.getText().toString();
                String mobile = inputcontact.getText().toString();
                String password = inputpassword.getText().toString();
                String conpassword = inputconpassword.getText().toString();
                if (TextUtils.isEmpty((name))) {
                    inputname.setError("Name is required");
                    inputname.requestFocus();
                } else if (TextUtils.isEmpty(email)) {
                    inputemail.setError("Email is required");
                    inputemail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    inputemail.setError("Invalid Email");
                    inputemail.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    inputpassword.setError("Password is required");
                    inputpassword.requestFocus();
                } else if (password.length() < 6) {
                    inputpassword.setError("Password is too weak");
                    inputpassword.requestFocus();
                } else if (TextUtils.isEmpty(conpassword)) {
                    inputconpassword.setError("Confirm password is required");
                    inputconpassword.requestFocus();
                } else if (!conpassword.equals(password)) {
                    inputconpassword.setError("Password didn't matches");
                    inputconpassword.requestFocus();
                } else {
                    SendOTP(mobile);
                    signupbtn.setEnabled(false);
                    signupbtn.setText("");
                    progressbar.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    private void SendOTP(String contact) {
        String name = inputname.getText().toString();
        String email = inputemail.getText().toString();
        String password = inputpassword.getText().toString();
        String countrycode = countrypicker.getSelectedCountryCode();
        String mobile = "+" + countrycode + contact;

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(mobile)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(RegistrationActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(RegistrationActivity.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                        signupbtn.setEnabled(true);
                        signupbtn.setText("Signup");
                        progressbar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationID, forceResendingToken);
                        Intent intent = new Intent(RegistrationActivity.this, VerificationActivity.class);
                        intent.putExtra("name",name);
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        intent.putExtra("mobile", mobile);
                        intent.putExtra("verificationID", verificationID);
                        startActivity(intent);
                        signupbtn.setEnabled(true);
                        signupbtn.setText("Signup");
                        progressbar.setVisibility(View.GONE);
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}