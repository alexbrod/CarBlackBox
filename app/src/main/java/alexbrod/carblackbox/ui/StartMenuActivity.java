package alexbrod.carblackbox.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.stetho.Stetho;

import alexbrod.carblackbox.R;
import alexbrod.carblackbox.bl.CarBlackBoxEngine;


public class StartMenuActivity extends Activity  {

    private Button mBtnDrive;
    private Button mBtnStats;
    private CarBlackBoxEngine carBlackBoxEngine;
    //-----------------------Activity lifecycle methods ---------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);
        carBlackBoxEngine = CarBlackBoxEngine.getInstance();


        mBtnDrive = (Button)findViewById(R.id.btnDrive);
        mBtnDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartMenuActivity.this.startActivity(
                        new Intent(StartMenuActivity.this, CarViewActivity.class));
            }
        });

        mBtnStats = (Button)findViewById(R.id.btnStats);
        mBtnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartMenuActivity.this.startActivity(
                        new Intent(StartMenuActivity.this, StatsActivity.class));
            }
        });

        //for inspecting sqlite db from chrome://inspect
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }




}
