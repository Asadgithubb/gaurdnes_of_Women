package com.example.guardianess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guardianess.classes.ReadWriteUserDetail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VerificationActivity extends AppCompatActivity {

    TextView codemessage;
    EditText inputOTP;
    Button registerbtn;
    ProgressBar progressbar;
    String name,mobile,email,password,verificationID;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);


        codemessage = findViewById(R.id.codemessage);
        inputOTP = findViewById(R.id.inputOTP);
        registerbtn = findViewById(R.id.registertn);
        progressbar = findViewById(R.id.progressbar);

        Intent intent = getIntent();
        if (intent!=null){
            name = intent.getStringExtra("name");
            mobile = intent.getStringExtra("mobile");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            verificationID = intent.getStringExtra("verificationID");
        }
        codemessage.setText("Enter code we sent to "+mobile);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredOTP = inputOTP.getText().toString();
                if (TextUtils.isEmpty(enteredOTP)) {
                    inputOTP.setError("Enter OTP");
                    inputOTP.requestFocus();
                }
                else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(enteredOTP,verificationID);
                    SignupUser(credential);
                    registerbtn.setEnabled(false);
                    registerbtn.setText("");
                    progressbar.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void SignupUser(PhoneAuthCredential credential) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    ReadWriteUserDetail writeDetails = new ReadWriteUserDetail(name,email,password,mobile);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UserDetails");
                    reference.child(firebaseUser.getUid()).setValue(writeDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
                                startActivity(intent);
                                registerbtn.setEnabled(true);
                                registerbtn.setText("Register");
                                progressbar.setVisibility(View.GONE);
                            }
                            else {
                                Toast.makeText(VerificationActivity.this, "OTP verification failed. Please try again", Toast.LENGTH_SHORT).show();
                                registerbtn.setEnabled(true);
                                registerbtn.setText("Register");
                                progressbar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });

    }
}