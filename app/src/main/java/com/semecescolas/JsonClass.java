package com.semecescolas;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hildebrandosegundo on 25/08/16.
 */
public class JsonClass {

    InputStream input = null;
    JSONObject jObect = null;
    String json = "";

    //Recebe sua url
    public JSONObject getJSONFromUrl(String url, double lat, double lng, String serie) {
        UrlEncodedFormEntity form;
        //HTTP request
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("lat", "" + lat));
            nameValuePairs.add(new BasicNameValuePair("lng", "" + lng));
            nameValuePairs.add(new BasicNameValuePair("serie", serie));
            Log.i("lista post",""+nameValuePairs);
            form = new UrlEncodedFormEntity(nameValuePairs);
            form.setContentEncoding(HTTP.UTF_8);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(form);
            HttpResponse httpResponse = client.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            input = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    input, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            input.close();
            json = sb.toString();
            Log.i("JRF", json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // Transforma a String de resposta em um JSonObject
        try {
            jObect = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // retorna o objeto
        return jObect;

    }
    public JSONObject getJSONEscolas(String url) {
        //HTTP request
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse httpResponse = client.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            input = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    input, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            input.close();
            json = sb.toString();
            Log.i("JRF", json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // Transforma a String de resposta em um JSonObject
        try {
            jObect = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // retorna o objeto
        return jObect;

    }
}