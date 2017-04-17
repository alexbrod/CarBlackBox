package alexbrod.carblackbox.db;

import alexbrod.carblackbox.bl.CarSensorEvent;
import alexbrod.carblackbox.bl.Travel;
import alexbrod.carblackbox.bl.TravelEvent;
import alexbrod.carblackbox.db.DbContract.*;
import alexbrod.carblackbox.sensors.ISensorsEvents;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

import java.util.ArrayList;


/**
 * Created by Alex Brod on 4/1/2017.
 */

public class DbManager extends SQLiteOpenHelper  {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CarBlackBox.db";

    private static DbManager dbManager;
    private static SQLiteDatabase readableDb;
    private static SQLiteDatabase writableDb;

    private static String TRVL_EVNTS_BY_TRVL_QRY =
            "SELECT " + TravelEvents.COL_DATE_OCCURRED + "," +
                    TravelEvents.COL_TYPE + "," +
                    TravelEvents.COL_VALUE + "," +
                    TravelEvents.COL_LOCATION_LAT + "," +
                    TravelEvents.COL_LOCATION_LONG  +
                    " FROM " + TravelEvents.TABLE_NAME + " te" +
                    " INNER JOIN " + Travels.TABLE_NAME + " t" +
                    " ON te." + TravelEvents.COL_TRAVEL_ID + "= t." + Travels.COL_START_TIME +
                    " WHERE t." + Travels.COL_START_TIME + " =?";

    private static String LAST_NUM_TRAVELS_QRY =
            "SELECT " + Travels.COL_START_TIME + "," +
                    Travels.COL_END_TIME +
                    " FROM " + Travels.TABLE_NAME +
                    " ORDER BY " + Travels.COL_START_TIME + " DESC LIMIT ?";


    private DbManager(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        readableDb = getReadableDatabase();
        writableDb = getWritableDatabase();
    }

    public static DbManager getInstance(Context context){
        if(dbManager == null){
            dbManager = new DbManager(context);
        }
        return dbManager;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Travels.SQL_CREATE);
        db.execSQL(TravelEvents.SQL_CREATE);
        db.execSQL(SensorEvents.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(Travels.SQL_DELETE);
        db.execSQL(TravelEvents.SQL_DELETE);
        db.execSQL(SensorEvents.SQL_DELETE);
        onCreate(db);
    }

    public long addTravelEvent(long travelStartTime, String type, long dateOccurred, double value,
                                double locLat, double locLong){
        ContentValues values = new ContentValues();
        values.put(TravelEvents.COL_TRAVEL_ID, travelStartTime);
        values.put(TravelEvents.COL_TYPE, type);
        values.put(TravelEvents.COL_DATE_OCCURRED, dateOccurred);
        values.put(TravelEvents.COL_VALUE, value);
        values.put(TravelEvents.COL_LOCATION_LAT, locLat);
        values.put(TravelEvents.COL_LOCATION_LONG, locLong);
        return writableDb.insert(TravelEvents.TABLE_NAME, null, values);
    }

    public long addTravel(long startTime, long endTime){
        ContentValues values = new ContentValues();
        values.put(Travels.COL_START_TIME, startTime);
        values.put(Travels.COL_END_TIME, endTime);
        return writableDb.insert(Travels.TABLE_NAME, null, values);
    }

    public int updateTravelEndTime(long travelId, long endTime){
        ContentValues values = new ContentValues();
        values.put(Travels.COL_END_TIME, endTime);
        String selection = Travels.COL_START_TIME + "=?";
        String[] selectionArgs = { String.valueOf(travelId)};
        return writableDb.update(Travels.TABLE_NAME, values, selection, selectionArgs);
    }

    public ArrayList<TravelEvent> getEventsByTravel(long travelId){
        ArrayList<TravelEvent> travelEvents = new ArrayList<>();
        Cursor c = readableDb.rawQuery(TRVL_EVNTS_BY_TRVL_QRY,
                new String[]{String.valueOf(travelId)});

        while(c.moveToNext()) {
            travelEvents.add(new TravelEvent(
                c.getLong(c.getColumnIndex(TravelEvents.COL_DATE_OCCURRED)),
                c.getString(c.getColumnIndex(TravelEvents.COL_TYPE)),
                c.getDouble(c.getColumnIndex(TravelEvents.COL_VALUE)),
                c.getDouble(c.getColumnIndex(TravelEvents.COL_LOCATION_LAT)),
                    c.getDouble(c.getColumnIndex(TravelEvents.COL_LOCATION_LONG))
            ));

        }
        c.close();
        return travelEvents;
    }

    public ArrayList<Travel> getLastNumTravels(int num){
        ArrayList<Travel> travels = new ArrayList<>();
        String[] selectionArgs = {String.valueOf(num)};

        Cursor c = readableDb.rawQuery(LAST_NUM_TRAVELS_QRY, selectionArgs);

        while(c.moveToNext()) {
            travels.add(new Travel(
                    c.getLong(c.getColumnIndex(Travels.COL_START_TIME)),
                    c.getLong(c.getColumnIndex(Travels.COL_END_TIME))
            ));

        }
        c.close();
        return travels;
    }

    public long addSensorEvent(CarSensorEvent event){
        ContentValues values = new ContentValues();
        values.put(SensorEvents.COL_TIMESTAMP, event.getTimestamp());
        values.put(SensorEvents.COL_TYPE, event.getType());
        values.put(SensorEvents.COL_AXIS, event.getAxis());
        values.put(SensorEvents.COL_VALUE, event.getValue());
        return writableDb.insert(SensorEvents.TABLE_NAME, null, values);
    }

    public void close(){
        super.close();
        dbManager = null;
    }
}
