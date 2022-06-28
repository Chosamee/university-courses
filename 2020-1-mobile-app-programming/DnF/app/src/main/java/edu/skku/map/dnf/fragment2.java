package edu.skku.map.dnf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
import java.util.Map;


public class fragment2 extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    boolean check = true;
    JsonObject jsonObject;
    String jsonData;
    String apiKey = "SyPGiVveXthbRZsfez4hi3uPmmzSnv7g";

    public fragment2() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_fragment2, container, false);

        try{
            String username = MyGlobals.getInstance().getData().getUsername();
        }
        catch (Exception e){
            check = false;
        }

        if (check){
            TextView characterName = (TextView) view.findViewById(R.id.characterName);
            TextView username = (TextView) view.findViewById(R.id.username);
            TextView server = (TextView) view.findViewById(R.id.server);
            ImageView characterImg = (ImageView) view.findViewById(R.id.characterImg) ;
            characterName.setText(MyGlobals.getInstance().getData().getCharacterName());
            username.setText(MyGlobals.getInstance().getData().getUsername());
            server.setText(MyGlobals.getInstance().getData().getServerName());

            Thread mThread = new Thread() {
                @Override
                public void run() {
                    try {
                        String serverKey = MyGlobals.getInstance().getData().getServerId();
                        String characterId = MyGlobals.getInstance().getData().getCharacterId();
                        URL url = new URL("https://img-api.neople.co.kr/df/servers/" + serverKey + "/characters/" + characterId);
                        Log.d("aaaaaaa", url.toString());
                        HttpURLConnection connect = (HttpURLConnection) url.openConnection();
                        connect.connect();
                        BufferedInputStream bis = new BufferedInputStream(connect.getInputStream());
                        Bitmap bm = BitmapFactory.decodeStream(bis);
                        bis.close();
                        MyGlobals.getInstance().getData().setCharacterPic(bm);
                    } catch (MalformedURLException e) {
                    } catch (IOException e) {
                    }
                    catch (Exception e){
                        Log.d("TAG", "NANI?");
                    }
                }
            };
            mThread.start();

            try {
                mThread.join();
                characterImg.setImageBitmap(MyGlobals.getInstance().getData().getCharacterPic());
            }
            catch (InterruptedException e){
            }
            catch (Exception e){

                Log.d("TAG", "NANI?");
            }
        }

        afterfunc afterfuncfunc = new afterfunc() {
            @Override
            public void afterfuncfunc(ArrayList<MyItem> items) {
                Context context = view.getContext();
                RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycle_personal_view);
                recyclerView.setHasFixedSize(true);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(layoutManager);


                RecyclerAdapter2 adapter = new RecyclerAdapter2(context, items);
                recyclerView.setAdapter(adapter);

            }
        };
        setData(afterfuncfunc);
        return view;
    }

    private void setData(final afterfunc afterfuncfunc){
        final ArrayList<MyItem> items = new ArrayList<>();
        final ArrayList<Map<String, Object>> postdata = new ArrayList<>();
        CollectionReference postRef = db.collection("posts");
        try{
            String username = MyGlobals.getInstance().getData().getUsername();
        }
        catch (Exception e){
            check = false;
        }
        if(check){
            Query query = postRef.whereEqualTo("username", MyGlobals.getInstance().getData().getUsername());
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            postdata.add(document.getData());
                            MyItem item = new MyItem();
                            Log.d("frag2", document.getId() + " => " + document.getData());
                            item.setUsername((String)document.getData().get("username"));
                            item.setTitle((String)document.getData().get("title"));
                            item.setContents((String)document.getData().get("contents"));
                            item.setPicLink((String)document.getData().get("pic_link"));
                            item.setCharacterName((String)document.getData().get("characterName"));
                            item.setServerName((String) document.getData().get("serverName"));
                            items.add(item);
                        }
                        afterfuncfunc.afterfuncfunc(items);
                    } else {
                        Log.w("frag2", "Error adding document");
                    }
                }
            });
        }
        else{
            Toast.makeText(getActivity(), "로그인 하세요", Toast.LENGTH_LONG).show();
        }

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