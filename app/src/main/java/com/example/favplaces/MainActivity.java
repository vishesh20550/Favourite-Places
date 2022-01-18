package com.example.favplaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    static ArrayAdapter<String> arrayAdapter;
    static ArrayList<String> places= new ArrayList<>();
    static ArrayList<LatLng> locations= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listview);

        SharedPreferences sharedPreferences= getSharedPreferences("com.example.favplaces", Context.MODE_PRIVATE);

        ArrayList<String> latitudes= new ArrayList<>();
        ArrayList<String> longitudes= new ArrayList<>();
        places.clear();
        latitudes.clear();
        longitudes.clear();
        locations.clear();
        try {
            places= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lats",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longs",ObjectSerializer.serialize(new ArrayList<String>())));
        }catch (Exception e){
            e.printStackTrace();
        }
        if(places.size()>0 && longitudes.size()>0 && latitudes.size()>0){
            if(places.size()==longitudes.size() && places.size()==latitudes.size()){
                for(int i=0;i<places.size();i++){
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }
        }
        else{
            places.add("Tap here to add a new place");
            locations.add(new LatLng(0,0));
        }
        arrayAdapter= new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,places);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent= new Intent(getApplicationContext(),MapsActivity.class);
            intent.putExtra("placeNumber",position);
            startActivity(intent);
        });
    }
}