package alexbrod.carblackbox.db;

import android.provider.BaseColumns;

/**
 * Created by Alex Brod on 4/2/2017.
 */

public final class DbContract {

    private DbContract(){}

    public static class Travels implements BaseColumns {
        public static final String TABLE_NAME = "travels";
        public static final String COL_START_TIME = "start_time";
        public static final String COL_END_TIME = "end_time";

        protected static final String SQL_CREATE =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        COL_START_TIME + " INTEGER UNIQUE, " +
                        COL_END_TIME + " INTEGER);";

        protected static final String SQL_DELETE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }


    public static class TravelEvents implements BaseColumns {
        public static final String TABLE_NAME = "travel_events";
        public static final String COL_TRAVEL_ID = "travel_id";
        public static final String COL_TYPE = "type";
        public static final String COL_DATE_OCCURRED = "date_occurred";
        public static final String COL_VALUE = "value";
        public static final String COL_LOCATION_LAT = "location_lat";
        public static final String COL_LOCATION_LONG = "location_long";

        protected static final String SQL_CREATE =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        COL_TRAVEL_ID + " INTEGER," +
                        COL_TYPE + " TEXT," +
                        COL_DATE_OCCURRED + " INTEGER," +
                        COL_VALUE + " NUMERIC," +
                        COL_LOCATION_LAT + " REAL," +
                        COL_LOCATION_LONG + " REAL," +
                        "FOREIGN KEY(" + COL_TRAVEL_ID + ") REFERENCES " +
                            Travels.TABLE_NAME + "(" + Travels.COL_START_TIME + "));";

        protected static final String SQL_DELETE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class SensorEvents implements BaseColumns{
        public static final String TABLE_NAME = "sensor_events";
        public static final String COL_TIMESTAMP = "timestamp";
        public static final String COL_TYPE = "type";
        public static final String COL_AXIS = "axis";
        public static final String COL_VALUE = "value";

        protected static final String SQL_CREATE =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        COL_TIMESTAMP + " INTEGER," +
                        COL_TYPE + " TEXT," +
                        COL_AXIS + " TEXT," +
                        COL_VALUE + " NUMERIC);";

        protected static final String SQL_DELETE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }


}
