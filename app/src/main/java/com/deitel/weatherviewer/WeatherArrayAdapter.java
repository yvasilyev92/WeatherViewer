package com.deitel.weatherviewer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
/**
 * Created by Yevgeniy on 6/15/2017.
 */


//To map weather data to ListView items, we extend class ArrayAdapter so that we can override
//ArrayAdapter method getView to configure a custom layout for each ListView item.
public class WeatherArrayAdapter extends ArrayAdapter<Weather> {

    //We use instance variable "bitmaps" to cache previously loaded weather-condition images
    //so they do not need to be redownloaded as the user scrolls through the weather forecast.
    //The cached images will remain in memory until Android terminates the app.
    private Map<String, Bitmap> bitmaps = new HashMap<>();

    //Here we create a constructor to initialize superclass inherited members,
    // , the constructor simply calls the superclass's 3 argument constructor
    //passing Context (i.e the activity in which the ListView is displayed)
    //and List<Weather> (the list of data to display) as the 1st and 3rd arguments.
    //The second superclass constructor arg represents a layout resource ID for a layout
    //that contains a TextView in which a ListView items data is displayed.
    //We put "-1" to indicate that we use a custom layout in this app so we can display
    //more than 1 textview.

    public WeatherArrayAdapter(Context context, List<Weather> forecast){
        super(context, -1, forecast);
    }



    //Nested clas ViewHolder defines instance variables that class WeatherArrayAdapter
    //access directly when manipulating ViewHolder objects. When a ListView item is created,
    //we'll associate a new ViewHolder object with that item.
    private static class ViewHolder{
        ImageView conditionImageView;
        TextView dayTextView;
        TextView lowTextView;
        TextView hiTextView;
        TextView humidityTextView;
    }


    //The method getView is called to get the View that displays a ListView item's data.
    //Overriding this method enables you to map data to a custom ListView item. The method
    //receives the ListView item's position, the View (convertView) representing that ListView item,
    //and the ListView item's parents as arguments.
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //here we call the inherited ArrayAdapter method getItem() to get from the List<Weather>
        //the weather object that will be displayed.
        Weather day = getItem(position);

        //Here we define the Viewholder variable that will be set a new ViewHolder object
        //or an existing one, depending on whether method getView's convertView arg is null.
        ViewHolder viewHolder;

        if (convertView == null){
            //If convertView is null we create a new ViewHolder to store references to a new
            //ListView's item's views.
            viewHolder = new ViewHolder();

            //Then we get the layout inflater which we use to inflate ListView item's layout.
            LayoutInflater inflater = LayoutInflater.from(getContext());

            //the first arg is the layout to inflate, the second is the layout's parent ViewGroup,
            //the third is a boolean indicating whether the views should be attached automatically.
            //We say "false" because the ListView calls method getView to obtain the item's view,
            //then attaches it to the listview.
            convertView = inflater.inflate(R.layout.list_item, parent,false);

            //Get references to views
            viewHolder.conditionImageView = (ImageView) convertView.findViewById(R.id.conditionImageView);
            viewHolder.dayTextView = (TextView) convertView.findViewById(R.id.dayTextView);
            viewHolder.lowTextView = (TextView) convertView.findViewById(R.id.lowTextView);
            viewHolder.hiTextView = (TextView) convertView.findViewById(R.id.hiTextView);
            viewHolder.humidityTextView = (TextView) convertView.findViewById(R.id.humidityTextView);

            //Here we set the new ViewHolder object as the ListView item's tag to store the ViewHolder with
            //the ListView item for future use.
            convertView.setTag(viewHolder);
        }
        else {
            //if the convertView is not null, the ListView is reusing a ListView item that scrolled off the screen
            //we use getTag() which gets the current ListView item's tag, which is the ViewHolder
            //that was prev attached to it.
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Now that we the ViewHolder we can start to set the data for the ListItem's views.

        //if the weather-condition image was prev downloaded, the bitmaps object will contain a key
        //for the Weather object's iconURL.
        if (bitmaps.containsKey(day.iconURL)){
            //if so, we get the existing Bitmap from bitmaps and set the conditionImageView's image
            viewHolder.conditionImageView.setImageBitmap(bitmaps.get(day.iconURL));
        }
        else {
            //otherwise we create a new LoadImageTask to download the image in a seperate thread.
            //The task's execute method receives the iconURL and initiates the task.
            new LoadImageTask(viewHolder.conditionImageView).execute(day.iconURL);
        }

        //Then  we set the Strings for the ListView item's TextViews

        //get other data from Weather object and place into views.
        Context context = getContext(); // for loading String resources.
        viewHolder.dayTextView.setText(context.getString(R.string.day_description, day.dayOfWeek, day.description));
        viewHolder.lowTextView.setText(context.getString(R.string.low_temp, day.minTemp));
        viewHolder.hiTextView.setText(context.getString(R.string.high_temp, day.maxTemp));
        viewHolder.humidityTextView.setText(context.getString(R.string.humidity, day.humidity));

        //Finally we return ListView item's configured View.
        return convertView;
    }


    //Nested class LoadImageTask defines how to download a weather-condition image in a seperate thread.
    //then return the image to the GUI thread for display in the ListView item's imageview.
    //AsyncTask is a generic type that requires 3 parameters:
    //1: doInBackground:
    //2: onProgressUpdate: (optional), we dont use so we put Void
    //3: onPostExecute:
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap>{

        //displays the thumbnail.
        private ImageView imageView;
        //store ImageView on which to set the downloaded Bitmap
        public LoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        //load image; params[0] is the String URL representing the image
        //We use a variable-length parameter-list type (String) for
        //the doInBackground method which you must overload.
        //When you call the tasks execute method its creates a thread
        //in which doInBackground performs the task. This app passes the weather conditions
        //icon's URL string as the argument to the execute method.
        //doInBackground uses an HttpURLConnection to download the weather-condition image.
        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                //Here we convert the URL string that was passed to the asynctask's execute method into a URL object.
                URL url = new URL(params[0]); //create URL for image

                //open an HttpURLConnection, get its InputStream and download the image.
                //we call the URL method openConnection to get an HttpURLConnection - the cast is required
                //because the method returns a URLConnection.
                //"openConnection" requests the content specified by the URL.
                connection = (HttpURLConnection) url.openConnection();


                //Here we get the HttpURLConnection's InputStream,
                try (InputStream inputStream = connection.getInputStream()){

                    //which we pass to BitmapFactory method "decodeStream" to read the image bytes and return a
                    //bitmap object containing the image.
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    //Then we cache the downloaded image in the bitmaps Map for potential reuse.
                    bitmaps.put(params[0], bitmap);

                } catch (Exception e){
                    e.printStackTrace();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            finally {
                connection.disconnect(); //close the connection.
            }
            //returns the downloaded Bitmap, which is then passed to onPostExecute
            //in the GUI thread, to display the image.
            return bitmap;
        }

        //set weather condition image in list item.
        //Bitmap is the type of the task's result,which is passed to the onPostExecute method.
        //This method executes in the GUI thread and enables the ListView item's ImageView to display
        //asynctask's results. The ImageView to update is specified as an argument to class LoadImageTask's constructor.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

}
