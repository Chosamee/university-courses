package edu.skku.map.dnf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FighterFemaleActivity extends AppCompatActivity {
    boolean isLogIn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fighter_female);
        ImageView _1 = (ImageView) findViewById(R.id.ynen);
        ImageView _2 = (ImageView) findViewById(R.id.ysk);
        ImageView _3 = (ImageView) findViewById(R.id.ysp);
        ImageView _4 = (ImageView) findViewById(R.id.ygp);

        _1.setOnClickListener(imgListener);
        _2.setOnClickListener(imgListener);
        _3.setOnClickListener(imgListener);
        _4.setOnClickListener(imgListener);

        ImageView goHome = (ImageView) findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(FighterFemaleActivity.this, MainActivity.class);
                startActivity(intent1);
            }
        });

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
                    Intent intent1 = new Intent(FighterFemaleActivity.this, LoginActivity.class);
                    startActivity(intent1);
                }
            }
        });
    }

    private ImageView.OnClickListener imgListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ynen:
                    startIntent("37495b941da3b1661bc900e68ef3b2c6");
                    break;
                case R.id.ysk:
                    startIntent("618326026de1a1f1cfba5dbd0b8396e7");
                    break;
                case R.id.ysp:
                    startIntent("6d459bc74ba73ee4fe5cdc4655400193");
                    break;
                case R.id.ygp:
                    startIntent("c9b492038ee3ca8d27d7004cf58d59f3");
                    break;
            }
        }
    };

    void startIntent(String jobGrowId){
        Intent intent1 = new Intent(FighterFemaleActivity.this, SkillListActivity.class);
        intent1.putExtra("jobId", "a7a059ebe9e6054c0644b40ef316d6e9");
        intent1.putExtra("jobGrowId", jobGrowId);
        startActivity(intent1);
    }
}