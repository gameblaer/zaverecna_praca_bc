package sk.ukf.wiw_google_earth;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;


public class DB_Functions extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "WIW";
    private static final int DOUBLE_COUNTER_DATA = 25;
    private static final int OFFSET_DATA = 3;

    private long gId;

    public DB_Functions(Context context){
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //WAY
        String TABLE_WAY = "CREATE TABLE " +
                MyDB.WAY.TABLE_WAY_NAME + "(" +
                MyDB.WAY.WAY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MyDB.WAY.NAME + " TEXT DEFAULT NULL," +
                MyDB.WAY.TRAVEL_WITH + " INTEGER NOT NULL," +
                MyDB.WAY.REFRESH_OPTION + " INTEGER NOT NULL," +
                MyDB.WAY.REFRESH_OBTAIN_DATA + " INTEGER NOT NULL," +
                MyDB.WAY.DATETIME + " DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," + // (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))," +
                MyDB.WAY.COMPLET_LONG_WAY + " INTEGER NOT NULL DEFAULT 0," +
                MyDB.WAY.COMPLET_LONG_WAY_TIME + " TIME NOT NULL DEFAULT 0)";
        //CREATE TABLE way_tracking (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT DEFAULT NULL, transport INTEGER NOT NULL, refresh_option INTEGER NOT NULL, refresh_time INTEGER NOT NULL, day_time DATETIME NOT NULL, complet_long_way INTEGER DEFAULT 0, complet_long_way_time TIME DEFAULT NULL);
        Log.d("TABLE_WAY %s", TABLE_WAY);
        db.execSQL(TABLE_WAY);

        String TABLE_DATA = "CREATE TABLE " +
                MyDB.DATA.TABLE_DATA_NAME + "(" +
                MyDB.DATA.DATA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + //0
                MyDB.DATA.DATE + " DATE NOT NULL," +
                MyDB.DATA.TIME + " TIME NOT NULL," +
                MyDB.DATA.ACCURACY + " DOUBLE NOT NULL," +
                MyDB.DATA.ACCURACY_GPS + " DOUBLE NOT NULL," +
                MyDB.DATA.LATITUDE + " DOUBLE NOT NULL," +
                MyDB.DATA.LONGTITUDE + " DOUBLE NOT NULL," +
                //MyDB.DATA.INCLANATION + " INTEGER DEFAULT 0," +
                MyDB.DATA.HEIGHT + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.SPEED + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.ACCELERACTION_X + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.ACCELERACTION_Y + " DOUBLE NOT NULL DEFAULT 0," +       //10
                MyDB.DATA.ACCELERACTION_Z + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.GRAVITY_X + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.GRAVITY_Y + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.GRAVITY_Z + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.GYROSCOPE_X + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.GYROSCOPE_Y + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.GYROSCOPE_Z + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.LINEAR_ACCELERATION_X + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.LINEAR_ACCELERATION_Y + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.LINEAR_ACCELERATION_Z + " DOUBLE NOT NULL DEFAULT 0," +  //20
                MyDB.DATA.ORIENTATION_X + " DOUBLE NOT NULL DEFAULT 0," +    //heading
                MyDB.DATA.ORIENTATION_Y + " DOUBLE NOT NULL DEFAULT 0," +    //tilt
                MyDB.DATA.ORIENTATION_Z + " DOUBLE NOT NULL DEFAULT 0," +    //roll
                MyDB.DATA.ROTATION_VECTOR_X + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.ROTATION_VECTOR_Y + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.ROTATION_VECTOR_Z + " DOUBLE NOT NULL DEFAULT 0," +
                MyDB.DATA.LONG_WAY + " DOUBLE NOT NULL," +
                MyDB.DATA.LONG_WAY_TIME + " TIME NOT NULL," +
                MyDB.DATA.ID_WAY + " INTEGER NOT NULL)";

        /*
        CREATE TABLE data (_id INTEGER PRIMARY KEY AUTOINCREMENT, day DATE NOT NULL, time TIME NOT NULL, latitude DOUBLE NOT NULL, longtitude DOUBLE NOT NULL, height DOUBLE DEFAULT 0, speed DOUBLE NOT NULL, acceleraction_x DOUBLE DEFAULT 0, acceleraction_y DOUBLE DEFAULT 0, acceleraction_z DOUBLE DEFAULT 0, gravity_x DOUBLE DEFAULT 0, gravity_y DOUBLE DEFAULT 0, gravity_z DOUBLE DEFAULT 0, gyroscope_x DOUBLE DEFAULT 0, gyroscope_y DOUBLE DEFAULT 0, gyroscope_z DOUBLE DEFAULT 0, linear_acceleration_x DOUBLE DEFAULT 0, linear_acceleration_y DOUBLE DEFAULT 0, linear_acceleration_z DOUBLE DEFAULT 0, orientation_x DOUBLE DEFAULT 0, orientation_y DOUBLE DEFAULT 0, orientation_z DOUBLE DEFAULT 0, rotation_vector_x DOUBLE DEFAULT 0, rotation_vector_y DOUBLE DEFAULT 0, rotation_vector_z DOUBLE DEFAULT 0, long_way DOUBLE NOT NULL, long_way_time TIME NOT NULL, id_way INTEGER NOT NULL);

        CREATE TABLE data ( _id INTEGER PRIMARY KEY AUTOINCREMENT,
                day DATE NOT NULL,
                time TIME NOT NULL,
                latitude DOUBLE NOT NULL,
                longtitude DOUBLE NOT NULL,
                height DOUBLE DEFAULT 0,
                speed DOUBLE NOT NULL,
                acceleraction_x DOUBLE DEFAULT 0,
                acceleraction_y DOUBLE DEFAULT 0,
                acceleraction_z DOUBLE DEFAULT 0,
                gravity_x DOUBLE DEFAULT 0,
                gravity_y DOUBLE DEFAULT 0,
                gravity_z DOUBLE DEFAULT 0,
                gyroscope_x DOUBLE DEFAULT 0,
                gyroscope_y DOUBLE DEFAULT 0,
                gyroscope_z DOUBLE DEFAULT 0,
                linear_acceleration_x DOUBLE DEFAULT 0,
                linear_acceleration_y DOUBLE DEFAULT 0,
                linear_acceleration_z DOUBLE DEFAULT 0,
                orientation_x DOUBLE DEFAULT 0,
                orientation_y DOUBLE DEFAULT 0,
                orientation_z DOUBLE DEFAULT 0,
                rotation_vector_x DOUBLE DEFAULT 0,
                rotation_vector_y DOUBLE DEFAULT 0,
                rotation_vector_z DOUBLE DEFAULT 0,
                long_way DOUBLE NOT NULL,
                long_way_time TIME NOT NULL,
                id_way INTEGER NOT NULL
        );

         */
        Log.d("TABLE_DATA %s", TABLE_DATA);
        db.execSQL(TABLE_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + MyDB.WAY.TABLE_WAY_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MyDB.DATA.TABLE_DATA_NAME);
        onCreate(db);
    }

    public void allDelete(){
        SQLiteDatabase db = getWritableDatabase();

        //onUpgrade(gDb,0,0);
        db.delete(MyDB.WAY.TABLE_WAY_NAME, null, null);
        db.delete(MyDB.DATA.TABLE_DATA_NAME, null, null);
        Log.d("DB_DELETE", "Delete all data inside db");

        db.close();
    }

    /** DB WAY
     *
     */
    public void addWay( int travelWith, int refreshOption, int refreshOBtainData ){
        //co chcem pridat do db prichystam
        ContentValues values = new ContentValues();

        values.put(MyDB.WAY.NAME, "");
        values.put(MyDB.WAY.TRAVEL_WITH, travelWith);
        values.put(MyDB.WAY.REFRESH_OPTION, refreshOption);
        values.put(MyDB.WAY.REFRESH_OBTAIN_DATA, refreshOBtainData);
        //values.put(MyDB.WAY.COMPLET_LONG_WAY, completLongWay);
        //values.put(MyDB.WAY.COMPLET_LONG_WAY_TIME, "00:00");

        //vlozim do db
        SQLiteDatabase db = getWritableDatabase(); //getWrite... znamena ze mozem z databazou vsetko robit
        gId = db.insert(MyDB.WAY.TABLE_WAY_NAME, null, values); //do id vlozim hodnotu aka mi vznikne v db

        db.close();

        //create name
        updateWayName(gId, getWayDatetime(gId));
    }

    public long getWayId() {
        return gId;
    }

    public String getWayName(long id) {
        SQLiteDatabase db = getWritableDatabase();

        String[] projection = { "*"
        }; //SELECT name, category, star is all column
        String selection = MyDB.WAY.WAY_ID + "=?" ;    //SELECT name, ... FROM MyDB.WAY.TABLE_WAY_NAME WHERE id =
        String[] selectionArgs = {""+id};   //SELECT name, ... FROM MyDB.WAY.TABLE_WAY_NAME WHERE id = 1
        Cursor cur = db.query(MyDB.WAY.TABLE_WAY_NAME,
                projection, selection, selectionArgs, null, null, null);
        if (cur.moveToFirst() && cur.getCount() > 0) {
            // There is record name
            @SuppressLint("Range") String sName = cur.getString(cur.getColumnIndex(MyDB.WAY.NAME));

            cur.close();
            db.close();

            return sName;
        }
        // There is no record

        cur.close();
        db.close();

        return null;
    }

    @SuppressLint("Range")
    public ArrayList<String> getAllWayData() {
        ArrayList<String> zoznam = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + MyDB.WAY.TABLE_WAY_NAME,null);
        if(c.moveToFirst()) {
            do {
                //string arraylist
                zoznam.add( c.getInt(c.getColumnIndex(MyDB.WAY.WAY_ID)) + ", " +
                        c.getString(c.getColumnIndex(MyDB.WAY.NAME)) + ", "
                );
            } while(c.moveToNext());
        }
        c.close();
        db.close();
        return zoznam;
    }

    public Cursor getCursorWayData(){
        SQLiteDatabase db = getWritableDatabase();

        String mysql = "SELECT * FROM " + MyDB.WAY.TABLE_WAY_NAME;
        Cursor c = db.rawQuery(mysql, null);
        c.moveToFirst();

        db.close();

        return c;
    }

    public String[] getWayData(long id){

        SQLiteDatabase db = getWritableDatabase();

        String[] projection = { "*"
        }; //SELECT name, category, star is all column
        String selection = MyDB.WAY.WAY_ID + "=?" ;    //SELECT name, ... FROM MyDB.WAY.TABLE_WAY_NAME WHERE id =
        String[] selectionArgs = {""+id};   //SELECT name, ... FROM MyDB.WAY.TABLE_WAY_NAME WHERE id = 1
        Cursor cur = db.query(MyDB.WAY.TABLE_WAY_NAME,
                projection, selection, selectionArgs, null, null, null);

        if (cur.moveToFirst() && cur.getCount() > 0) {
            // There is record
            @SuppressLint("Range") String[] wayArr = { String.valueOf( id ),
                    cur.getString(cur.getColumnIndex(MyDB.WAY.NAME)),
                    String.valueOf(cur.getInt(cur.getColumnIndex(MyDB.WAY.TRAVEL_WITH))),
                    String.valueOf(cur.getInt(cur.getColumnIndex(MyDB.WAY.REFRESH_OPTION))),
                    String.valueOf(cur.getInt(cur.getColumnIndex(MyDB.WAY.REFRESH_OBTAIN_DATA))),
                    getWayDatetime(id),
                    String.valueOf(cur.getInt(cur.getColumnIndex(MyDB.WAY.COMPLET_LONG_WAY))),
                    String.valueOf(cur.getInt(cur.getColumnIndex(MyDB.WAY.COMPLET_LONG_WAY_TIME))),
            };

            cur.close();
            db.close();

            return wayArr;
        }
        // There is no record

        cur.close();
        db.close();

        return null;
    }

    @SuppressLint("Range")
    public String getWayDatetime(long id){

        SQLiteDatabase db = getWritableDatabase();

        String date = null;
        String select = "SELECT strftime('%Y-%m-%d %H:%M:%S', "+ MyDB.WAY.DATETIME +") AS date FROM " + MyDB.WAY.TABLE_WAY_NAME + " WHERE "+ MyDB.WAY.WAY_ID+"="+id;
        Log.d("SELECT", "Select "+ select);
        Cursor cursor = db.rawQuery(select, null);
        if (cursor.moveToFirst()) {
            date = cursor.getString(cursor.getColumnIndex("date"));
        }

        cursor.close();
        //db.close();

        Log.d("DATETIME", "Datetime "+ date);

        return date;
    }

    public void printWay(long id){
        Log.d("SELECT", "Number of count "+ numberOfCount());
        Log.d("SELECT", "Vypis db("+id+"):");

        String[] tempArr = getWayData(id);
        if( tempArr != null ) {
            for (int i = 0; i < tempArr.length; i++) {
                Log.d("SELECT", i+". "+tempArr[i]);
            }
        } else {
            Log.d("SELECT", "Vypis db(id="+id+") neexistuje");
        }
    }

    public int numberOfCount() {
        SQLiteDatabase db = getWritableDatabase();

        String[] projection = {"COUNT(*)"};
        Cursor cursor = db.query(MyDB.WAY.TABLE_WAY_NAME, projection, null, null, null, null, null);
        cursor.moveToFirst();
        int pocetZaznamov = cursor.getInt(0);

        cursor.close();
        db.close();

        return pocetZaznamov;
    }

    public void updateWayName(long id, String newName){
        ContentValues values = new ContentValues();
        values.put(MyDB.WAY.NAME, newName);

        SQLiteDatabase db = getWritableDatabase();
        db.update(MyDB.WAY.TABLE_WAY_NAME, values, MyDB.WAY.WAY_ID + "= ?", new String[] {"" + id});

        db.close();
    }

    public void updateWayEnd(int TimeWayComplet, int longWayComplet){
        ContentValues values = new ContentValues();
        values.put(MyDB.WAY.COMPLET_LONG_WAY_TIME, TimeWayComplet);
        values.put(MyDB.WAY.COMPLET_LONG_WAY, longWayComplet);

        SQLiteDatabase db = getWritableDatabase();
        db.update(MyDB.WAY.TABLE_WAY_NAME, values, MyDB.WAY.WAY_ID + "= ?", new String[] {"" + gId});

        db.close();
    }

    public void deleteWay(long id){

        //jeden z tychto dvoch vybrat
        boolean deleting = false;

        int rowsDeleted;

        //first delete data
        deleteAllDataByWay(id);

        //check if is all ok
        //getRecordingInData(); //for debug //todo mv vypnut na konci

        SQLiteDatabase db = getWritableDatabase();

        if( deleting ) {
            //delete way
            rowsDeleted = db.delete(MyDB.WAY.TABLE_WAY_NAME,
                    //MyDB.WAY.TABLE_WAY_NAME + "=" + id_way + " AND " +
                    MyDB.WAY.WAY_ID + "=" + id,
                    null);

        } else {
            //delete way
            rowsDeleted = db.delete(MyDB.WAY.TABLE_WAY_NAME,
                    //MyDB.WAY.TABLE_WAY_NAME + "=" + id_way + " AND " +
                    MyDB.WAY.WAY_ID + "= ?",
                    new String[] {""+id});
        }

        Log.d("DB_DELETE", "Delete "+rowsDeleted+" data inside "+ MyDB.WAY.TABLE_WAY_NAME);

        db.close();
    }

    /** DB DATA
     *
     */
    public long addCheckPointData(Date date, Time time, double accuracy, double accuracyGps, double latitude, double longitude, double height, double speed, double longWay, double accelerationX, double accelerationY, double accelerationZ, double gravityX, double gravityY, double gravityZ, double gyroscopeX, double gyroscopeY, double gyroscopeZ, double linearAccelerationX, double linearAccelerationY, double linearAccelerationZ, double orientationX, double orientationY, double orientationZ, double rotationVectorX, double rotationVectorY, double rotationVectorZ, int longWayTime) {
        SQLiteDatabase db = getWritableDatabase();
        long newRowId;
        try {
            ContentValues values = new ContentValues();
            values.put(MyDB.DATA.DATE, date.toString());
            values.put(MyDB.DATA.TIME, time.toString());
            values.put(MyDB.DATA.ACCURACY, accuracy);
            values.put(MyDB.DATA.ACCURACY_GPS, accuracyGps);
            values.put(MyDB.DATA.LATITUDE, latitude);
            values.put(MyDB.DATA.LONGTITUDE, longitude);
            values.put(MyDB.DATA.HEIGHT, height);
            values.put(MyDB.DATA.SPEED, speed);
            values.put(MyDB.DATA.LONG_WAY, longWay);
            values.put(MyDB.DATA.ACCELERACTION_X, accelerationX);
            values.put(MyDB.DATA.ACCELERACTION_Y, accelerationY);
            values.put(MyDB.DATA.ACCELERACTION_Z, accelerationZ);
            values.put(MyDB.DATA.GRAVITY_X, gravityX);
            values.put(MyDB.DATA.GRAVITY_Y, gravityY);
            values.put(MyDB.DATA.GRAVITY_Z, gravityZ);
            values.put(MyDB.DATA.GYROSCOPE_X, gyroscopeX);
            values.put(MyDB.DATA.GYROSCOPE_Y, gyroscopeY);
            values.put(MyDB.DATA.GYROSCOPE_Z, gyroscopeZ);
            values.put(MyDB.DATA.LINEAR_ACCELERATION_X, linearAccelerationX);
            values.put(MyDB.DATA.LINEAR_ACCELERATION_Y, linearAccelerationY);
            values.put(MyDB.DATA.LINEAR_ACCELERATION_Z, linearAccelerationZ);
            values.put(MyDB.DATA.ORIENTATION_X, orientationX);
            values.put(MyDB.DATA.ORIENTATION_Y, orientationY);
            values.put(MyDB.DATA.ORIENTATION_Z, orientationZ);
            values.put(MyDB.DATA.ROTATION_VECTOR_X, rotationVectorX);
            values.put(MyDB.DATA.ROTATION_VECTOR_Y, rotationVectorY);
            values.put(MyDB.DATA.ROTATION_VECTOR_Z, rotationVectorZ);
            values.put(MyDB.DATA.LONG_WAY_TIME, longWayTime);
            values.put(MyDB.DATA.ID_WAY, gId);

            newRowId = db.insert(MyDB.DATA.TABLE_DATA_NAME, null, values);
        } catch (Exception ignored){
            newRowId = -1;
        }
        db.close();

        //Log.d("ADD_CHECKPOINT_DATA", "Add "+newRowId+" data inside "+ MyDB.DATA.TABLE_DATA_NAME);

        return newRowId;
    }

    public int getRecordingInData() {
        SQLiteDatabase db = getWritableDatabase();

        String[] projection = {"COUNT(*)"};
        Cursor cursor = db.query(MyDB.DATA.TABLE_DATA_NAME, projection, null, null, null, null, null);
        cursor.moveToFirst();
        int pocetZaznamov = cursor.getInt(0);

        Log.d("SqlLite", "ALLSqlData have recording "+pocetZaznamov+" data inside "+ MyDB.DATA.TABLE_DATA_NAME);

        String[] projection1 = { "*" }; //SELECT name, category, star is all column
        String selection = MyDB.DATA.ID_WAY + "=?" ;    //SELECT name, ... FROM MyDB.WAY.TABLE_WAY_NAME WHERE id =
        String[] selectionArgs = {""+gId};   //SELECT name, ... FROM MyDB.WAY.TABLE_WAY_NAME WHERE id = 1
        Cursor cur = db.query(MyDB.DATA.TABLE_DATA_NAME,
                projection1, selection, selectionArgs, null, null, null);
        cur.moveToFirst();
        Log.d("SqlLite", "SqlDataId have recording "+cur.getCount()+" data inside "+ MyDB.DATA.TABLE_DATA_NAME);

        cursor.close();
        cur.close();
        db.close();

        return pocetZaznamov;
    }

    public Cursor getCursorData(long id){
        SQLiteDatabase db = getWritableDatabase();

        String[] projection = {"*"};
        String selection = MyDB.DATA.ID_WAY + "=?";
        String[] selectionArgs = {""+id};

        Cursor c = db.query(MyDB.DATA.TABLE_DATA_NAME, projection, selection, selectionArgs, null, null, null);
        c.moveToFirst();

        //db.close();

        return c;
    }

    @SuppressLint("Range")
    public ArrayList<String> getCheckPointData(long id) {
        ArrayList<String> zoznam = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();

        //vyberie potrebné stĺpce z tabuľky
        String[] projection = {"*"};
        String selection = MyDB.DATA.ID_WAY + "=?";
        String[] selectionArgs = {""+id};

        Cursor c = db.query(MyDB.DATA.TABLE_DATA_NAME, projection, selection, selectionArgs, null, null, null);

        final int max = DOUBLE_COUNTER_DATA + OFFSET_DATA;

        if (c.moveToFirst()) {
            do {
                StringBuilder temp;

                temp = new StringBuilder(c.getLong(0) + ", " +
                        c.getString(1) + ", " +
                        c.getString(2) + ", ");
                for (int i = OFFSET_DATA; i < max; i++){
                    temp.append(c.getDouble(i)).append(", ");
                    //Log.d("vypis sql", "i je "+i);
                }
                //Log.d("vypis sql", "i je "+max+offset);
                temp.append(c.getString(max)).append(", ").append(c.getLong((max + 1))).append(", ");
                //pridá hodnoty získané z databázy do ArrayListu
                zoznam.add(temp.toString());
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return zoznam;
    }

    public Cursor getAllCursorData(){
        SQLiteDatabase db = getWritableDatabase();

        String mysql = "SELECT * FROM " + MyDB.DATA.TABLE_DATA_NAME;

        Cursor c = db.rawQuery(mysql, null);
        c.moveToFirst();

        db.close();

        return c;
    }

    @SuppressLint("Range")
    public ArrayList<String> getAllCheckPointData() {
        ArrayList<String> zoznam = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();

        //vyberie potrebné stĺpce z tabuľky
        String sqlQuery = "SELECT * FROM " + MyDB.DATA.TABLE_DATA_NAME;

        Cursor c = db.rawQuery(sqlQuery, null);

        final int max = DOUBLE_COUNTER_DATA + OFFSET_DATA;

        if (c.moveToFirst()) {
            do {
                StringBuilder temp;

                temp = new StringBuilder(c.getLong(0) + ", " +
                        c.getString(1) + ", " +
                        c.getString(2) + ", ");
                for (int i = OFFSET_DATA; i < max; i++){
                    temp.append(c.getDouble(i)).append(", ");
                    //Log.d("vypis sql", "i je "+i);
                }
                //Log.d("vypis sql", "i je "+max+offset);
                temp.append(c.getString(max)).append(", ").append(c.getLong((max + 1))).append(", ");
                //pridá hodnoty získané z databázy do ArrayListu
                zoznam.add(temp.toString());
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return zoznam;
    }

    public void deleteAllDataByWay(long id){
        SQLiteDatabase db = getWritableDatabase();

        int rowsDeleted = db.delete(MyDB.DATA.TABLE_DATA_NAME, MyDB.DATA.ID_WAY+"=?", new String[] { ""+id });

        Log.d("DB_DELETE", "Delete "+rowsDeleted+" data inside "+ MyDB.DATA.TABLE_DATA_NAME);

        db.close();
        //return rowsDeleted;
    }

    public void deleteData(long id) {
        SQLiteDatabase db = getWritableDatabase();

        int rowsDeleted = db.delete(MyDB.DATA.TABLE_DATA_NAME, MyDB.DATA.DATA_ID+"=?", new String[] { ""+id });

        Log.d("DB_DELETE", "Delete "+rowsDeleted+" data inside "+ MyDB.DATA.TABLE_DATA_NAME);

        db.close();
        //return rowsDeleted;
    }

    public int numberODatafCount(long id) {
        SQLiteDatabase db = getWritableDatabase();

        String[] projection = {"COUNT(*)"};
        String selection = MyDB.DATA.ID_WAY + "=?";
        String[] selectionArgs = {""+id};

        Cursor cursor = db.query(MyDB.DATA.TABLE_DATA_NAME, projection, selection, selectionArgs, null, null, null);
        cursor.moveToFirst();
        int pocetZaznamov = cursor.getInt(0);

        cursor.close();
        db.close();

        return pocetZaznamov;
    }
    /*
    public ArrayList<String> getAllCheckPointData() {
        ArrayList<String> zoznam = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + MyDB.DATA.TABLE_DATA_NAME,null);
        if(c.moveToFirst()) {
            do {
                //string arraylist
                zoznam.add( c.getInt(c.getColumnIndex(MyDB.DATA.DATA_ID)) + ", " +
                        String.valueOf(c.getInt(c.getColumnIndex(MyDB.WAY.COMPLET_LONG_WAY)))  + ", " +
                        String.valueOf(c.getInt(c.getColumnIndex(MyDB.WAY.COMPLET_LONG_WAY_TIME)))  + ", " +
                        getWayDatetime() + ", " +
                        String.valueOf(c.getInt(c.getColumnIndex(MyDB.WAY.REFRESH_OBTAIN_DATA))) + ", " +
                        String.valueOf(c.getInt(c.getColumnIndex(MyDB.WAY.REFRESH_OPTION)))  + ", " +
                        String.valueOf(c.getInt(c.getColumnIndex(MyDB.WAY.TRAVEL_WITH))) + ", "
                );
            } while(c.moveToNext());
        }
        c.close();
        db.close();
        return zoznam;
    }

     */
}



