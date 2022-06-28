package edu.skku.map.dnf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class GunnerFemaleActivity extends AppCompatActivity {
    boolean isLogIn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gunner_female);

        ImageView _1 = (ImageView) findViewById(R.id.ylc);
        ImageView _2 = (ImageView) findViewById(R.id.ysf);
        ImageView _3 = (ImageView) findViewById(R.id.yrg);
        ImageView _4 = (ImageView) findViewById(R.id.ymc);

        _1.setOnClickListener(imgListener);
        _2.setOnClickListener(imgListener);
        _3.setOnClickListener(imgListener);
        _4.setOnClickListener(imgListener);

        ImageView goHome = (ImageView) findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(GunnerFemaleActivity.this, MainActivity.class);
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
                    Intent intent1 = new Intent(GunnerFemaleActivity.this, LoginActivity.class);
                    startActivity(intent1);
                }
            }
        });
    }

    private ImageView.OnClickListener imgListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ylc:
                    startIntent("8022a99e29b5e4845985fe1d79a233f3");
                    break;
                case R.id.ysf:
                    startIntent("64bbd751c0fcae0e74758b871a2296c1");
                    break;
                case R.id.yrg:
                    startIntent("80ec67d0356defa46a989914caca5820");
                    break;
                case R.id.ymc:
                    startIntent("15ce2751ab9c8302f39636cdb7c3dcfa");
                    break;
            }
        }
    };

    void startIntent(String jobGrowId){
        Intent intent1 = new Intent(GunnerFemaleActivity.this, SkillListActivity.class);
        intent1.putExtra("jobId", "944b9aab492c15a8474f96947ceeb9e4");
        intent1.putExtra("jobGrowId", jobGrowId);
        startActivity(intent1);
    }
}