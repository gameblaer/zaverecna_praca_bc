package sk.ukf.wiw_google_earth;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.ElevationResult;

import java.util.ArrayList;
import java.util.List;

import sk.ukf.wiw_google_earth.databinding.ActivityMaps3DBinding;

//import com.google.maps.model.LatLng as MapsLatLng;

public class Maps3D extends AppCompatActivity implements OnMapReadyCallback {

    private final DB_Functions dbh = new DB_Functions(this);

    private GoogleMap mMap;
    private ActivityMaps3DBinding binding;
    private ArrayAdapter gAdapter;
    private long gId = -1;
    private LocationManager mLocationManager = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMaps3DBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map3d);
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Intent intentRead = getIntent();
        try {
            gId = Long.parseLong(intentRead.getStringExtra("idWay"));
        } catch (Exception ignored) {
        }

        showData();
    }

    private void showData() {
        gAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, dbh.getCheckPointData(gId));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
         */
        /*
        LatLng jdpLatLng = new LatLng(48.30143093,18.06246514);
        String temp = getResources().getString(R.string.you_are_here);
        MarkerOptions markerOptions = new MarkerOptions().position(jdpLatLng).title(temp);
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(jdpLatLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jdpLatLng, 16f));
*/

        Cursor cursor = dbh.getCursorData(gId);

        ArrayList<Double[]> xyz = new ArrayList<>();
        List<LatLng> points = new ArrayList<>();

        if(!cursor.moveToFirst()){
            Log.d("Points", "NoFirst" );
            endActivity();
            return;
        }

        do {
            if( cursor.getColumnCount() > 0 ) {
                Double[] xyzRow = {
                        cursor.getDouble(5),
                        cursor.getDouble(6),
                        cursor.getDouble(7)
                };
                xyz.add( xyzRow );

                points.add( new LatLng( cursor.getDouble(5), cursor.getDouble(6) ) );
            }
        } while (cursor.moveToNext());
        Log.d("Points", "velkost pola " + points.size());

        double[] resultElevation = getElevations(points);

        Log.d("Points", "ele hotovo ");

        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.reddot);
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);

        for(int i = 0 ; i < resultElevation.length; i++) {
            MarkerOptions markerOptions1 = new MarkerOptions()
                    .position(points.get(i))
                    .zIndex(xyz.get(i)[2].floatValue())
                    .title(getString(R.string.height))
                    .icon(icon);
            googleMap.addMarker(markerOptions1);
            options.add(points.get(i));
        }
        Log.d("Points", "cyklus1");
        for(int i = 0 ; i < resultElevation.length; i++){
            Log.d("Points", ". heightGPS(" + resultElevation[i] + ") - " + xyz.get(i)[2] + " = division(" + (xyz.get(i)[2] - resultElevation[i]) + ")" );
            Double[] xyzRow = {
                    xyz.get(i)[0],
                    xyz.get(i)[1],
                    xyz.get(i)[2] - resultElevation[i],
            };

            xyz.set(i, xyzRow );
        }
        Log.d("Points", "cyklus2");

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(points.get(0)));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 16f));

        googleMap.addPolyline(options);

        //po kliknuti vypise v akej je vyske dany oznaceny bod
        googleMap.setOnMarkerClickListener(marker -> {
            double position = marker.getZIndex();
            String title = marker.getTitle();
            marker.setTitle(title + " ("+  getString(R.string.height) +": " + position + "m)");
            return false;
        });
        Log.d("Points", "zobraz");
    }

    public double truncateDouble(double d, int numberOfPoistionDecimal) {
        int a = (int) (Math.round(d * Math.pow(10, numberOfPoistionDecimal)));
        //Log.d("Points", "na kolko " + Math.pow(10, numberOfPoistionDecimal) + " double " +(d * Math.pow(10, numberOfPoistionDecimal)+ " round " +Math.round(d * Math.pow(10, numberOfPoistionDecimal))+  " a " +a));
        return (double) a / (double) Math.pow(10, numberOfPoistionDecimal);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.maps3dSpet) {
            //startActivityForResult(new Intent(MapsActivity.this, Settings.class), 1);
            endActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menumaps3d, menu);

        return true;
    }
}