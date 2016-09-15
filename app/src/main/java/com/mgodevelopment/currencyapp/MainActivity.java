package com.mgodevelopment.currencyapp;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mgodevelopment.currencyapp.database.CurrencyDatabaseAdapter;
import com.mgodevelopment.currencyapp.database.CurrencyTableHelper;
import com.mgodevelopment.currencyapp.receivers.CurrencyReceiver;
import com.mgodevelopment.currencyapp.services.CurrencyService;
import com.mgodevelopment.currencyapp.utils.LogUtils;
import com.mgodevelopment.currencyapp.value_objects.Currency;

public class MainActivity extends AppCompatActivity
        implements CurrencyReceiver.Receiver {

    private String mBaseCurrency = Constants.CURRENCY_CODES[0];
    private String mTargetCurrency = Constants.CURRENCY_CODES[1];
    private CurrencyTableHelper mCurrencyTableHelper;

    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDB();
        retrieveCurrencyExchangeRate();

    }

    @Override
    public void onReceiveResult(int resultCode, final Bundle resultData) {

        switch (resultCode) {
            case Constants.STATUS_RUNNING:
                LogUtils.log(TAG, "Currency Service running!");
                break;
            case Constants.STATUS_FINISHED:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Currency currencyParcel = resultData.getParcelable(Constants.RESULT);
                        if (currencyParcel != null) {

                            String message = "Currency: " + currencyParcel.getBase() + " - " +
                                    currencyParcel.getName() + ": " + currencyParcel.getRate();
                            LogUtils.log(TAG, message);

                            Long id = mCurrencyTableHelper.insertCurrency(currencyParcel);
                            Currency currency = currencyParcel;

                            try {
                                currency = mCurrencyTableHelper.getCurrency(id);
                            } catch (SQLException e) {
                                LogUtils.log(TAG, "Currency retrieval has failed");
                            }

                            if (currency != null) {

                                String dbMessage = "Currency (DB): " + currency.getBase() + " - " +
                                        currency.getName() + ": " + currency.getRate();
                                LogUtils.log(TAG, dbMessage);

                            }

                        }
                    }
                });
                break;
            case Constants.STATUS_ERROR:
                String error = resultData.getString(Intent.EXTRA_TEXT);
                LogUtils.log(TAG, error);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }

    }

    private void initDB() {

        CurrencyDatabaseAdapter currencyDatabaseAdapter = new CurrencyDatabaseAdapter(this);
        mCurrencyTableHelper = new CurrencyTableHelper(currencyDatabaseAdapter);

    }

    private void retrieveCurrencyExchangeRate() {

        CurrencyReceiver receiver = new CurrencyReceiver(new Handler());
        receiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, getApplicationContext(), CurrencyService.class);
        intent.setExtrasClassLoader(CurrencyService.class.getClassLoader());

        Bundle bundle = new Bundle();
        String url = Constants.CURRENCY_URL + mBaseCurrency;
        bundle.putString(Constants.URL, url);
        bundle.putParcelable(Constants.RECEIVER, receiver);
        bundle.putInt(Constants.REQUEST_ID, Constants.REQUEST_ID_NUM);
        bundle.putString(Constants.CURRENCY_NAME, mTargetCurrency);
        bundle.putString(Constants.CURRENCY_BASE, mBaseCurrency);
        intent.putExtra(Constants.BUNDLE, bundle);
        startService(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
