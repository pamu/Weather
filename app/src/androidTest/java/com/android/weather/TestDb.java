package com.android.weather;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.android.weather.data.WeatherContract;
import com.android.weather.data.WeatherDbHelper;
import com.android.weather.data.WeatherContract.WeatherEntry;

/**
 * Created by android on 23/3/15.
 */
public class TestDb extends AndroidTestCase {
    private final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() throws Throwable {
        String testName = "Palakurthy";
        String testLocationSetting = "Palakurthy,In";
        double latitude = 1122.11;
        double longitude =22.22;

        WeatherDbHelper weatherDbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = weatherDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, testName);
        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, latitude);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, longitude);
        long rowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
        assertTrue(rowId != 0);
        Log.d(LOG_TAG, "row id is " + rowId);

        String[] columns = {
                WeatherContract.LocationEntry._ID,
                WeatherContract.LocationEntry.COLUMN_CITY_NAME,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_COORD_LONG
        };

        Cursor cursor = db.query(
                WeatherContract.LocationEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
                );

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_CITY_NAME));
            String location = cursor.getString(cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING));
            double lat = cursor.getDouble(cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LAT));
            double longi = cursor.getDouble(cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LONG));

            assertEquals(testName, name);
            assertEquals(testLocationSetting, location);
            assertEquals(latitude, lat);
            assertEquals(longitude, longi);

            Log.d(LOG_TAG, "tests passed");

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, rowId);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "221212");
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "stars");
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

            long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);

            assertTrue(weatherRowId != -1);

            Cursor wCursor = db.query(
                    WeatherContract.LocationEntry.TABLE_NAME,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (wCursor.moveToFirst()) {

            } else {
                fail("no values returned :(");
            }

        } else {
            fail("no values returned :(");
        }
        db.close();
    }
}
