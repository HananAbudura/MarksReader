package com.example.hanan.marksreader;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class doctororstudent extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


            if (ContextCompat.checkSelfPermission( this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE);}


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctororstudent);


    }
    public void go1(View v)
    {
        Intent i=new Intent(this,DoctorImage.class);
        startActivity(i);
    }
    public void go2(View v)
    {
       Intent i=new Intent(this,firstImage.class);
        startActivity(i);
    }
    public void finish(View v) {

            Intent i=new Intent(this,tabbedActivity.class);
            startActivity(i);

    }

}
