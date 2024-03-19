package com.example.proform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.Manifest;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeTransporter extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private TextView locationTextView;
    private FusedLocationProviderClient fusedLocationClient;
    private BottomNavigationView bottomNavigationView;
    private CardView orderCardView, administrationCardView, delivredCardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_transporter);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home11);
        orderCardView = findViewById(R.id.orderCardView);
        administrationCardView = findViewById(R.id.administrationCardView);
        delivredCardView = findViewById(R.id.delivredCardView);
        locationTextView = findViewById(R.id.location);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestLocation();
        }

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // if (item.getItemId() == R.id.like22) {
                //   return true;
                // } else if (item.getItemId() == R.id.cart11) {
                // return true;
                //}
                //else
                int itemId = item.getItemId();
                if (itemId == R.id.home11 || itemId == R.id.profile11) {
                    return true;
                }
                return false;
            }

        });
        orderCardView.setOnClickListener(v -> {
            Intent intent = new Intent(this, orders.class);
            startActivity(intent);
        });

        administrationCardView.setOnClickListener(v -> {
            Intent intent = new Intent(this, administration.class);
            startActivity(intent);
        });

        delivredCardView.setOnClickListener(v -> {
            Intent intent = new Intent(this, delivred.class);
            startActivity(intent);
        });
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission granted, retrieve location
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(HomeTransporter.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (!addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                // Construct the city and country string
                                String city = address.getLocality();
                                String country = address.getCountryName();
                                String locationString = city + ", " + country;
                                locationTextView.setText(locationString);
                            } else {
                                locationTextView.setText("Location not available");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            locationTextView.setText("Error retrieving location");
                        }
                    } else {
                        locationTextView.setText("Location not available");
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, retrieve location
                requestLocation();
            } else {
                // Permission denied, inform the user
                locationTextView.setText("Location permission denied");
            }
        }
    }


}