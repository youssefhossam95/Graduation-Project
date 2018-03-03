package com.example.youssefhossam.graphsvisualisationapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.markushi.ui.CircleButton;

public class editUserName extends AppCompatActivity {
    CircleButton saveDataButton;
    EditText userNameText;
    private Pattern pattern;
    private Matcher matcher;
    private static final String USERNAME_PATTERN = "^([A-z]+(\\s))+[A-z]*$";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_name);
        pattern = Pattern.compile(USERNAME_PATTERN);
        saveDataButton=(CircleButton)findViewById(R.id.saveDataButton);
        userNameText=(EditText)findViewById(R.id.userNameText);
        userNameText.setHint("Old name :"+getIntent().getExtras().getString("userName"));
        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(validate(userNameText.getText().toString()))
            {
                Intent intent = new Intent();
                intent.putExtra("userName",userNameText.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                Toast.makeText(getApplicationContext(), "Name updated successfully",Toast.LENGTH_SHORT).show();
                finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Invalid name ex:'Ahmed Adel'",Toast.LENGTH_SHORT).show();
            }
            }
        });
    }
    public boolean validate(final String username){

        matcher = pattern.matcher(username);
        return matcher.matches();

    }
}
