package com.example.wattwise;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WattWiseDB.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE bills (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "month TEXT, " +
                "year INTEGER, " +
                "unit INTEGER, " +
                "rebate INTEGER, " +
                "totalCharges REAL, " +
                "finalCost REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS bills");
        onCreate(db);
    }

    public boolean insertBill(String month, int year, int unit, int rebate, double totalCharges, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("month", month);
        values.put("year", year);
        values.put("unit", unit);
        values.put("rebate", rebate);
        values.put("totalCharges", totalCharges);
        values.put("finalCost", finalCost);

        long result = db.insert("bills", null, values);
        return result != -1;
    }

    public Cursor getAllBills() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT id, month, year, unit, rebate, finalCost FROM bills ORDER BY id DESC",
                null
        );
    }

    public Cursor getBillById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM bills WHERE id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public Cursor getLatestBill() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT month, year, finalCost FROM bills ORDER BY id DESC LIMIT 1",
                null
        );
    }

    public Cursor getHistorySummary() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT COUNT(*), AVG(finalCost) FROM bills",
                null
        );
    }

    public boolean updateBill(int id, String month, int year, int unit, int rebate, double totalCharges, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("month", month);
        values.put("year", year);
        values.put("unit", unit);
        values.put("rebate", rebate);
        values.put("totalCharges", totalCharges);
        values.put("finalCost", finalCost);

        int result = db.update(
                "bills",
                values,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }

    public boolean deleteBill(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                "bills",
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }
}