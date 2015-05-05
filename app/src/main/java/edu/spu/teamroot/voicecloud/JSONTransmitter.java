package edu.spu.teamroot.voicecloud;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class JSONTransmitter extends AsyncTask<JSONObject, JSONObject, JSONObject> {

    String url = "http://72.233.176.79:9000/";
    String key = "4r3hjiohs3jfiuh3";

    @Override
    protected JSONObject doInBackground(JSONObject... data) {
        String json = data[0].toString();
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 100000);
        JSONObject jsonResponse = null;
        HttpPost post = new HttpPost(url);
        try {
            StringEntity se = new StringEntity(key+"="+json);
            post.addHeader("content-type", "application/x-www-form-urlencoded");
            //se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);

            HttpResponse response = client.execute(post);
            String resFromServer = org.apache.http.util.EntityUtils.toString(response.getEntity());
            jsonResponse=new JSONObject(resFromServer);
            Log.i("Response from server", jsonResponse.toString());

        } catch (Exception e) { e.printStackTrace();}

        return jsonResponse;
    }

}