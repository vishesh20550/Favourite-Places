package com.example.favplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.favplaces.databinding.ActivityMapsBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.favplaces.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }


    public void centerOnMap(Location location, String title){
        if(location!=null){
            mMap.clear();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        }
        else{
            Toast.makeText(this, "GPS can't locate :(\nCheck your internet connection or try moving around", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerOnMap(lastKnownLocation,"Your Location :)");
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent= getIntent();
        int placeNumber= intent.getIntExtra("placeNumber",0);
        if(placeNumber==0){
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = location -> centerOnMap(location,"Your Location :)");
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerOnMap(lastKnownLocation,"Your Location :)");
            }
        }
        else{
            Location placeLocation= new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).longitude);
            centerOnMap(placeLocation,MainActivity.places.get(intent.getIntExtra("placeNumber",0)));
        }
    }


    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (listAddresses != null && listAddresses.size() > 0) {
                if (listAddresses.get(0).getThoroughfare() != null) {
                    if (listAddresses.get(0).getSubThoroughfare() != null) {
                        address += listAddresses.get(0).getSubThoroughfare() + " ";
                    }
                    address += listAddresses.get(0).getThoroughfare();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error:(", Toast.LENGTH_SHORT).show();
        }
        if (address.equals("")) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");
            address += sdf.format(new Date());
        }
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();
        SharedPreferences sharedPreferences= getSharedPreferences("com.example.favplaces",Context.MODE_PRIVATE);

        try{
            ArrayList<String> latitudes= new ArrayList<>();
            ArrayList<String> longitudes= new ArrayList<>();
            for (LatLng coord: MainActivity.locations){
                longitudes.add(Double.toString(coord.longitude));
                latitudes.add(Double.toString(coord.latitude));
            }
            sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places)).apply();
            sharedPreferences.edit().putString("lats",ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longs",ObjectSerializer.serialize(longitudes)).apply();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Toast.makeText(this, "Location Saved!!", Toast.LENGTH_SHORT).show();
    }
}