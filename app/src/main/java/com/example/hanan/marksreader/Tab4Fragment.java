package com.example.hanan.marksreader;

/**
 * Created by hanan on 03/30/2018.
 */
import android.database.Cursor;
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


public class Tab4Fragment extends Fragment {
    private static final String TAG = "Tab4Fragment";

    private ListView m_listview;
private String[] allnames=new String[1000];
    private String[] namesnota=new String[1000];
private int num=0;
private int num2=0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab4,container,false);
        m_listview = (ListView) view.findViewById(R.id.listView);
        DBHelper dbHelper = new DBHelper(this.getContext());
        int max=-1;
        int min=100000;
        String maxid="";
        String minid="";
        ArrayList<person> peopleList = new ArrayList<>();
        person p = new person("Name","ID");
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
            String circles2[]=new String[numofcircles];//the answer of student
            String[] s22=  stu2[2].split("_");

            for(int i=0;i<numofcircles;i++)
            {
                circles2[i]=s22[2*i+1];
            }
            for(int i=0;i<numofcircles;i++)
            {
                full[i]= circles2[i];
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
            String studentname="";
            String cn="";

            String choi=readfile3();
            String[] ss=  choi.split("/");
            cn=ss[0];
            final Cursor cursor5 = dbHelper.getuser3(cn);
            if (cursor5.moveToFirst()) {
                do {
                    String Student_ID = cursor5.getString(cursor5.getColumnIndex("Student_ID"));
                    String Student_Name = cursor5.getString(cursor5.getColumnIndex("Student_Name"));

                //    Toast.makeText(this.getContext(),Student_ID+Student_Name,Toast.LENGTH_LONG).show();
                    String temp=id;
               //     id= id.replace("0","٠").replace("1","١").replace("2","٢").replace("3","٣").replace("4","٤").replace("5","٥").replace("6","٦").replace("7","٧").replace("8","٨").replace("9","٩");

                    if(id.equals(Student_ID))
                    {
                        studentname=Student_Name;
                        namesnota[num2]=studentname;
                        num2++;
                    }

                    id=temp;
                } while (cursor5.moveToNext());
            }
            //closing cursor
            cursor5.close();


        }

        String choi=readfile3();
        String[] ss=  choi.split("/");
      String cn=ss[0];
        final Cursor cursor5 = dbHelper.getuser3(cn);
        if (cursor5.moveToFirst()) {
            do {
                String Student_ID = cursor5.getString(cursor5.getColumnIndex("Student_ID"));
                String Student_Name = cursor5.getString(cursor5.getColumnIndex("Student_Name"));

                allnames[num]=Student_Name;
                num++;
            } while (cursor5.moveToNext());
        }
        cursor5.close();
        boolean [] aornot=new boolean[num];
        int absents=num-num2;
        String[] abs=new String[absents];
        for(int jj=0;jj<num;jj++)
        {

                aornot[jj]=true;

        }

        for(int i=0;i<num;i++)
            for(int jj=0;jj<num2;jj++)
            {
                if(allnames[i].equals(namesnota[jj]))
                {
                    aornot[i]=false;
                }
            }
            int h=0;
        for(int jj=0;jj<num;jj++)
        {
            if(aornot[jj])
            {
                abs[h]=allnames[jj];
            }
        }
        for(int jj=0;jj<absents;jj++)
        {      String id2="";
            String choi2=readfile3();
            String[] ss2=  choi2.split("/");
            String cn2=ss2[0];
            final Cursor cursor52 = dbHelper.getuser3(cn2);
            if (cursor52.moveToFirst()) {
                do {
                    String Student_ID = cursor52.getString(cursor52.getColumnIndex("Student_ID"));
                    String Student_Name = cursor52.getString(cursor52.getColumnIndex("Student_Name"));
                    if(abs[jj].equals(Student_Name)){
    id2=Student_ID;
}
                } while (cursor52.moveToNext());
            }
            cursor52.close();
        person p2 = new person(abs[jj],id2);
        peopleList.add(p2);
       }

        PersonListAdapter adapter = new PersonListAdapter(view.getContext(),
                R.layout.adapter_view_layout ,peopleList );
        m_listview.setAdapter(adapter);
num=0;
num2=0;

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
        return str;}
    public String readfile3()
    { String str="";
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/storagefile/choice.txt");
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