package sg.edu.rp.c346.p09gettingmylocations;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private GoogleMap map;

    TextView tvLong, tvLat, tvCurrLat, tvCurrLong;
    Button btnCheck, btnStart, btnStop;

    String prefLat, prefLong;
    Marker myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCheck = (Button) this.findViewById(R.id.btnCheck);
        btnStart = (Button) this.findViewById(R.id.btnStart);
        btnStop = (Button) this.findViewById(R.id.btnStop);
        tvLong = (TextView) this.findViewById(R.id.tvLongitude);
        tvLat = (TextView) this.findViewById(R.id.tvLatitude);
        tvCurrLat = (TextView) this.findViewById(R.id.tvCurrLattitude);
        tvCurrLong = (TextView) this.findViewById(R.id.tvCurrLongitude);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        tvCurrLat.setText(prefLat);
        tvCurrLong.setText(prefLong);

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
                    map.setMyLocationEnabled(true);

                    UiSettings ui = map.getUiSettings();
                    ui.setCompassEnabled(true);
                    ui.setZoomControlsEnabled(true);

                    LatLng currLocation = new LatLng(Double.parseDouble(prefLat), Double.parseDouble(prefLong));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocation, 18));

                    myLocation = map.addMarker(new
                            MarkerOptions()
                            .position(currLocation)
                            .title("My Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                } else {
                    Log.e("GMap - Permission", "GPS access has not been granted");
                }
            }
        });


        int permissionCheckStorage = PermissionChecker.checkSelfPermission
                (MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionCheckFineLocation = PermissionChecker.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheckStorage != PermissionChecker.PERMISSION_GRANTED && permissionCheckFineLocation != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 0);

//            // stops the action from proceeding further as permission not
            //  granted yet
            return;
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                startService(i);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                stopService(i);
            }
        });

    }

    protected void onResume() {
        super.onResume();
        SharedPreferences prefsLat = PreferenceManager
                .getDefaultSharedPreferences(this);
        prefLat = prefsLat.getString("lat", "0.0");
        tvLat.setText("Latitude: " + prefLat);

        SharedPreferences prefsLong = PreferenceManager
                .getDefaultSharedPreferences(this);
        prefLong = prefsLong.getString("long", "0.0");
        tvLong.setText("Longitude: " + prefLong);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest
                    .PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setSmallestDisplacement(100);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);

        } else {
            mLocation = null;
            Toast.makeText(MainActivity.this,
                    "Permission not granted to retrieve location info",
                    Toast.LENGTH_SHORT).show();
        }

        if (mLocation != null) {
            Toast.makeText(this, "Lat : " + mLocation.getLatitude() +
                            " Lng : " + mLocation.getLongitude(),
                    Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Location not Detected",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        tvLat.setText(String.valueOf(location.getLatitude()));
        tvLong.setText(String.valueOf(location.getLongitude()));

        myLocation.remove();

        LatLng currLocation = new LatLng(location.getLatitude(), location.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocation, 18));

        myLocation = map.addMarker(new
                MarkerOptions()
                .position(currLocation)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        tvCurrLat.setText("Latitude: " + String.valueOf(location.getLatitude()));
        tvCurrLong.setText("Longitude: " + String.valueOf(location.getLongitude()));

    }
}
