package alexbrod.carblackbox.ui;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;

import alexbrod.carblackbox.R;
import alexbrod.carblackbox.bl.Travel;
import alexbrod.carblackbox.bl.TravelEvent;
import alexbrod.carblackbox.db.DbManager;

import static alexbrod.carblackbox.utilities.MyUtilities.formatDateTime;

public class StatsActivity extends AppCompatActivity implements ReportsMapFragment.IMapFragmentEvents {

    private static final String TAG = "StatsActivity";
    private static final int TRAVELS_IN_SPINNER = 10;
    private Spinner mSpinnerDates;
    private ArrayList<String> mDates;
    private HashMap<String,Long> mDateToLong;
    private DbManager mDbManager;
    private ReportsMapFragment mReportsMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        mDbManager = DbManager.getInstance(this);
        mSpinnerDates = (Spinner)findViewById(R.id.spinnerDates);
        mDates = new ArrayList<>();
        mDateToLong = new HashMap<>();
        initSpinner();

        mReportsMapFragment = ReportsMapFragment.newInstance();
        setFragment(mReportsMapFragment);
        mReportsMapFragment.registerToMapFragmentEvents(this);

    }

    private void initSpinner(){
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_layout,mDates);

        mSpinnerDates.setAdapter(arrayAdapter);
        for(Travel t:mDbManager.getLastNumTravels(TRAVELS_IN_SPINNER)){
            String formattedDate = formatDateTime(t.getStartTime());
            mDateToLong.put(formattedDate,t.getStartTime());
            arrayAdapter.add(formattedDate);
        }
        mSpinnerDates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String str = (String)adapterView.getItemAtPosition(i);
                ArrayList<TravelEvent> travelEvents = mDbManager.getEventsByTravel(mDateToLong.get(str));
                mReportsMapFragment.clearEventsFromMap();
                mReportsMapFragment.showSavedEventsOnMap(travelEvents);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setFragment(Fragment fragment){
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container_stats) != null) {
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_stats, fragment
                            ,fragment.getClass().getSimpleName()).commit();
        }
        else{
            Log.e(getClass().getSimpleName(),"No fragment container");
        }
    }

    @Override
    public void onMapFragmentReady() {
        //do nothing
    }
}
