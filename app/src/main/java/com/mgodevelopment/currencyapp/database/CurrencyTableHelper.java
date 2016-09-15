package com.mgodevelopment.currencyapp.database;

import android.database.Cursor;

import com.mgodevelopment.currencyapp.Constants;
import com.mgodevelopment.currencyapp.value_objects.Currency;

import java.util.ArrayList;

/**
 * Created by Martin on 9/14/2016.
 */
public class CurrencyTableHelper {

    private CurrencyDatabaseAdapter mAdapter;

    public CurrencyTableHelper(CurrencyDatabaseAdapter adapter) {
        mAdapter = adapter;
    }

    public long insertCurrency(Currency currency) {

        ArrayList<Currency> currencies = getCurrencyHistory(currency.getBase(), currency.getName(), currency.getDate());


    }

    public ArrayList<Currency> getCurrencyHistory(String base, String name, String date) {

        ArrayList<Currency> currencies = new ArrayList<>();
        Cursor cursor = mAdapter.getWritableDatabase().query(
                Constants.CURRENCY_TABLE,
                new String[]{Constants.KEY_ID, Constants.KEY_BASE, Constants.KEY_DATE, Constants.KEY_RATE, Constants.KEY_NAME},
                Constants.KEY_BASE + " = '" + base + "' AND " +
                        Constants.KEY_NAME + " = '" + name + "' AND " +
                        Constants.KEY_DATE + " = '" + date + "'",
                null, null, null, null);

        if (cursor != null) {

            if (cursor.moveToFirst()) {
                currencies.add(parseCurrency(cursor));
            }

            while (cursor.moveToNext()) {
                currencies.add(parseCurrency(cursor));
            }

        }

    }

}
