package com.example.hanan.marksreader;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.todobom.opennotescanner.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;


/**
 * Called when the activity will start interacting with the user.
 * At this point your activity is at the top of the activity stack,
 * with user input going to it.
 */

public class tabbedActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {
private static final int MY_PERMISSIONS_REQUEST_WRITE = 3;

private static final int REQUEST_CODE_RESOLUTION = 1;
private static final  int REQUEST_CODE_OPENER = 2;
private GoogleApiClient mGoogleApiClient;
private int fileOperation = 0;
private DriveId mFileId;
public DriveFile file;
private String filname;
    private String name1;
    private static final String TAG = "MainActivity";

    private SectionsPageAdapter mSectionsPageAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);
        Log.d(TAG, "onCreate: Starting.");
        if (ContextCompat.checkSelfPermission( this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE);}


        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menutool2,menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.up1: {   fileOperation = 0;

                // create new contents resource
                Drive.DriveApi.newDriveContents(mGoogleApiClient)
                        .setResultCallback(driveContentsCallback);

                break;}
            case R.id.up2:
            {  fileOperation = 1;

                // create new contents resource
                Drive.DriveApi.newDriveContents(mGoogleApiClient)
                        .setResultCallback(driveContentsCallback);
                break;}
            case R.id.up3:

            {
                try {
                    fileOperation = 2;

                    // create new contents resource
                    Drive.DriveApi.newDriveContents(mGoogleApiClient)
                            .setResultCallback(driveContentsCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;}
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new Tab1Fragment(), "Bar Graph");
        adapter.addFragment(new Tab2Fragment(), "ID&Mark");
        adapter.addFragment(new Tab3Fragment(), "Max&Min");
        adapter.addFragment(new Tab4Fragment(), "Absent Students");
        viewPager.setAdapter(adapter);
    }
public void upload1(DriveApi.DriveContentsResult result){
    final DriveContents driveContents = result.getDriveContents();

    // Perform I/O off the UI thread.
    new Thread() {
        @Override
        public void run() {
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
    //Toast.makeText(getApplication(), ""+circles[0]+circles[1], Toast.LENGTH_SHORT).show();
    DBHelper dbHelper = new DBHelper(getApplication());
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

        boolean [] result2=new boolean[numofcircles];
        int count=0;
        int finalmark=0;

        // Toast.makeText(getApplication(), ""+circles2[0]+circles2[1], Toast.LENGTH_SHORT).show();
        for(int i=0;i<numofcircles;i++)
        {  if(circles[i].equals(circles2[i]))
            result2[i]=true;
            if(!circles[i].equals(circles2[i]))
                result2[i]=false;


        }
        for(int i=0;i<numofcircles;i++)
        {
            if(result2[i]==true)
                count++;
        }
        finalmark=mark*count+marksfromhw;
        String studentname="";
        String cn="";
        String choi=readfile3();
        String[] ss=  choi.split("/");
        cn=ss[0];
        name1=cn;

        Cursor cursor5 = dbHelper.getuser3(cn);
        if (cursor5.moveToFirst()) {
            do {
                String Student_ID = cursor5.getString(cursor5.getColumnIndex("Student_ID"));
                String Student_Name = cursor5.getString(cursor5.getColumnIndex("Student_Name"));
                //     Toast.makeText(getApplicationContext(),Student_ID+Student_Name,Toast.LENGTH_LONG).show();
                String temp=id;
           //     id= id.replace("0","٠").replace("1","١").replace("2","٢").replace("3","٣").replace("4","٤").replace("5","٥").replace("6","٦").replace("7","٧").replace("8","٨").replace("9","٩");

                if(id.equals(Student_ID))
                {
                    studentname=Student_Name;
                }
                id=temp;
            } while (cursor5.moveToNext());
        }
        //closing cursor
        cursor5.close();
        dbHelper.insertData(id,finalmark,studentname);
    }




    final Cursor cursor = dbHelper.getuser();

    File sd = Environment.getExternalStorageDirectory();
    String csvFile = "MarksData.xls";

    File directory = new File(sd.getAbsolutePath());
    //create directory if not exist
    if (!directory.isDirectory()) {
        directory.mkdirs();
    }
    try {

        //file path
        File file = new File(directory, csvFile);
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;
        workbook = Workbook.createWorkbook(file, wbSettings);
        //Excel sheet name. 0 represents first sheet

        WritableSheet sheet = workbook.createSheet("userList", 0);

        sheet.addCell(new Label(0, 0, "Student_ID")); // column and row

        sheet.addCell(new Label(1, 0, "Student_Name"));
        sheet.addCell(new Label(2, 0, "Student_Mark"));
        if (cursor.moveToFirst()) {
            do {
                String Student_ID = cursor.getString(cursor.getColumnIndex("Student_ID"));
                String Student_Name = cursor.getString(cursor.getColumnIndex("Student_Name"));
                String Student_Mark = cursor.getString(cursor.getColumnIndex("Student_Mark"));
                int i = cursor.getPosition() + 1;
                sheet.addCell(new Label(0, i, Student_ID));
                sheet.addCell(new Label(1, i, Student_Name));
                sheet.addCell(new Label(2, i, Student_Mark));

            } while (cursor.moveToNext());
        }
        //closing cursor
        cursor.close();
        workbook.write();
        workbook.close();
        // Toast.makeText(getApplication(), "Data Exported in two Excel Sheets", Toast.LENGTH_SHORT).show();
        dbHelper.deleteData();



        dbHelper.deleteData2();


    FileInputStream is = null;
    int read = 0;
    byte[] bytes = new byte[1024];
    try {
        is =
                new FileInputStream(file);
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    OutputStream outputStream = driveContents.getOutputStream();
    Writer writer = new OutputStreamWriter(outputStream);
    ByteArrayOutputStream byt = new ByteArrayOutputStream();
    try {


        try {
            while ((read = is.read(bytes)) != -1) {
                assert outputStream != null;

                byt.write(bytes, 0, read);
            }
            outputStream.write(byt.toByteArray());

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

}
            catch (Exception e) {
        e.printStackTrace();
    }
    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
            .setTitle(""+name1+"_1")
            .setMimeType("application/vnd.ms-excel")
            .setStarred(true).build();

    // create a file in root folder
                Drive.DriveApi.getRootFolder(mGoogleApiClient)
            .createFile(mGoogleApiClient, changeSet, driveContents)
                        .setResultCallback(fileCallback);
    //
}}.start();}

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
        public void upload2(DriveApi.DriveContentsResult result)
        {  final DriveContents driveContents = result.getDriveContents();

            // Perform I/O off the UI thread.
            new Thread() {
                @Override
                public void run() {
                    // write content to DriveContents


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
            //Toast.makeText(getApplication(), ""+circles[0]+circles[1], Toast.LENGTH_SHORT).show();
            DBHelper dbHelper = new DBHelper(getApplication());
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
                dbHelper.insertData2(id,full[0],full[1],full[2],full[3],full[4],full[5],full[6],full[7],full[8],full[9],full[10],full[11],full[12],full[13],full[14],full[15],full[16],full[17],full[18],full[19],full[20],full[21],full[22],full[23],full[24],full[25],full[26],full[27],full[28],full[29],full[30],full[31],full[32],full[33],full[34],full[35],full[36],full[37],full[38],full[39],full[40],full[41],full[42],full[43],full[44],full[45],full[46],full[47],full[48],full[49]);


            }




            final Cursor cursor2 = dbHelper.getuser2();
            File sd = Environment.getExternalStorageDirectory();

            String csvFile2 = "MarksData2.xls";
            File directory = new File(sd.getAbsolutePath());
            //create directory if not exist
            if (!directory.isDirectory()) {
                directory.mkdirs();
            }
            try {


                File file2 = new File(directory, csvFile2);
                WorkbookSettings wbSettings2 = new WorkbookSettings();
                wbSettings2.setLocale(new Locale("en", "EN"));
                WritableWorkbook workbook2;
                workbook2 = Workbook.createWorkbook(file2, wbSettings2);
                //Excel sheet name. 0 represents first sheet

                WritableSheet sheet2 = workbook2.createSheet("userList", 0);
                sheet2.addCell(new Label(0, 0, "Student_ID")); // column and row
                for (int h = 1; h <= colss; h++) {
                    sheet2.addCell(new Label(h, 0, "" + h));
                }


                if (cursor2.moveToFirst()) {
                    do {
                        String Student_ID = cursor2.getString(cursor2.getColumnIndex("Student_ID"));
                        String a1 = cursor2.getString(cursor2.getColumnIndex("a1"));
                        String a2 = cursor2.getString(cursor2.getColumnIndex("a2"));
                        String a3 = cursor2.getString(cursor2.getColumnIndex("a3"));
                        String a4 = cursor2.getString(cursor2.getColumnIndex("a4"));
                        String a5 = cursor2.getString(cursor2.getColumnIndex("a5"));
                        String a6 = cursor2.getString(cursor2.getColumnIndex("a6"));
                        String a7 = cursor2.getString(cursor2.getColumnIndex("a7"));
                        String a8 = cursor2.getString(cursor2.getColumnIndex("a8"));
                        String a9 = cursor2.getString(cursor2.getColumnIndex("a9"));
                        String a10 = cursor2.getString(cursor2.getColumnIndex("a10"));
                        String a11 = cursor2.getString(cursor2.getColumnIndex("a11"));
                        String a12 = cursor2.getString(cursor2.getColumnIndex("a12"));
                        String a13 = cursor2.getString(cursor2.getColumnIndex("a13"));
                        String a14 = cursor2.getString(cursor2.getColumnIndex("a14"));
                        String a15 = cursor2.getString(cursor2.getColumnIndex("a15"));
                        String a16 = cursor2.getString(cursor2.getColumnIndex("a16"));
                        String a17 = cursor2.getString(cursor2.getColumnIndex("a17"));
                        String a18 = cursor2.getString(cursor2.getColumnIndex("a18"));
                        String a19 = cursor2.getString(cursor2.getColumnIndex("a19"));
                        String a20 = cursor2.getString(cursor2.getColumnIndex("a20"));
                        String a21 = cursor2.getString(cursor2.getColumnIndex("a21"));
                        String a22 = cursor2.getString(cursor2.getColumnIndex("a22"));
                        String a23 = cursor2.getString(cursor2.getColumnIndex("a23"));
                        String a24 = cursor2.getString(cursor2.getColumnIndex("a24"));
                        String a25 = cursor2.getString(cursor2.getColumnIndex("a25"));
                        String a26 = cursor2.getString(cursor2.getColumnIndex("a26"));
                        String a27 = cursor2.getString(cursor2.getColumnIndex("a27"));
                        String a28 = cursor2.getString(cursor2.getColumnIndex("a28"));
                        String a29 = cursor2.getString(cursor2.getColumnIndex("a29"));
                        String a30 = cursor2.getString(cursor2.getColumnIndex("a30"));
                        String a31 = cursor2.getString(cursor2.getColumnIndex("a31"));
                        String a32 = cursor2.getString(cursor2.getColumnIndex("a32"));
                        String a33 = cursor2.getString(cursor2.getColumnIndex("a33"));
                        String a34 = cursor2.getString(cursor2.getColumnIndex("a34"));
                        String a35 = cursor2.getString(cursor2.getColumnIndex("a35"));
                        String a36 = cursor2.getString(cursor2.getColumnIndex("a36"));
                        String a37 = cursor2.getString(cursor2.getColumnIndex("a37"));
                        String a38 = cursor2.getString(cursor2.getColumnIndex("a38"));
                        String a39 = cursor2.getString(cursor2.getColumnIndex("a39"));
                        String a40 = cursor2.getString(cursor2.getColumnIndex("a40"));
                        String a41 = cursor2.getString(cursor2.getColumnIndex("a41"));
                        String a42 = cursor2.getString(cursor2.getColumnIndex("a42"));
                        String a43 = cursor2.getString(cursor2.getColumnIndex("a43"));
                        String a44 = cursor2.getString(cursor2.getColumnIndex("a44"));
                        String a45 = cursor2.getString(cursor2.getColumnIndex("a45"));
                        String a46 = cursor2.getString(cursor2.getColumnIndex("a46"));
                        String a47 = cursor2.getString(cursor2.getColumnIndex("a47"));
                        String a48 = cursor2.getString(cursor2.getColumnIndex("a48"));
                        String a49 = cursor2.getString(cursor2.getColumnIndex("a49"));
                        String a50 = cursor2.getString(cursor2.getColumnIndex("a50"));


                        int i = cursor2.getPosition() + 1;
                        sheet2.addCell(new Label(0, i, Student_ID));
                        sheet2.addCell(new Label(1, i, a1));
                        sheet2.addCell(new Label(2, i, a2));
                        sheet2.addCell(new Label(3, i, a3));
                        sheet2.addCell(new Label(4, i, a4));
                        sheet2.addCell(new Label(5, i, a5));
                        sheet2.addCell(new Label(6, i, a6));
                        sheet2.addCell(new Label(7, i, a7));
                        sheet2.addCell(new Label(8, i, a8));
                        sheet2.addCell(new Label(9, i, a9));
                        sheet2.addCell(new Label(10, i, a10));
                        sheet2.addCell(new Label(11, i, a11));
                        sheet2.addCell(new Label(12, i, a12));
                        sheet2.addCell(new Label(13, i, a13));
                        sheet2.addCell(new Label(14, i, a14));
                        sheet2.addCell(new Label(15, i, a15));
                        sheet2.addCell(new Label(16, i, a16));
                        sheet2.addCell(new Label(17, i, a17));
                        sheet2.addCell(new Label(18, i, a18));
                        sheet2.addCell(new Label(19, i, a19));
                        sheet2.addCell(new Label(20, i, a20));
                        sheet2.addCell(new Label(21, i, a21));
                        sheet2.addCell(new Label(22, i, a22));
                        sheet2.addCell(new Label(23, i, a23));
                        sheet2.addCell(new Label(24, i, a24));
                        sheet2.addCell(new Label(25, i, a25));
                        sheet2.addCell(new Label(26, i, a26));
                        sheet2.addCell(new Label(27, i, a27));
                        sheet2.addCell(new Label(28, i, a28));
                        sheet2.addCell(new Label(29, i, a29));
                        sheet2.addCell(new Label(30, i, a30));
                        sheet2.addCell(new Label(31, i, a31));
                        sheet2.addCell(new Label(32, i, a32));
                        sheet2.addCell(new Label(33, i, a33));
                        sheet2.addCell(new Label(34, i, a34));
                        sheet2.addCell(new Label(35, i, a35));
                        sheet2.addCell(new Label(36, i, a36));
                        sheet2.addCell(new Label(37, i, a37));
                        sheet2.addCell(new Label(38, i, a38));
                        sheet2.addCell(new Label(39, i, a39));
                        sheet2.addCell(new Label(40, i, a40));
                        sheet2.addCell(new Label(41, i, a41));
                        sheet2.addCell(new Label(42, i, a42));
                        sheet2.addCell(new Label(43, i, a43));
                        sheet2.addCell(new Label(44, i, a44));
                        sheet2.addCell(new Label(45, i, a45));
                        sheet2.addCell(new Label(46, i, a46));
                        sheet2.addCell(new Label(47, i, a47));
                        sheet2.addCell(new Label(48, i, a48));
                        sheet2.addCell(new Label(49, i, a49));
                        sheet2.addCell(new Label(50, i, a50));
                    } while (cursor2.moveToNext());
                }
                //closing cursor
                cursor2.close();
                workbook2.write();
                workbook2.close();
                dbHelper.deleteData();
                dbHelper.deleteData2();


                String choi=readfile3();
                String[] ss=  choi.split("/");
              String cn4=ss[0];
                name1=cn4;
                FileInputStream is = null;
                int read = 0;
                byte[] bytes = new byte[1024];
                try {
                    is =
                            new FileInputStream(file2);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                OutputStream outputStream = driveContents.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                ByteArrayOutputStream byt = new ByteArrayOutputStream();
                try {


                    try {
                        while ((read = is.read(bytes)) != -1) {
                            assert outputStream != null;

                            byt.write(bytes, 0, read);
                        }
                        outputStream.write(byt.toByteArray());

                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(""+name1+"_2")
                        .setMimeType("application/vnd.ms-excel")
                        .setStarred(true).build();

                // create a file in root folder
                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, driveContents)
                        .setResultCallback(fileCallback);
  //                  Toast.makeText(getApplication(), "The Excel2 Uploaded to your Google Drive", Toast.LENGTH_SHORT).show();
            }}.start();}


    public void upload4(DriveApi.DriveContentsResult result) throws Exception {

        final DriveContents driveContents = result.getDriveContents();

        // Perform I/O off the UI thread.
        new Thread() {
            @Override
            public void run() {
                // write content to DriveContents


//if the external storage directory does not exists, we create it

                File file;
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Report.pdf");

                //BaseFont bfBold = BaseFont.createFont("assets/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                //  Toast.makeText(getApplication(), FONT, Toast.LENGTH_SHORT).show();


//font of the title

                // Typeface tf = Typeface.createFromAsset(getAssets(), Path2font);


                Document document = new Document();
                PdfWriter writer2 = null;
                try {
                    writer2 = PdfWriter.getInstance(document, new FileOutputStream(file));
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                document.open();

                Phrase p;

                p = new Phrase("List Of Marks");

                ColumnText canvas = new ColumnText(writer2.getDirectContent());

                canvas.setSimpleColumn(250, 3, 830, 830);

                canvas.setAlignment(Element.ALIGN_CENTER);
                canvas.addElement(p);
                try {
                    canvas.go();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }

                PdfPTable table = new PdfPTable(6);


                PdfPCell cell = new PdfPCell(new Phrase("Registration Number"));
                cell.setFixedHeight(30);
                cell.setColspan(2);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                PdfPCell cell2 = new PdfPCell(new Phrase("Student Name"));
                cell2.setFixedHeight(15);
                cell2.setColspan(2);
                cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell2);

                PdfPCell cell3 = new PdfPCell(new Phrase("Student Mark"));
                cell3.setFixedHeight(30);
                cell3.setColspan(2);
                cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell3);

                int numofcircles = 0;
                int colss = 0;
                String doctor_page = readfile();
                String[] s = doctor_page.split("/");

                numofcircles = Integer.parseInt(s[0].trim());
                colss = numofcircles;
                String circles[] = new String[numofcircles];
                String[] s2 = s[1].split("_");
                int j = 0;
                int m = 0;
                int a[] = new int[numofcircles];
                for (int i = 0; i < numofcircles; i++) {
                    a[i] = Integer.parseInt(s2[2 * i].trim());
                }
                for (int i = 0; i < numofcircles; i++) {
                    circles[i] = s2[2 * i + 1];
                }

                int mark = Integer.parseInt(s[2].trim());
                //Toast.makeText(getApplication(), ""+circles[0]+circles[1], Toast.LENGTH_SHORT).show();
                DBHelper dbHelper = new DBHelper(getApplication());
                String stu_page = readfile2();
                String[] stu = stu_page.split(",");
                for (int b = 0; b < stu.length; b++) {
                    String full[] = new String[50];
                    for (int i = 0; i < 50; i++)
                        full[i] = " ";
                    String[] stu2 = stu[b].split("/");
                    int studennum = Integer.parseInt(stu2[1]);
                    String circles2[] = new String[studennum];//the answer of student

                    int rest = studennum - numofcircles;
                    String[] s22 = stu2[2].split("_");
                    int marksfromhw = Integer.parseInt(stu2[0]);
                    String[] replace = new String[rest];// for replacment the circle

                    for (int i = 0; i < studennum; i++) {
                        circles2[i] = s22[2 * i + 1];
                    }
                    for (int i = 0; i < rest; i++) {
                        replace[i] = circles2[numofcircles + i];

                    }
                    int y = 0;
                    for (int i = 0; i < numofcircles; i++) {
                        full[i] = circles2[i];
                        if (full[i].equals("r")) {
                            full[i] = replace[y];
                            circles2[i] = replace[y];

                            y++;
                        }
                    }
                    String id = stu2[3];
                    boolean[] result = new boolean[numofcircles];
                    int count = 0;
                    int finalmark = 0;
                    // Toast.makeText(getApplication(), ""+circles2[0]+circles2[1], Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < numofcircles; i++) {
                        if (circles[i].equals(circles2[i]))
                            result[i] = true;
                        if (!circles[i].equals(circles2[i]))
                            result[i] = false;


                    }
                    for (int i = 0; i < numofcircles; i++) {
                        if (result[i] == true)
                            count++;
                    }
                    finalmark = mark * count + marksfromhw;
                    String studentname = "";
                    String cn = "";
                    String choi = readfile3();
                    String[] ss = choi.split("/");
                    cn = ss[0];
                    Cursor cursor5 = dbHelper.getuser3(cn);
                    if (cursor5.moveToFirst()) {
                        do {
                            String Student_ID = cursor5.getString(cursor5.getColumnIndex("Student_ID"));
                            String Student_Name = cursor5.getString(cursor5.getColumnIndex("Student_Name"));
                            //  Toast.makeText(getApplicationContext(),Student_ID+Student_Name,Toast.LENGTH_LONG).show();
                            String temp = id;
                          //  id = id.replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣").replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧").replace("8", "٨").replace("9", "٩");

                            if (id.equals(Student_ID)) {
                                studentname = Student_Name;
                            }

                        } while (cursor5.moveToNext());
                    }
                    //closing cursor
                    cursor5.close();
                    PdfPCell cell4 = new PdfPCell(new Phrase("" + id));
                    cell4.setFixedHeight(30);
                    cell4.setColspan(2);
                    cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell4);

                    PdfPCell cell5 = new PdfPCell(new Phrase(studentname));
                    cell5.setFixedHeight(15);
                    cell5.setColspan(2);
                    cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell5);

                    PdfPCell cell6 = new PdfPCell(new Phrase(finalmark + ""));
                    cell6.setFixedHeight(30);
                    cell6.setColspan(2);
                    cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell6);

                }


                try {
                    document.add(table);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }


                document.close();
                dbHelper.deleteData2();
                dbHelper.deleteData();
                FileInputStream is = null;
                int read = 0;
                byte[] bytes = new byte[1024];
                try {
                    is=
                            new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                OutputStream outputStream = driveContents.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                ByteArrayOutputStream byt = new ByteArrayOutputStream();
                try {


                    try {
                        while ((read = is.read(bytes)) != -1) {
                            assert outputStream != null;

                            byt.write(bytes, 0, read);
                        }
                        outputStream.write(byt.toByteArray());

                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }} catch (Exception e) {
                    e.printStackTrace();
                }


                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(""+name1)
                        .setMimeType("application/pdf")
                        .setStarred(true).build();

                // create a file in root folder
                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, driveContents)
                        .setResultCallback(fileCallback);
//                Toast.makeText(getApplication(), "The Report Uploaded to your Google Drive", Toast.LENGTH_SHORT).show();
            }}.start();}
    public void upload3(DriveApi.DriveContentsResult result) throws Exception {

        final DriveContents driveContents = result.getDriveContents();

        // Perform I/O off the UI thread.
        new Thread() {
            @Override
            public void run() {
                // write content to DriveContents


//if the external storage directory does not exists, we create it

                File file;
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Report.pdf");

                //BaseFont bfBold = BaseFont.createFont("assets/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                String FONT = "assets/NotoNaskhArabic-Regular.ttf";
                //  Toast.makeText(getApplication(), FONT, Toast.LENGTH_SHORT).show();
                String ARABIC = "حنان";

//font of the title

                // Typeface tf = Typeface.createFromAsset(getAssets(), Path2font);
                Font f = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                f.setSize(11.0f);
                Font f2 = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                f2.setSize(30.0f);
                Document document = new Document();
                PdfWriter writer2 = null;
                try {
                    writer2 = PdfWriter.getInstance(document, new FileOutputStream(file));
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                document.open();

                Phrase p;

                p = new Phrase("");
                p.add(new Chunk("قائمة العلامات", f2));
                ColumnText canvas = new ColumnText(writer2.getDirectContent());

                canvas.setSimpleColumn(250, 3, 830, 830);
                canvas.setRunDirection(PdfWriter.RUN_DIRECTION_LTR);
                canvas.setAlignment(Element.ALIGN_CENTER);
                canvas.addElement(p);
                try {
                    canvas.go();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }

                PdfPTable table = new PdfPTable(6);

                table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                PdfPCell cell = new PdfPCell(new Phrase("رقم التسجيل", f));
                cell.setFixedHeight(30);
                cell.setColspan(2);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
                table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                PdfPCell cell2 = new PdfPCell(new Phrase("اسم الطالب", f));
                cell2.setFixedHeight(15);
                cell2.setColspan(2);
                cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell2);
                table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                PdfPCell cell3 = new PdfPCell(new Phrase("العلامة", f));
                cell3.setFixedHeight(30);
                cell3.setColspan(2);
                cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell3);

                int numofcircles = 0;
                int colss = 0;
                String doctor_page = readfile();
                String[] s = doctor_page.split("/");

                numofcircles = Integer.parseInt(s[0].trim());
                colss = numofcircles;
                String circles[] = new String[numofcircles];
                String[] s2 = s[1].split("_");
                int j = 0;
                int m = 0;
                int a[] = new int[numofcircles];
                for (int i = 0; i < numofcircles; i++) {
                    a[i] = Integer.parseInt(s2[2 * i].trim());
                }
                for (int i = 0; i < numofcircles; i++) {
                    circles[i] = s2[2 * i + 1];
                }

                int mark = Integer.parseInt(s[2].trim());
                //Toast.makeText(getApplication(), ""+circles[0]+circles[1], Toast.LENGTH_SHORT).show();
                DBHelper dbHelper = new DBHelper(getApplication());
                String stu_page = readfile2();
                String[] stu = stu_page.split(",");
                for (int b = 0; b < stu.length; b++) {
                    String full[] = new String[50];
                    for (int i = 0; i < 50; i++)
                        full[i] = " ";
                    String[] stu2 = stu[b].split("/");
                    int studennum = Integer.parseInt(stu2[1]);
                    String circles2[] = new String[studennum];//the answer of student

                    int rest = studennum - numofcircles;
                    String[] s22 = stu2[2].split("_");
                    int marksfromhw = Integer.parseInt(stu2[0]);
                    String[] replace = new String[rest];// for replacment the circle

                    for (int i = 0; i < studennum; i++) {
                        circles2[i] = s22[2 * i + 1];
                    }
                    for (int i = 0; i < rest; i++) {
                        replace[i] = circles2[numofcircles + i];

                    }
                    int y = 0;
                    for (int i = 0; i < numofcircles; i++) {
                        full[i] = circles2[i];
                        if (full[i].equals("r")) {
                            full[i] = replace[y];
                            circles2[i] = replace[y];

                            y++;
                        }
                    }
                    String id = stu2[3];
                    boolean[] result = new boolean[numofcircles];
                    int count = 0;
                    int finalmark = 0;
                    // Toast.makeText(getApplication(), ""+circles2[0]+circles2[1], Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < numofcircles; i++) {
                        if (circles[i].equals(circles2[i]))
                            result[i] = true;
                        if (!circles[i].equals(circles2[i]))
                            result[i] = false;


                    }
                    for (int i = 0; i < numofcircles; i++) {
                        if (result[i] == true)
                            count++;
                    }
                    finalmark = mark * count + marksfromhw;
                    String studentname = "";
                    String cn = "";
                    String choi = readfile3();
                    String[] ss = choi.split("/");
                    cn = ss[0];
                    Cursor cursor5 = dbHelper.getuser3(cn);
                    if (cursor5.moveToFirst()) {
                        do {
                            String Student_ID = cursor5.getString(cursor5.getColumnIndex("Student_ID"));
                            String Student_Name = cursor5.getString(cursor5.getColumnIndex("Student_Name"));
                            //  Toast.makeText(getApplicationContext(),Student_ID+Student_Name,Toast.LENGTH_LONG).show();
                            String temp = id;
                            id = id.replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣").replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧").replace("8", "٨").replace("9", "٩");

                            if (id.equals(Student_ID)) {
                                studentname = Student_Name;
                            }

                        } while (cursor5.moveToNext());
                    }
                    //closing cursor
                    cursor5.close();
                    PdfPCell cell4 = new PdfPCell(new Phrase("" + id, f));
                    cell4.setFixedHeight(30);
                    cell4.setColspan(2);
                    cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell4);
                    table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                    PdfPCell cell5 = new PdfPCell(new Phrase(studentname, f));
                    cell5.setFixedHeight(15);
                    cell5.setColspan(2);
                    cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell5);
                    table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                    PdfPCell cell6 = new PdfPCell(new Phrase(finalmark + "", f));
                    cell6.setFixedHeight(30);
                    cell6.setColspan(2);
                    cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell6);

                }


                try {
                    document.add(table);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }


                document.close();
                dbHelper.deleteData2();
                dbHelper.deleteData();
                FileInputStream is = null;
                int read = 0;
                byte[] bytes = new byte[1024];
                try {
                    is=
                            new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                OutputStream outputStream = driveContents.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                ByteArrayOutputStream byt = new ByteArrayOutputStream();
                try {


                    try {
                        while ((read = is.read(bytes)) != -1) {
                            assert outputStream != null;

                            byt.write(bytes, 0, read);
                        }
                        outputStream.write(byt.toByteArray());

                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }} catch (Exception e) {
                        e.printStackTrace();
                    }


                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(""+name1)
                        .setMimeType("application/pdf")
                        .setStarred(true).build();

                // create a file in root folder
                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, driveContents)
                        .setResultCallback(fileCallback);
//                Toast.makeText(getApplication(), "The Report Uploaded to your Google Drive", Toast.LENGTH_SHORT).show();
            }}.start();}


    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {

            /**
             * Create the API client and bind it to an instance variable.
             * We use this instance as the callback for connection and connection failures.
             * Since no account name is passed, the user is prompted to choose.
             */
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {

            // disconnect Google API client connection
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed:" + result.toString());

        if (!result.hasResolution()) {

            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }

        /**
         *  The failure has a resolution. Resolve it.
         *  Called typically when the app is not yet authorized, and an  authorization
         *  dialog is displayed to the user.
         */

        try {

            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);

        } catch (IntentSender.SendIntentException e) {

        }
    }

    /**
     * It invoked when Google API client connected
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {

        Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_LONG).show();
    }

    /**
     * It invoked when connection suspend
     * @param cause
     */
    @Override
    public void onConnectionSuspended(int cause) {


    }






    /**
     * This is Result result handler of Drive contents.
     * this callback method call CreateFileOnGoogleDrive() method
     * and also call OpenFileFromGoogleDrive() method,
     * send intent onActivityResult() method to handle result.
     */
    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(DriveApi.DriveContentsResult result) {

            if (result.getStatus().isSuccess()) {

                if (fileOperation == 0) {

                   upload1(result);

                }

                if (fileOperation == 1) {

                   upload2(result);

                }

                if (fileOperation == 2) {

                    try {
                        upload4(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

        }
    };


    /**
     * Handle result of Created file
     */
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (result.getStatus().isSuccess()) {

                        Toast.makeText(getApplication(), "The File Uploaded to your Google Drive", Toast.LENGTH_SHORT).show();
                    }

                    return;

                }
            };
}

