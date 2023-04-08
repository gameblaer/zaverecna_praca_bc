package sk.ukf.wiw_google_earth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.ElevationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShowData extends AppCompatActivity implements OnMapReadyCallback {

    //define
    private final int START_VALUE_TEXTVIEW = 39;
    private final int TEXT_VIEW_NUMBER = 9;

    //attributes
    private final DB_Functions dbh = new DB_Functions(this);
    private ListView gLV;
    private ArrayAdapter gAdapter;
    private SimpleCursorAdapter cadapter;
    private long gId = -1;
    private final TextView[] gShowTextView = new TextView[TEXT_VIEW_NUMBER];
    private Button gShowMap;
    private String gName;

    private int gTypeTravel = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);

        gLV = findViewById(R.id.ShowDataLV);
        gShowMap = findViewById(R.id.showInMap);

        Intent intentRead = getIntent();
        try {
            gId = Long.parseLong(intentRead.getStringExtra("idWay"));
        } catch (Exception ignored) {
        }

        //for textView
        String textViewID;
        int resourceViewID;
        for (int i = 0; i < TEXT_VIEW_NUMBER; i++) {
            textViewID = "textView" + (i + START_VALUE_TEXTVIEW); //radiobuttons start 39 and end 47
            resourceViewID = getResources().getIdentifier(textViewID, "id", getPackageName());
            gShowTextView[i] = findViewById(resourceViewID);
        }

        loadWayData();
        showData();
        addCursorAdapter();
        showMap();
        permision();
    }

    private void permision(){
        //can save file
        if (ContextCompat.checkSelfPermission(ShowData.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) ShowData.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Log.d("File", "You dont have access to save file.");
        }
    }

    private void createFileFast() {
        Cursor cursor = dbh.getCursorData(gId);

        List<String[]> data = new ArrayList<String[]>();
        ArrayList<Double[]> xyz = new ArrayList<>();
        List<LatLng> points = new ArrayList<>();

        do {
            if(cursor.getCount() > 0) {
                Double[] xyzRow = {
                        cursor.getDouble(5),
                        cursor.getDouble(6),
                        cursor.getDouble(7)
                };
                xyz.add( xyzRow );

                points.add( new LatLng( cursor.getDouble(5), cursor.getDouble(6) ) );
            }
        } while (cursor.moveToNext());

        //todo mv treba najprv otestovat, prichystany if nahradi riadok: double[] resultElevation = getElevations(points);
        //double[] resultElevation = getElevations(points);
        double[] resultElevation = new double[points.size()];
        if( gTypeTravel == 2 ) {
            resultElevation = getElevations(points);
        }

        data.clear();
        for(int i = 0 ; i < resultElevation.length; i++){
            Log.d("Points", ". heightGPS(" + resultElevation[i] + ") - " + xyz.get(i)[2] + " = division(" + (xyz.get(i)[2] - resultElevation[i]) + ")" );

            //calculate height if is travel is fly
            if( gTypeTravel == 2 ) {
                String[] row = {
                        xyz.get(i)[1].toString(),
                        xyz.get(i)[0].toString(),
                        String.valueOf(xyz.get(i)[2] - resultElevation[i]),
                };
                data.add(row);
            } else {
                String[] row = {
                        xyz.get(i)[1].toString(),
                        xyz.get(i)[0].toString(),
                        "0"
                };
                data.add(row);
            }
        }

        SaveFile file = new SaveFile();
        try {
            file.createFile(ShowData.this, gName, data );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFileAnimationFast() {

        Cursor cursor = dbh.getCursorData(gId);

        List<String[]> data = new ArrayList<String[]>();
        ArrayList<Double[]> xyz = new ArrayList<>();
        List<LatLng> points = new ArrayList<>();

        double fAttlitude = 2000.0;
        int ANGLE = 80;

        do {
            if(cursor.getCount() > 0) {

                double fTemp;
                double fTilt = cursor.getDouble(22) + fAttlitude;

                //first set
                if( fAttlitude > 1000 ) {
                    fAttlitude = ANGLE - cursor.getDouble(22);
                    fTilt = ANGLE;
                }

                Log.d("Tilt", "( cursor.getDouble(22: " + cursor.getDouble(22) + ") + " + fAttlitude + ") = " + fTilt + " - " + ANGLE + " = " + ( fTilt - ANGLE) );
                if( (fTilt - ANGLE) > 0 ) {
                    fTemp = fTilt - ANGLE; //e.g. -70 + 150 - 80 = 0
                } else {
                    fTemp = 0.0;
                }

                if( gTypeTravel != 2 ) {
                    fTilt = ANGLE;
                }

                Double[] xyzRow = {
                        cursor.getDouble(5),        //latitude
                        cursor.getDouble(6),        //longtitude
                        cursor.getDouble(7) + fTemp,//atlitude
                        cursor.getDouble(8),        //speed
                        cursor.getDouble(21),       //heading
                        fTilt,                                 //tilt naklon vpred vzad
                };
                xyz.add(xyzRow);
                //set value for calculate tilt
                //fAttlitude = ANGLE - xyz.get(0)[5];

                points.add(new LatLng(cursor.getDouble(5), cursor.getDouble(6)));
            }
        } while (cursor.moveToNext());

        double[] resultElevation = new double[points.size()];
        if( gTypeTravel == 2 ) {
            resultElevation = getElevations(points);
        }

        data.clear();
        for(int i = 0 ; i < resultElevation.length; i++){
            Log.d("Points", ". heightGPS(" + resultElevation[i] + ") - " + xyz.get(i)[2] + " = division(" + (xyz.get(i)[2] - resultElevation[i]) + ")" );

            //calculate height if is travel is fly
            if( gTypeTravel == 2 ) {
                //airplan
                String[] row = {
                        xyz.get(i)[1].toString(),                           //latitude
                        xyz.get(i)[0].toString(),                           //longtitude
                        String.valueOf(xyz.get(i)[2] - resultElevation[i]), //atlitude
                        xyz.get(i)[3].toString(),                           //speed
                        xyz.get(i)[4].toString(),                           //heading
                        xyz.get(i)[5].toString(),                           //tilt
                };
                data.add(row);
            } else {
                //walk or road
                String[] row = {
                        xyz.get(i)[1].toString(),
                        xyz.get(i)[0].toString(),
                        "0",
                        xyz.get(i)[3].toString(),
                        xyz.get(i)[4].toString(),
                        xyz.get(i)[5].toString(),
                };
                data.add(row);
            }
        }

        SaveFile file = new SaveFile();
        try {
            file.createFileKmlAnimation(ShowData.this, gName, data );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFile() {
        List<String[]> data = new ArrayList<String[]>();
        Cursor cursor = dbh.getCursorData(gId);
        int count = 0;
        do {
            if( cursor.getColumnCount() > 0 ) {
                //calculate height if is travel is fly
                if( gTypeTravel == 2 ) {
                    double GpsXY = getElevationFromDevice(cursor.getDouble(5), cursor.getDouble(6));
                    double division = (cursor.getDouble(7) - GpsXY);
                    Log.d("Points", ++count + ". heightGPS(" + GpsXY + ") - " + cursor.getDouble(7) + " = division(" + division + ")");
                    String[] row = {
                            cursor.getString(6),
                            cursor.getString(5),
                            String.valueOf(division)
                    };
                    data.add(row);
                } else {
                    String[] row = {
                            cursor.getString(6),
                            cursor.getString(5),
                            "0"
                    };
                    data.add(row);
                }
            }
        } while (cursor.moveToNext());

        SaveFile file = new SaveFile();
        try {
            file.createFile(ShowData.this, gName, data );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double[] getElevations(List<LatLng> points) {
        double[] elevations = new double[points.size()];
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(MyDB.gApiKey)
                .build();
        com.google.maps.model.LatLng[] locations = new com.google.maps.model.LatLng[points.size()];
        for (int i = 0; i < points.size(); i++) {
            //Log.d("Points", "latitude "+ points.get(i).latitude + " longitude "+points.get(i).longitude);
            locations[i] = new com.google.maps.model.LatLng(points.get(i).latitude, points.get(i).longitude);
        }
        try {
            ElevationResult[] elevationResults = ElevationApi.getByPoints(context, locations).await();
            if (elevationResults != null) {
                for (int i = 0; i < elevationResults.length; i++) {
                    elevations[i] = elevationResults[i].elevation;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return elevations;
    }

    private double getElevationFromDevice(double lat, double lng) {
        double elevation = 0.0;
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(MyDB.gApiKey)
                .build();
        com.google.maps.model.LatLng location = new com.google.maps.model.LatLng(lat, lng);
        try {
            ElevationResult elevationResult = ElevationApi.getByPoint(context, location).await();
            if (elevationResult != null ) {
                elevation = elevationResult.elevation;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return elevation;
    }

    private void endActivity() {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

    private void showMap(){
        gShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( ShowData.this, Maps3D.class);
                intent.putExtra("idWay", Long.toString(gId) );
                startActivity(intent);
            }
        });
    }

    private void loadWayData() {

        String[] temp = dbh.getWayData(gId);

        for (int i = 0; i < temp.length; i++)
        {
            //get name
            if( i == 1 ){
                gName = temp[i];
            }
            //load way name
            if( i == 2 ) {
                switch( Integer.parseInt(temp[i]) ){
                    case 0 :
                        temp[i] = getString(R.string.turist);
                        gTypeTravel = 0;
                        break;
                    case 1 :
                        temp[i] = getString(R.string.journy);
                        gTypeTravel = 1;
                        break;
                    case 2 :
                        temp[i] = getString(R.string.by_air);
                        gTypeTravel = 2;
                        break;
                }
            }
            //load way name
            if( i == 3 ) {
                switch( Integer.parseInt(temp[i]) ){
                    case 0 :
                        temp[i] = getString(R.string._1_minute);
                        break;
                    case 1 :
                        temp[i] = getString(R.string._2_minutes);
                        break;
                    case 2 :
                        temp[i] = getString(R.string._5_minutes);
                        break;
                    case 3 :
                        temp[i] = getString(R.string._10_minutes);
                        break;
                    case 4 :
                        temp[i] = getString(R.string._30_minutes);
                        break;
                    case 5 :
                        temp[i] = getString(R.string.actually_settings);
                        break;
                    case 6 :
                        temp[i] = getString(R.string.when_you_move_away);
                        break;
                }
            }
            gShowTextView[i].setText(temp[i]);
        }
        String sTemp = String.valueOf( dbh.numberODatafCount(gId) );
        gShowTextView[gShowTextView.length-1].setText(sTemp);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //CreateFile
        if (item.getItemId() == R.id.downloadFile) {
            createFileFast();
        }

        //CreateFileAnimation
        if (item.getItemId() == R.id.downloadAnimation) {
            createFileAnimationFast();
        }
        //Rename
        if (item.getItemId() == R.id.RenameShowData) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.renameName));
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                String text = input.getText().toString();
                // spracovanie textu
                dbh.updateWayName(gId, text);

                loadWayData();
                showData();
                addCursorAdapter();
            });

            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());

            builder.show();
        }
        //Delete
        if (item.getItemId() == R.id.DelerteWayData) {

            final boolean[] isReturn = {false};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.delete)+" ?");
            builder.setMessage(getString(R.string.deleteQuestion));
            builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                //show what delete
                Toast.makeText(ShowData.this, getString(R.string.deleted)+" id("+gId+") "+getString(R.string.name)+"("+dbh.getWayName(gId)+").", Toast.LENGTH_LONG).show();

                //detele
                dbh.deleteWay(gId);

                setResult(RESULT_OK, new Intent());
                finish();
                isReturn[0] = true;
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                // akcia pre stlačenie tlačidla Zrušiť
                isReturn[0] = false;
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            return isReturn[0];
        }
        //Back
        if (item.getItemId() == R.id.BackShowData) {
            //startActivityForResult(new Intent(MapsActivity.this, Settings.class), 1);
            setResult(RESULT_CANCELED, new Intent());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menushowdata, menu);

        return true;
    }

    private void showData(){
        gAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, dbh.getCheckPointData(gId));
        gLV.setAdapter(gAdapter);
    }

    private void addCursorAdapter(){

        Cursor cursor = dbh.getCursorData(gId);
        if (cursor != null) {
            cadapter = new SimpleCursorAdapter(this, R.layout.show_data, cursor, new String[] {
                    MyDB.DATA.DATA_ID,
                    MyDB.DATA.DATE,
                    MyDB.DATA.TIME,
                    MyDB.DATA.ACCURACY,
                    MyDB.DATA.ACCURACY_GPS,
                    MyDB.DATA.LATITUDE,
                    MyDB.DATA.LONGTITUDE,
                    MyDB.DATA.HEIGHT,
                    MyDB.DATA.SPEED,
                    MyDB.DATA.ACCELERACTION_X,
                    MyDB.DATA.ACCELERACTION_Y,
                    MyDB.DATA.ACCELERACTION_Z,
                    MyDB.DATA.GRAVITY_X,
                    MyDB.DATA.GRAVITY_Y,
                    MyDB.DATA.GRAVITY_Z,
                    MyDB.DATA.GYROSCOPE_X,
                    MyDB.DATA.GYROSCOPE_Y,
                    MyDB.DATA.GYROSCOPE_Z,
                    MyDB.DATA.LINEAR_ACCELERATION_X,
                    MyDB.DATA.LINEAR_ACCELERATION_Y,
                    MyDB.DATA.LINEAR_ACCELERATION_Z,
                    MyDB.DATA.ORIENTATION_X,
                    MyDB.DATA.ORIENTATION_Y,
                    MyDB.DATA.ORIENTATION_Z,
                    MyDB.DATA.ROTATION_VECTOR_X,
                    MyDB.DATA.ROTATION_VECTOR_Y,
                    MyDB.DATA.ROTATION_VECTOR_Z,
                    MyDB.DATA.LONG_WAY,
                    MyDB.DATA.LONG_WAY_TIME,
                    MyDB.DATA.ID_WAY,
            }, new int[]{
                    R.id.showdata0,
                    R.id.showdata1,
                    R.id.showdata2,
                    R.id.showdata3,
                    R.id.showdata4,
                    R.id.showdata5,
                    R.id.showdata6,
                    R.id.showdata7,
                    R.id.showdata8,
                    R.id.showdata9,
                    R.id.showdata10,
                    R.id.showdata11,
                    R.id.showdata12,
                    R.id.showdata13,
                    R.id.showdata14,
                    R.id.showdata15,
                    R.id.showdata16,
                    R.id.showdata17,
                    R.id.showdata18,
                    R.id.showdata19,
                    R.id.showdata20,
                    R.id.showdata21,
                    R.id.showdata22,
                    R.id.showdata23,
                    R.id.showdata24,
                    R.id.showdata25,
                    R.id.showdata26,
                    R.id.showdata27,
                    R.id.showdata28,
                    R.id.showdata29,
            }, 0);

            cadapter.setViewBinder((view, cursor1, columnIndex) -> {
                String[] columnNames = {
                        "Data_id",
                        "Date",
                        "Time",
                        "Accuracy",
                        "AccuracyGps",
                        "Latitude",
                        "Longtitude",
                        "Height",
                        "Speed",
                        "Acceleraction_x",
                        "Acceleraction_y",
                        "Acceleraction_z",
                        "Gravity_x",
                        "Gravity_y",
                        "Gravity_z",
                        "Gyroscope_x",
                        "Gyroscope_y",
                        "Gyroscope_z",
                        "Linear_acceleration_x",
                        "Linear_acceleration_y",
                        "Linear_acceleration_z",
                        "Orientation_x",
                        "Orientation_y",
                        "Orientation_z",
                        "Rotation_vector_x",
                        "Rotation_vector_y",
                        "Rotation_vector_z",
                        "Long_way",
                        "Long_way_time",
                        "Id_way",
                };
                String[] arrTemp = new String[] {
                        MyDB.DATA.DATA_ID,
                        MyDB.DATA.DATE,
                        MyDB.DATA.TIME,
                        MyDB.DATA.ACCURACY,
                        MyDB.DATA.ACCURACY_GPS,
                        MyDB.DATA.LATITUDE,
                        MyDB.DATA.LONGTITUDE,
                        MyDB.DATA.HEIGHT,
                        MyDB.DATA.SPEED,
                        MyDB.DATA.ACCELERACTION_X,
                        MyDB.DATA.ACCELERACTION_Y,
                        MyDB.DATA.ACCELERACTION_Z,
                        MyDB.DATA.GRAVITY_X,
                        MyDB.DATA.GRAVITY_Y,
                        MyDB.DATA.GRAVITY_Z,
                        MyDB.DATA.GYROSCOPE_X,
                        MyDB.DATA.GYROSCOPE_Y,
                        MyDB.DATA.GYROSCOPE_Z,
                        MyDB.DATA.LINEAR_ACCELERATION_X,
                        MyDB.DATA.LINEAR_ACCELERATION_Y,
                        MyDB.DATA.LINEAR_ACCELERATION_Z,
                        MyDB.DATA.ORIENTATION_X,
                        MyDB.DATA.ORIENTATION_Y,
                        MyDB.DATA.ORIENTATION_Z,
                        MyDB.DATA.ROTATION_VECTOR_X,
                        MyDB.DATA.ROTATION_VECTOR_Y,
                        MyDB.DATA.ROTATION_VECTOR_Z,
                        MyDB.DATA.LONG_WAY,
                        MyDB.DATA.LONG_WAY_TIME,
                        MyDB.DATA.ID_WAY,
                };
                //for textView
                String textViewID;
                int resourceViewID;
                for (int i = 0; i < arrTemp.length; i++)
                {
                    textViewID = "showdata" + (i);
                    resourceViewID = getResources().getIdentifier(textViewID, "id", getPackageName());

                    if (view.getId() == resourceViewID) {
                        @SuppressLint("Range") String data = cursor1.getString(cursor1.getColumnIndex(arrTemp[i]));
                        TextView dateTextView = (TextView) view;
                        String temp = columnNames[i] + ": " +data;
                        dateTextView.setText(temp);
                        return true;
                    }
                }

                return false;
            });

            gLV.setAdapter(cadapter);
        } else {
            showData();
        }

        gLV.setOnItemClickListener((adapterView, view, i, l) -> {
            Cursor c = ((SimpleCursorAdapter) gLV.getAdapter()).getCursor();
            c.moveToPosition(i);
            //Log.d("CADAPTER",  c.getLong(0)+"" );

            //Toast.makeText(ShowData.this, "Funguje", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(History.this, Restika.class);
//            intent.putExtra("idWay", Long.toString(gId) );
//            startActivityForResult(intent, 1);
        });

        gLV.setOnItemLongClickListener((adapterView, view, i, l) -> {

            //Delete
            final boolean[] isReturn = {false};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.delete)+" ?");
            builder.setMessage(getString(R.string.deleteQuestion));
            builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                //detele
                Cursor c = ((SimpleCursorAdapter) gLV.getAdapter()).getCursor();
                c.moveToPosition(i);
                long id = c.getLong(0);

                //show what delete
                Toast.makeText(ShowData.this, getString(R.string.deleted)+" id("+id+") "+getString(R.string.name)+"("+dbh.getWayName(id)+").", Toast.LENGTH_SHORT).show();

                //detele
                dbh.deleteData(id);
                //refresh lv
                loadWayData();
                showData();
                addCursorAdapter();

                isReturn[0] = true;
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                // akcia pre stlacenie tlacidla Zrusit
                isReturn[0] = false;
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            return isReturn[0];

            //return true blokovat onItemClickListener
            //return false neblokovat onItemClickListener

        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
}