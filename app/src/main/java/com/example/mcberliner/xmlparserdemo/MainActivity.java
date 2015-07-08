package com.example.mcberliner.xmlparserdemo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {

    private String baseUrl = "http://api.openweathermap.org/data/2.5/weather?q=";
    private String parseMode = "andmode=xml";

    private EditText etLocation;
    private TextView tvOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etLocation = (EditText) findViewById(R.id.etLocation);
        tvOutput = (TextView) findViewById(R.id.tvOutput);
    }

    public void getWeather(View view) {
        String location = etLocation.getText().toString();
        String urlString = baseUrl + location + parseMode;

        new XMLParserTask().execute(urlString);
    }

    public String[] parseXML(XmlPullParser parser) {
        String[] data = {null, null, null};
        int event;

        try {
            event = parser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();

                switch (event) {
                    case XmlPullParser.END_TAG:
                        if (name.equals("humidity")) {
                            data[0] = parser.getAttributeValue(null, "value");
                        } else if (name.equals("pressure")) {
                            data[1] = parser.getAttributeValue(null, "value");
                        } else if (name.equals("temperature")) {
                            data[2] = parser.getAttributeValue(null, "value");
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private String fetchWeatherData (String urlString) {
        String[] dataArr = {null, null, null};
        String weatherData = null;

        XmlPullParserFactory xmlFactory;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.connect();

            InputStream stream = conn.getInputStream();
            xmlFactory = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactory.newPullParser();

            myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            myParser.setInput(stream, null);
            dataArr = parseXML(myParser);
            stream.close();

            if (dataArr[0] != null && dataArr[1] != null && dataArr[2] != null) {
                weatherData = "Temperature: " + dataArr[2] + "\nHumidity: " + dataArr[0] + "\nPressure: " + dataArr[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weatherData;
    }

    private class XMLParserTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String weatherData = fetchWeatherData(params[0]);
            return weatherData;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                tvOutput.setText(result);
            } else {
                tvOutput.setText("Can't fetch weather data!");
            }
        }
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
