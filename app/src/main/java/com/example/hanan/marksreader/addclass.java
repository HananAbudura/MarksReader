package com.example.hanan.marksreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by hanan on 04/12/2018.
 */
public class addclass extends AppCompatActivity {

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
        setContentView(R.layout.classorcheck);
        Toolbar toolbar= (Toolbar) findViewById( R.id.toolbar);
        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
setSupportActionBar(toolbar);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menutool,menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.add: { Intent i=new Intent(this,addclass1.class);
                startActivity(i);
            break;}
            case R.id.delete:
            { Intent i=new Intent(this,deleteclass.class);
                startActivity(i);
            break;}
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void go2(View v)
    {
        Intent i=new Intent(this,choice.class);
        startActivity(i);
    }}