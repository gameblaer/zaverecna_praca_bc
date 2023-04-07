package sk.ukf.wiw_google_earth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ActualDataSenzor extends AppCompatActivity implements SensorEventListener {

    //db
    private final DB_Functions dbh = new DB_Functions(this);

    //define
    private static final int DEFAULT_TIME_REFRESH = 1;
    private static final int DEFAULT_RADIOBUTTON_FROM_SETTINGS = 1;
    private static final Double DEFAULT_COMPARE_TIME_REFRESH = 30.0;
    private static final int INTERVAL = 1000;

    private Button gWhereIAm;
    private TextView goTempDate;
    private TextView goTempTime;
    private TextView goTempGPS;
    private TextView goTempGpsLong;
    private TextView goTempSpeed;
    private TextView goTempAccuracy;
    private TextView goTempAccuracyGPS;
    private TextView goTempHeight;
    private TextView goTempDistance;
    private TextView goTempAcceleration;
    private TextView goTempGravity;
    private TextView goTempGyroscope;
    private TextView goTempLinearAccele;
    private TextView goTempOrientation;
    private TextView goTempRotationVector;
    private TextView goTempTravelTime;
    private int gnRefreshTimeMinute = 1;

    private Timer timer;
    private TimerTask timerTask;
    private Double time = 0.0;
    private Double gnRefreshTime = 0.0;
    private double gfTravelDisatanceValue = 0.0;
    private double gfTravelDisatanceValuePreviousLatitude = 0.0;
    private double gfTravelDisatanceValuePreviousLongitude = 0.0;
    private Location gLocation = null;
    private LocationProvider gLocationProvider = null;
    private LocationManager gLocationManager = null;
    private boolean gbSaveDataGoOut = false;
    private boolean gbDistanceGoOut = false;
    private boolean gbFirstUse = true;


    //sensors
    private final int[] goSensorsName = { Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_GRAVITY, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_ORIENTATION, Sensor.TYPE_ROTATION_VECTOR};
    private final String[][] gsData3D = new String[goSensorsName.length][3];
    private final boolean[] gbHasSensor = new boolean[goSensorsName.length];
    private final SensorManager[] goSensorManager = new SensorManager[goSensorsName.length];
    private final Sensor[] goSensor = new Sensor[goSensorsName.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual_data_senzor);

        //allways dispaly
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //button
        Button gCancel = findViewById(R.id.CancelADS);
        //commponents
        Button gFinish = findViewById(R.id.FinishADS);
        gWhereIAm = findViewById(R.id.WhereIAmADS);

        //textview
        goTempDate = findViewById(R.id.TempDate);
        goTempTime = findViewById(R.id.TempTime);
        goTempGPS = findViewById(R.id.TempGPS);
        goTempGpsLong = findViewById(R.id.TempGpsLong);
        goTempSpeed = findViewById(R.id.TempSpeed);
        goTempAccuracy = findViewById(R.id.Accuracy);
        goTempAccuracyGPS = findViewById(R.id.AccuracyGPS);
        goTempHeight = findViewById(R.id.TempHeight);
        goTempDistance = findViewById(R.id.TempDistance);
        TextView goTempInclanation = findViewById(R.id.TempInclination);
        TextView goTextInclanation = findViewById(R.id.TextInclination);
        goTempTravelTime = findViewById(R.id.TempTravelTime);
        goTempAcceleration = findViewById(R.id.Acceleration13);
        goTempGravity = findViewById(R.id.Gravity15);
        goTempGyroscope = findViewById(R.id.Gyroscope17);
        goTempLinearAccele = findViewById(R.id.LinearAccele);
        goTempOrientation = findViewById(R.id.Orientation24);
        goTempRotationVector = findViewById(R.id.RotationVector);

        /*
        for (int row = 0; row < gsData3D.length; row++) {
            for (int i = 0; i < gsData3D[row].length; i++) {
                gsData3D[row][i] = "0.0";
            }
        }
        */
        for (String[] strings : gsData3D) {
            Arrays.fill(strings, "0");
        }

        //visible inclanation
        SharedPreferences spMainChoose = getApplicationContext().getSharedPreferences(MyDB.NameKeys.UserSettings.toString(), Context.MODE_PRIVATE);
        int indexRadioButton = spMainChoose.getInt(MyDB.NameKeys.MainCheckedRadioButtonIndex.toString(), 0);

        /*
        if (indexRadioButton == MyDB.MainOptions.ByAir.number()) {
            goTempInclanation.setVisibility(View.VISIBLE);
            goTextInclanation.setVisibility(View.VISIBLE);
        } else {
            /*
            invsible and gone hide text
            invisible make empty line
            gone make invisible text and delete empty line
            *//*
            goTempInclanation.setVisibility(View.GONE);
            goTextInclanation.setVisibility(View.GONE);
        }
        */
        goTempInclanation.setVisibility(View.GONE);
        goTextInclanation.setVisibility(View.GONE);

        //get data RefreshTime from settings
        SharedPreferences spSettingChoose = getApplicationContext().getSharedPreferences(MyDB.NameKeys.UserSettings.toString(), Context.MODE_PRIVATE);
        int indexSettingRadioButton = spSettingChoose.getInt(MyDB.NameKeys.RereshTime.toString(), 0);
        int nFirstSet = spSettingChoose.getInt( MyDB.NameKeys.FirstSet.toString(), 0);

        //if i dont set settings i have nFirstSet == 0, but if i set settings once so i have always nFirstSet == 1
        Log.d("Setting", "nFirstSet is " + nFirstSet);
        if(nFirstSet == 0){
            indexSettingRadioButton = DEFAULT_RADIOBUTTON_FROM_SETTINGS;
        }
        /*
        refresh time
        value -1 is OutRadius
        */
        if (indexSettingRadioButton < 1) {
            gnRefreshTimeMinute = DEFAULT_TIME_REFRESH;
            gbSaveDataGoOut = true;
        } else {
            gnRefreshTimeMinute = indexSettingRadioButton;
            gbSaveDataGoOut = false;
        }
        //Log.d("Setting", "index " + indexSettingRadioButton);

        //get data RefreshTime from settings
        spSettingChoose = getApplicationContext().getSharedPreferences(MyDB.NameKeys.UserSettings.toString(), Context.MODE_PRIVATE);
        int indexRadioButtonSettings = spSettingChoose.getInt(MyDB.NameKeys.SettingsCheckedRadioButtonIndex.toString(), 0);

        //Log.d("Setting", "indexArr" + gnRefreshTimeMinute);

        //RequestPermision
        //GpsPermision
        if (    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)          != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)     != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, 1);
        }

        //sensors
        //LocationManager gGpsLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        gLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Sensors
        for (int i = 0 ; i < goSensorsName.length; i++) {
            goSensorManager[i] = (SensorManager) getSystemService(SENSOR_SERVICE);
            if (goSensorManager[i] != null) {

                goSensor[i] = goSensorManager[i].getDefaultSensor(goSensorsName[i]);

                if (goSensor[i] != null) {
                    goSensorManager[i].registerListener(this, goSensor[i], SensorManager.SENSOR_DELAY_NORMAL);
                    gbHasSensor[i] = true;
                } else {
                    gbHasSensor[i] = false;
                }
            } else {
                gbHasSensor[i]  = false;
            }
        }

        //Log.d("Setting", "indexRadioButton "+indexRadioButton+", indexRadioButtonSettings "+indexRadioButtonSettings+", indexSettingRadioButton "+indexSettingRadioButton+"\n");
        dbh.addWay(indexRadioButton, indexRadioButtonSettings, gnRefreshTimeMinute);

        //dbh.printWay(dbh.getWayId());//todo mv na konci vypnut, sluzi len ako vypis vsetkych stlpcov vo way

        //timer
        timer = new Timer();
        startTimer();

        //Where i am
        whereIAm();

        //listeners cancel
        gCancel.setOnClickListener(view -> {

            timerTask.cancel();

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            dbh.deleteWay(dbh.getWayId());

            Toast.makeText(ActualDataSenzor.this, getString(R.string.cancel), Toast.LENGTH_SHORT).show();

            setResult(RESULT_CANCELED, new Intent());
            finish();
        });

        //listeners finish
        gFinish.setOnClickListener(view -> {

            timerTask.cancel();

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            updateWay();

            Toast.makeText(ActualDataSenzor.this, getString(R.string.save), Toast.LENGTH_SHORT).show();

            setResult(RESULT_OK, new Intent());
            finish();
        });
    }

    private void whereIAm(){
        gWhereIAm.setOnClickListener(view -> {
            Intent intent = new Intent( ActualDataSenzor.this, MapsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0 ; i < goSensorsName.length; i++) {
            if (!gbHasSensor[i]) return;

            goSensorManager[i].registerListener(this, goSensor[i], SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        for (int i = 0 ; i < goSensorsName.length; i++) {
            if (!gbHasSensor[i]) return;

            goSensorManager[i].unregisterListener(this);
        }
    }

    private String Data3D(SensorEvent event, int row){
        String temp;
        for (int i = 0; i < gsData3D[row].length; i++) {
            gsData3D[row][i] = String.valueOf(event.values[i]);
        }

        temp =  String.format("x=%.3f", event.values[0]) +
                String.format(", y=%.3f", event.values[1]) +
                String.format(", z=%.3f", event.values[2]);

        return temp;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int row;
        //0
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            row = 0;
            if ( !gbHasSensor[row] ) return;

            //long actualTime = System.currentTimeMillis();
            //if(actualTime - gnLastUpdate > INTERVAL){

            goTempAcceleration.setText(Data3D(event, row));
            //}
        }

        //1
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            row = 1;
            if ( !gbHasSensor[row] ) return;

            goTempLinearAccele.setText(Data3D(event, row));
        }

        //2
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            row = 2;
            if ( !gbHasSensor[row] ) return;

            goTempGravity.setText(Data3D(event, row));
        }

        //3
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            row = 3;
            if ( !gbHasSensor[row] ) return;

            goTempGyroscope.setText(Data3D(event, row));
        }

        //4
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            row = 4;
            if ( !gbHasSensor[row] ) return;

            goTempOrientation.setText(Data3D(event, row));
        }

        //5
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            row = 5;
            if ( !gbHasSensor[row] ) return;

            goTempRotationVector.setText(Data3D(event, row));
            try {
                String temp = event.values[3] + "";
                //Log.d("Distance", "goTempRotationVector is " + temp);
            } catch (Exception e){
                Log.d("Distance", "goTempRotationVector neni " + e);
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            String temp;
            //gps
            temp = String.valueOf(location.getLatitude());
            goTempGPS.setText(temp);
            temp = String.valueOf(location.getLongitude());
            goTempGpsLong.setText(temp);
            temp = String.valueOf( location.getAltitude() );
            goTempHeight.setText(temp);

            // Ziskat hodnotu presnosti polohy
            float accuracy = location.getAccuracy();
            temp = String.valueOf( accuracy );
            goTempAccuracy.setText(temp);

            // Ziskat hodnotu presnosti GPS signalu
            gLocationProvider = gLocationManager.getProvider(LocationManager.GPS_PROVIDER);
            float gpsAccuracy = gLocationProvider.getAccuracy();
            temp = String.valueOf( gpsAccuracy );
            goTempAccuracyGPS.setText(temp);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }
    };

    private void getCurrentPosition() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        /*
        if (    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)      != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)    != PackageManager.PERMISSION_GRANTED) {
            return;
        }*/

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        if (    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)      != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)    != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        updateGPS(locationManager, provider);

        if( gbFirstUse ){
            gbFirstUse = false;
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
        }
/*
        if (gbSaveDataGoOut) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                Location lastLocation = null;

                @Override
                public void onLocationChanged(@NonNull Location location) {
                    //todo mv neukladam udaje z gps polohy
                    if (lastLocation != null) {
                        gfTravelDisatanceValue = gfTravelDisatanceValue + location.distanceTo(lastLocation);

                        updateGPS(locationManager/*, provider*//*);
                    }
                    lastLocation = location;

                    String temp = String.valueOf(gfTravelDisatanceValue);
                    gbDistanceGoOut = true;
                    goTempDistance.setText(temp);
                }
            });
        } else {
            updateGPS(locationManager/*, provider*//*);
        }*/


    }

    private void updateGPS(LocationManager locationManager, String provider) {
        if (    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)      != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)    != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //Log.d("Location", "location" +location);
        if (location == null)
            return;
        //Log.d("Location", "location!=null, !gbSaveDataGoOut "+!gbSaveDataGoOut );
        String temp;
        gLocation = location;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        //gps
        temp = String.valueOf(latitude);
        goTempGPS.setText(temp);
        temp = String.valueOf(longitude);
        goTempGpsLong.setText(temp);

        //height above sea
        temp = String.valueOf( location.getAltitude() );
        goTempHeight.setText(temp);

        // Získať hodnotu presnosti polohy
        float accuracy = location.getAccuracy();
        temp = String.valueOf( accuracy );
        goTempAccuracy.setText(temp);

        // Získať hodnotu presnosti GPS signálu
        gLocationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        float gpsAccuracy = gLocationProvider.getAccuracy();
        temp = String.valueOf( gpsAccuracy );
        goTempAccuracyGPS.setText(temp);

        //speed
        double speed = location.getSpeed() * 3.6f;
        temp = String.format("%.2f km/h", speed);
        goTempSpeed.setText(temp);

        //distance
        if ( !gbSaveDataGoOut ) {
            float[] distance = new float[1];
            if ((gfTravelDisatanceValuePreviousLatitude > 0) && (gfTravelDisatanceValuePreviousLongitude > 0)) {

                //between
                Location.distanceBetween(
                        gfTravelDisatanceValuePreviousLatitude, gfTravelDisatanceValuePreviousLongitude,
                        latitude, longitude, distance);

                //plus distance
                Log.d("Distance", "Distance[0] is " + distance[0] + ", gDistance" + gfTravelDisatanceValue);
                Log.d("Speed", "Speed("+ (speed != 0) +") is " + speed);
                if( speed != 0 ) {
                    gfTravelDisatanceValue = gfTravelDisatanceValue + distance[0];
                }
            }

            //Log.d("Distance", "Distance("+ gfTravelDisatanceValue +")" );

            //save data
            gfTravelDisatanceValuePreviousLatitude = latitude;
            gfTravelDisatanceValuePreviousLongitude = longitude;

            temp = String.valueOf(gfTravelDisatanceValue);
            goTempDistance.setText(temp);
        }
    }

    private boolean checkGPSLocation(){

        boolean gps_enabled;

        gps_enabled =   !(    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)        != PackageManager.PERMISSION_GRANTED
                           && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)      != PackageManager.PERMISSION_GRANTED
                           && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)  != PackageManager.PERMISSION_GRANTED
                        );

        //Log.d("boolean gps", "boolean gps " + gps_enabled );

        if( gps_enabled ) {
            String temp = "GPS is ON";
            goTempGPS.setText(temp);
            return true;
        } else {
            String temp = "GPS is OFF";
            goTempGPS.setText(temp);
            GpsRequest();
        }
        return false;
    }

    private void GpsRequest(){
        if (
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)      != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)    != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            //Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimer(){
        Double SaveDataTimer = DEFAULT_COMPARE_TIME_REFRESH;
        try {
            SaveDataTimer = Double.parseDouble( String.valueOf(gnRefreshTimeMinute) );
            Log.d("SaveDataTimer", "SaveDataTimer is " + SaveDataTimer);
        } catch(Exception e){
            Log.e("ErrorStartTimer", "error " + e);
        }

        //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Double finalSaveDataTimer = SaveDataTimer;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    String temp;

                    //timer
                    time++;
                    goTempTravelTime.setText( getTimerText(time) );

                    //datum
                    //Date currentTime = Calendar.getInstance().getTime();
                    temp = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
                    goTempDate.setText(temp);

                    //time
                    temp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    goTempTime.setText(temp);

                    //gps
                    if ( checkGPSLocation() ) getCurrentPosition();

                    //Log.d("SaveData", "Data will save if("+ finalSaveDataTimer +"<"+ (gnRefreshTime+1) +")"  );
                    //Log.d("SaveData", "Data will save if( ("+ finalSaveDataTimer < (gnRefreshTime+1) +")||("+ gbDistanceGoOut +") )");

                    //save data
                    if( ( finalSaveDataTimer <= ++gnRefreshTime ) || ( gbDistanceGoOut ) ){
                        gnRefreshTime = 0.0;
                        gbDistanceGoOut = false;

                        addSensorDataToDB();
                        updateWay();

                        //Log.d("SaveData", "Data saved");
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, INTERVAL);
    }

    private void updateWay(){
        dbh.updateWayEnd( time.intValue(), (int)gfTravelDisatanceValue);
    }

    private String getTimerText(double time) {
        int rounded = (int) Math.round(time);

        int seconds = ( (rounded % 86400) % 3600 ) % 60;
        int minutes = ( (rounded % 86400) % 3600 ) / 60;
        int hours = ( rounded % 86400 ) / 3600;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void addSensorDataToDB() {

        if(gLocation == null){
            Log.e("Error", "gLocation is null" );
            Toast.makeText(ActualDataSenzor.this, getString( R.string.gps_connect ), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Date date = new Date();
            Time time = new Time(date.getTime());

            int longWayTime = this.time.intValue();

            double accuracy = gLocation.getAccuracy();
            double accuracyGps = gLocationProvider.getAccuracy();
            double latitude = gLocation.getLatitude();
            double longitude = gLocation.getLongitude();
            double height = gLocation.getAltitude();
            double speed = gLocation.getSpeed() * 3.6f;
            double longWay = gfTravelDisatanceValue;

            double accelerationX = Double.parseDouble(gsData3D[0][0]);
            double accelerationY = Double.parseDouble(gsData3D[0][1]);
            double accelerationZ = Double.parseDouble(gsData3D[0][2]);

            double linearAccelerationX = Double.parseDouble(gsData3D[1][0]);
            double linearAccelerationY = Double.parseDouble(gsData3D[1][1]);
            double linearAccelerationZ = Double.parseDouble(gsData3D[1][2]);

            double gravityX = Double.parseDouble(gsData3D[2][0]);
            double gravityY = Double.parseDouble(gsData3D[2][1]);
            double gravityZ = Double.parseDouble(gsData3D[2][2]);

            double gyroscopeX = Double.parseDouble(gsData3D[3][0]);
            double gyroscopeY = Double.parseDouble(gsData3D[3][1]);
            double gyroscopeZ = Double.parseDouble(gsData3D[3][2]);

            double orientationX = Double.parseDouble(gsData3D[4][0]);
            double orientationY = Double.parseDouble(gsData3D[4][1]);
            double orientationZ = Double.parseDouble(gsData3D[4][2]);

            double rotationVectorX = Double.parseDouble(gsData3D[5][0]);
            double rotationVectorY = Double.parseDouble(gsData3D[5][1]);
            double rotationVectorZ = Double.parseDouble(gsData3D[5][2]);

            long newRowId = dbh.addCheckPointData(date, time, accuracy, accuracyGps, latitude, longitude, height, speed, longWay,
                    accelerationX, accelerationY, accelerationZ,
                    gravityX, gravityY, gravityZ,
                    gyroscopeX, gyroscopeY, gyroscopeZ,
                    linearAccelerationX, linearAccelerationY, linearAccelerationZ,
                    orientationX, orientationY, orientationZ,
                    rotationVectorX, rotationVectorY, rotationVectorZ,
                    longWayTime);
            if (newRowId == -1) {
                Toast.makeText(ActualDataSenzor.this, getString( R.string.sql_insert ), Toast.LENGTH_LONG).show();
                Log.e("Error", getString( R.string.sql_insert ) );
            }
        } catch (Exception e){
            Log.e("Error", getString( R.string.sql_wrong_data ) + e);
            Toast.makeText(ActualDataSenzor.this, getString( R.string.sql_wrong_data ), Toast.LENGTH_LONG).show();
        }
    }


}