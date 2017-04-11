package alexbrod.carblackbox.sensors;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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

import static android.location.GpsStatus.GPS_EVENT_STARTED;
import static android.location.GpsStatus.GPS_EVENT_STOPPED;


/**
 * Created by Alex Brod on 3/27/2017.
 */

public class CarLocationManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>,
        LocationListener {

    private static final long LOCATION_REQUEST_INTERVAL = 2000; //ms
    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 1000; //ms
    private static final String TAG = "CarLocationManager";

    private static CarLocationManager mCarLocationManager;
    private GoogleApiClient mGoogleApiClient;
    private LocationSettingsRequest.Builder mLocationSettingsRequestBuilder;
    private LocationRequest mLocationRequest;
    private ILocationManagerEvents mLocationManagerListener;
    private Location mLastLocation;
    private int mSpeed = 0;

    private CarLocationManager(Context context) {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        setGpsListener(context);

    }

    private void setGpsListener(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG,"No permission to use GPS");
            return;
        }
        //this method is deprecated in API level 24, but the application
        //supports API level 21 users, so this it's used instead
        //registerGnssStatusCallback(GnssStatus.Callback)
        lm.addGpsStatusListener(new android.location.GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    case GPS_EVENT_STARTED:
                        tryStartLocationUpdates();
                        break;
                    case GPS_EVENT_STOPPED:
                        stopLocationUpdates();
                        break;
                }
            }
        });
    }

    public static CarLocationManager getInstance(Context context) {
        if (mCarLocationManager == null) {
            mCarLocationManager = new CarLocationManager(context);
        }
        return mCarLocationManager;
    }

    //----------------------Location inner management----------------
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationSettingsRequestBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mGoogleApiClient.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mGoogleApiClient.getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }


    private void stopLocationUpdates() {
        if(isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    private void tryStartLocationUpdates() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        mLocationSettingsRequestBuilder.build());
        result.setResultCallback(this);
    }

    //--------------------Location external management ----------------------
    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    public void registerToLocationManagerEvents(ILocationManagerEvents listener){
        mLocationManagerListener = listener;
    }

    public void unregisterFromLocationManagerEvents() {
        mLocationManagerListener = null;
    }

    public boolean isConnected(){
        return mGoogleApiClient.isConnected();
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
        Log.w(TAG,"Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG,"Connection failed");
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
                if(mLocationManagerListener != null){
                    mLocationManagerListener.onLocationResolutionRequired(status);
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
        if(mLocationManagerListener == null){
            return;
        }
        if(location.hasSpeed()){
            int tmpSpeed = (int)(location.getSpeed()*3.6); //convert to km/h
            Log.w(getClass().getSimpleName(),"Speed: " + mSpeed
                + "," + tmpSpeed);
            if(mSpeed != tmpSpeed){
                mSpeed = tmpSpeed;
                mLocationManagerListener.onSpeedChanged(mSpeed, location);
            }
        }
        mLocationManagerListener.onLocationChanged(location);

    }


}
