package com.example.guardianess;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.Manifest;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.guardianess.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private final long min_time = 1000;
    private final float min_dis = 5;
    private Polyline polyline;
    private List<LatLng> pathPoints = new ArrayList<>();
    private Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Request necessary permissions
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.INTERNET
        }, PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker at the initial position and move the camera
        LatLng initialPosition = new LatLng(0, 0);
        currentLocationMarker = mMap.addMarker(new MarkerOptions().position(initialPosition).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialPosition));

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                try {
                    LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // Update marker position for the current location
                    currentLocationMarker.setPosition(newLatLng);

                    // Move camera to the current location with smooth animation
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f));

                    // Add the current location to the pathPoints list
                    pathPoints.add(newLatLng);

                    // Draw polyline to track movement
                    if (polyline != null) {
                        polyline.remove();
                    }
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(pathPoints)
                            .width(5f)
                            .color(Color.BLUE);
                    polyline = mMap.addPolyline(polylineOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Handle status changes if needed
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                // Handle provider enabled
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                // Handle provider disabled
            }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, min_time, min_dis, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, min_time, min_dis, locationListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
