package com.example.hanan.marksreader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class deleteclass extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleteclass);

    }
    public void delete (View v)
    {
        EditText et=  (EditText)findViewById(R.id.cn);
        String coursname= et.getText().toString();
        EditText et1=  (EditText)findViewById(R.id.cn1);
        String classnum= et1.getText().toString();
        classnum=classnum.replaceAll("\\s+","");

        String s=coursname+classnum;
        DBHelper d=new DBHelper(this);
       // d.deleteDatat(s);
        d.deleteData();
        d.deleteData2();
        Toast.makeText(getApplicationContext(),"Class is Deleted", Toast.LENGTH_LONG).show();
    }

}
