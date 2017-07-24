package com.deitel.weatherviewer;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.content.Context;
import android.os.AsyncTask;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

//MainActivity define the app user's interface, the logic for interacting with the
//openweathermap.org web service and the logic for processing the JSON response from the web service.
//The nested GetWeatherTask asynctask performs the web service request in a seperate thread.
//This app doesnt require a menu so we remove methods onCreateOptionsMenu & onOptionsItemSelected
public class MainActivity extends AppCompatActivity {

    //weatherList is an ArrayList that stores Weather objects, each object represents one day in forecast.
    private List<Weather> weatherList = new ArrayList<>();
    //weatherArrayAdapter will refer to a WeatherArrayAdapter object that binds the weatherList to the ListView'sitems.
    private WeatherArrayAdapter weatherArrayAdapter;
    //weatherListView will refer to MainActivity's ListView. It will display weather info.
    private ListView weatherListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //inflate GUI
        setContentView(R.layout.activity_main);
        //create Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //attach Toolbar to activity.
        setSupportActionBar(toolbar);


        weatherListView = (ListView) findViewById(R.id.weatherListView);
        weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
        weatherListView.setAdapter(weatherArrayAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get a reference to the app's EditText then use it to get the user's input.
                EditText locationEditText = (EditText) findViewById(R.id.locationEditText);
                //we then pass it to the createURL method to create the URL representing the web service request
                //that will returnthe city's weather forecast.
                URL url = createURL(locationEditText.getText().toString());


                //if the URL is created successfully we hide the keyboard and create a new GetWeatherTask
                //to obtain the weather forecast in a seperate thread.
                //if url is not successfully created we create snackbar saying url was invalid.
                if (url != null){
                    dismissKeyboard(locationEditText);
                    GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
                    //we execute that task by passing the URL of the webservice as an arg to asyntask.
                    getLocalWeatherTask.execute(url);
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.invalid_url,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    //method dismissKeyboard is called to hide the soft keyboard when the user touches the FAB
    //to submit a city to the app.
    private void dismissKeyboard(View view){

        //Android provides a service for managing the keyboard programatically, you can obtain a ref
        //to this by calling getSystemService with the constant Context.INPUT_METHOD_SERVICE.
        //getSystemService can return objects of many dif types so you must cast it appropriately.
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //to dismiss the keyboard, call inputmethodmanager's method hideSoftInputFromWindow.
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //method createURL assembles the String representation of the URL for the web service request.
    //create openweathermap.org web service url using city.
    private URL createURL(String city){

        String apiKey = BuildConfig.API_KEY;
        String baseUrl = getString(R.string.web_service_url);

        //Here we attempt to create and return a URL object initialized with the URL String.
        try {
            //create URL for specified city and imperial units (fahrenheit)
            //we add parameters to the web service query.
            String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") + "&units=imperial&cnt=16&APPID=" + apiKey;
            return new URL(urlString);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null; //URL was malformed
    }


    //Nested AsyncTask subclass GetWeatherTask performs the web service request and processes
    //the response in a seperate thread, then passes the forecast info as a JSONObject to the
    //GUI thread for display.
    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject>{

        //GetWeatherTask's 3 generic type parameters are
        //URL - for the doInBackground method - the web service request URL.
        //Void - for onProgressUpdate which we dont use
        //JSONObject -for the type of the task's result, which is passed to onPostExecute
        // in the GUI thread to display the results.



        //the doInBackground method creates the HttpURLConnection's thats used to invoke the web service.
        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;



            try {
                //simply opening the connection makes the request.
                connection = (HttpURLConnection) params[0].openConnection();
                //we get the response code from the server.
                int response = connection.getResponseCode();


                //if the response code is HttpURLConnection.HTTP_OK the web service was invoked properly
                //and there is a response to process.
                if (response == HttpURLConnection.HTTP_OK){

                    StringBuilder stringBuilder = new StringBuilder();
                    //In this case we get the HttpURLConnection's InputStream, wrap it in a BufferedReader
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    )){
                        //read each line of text from the response and append it to a StringBuilder.
                        String line;
                        while ((line = reader.readLine()) != null){
                            stringBuilder.append(line);
                        }
                    } catch (IOException e){
                        //if an error occurs we display a Snackbar indicating a problem occured.
                        Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.read_error,
                                Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    //Then we convert the JSON String in the StringBuilder to a JSON Object
                    //and return it to the GUI thread.
                    return new JSONObject(stringBuilder.toString());
                }
                else {
                    //if an error occurs we display a Snackbar indicating a problem occured.
                    Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connect_error,
                            Snackbar.LENGTH_LONG).show();
                }
            }
            catch (Exception e){
                //if an error occurs we display a Snackbar indicating a problem occured.
                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connect_error,
                        Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }
            finally {
                connection.disconnect(); //close the HttpUrlConnection
            }
            return null;
        }

        //process JSON response and update ListView
        @Override
        protected void onPostExecute(JSONObject weather) {

            //When onPostExecute is called in the GUI thread, we call convertJSONtoArrayList
            //to extract the weather data from JSONObject and place it in weatherList
            convertJSONtoArrayList(weather); //repopulate weatherList

            //then we call ArrayAdapter's notifyDataSetChanged method which causes
            //weatherListView to update itself with new data.
            weatherArrayAdapter.notifyDataSetChanged();

            //then we call ListView's smoothToScrollPosition to reposition the ListView's
            //first item to the top of the ListView. - this ensures that the new weather
            //forecast's first day is shown at the top.
            weatherListView.smoothScrollToPosition(0);
        }
    }


    //openweathermap.org returns JSON.
    //the convertJSONtoArrayList method extracts this weather data from its JSON argument.
    //create Weather objects from JSONObject containing the forecast.
    private void convertJSONtoArrayList(JSONObject forecast){

        //First we clear the weatherList of any existing Weather objects.
        weatherList.clear();

        //Processing JSON data in a JSONObject or JSONArray can result in JSONExceptions
        //so we use a try block.

        try {
            //First we obtain the "list" element which is an array in the JSON object returned
            //by calling getJSONArray. "list" contains "dt" and the "temp" array
            //"temp" contains the min/max temps we want.
            JSONArray list = forecast.getJSONArray("list");

            //Then we create a Weather object for every element in JSONArray.
            //JSONArray method length returns the number of elements in the array.
            for (int i = 0; i < list.length(); i++){

                //get one day's forecast by calling getJSONObject, which receives an index as its arg.
                JSONObject day = list.getJSONObject(i);
                //get the "temp" JSON object, which contains the day's temp data.
                JSONObject temperatures = day.getJSONObject("temp");
                //get the "weather" JSONArray and get its first element(0), which also happens to be
                //an array that contains the day's weather descrip and icon.
                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);

                //Then we create a Weather object and add it to the weatherList array.
                weatherList.add(new Weather(
                        day.getLong("dt"), //date/time timestamp
                        temperatures.getDouble("min"), //min temp
                        temperatures.getDouble("max"), //max temp
                        day.getDouble("humidity"), //percent humidity
                        weather.getString("description"), //weather conditions
                        weather.getString("icon") //icon name
                ));
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }


}
