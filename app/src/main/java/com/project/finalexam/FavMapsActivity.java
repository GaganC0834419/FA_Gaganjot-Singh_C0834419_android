package com.project.finalexam;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.project.finalexam.databinding.ActivityFavMapsBinding;
import com.project.finalexam.db.DatabaseClient;
import com.project.finalexam.db.entities.AddExpense;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    @BindView(R.id.addressEt)
    TextInputEditText mAddressEt;
    @BindView(R.id.mUpdateLocationBtn)
    Button mUpdateLocationBtn;
    private GoogleMap mMap;
    private ActivityFavMapsBinding binding;
    String gettinglat, gettinglng, name;
    int id;
    double lat, lng;
    AddExpense obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fav_maps);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
             obj = (AddExpense) bundle.getSerializable("OBJ");

            gettinglat = obj.getLat();
            gettinglng = obj.getLng();
            name = obj.getName();
            id = obj.getId();

            lat = Double.parseDouble(gettinglng);
            lng = Double.parseDouble(gettinglng);

            /*gettinglat = bundle.getString("LATITUDE");
            gettinglng = bundle.getString("LONGITUDE");
            name = bundle.getString("NAME");
            id = bundle.getString("ID");

            lat = Double.parseDouble(gettinglng);
            lng = Double.parseDouble(gettinglng);*/


        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.myMap);
        mapFragment.getMapAsync(this);

        mAddressEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                //Create Intent
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(FavMapsActivity.this);
                startActivityForResult(intent, 100);

            }
        });

        mUpdateLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLocation(obj,lat,lng,getCompleteAddressString(lat,lng));

            }
        });
    }

    private void updateLocation(AddExpense obj, double lat, double lng, String completeAddressString) {

        class SaveExpense extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

               obj.setIsVisited("true");
               obj.setLat(String.valueOf(lat));
               obj.setLng(String.valueOf(lng));
               obj.setName(completeAddressString);


                DatabaseClient.getInstance(FavMapsActivity.this).getAppDatabase()
                        .addExpenseDao()
                        .update(obj);
                return null;

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(FavMapsActivity.this, "UPDATED", Toast.LENGTH_LONG).show();
                startActivity(new Intent(FavMapsActivity.this,FavouriteActivity.class));
                finish();
            }
        }

        SaveExpense saveExpense = new SaveExpense();
        saveExpense.execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            //initialise place
            Place place = Autocomplete.getPlaceFromIntent(data);
            mAddressEt.invalidate();
            mAddressEt.setText(place.getAddress());
            LatLng queriedLocation = place.getLatLng();
            lat = queriedLocation.latitude;
            lng = queriedLocation.longitude;
            Log.v("Latitude is", "" + queriedLocation.latitude);
            Log.v("Longitude is", "" + queriedLocation.longitude);

            mMap.clear();
            MarkerOptions markerOptions = new MarkerOptions().position(place.getLatLng()).title(place.getAddress()).draggable(true);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            mMap.addMarker(markerOptions);

            //saveToFavourites(place.getAddress(),lat,lng);


        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            //initialise status
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(FavMapsActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        LatLng latLng = new LatLng(Double.parseDouble(gettinglat), Double.parseDouble(gettinglng));
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(name);
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
        googleMap.addMarker(markerOptions);

        /*mAddressEt.invalidate();
        mAddressEt.setText(getCompleteAddressString(lat, lng));*/

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                // TODO Auto-generated method stub
                Log.e("Drag Start", marker.getPosition().latitude + "..." + marker.getPosition().longitude);
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // TODO Auto-generated method stub
                //Log.e("Drag End",marker.getPosition().latitude+"..."+marker.getPosition().longitude);

                //googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // TODO Auto-generated method stub
                Log.e("Drag End", marker.getPosition().latitude + "..." + marker.getPosition().longitude);

                mAddressEt.invalidate();
                mAddressEt.setText(getCompleteAddressString(marker.getPosition().latitude, marker.getPosition().longitude));

                lat = marker.getPosition().latitude;
                lng = marker.getPosition().longitude;

                mMap.clear();
                MarkerOptions markerOptions = new MarkerOptions().position(marker.getPosition()).title(getCompleteAddressString(marker.getPosition().latitude, marker.getPosition().longitude)).draggable(true);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
                mMap.addMarker(markerOptions);

                //saveToFavourites(getCompleteAddressString(marker.getPosition().latitude, marker.getPosition().longitude),lat,lng);

            }
        });

        //Initialise Places Api
        Places.initialize(FavMapsActivity.this, getString(R.string.google_maps_key));
        mAddressEt.setFocusable(false);

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.e("Current Address", strReturnedAddress.toString());
            } else {
                Log.e("Current Address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(FavMapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return strAdd;
    }
}