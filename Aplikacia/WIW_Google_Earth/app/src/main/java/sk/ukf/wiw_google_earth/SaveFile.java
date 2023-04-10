package sk.ukf.wiw_google_earth;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SaveFile extends AppCompatActivity {
    private static String FILE_NAME = "NoName.kml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

    }

    public void createFile(Context context, String name, List<String[]> data) throws IOException {

        if( !askForPermissions(context) ) return;

        if( name.length() > 0 ) FILE_NAME = name + ".kml";
        else                    FILE_NAME = "NoName.kml";

        // vytvorenie súboru v priečinku aplikacie na vnutornej pameti zariadenia
        File root = new File(context.getExternalFilesDir(null), String.valueOf(R.string.app_name));
        if (!root.exists()) {
            root.mkdirs();
        }
        File file = new File(root, FILE_NAME);
        //Log.d("File", "Som tu '" + context + "'");
        try {
            FileWriter writer = new FileWriter(file);

            //head tag start
            String[] head = head(true,name);
            //create head
            for (String s : head) {
                writer.append(s);
            }
            //body
            writer.append( placemarkFullData(data) );

            //end tag head
            head = head(false, "");
            for (String s : head) {
                writer.append(s);
            }

            writer.flush();
            writer.close();

            String sTemp = R.string.createFile + "" + root; //this line must because drop app
            Toast.makeText(context, sTemp, Toast.LENGTH_LONG).show();
            Log.d("File", "Created '" + root + "'");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,getString( R.string.createFileError), Toast.LENGTH_LONG).show();
        }
    }

    private boolean askForPermissions(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Log.d("File", "You dont have access");
            return false;
        }
        return true;
    }

    private String placemarkFullData(List<String[]> data){
        String full = "";
        String[] tempplate;
        for (int i = 0; i < data.size() ; i++ ) {
            String[] rows = data.get(i);
            if( i == 0 ){
                //head tag
                tempplate = placemark(true ,rows[0],rows[1]);
                for (String s : tempplate) {
                    full = full + s;
                }
            }
            String temp = "\t\t\t\t"+rows[0] +","+ rows[1] +","+ rows[2]+"\n";
            full = full + temp;
        }
        //end tag
        tempplate = placemark(false,null,null);
        for (String s : tempplate) {
            full = full + s;
        }

        return full;
    }

    /**
     * version 1 save to file, only x,y,z
     */
    private String[] head(boolean top, String name){
/*
<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2"
 xmlns:gx="http://www.google.com/kml/ext/2.2">

  <Document>
    <name>balloonVisibility Example</name>
    <open>1</open>
    <Style id="exampleStyleDocument">
      <LabelStyle>
        <color>ff0000cc</color>
      </LabelStyle>
    </Style>

  </Document>
</kml>
*/
        if( top ){
            return new String[]{
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n",
                    " <kml xmlns=\"http://www.opengis.net/kml/2.2\"\n",
                    "  creator=\"WIW\"\n",
                    "  xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n\n",
                    "\t<Document>\n",
                    "\t\t<name>"+name+"</name>\n",
                    "\t\t<open>1</open>\n",
                    "\t\t<Style id=\"exampleStyleDocument\">\n",
                    "\t\t\t<LabelStyle>\n",
                    "\t\t\t\t<color>ff0000cc</color>\n",
                    "\t\t\t</LabelStyle>\n",
                    "\t\t</Style>\n\n",
                    //body
            };
        } else {
            return new String[]{
                    "\t</Document>\n",
                    "</kml>"
            };
        }
    }

    private String[] placemark(boolean top ,String longtitude, String latitude){
                    /*
                    <Placemark>
                      <name>gx:altitudeMode Example</name>
                      <LookAt>
                        <longitude>18.0633</longitude>
                        <latitude>48.2951</latitude>
                        <heading>-60</heading>
                        <tilt>70</tilt>
                        <range>6300</range>
                        <gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>
                      </LookAt>
                      <LineString>
                        <extrude>1</extrude>
                        <gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>
                        <coordinates>
                          18.0633,48.2951,379.437 //longtitude+","+latitude+","+atlitude,
                          18.0668,48.2961,350.858
                          18.0672,48.2962,344.329
                        </coordinates>
                      </LineString>
                    </Placemark>
                     */

        if( top ){
            return new String[]{
                    "\t\t<Placemark>\n",
                    "\t\t\t<name>gx:altitudeMode Example</name>\n",
                    "\t\t\t<LookAt>\n",
                    "\t\t\t\t<longitude>" + longtitude + "</longitude>\n",
                    "\t\t\t\t<latitude>" + latitude + "</latitude>\n",
                    "\t\t\t\t<heading>-60</heading>\n",
                    "\t\t\t\t<tilt>70</tilt>\n",
                    "\t\t\t\t<range>6300</range>\n",
                    "\t\t\t\t<gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>\n",
                    "\t\t\t</LookAt>\n",
                    "\t\t\t<LineString>\n",
                    "\t\t\t\t<extrude>1</extrude>\n",
                    "\t\t\t\t<tessellate>1</tessellate>\n",
                    "\t\t\t\t<gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>\n",
                    "\t\t\t\t<coordinates>\n"
            };
        } else {
            return new String[]{
                    //"	\t\t\t\t"+longtitude+","+latitude+","+atlitude,
                    "\t\t\t\t</coordinates>\n",
                    "\t\t\t</LineString>\n",
                    "\t\t</Placemark>\n"
            };
        }
    }

    /**
     * kml animation
     * show x,y,z, heading
     */
    public void createFileKmlAnimation(Context context, String name, List<String[]> data) throws IOException {

        if( !askForPermissions(context) ) return;

        if( name.length() > 0 ) FILE_NAME =  name + "_Animation" + ".kml";
        else                    FILE_NAME = "AnimationNoName.kml";

        // vytvorenie suboru v priecinku Documents na vnutornej pameti zariadenia
        File root = new File(context.getExternalFilesDir(null), String.valueOf(R.string.app_name));
        if (!root.exists()) {
            root.mkdirs();
        }
        File file = new File(root, FILE_NAME);

        try {
            FileWriter writer = new FileWriter(file);

            //head tag start
            String[] head = headKmlAnimation(true, name);
            //create head
            for (String s : head) {
                writer.append(s);
            }

            //body, read data row is data.get(i), create tag for one row
            for (int i = 0; i < data.size() ; i++ ) {
                writer.append( kmlAnimationBody(data.get(i)) );
            }

            //end tag head
            head = headKmlAnimation(false, "");
            for (String s : head) {
                writer.append(s);
            }

            writer.flush();
            writer.close();

            String temp = R.string.createFile + "" + root;
            Toast.makeText(context, temp, Toast.LENGTH_LONG).show();
            Log.d("File", "Created '" + root + "'");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.createFileError, Toast.LENGTH_SHORT).show();
        }
    }

    private String[] headKmlAnimation(boolean top, String name){
/*
<?xml version="1.0" encoding="utf-8"?>
<kml xmlns:gx="http://www.google.com/kml/ext/2.2" creator="SkyDemon Plan v3.16.6.0" xmlns="http://www.opengis.net/kml/2.2">
  <Document>
    <name>LZNI Nitra - LZNI Nitra 202302280759</name>
    <gx:Tour>
      <name>Track Log Fly-Through</name>
      <gx:Playlist>

      </gx:Playlist>
    </gx:Tour>
  </Document>
</kml>
*/
        if( top ){
            return new String[]{
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n",
                    "<kml xmlns:gx=\"http://www.google.com/kml/ext/2.2\" creator=\"WIW\" xmlns=\"http://www.opengis.net/kml/2.2\">\n",
                    "\t<Document>\n",
                    "\t\t<name>"+name+"</name>\n",
                    "\t\t<gx:Tour>\n",
                    "\t\t<name>Track Log Fly-Through</name>\n",
                    "\t\t\t<gx:Playlist>\n",
                    //body
            };
        } else {
            return new String[]{
                    "\t\t\t</gx:Playlist>\n",
                    "\t\t</gx:Tour>\n",
                    "\t</Document>\n",
                    "</kml>"
            };
        }
    }

    private String kmlAnimationBody( String[] rows ){
        String full = "";
        String[] tempplate;

        /*add row to string,
        0 = latitude
        1 = longitude
        2 = altitude
        3 = speed
        4 = heading
        5 = tilt
        6 = roll
         */
        double second = 2.5;
        try {
            //time = ((900.0 - Double.parseDouble(rows[0])) / 900.0) * 9.0 + 1.0; //pre rychlost 900kmh / 1s a 0kmh / 10s
            double speed = Double.parseDouble(rows[0]);
            if(speed <= 0) {
                second = 5;
            } else if(speed >= 100) {
                second = 1;
            } else {
                second = (100.0 - speed) / 100.0 * 4.0 + 1.0;
            }
        } catch (Exception ignore){}
        tempplate = kmlAnimationTag(rows[0],rows[1],rows[2],rows[4],rows[5], second, rows[6]);
        for (String s : tempplate) {
            full = full + s;
        }

        return full;
    }

    private String[] kmlAnimationTag(String longtitude, String latitude, String altitude, String heading, String tilt, double time, String roll){
        /*
         <gx:FlyTo>
          <gx:duration>1.00</gx:duration>
          <gx:flyToMode>smooth</gx:flyToMode>
          <Camera>
            <longitude>18.1359</longitude>
            <latitude>48.2767</latitude>
            <altitude>136</altitude>
            <heading>324</heading>
            <tilt>80</tilt>
            <altitudeMode>absolute</altitudeMode>
          </Camera>
        </gx:FlyTo>
         */
        return new String[]{
                "\t\t\t\t<gx:FlyTo>\n",
                "\t\t\t\t\t<gx:duration>"+time+"</gx:duration>\n",
                "\t\t\t\t\t<gx:flyToMode>smooth</gx:flyToMode>\n",
                "\t\t\t\t\t<Camera>\n",
                "\t\t\t\t\t\t<longitude>"+longtitude+"</longitude>\n",
                "\t\t\t\t\t\t<latitude>"+latitude+"</latitude>\n",
                "\t\t\t\t\t\t<altitude>"+altitude+"</altitude>\n",
                "\t\t\t\t\t\t<heading>"+heading+"</heading>\n",
                "\t\t\t\t\t\t<tilt>"+tilt+"</tilt>\n",
                "\t\t\t\t\t\t<roll>"+roll+"</roll>\n",
                "\t\t\t\t\t\t<range>0</range>\n",
                "\t\t\t\t\t\t<altitudeMode>absolute</altitudeMode>\n",
                "\t\t\t\t\t</Camera>\n",
                "\t\t\t\t</gx:FlyTo>\n",
        };
    }
}


