package com.example.hanan.marksreader;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;

public class choice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
    }
    public void store(View v)
    {


        EditText et=  (EditText)findViewById(R.id.cn);
        String coursname= et.getText().toString();
        EditText et1=  (EditText)findViewById(R.id.cn1);
        String classnum= et1.getText().toString();
        classnum=classnum.replaceAll("\\s+","");
        EditText et2=  (EditText)findViewById(R.id.nq);
        String qn= et2.getText().toString();
        String s=coursname+classnum+"/"+qn;
      writeToFile(s);

        Intent i=new Intent(this,doctororstudent.class);
        startActivity(i);
    }
    private void writeToFile(String data) {

        File file = new File(Environment.getExternalStorageDirectory(),"storagefile");
        if(!file.exists()){
            if(file.mkdir()){

            }

        }

        File newfile = new File(file.getAbsolutePath()+"/choice.txt");
        try {
            if(!newfile.exists()){
                newfile.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(newfile);
            String finalres;
            finalres=data;
            outputStream.write(finalres.getBytes());
            outputStream.close();

        } catch (Exception e) {

        }


    }
}
