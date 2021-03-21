package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }
    }

    public void centerMapOnLocation(Location location, String title)
    {
        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.clear();
        if(title != "Your Location")
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        if(intent.getIntExtra("placeNumber",0) == 0)
        {
            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLocation(location,"Your Location");
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                else
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Location curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    centerMapOnLocation(curLocation,"Your Location");
                }
        }
        else
        {
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).latitude);
            location.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).longitude);
            centerMapOnLocation(location,MainActivity.places.get(intent.getIntExtra("placeNumber",0)));
        }
        // Add a marker in Sydney and move the camera
    }
    public void onMapLongClick(LatLng point)
    {
        mMap.clear();
        String address = "";
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            if(addressList!= null && addressList.size() > 0)
            {

                if(addressList.get(0).getLocality() != null)
                {
                    if(addressList.get(0).getSubLocality() != null)
                    {
                        address += addressList.get(0).getSubLocality();
                    }
                    addressList.get(0).getLocality();
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i("error","not working");
        }
        if(address == "")
        {
            SimpleDateFormat sdf = new SimpleDateFormat("mm:HH dd-MM-YYYY");
            address = sdf.format(new Date());
        }
        MainActivity.places.add(address);
        MainActivity.locations.add(point);
        MainActivity.arrayAdapter.notifyDataSetChanged();
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces",Context.MODE_PRIVATE);
        try {
            sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> lattitude = new ArrayList<>();
        ArrayList<String> longitude = new ArrayList<>();
        for(LatLng coord : MainActivity.locations)
        {
            lattitude.add(Double.toString(coord.latitude));
            longitude.add(Double.toString(coord.longitude));
        }
        try {
            sharedPreferences.edit().putString("lattitude",ObjectSerializer.serialize(lattitude)).apply();
            sharedPreferences.edit().putString("longitude",ObjectSerializer.serialize(longitude)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }


        mMap.addMarker(new MarkerOptions().position(point).title(address));
        Toast.makeText(this,"Location Saved",Toast.LENGTH_LONG).show();
    }
}
