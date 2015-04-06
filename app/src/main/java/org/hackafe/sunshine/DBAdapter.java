package org.hackafe.sunshine;

/**
 * Created by Nikola on 5.4.2015 г..
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// TO USE:
// Change the package (at top) to match your project.
// Search for "TODO", and make the appropriate changes.
public class DBAdapter {

    // ///////////////////////////////////////////////////////////////////
    // Constants & Data
    // ///////////////////////////////////////////////////////////////////
    // For logging:
    private static final String TAG = "DBAdapter";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;
    /*
     * CHANGE 1:
     */
    // TODO: Setup your fields here:
    public static final String KEY_DATE = "DATE";
    public static final String KEY_FORECAST = "FORECAST";

    // TODO: Setup your field numbers here (0 = KEY_ROWID, 1=...)
    public static final int COL_DATE = 1;
    public static final int COL_FORECAST = 2;

    public static final String[] ALL_KEYS = new String[]{KEY_ROWID,
            KEY_DATE, KEY_FORECAST};

    public static final String[] FORECAST_KEYS = new String[]{KEY_DATE, KEY_FORECAST};

    public static final String DATABASE_NAME = "forecastDb";
    public static final String DATABASE_TABLE = "forecastData";
    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 1;


    private static final String DATABASE_CREATE_SQL = "create table "
            + DATABASE_TABLE + " (" + KEY_ROWID
            + " integer primary key autoincrement, "
            + KEY_DATE + " long not Null, " + KEY_FORECAST
            + " Text " + ");";

    // Context of application who uses us.
    private final Context context;
    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;


    // ///////////////////////////////////////////////////////////////////
    // Public methods:
    // ///////////////////////////////////////////////////////////////////

    public DBAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public DBAdapter open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    // Add a new set of values to the database.
    public long insertRowForecast(Long date, String forecast) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_FORECAST, forecast);
        // Insert it into the database.
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    // Return all data in the database.
    public Cursor getAllRows(String[] columns, String Where) {
        String order = (KEY_DATE + " ASC, "
                + KEY_FORECAST + " ASC");
        Cursor c = db.query(true, DATABASE_TABLE, columns, Where, null, null,
                null, order, null);

        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Return data from the database.
    public Forecast getDayForecast(String Where) {
        Forecast res = new Forecast();
        Cursor c = db.query(true, DATABASE_TABLE, FORECAST_KEYS, Where, null, null,
                null, null, null);

        if (c != null) {
            c.moveToFirst();
            res.timestamp = c.getLong(0);
            res.desc = c.getString(1);
        }
        return res;
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows(DBAdapter.ALL_KEYS, "");
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }


    // Reset autoincremet key _id
    public boolean resetID() {
        Boolean result;
        ContentValues values = new ContentValues();
        values.put("seq", 0);
        result = db.update("sqlite_sequence", values, null, null) != 0;
        return result;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version "
                    + oldVersion + " to " + newVersion
                    + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);


            // Recreate new database:
            onCreate(_db);
        }
    }
}