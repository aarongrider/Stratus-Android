package edu.spu.teamroot.voicecloud;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class JSONTransmitter extends AsyncTask<JSONObject, JSONObject, JSONObject> {

    String baseURL = "http://52.24.35.51/cloud";

    @Override
    protected JSONObject doInBackground(JSONObject... data) {

        String url = baseURL;

        if (data[0].has("layout")) url = baseURL + "/save";
        else url = baseURL + "/load.json";

        // Create json string from JSONObject
        String json = data[0].toString();

        // Set up client connection
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 100000);

        // Initialize return data object
        InputStream inputStream = null;
        String result = "";

        HttpPost httpPost = new HttpPost(url);

        try {

            // Create StringEntity and add to POST
            //StringEntity se = new StringEntity(key+"="+json);
            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);

            // Set headers so server knows what we are sending
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // Execute POST request
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // Receive response
            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // Log result
        Log.d("Result", result);

        // Return result
        JSONObject returnObject = new JSONObject();
        try {
            returnObject.put("id", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return returnObject;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

}