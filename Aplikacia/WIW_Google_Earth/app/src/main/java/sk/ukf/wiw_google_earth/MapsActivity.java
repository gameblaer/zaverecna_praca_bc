package sk.ukf.wiw_google_earth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

import sk.ukf.wiw_google_earth.databinding.ActivityMapsBinding;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int INTERVAL = 1000;

    private Timer timer;

    private double gLatitude = 48.308372584007856;
    private double gLongtitude = 18.076631443498798;
    private boolean gbFirstuse = true;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Marker mMarker;
    private boolean mIsMarkerVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sk.ukf.wiw_google_earth.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //timer
        timer = new Timer();
        startTimer();

    }

    private void endActivity() {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        /*
        LatLng jdpLatLng = new LatLng(gLatitude, gLongtitude);
        String temp = getResources().getString(R.string.you_are_here);
        MarkerOptions markerOptions = new MarkerOptions().position(jdpLatLng).title(temp);
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(jdpLatLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jdpLatLng, 16f));
         */

        //for refresh map
        mMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (mMarker != null) {
                    mMarker.remove();
                }
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMarker = mMap.addMarker(new MarkerOptions().position(latLng));
                if (!mIsMarkerVisible) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    mIsMarkerVisible = true;
                }
            }
        });
    }

    public void refreshMap() {
        mMap.clear();
        mMarker = null;
        mIsMarkerVisible = false;
        //onMapReady(mMap);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            getCurrentPosition();
            refreshMap();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }
    };

    private void GpsRequest() {
        if (
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            //Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkGPSLocation() {

        boolean gps_enabled;

        gps_enabled = !(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);

        //System.out.println("boolean gps " + gps_enabled);

        if (gps_enabled) {
            return true;
        } else {
            GpsRequest();
        }
        return false;
    }

    private void getCurrentPosition() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);

        if (    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if( gbFirstuse ){
            //gbFirstuse = false;
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
        }

        Location location = locationManager.getLastKnownLocation(provider);
        if (location == null)
            return;

        gLatitude = location.getLatitude();
        gLongtitude = location.getLongitude();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.BackFromMaps) {
            //startActivityForResult(new Intent(MapsActivity.this, Settings.class), 1);
            endActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menumaps, menu);

        return true;
    }

    /*
    private void showMap() {

        // Získanie referencie na objekt Google Earth
        GoogleEarth ge = GoogleEarth.getInstance();
        // Vytvorenie prvého bodu na mape
        Point point1 = ge.createPoint(48.8584, 2.2945);
        //Vytvorenie druhého bodu na mape
        Point point2 = ge.createPoint(51.5074, 0.1278);
        // Vytvorenie žltej čiary medzi bodmi
        LineString lineString = ge.createLineString(point1, point2); lineString.setColor(Color.YELLOW);
        // Zobrazenie čiary na mape
        ge.getFeatures().appendChild(lineString);
    }

    public static class GoogleEarth {
        private static GoogleEarth instance; private GoogleEarth(){

        }
        public static GoogleEarth getInstance(){
            if (instance == null){
                instance = new GoogleEarth();
            }
            return instance;
        }

        public Point createPoint(double latitude, double longitude){
            return new Point(latitude, longitude);
        }
        public LineString createLineString(Point point1, Point point2){
            return new LineString(point1, point2);
        }

    }

    public class LineString {
        private Point point1;
        private Point point2;
        private Color color;


        public LineString(Point point1, Point point2) {
            this.point1 = point1; this.point2 = point2;
        }

        public void setColor(Color color){
            this.color = color;
        }

        public Point getPoint1(){
            return this.point1;
        }

        public Point getPoint2(){
            return this.point2;
        }

        public Color getColor(){
            return this.color;
        }
    }
     */

    private void startTimer(){
        //gps
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    //gps
                    if (checkGPSLocation()){
                        getCurrentPosition();
                        //refreshMap();
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, INTERVAL);
    }
}