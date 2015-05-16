package com.fujitsu.jp.stadiumcoach;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;

/**
 * Created by yokoi.shinya on 2015/01/19.
 */
public class SendHttpRequest {

    public String sendRequestToGarako(String projectId) {

        HttpClient httpClient = new DefaultHttpClient();

        //StringBuilder uri = new StringBuilder("http://ec2-54-65-250-88.ap-northeast-1.compute.amazonaws.com/python/querygyarako.py?foo=" + msg);
        StringBuilder uri = new StringBuilder("https://limitless-sands-8750.herokuapp.com/projects/"+ projectId + "/events.json");
        HttpGet request = new HttpGet(uri.toString());

        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(request);
        } catch (Exception e) {
            Log.d("HttpSampleActivity", "Error Execute");
        }


        //レスポンスの処理
        String url = "";
        int status = httpResponse.getStatusLine().getStatusCode();

        if (HttpStatus.SC_OK == status) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                httpResponse.getEntity().writeTo(outputStream);
                url = outputStream.toString();
            } catch (Exception e) {
                Log.d("HttpSampleActivity", "Error");
            }
        } else {
            Log.d("HttpSampleActivity", "Status" + status);
        }

        return url;
    }

    public String getProjectList(){
        HttpClient httpClient = new DefaultHttpClient();

        //StringBuilder uri = new StringBuilder("http://ec2-54-65-250-88.ap-northeast-1.compute.amazonaws.com/python/querygyarako.py?foo=" + msg);
        StringBuilder uri = new StringBuilder("https://limitless-sands-8750.herokuapp.com/projects.json");
        HttpGet request = new HttpGet(uri.toString());

        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(request);
        } catch (Exception e) {
            Log.d("HttpSampleActivity", "Error Execute");
        }


        //レスポンスの処理
        String list = "";
        int status = httpResponse.getStatusLine().getStatusCode();

        if (HttpStatus.SC_OK == status) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                httpResponse.getEntity().writeTo(outputStream);
                list = outputStream.toString();
            } catch (Exception e) {
                Log.d("HttpSampleActivity", "Error");
            }
        } else {
            Log.d("HttpSampleActivity", "Status" + status);
        }

        return list;
    }
}
