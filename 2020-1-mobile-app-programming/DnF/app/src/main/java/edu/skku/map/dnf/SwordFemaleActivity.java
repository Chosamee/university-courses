package edu.skku.map.dnf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SwordFemaleActivity extends AppCompatActivity {
    boolean isLogIn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String jobId = "1645c45aabb008c98406b3a16447040d";

        setContentView(R.layout.activity_sword_female);

        ImageView sdmaster = (ImageView) findViewById(R.id.sdmaster);
        ImageView gumma = (ImageView) findViewById(R.id.gumma);
        ImageView gumje = (ImageView) findViewById(R.id.gumje);
        ImageView darktem = (ImageView) findViewById(R.id.darktem);

        sdmaster.setOnClickListener(imgListener);
        gumma.setOnClickListener(imgListener);
        gumje.setOnClickListener(imgListener);
        darktem.setOnClickListener(imgListener);

        ImageView goHome = (ImageView) findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(SwordFemaleActivity.this, MainActivity.class);
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
                    Intent intent1 = new Intent(SwordFemaleActivity.this, LoginActivity.class);
                    startActivity(intent1);
                }
            }
        });
    }

    private ImageView.OnClickListener imgListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sdmaster:
                    startIntent("37495b941da3b1661bc900e68ef3b2c6");
                    break;
                case R.id.gumma:
                    startIntent("6d459bc74ba73ee4fe5cdc4655400193");
                    break;
                case R.id.gumje:
                    startIntent("c9b492038ee3ca8d27d7004cf58d59f3");
                    break;
                case R.id.darktem:
                    startIntent("618326026de1a1f1cfba5dbd0b8396e7");
                    break;

            }
        }
    };

    void startIntent(String jobGrowId){
        Intent intent1 = new Intent(SwordFemaleActivity.this, SkillListActivity.class);
        intent1.putExtra("jobId", "1645c45aabb008c98406b3a16447040d");
        intent1.putExtra("jobGrowId", jobGrowId);
        startActivity(intent1);
    }
}