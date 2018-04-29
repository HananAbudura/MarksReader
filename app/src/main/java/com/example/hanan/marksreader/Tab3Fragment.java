package com.example.hanan.marksreader;

/**
 * Created by hanan on 03/30/2018.
 */
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class Tab3Fragment extends Fragment {
    private static final String TAG = "Tab3Fragment";

    private ListView m_listview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab3_layout,container,false);
        m_listview = (ListView) view.findViewById(R.id.listView);
        int max=-1;
        int min=100000;
        String maxid="";
        String minid="";
        ArrayList<person> peopleList = new ArrayList<>();
        person p = new person("Mark","ID");
        peopleList.add(p);
        int numofcircles=0;
        int colss=0;
        String doctor_page=readfile();
        String[] s=  doctor_page.split("/");

        numofcircles= Integer.parseInt(s[0].trim());
        colss=numofcircles;
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

            for(int i=0;i<numofcircles;i++)
            {  if(circles[i].equals(circles2[i]))
                result[i]=true;
                if(!circles[i].equals(circles2[i]))
                    result[i]=false;

            }
            for(int i=0;i<numofcircles;i++)
            {
                if(result[i]==true)
                    count++;
            }
            int marksfromhw= Integer.parseInt(stu2[0]);
            finalmark=mark*count+marksfromhw;

          if(finalmark>max)
          {
              max=finalmark;
              maxid=id;

          }
            if(finalmark<min)
            {
                min=finalmark;
                minid=id;
            }
        }

        person p2 = new person(""+max,"Max"+"   "+maxid);
        peopleList.add(p2);
        person p3 = new person(""+min,"Min"+"   "+minid);
        peopleList.add(p3);
        PersonListAdapter adapter = new PersonListAdapter(view.getContext(),
                R.layout.adapter_view_layout ,peopleList );
        m_listview.setAdapter(adapter);

        return view;
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
        return str;}}