package com.example.okhttp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView responseText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button sendRequest = (Button) findViewById(R.id.send_request);
        responseText = (TextView) findViewById(R.id.response_text);
        sendRequest.setOnClickListener(this);
    }

    public void getClick(View view) {

        //1. 获取OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();

        //2. 构造Request对象
        Request.Builder builder = new Request.Builder();
        builder.get();
        builder.url("https://www.baidu.com");
        Request request = builder.build();

        //3. 将Request封装成Call
        Call call = okHttpClient.newCall(request);

        // 异步 (同步调用会阻塞主线程，一般不使用。)
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("1103", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("1103", "onResponse: " + Objects.requireNonNull(response.body()).string());
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_request) {
            sendRequestWithOkHttp();
        }
    }

    private void sendRequestWithOkHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //1. 创建OKHTTPClient实例
                    OkHttpClient client = new OkHttpClient();

                    //2. 创建Request对象,设置目标网络地址
                    Request request = new Request.Builder()

                            //这里降低了targetSdk版本 不然会报错，无法解析
                            .url("http://10.0.2.2/get_data.json")
                            .build();

                    //3. 发送请求获取返回数据
                    Response response = client.newCall(request).execute();

                    //4. 获得具体内容
                    String responseData = Objects.requireNonNull(response.body()).string();

                    //5. 自定义操作
                    //showResponse(responseData); //打印到屏幕

                    //parseXMLWithPull(responseData); //解析获取到的xml

                    //parseJSONWithJSONObject(responseData); //用JSONObject解析json

                    parseJSONWithGSON(responseData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSONWithGSON(String jsonData) {
        Gson gson = new Gson();
        List<App> appList = gson.fromJson(jsonData,
                new TypeToken<List<App>>() {
                }.getType());
        for (App app : appList) {
            Log.d("MainActivity", "id is: " + app.getId());
            Log.d("MainActivity", "name is: " + app.getName());
            Log.d("MainActivity", "version is: " + app.getVersion());
        }
    }

    private void parseJSONWithJSONObject(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");
                String version = jsonObject.getString("version");
                Log.d("MainActivity", "id is: " + id);
                Log.d("MainActivity", "name is: " + name);
                Log.d("MainActivity", "version is: " + version);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseXMLWithPull(String xmlData) {
        try {

            //1. 获取XmlPullParserFactory实例
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

            //2. 利用工厂模式创建对象
            XmlPullParser xmlPullParser = factory.newPullParser();

            //3. 将获得的xml数据传进去，开始解析
            xmlPullParser.setInput(new StringReader(xmlData));

            //4. 获取当前解析事件
            int eventType = xmlPullParser.getEventType();
            String id = "";
            String name = "";
            String version = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();

                switch (eventType) {

                    // 开始解析一个节点
                    case XmlPullParser.START_TAG: {
                        if ("id".equals(nodeName)) {
                            id = xmlPullParser.nextText();
                        } else if ("name".equals(nodeName)) {
                            name = xmlPullParser.nextText();
                        } else if ("version".equals(nodeName)) {
                            version = xmlPullParser.nextText();
                        }
                        break;
                    }

                    // 完成解析某个节点
                    case XmlPullParser.END_TAG: {
                        if ("app".equals(nodeName)) {
                            Log.d("MainActivity", "id is: " + id);
                            Log.d("MainActivity", "name is: " + name);
                            Log.d("MainActivity", "version is: " + version);
                        }
                        break;
                    }

                    default:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showResponse(String responseData) {

        //返回主线程
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                responseText.setText(responseData);
            }
        });
    }
}