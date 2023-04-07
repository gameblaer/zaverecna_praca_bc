package sk.ukf.wiw_google_earth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    private Button gSave;
    private TextView gTwRefreshTime;
    private final RadioButton[] gRadioButton = new RadioButton[7];
    private RadioGroup gRadioGroup;
    private int gCheckedRadioButtonIndex = 0;
    private SharedPreferences gSP;
    private int gnTextViewValue;
    private int gCurrentlyNumber;
    private Boolean gNumberIsOk = false;

    //define
    private final int START_VALUE_RADIOBUTTON = 4;
    private final int SHOW_TEXT_VIEW_INDEX = 5;
    private final int OUT_RANGE_INDEX = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        gSave = findViewById(R.id.SaveSettings);
        Button gBack = findViewById(R.id.BackSettings);
        //radiobuttons
        gRadioGroup = findViewById(R.id.radioGroupSettings);
        /*
        gRadioButton[0] = findViewById( R.id.radioButton4 );
        gRadioButton[1] = findViewById( R.id.radioButton5 );
        gRadioButton[2] = findViewById( R.id.radioButton6 );
        gRadioButton[3] = findViewById( R.id.radioButton7 );
        gRadioButton[4] = findViewById( R.id.radioButton8 );
        gRadioButton[5] = findViewById( R.id.radioButton9 );
        */
        //for radiobuttons
        String radioButtonID;
        int resourceViewID;
        for (int i = 0; i < gRadioButton.length; i++)
        {
            radioButtonID = "radioButton" + (i+START_VALUE_RADIOBUTTON); //radiobuttons start 4 and end 10
            resourceViewID = getResources().getIdentifier(radioButtonID, "id", getPackageName());
            gRadioButton[i] = ( (RadioButton)findViewById(resourceViewID) );
        }

        gRadioButton[6].setVisibility(View.GONE);
        //refresh time and invisible
        gTwRefreshTime = findViewById(R.id.LwRefreshTime);

        loadData();
        //gTwRefreshTime.setVisibility(View.INVISIBLE);
        getRadioButtonIndex();

        //radioGroup log, which radiobutton is marked
        for (int i = 0 ; i < gRadioButton.length ; i++ ) {
            if ( gRadioButton[i].isChecked() ) {
                String sText = String.valueOf( i );
                gCheckedRadioButtonIndex = i;
                Log.d("RadioButtonChecked is ", sText);
            } else {
                String sText = String.valueOf( i );
                Log.d("RadioButtonChecked isnt ", sText);
            }
        }

        //radio button listener
        RadioButtonChangeListener();

        //listeners cancel
        gBack.setOnClickListener(view -> {
            Toast.makeText(Settings.this, getString(R.string.cancel), Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, new Intent());
            finish();
        });

        SaveListener();
        textViewListener();
    }

    private void loadData(){
        SharedPreferences sp = getApplicationContext().getSharedPreferences( MyDB.NameKeys.UserSettings.toString() , Context.MODE_PRIVATE );

        int indexRadioButton = sp.getInt( MyDB.NameKeys.SettingsCheckedRadioButtonIndex.toString(),0 );
        int nRefreshTime = sp.getInt( MyDB.NameKeys.RereshTime.toString(),0 );
        int nTextView = sp.getInt( MyDB.NameKeys.TextViewValue.toString(), 0);
        int nFirstSet = sp.getInt( MyDB.NameKeys.FirstSet.toString(), 0);

        //System.out.println("text padne "+ nRefreshTime);

        if( nRefreshTime > 0 )      gCurrentlyNumber = nRefreshTime;
        if( ( indexRadioButton >= 0 ) && ( indexRadioButton < gRadioButton.length ) ) {
            gRadioButton[indexRadioButton].setChecked(true);
            gCheckedRadioButtonIndex = indexRadioButton;
            VisibleTextView();
        }
        if( nTextView > 0 ){
            gnTextViewValue = nTextView;
            String sTemp = Integer.toString(gnTextViewValue);
            gTwRefreshTime.setText( sTemp ) ;
        }

    }

    private boolean textViewIsGood(){
        if( gCheckedRadioButtonIndex == SHOW_TEXT_VIEW_INDEX ) {
            String value = gTwRefreshTime.getText().toString();
            value = value.trim();
            int nValue = 0;
            //textview have number more how 0
            if( value.length() > 0 ) {
                nValue = Integer.parseInt(value);
            }
            if( nValue > 0 ){
                gTwRefreshTime.setTextColor( getResources().getColor(R.color.green) );
                gNumberIsOk = true;
            } else {
                gTwRefreshTime.setTextColor( getResources().getColor(R.color.red) );
                gNumberIsOk = false;
                return gNumberIsOk;
            }
            gnTextViewValue = nValue;
        }

        return true;
    }

    private boolean CheckedButton(int index) {

        final int[] RadioButton = { 1, 2, 5, 10, 30, gnTextViewValue, -1 };

        /* if not have valid number gnTextViewValue
        1. if is good txt in TextView && SHOW_TEXT_VIEW_INDEX is focus
        2. if is good range
         */
        if( ( !textViewIsGood() ) && ( index == SHOW_TEXT_VIEW_INDEX ) || ( index < 0 ) || ( index >= RadioButton.length) ){
            Toast.makeText(Settings.this, getString(R.string.bad_settings), Toast.LENGTH_SHORT).show();
            return false;
        }
        //everthing is ok
        gCurrentlyNumber = RadioButton[index];
        return true;
    }

    private void textViewListener(){
        gTwRefreshTime.setOnClickListener(view ->
            textViewIsGood()
        );
    }

    //get index radiobutton and set number radiobutton
    private void getRadioButtonIndex(){
        for (int i = 0 ; i < gRadioButton.length; i++){
            if ( gRadioButton[i].isChecked() ) {
                gCheckedRadioButtonIndex = i;
                break;
            }
        }
        //return gCheckedRadioButtonIndex;
    }

    private void VisibleTextView(){
        //show textviem for currently number
        if ( gRadioButton[SHOW_TEXT_VIEW_INDEX].isChecked() ) {
            gTwRefreshTime.setVisibility(View.VISIBLE);
        } else {
            gTwRefreshTime.setVisibility(View.INVISIBLE);
        }
    }

    private void RadioButtonChangeListener(){
        gRadioGroup.setOnCheckedChangeListener( (radioGroup, i) -> {

            getRadioButtonIndex();

            //show textviem for currently number
            VisibleTextView();

            //String sText = String.valueOf( gCheckedRadioButtonIndex );
            //Log.d("RadioGroupChange is ", sText);
            //Toast.makeText(Settings.this, "Settings Changed. RadioButton is " + gCheckedRadioButtonIndex, Toast.LENGTH_SHORT).show();
        });
    }

    private void SaveListener() {
        //on click ulozit
        gSave.setOnClickListener(view -> {

            if( !CheckedButton(gCheckedRadioButtonIndex) ) return;

            //Save data
            gSP = getSharedPreferences( MyDB.NameKeys.UserSettings.KeyName(), Context.MODE_PRIVATE );

            SharedPreferences.Editor editor = gSP.edit();

            //saved data, key and value
            editor.putInt( MyDB.NameKeys.SettingsCheckedRadioButtonIndex.toString(), gCheckedRadioButtonIndex );
            editor.putInt( MyDB.NameKeys.RereshTime.toString(), gCurrentlyNumber );
            editor.putInt( MyDB.NameKeys.TextViewValue.toString(),  gnTextViewValue);
            editor.putInt( MyDB.NameKeys.FirstSet.toString(),  1);

            //only text
            String sTemp = "";
            if( ( gCurrentlyNumber == 1 ) || ( ( gnTextViewValue == 1) && SHOW_TEXT_VIEW_INDEX == gCheckedRadioButtonIndex ) ) sTemp = gCurrentlyNumber + " " + getString(R.string.minute);
            else if( gCurrentlyNumber > 1 && gCurrentlyNumber < 5 ) sTemp = gCurrentlyNumber + " " + getString(R.string.minutes);
            else if (gCurrentlyNumber >= 5) sTemp = gCurrentlyNumber + " " + getString(R.string.minutesSk);
            else if ( gCurrentlyNumber == -1 ) sTemp = getString(R.string.distance);

            editor.commit();
            Toast.makeText(Settings.this, getString(R.string.settings_saved) + " " + sTemp, Toast.LENGTH_LONG).show();

            Intent intent = new Intent();
            setResult(RESULT_OK, intent);

            finish();
        });
    }
}