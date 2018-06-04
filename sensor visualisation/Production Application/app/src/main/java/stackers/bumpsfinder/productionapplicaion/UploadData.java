package stackers.bumpsfinder.productionapplicaion;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class UploadData extends AppCompatActivity {
    TextView bumpsNumberTextView;
    FileHandler myFilerHandler;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_layout);
        myFilerHandler = FileHandler.getFileHandler();
        bumpsNumberTextView = (TextView)findViewById(R.id.detectedBumpsTextView);
        bumpsNumberTextView.setText("Detected Bumps = "+myFilerHandler.getNumberOfDefects());
    }
    public void uploadLocalData(View V){
        myFilerHandler.uploadLocalData();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (FileHandler.isCurrentlyUploading) {
                    bumpsNumberTextView.setText("Detected Bumps = "+myFilerHandler.getNumberOfDefects());
                }
            }
        });
    }
}
