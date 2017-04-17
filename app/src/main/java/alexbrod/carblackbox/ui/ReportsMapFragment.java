package alexbrod.carblackbox.ui;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import alexbrod.carblackbox.R;
import alexbrod.carblackbox.bl.TravelEvent;
import alexbrod.carblackbox.utilities.MyUtilities;

import static alexbrod.carblackbox.utilities.MyUtilities.SHARP_TURN;
import static alexbrod.carblackbox.utilities.MyUtilities.SPEEDING;
import static alexbrod.carblackbox.utilities.MyUtilities.SUDDEN_BRAKE;

public class ReportsMapFragment extends SupportMapFragment implements OnMapReadyCallback {
    //TODO: Pass those constants as parameters to the fragment
    private static final float DEFAULT_ZOOM = 16;
    private static final int MAP_PADDING = 10;
    private MapView mapView;
    private GoogleMap map;
    private IMapFragmentEvents mMapFragmentListener;

    public ReportsMapFragment() {
        // Required empty public constructor
    }

    public static ReportsMapFragment newInstance() {
        ReportsMapFragment fragment = new ReportsMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    interface IMapFragmentEvents {
        void onMapFragmentReady();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //register to map fragment events

        View v = inflater.inflate(R.layout.fragment_map_layout, container, false);
        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();// needed to get the map to display immediately
        mapView.getMapAsync(this);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        enableMyLocation();
        mMapFragmentListener.onMapFragmentReady();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(getTag(),"No permissions to use MyLocation");
            return;
        }
        map.setMyLocationEnabled(true);
    }

    public void updateMapCameraView(Location location){
        if(!isMapReady()){
            return;
        }
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
    }

    public void addReport(String eventType, double value, Location location) {
        if(!isMapReady() || location == null){
            return;
        }
        addMarkerToMap(eventType, value, location.getTime(),
                location.getLatitude(), location.getLongitude());
    }

    private void addMarkerToMap(String eventType, double value, long timestamp,
                                double locLat, double locLong) {
        // create marker
        MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(locLat, locLong));
        marker.title("Event:" + eventType);
        marker.snippet("Date: " + MyUtilities.formatDateTime(timestamp) +
            " Value: " + String.format("%.1f",value));

        // Changing marker icon
        switch (eventType){
            case SPEEDING:
                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.speed_sign));
                break;
            case SHARP_TURN:
                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.sharp_turn));
                break;
            case SUDDEN_BRAKE:
                marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.sudden_break));
                break;
        }
        // adding marker
        map.addMarker(marker);
    }

    public void clearEventsFromMap(){
        map.clear();
    }

    public void showSavedEventsOnMap(ArrayList<TravelEvent> travelEvents){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int eventsCounter = 0;
        for (TravelEvent te: travelEvents) {
            addMarkerToMap(te.getType(),te.getValue(),te.getTimeOccurred(),
                    te.getLocLat(),te.getLocLong());
            builder.include(new LatLng(te.getLocLat(),te.getLocLong()));
            eventsCounter++;
        }
        if(eventsCounter > 0){
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING);
            map.animateCamera(cu);
        }
    }

    public void registerToMapFragmentEvents(IMapFragmentEvents listener){
        mMapFragmentListener = listener;
    }

    public boolean isMapReady(){
        if(map == null){
            return false;
        }
        return true;
    }
}