package com.favepc.reader.rfidreaderutility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class LocalDbHelper extends SQLiteOpenHelper {

    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_RFID_NUMBER = "RF" + COLUMN_ID + "_NUMBER";
    public static final String TEMP_TABLE = "TEMP_TABLE";
    public static final String COLUMN_TEMPERATURE = "TEMPERATURE";
    public static final String COLUMN_LOCATION = "LOCATION";
    public static final String COLUMN_DATETIME = "DATETIME";

    public LocalDbHelper(@Nullable Context context) {
        super(context, "LocalTemp.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TEMP_TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RFID_NUMBER + " INTEGER, " +
                COLUMN_TEMPERATURE + " REAL, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_DATETIME + " NUMERIC )";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addOne (LocalTempModel localTempModel){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_RFID_NUMBER, localTempModel.getRfidNumber());
        cv.put(COLUMN_TEMPERATURE, localTempModel.getTemperature());
        cv.put(COLUMN_LOCATION, localTempModel.getLocation());
        cv.put(COLUMN_DATETIME, localTempModel.getDatetime());

        long insert = db.insert(TEMP_TABLE, null, cv);

        if (insert == -1){
            return false;
        } else {
            return true;
        }
    }

    public LocalTempModel getOldestOne(){

        LocalTempModel returnLocalTempModel = new LocalTempModel();

        String queryString = "SELECT * FROM " + TEMP_TABLE +
                " WHERE " + COLUMN_ID +
                " = (SELECT MIN(" + COLUMN_ID + ") FROM " + TEMP_TABLE + ")";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        //if results get the items to be sent to the cloud db
        if(cursor.moveToFirst()){
            int tagID = cursor.getInt(0);
            int tagRfidNumber = cursor.getInt(1);
            double tagTemperature = cursor.getDouble(2);
            String tagLocation = cursor.getString(3);
            String tagDateTime = cursor.getString(4);

            returnLocalTempModel = new LocalTempModel(tagID,tagRfidNumber,tagTemperature,tagLocation,tagDateTime);
        }else{
            //failure do not do anything
        }

        cursor.close();
        db.close();

        return returnLocalTempModel;
    }

    public boolean deleteOldestOne (){
        //delete the oldest (lowest ID number entry)

        //String queryString = "DELETE FROM " + TEMP_TABLE + " WHERE " + COLUMN_ID + " = (SELECT MIN(" + COLUMN_ID + ") FROM " + TEMP_TABLE + ")";
        String queryString = "DELETE FROM TEMP_TABLE WHERE ID = (SELECT MIN(ID) FROM TEMP_TABLE)";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()){
            return true;
        }
        else {
            return false;
        }
    }
}