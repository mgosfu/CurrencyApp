package com.mgodevelopment.currencyapp;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.mgodevelopment.currencyapp.adapters.CurrencyAdapter;
import com.mgodevelopment.currencyapp.database.CurrencyDatabaseAdapter;
import com.mgodevelopment.currencyapp.database.CurrencyTableHelper;
import com.mgodevelopment.currencyapp.receivers.CurrencyReceiver;
import com.mgodevelopment.currencyapp.services.CurrencyService;
import com.mgodevelopment.currencyapp.utils.AlarmUtils;
import com.mgodevelopment.currencyapp.utils.LogUtils;
import com.mgodevelopment.currencyapp.utils.NotificationUtils;
import com.mgodevelopment.currencyapp.utils.SharedPreferencesUtils;
import com.mgodevelopment.currencyapp.value_objects.Currency;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements CurrencyReceiver.Receiver {

    private String mBaseCurrency = Constants.CURRENCY_CODES[3];
    private String mTargetCurrency = Constants.CURRENCY_CODES[30];
    private CurrencyTableHelper mCurrencyTableHelper;

    private static final String TAG = MainActivity.class.getName();

    private int mServiceRepetition = AlarmUtils.REPEAT.REPEAT_EVERY_MINUTE.ordinal();

    private CoordinatorLayout mLogLayout;
    private FloatingActionButton mFloatingActionButton;

    private boolean mIsLogVisible;
    private boolean isFabVisibile = true;
    private ListView mBaseCurrencyList;
    private ListView mTargetCurrencyList;
    private LineChart mLineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resetDownloads();

        initCurrencies();
        initDB();
        initToolbar();
        initSpinner();
        initCurrencyList();
        initLineChart();

        showLogs();

        mLogLayout = (CoordinatorLayout) findViewById(R.id.log_layout);

    }

    @Override
    protected void onResume() {

        super.onResume();
        mServiceRepetition = SharedPreferencesUtils.getServiceRepetition(this);
        retrieveCurrencyExchangeRate();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        LogUtils.setLogListener(null);

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

                                NotificationUtils.showNotificationMessage(getApplicationContext(),
                                        "Currency Exchange Rate", dbMessage);

                            }

                            if (NotificationUtils.isAppInBackground(MainActivity.this)) {

                                int numDownloads = SharedPreferencesUtils.getNumDownloads(getApplicationContext());
                                SharedPreferencesUtils.updateNumDownloads(getApplicationContext(), ++numDownloads);

                                if (numDownloads == Constants.MAX_DOWNLOADS) {

                                    LogUtils.log(TAG, "Max downloads for the background processing has been reached");
                                    mServiceRepetition = AlarmUtils.REPEAT.REPEAT_EVERY_DAY.ordinal();
                                    retrieveCurrencyExchangeRate();

                                }

                            } else {
                                updateLineChart();
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

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    private void initSpinner() {

        final Spinner spinner = (Spinner) findViewById(R.id.time_frequency);
        spinner.setSaveEnabled(true);
        spinner.setSelection(SharedPreferencesUtils.getServiceRepetition(this), false);
        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        SharedPreferencesUtils.updateServiceRepetition(MainActivity.this, position);
                        mServiceRepetition = position;

                        if (position >= AlarmUtils.REPEAT.values().length) {
                            AlarmUtils.stopService();
                        } else {
                            retrieveCurrencyExchangeRate();
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });

    }

    private void initCurrencyList() {

        mBaseCurrencyList = (ListView) findViewById(R.id.base_currency_list);
        mTargetCurrencyList = (ListView) findViewById(R.id.target_currency_list);

        CurrencyAdapter baseCurrencyAdapter = new CurrencyAdapter(this);
        CurrencyAdapter targetCurrencyAdapter = new CurrencyAdapter(this);

        mBaseCurrencyList.setAdapter(baseCurrencyAdapter);
        mTargetCurrencyList.setAdapter(targetCurrencyAdapter);

        int baseCurrencyIndex = retrieveIndexOf(mBaseCurrency);
        int targetCurrencyIndex = retrieveIndexOf(mTargetCurrency);

        mBaseCurrencyList.setItemChecked(baseCurrencyIndex, true);
        mTargetCurrencyList.setItemChecked(targetCurrencyIndex, true);

        mBaseCurrencyList.setSelection(baseCurrencyIndex);
        mTargetCurrencyList.setSelection(targetCurrencyIndex);

        addCurrencySelectionListener();

    }

    private void initCurrencies() {

        mBaseCurrency = SharedPreferencesUtils.getCurrency(this, true);
        mTargetCurrency = SharedPreferencesUtils.getCurrency(this, false);

    }

    private void initLineChart() {



    }

    private void showLogs() {

        final TextView logText = (TextView) findViewById(R.id.log_text);
        LogUtils.setLogListener(new LogUtils.LogListener() {
            @Override
            public void onLogged(final StringBuffer log) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logText.setText(log.toString());
                        logText.invalidate();
                    }
                });
            }
        });

    }

    private int retrieveIndexOf(String currency) {
        return Arrays.asList(Constants.CURRENCY_CODES).indexOf(currency);
    }

    private void addCurrencySelectionListener() {

        mBaseCurrencyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mBaseCurrency = Constants.CURRENCY_CODES[position];
                LogUtils.log(TAG, "Base Currency has changed to: " + mBaseCurrency);
                SharedPreferencesUtils.updateCurrency(MainActivity.this, mBaseCurrency, true);
                retrieveCurrencyExchangeRate();

            }
        });

        mTargetCurrencyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mTargetCurrency = Constants.CURRENCY_CODES[position];
                LogUtils.log(TAG, "Target Currency has changed to: " + mTargetCurrency);
                SharedPreferencesUtils.updateCurrency(MainActivity.this, mTargetCurrency, false);
                retrieveCurrencyExchangeRate();

            }
        });

    }

    private void retrieveCurrencyExchangeRate() {

        if (mServiceRepetition < AlarmUtils.REPEAT.values().length) {

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
            //startService(intent);

            AlarmUtils.startService(this, intent,
                    AlarmUtils.REPEAT.values()[mServiceRepetition]);

        }

    }

    private void resetDownloads() {
        SharedPreferencesUtils.updateNumDownloads(this, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_clear_logs:
                LogUtils.clearLogs();
                return true;
            case R.id.action_show_logs:
                mIsLogVisible = !mIsLogVisible;
                item.setIcon(mIsLogVisible ? R.drawable.ic_keyboard_hide : R.drawable.ic_keyboard);
                mLogLayout.setVisibility(mIsLogVisible ? View.VISIBLE : View.GONE);
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);

    }
}
