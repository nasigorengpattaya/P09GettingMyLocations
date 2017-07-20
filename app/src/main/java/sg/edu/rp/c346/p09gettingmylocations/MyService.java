package sg.edu.rp.c346.p09gettingmylocations;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MyService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    String folderLocation;

    boolean started;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        folderLocation = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/Location";

        File folder = new File(folderLocation);
        if (folder.exists() == false) {
            boolean result = folder.mkdir();
            if (result == true) {
                Log.d("File Read/Write", "Folder created");
            }
        }
        Log.d("Service", "Created");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MyService.this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MyService.this, android.Manifest.permission.ACCESS_FINE_LOCATION);

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
            Toast.makeText(MyService.this,
                    "Permission not granted to retrieve location info",
                    Toast.LENGTH_SHORT).show();
        }

        if (mLocation != null) {

            File targetFile = new File(folderLocation, "data.txt");

            try {
                FileWriter writer = new FileWriter(targetFile, true);
                writer.write(mLocation.getLatitude() + ", " + mLocation.getLongitude() + "\n");
                writer.flush();
                writer.close();

                if (targetFile.exists() == true) {
                    String data = "";
                    try {
                        FileReader reader = new FileReader(targetFile);
                        BufferedReader br = new BufferedReader(reader);
                        String line = br.readLine();
                        while (line != null) {
                            data += line + "\n";
                            line = br.readLine();
                        }
                        br.close();
                        reader.close();
                    } catch (Exception e) {
                        Toast.makeText(MyService.this, "Failed to read!",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    SharedPreferences prefsLat = PreferenceManager
                            .getDefaultSharedPreferences(MyService.this);
                    SharedPreferences.Editor prefEditLat = prefsLat.edit();
                    prefEditLat.putString("lat", String.valueOf(mLocation.getLatitude()));
                    prefEditLat.commit();

                    SharedPreferences prefsLong = PreferenceManager
                            .getDefaultSharedPreferences(MyService.this);
                    SharedPreferences.Editor prefEditLong = prefsLong.edit();
                    prefEditLong.putString("long", String.valueOf(mLocation.getLongitude()));
                    prefEditLong.commit();

                    Toast.makeText(MyService.this, data, Toast.LENGTH_LONG).show();
                }


            } catch (Exception e) {
                Toast.makeText(MyService.this, "Failed to write!",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Location not Detected",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //the detected location is given by the variable location in the signature

        Toast.makeText(this, "Lat : " + location.getLatitude() + " Lng : " +
                location.getLongitude(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mGoogleApiClient.connect();

        if (started == false) {
            started = true;

            Toast.makeText(MyService.this, "Service Started", Toast.LENGTH_LONG).show();
        } else {

            File targetFile = new File(folderLocation, "data.txt");

            try {
                FileWriter writer = new FileWriter(targetFile, true);
                writer.write(mLocation.getLatitude() + ", " + mLocation.getLongitude() + "\n");
                writer.flush();
                writer.close();

                if (targetFile.exists() == true) {
                    String data = "";
                    try {
                        FileReader reader = new FileReader(targetFile);
                        BufferedReader br = new BufferedReader(reader);
                        String line = br.readLine();
                        while (line != null) {
                            data += line + "\n";
                            line = br.readLine();
                        }
                        br.close();
                        reader.close();
                    } catch (Exception e) {
                        Toast.makeText(MyService.this, "Failed to read!",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    Toast.makeText(MyService.this, data, Toast.LENGTH_LONG).show();
                    SharedPreferences prefsLat = PreferenceManager
                            .getDefaultSharedPreferences(MyService.this);
                    SharedPreferences.Editor prefEditLat = prefsLat.edit();
                    prefEditLat.putString("lat", String.valueOf(mLocation.getLatitude()));
                    prefEditLat.commit();

                    SharedPreferences prefsLong = PreferenceManager
                            .getDefaultSharedPreferences(MyService.this);
                    SharedPreferences.Editor prefEditLong = prefsLong.edit();
                    prefEditLong.putString("long", String.valueOf(mLocation.getLongitude()));
                    prefEditLong.commit();
                }
                Toast.makeText(MyService.this, "Service is Running", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(MyService.this, "Failed to write!",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }


        }
        return Service.START_STICKY;

    }

    @Override
    public void onDestroy() {
        Toast.makeText(MyService.this, "Service Stopped", Toast.LENGTH_LONG).show();
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        File targetFile = new File(folderLocation, "data.txt");

        try {
            FileWriter writer = new FileWriter(targetFile, true);
            writer.write("");
            writer.flush();
            writer.close();

        } catch (Exception e) {
            Toast.makeText(MyService.this, "Failed to write!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }
}
