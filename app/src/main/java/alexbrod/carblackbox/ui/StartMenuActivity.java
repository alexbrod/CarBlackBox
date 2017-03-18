package alexbrod.carblackbox.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import alexbrod.carblackbox.R;
import alexbrod.carblackbox.bl.CarBlackBoxEngine;


public class StartMenuActivity extends Activity  {

    private Button mBtnDrive;
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
