package com.example.hanan.marksreader;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


public class addclass1 extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final int MY_PERMISSIONS_REQUEST_WRITE = 3;
    private static final String TAG = "Google Drive Activity";
    private static final int REQUEST_CODE_RESOLUTION = 1;
    private static final  int REQUEST_CODE_OPENER = 2;
    private GoogleApiClient mGoogleApiClient;
    private boolean fileOperation = false;
    private DriveId mFileId;
    public DriveFile file;
private String filname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission( this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE);}

        super.onCreate(savedInstanceState);
        setContentView(R.layout.addclass1);

    }

    /**
     * Called when the activity will start interacting with the user.
     * At this point your activity is at the top of the activity stack,
     * with user input going to it.
     */

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

    public void onClickCreateFile(View view){
        fileOperation = true;

        // create new contents resource
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);

    }

    public void onClickOpenFile(View view){
        fileOperation = false;

        // create new contents resource
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);
    }

    /**
     *  Open list of folder and file of the Google Drive
     */
    public void OpenFileFromGoogleDrive(){

        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .build(mGoogleApiClient);
        try {
            startIntentSenderForResult(

                    intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);

        } catch (IntentSender.SendIntentException e) {

        }

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

                if (fileOperation == true) {

                    CreateFileOnGoogleDrive(result);

                } else {

                    OpenFileFromGoogleDrive();

                }
            }

        }
    };

    /**
     * Create a file in root folder using MetadataChangeSet object.
     * @param result
     */
    public void CreateFileOnGoogleDrive(DriveApi.DriveContentsResult result){

        final DriveContents driveContents = result.getDriveContents();

        // Perform I/O off the UI thread.
        new Thread() {
            @Override
            public void run() {
                // write content to DriveContents
                OutputStream outputStream = driveContents.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                try {
                    writer.write("Hello abhay!");
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle("abhaytest2")
                        .setMimeType("text/plain")
                        .setStarred(true).build();

                // create a file in root folder
                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, driveContents)
                        .setResultCallback(fileCallback);
            }
        }.start();
    }

    /**
     * Handle result of Created file
     */
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
    ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(DriveFolder.DriveFileResult result) {
            if (result.getStatus().isSuccess()) {

                Toast.makeText(getApplicationContext(), "file created:"+""+
                        result.getDriveFile().getDriveId(), Toast.LENGTH_LONG).show();

            }

            return;

        }
    };

    /**
     *  Handle Response of selected file
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent data) {
        switch (requestCode) {

            case REQUEST_CODE_OPENER:

                if (resultCode == RESULT_OK) {

                    mFileId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    Log.e("file id", mFileId.getResourceId() + "");

                    String url = "https://drive.google.com/open?id="+ mFileId.getResourceId();

                 DriveFile m=   mFileId.asDriveFile();
                    new DownloadFilesTask().execute("");
                    Toast.makeText(getApplicationContext(),"Your Selected File Is Stored!", Toast.LENGTH_LONG).show();
                    Button b= (Button) findViewById(R.id.store);
                    b.setEnabled(true);
b.setBackgroundResource(R.drawable.cor);


                 /*   Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);*/
                }

                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
    private class DownloadFilesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            DriveApi.DriveContentsResult result = mFileId.asDriveFile()
                    .open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).await();
           if( result.getStatus().isSuccess())
           {
                InputStream is= result.getDriveContents().getInputStream();
               OutputStream os =null;
               int read = 0;
               byte[] bytes = new byte[1024];
               try {
                   String csvFile = "Data.xls";
                //   File  =new File(Environment.getExternalStorageDirectory()+"/hanan.txt");
                   filname= Environment.getExternalStorageDirectory()+"/Data.xls";
                   File newfile = new File(filname);
                   try {
                       if(!newfile.exists()){
                           newfile.createNewFile();
                       }
                 os=
                           new FileOutputStream(newfile);
               } catch (FileNotFoundException e) {
                   e.printStackTrace();
               } catch (IOException e) {
                       e.printStackTrace();
                   }

                   try {
                   while ((read = is.read(bytes)) != -1) {
                       assert os != null;
                       os.write(bytes, 0, read);
                   }
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }catch (Exception e) {
                   e.printStackTrace();}}
           return "suc";
        }


    }

    public void store(View v)
{
    DBHelper dbHelper = new DBHelper(this);

  EditText et=  (EditText)findViewById(R.id.input_name);
 String doctorname= et.getText().toString();
    EditText et1=  (EditText)findViewById(R.id.input_nameofcourse);
    String coursename= et1.getText().toString();
    EditText et2=  (EditText)findViewById(R.id.input_numberofclass);
    String classnum= et2.getText().toString();
    dbHelper.creattable(coursename+classnum);
    ArrayList<String> resultSet = new ArrayList<String>();
  filname= Environment.getExternalStorageDirectory()+"/Data.xls";
    File inputWorkbook = new File(filname);
    String name=" ";
    String id=" ";
    if(inputWorkbook.exists()){
        Workbook w;
        try {
            w = Workbook.getWorkbook(inputWorkbook);
            // Get the first sheet
            Sheet sheet = w.getSheet(0);
            // Loop over column and lines
            for (int j = 0; j < sheet.getRows(); j++) {
                Cell cell = sheet.getCell(0, j);
               name= cell.getContents();
                Cell cell2 = sheet.getCell(1, j);
                id= cell2.getContents();

                dbHelper.insertData3(id,name,coursename+classnum);
                continue;
            }
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    else
    {
        resultSet.add("File not found..!");
    }
    if(resultSet.size()==0){
        resultSet.add("Data not found..!");
    }
    final Cursor cursor = dbHelper.getuser3(coursename+classnum);
    if (cursor.moveToFirst()) {
        do {
            String Student_ID = cursor.getString(cursor.getColumnIndex("Student_ID"));
            String Student_Name = cursor.getString(cursor.getColumnIndex("Student_Name"));



        } while (cursor.moveToNext());
    }
    //closing cursor
    cursor.close();
    Toast.makeText(getApplicationContext(),"Class is added", Toast.LENGTH_LONG).show();

}
}
