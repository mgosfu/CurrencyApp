package com.mgodevelopment.currencyapp.utils;

import com.mgodevelopment.currencyapp.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Martin on 9/13/2016.
 */
public class WebServiceUtils {

    public static final String TAG = WebServiceUtils.class.getName();

    public static JSONObject requestJSONObject(String serviceURL) {

        HttpURLConnection urlConnection = null;
        try {

            URL urlToRequest = new URL(serviceURL);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                LogUtils.log(TAG, "Unauthorized access!");
            } else if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                LogUtils.log(TAG, "404 page not found");
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                LogUtils.log(TAG, "URL Response error");
            }
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONObject(convertInputStreamtoString(in));

        } catch (MalformedURLException e) {
            LogUtils.log(TAG, e.getMessage());
        } catch (SocketTimeoutException e) {
            LogUtils.log(TAG, e.getMessage());
        } catch (IOException e)  {
            LogUtils.log(TAG, e.getMessage());
        } catch (JSONException e) {
            LogUtils.log(TAG, e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;

    }

}
