package edu.skku.map.dnf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import kotlin.ranges.CharRange;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {
    String apiKey = "SyPGiVveXthbRZsfez4hi3uPmmzSnv7g";
    boolean character_auth = false;
    boolean isCanUseId = true;
    JsonObject jsonObject;
    String jsonData;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        final EditText usernameET = (EditText) findViewById(R.id.username);
        final EditText passwordET = (EditText) findViewById(R.id.password);
        final EditText characterNameET = (EditText) findViewById(R.id.characterName);
        final EditText serverET = (EditText) findViewById(R.id.serverET);
        Button character_submit = (Button) findViewById(R.id.character_submit);
        Button submit = (Button) findViewById(R.id.submit);
        final ImageView characterImg = findViewById(R.id.characterImg);
        final TextView server = findViewById(R.id.server);
        final TextView level = findViewById(R.id.level);
        final TextView job = findViewById(R.id.job);
        final TextView jobGrow = findViewById(R.id.jobGrow);
        ImageView goHome = findViewById(R.id.goHome);

        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        character_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String characterName = characterNameET.getText().toString();
                final String serverVal = serverET.getText().toString();
                String serverKey;
                serverKey = changeServerKey(serverVal);
                final String targetUrl = "https://api.neople.co.kr/df/servers/" + serverKey + "/characters?characterName=" + characterName
                         + "&apikey=" + apiKey;

                Thread mThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            jsonData = httpConnection(targetUrl);
                            JsonParser parser = new JsonParser();
                            Object object = parser.parse(jsonData);
                            jsonObject = (JsonObject) object;
                            Log.d("asdsa", jsonObject.toString());
                        }
                        catch (Exception e){

                        }
                    }
                };
                mThread.start();

                try{
                    mThread.join();
                    JsonArray jarray = jsonObject.getAsJsonArray("rows");
                    JsonElement jsonElement = jarray.get(0);
                    CharacterInfo characterInfo = new CharacterInfo();
                    String characterId = jsonElement.getAsJsonObject().get("characterId").getAsString();
                    String charName = jsonElement.getAsJsonObject().get("characterName").getAsString();
                    String jobName = jsonElement.getAsJsonObject().get("jobName").getAsString();
                    String jobGrowName = jsonElement.getAsJsonObject().get("jobGrowName").getAsString();
                    String level = jsonElement.getAsJsonObject().get("level").getAsString();
                    characterInfo.setServerId(serverKey);
                    characterInfo.setCharacterId(characterId);
                    characterInfo.setCharacterName(charName);
                    characterInfo.setJobName(jobName);
                    characterInfo.setJobGrowName(jobGrowName);
                    characterInfo.setLevel(level);
                    MyGlobalChar.getInstance().setData(characterInfo);

                } catch (InterruptedException e) {
                    Toast.makeText(SignUpActivity.this, "error", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                catch (Exception e){
                    Toast.makeText(SignUpActivity.this, "올바른 캐릭터명을 입력하세요.", Toast.LENGTH_LONG).show();
                }

                Thread mThread2 = new Thread() {
                    @Override
                    public void run() {
                        try {
                            String serverKey = MyGlobalChar.getInstance().getData().getServerId();
                            String characterId = MyGlobalChar.getInstance().getData().getCharacterId();
                            URL url = new URL("https://img-api.neople.co.kr/df/servers/" + serverKey + "/characters/" + characterId);
                            Log.d("asdaa", url.toString());
                            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
                            connect.connect();
                            BufferedInputStream bis = new BufferedInputStream(connect.getInputStream());
                            Bitmap bm = BitmapFactory.decodeStream(bis);
                            bis.close();
                            MyGlobalChar.getInstance().getData().setCharacterPic(bm);
                        } catch (MalformedURLException e) {
                        } catch (IOException e) {
                        }
                        catch (Exception e){
                        }
                    }
                };
                mThread2.start();

                try {
                    mThread2.join();
                    characterImg.setImageBitmap(MyGlobalChar.getInstance().getData().getCharacterPic());
                    character_auth = true;
                    server.setText(serverVal);
                    level.setText(MyGlobalChar.getInstance().getData().getLevel());
                    job.setText(MyGlobalChar.getInstance().getData().getJobName());
                    jobGrow.setText(MyGlobalChar.getInstance().getData().getJobGrowName());
                }
                catch (InterruptedException e){
                    Toast.makeText(SignUpActivity.this, "error", Toast.LENGTH_LONG).show();
                }
                catch (Exception e){

                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String serverVal = serverET.getText().toString();
                final String username = usernameET.getText().toString();
                final String password = passwordET.getText().toString();
                CollectionReference userRef = db.collection("users");
                Query query = userRef.whereEqualTo("username", username);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().isEmpty()){
                            if(username.getBytes().length <= 0 || password.getBytes().length <= 0){
                                Toast.makeText(SignUpActivity.this, "Please fill all blanks", Toast.LENGTH_LONG).show();
                            }
                            else if(character_auth){
                                final Map<String, Object> daat = new HashMap<>();
                                daat.put("username", username);
                                daat.put("password", password);
                                daat.put("characterName", MyGlobalChar.getInstance().getData().getCharacterName());
                                daat.put("characterId", MyGlobalChar.getInstance().getData().getCharacterId());
                                daat.put("serverId", MyGlobalChar.getInstance().getData().getServerId());
                                daat.put("serverName", serverVal);
                                db.collection("users").add(daat);
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(SignUpActivity.this, "캐릭터 인증해주세요.", Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            Toast.makeText(SignUpActivity.this, "이미 있는 아이디입니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }
        });


    }

    public String changeServerKey(String serverVal){
        String serverKey = "";
        if(serverVal == "안톤") {serverKey = "anton";}
        else if(serverVal == "바칼") {serverKey = "bakal";}
        else if(serverVal.equals("카인")) {serverKey = "cain";}
        else if(serverVal.equals("카시야스")) {serverKey = "casillas";}
        else if(serverVal.equals("디레지에")) {serverKey = "diregie";}
        else if(serverVal.equals("힐더")) {serverKey = "hilder";}
        else if(serverVal.equals("프레이")) {serverKey = "prey";}
        else if(serverVal.equals("시로코")) {serverKey = "siroco";}
        return serverKey;
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