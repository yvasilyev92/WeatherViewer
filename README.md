# WeatherViewer
Android App uses to OpenWeatherMap.org API to show user the Weather in the city of their choice.
This is the WeatherView App from the Android 6 for Programmers book 3rd Edition by Deitel Developer Series. (C) Copyright 1992-2014 by Deitel & Associates, Inc. and Pearson Education, Inc. All Rights Reserved.

This app shows how to use REST Web Services, AsyncTask, HttpUrlConnection, Processing JSON Responses, JSONObject, JSONArray, ListView,
ArrayAdapter, ViewHolder Pattern, and TextInputLayout.

The WeatherViewer App uses the free OpenWeatherMap.org REST Webservies to obtain a 16-day weather forecast depending on the city 
the user specifies. We use an AsyncTask and HttpUrlConnection to invoke REST web service to download an image in a separate thread and 
deliver the results to the GUI thread. We process a JSON response using package org.json classes JSONObjects and JSONArrays.
We define an ArrayAdapter that specifies the data to display in a ListView. We use the ViewHolder pattern to reuse views that scroll off
the screen in a ListView.

I, myself, specifically commented on every line of code so that a beginner Android developer may clearly understand and learn from this application.
