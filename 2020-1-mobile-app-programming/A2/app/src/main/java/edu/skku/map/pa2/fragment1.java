package edu.skku.map.pa2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;


public class fragment1 extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public fragment1() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_fragment1, container, false);
        afterfunc afterfuncfunc = new afterfunc() {
            @Override
            public void afterfuncfunc(ArrayList<MyItem> items) {
                Context context = view.getContext();
                RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycle_public_view);
                recyclerView.setHasFixedSize(true);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(layoutManager);


                RecyclerAdapter adapter = new RecyclerAdapter(context, items);
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
        Query query = postRef.whereEqualTo("ispersonal", 0);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        postdata.add(document.getData());
                        MyItem item = new MyItem();
                        Log.d("frag1", document.getId() + " => " + document.getData());
                        item.setusername((String)document.getData().get("username"));
                        item.settags((String)document.getData().get("tags"));
                        item.setcontents((String)document.getData().get("contents"));
                        item.setPicLink((String)document.getData().get("pic_link"));
                        Log.d("seree", (String)document.getData().get("contents"));
                        items.add(item);
                    }
                    afterfuncfunc.afterfuncfunc(items);
                } else {
                    Log.w("frag1", "Error adding document");
                }
            }
        });

    }
}

interface afterfunc{
    void afterfuncfunc(ArrayList<MyItem> items);
}