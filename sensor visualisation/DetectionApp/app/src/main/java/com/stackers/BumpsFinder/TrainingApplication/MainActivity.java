package com.stackers.BumpsFinder.TrainingApplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.markushi.ui.CircleButton;

public class MainActivity extends AppCompatActivity {
    CircleButton loginButton;
    String fullName="";
    FileHandler myFile;
    ContextHolder contextHolder;
    private Pattern pattern;
    private Matcher matcher;
    private static final String USERNAME_PATTERN = "^([A-z]+(\\s))+[A-z]*$";
    EditText userName;
    EditText enrollmentKeyText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextHolder.setContext(getApplicationContext());
        myFile=FileHandler.getFileHandlerObject(this);
        String temp=myFile.readSingleFile("userInfo");
        if(temp!=null)
        {
            displayExceptionMessage("Welcome back "+temp);
            fullName=temp;
            Intent myIntent = new Intent(getApplicationContext(), SimpleActivity.class);
            myIntent.putExtra("username", fullName);
            startActivity(myIntent);
        }
        setContentView(R.layout.activity_main);
        userName=(EditText)findViewById(R.id.userNameText);
        pattern = Pattern.compile(USERNAME_PATTERN);
        enrollmentKeyText=(EditText)findViewById(R.id.enrollmentkeyText);
        loginButton=(CircleButton)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkUserInformation())
                {
                    Toast V=Toast.makeText(MainActivity.this,"Logged in successfully Mr."+fullName,Toast.LENGTH_SHORT);
                    V.show();
                    Intent myIntent = new Intent(getApplicationContext(), SimpleActivity.class);
                    myIntent.putExtra("username", fullName);
                    startActivity(myIntent);
                }

            }
        });
    }
    public boolean validate(final String username){

        matcher = pattern.matcher(username);
        return matcher.matches();

    }
    boolean checkUserInformation()
    {
        if(validate(userName.getText().toString()))
        {
            fullName=userName.getText().toString();
        }
        else
        {
            displayExceptionMessage("Please enter valid Full name");
            return false;
        }
        int temp=Integer.valueOf(enrollmentKeyText.getText().toString());
        if(temp==1995)
        {
            return true;
        }
        else
        {
            displayExceptionMessage("Please enter valid Enrollment Key");
            return false;
        }

    }
    protected void onResume() {
        super.onResume();
    }
    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
    }
    protected void onDestroy(){
        super.onDestroy();
    }
    public void displayExceptionMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}




