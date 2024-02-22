package com.example.guardianess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowContact extends AppCompatActivity {

    TextView primaryContactTextView, secondaryContact1TextView, secondaryContact2TextView,
            secondaryContact3TextView, secondaryContact4TextView;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_contact);

        primaryContactTextView = findViewById(R.id.primaryContactTextView);
        secondaryContact1TextView = findViewById(R.id.secondaryContact1TextView);
        secondaryContact2TextView = findViewById(R.id.secondaryContact2TextView);
        secondaryContact3TextView = findViewById(R.id.secondaryContact3TextView);
        secondaryContact4TextView = findViewById(R.id.secondaryContact4TextView);

        // Fetch contacts from Firebase and set them in TextViews
        fetchContacts();
    }

    private void fetchContacts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("EmergencyContacts").child(userId);

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String primaryContact = dataSnapshot.child("PrimaryContact").getValue(String.class);
                        ArrayList<String> secondaryContacts = (ArrayList<String>) dataSnapshot.child("SecondaryContact").getValue();

                        // Set contacts in TextViews
                        primaryContactTextView.setText(primaryContact);
                        if (secondaryContacts != null && secondaryContacts.size() >= 4) {
                            secondaryContact1TextView.setText(secondaryContacts.get(0));
                            secondaryContact2TextView.setText(secondaryContacts.get(1));
                            secondaryContact3TextView.setText(secondaryContacts.get(2));
                            secondaryContact4TextView.setText(secondaryContacts.get(3));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if any
                }
            });
        }
    }
}