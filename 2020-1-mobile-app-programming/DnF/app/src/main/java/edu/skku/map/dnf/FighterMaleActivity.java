package edu.skku.map.dnf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FighterMaleActivity extends AppCompatActivity {
    boolean isLogIn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fighter_male);

        ImageView _1 = (ImageView) findViewById(R.id.namnen);
        ImageView _2 = (ImageView) findViewById(R.id.namsk);
        ImageView _3 = (ImageView) findViewById(R.id.namsp);
        ImageView _4 = (ImageView) findViewById(R.id.namgp);

        _1.setOnClickListener(imgListener);
        _2.setOnClickListener(imgListener);
        _3.setOnClickListener(imgListener);
        _4.setOnClickListener(imgListener);

        ImageView goHome = (ImageView) findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(FighterMaleActivity.this, MainActivity.class);
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
                    Intent intent1 = new Intent(FighterMaleActivity.this, LoginActivity.class);
                    startActivity(intent1);
                }
            }
        });
    }

    private ImageView.OnClickListener imgListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.namnen:
                    startIntent("37495b941da3b1661bc900e68ef3b2c6");
                    break;
                case R.id.namsk:
                    startIntent("618326026de1a1f1cfba5dbd0b8396e7");
                    break;
                case R.id.namsp:
                    startIntent("6d459bc74ba73ee4fe5cdc4655400193");
                    break;
                case R.id.namgp:
                    startIntent("c9b492038ee3ca8d27d7004cf58d59f3");
                    break;
            }
        }
    };

    void startIntent(String jobGrowId){
        Intent intent1 = new Intent(FighterMaleActivity.this, SkillListActivity.class);
        intent1.putExtra("jobId", "ca0f0e0e9e1d55b5f9955b03d9dd213c");
        intent1.putExtra("jobGrowId", jobGrowId);
        startActivity(intent1);
    }
}