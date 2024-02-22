package com.example.guardianess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.guardianess.classes.LocationHelper;
import com.example.guardianess.classes.ShakeDetector;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

interface ContactsExistCallback {
    void onContactsExist(boolean contactsExist);
}
public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    Button contact, makecall, sendmessage, locationbtn,instruction,profile;
    String primaryContact;
    private static final int CALL_PERMISSION_REQUEST_CODE = 123;
    private static final int SMS_PERMISSION_REQUEST_CODE = 321;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private ShakeDetector shakeDetector;
    DrawerLayout drawerLayout;
    NavigationView navigationview;
    Toolbar toolbar;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contact = findViewById(R.id.contact);
        makecall = findViewById(R.id.makecall);
        sendmessage = findViewById(R.id.sendMessage);
        locationbtn = findViewById(R.id.locationbtn);
        profile = findViewById(R.id.profile);
        instruction = findViewById(R.id.instruction);

        mAuth = FirebaseAuth.getInstance();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        shakeDetector = new ShakeDetector();
        shakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                // Get the live location and include it in the message
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    LocationHelper locationHelper = new LocationHelper(MainActivity.this);
                    locationHelper.startLocationUpdates(new LocationHelper.LocationListener() {
                        @Override
                        public void onLocationUpdate(Location location) {
                            // Generate a message with live location information
                            String shakeMessage = "Emergency! I need help. My current location: " +
                                    "Latitude: " + location.getLatitude() +
                                    ", Longitude: " + location.getLongitude();
                            initiateMessageWithPermission(shakeMessage);
                            // Stop location updates after sending the message
                            locationHelper.stopLocationUpdates();
                        }
                    });
                } else {
                    // Request location permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        instruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SafetyInstruction.class);
                startActivity(intent);
            }
        });
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkContactsExist(new ContactsExistCallback() {
                    @Override
                    public void onContactsExist(boolean contactsExist) {
                        if (contactsExist) {
                            Intent intent = new Intent(MainActivity.this, ShowContact.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(MainActivity.this, AddEmergencyContact.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
        makecall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateEmergencyCall();
            }
        });
        sendmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmergencyMessage();
            }
        });
        locationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(shakeDetector,accelerometerSensor,sensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeDetector);
    }

    private void initiateEmergencyCall() {
        getPrimaryContact();
        if (primaryContact != null){
            initiateCallWithPermission();
        }
    }

    private void sendEmergencyMessage() {
        getPrimaryContact();
        if (primaryContact != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // Get the current location
                LocationHelper locationHelper = new LocationHelper(this);
                locationHelper.startLocationUpdates(new LocationHelper.LocationListener() {
                    @Override
                    public void onLocationUpdate(Location location) {
                        String locationLink = "https://maps.google.com/?q=" +
                                location.getLatitude() + "," + location.getLongitude();
                        String shakeMessage = "Emergency! I need help. My current location: " + locationLink;
                        initiateMessageWithPermission(shakeMessage);
                        // Stop location updates after sending the message
                        locationHelper.stopLocationUpdates();
                    }
                });

            } else {
                // Request location permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    //---------------------------------METHOD TO INITIATE EMERGENCY CALL
    private void initiateCallWithPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + primaryContact));
            startActivity(callIntent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
        }
    }
    private void initiateMessageWithPermission(String message) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(primaryContact, null, message, null, null);
            Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        }
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            initiateEmergencyCall();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            initiateEmergencyCall();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Try initiating the call again
                initiateEmergencyCall();
            } else {
                // If permission not granted
                Toast.makeText(MainActivity.this, "Call permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == SMS_PERMISSION_REQUEST_CODE){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                sendEmergencyMessage();
            }
            else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getPrimaryContact(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserID = currentUser.getUid();
        DatabaseReference contactRef = database.getReference("EmergencyContacts").child(currentUserID);
        contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    primaryContact = snapshot.child("PrimaryContact").getValue(String.class);
                }
                else {
                    Toast.makeText(MainActivity.this, "Primary contact does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void checkContactsExist(final ContactsExistCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("EmergencyContacts").child(userId);

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        callback.onContactsExist(true);
                    } else {
                        callback.onContactsExist(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if any
                    callback.onContactsExist(false);
                }
            });
        } else {
            // Default to false if the user is not logged in
            callback.onContactsExist(false);
        }
    }

    private void updateUI(boolean contactsExist) {
        if (contactsExist) {
            contact.setText("Show Contacts");
        } else {
            contact.setText("Add Contact");
        }
    }
}
