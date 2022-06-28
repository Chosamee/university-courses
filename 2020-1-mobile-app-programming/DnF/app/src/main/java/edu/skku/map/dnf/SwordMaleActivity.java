package edu.skku.map.dnf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SwordMaleActivity extends AppCompatActivity {

    boolean isLogIn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sword_male);

        final String jobId = "41f1cdc2ff58bb5fdc287be0db2a8df3";

        ImageView wpmaster = (ImageView) findViewById(R.id.wpmaster);
        ImageView asura = (ImageView) findViewById(R.id.asura);
        ImageView gumgwi = (ImageView) findViewById(R.id.gumgwi);
        ImageView berserker = (ImageView) findViewById(R.id.berserkser);
        ImageView soulbr = (ImageView) findViewById(R.id.soulbr);

        wpmaster.setOnClickListener(imgListener);
        asura.setOnClickListener(imgListener);
        gumgwi.setOnClickListener(imgListener);
        berserker.setOnClickListener(imgListener);
        soulbr.setOnClickListener(imgListener);

        ImageView goHome = (ImageView) findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(SwordMaleActivity.this, MainActivity.class);
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
                    Intent intent1 = new Intent(SwordMaleActivity.this, LoginActivity.class);
                    startActivity(intent1);
                }
            }
        });
    }

    private ImageView.OnClickListener imgListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.wpmaster:
                    startIntent("37495b941da3b1661bc900e68ef3b2c6");
                    break;
                case R.id.asura:
                    startIntent("c9b492038ee3ca8d27d7004cf58d59f3");
                    break;
                case R.id.berserkser:
                    startIntent("6d459bc74ba73ee4fe5cdc4655400193");
                    break;
                case R.id.soulbr:
                    startIntent("618326026de1a1f1cfba5dbd0b8396e7");
                    break;
                case R.id.gumgwi:
                    startIntent("92da05ec93fb43406e193ffb9a2a629b");
                    break;

            }
        }
    };

    void startIntent(String jobGrowId){
        Intent intent1 = new Intent(SwordMaleActivity.this, SkillListActivity.class);
        intent1.putExtra("jobId", "41f1cdc2ff58bb5fdc287be0db2a8df3");
        intent1.putExtra("jobGrowId", jobGrowId);
        startActivity(intent1);
    }
}