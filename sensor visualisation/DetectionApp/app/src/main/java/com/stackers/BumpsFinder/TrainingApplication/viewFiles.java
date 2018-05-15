package com.stackers.BumpsFinder.TrainingApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;


public class viewFiles extends  ListActivity {

    private FileHandler fileHandler;
    private String[] myStringArray;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileHandler = getIntent().getExtras().getParcelable("myFile");
        String[] myStringArray = fileHandler.getAllFiles();
        if (myStringArray == null)
        {
            myStringArray = new String[1];
            myStringArray[0] = "No Files at local device";
        }

        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(myStringArray));
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lst);
        setListAdapter(adapter);
    }
    protected void onResume() {
        super.onResume();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("fileHandler", fileHandler);
        setResult(Activity.RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Do something when a list item is clicked
        String selectedFromList = (l.getItemAtPosition(position).toString());
        final String[] selectedObject = selectedFromList.split("\n");
        String [] temp=selectedObject[0].split("File");
        final int fileNumber=Integer.valueOf(temp[1])-1;
        if(selectedObject[0]=="No Files at local device")
        {
            return;
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm");
            builder.setMessage("Are you sure you want to delete " + selectedObject[0]);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    if (fileHandler.deleteFile("File"+String.valueOf(fileNumber)+ ".txt"))
                    {
                        Toast.makeText(viewFiles.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        myStringArray=fileHandler.getAllFiles();
                        adapter.clear();
                        if (myStringArray == null) {
                            myStringArray = new String[1];
                            myStringArray[0] = "No files at local device";
                        }
                        adapter.addAll(myStringArray);
                        adapter.notifyDataSetChanged();
                        setListAdapter(adapter);
                    } else {
                        Toast.makeText(viewFiles.this, "Error Deleting File", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }


            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }


    }



}
