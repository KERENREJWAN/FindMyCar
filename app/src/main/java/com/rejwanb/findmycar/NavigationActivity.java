package com.rejwanb.findmycar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private View navHeader;
    FirebaseAuth auth;
    private Button buttonSaveLocation;
    private TextView nameDisplay, emailDisplay;
    private String name, email;
    private FirebaseDatabase mFirebaseDatabase;
    double latitude = 0.0, longitude = 0.0;
    private Location currentLocation, prevLocation;
    private Marker marker;

    private DatabaseReference mLocationDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        auth = FirebaseAuth.getInstance();
        buttonSaveLocation = (Button) findViewById(R.id.buttonSaveLocation);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navHeader = navigationView.getHeaderView(0);

        nameDisplay = (TextView) navHeader.findViewById(R.id.name);
        emailDisplay = (TextView) navHeader.findViewById(R.id.email);
        name = auth.getCurrentUser().getDisplayName();
        email = auth.getCurrentUser().getEmail();
        nameDisplay.setText(name);
        emailDisplay.setText(email);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mLocationDatabaseReference = mFirebaseDatabase.getReference().child(auth.getCurrentUser().getUid());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocationDatabaseReference.child("latitude").setValue(location.getLatitude());
                mLocationDatabaseReference.child("longitude").setValue(location.getLongitude());
                if (currentLocation != null) {
                    prevLocation = currentLocation;
                } else
                    prevLocation = location;
                currentLocation = location;
               if (marker != null)
               // if (latitude != location.getLatitude() && longitude != location.getLongitude())
                    marker.remove();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        configureButton();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configureButton();
                break;
            default:
                break;
        }
    }

    private void configureButton() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }

        buttonSaveLocation.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
                if (marker != null)
               // if (latitude != 0.0 && longitude != 0.0)
                    marker.remove();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        if (auth.getCurrentUser() == null)
            finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(NavigationActivity.this, SettingsActivity.class);
        }
        else if (id == R.id.action_guide)
        {

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
            startActivity(new Intent(NavigationActivity.this, UserInfoActivity.class));

        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Hi, I would like to share my car's location with you. " + "http://maps.google.com/maps?saddr=" + latitude + "," + longitude);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        } else if (id == R.id.nav_lastLoc) {
             LatLng prev = new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude());
             Marker prevMarker = mMap.addMarker(new MarkerOptions().position(prev).title("Car's Previous Location"));
             prevMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        } else if (id == R.id.signout) {
            auth.signOut();
            Toast.makeText(NavigationActivity.this, "signing out..", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent i = new Intent(NavigationActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }, 2000);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public interface MyCallback {
        void onCallback(double latitude, double longitude);
    }

    public void readData(final MyCallback myCallback) {
        mLocationDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("latitude").exists() && dataSnapshot.child("longitude").exists()) {
                    latitude = (Double) dataSnapshot.child("latitude").getValue();
                    longitude = (Double) dataSnapshot.child("longitude").getValue();
                    myCallback.onCallback(latitude, longitude);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                myCallback.onCallback(0.0, 0.0);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        readData(new MyCallback() {
            @Override
            public void onCallback(double lat, double lng) {
                if (lat != 0.0 && lng != 0.0) {
                    LatLng location1 = new LatLng(lat, lng);
                    marker = mMap.addMarker(new MarkerOptions().position(location1).title("Car's Location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 17.0f));
                } else {
                   LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                   marker = mMap.addMarker(new MarkerOptions().position(location).title("Car's Location"));
                   mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17.0f));
                }
            }
        });
    }
}


//https://github.com/pR0Ps/LocationShare - how to share location
//https://stackoverflow.com/questions/44977332/how-to-retrieve-location-which-is-saved-using-geofire-on-real-time-database-fire
////https://stackoverflow.com/questions/38340949/how-to-save-geofire-coordinates-along-with-other-items-in-firebase-database