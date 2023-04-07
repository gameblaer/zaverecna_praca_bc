package sk.ukf.wiw_google_earth;

public final class MyDB {

    public static class DATA {
        public static final String TABLE_DATA_NAME = "data";
        public static final String DATA_ID = "_id";
        public static final String DATE = "day";
        public static final String TIME = "time";
        public static final String ACCURACY = "accuracy";
        public static final String ACCURACY_GPS = "gps_accuracy";
        public static final String LATITUDE = "latitude";
        public static final String LONGTITUDE = "longtitude";
        //public static final String INCLANATION = "inclanation";
        public static final String HEIGHT = "height";
        public static final String SPEED = "speed";
        public static final String ACCELERACTION_X = "acceleraction_x";
        public static final String ACCELERACTION_Y = "acceleraction_y";
        public static final String ACCELERACTION_Z = "acceleraction_z";
        public static final String GRAVITY_X = "gravity_x"; //Similarly ACCELERATION
        public static final String GRAVITY_Y = "gravity_y";
        public static final String GRAVITY_Z = "gravity_z";
        public static final String GYROSCOPE_X = "gyroscope_x";
        public static final String GYROSCOPE_Y = "gyroscope_y";
        public static final String GYROSCOPE_Z = "gyroscope_z";
        public static final String LINEAR_ACCELERATION_X = "linear_acceleration_x"; //Similarly GYROSCOPE
        public static final String LINEAR_ACCELERATION_Y = "linear_acceleration_y";
        public static final String LINEAR_ACCELERATION_Z = "linear_acceleration_z";
        public static final String ORIENTATION_X = "orientation_x"; //EXCLUSIVE
        public static final String ORIENTATION_Y = "orientation_y";
        public static final String ORIENTATION_Z = "orientation_z";
        public static final String ROTATION_VECTOR_X = "rotation_vector_x"; //EXCLUSIVE
        public static final String ROTATION_VECTOR_Y = "rotation_vector_y";
        public static final String ROTATION_VECTOR_Z = "rotation_vector_z";
        public static final String LONG_WAY = "long_way";
        public static final String LONG_WAY_TIME = "long_way_time";
        public static final String ID_WAY = "id_way";
    }

    public static class WAY {
        public static final String TABLE_WAY_NAME = "way_tracking";
        public static final String WAY_ID = "_id";
        public static final String NAME = "name";
        public static final String TRAVEL_WITH = "travel_with"; //travel with
        public static final String REFRESH_OPTION = "refresh_option";
        public static final String REFRESH_OBTAIN_DATA = "refresh_time";
        public static final String DATETIME = "day_time";
        public static final String COMPLET_LONG_WAY = "complet_long_way";
        public static final String COMPLET_LONG_WAY_TIME = "complet_long_way_time";
    }

    public static final String gApiKey = "AIzaSyCqJEVNMXDP18Kf7aTyIkygtjFT0R2v3p8";

    /**
     //read all enums
     for (NameKeys name : NameKeys.values()) {
     if( name.value == 0 ) continue;
     sp.getString( name.toString(),"" );
     System.out.println("test name" + name);
     }
     */
    public enum NameKeys {
        UserSettings(0),
        SettingsCheckedRadioButtonIndex(1),
        RereshTime(2),
        TextViewValue(3),
        MainCheckedRadioButtonIndex(4),
        FirstSet(5);

        NameKeys(int value) {
            this.value = value;
        }

        private final int value;

        private final String[] gNameKey = { "UserSettings", "SettingsCheckedRadioButtonIndex", "RereshTime", "TextViewValue", "MainCheckedRadioButtonIndex", "FirstSet" };

        public String KeyName() {
            return gNameKey[value];
        }

        public int length() {
            return gNameKey.length;
        }
    }

    public enum MainOptions {
        Tourist(0),
        AsphaltRoad(1),
        ByAir(2);

        MainOptions(int value) {
            this.value = value;
        }

        private final int value;

        public String stringName() {
            return gNameKey[value];
        }

        public int number() { return value; }

        private final String[] gNameKey = { "Tourist", "AsphaltRoad", "ByAir" };
    }
}
