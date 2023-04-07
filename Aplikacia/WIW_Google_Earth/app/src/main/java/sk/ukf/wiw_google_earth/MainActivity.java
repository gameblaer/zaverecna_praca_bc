package sk.ukf.wiw_google_earth;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences gSP;
    private int gCheckedRadioButtonIndex = 0;
    private final RadioButton[] gRadioButton = new RadioButton[3];
    private final int START_VALUE_RADIOBUTTON = 1;
    private RadioGroup gRadioGroup;
    private Button gStart;
    private Button gHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gStart = findViewById(R.id.StartMain);
        gHistory = findViewById(R.id.btnHistory);
        //radiobuttons
        gRadioGroup = findViewById(R.id.radioGroupMain);
        //for radiobuttons
        String radioButtonID;
        int resourceViewID;
        for (int i = 0; i < gRadioButton.length; i++)
        {
            radioButtonID = "radioButton" + (i+START_VALUE_RADIOBUTTON); //radiobuttons start 4 and end 9
            resourceViewID = getResources().getIdentifier(radioButtonID, "id", getPackageName());
            gRadioButton[i] = ( (RadioButton)findViewById(resourceViewID) );
        }



        loadData();
        RadioButtonChangeListener();
        start();
        history();
        permision();
    }

    private void permision() {
        //save file
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Log.d("File", "You dont have access to save file.");
        }

        //GpsPermision
        if (    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)          != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)     != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ){

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, 1);
            Log.d("File", "You dont have access to GPS");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.Preferences) {
            startActivityForResult(new Intent(MainActivity.this, Settings.class), 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    private void start(){
        gStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( MainActivity.this, ActualDataSenzor.class);
                startActivity(intent);
            }
        });
    }

    private void history(){
        gHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( MainActivity.this, History.class);
                startActivity(intent);
            }
        });
    }

    private void saveData() {
        //Save data
        gSP = getSharedPreferences(MyDB.NameKeys.UserSettings.KeyName(), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = gSP.edit();

        editor.putInt(MyDB.NameKeys.MainCheckedRadioButtonIndex.toString(), gCheckedRadioButtonIndex);

        editor.commit();
    }

    private void loadData(){
        SharedPreferences sp = getApplicationContext().getSharedPreferences( MyDB.NameKeys.UserSettings.toString() , Context.MODE_PRIVATE );

        int indexRadioButton = sp.getInt( MyDB.NameKeys.MainCheckedRadioButtonIndex.toString(),0 );

        if( ( indexRadioButton >= 0 ) && ( indexRadioButton < gRadioButton.length ) ) {
            gRadioButton[indexRadioButton].setChecked(true);
            gCheckedRadioButtonIndex = indexRadioButton;
        }
    }

    private void RadioButtonChangeListener(){
        gRadioGroup.setOnCheckedChangeListener( (radioGroup, i) -> {

            getRadioButtonIndex();

            saveData();
        });
    }

    //get index radiobutton and set number radiobutton
    private void getRadioButtonIndex(){
        for (int i = 0 ; i < gRadioButton.length; i++){
            if ( gRadioButton[i].isChecked() ) {
                gCheckedRadioButtonIndex = i;
                break;
            }
        }
    }
}