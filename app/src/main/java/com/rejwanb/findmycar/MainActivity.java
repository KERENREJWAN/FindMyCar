package com.rejwanb.findmycar;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {
    private Button buttonGiveLocation;
    private TextView entrance;
    private Button buttonGetLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    double Latitude, Longitude;
    private DatabaseReference carLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonGiveLocation = (Button) findViewById(R.id.buttonGiveLocation);
        entrance = (TextView) findViewById(R.id.entrance);
        buttonGetLocation = (Button) findViewById(R.id.buttonGetLocation);
        carLocation = FirebaseDatabase.getInstance().getReference();

        buttonGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, NavigationActivity.class);
                Bundle b = new Bundle();

                b.putDouble("Latitude", Latitude);
                b.putDouble("Longitude", Longitude);
                i.putExtras(b);
                if (b!=null)
                    startActivity(i);
            }
        });
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                entrance.append("\n " + location.getLatitude() + " " + location.getLongitude());
                Latitude = location.getLatitude();
                Longitude = location.getLongitude();
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
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                    configureButton();
                    break;
                    default:
                        break;
        }
    };

    private void configureButton() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }

        buttonGiveLocation.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,0,locationListener);
            }
        });
    }
}
//https://github.com/miskoajkula/Gps_location- the code
//https://www.androidhive.info/2016/10/android-working-with-firebase-realtime-database/ ---?
// https://www.youtube.com/watch?v=AyGK4O9m2hA&index=10&list=PLGCjwl1RrtcSi2oV5caEVScjkM6r3HO9t - firebase explanation
// https://www.raywenderlich.com/5114-firebase-tutorial-for-android-getting-started - sign up and data base - app called whySoSerious
//https://www.androidhive.info/2013/11/android-sliding-menu-using-navigation-drawer/ - navigation