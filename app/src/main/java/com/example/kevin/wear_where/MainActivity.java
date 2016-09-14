package com.example.kevin.wear_where;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


import com.example.kevin.wear_where.data.Channel;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

public class MainActivity extends AppCompatActivity {

    TextView temperature, location, description;            // TextView in xml
    URL request;                                            // The link requesting service from Yahoo query
    Channel channel;                                        // Channel Object


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temperature = (TextView) findViewById(R.id.temperature);
        location = (TextView) findViewById(R.id.location);
        description = (TextView) findViewById(R.id.description);

        channel = new Channel();

        getRequest();
    }

    private void displayResults() {
        temperature.setText("" + channel.getItem().getCondition().getTemperature() + (char) 0x00B0 + " " + channel.getUnits().getTemperatureUnit());
        location.setText(channel.getLocation());
        description.setText(channel.getItem().getCondition().getDescription());
        // Debugger
        //Log.d("WearWhere", "" + channel.getItem().getCondition().getTemperature());
        //temperature.setText("" + channel.getLocation());
    }

    public void getRequest() {
        new AsyncTask<Void, Channel, Channel>() {

            @Override
            protected Channel doInBackground(Void... params) {
                // Temporary Channel Object holder
                Channel channelTemp = new Channel();

                String query = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"buffalo, ny\")");

                String link = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(query));

                try {
                    request = new URL(link);
                    // Open a URL connection to link
                    URLConnection urlConnection = request.openConnection();
                    // Get the input stream of link
                    InputStream in = urlConnection.getInputStream();
                    // Read buffer
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    // Store the buffer of link into result
                    StringBuilder result = new StringBuilder();
                    // Store each line of buffer into line
                    String line;

                    // Get each line from buffer and stores them into result
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    JSONObject data = new JSONObject(result.toString());
                    JSONObject queryResult = data.optJSONObject("query");

                    // Check for number of results to verify acceptable location
                    int count = queryResult.optInt("count");
                    if (count == 0) {
                        return null;
                    }

                    JSONObject resultsObject = queryResult.optJSONObject("results");
                    JSONObject channelObject = resultsObject.optJSONObject("channel");

                    channelTemp.retrieveData(channelObject);

                    return channelTemp;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Channel item) {
                channel = item;
                displayResults();
            }
        }.execute();
    }
}
