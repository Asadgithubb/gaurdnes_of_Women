package com.example.guardianess;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private EditText nameEditText, emailEditText, contactEditText;
    private ImageView profileImageView;
    private Button saveButton;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        contactEditText = view.findViewById(R.id.contactEditText);
        profileImageView = view.findViewById(R.id.profileImageView);
        saveButton = view.findViewById(R.id.saveButton);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("UserDetails").child(user.getUid());
            storageReference = FirebaseStorage.getInstance().getReference("profile_images").child(user.getUid());

            // Retrieve user data from Firebase and populate the EditText fields
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String contact = snapshot.child("mobile").getValue(String.class);

                    nameEditText.setText(name);
                    emailEditText.setText(email);
                    contactEditText.setText(contact);

                    // Load the profile image using the URL stored in the database
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    // Use Picasso to load the image
                    loadProfileImage(profileImageUrl);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle database error
                }
            });

            // Handle image selection from the gallery
            profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openImagePicker();
                }
            });

            // Handle save button click
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveUserProfile();
                }
            });
        }

        return view;
    }

    private void saveUserProfile() {
        // Update user profile in Firebase
        String newName = nameEditText.getText().toString();
        String newEmail = emailEditText.getText().toString();
        String newContact = contactEditText.getText().toString();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && selectedImageUri != null) {
            // Update email in Firebase Authentication
            user.updateEmail(newEmail)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Email updated successfully in Authentication, now update in Database
                            updateProfileInDatabase(user.getUid(), newName, newEmail, newContact);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle the failure
                            Toast.makeText(getActivity(), "Failed to update email", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateProfileInDatabase(final String userId, final String newName, final String newEmail, final String newContact) {
        // Update user data in the database
        databaseReference.child("name").setValue(newName);
        databaseReference.child("email").setValue(newEmail);
        databaseReference.child("mobile").setValue(newContact);

        // Upload image to Firebase Storage
        uploadImageToStorage(userId, newName, newEmail, newContact, selectedImageUri);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void uploadImageToStorage(final String userId, final String newName, final String newEmail, final String newContact, Uri imageUri) {
        if (userId != null) {
            // Define the path where the image will be stored in Firebase Storage
            final StorageReference imageRef = storageReference.child(userId).child("profile_image.jpg");

            // Upload image to Firebase Storage
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get the download URL
                            imageRef.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri downloadUri) {
                                            // Update user data in the database with the new profile image URL
                                            databaseReference.child("profileImageUrl").setValue(downloadUri.toString());

                                            Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle the failure
                                            Toast.makeText(getActivity(), "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle the failure
                            Toast.makeText(getActivity(), "Failed to upload profile image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadProfileImage(String imageUrl) {
        // Use Picasso to load the image into the ImageView
        Picasso.get().load(imageUrl).into(profileImageView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            // Display the selected image in the ImageView
            profileImageView.setImageURI(selectedImageUri);
        }
    }
}