package com.mgodevelopment.currencyapp.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mgodevelopment.currencyapp.Constants;
import com.mgodevelopment.currencyapp.utils.LogUtils;

/**
 * Created by Martin on 9/14/2016.
 */
public class CurrencyDatabaseAdapter extends SQLiteOpenHelper {

    public static final String TAG = CurrencyDatabaseAdapter.class.getName();

    public static final int DATABASE_VERSION = 1;
    public static final String CURRENCY_TABLE_CREATE = "create table " +
            Constants.CURRENCY_TABLE + " (" +
            Constants.KEY_ID + " integer primary key autoincrement, " +
            Constants.KEY_BASE + " text not null, " +
            Constants.KEY_NAME + " text not null, " +
            Constants.KEY_RATE + " real, " +
            Constants.KEY_DATE + " date);";

    public CurrencyDatabaseAdapter(Context context) {
        super(context, Constants.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {

            db.execSQL(CURRENCY_TABLE_CREATE);
            LogUtils.log(TAG, "Currency table create");

        } catch (SQLException e) {

            e.printStackTrace();
            LogUtils.log(TAG, "Currency table creation error");

        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        clearCurrencyTable(db);
        onCreate(db);
    }

    private void clearCurrencyTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.CURRENCY_TABLE);
    }

}