package saain.kokil;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    ImageView splashlogo;
    Button getStartedButton;
    Animation animation;
    CheckBox Tnc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        splashlogo=(ImageView)findViewById(R.id.SplashLogo);
        getStartedButton=(Button) findViewById(R.id.GetStartedButton);
        Tnc=(CheckBox)findViewById(R.id.TC_Box);
        animation= AnimationUtils.loadAnimation(this,R.anim.frombottom);

        splashlogo.setAnimation(animation);

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Tnc.isChecked()){
                    Intent i = new Intent(SplashActivity.this, LoginActivity2.class);
                    startActivity(i);
                }
                else {
                    Toast.makeText(getApplicationContext(),"Please Accept the T&Cs to Continue!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
