package com.mgodevelopment.currencyapp.helpers;

import com.mgodevelopment.currencyapp.Constants;
import com.mgodevelopment.currencyapp.value_objects.Currency;

import org.json.JSONObject;

/**
 * Created by Martin on 9/13/2016.
 */
public class CurrencyParserHelper {

    public static Currency parseCurrency(JSONObject obj, String currencyName) {

        Currency currency = new Currency();
        currency.setBase(obj.optString(Constants.BASE));
        currency.setDate(obj.optString(Constants.DATE));
        JSONObject rateObject = obj.optJSONObject(Constants.RATES);
        if (rateObject != null) {
            currency.setRate(rateObject.optDouble(currencyName));
        }
        currency.setName(currencyName);
        return currency;
    }

}
