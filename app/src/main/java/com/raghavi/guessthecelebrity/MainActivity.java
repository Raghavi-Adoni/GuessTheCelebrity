package com.raghavi.guessthecelebrity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button opt1;
    Button opt2;
    Button opt3;
    Button opt4;

    int chosenCeleb;
    int correctLocation;

    ArrayList<String> imageURLs;
    ArrayList<String> names;
    String[] answers;
    String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialise();

        DownloadSource task = new DownloadSource();

        try {
            try {
                //getting the data in result variable
                result = task.execute("http://www.posh24.se/kandisar").get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("Contents are:", result);

            //discarding the data after listed artiles and it's not required
            String[] splitresult = result.split("<div class=\"listedArticles\">");

            //getting the URL from the data
            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(splitresult[0]);
            while (m.find()) {
                imageURLs.add(m.group(1));
            }

            //getting the name of the celebrity from the data
            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitresult[0]);
            while (m.find()) {
                names.add(m.group(1));
            }

            NewQuestions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //getting in new questions
    public void NewQuestions() throws ExecutionException, InterruptedException {
        Random rand = new Random();

        chosenCeleb = rand.nextInt(imageURLs.size());
        ImageDownloader downloader = new ImageDownloader();
        Bitmap myImage = downloader.execute(imageURLs.get(chosenCeleb)).get();
        imageView.setImageBitmap(myImage);

        correctLocation = rand.nextInt(4);
        answers[correctLocation] = names.get(chosenCeleb);

        int incorrectLocation;
        for (int i = 0; i < 4; i++) {
            if (i != correctLocation) {
                incorrectLocation = rand.nextInt(imageURLs.size());
                while (chosenCeleb == incorrectLocation) {
                    incorrectLocation = rand.nextInt(imageURLs.size());
                }

                answers[i] = names.get(incorrectLocation);


            }
        }

        opt1.setText(answers[0]);
        opt2.setText(answers[1]);
        opt3.setText(answers[2]);
        opt4.setText(answers[3]);

    }


    //Downloading the image
    @SuppressLint("StaticFieldLeak")
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;


            } catch (Exception e1) {
                e1.printStackTrace();
                return null;
            }

        }


    }


    //setting up the onclick listeners
    public void onClick(View view) throws ExecutionException, InterruptedException {

        if (view.getTag().toString().equals(Integer.toString(correctLocation))) {
            Toast.makeText(getApplicationContext(), "Correct Answer", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "WRONG! Correct Answer is" + names.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }
        NewQuestions();
    }

    //Downloading all the html source code
    private class DownloadSource extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                // get URL
                URL url = new URL(urls[0]);
                // create connection from URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // connect
                connection.connect();
                // get InputStream from the connection
                InputStream inputStream = connection.getInputStream();
                // StreamReader for the inputStream
                InputStreamReader streamReader = new InputStreamReader(inputStream);
                // reads the first char
                int data = streamReader.read();

                while (data != -1) {

                    stringBuilder.append((char) data);
                    // read next char
                    data = streamReader.read();
                }

                // return stringBuilder as String
                return stringBuilder.toString();

            } catch (MalformedURLException e) {
                Log.i("ERROR", "MalformedURLException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.i("ERROR", "IOException");
                e.printStackTrace();
            }
            return null;
        }
    }

    public void initialise() {
        imageURLs = new ArrayList<String>();
        names = new ArrayList<String>();

        opt1 = (Button) findViewById(R.id.button);
        opt2 = (Button) findViewById(R.id.button2);
        opt3 = (Button) findViewById(R.id.button3);
        opt4 = (Button) findViewById(R.id.button4);

        imageView = (ImageView) findViewById(R.id.image);

        answers = new String[4];
        correctLocation = 0;
        result = "";

    }
}