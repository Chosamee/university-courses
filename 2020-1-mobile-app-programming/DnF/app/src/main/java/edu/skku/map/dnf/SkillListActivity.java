package edu.skku.map.dnf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SkillListActivity extends AppCompatActivity {
    String apiKey = "SyPGiVveXthbRZsfez4hi3uPmmzSnv7g";
    String teet;
    JsonObject jsonObject;
    String jsonData;
    private RecyclerAdapter adapter;
    boolean isLogIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_list);

        TextView loginButton = (TextView)findViewById(R.id.loginButton);

        try{
            String characterName = MyGlobals.getInstance().getData().getCharacterName();
            isLogIn = true;
            loginButton.setText("어서오세요 " + characterName + " 님");

        }catch (Exception e){
            Log.d("TAG", "not login");
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLogIn){
                    Intent intent1 = new Intent(SkillListActivity.this, LoginActivity.class);
                    startActivity(intent1);
                }
            }
        });


        Intent intent = getIntent();
        String jobId = intent.getStringExtra("jobId");
        String jobGrowId = intent.getStringExtra("jobGrowId");

        final String targetUrl = "https://api.neople.co.kr/df/skills/" + jobId + "?jobGrowId=" + jobGrowId + "&apikey=" + apiKey;
        init();

        Thread mThread = new Thread() {
            @Override
            public void run() {
                jsonData = httpConnection(targetUrl);
                JsonParser parser = new JsonParser();
                Object object = parser.parse(jsonData);

                jsonObject = (JsonObject) object;
                teet = jsonObject.toString();

            }
        };

        mThread.start();

        try{
            mThread.join();
            JsonArray jarray = jsonObject.getAsJsonArray("skills");
            getData(jarray);


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ImageView goHome = (ImageView) findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(SkillListActivity.this, MainActivity.class);
                startActivity(intent1);
            }
        });
    }

    private void init() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void getData(JsonArray jarray){
        Log.d("aaaa", String.format("%d", jarray.size()));
        for(int i= 0; i< jarray.size(); i++){
            JsonElement jsonElement = jarray.get(i);
            String skillName = jsonElement.getAsJsonObject().get("name").getAsString();
            String skillId = jsonElement.getAsJsonObject().get("skillId").getAsString();
            String requiredLevel = jsonElement.getAsJsonObject().get("requiredLevel").getAsString();
            String skillType = jsonElement.getAsJsonObject().get("type").getAsString();
            String costType = jsonElement.getAsJsonObject().get("costType").getAsString();

            SkillData data = new SkillData();
            data.setSkillId(skillId);
            data.setCostType(costType);
            data.setSkillName(skillName);
            data.setSkillType(skillType);
            data.setRequiredLevel(requiredLevel);
            Log.d("dddddd", skillName);
            adapter.addItem(data);
        }

        adapter.notifyDataSetChanged();
    }

    public String httpConnection(String targetUrl) {
        URL url = null;
        HttpURLConnection conn = null;
        String jsonData = "";
        BufferedReader br = null;
        StringBuffer sb = null;
        String returnText = "";

        try {
            url = new URL(targetUrl);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.connect();

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            sb = new StringBuffer();

            while ((jsonData = br.readLine()) != null) {
                sb.append(jsonData);
            }

            returnText = sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return returnText;
    }
}