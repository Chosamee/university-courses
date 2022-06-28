package edu.skku.map.dnf;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class fragment0 extends Fragment {
    JsonObject jsonObject;
    String jsonData;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String apiKey = "SyPGiVveXthbRZsfez4hi3uPmmzSnv7g";

    public fragment0() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_fragment0, container, false);
        LinearLayout swordMale = (LinearLayout) view.findViewById(R.id.swordMale);
        LinearLayout swordFemale = (LinearLayout) view.findViewById(R.id.swordFemale);
        LinearLayout fighterMale = (LinearLayout) view.findViewById(R.id.fighterMale);
        LinearLayout fighterFemale = (LinearLayout) view.findViewById(R.id.fighterFeMale);
        LinearLayout gunnerMale = (LinearLayout) view.findViewById(R.id.gunnerMale);
        LinearLayout gunnerFemale = (LinearLayout) view.findViewById(R.id.gunnerFemale);

        swordMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SwordMaleActivity.class);
                intent.putExtra("jobId", "41f1cdc2ff58bb5fdc287be0db2a8df3");
                startActivity(intent);
            }
        });
        swordFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SwordFemaleActivity.class);
                intent.putExtra("jobId", "1645c45aabb008c98406b3a16447040d");
                startActivity(intent);
            }
        });
        fighterMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FighterMaleActivity.class);
                intent.putExtra("jobId", "ca0f0e0e9e1d55b5f9955b03d9dd213c");
                startActivity(intent);
            }
        });
        fighterFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FighterFemaleActivity.class);
                intent.putExtra("jobId", "a7a059ebe9e6054c0644b40ef316d6e9");
                startActivity(intent);
            }
        });
        gunnerMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GunnerMaleActivity.class);
                intent.putExtra("jobId", "afdf3b989339de478e85b614d274d1ef");
                startActivity(intent);
            }
        });
        gunnerFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GunnerFemaleActivity.class);
                intent.putExtra("jobId", "944b9aab492c15a8474f96947ceeb9e4");
                startActivity(intent);
            }
        });



        final EditText characterNameET = (EditText) view.findViewById(R.id.characterNameET);
        final EditText serverET = (EditText) view.findViewById(R.id.serverET);
        Button search = (Button) view.findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String characterName = characterNameET.getText().toString();
                final String serverVal = serverET.getText().toString();
                final callBack callBackFunc = new callBack() {
                    @Override
                    public void callBackFunc(ArrayList<CharacterInfo> infoList) {
                        Context context = view.getContext();
                        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_char);
                        recyclerView.setHasFixedSize(true);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                        recyclerView.setLayoutManager(layoutManager);
                        RecyclerAdapterChar adapter = new RecyclerAdapterChar(context, infoList);
                        recyclerView.setAdapter(adapter);

                    }
                };
                setData(characterName, serverVal, callBackFunc);
            }
        });

        return view;
    }

    public void setData(String characterName, String serverVal, callBack callBackFunc){
        final ArrayList<CharacterInfo> infoList = new ArrayList<>();

        String serverKey;
        serverKey = changeServerKey(serverVal);
        final String targetUrl = "https://api.neople.co.kr/df/servers/" + serverKey + "/characters?characterName=" + characterName + "&wordType=full"
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
            for(int i =0; i< jarray.size(); i++){
                JsonElement jsonElement = jarray.get(i);
                final CharacterInfo characterInfo = new CharacterInfo();
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
                Thread mThread2 = new Thread() {
                    @Override
                    public void run() {
                    try {
                        String serverKey = characterInfo.getServerId();
                        String characterId = characterInfo.getCharacterId();
                        URL url = new URL("https://img-api.neople.co.kr/df/servers/" + serverKey + "/characters/" + characterId);
                        Log.d("asdaa", url.toString());
                        HttpURLConnection connect = (HttpURLConnection) url.openConnection();
                        connect.connect();
                        BufferedInputStream bis = new BufferedInputStream(connect.getInputStream());
                        Bitmap bm = BitmapFactory.decodeStream(bis);
                        bis.close();
                        characterInfo.setCharacterPic(bm);

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
                    infoList.add(characterInfo);
                    callBackFunc.callBackFunc(infoList);
                }
                catch (InterruptedException e){
                    Toast.makeText(getActivity(), "error", Toast.LENGTH_LONG).show();
                }
                catch (Exception e){

                }
            }
        } catch (InterruptedException e) {
            Toast.makeText(getActivity(), "error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        catch (Exception e){
            Toast.makeText(getActivity(), "올바른 캐릭터명을 입력하세요.", Toast.LENGTH_LONG).show();
        }


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

interface callBack{
    void callBackFunc(ArrayList<CharacterInfo> infoList);
}