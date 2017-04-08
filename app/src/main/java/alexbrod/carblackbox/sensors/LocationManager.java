package alexbrod.carblackbox.sensors;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;


/**
 * Created by Alex Brod on 3/27/2017.
 */

public class LocationManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>,
        LocationListener {

    private static final long LOCATION_REQUEST_INTERVAL = 2000; //ms
    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 1000; //ms

    private static LocationManager locationManager;
    private GoogleApiClient googleApiClient;
    private LocationSettingsRequest.Builder locationSettingsRequestBuilder;
    private LocationRequest locationRequest;
    private ILocationManagerEvents locationManagerListener;
    private int speed = 0;
    private Location mLastLocation;

    private LocationManager(Context context) {
        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    public static LocationManager getInstance(Context context) {
        if (locationManager == null) {
            locationManager = new LocationManager(context);
        }
        return locationManager;
    }

    //----------------------Location inner management----------------
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationSettingsRequestBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(googleApiClient.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(googleApiClient.getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }


    private void stopLocationUpdates() {
        if(isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    private void tryStartLocationUpdates() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        locationSettingsRequestBuilder.build());
        result.setResultCallback(this);
    }

    //--------------------Location external management ----------------------
    public void connect() {
        googleApiClient.connect();
    }

    public void disconnect() {
        stopLocationUpdates();
        googleApiClient.disconnect();
    }

    public void registerToLocationManagerEvents(ILocationManagerEvents listener){
        locationManagerListener = listener;
    }

    public void unregisterFromLocationManagerEvents() {
        locationManagerListener = null;
    }

    public boolean isConnected(){
        return googleApiClient.isConnected();
    }

    public Location getLastKnownLocation(){
        return mLastLocation;
    }

    //---------------------Events Location Manager listens--------------------------
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();
        tryStartLocationUpdates();
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        //        final LocationSettingsStates = result.getLocationSettingsStates();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // All location settings are satisfied. The client can
                // initialize location requests here.
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                if(locationManagerListener != null){
                    locationManagerListener.onLocationResolutionRequired(status);
                }
                break;
            case LocationSettingsStatusCodes.NETWORK_ERROR:
                Log.e(this.getClass().getSimpleName(),"Network Error");
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way
                // to fix the settings so we won't show the dialog.
                Log.e(this.getClass().getSimpleName(),"Unknown Location Error");
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if(locationManagerListener == null){
            return;
        }
        if(location.hasSpeed()){
            int tmpSpeed = (int)(location.getSpeed()*3.6); //convert to km/h
            Log.w(getClass().getSimpleName(),"Speed: " + speed
                + "," + tmpSpeed);
            if(speed != tmpSpeed){
                speed = tmpSpeed;
                locationManagerListener.onSpeedChanged(speed, location);
            }
        }
        locationManagerListener.onLocationChanged(location);

    }


}
