package com.android.weather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by android on 16/3/15.
 */
public class ForecastFragment extends Fragment {

    public static final String URL = "http://api.openweathermap.org/data/2.5/forecast/daily?" +
            "q=London&mode=json&units=metric&cnt=7";

    ArrayAdapter<String> forecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.prefs_location_key),
                getString(R.string.default_location));
        new FetchWeatherTask(getActivity()).execute(new String[]{location});
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(new String[]{"Hit Refresh to Get the Data"}));

        forecastAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                list);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = forecastAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private Activity activity;
        private ProgressDialog dialog;

        public FetchWeatherTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(activity);
            dialog.setIndeterminate(true);
            dialog.setTitle("Processing ...");
            dialog.setMessage("Fetching Weather Data from Open Weather");
            dialog.show();
        }

        @Override
        protected String[] doInBackground(String... params) {

            String format = "json";
            String units = "metric";
            int numDays = 7;

            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.valueOf(numDays).toString())
                    .build();


            String jsonData = null;

            try {

                URL url = new URL(builtUri.toString());
                Log.d(LOG_TAG, url.toString());

                HttpGet get = new HttpGet(url.toString());
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(get);
                if(response == null) return null;
                HttpEntity entity = response.getEntity();
                if(entity == null) return null;
                if(response.getStatusLine().getStatusCode() == 200) {
                    jsonData = EntityUtils.toString(entity);
                    Log.i(LOG_TAG, jsonData);
                    Log.i(LOG_TAG, "Got data successfully from open weather");
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                Log.i(LOG_TAG, "Fetching data failed");
                Log.d(LOG_TAG, ex.getMessage());
            }
            if(jsonData == null) {
                return null;
            }else {
                try {
                    return getWeatherDataFromJson(jsonData, numDays);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(LOG_TAG, "Parsing JSON data failed");
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(String[] results) {
            super.onPostExecute(results);
            if(results != null) {
                forecastAdapter.clear();
                for(String result : results) {
                    forecastAdapter.add(result);
                }
            }
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if(dialog != null) dialog.dismiss();
        }

        @Override
        protected void onCancelled(String[] results) {
            super.onCancelled(results);
            if(dialog != null) dialog.dismiss();
        }

        /*
        public Double getMinTempForDay(String json, int index) throws JSONException {
            JSONObject days = new JSONObject(json);
            JSONArray daysInfo = days.getJSONArray("list");
            JSONObject item = daysInfo.getJSONObject(index);
            JSONObject temp = item.getJSONObject("temp");
            return temp.getDouble("min");
        }
        public Double getMaxTempForDay(String json, int index) throws JSONException {
            JSONObject days = new JSONObject(json);
            JSONArray daysInfo = days.getJSONArray("list");
            JSONObject item = daysInfo.getJSONObject(index);
            JSONObject temp = item.getJSONObject("temp");
            return temp.getDouble("max");
        }
        public Double getTempForDay(String json, int index) throws JSONException {
            JSONObject days = new JSONObject(json);
            JSONArray daysInfo = days.getJSONArray("list");
            JSONObject item = daysInfo.getJSONObject(index);
            JSONObject temp = item.getJSONObject("temp");
            return temp.getDouble("day");
        }
        public Long getTime(String json, int index) throws JSONException {
            JSONObject days = new JSONObject(json);
            JSONArray daysInfo = days.getJSONArray("list");
            JSONObject item = daysInfo.getJSONObject(index);
            return item.getLong("dt");
        }
        */

        public String[] getWeatherDataFromJson(String json, int numDays) throws JSONException {
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DATETIME = "dt";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(json);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] results = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;
                String highAndLow;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dateTime = dayForecast.getLong(OWM_DATETIME);
                day = getReadableDateString(dateTime);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);

                results[i] = day + " - " + description + " - " + highAndLow;
            }

            for(String s : results) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }

            return results;
        }

        public String getReadableDateString(long dateTime) {
            Date date = new Date();
            date.setTime(dateTime * 1000);
            return new SimpleDateFormat("EEE, dd MMM").format(date);
        }

        public String formatHighLows(double high, double low) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedPrefs.getString(getString(R.string.units_label_key),
                    getString(R.string.units_value));
            if (unitType.equals(getString(R.string.units_value2))) {
                high = (high * 1.8) + 32;
                low = (low * 1.8) + 32;
            } else if(!unitType.equals(getString(R.string.units_value))) {
                Log.d(LOG_TAG, "unknown units type found");
            }
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            return roundedHigh+"/"+roundedLow;
        }
    }
}