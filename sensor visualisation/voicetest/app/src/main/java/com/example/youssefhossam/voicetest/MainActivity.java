package com.example.youssefhossam.voicetest;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text=(TextView)findViewById(R.id.TextView);
    }
    public void onClick(View v)
    {
        promptSpeechInput();
    }
        private void promptSpeechInput(){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            try {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ar");
                startActivityForResult(intent,100);
            } catch (Exception e) {
                displayExceptionMessage(e.getMessage());
            }
        }

        /**
         * Receiving speech input
         * */
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            switch (requestCode) {
                case 100: {
                    if (resultCode == RESULT_OK && null != data) {

                        ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        String s="";
                        for(String str:result)
                            s+=str+=", ";
                        text.setText(s);
                    }
                    break;
                }

            }
        }
    public void displayExceptionMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


}
