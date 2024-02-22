package com.example.guardianess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.HashMap;

public class AddEmergencyContact extends AppCompatActivity {

    EditText[] inputphones = new EditText[5];
    RadioGroup radioGroupPrimary;
    RadioButton[] radiobtn = new RadioButton[5];
    CountryCodePicker[] countrypicker = new CountryCodePicker[5];
    Button savebtn;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emergency_contact);

        inputphones[0] = findViewById(R.id.inputphone1);
        inputphones[1] = findViewById(R.id.inputphone2);
        inputphones[2] = findViewById(R.id.inputphone3);
        inputphones[3] = findViewById(R.id.inputphone4);
        inputphones[4] = findViewById(R.id.inputphone5);

        radiobtn[0] = findViewById(R.id.radiobtn1);
        radiobtn[1] = findViewById(R.id.radiobtn2);
        radiobtn[2] = findViewById(R.id.radiobtn3);
        radiobtn[3] = findViewById(R.id.radiobtn4);
        radiobtn[4] = findViewById(R.id.radiobtn5);

        countrypicker[0] = findViewById(R.id.countrypicker1);
        countrypicker[1] = findViewById(R.id.countrypicker2);
        countrypicker[2] = findViewById(R.id.countrypicker3);
        countrypicker[3] = findViewById(R.id.countrypicker4);
        countrypicker[4] = findViewById(R.id.countrypicker5);

        radioGroupPrimary = findViewById(R.id.radioGroupPrimary);
        savebtn = findViewById(R.id.savebtn);


        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> countryCodes = new ArrayList<>();
                for (int i = 0; i < countrypicker.length; i++) {
                    countryCodes.add(countrypicker[i].getSelectedCountryCodeWithPlus());
                }

                ArrayList<String> contact = new ArrayList<>();
                for (int i = 0; i < inputphones.length; i++) {
                    String PhoneNumber = countryCodes.get(i) + inputphones[i].getText().toString();
                    contact.add(PhoneNumber);
                }

                for (int i = 0; i < inputphones.length; i++) {
                    if (TextUtils.isEmpty(contact.get(i))) {
                        inputphones[i].setError("Contact Number is required");
                        inputphones[i].requestFocus();
                    }
                }
                // Get number of selected radio button
                int RadioId = radioGroupPrimary.getCheckedRadioButtonId();
                // give id to primary contact equal to selected radio button id
                int PrimaryContactId = getIDFromIndex(RadioId);
                String primaryContact = null;
                ArrayList<String> secondaryContacts = new ArrayList<>();
                if (RadioId != -1) {
                    primaryContact = countryCodes.get(PrimaryContactId) + inputphones[PrimaryContactId].getText().toString();

                    for (int j = 0; j < 5; j++) {
                        if (j != PrimaryContactId) {
                            secondaryContacts.add(countryCodes.get(j) + inputphones[j].getText().toString());
                        }
                    }
                }
                SaveContact(primaryContact, secondaryContacts);
            }
        });
    }

    private void SaveContact(String primaryContact, ArrayList<String> secondaryContacts) {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("EmergencyContacts").child(userId);

            HashMap<String, Object> contactMap = new HashMap<>();
            contactMap.put("PrimaryContact", primaryContact);
            contactMap.put("SecondaryContact", secondaryContacts);
            reference.setValue(contactMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddEmergencyContact.this, "Contacts added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddEmergencyContact.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private int getIDFromIndex(int radioId) {
        for (int i = 0; i < radiobtn.length; i++) {
            if (radiobtn[i].getId() == radioId) {
                return i;
            }
        }
        return -1;
    }
}