package com.example.hanan.marksreader;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.PathShape;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.todobom.opennotescanner.helpers.OpenNoteMessage;
import com.todobom.opennotescanner.helpers.PreviewFrame;
import com.todobom.opennotescanner.helpers.Quadrilateral;
import com.todobom.opennotescanner.helpers.ScannedDocument;
import com.todobom.opennotescanner.views.HUDCanvasView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import static org.opencv.imgproc.Imgproc.GaussianBlur;

/**
 * Created by allgood on 05/03/16.
 */
public class ImageProcessor3 extends Handler {

    private static final String TAG = "ImageProcessor3";
    private final Handler mUiHandler;
    private final firstImage mMainActivity;
    private boolean mBugRotate;
    private boolean colorMode=false;
    private boolean filterMode=true;
    private double colorGain = 1.5;       // contrast
    private double colorBias = 0;         // bright
    private int colorThresh = 110;        // threshold
    private Size mPreviewSize;
    private Point[] mPreviewPoints;
    private ResultPoint[] qrResultPoints;
    private TessBaseAPI mTess;
    private ImageProcessor3 instance =null;
    String datapath = "";

    public ImageProcessor3 (Looper looper , Handler uiHandler , firstImage mainActivity ) {
        super(looper);
        mUiHandler = uiHandler;
        mMainActivity = mainActivity;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        mBugRotate = sharedPref.getBoolean("bug_rotate",false);
    }

    public void handleMessage ( Message msg ) {

        if (msg.obj.getClass() == OpenNoteMessage.class) {

            OpenNoteMessage obj = (OpenNoteMessage) msg.obj;

            String command = obj.getCommand();

            Log.d(TAG, "Message Received: " + command + " - " + obj.getObj().toString() );

            if ( command.equals("previewFrame")) {
                processPreviewFrame((PreviewFrame) obj.getObj());
            } else if ( command.equals("pictureTaken")) {
                processPicture((Mat) obj.getObj());
            }
        }
    }

    private void processPreviewFrame( PreviewFrame previewFrame ) {

        Result[] results = {};

        Mat frame = previewFrame.getFrame();

        try {
            results = zxing(frame);
        } catch (ChecksumException | FormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        if ( detectPreviewDocument(frame))
        {


            mMainActivity.requestPicture();
        }



    }

    public void processPicture( Mat picture ) {

        Mat img = Imgcodecs.imdecode(picture, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        picture.release();



        if (mBugRotate) {
            Core.flip(img, img, 1 );
            Core.flip(img, img, 0 );
        }

        ScannedDocument doc = detectDocument(img);



        picture.release();

        mMainActivity.setImageProcessorBusy(false);
        mMainActivity.setAttemptToFocus(false);
        mMainActivity.waitSpinnerInvisible();
        if(doc!=null) {
        //    mMainActivity.saveDocument(doc);
           mMainActivity.goToSecond();
        }
    }


    private ScannedDocument detectDocument(Mat inputRgba) {
        ArrayList<MatOfPoint> contours = findContours(inputRgba);

        ScannedDocument sd = new ScannedDocument(inputRgba);

        Quadrilateral quad = getQuadrilateral(contours, inputRgba.size());

        Mat doc;

        if (quad != null) {

            MatOfPoint c = quad.contour;

            sd.quadrilateral = quad;
            sd.previewPoints = mPreviewPoints;
            sd.previewSize = mPreviewSize;

            doc = fourPointTransform(inputRgba, quad.points);

        } else {
            return null;
        }
        Mat t=doc.clone();
        enhanceDocument(doc);
        Point[] big_rect = find_first_rec(doc);

     //   circle(t, big_rect[3],4, new Scalar(0, 0, 255), 3, 8, 0);

        String cn=" ";
        String choi=readfile3();
        String[] ss=  choi.split("/");
        cn=ss[1];
        int Q= Integer.parseInt(cn);
        int nump= Integer.parseInt(cn)+2;
        int [] marks= new int [Q-1];
        int mr=0;
        if (big_rect[0].y < 100.0&&big_rect[3].y < 100.0 && big_rect[1].y > 1400.0&&big_rect[2].y > 1400.0) {

            double coldis = (big_rect[2].y - big_rect[0].y) / 6;
            double rowdis = (big_rect[3].x - big_rect[1].x) / nump;
            Point start = new Point();
            start = big_rect[1];
            Point[] points2 = big_rect;
            Point[] temp=new Point[4];
            Point[] points3 = big_rect;
            for (int r = 1; r <= Q; r++)//the number that enter by the doctor rather than 2
                if (r == 1) {
                    //the points cloumns only
                }
                else {
                    double c1 = 1.0;
                    double c2 = 3.0;
                    double height = start.y - (c1 + 1) * coldis;
                    double width = start.x + (r + 1) * rowdis;
                    for(int i=0;i<4;i++)
                    {
                        points2[i].y=0.0;
                                points2[i].x=0.0;
                    }


                    points2[1].y = height+10.0;
                    points2[0].x = width-rowdis+10.0;
                    points2[2].x = width-10.0;
                    points2[3].y = height+coldis-10.0;
                    points2[0].y =  height+coldis-10.0;
                    points2[2].y = height+10.0;
                    points2[1].x = width-rowdis+10.0;
                    points2[3].x = width-10.0;
                    for(int m=0;m<4;m++)
                    {
                        temp[m]=new Point(points2[m].x,points2[m].y);
                    }
                    double height2 = height - (c2 ) * coldis;

                    for(int i=0;i<4;i++)
                    {
                        points3[i].y=0.0;
                        points3[i].x=0.0;
                    }


                    points3[1].y = height2+10.0;
                    points3[0].x = width-rowdis+10.0;
                    points3[2].x = width-10.0;
                    points3[3].y = height2+coldis-10.0;
                    points3[0].y =  height2+coldis-10.0;
                    points3[2].y = height2+10.0;
                    points3[1].x = width-rowdis+10.0;
                    points3[3].x = width-10.0;

            enhanceDocument(t);
doc=t;
          Mat  doc2 = fourPointTransform2(doc, temp);
            Mat  doc3= fourPointTransform2(doc, points3);
          //  Rect r= new Rect((int)points2[0].x,(int)points2[0].y,(int)(points2[0].y-points2[1].y),(int)(points2[0].x-points2[2].x));
            ScannedDocument doc4=sd.setProcessed(doc2);
                mMainActivity.saveDocument(doc4);
                Bitmap m=readim();
                instance=this;
            String language = "eng";
            datapath = mMainActivity.getFilesDir() + "/tesseract/";
            mTess = new TessBaseAPI();

            checkFile(new File(datapath + "tessdata/"));

            mTess.init(datapath, language);
            String num = tess_print(m);
            boolean seq[]=new boolean[num.length()];
            for(int i=0;i<num.length();i++)
            {

                    seq[i]=false;

            }
            for(int i=0;i<num.length();i++)
            {
                if(num.charAt(i)>='0'&&num.charAt(i)<='9')
                {
                    seq[i]=true;
                }
            }
            boolean res=true;
            for(int i=0;i<num.length();i++)
            {

                res&=seq[i];

            }
            int pm=0;
            if(res)
            {pm= Integer.parseInt(num);
     //       mMainActivity.pr(num+seq[0]+seq[1]);
            ScannedDocument doc5=sd.setProcessed(doc3);
            language="hwdigits";
            mMainActivity.saveDocument(doc5);
            datapath = mMainActivity.getFilesDir() + "/tesseract/";
            checkFile(new File(datapath + "tessdata/"));
     mTess.init(datapath, language);
            Bitmap m2=readim();
            String num2 = tess_hw(m2);
                boolean seq2[]=new boolean[num2.length()];
                for(int i=0;i<num2.length();i++)
                {

                    seq2[i]=false;

                }
                for(int i=0;i<num2.length();i++)
                {
                    if(num2.charAt(i)>='0'&&num2.charAt(i)<='9')
                    {
                        seq2[i]=true;
                    }
                }
                boolean res2=true;
                for(int i=0;i<num2.length();i++)
                {

                    res2&=seq2[i];

                }
                if(res2)
                {  int hwm= Integer.parseInt(num2);
            if(hwm>0&&hwm<=pm) {
                mMainActivity.pr(num2);
                marks[mr]=hwm;
                mr++;

            }
                }
                else return null;
            }
            else
            return null;
        }
            int rr=0;
        for (int u=0;u<mr;u++)
        {
            rr+=marks[u];

        }
            mMainActivity.pr(rr);
            writeToFile(""+rr);
        return sd.setProcessed(doc);
        }
        else return null;

    }
    private void writeToFile(String data) {
        String append=readfile();

        File file = new File(Environment.getExternalStorageDirectory(),"storagefile");
        if(!file.exists()){
            if(file.mkdir()){

            }

        }

        File newfile = new File(file.getAbsolutePath()+"/student.txt");
        try {
            if(!newfile.exists()){
                newfile.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(newfile);
            String finalres;
            if(!append.equals(""))
                finalres=append+","+data+"/";
            else
                finalres=data+"/";
            outputStream.write(finalres.getBytes());
            outputStream.close();

        } catch (Exception e) {

        }


    }
    public String readfile()
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
    private Bitmap readim()
    {


        String photopath= Environment.getExternalStorageDirectory()+"/MarksReader"+"/im.jpg";
        Bitmap bitmap= BitmapFactory.decodeFile(photopath);

        Bitmap true2=bitmap.copy(Bitmap.Config.ARGB_8888,true);

        Matrix matrix=new Matrix();
        matrix.postRotate(270);
        Bitmap true3= Bitmap.createBitmap(true2,0,0,true2.getWidth(),true2.getHeight(),matrix,true);

        return true3;

    }

    public String tess_print(Bitmap image)
    {
        String OCRresult=" ";
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        return OCRresult;
    }
    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }
    public String tess_hw(Bitmap image)
    {
        String OCRresult=" ";
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        return OCRresult;
    }

    private void checkFile2(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles2();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/hwdigits.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles2();
            }
        }
    }
    private void copyFiles() {
        try {

            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = mMainActivity.getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void copyFiles2() {
        try {

            String filepath = datapath + "/tessdata/hwdigits.traineddata";
            AssetManager assetManager = mMainActivity.getAssets();

            InputStream instream = assetManager.open("tessdata/hwdigits.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
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
    private ArrayList<MatOfPoint> findContours2(Mat src) {

        Mat grayImage = src;
        Mat cannedImage = null;




        cannedImage = new Mat();


//        Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
       GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
       Imgproc.Canny(grayImage, cannedImage, 75, 200);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannedImage, contours,hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        hierarchy.release();

        grayImage.release();
        cannedImage.release();

        return contours;}
    private Point [] find_first_rec(Mat mat){
        ArrayList<MatOfPoint> contours = findContours2(mat);
        double s=0.0;
        double s2=0.0;
        int c1=0;
        int c2=0;
        Point[] pointsm=new Point[4];

        for (int i=0;i<contours.size();i++ ) {


            MatOfPoint c=contours.get(i);
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);
            Point[] points = approx.toArray();
            double ratio1 = mat.size().height / 500;
            int height = Double.valueOf(mat.size().height / ratio1).intValue();
            int width = Double.valueOf(mat.size().width / ratio1).intValue();
            Size size = new Size(width,height);
            //  Mat dst =new Mat();
            //  dst=  Imgproc.getPerspectiveTransform(mat ,dst);
            if (points.length == 4) {

                double area=  Imgproc.contourArea(contours.get(i));

                if(area>s)
                {


                    s=area;

                    pointsm=points;

                }
            }}


        return pointsm;
    }
    private HashMap<String,Long> pageHistory = new HashMap<>();

    private boolean checkQR(String qrCode) {

        return ! ( pageHistory.containsKey(qrCode) &&
                pageHistory.get(qrCode) > new Date().getTime()/1000-15) ;

    }

    private boolean detectPreviewDocument(Mat inputRgba) {

        ArrayList<MatOfPoint> contours = findContours(inputRgba);

        Quadrilateral quad = getQuadrilateral(contours, inputRgba.size());

        mPreviewPoints = null;
        mPreviewSize = inputRgba.size();

        if (quad != null) {

            Point[] rescaledPoints = new Point[4];

            double ratio = inputRgba.size().height / 500;

            for ( int i=0; i<4 ; i++ ) {
                int x = Double.valueOf(quad.points[i].x*ratio).intValue();
                int y = Double.valueOf(quad.points[i].y*ratio).intValue();
                if (mBugRotate) {
                    rescaledPoints[(i+2)%4] = new Point( Math.abs(x- mPreviewSize.width), Math.abs(y- mPreviewSize.height));
                } else {
                    rescaledPoints[i] = new Point(x, y);
                }
            }

            mPreviewPoints = rescaledPoints;

            drawDocumentBox(mPreviewPoints, mPreviewSize);

            Log.d(TAG, quad.points[0].toString() + " , " + quad.points[1].toString() + " , " + quad.points[2].toString() + " , " + quad.points[3].toString());

            return true;

        }

        mMainActivity.getHUD().clear();
        mMainActivity.invalidateHUD();

        return false;

    }

    private void drawDocumentBox(Point[] points, Size stdSize) {

        Path path = new Path();

        HUDCanvasView hud = mMainActivity.getHUD();

        // ATTENTION: axis are swapped

        float previewWidth = (float) stdSize.height;
        float previewHeight = (float) stdSize.width;

        path.moveTo( previewWidth - (float) points[0].y, (float) points[0].x );
        path.lineTo( previewWidth - (float) points[1].y, (float) points[1].x );
        path.lineTo( previewWidth - (float) points[2].y, (float) points[2].x );
        path.lineTo( previewWidth - (float) points[3].y, (float) points[3].x );
        path.close();

        PathShape newBox = new PathShape(path , previewWidth , previewHeight);

        Paint paint = new Paint();
        paint.setColor(Color.argb(64, 0, 0, 255));

        Paint border = new Paint();
        border.setColor(Color.rgb(0, 0, 255));
        border.setStrokeWidth(5);

        hud.clear();
        hud.addShape(newBox, paint, border);
        mMainActivity.invalidateHUD();


    }

    private Quadrilateral getQuadrilateral(ArrayList<MatOfPoint> contours , Size srcSize ) {

        double ratio = srcSize.height / 500;
        int height = Double.valueOf(srcSize.height / ratio).intValue();
        int width = Double.valueOf(srcSize.width / ratio).intValue();
        Size size = new Size(width,height);

        for ( MatOfPoint c: contours ) {
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);

            Point[] points = approx.toArray();

            // select biggest 4 angles polygon
            if (points.length == 4) {
                Point[] foundPoints = sortPoints(points);

                if (insideArea(foundPoints, size)) {
                    return new Quadrilateral( c , foundPoints );
                }
            }
        }

        return null;
    }

    private Point[] sortPoints( Point[] src ) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        Point[] result = { null , null , null , null };

        Comparator<Point> sumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x);
            }
        };

        Comparator<Point> diffComparator = new Comparator<Point>() {

            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x);
            }
        };

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator);

        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator);

        // top-right corner = minimal diference
        result[1] = Collections.min(srcPoints, diffComparator);

        // bottom-left corner = maximal diference
        result[3] = Collections.max(srcPoints, diffComparator);

        return result;
    }

    private boolean insideArea(Point[] rp, Size size) {

        int width = Double.valueOf(size.width).intValue();
        int height = Double.valueOf(size.height).intValue();
        int baseMeasure = height/4;

        int bottomPos = height-baseMeasure;
        int topPos = baseMeasure;
        int leftPos = width/2-baseMeasure;
        int rightPos = width/2+baseMeasure;

        return (
                rp[0].x <= leftPos && rp[0].y <= topPos
                        && rp[1].x >= rightPos && rp[1].y <= topPos
                        && rp[2].x >= rightPos && rp[2].y >= bottomPos
                        && rp[3].x <= leftPos && rp[3].y >= bottomPos

        );
    }

    private void enhanceDocument( Mat src ) {
        if (colorMode && filterMode) {
            src.convertTo(src,-1, colorGain , colorBias);
            Mat mask = new Mat(src.size(), CvType.CV_8UC1);
            Imgproc.cvtColor(src,mask,Imgproc.COLOR_RGBA2GRAY);

            Mat copy = new Mat(src.size(), CvType.CV_8UC3);
            src.copyTo(copy);

            Imgproc.adaptiveThreshold(mask,mask,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,15,15);

            src.setTo(new Scalar(255,255,255));
            copy.copyTo(src,mask);

            copy.release();
            mask.release();

            // special color threshold algorithm
            colorThresh(src,colorThresh);
        } else if (!colorMode) {
            Imgproc.cvtColor(src,src,Imgproc.COLOR_RGBA2GRAY);
            if (filterMode) {
                Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 15);
            }
        }
    }

    /**
     * When a pixel have any of its three elements above the threshold
     * value and the average of the three values are less than 80% of the
     * higher one, brings all three values to the max possible keeping
     * the relation between them, any absolute white keeps the value, all
     * others go to absolute black.
     *
     * src must be a 3 channel image with 8 bits per channel
     *
     * @param src
     * @param threshold
     */
    private void colorThresh(Mat src, int threshold) {
        Size srcSize = src.size();
        int size = (int) (srcSize.height * srcSize.width)*3;
        byte[] d = new byte[size];
        src.get(0,0,d);

        for (int i=0; i < size; i+=3) {

            // the "& 0xff" operations are needed to convert the signed byte to double

            // avoid unneeded work
            if ( (double) (d[i] & 0xff) == 255 ) {
                continue;
            }

            double max = Math.max(Math.max((double) (d[i] & 0xff), (double) (d[i + 1] & 0xff)),
                    (double) (d[i + 2] & 0xff));
            double mean = ((double) (d[i] & 0xff) + (double) (d[i + 1] & 0xff)
                    + (double) (d[i + 2] & 0xff)) / 3;

            if (max > threshold && mean < max * 0.8) {
                d[i] = (byte) ((double) (d[i] & 0xff) * 255 / max);
                d[i + 1] = (byte) ((double) (d[i + 1] & 0xff) * 255 / max);
                d[i + 2] = (byte) ((double) (d[i + 2] & 0xff) * 255 / max);
            } else {
                d[i] = d[i + 1] = d[i + 2] = 0;
            }
        }
        src.put(0,0,d);
    }

    private Mat fourPointTransform( Mat src , Point[] pts ) {

        double ratio = src.size().height / 500;
        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();

        Point tl = pts[0];
        Point tr = pts[1];
        Point br = pts[2];
        Point bl = pts[3];

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));

        double dw = Math.max(widthA, widthB)*ratio;
        int maxWidth = Double.valueOf(dw).intValue();


        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));

        double dh = Math.max(heightA, heightB)*ratio;
        int maxHeight = Double.valueOf(dh).intValue();

        Mat doc = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x*ratio, tl.y*ratio, tr.x*ratio, tr.y*ratio, br.x*ratio, br.y*ratio, bl.x*ratio, bl.y*ratio);
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Imgproc.warpPerspective(src, doc, m, doc.size());

        return doc;
    }
    private Mat fourPointTransform2( Mat src , Point[] pts ) {

        Point tl = pts[0];
        Point tr = pts[1];
        Point br = pts[2];
        Point bl = pts[3];

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(bl.y - br.y, 2));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tl.y - tr.y, 2));

        double dw = Math.max(widthA, widthB);
        int maxWidth = Double.valueOf(dw).intValue();


        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));

        double dh = Math.max(heightA, heightB);
        int maxHeight = Double.valueOf(dh).intValue();

        Mat doc = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x, tl.y, tr.x, tr.y, br.x, br.y, bl.x, bl.y);
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Imgproc.warpPerspective(src, doc, m, doc.size());

        return doc;
    }
    private ArrayList<MatOfPoint> findContours(Mat src) {

        Mat grayImage = null;
        Mat cannedImage = null;
        Mat resizedImage = null;

        double ratio = src.size().height / 500;
        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();
        Size size = new Size(width,height);

        resizedImage = new Mat(size, CvType.CV_8UC4);
        grayImage = new Mat(size, CvType.CV_8UC4);
        cannedImage = new Mat(size, CvType.CV_8UC1);

        Imgproc.resize(src,resizedImage,size);
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
        Imgproc.Canny(grayImage, cannedImage, 75, 200);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannedImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        hierarchy.release();

        Collections.sort(contours, new Comparator<MatOfPoint>() {

            @Override
            public int compare(MatOfPoint lhs, MatOfPoint rhs) {
                return Double.valueOf(Imgproc.contourArea(rhs)).compareTo(Imgproc.contourArea(lhs));
            }
        });

        resizedImage.release();
        grayImage.release();
        cannedImage.release();

        return contours;
    }

    private QRCodeMultiReader qrCodeMultiReader = new QRCodeMultiReader();



    public Result[] zxing( Mat inputImage ) throws ChecksumException, FormatException {

        int w = inputImage.width();
        int h = inputImage.height();

        Mat southEast;

        if (mBugRotate) {
            southEast = inputImage.submat(h-h/4 , h , 0 , w/2 - h/4 );
        } else {
            southEast = inputImage.submat(0, h / 4, w / 2 + h / 4, w);
        }

        Bitmap bMap = Bitmap.createBitmap(southEast.width(), southEast.height(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(southEast, bMap);
        southEast.release();
        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(),intArray);

        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result[] results = {};
        try {
            results = qrCodeMultiReader.decodeMultiple(bitmap);
        }
        catch (NotFoundException e) {
        }

        return results;

    }

    public void setBugRotate(boolean bugRotate) {
        mBugRotate = bugRotate;
    }
}