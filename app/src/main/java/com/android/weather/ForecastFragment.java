package com.android.weather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by android on 16/3/15.
 */
public class ForecastFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new String[]{"Java", "Scala"});

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);

        return rootView;
    }

    public static Double getMinTempForDay(String json, int index) throws JSONException {
        JSONObject days = new JSONObject(json);
        JSONArray daysInfo = days.getJSONArray("list");
        JSONObject item = daysInfo.getJSONObject(index);
        JSONObject temp = item.getJSONObject("temp");
        return temp.getDouble("min");
    }
    public static Double getMaxTempForDay(String json, int index) throws JSONException {
        JSONObject days = new JSONObject(json);
        JSONArray daysInfo = days.getJSONArray("list");
        JSONObject item = daysInfo.getJSONObject(index);
        JSONObject temp = item.getJSONObject("temp");
        return temp.getDouble("max");
    }
    public static Double getTempForDay(String json, int index) throws JSONException {
        JSONObject days = new JSONObject(json);
        JSONArray daysInfo = days.getJSONArray("list");
        JSONObject item = daysInfo.getJSONObject(index);
        JSONObject temp = item.getJSONObject("temp");
        return temp.getDouble("day");
    }
    public static Long getTime(String json, int index) throws JSONException {
        JSONObject days = new JSONObject(json);
        JSONArray daysInfo = days.getJSONArray("list");
        JSONObject item = daysInfo.getJSONObject(index);
        return item.getLong("dt");
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String> {
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
        protected String doInBackground(String... params) {
            String jsonData = null;
            HttpGet get = new HttpGet(params[0]);
            HttpClient client = new DefaultHttpClient();
            try {
                HttpResponse response = client.execute(get);
                if(response == null) return null;
                HttpEntity entity = response.getEntity();
                if(entity == null) return null;
                if(response.getStatusLine().getStatusCode() == 200) {
                    jsonData = EntityUtils.toString(entity);
                    Log.i(LOG_TAG, "Got data successfully from open weather");
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                Log.i(LOG_TAG, "Fetching data failed");
                Log.d(LOG_TAG, ex.getMessage());
            }
            return jsonData;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if(dialog != null) dialog.dismiss();
        }

        @Override
        protected void onCancelled(String result) {
            super.onCancelled(result);
            if(dialog != null) dialog.dismiss();
        }
    }
}