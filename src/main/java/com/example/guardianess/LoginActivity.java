package com.example.guardianess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    EditText login_email, login_password;
    Button login_button;
    ProgressBar progressbar;
    TextView signuptext;
    FirebaseAuth loginAuth = FirebaseAuth.getInstance();
    private static  final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        // Edit text Find view by ID's
        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        signuptext = findViewById(R.id.signuptext);
        login_button = findViewById(R.id.login_button);
        progressbar = findViewById(R.id.progressbar);

        signuptext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(i);
            }
        });
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = login_email.getText().toString();
                String password = login_password.getText().toString();
                if (TextUtils.isEmpty(email)){
                    login_email.setError("Please enter Email");
                    login_email.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    login_password.setError("Enter password");
                    login_password.requestFocus();
                }else {
                    login_button.setEnabled(false);
                    login_button.setText("");
                    progressbar.setVisibility(View.VISIBLE);
                    LoginUser(email,password);
                }
            }
        });

    }

    private void LoginUser(String email, String password) {
        loginAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "WelcomeBack", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    login_button.setEnabled(true);
                    login_button.setText("Login");
                    progressbar.setVisibility(View.GONE);
                }
                else {
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        login_email.setError("Email is not verified");
                        login_email.requestFocus();
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        login_password.setError("Invalid password");
                        login_password.requestFocus();
                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                    }
                    login_button.setEnabled(true);
                    login_button.setText("Login");
                    progressbar.setVisibility(View.GONE);
                }
            }
        });
    }
}