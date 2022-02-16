package com.project.finalexam;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.project.finalexam.db.DatabaseClient;
import com.project.finalexam.db.entities.AddExpense;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {


    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.nav_view)
    NavigationView mNavView;

    @BindView(R.id.menuImg)
    LinearLayout mMenuImg;

    @BindView(R.id.addressEt)
    TextInputEditText mAddressEt;

    @BindView(R.id.currentPositionIMg)
    ImageView currentPositionIMg;

    @BindView(R.id.mMapTypeBtn)
    Button mMapTypeBtn;

    @BindView(R.id.mCurrentLocationBtn)
    Button mCurrentLocationBtn;

    private GoogleMap mMap;
    double lat, lng;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    SharedPreference sharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);


        sharedPreference=new SharedPreference(MapsActivity.this);
        mNavView.setNavigationItemSelectedListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            lat = bundle.getDouble("Lat");
            lng = bundle.getDouble("Lng");
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.myMap);
        mapFragment.getMapAsync(this);

        mMenuImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }

            }
        });

        mAddressEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                //Create Intent
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(MapsActivity.this);
                startActivityForResult(intent, 100);

            }
        });

        mMapTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapTypeDialog();
            }
        });

        mCurrentLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fetchLocation();

                mMap.clear();

                LatLng latLng = new LatLng(Double.parseDouble(SharedPreference.getLatitude()), Double.parseDouble(SharedPreference.getLongitude()));
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!");
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                mMap.addMarker(markerOptions);

                mAddressEt.invalidate();
                mAddressEt.setText(getCompleteAddressString(Double.parseDouble(SharedPreference.getLatitude()), Double.parseDouble(SharedPreference.getLongitude())));

            }
        });
    }


    private void showMapTypeDialog() {
        Dialog alertDialog = new Dialog(MapsActivity.this);
        ;
        View view = getLayoutInflater().inflate(R.layout.dialog_layout, null);

        TextView mHybrid = view.findViewById(R.id.mHybrid);
        TextView mTerrain = view.findViewById(R.id.mTerrain);
        TextView mSatellite = view.findViewById(R.id.mSatellite);
        TextView mRoadmap = view.findViewById(R.id.mRoadmap);
        TextView mNone = view.findViewById(R.id.mNone);

        mHybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                alertDialog.dismiss();
            }
        });

        mTerrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                alertDialog.dismiss();
            }
        });

        mSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                alertDialog.dismiss();
            }
        });

        mRoadmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                alertDialog.dismiss();

            }
        });
        mNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                alertDialog.dismiss();

            }
        });


        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //this line MUST BE BEFORE setContentView
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(true);
        alertDialog.setContentView(view);
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        LatLng latLng = new LatLng(lat, lng);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
        googleMap.addMarker(markerOptions);

        mAddressEt.invalidate();
        mAddressEt.setText(getCompleteAddressString(lat, lng));

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.e("Drag Start", marker.getPosition().latitude + "..." + marker.getPosition().longitude);
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // TODO Auto-generated method stub
                Log.e("Drag End", marker.getPosition().latitude + "..." + marker.getPosition().longitude);

                mAddressEt.invalidate();
                mAddressEt.setText(getCompleteAddressString(marker.getPosition().latitude, marker.getPosition().longitude));

                lat = marker.getPosition().latitude;
                lng = marker.getPosition().longitude;

                mMap.addMarker(new MarkerOptions()
                        .position(marker.getPosition())
                        .title(getCompleteAddressString(lat,lng))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                saveToFavourites(getCompleteAddressString(lat, lng),lat,lng);

                /*mMap.clear();
                MarkerOptions markerOptions = new MarkerOptions().position(marker.getPosition()).title("I am here!").draggable(true);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
                mMap.addMarker(markerOptions);

                saveToFavourites(getCompleteAddressString(marker.getPosition().latitude, marker.getPosition().longitude),lat,lng);
*/
            }
        });

        //Initialise Places Api
        Places.initialize(MapsActivity.this, getString(R.string.google_maps_key));
        mAddressEt.setFocusable(false);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                double lat = latLng.latitude;
                double lng = latLng.longitude;
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getCompleteAddressString(lat,lng))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                saveToFavourites(getCompleteAddressString(lat, lng),lat,lng);


            }
        });

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
            Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return strAdd;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_home) {
            recreate();
        } else if (id == R.id.nav_fav) {
            startActivity(new Intent(MapsActivity.this,FavouriteActivity.class));

        }
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        return true;
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
            MarkerOptions markerOptions = new MarkerOptions().position(place.getLatLng()).title(place.getAddress()).draggable(false);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            mMap.addMarker(markerOptions);
            
            saveToFavourites(place.getAddress(),lat,lng);


        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            //initialise status
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(MapsActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToFavourites(String address, double lat, double lng) {
        class SaveExpense extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                AddExpense addIncome = new AddExpense(address,String.valueOf(lat),String.valueOf(lng),"false");

                //adding to database
                DatabaseClient.getInstance(MapsActivity.this).getAppDatabase()
                        .addExpenseDao()
                        .insert(addIncome);
                return null;

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(MapsActivity.this, "Your data has been saved successfully", Toast.LENGTH_LONG).show();

            }
        }

        SaveExpense saveExpense = new SaveExpense();
        saveExpense.execute();

    }
}