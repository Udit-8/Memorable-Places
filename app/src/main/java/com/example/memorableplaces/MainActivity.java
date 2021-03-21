package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> places;
    static ArrayList<LatLng> locations;
    static  ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces",Context.MODE_PRIVATE);
        ArrayList<String> lattitude = new ArrayList<>();
        ArrayList<String> longitude = new ArrayList<>();
        locations = new ArrayList<>();
        ListView memorableList = findViewById(R.id.memorableList);
        places = new ArrayList<>();
        places.clear();
        lattitude.clear();
        longitude.clear();
        locations.clear();
        try {
            places = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<String>())));
            lattitude = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("lattitude", ObjectSerializer.serialize(new ArrayList<String>())));
            longitude = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("longitude", ObjectSerializer.serialize(new ArrayList<String>())));
        }
        catch ( IOException e)
        {
            e.printStackTrace();
        }

        if(places.size() > 0 && longitude.size() > 0 && lattitude.size() > 0)
        {
            if(places.size() == longitude.size() && lattitude.size() == longitude.size())
            {
                for(int i = 0; i < lattitude.size(); i++)
                {
                    locations.add(new LatLng(Double.parseDouble(lattitude.get(i)),Double.parseDouble(longitude.get(i))));
                }
            }
        }

         else {
            places.add("Add a new place...");
            locations.add(new LatLng(0, 0));
        }
        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,places);
        memorableList.setAdapter(arrayAdapter);
        memorableList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("placeNumber",i);
                startActivity(intent);
            }
        });
    }
}
