package com.example.hanan.marksreader;

/**
 * Created by hanan on 03/30/2018.
 */
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class Tab1Fragment extends Fragment {
    private static final String TAG = "Tab1Fragment";
    BarChart barChart;
    ArrayList<String> dates;
    Random random;
    ArrayList<BarEntry> barEntries;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1_fragment,container,false);
        barChart = (BarChart) view.findViewById(R.id.bargraph);

        createRandomBarGraph("2016/05/05", "2016/06/01");
        return view;
    }
    public void createRandomBarGraph(String Date1, String Date2){

        int numofcircles=0;

        String doctor_page=readfile();
        String[] s=  doctor_page.split("/");

        numofcircles= Integer.parseInt(s[0].trim());
        int fin[]=new int [numofcircles];
        String circles[]=new String[numofcircles];
        String[] s2=  s[1].split("_");
        int j=0;int m=0;
        int a[]=new int[numofcircles];
        for(int i=0;i<numofcircles;i++)
        {
            a[i]= Integer.parseInt(s2[2*i].trim());
        }
        for(int i=0;i<numofcircles;i++)
        {
            circles[i]=s2[2*i+1];
        }

        int mark= Integer.parseInt(s[2].trim());
        //Toast.makeText(getApplication(), ""+circles[0]+circles[1], Toast.LENGTH_SHORT).show();

        String stu_page=readfile2();
        String[] stu=  stu_page.split(",");
        for(int b=0;b<stu.length;b++)
        {
            String full[]=new String[50];
            for(int i=0;i<50;i++)
                full[i]=" ";
            String[] stu2= stu[b].split("/");
            int studennum= Integer.parseInt(stu2[1]);
            String circles2[]=new String[studennum];//the answer of student

            int rest=studennum-numofcircles;
            String[] s22=  stu2[2].split("_");
            int marksfromhw= Integer.parseInt(stu2[0]);
            String[]replace=new String[rest];// for replacment the circle

            for(int i=0;i<studennum;i++)
            {
                circles2[i]=s22[2*i+1];
            }
            for(int i=0;i<rest;i++)
            {
                replace[i]=circles2[numofcircles+i];

            }
            int y=0;
            for(int i=0;i<numofcircles;i++)
            {
                full[i]= circles2[i];
                if(full[i].equals("r"))
                {
                    full[i]=replace[y] ;
                    circles2[i]=replace[y];

                    y++;
                }
            }
            String id=stu2[3];
            boolean [] result=new boolean[numofcircles];
            int count=0;
            int finalmark=0;

            // Toast.makeText(getApplication(), ""+circles2[0]+circles2[1], Toast.LENGTH_SHORT).show();
            for(int i=0;i<numofcircles;i++)
            {  if(circles[i].equals(circles2[i]))
                result[i]=true;
                if(!circles[i].equals(circles2[i]))
                    result[i]=false;


            }
            for(int i=0;i<numofcircles;i++)
            {
                if(result[i]==true)
                    fin[i]++;
            }}


            barEntries = new ArrayList<>();

            for(int c= 0; c< numofcircles;c++){

                barEntries.add(new BarEntry(fin[c],c));
            }
        ArrayList<String> list = new ArrayList<String>();
        int y=0;
        for(int i=0;i<numofcircles;i++)
        {   y=i+1;
            list.add(""+y);
        }

        BarDataSet barDataSet = new BarDataSet(barEntries,"The number of true answers for each question");

      barDataSet.setColor(Color.rgb(75,140,97));
        BarData barData = new BarData(list,barDataSet);

        barChart.setData(barData);
        barChart.setDescription("Bar Graph!");


    }


    public String readfile()
    { String str="";
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/storagefile/testfile.txt");
        if(file != null && file.exists()){

            try {
                FileInputStream fis = new FileInputStream(file);
                str = "";
                int data;
                while((data = fis.read()) != -1){
                    str += (char)data;
                }

            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }

        }
        return str;}
    public String readfile2()
    { String str="";
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/storagefile/student.txt");
        if(file != null && file.exists()){

            try {
                FileInputStream fis = new FileInputStream(file);
                str = "";
                int data;
                while((data = fis.read()) != -1){
                    str += (char)data;
                }

            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }

        }
        return str;}
}