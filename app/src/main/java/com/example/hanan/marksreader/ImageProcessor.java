package com.example.hanan.marksreader;

import android.content.SharedPreferences;
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
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import static java.lang.Math.pow;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.CV_HOUGH_GRADIENT;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.HoughCircles;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.circle;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.resize;

/**
 * Created by allgood on 05/03/16.
 */
public class ImageProcessor extends Handler {

    private static final String TAG = "ImageProcessor";
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
private Point pointsquad[];
double minn, maxx;


    public ImageProcessor (Looper looper , Handler uiHandler ,FirstImage mainActivity ) {
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

        if ( detectPreviewDocument(frame)){
           // mMainActivity.waitSpinnerVisible();

            mMainActivity.requestPicture();
        }


        frame.release();
        mMainActivity.setImageProcessorBusy(false);

    }

    public void processPicture( Mat picture ) {

        Mat img = Imgcodecs.imdecode(picture, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        picture.release();

        Log.d(TAG, "processPicture - imported image " + img.size().width + "x" + img.size().height);

        if (mBugRotate) {
            Core.flip(img, img, 1 );
            Core.flip(img, img, 0 );
        }

        ScannedDocument doc = detectDocument(img);
        if(doc!=null) {
            mMainActivity.saveDocument(doc);
            mMainActivity.goTofirst();

        }
/**/
        picture.release();

        mMainActivity.setImageProcessorBusy(false);
        mMainActivity.setAttemptToFocus(false);
        mMainActivity.waitSpinnerInvisible();
    }


    private ScannedDocument detectDocument(Mat inputRgba) {
        ArrayList<MatOfPoint> contours = findContours(inputRgba);
        ArrayList<MatOfPoint> contours2;
        ScannedDocument sd = new ScannedDocument(inputRgba);

        Quadrilateral quad = getQuadrilateral(contours, inputRgba.size());

        Mat doc;
        boolean min1=true;
        boolean max1=true;

        int count=0;
        for ( MatOfPoint c2: contours ) {

            MatOfPoint2f c2f = new MatOfPoint2f(c2.toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);

            Point[] points = approx.toArray();





            count++;
        }
        if (quad != null) {

            MatOfPoint c = quad.contour;

            sd.quadrilateral = quad;
            sd.previewPoints = mPreviewPoints;
            sd.previewSize = mPreviewSize;

            doc = fourPointTransform(inputRgba, quad.points);

           contours2 = findContours(doc);


            }



        else {
           return null;
        }
        Point circles1to50[]=new Point [50];
        Point circlesid[]=new Point [8];
        Mat ret=doc.clone();
        Point  small_rect []=find_small_rec(doc);
        Point[] big_rect=find_first_rec(doc);
        if (big_rect != null && small_rect != null) {
            if(small_rect[0]!=null&&small_rect[1]!=null&&small_rect[2]!=null&&small_rect[3]!=null&&big_rect[0]!=null&&big_rect[1]!=null&&big_rect[2]!=null&&big_rect[3]!=null)
            { if (small_rect[2].y < big_rect[1].y&&big_rect[0].x>2000.0&&big_rect[3].x>2000.0&&small_rect[0].x>1000.0&&small_rect[1].x>1000.0) {

                Mat blue_hsv = new Mat();
                cvtColor(doc, blue_hsv, COLOR_BGR2HSV);
                Core.inRange(blue_hsv, new Scalar(110, 50, 50), new Scalar(130, 255, 255), doc);//blue circles
                Imgproc.erode(doc, doc, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(11, 11)));
                Imgproc.medianBlur(doc, doc, 3);

                Mat circles = new Mat();
                Imgproc.HoughCircles(doc, circles, Imgproc.CV_HOUGH_GRADIENT,
                        1, 20, 20, 10, 15, 50);
                int i = 0;
                int n = 0;
                if (circles.cols() > 0)
                    for (int x = 0; x < circles.cols(); x++) {
                        double vCircle[] = circles.get(0, x);

                        if (vCircle == null)
                            break;

                        Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                        int radius = (int) Math.round(vCircle[2]);
                        if ((big_rect != null && pt.x < big_rect[0].x && pt.x > big_rect[2].x && pt.y > big_rect[1].y && pt.y < big_rect[3].y)) {
                            circle(doc, pt, 3, new Scalar(0, 255, 0), -1, 8, 0);
                            // draw the circle outline
                            circle(doc, pt, radius, new Scalar(0, 0, 255), 3, 8, 0);
                            circles1to50[i] = pt;
                            i++;
                        } else if (small_rect != null)
                            if ((small_rect != null && pt.x < small_rect[0].x && pt.x > small_rect[2].x && pt.y < small_rect[2].y && pt.y > small_rect[3].y)) {
                                circle(doc, pt, 3, new Scalar(0, 255, 0), -1, 8, 0);
                                // draw the circle outline
                                circle(doc, pt, radius, new Scalar(0, 0, 255), 3, 8, 0);
                                circlesid[n] = pt;
                                n++;
                            }
                    }

                Point[] c1to50 = new Point[i];//i=number of circles in doctor page
                for (int y1 = 0; y1 < i; y1++) {
                    c1to50[y1] = circles1to50[y1];
                }
                String s1to50 = getMultichoiceArrayonetotwentyfifty(doc, c1to50, big_rect);

                String id = "";
                if (n != 0) {
                    id = student_id(doc, circlesid, small_rect);

                }
                boolean flag=true;
                boolean seq=false;
                String sb = readfile();
                for(int c=0;c<sb.length();c++)
                {
                    if(sb.charAt(c)==',')
                    {
                        seq=true;
                    }
                }
                if(seq){
                String[] stu=  sb.split(",");
                for(int b=0;b<stu.length-1;b++) {
                    String[] stu2 = stu[b].split("/");
                    String id2=stu2[3];
                  if(id2.equals(id)){
                     flag=false;
                         }

                }}
                if(flag==true) {
                    String docdata = "" + i + "/" + s1to50 + "/" + id;

                    writeToFile(docdata);
                    mMainActivity.prints();
                    return sd.setProcessed(doc);
                }
                else
                {
                    mMainActivity.prints2();
                    return  null;
                }


            }
            else return null;
    }else return null;}
    else return null;}

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
             finalres=append+data;
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
    public String getMultichoiceArrayonetotwentyfifty(Mat id_mat, Point id_circles[], Point id_rec[])
    {

        String mc="";
        int s=id_circles.length;
        char mc1to50[]=new char[50];
        for(int i=0;i<50;i++)
        {
            mc1to50[i]=' ';
        }
        double coldis=(id_rec[2].y-id_rec[1].y)/11;
        double rowdis=(id_rec[3].x-id_rec[2].x)/25;
        Point start=new Point();
        start=id_rec[2];
        for(int r=0;r<25;r++)

            for(int c=0;c<5;c++)
            {
                double height=start.y-(c+1)*coldis;
                double width=start.x+(r+1)*rowdis;


                for(int circle=0;circle<s;circle++)
                {
                    if(id_circles[circle].y<start.y-c*coldis&&id_circles[circle].y>height&&id_circles[circle].x>start.x+r*rowdis&&id_circles[circle].x<width) {
                        {
                            if (c == 0)
                                if(mc1to50[r]==' ')
                                    mc1to50[r] = 'A';
                                  else
                                if(mc1to50[r]=='A')
                                    mc1to50[r] = 'A';
                                 else
                                    mc1to50[r] = 'r';
                            if (c == 1)
                                if(mc1to50[r]==' ')
                                    mc1to50[r] = 'B';
                                else
                                if(mc1to50[r]=='B')mc1to50[r] = 'B';
                                else
                                    mc1to50[r] = 'r';
                            if (c == 2)
                                if(mc1to50[r]==' ')
                                    mc1to50[r] = 'C';
                                else
                                if(mc1to50[r]=='C')mc1to50[r] = 'C';
                                else
                                    mc1to50[r] = 'r';
                            if (c == 3)
                                if(mc1to50[r]==' ')
                                    mc1to50[r] = 'D';
                                else
                                if(mc1to50[r]=='D')mc1to50[r] = 'D';
                                else
                                    mc1to50[r] = 'r';
                            if (c == 4)
                                if(mc1to50[r]==' ')
                                    mc1to50[r] = 'E';
                                else
                                { if(mc1to50[r]=='E')mc1to50[r] = 'E';
                                else
                                    mc1to50[r] = 'r';}

                        }

                    }

                }}
        for(int r=0;r<25;r++)

            for(int c=6;c<11;c++)
            {
                double height=start.y-(c+1)*coldis;
                double width=start.x+(r+1)*rowdis;


                for(int circle=0;circle<s;circle++)
                {
                    if(id_circles[circle].y<start.y-c*coldis&&id_circles[circle].y>height&&id_circles[circle].x>start.x+r*rowdis&&id_circles[circle].x<width) {
                        {
                            if (c == 0)
                                if(mc1to50[r]==' ')
                                    mc1to50[r] = 'A';
                                else
                                if(mc1to50[r]=='A')mc1to50[r] = 'A';
                                else
                                    mc1to50[r] = 'r';
                            if (c == 1)
                                if(mc1to50[r]==' ')
                                    mc1to50[r] = 'B';
                                else
                                if(mc1to50[r]=='B')mc1to50[r] = 'B';
                                else
                                    mc1to50[r] = 'r';
                            if (c == 2)
                                if(mc1to50[r]==' ')
                                    mc1to50[r] = 'C';
                                else
                                if(mc1to50[r]=='C')mc1to50[r] = 'C';
                                else
                                    mc1to50[r] = 'r';
                            if (c == 3)
                                if(mc1to50[r]==' ')
                                    mc1to50[r] = 'D';
                                else
                                if(mc1to50[r]=='D')mc1to50[r] = 'D';
                                else
                                    mc1to50[r] = 'r';
                            if (c == 4)
                                if(mc1to50[r]==' ')
                                    mc1to50[r] = 'E';
                                else
                                if(mc1to50[r]=='E')mc1to50[r] = 'E';
                                else
                                    mc1to50[r] = 'r';

                        }
                    }

                }}
                s=50;
        for(int o=0;o<s;o++)
        {     int num=o+1;
            mc=mc+num+'_'+mc1to50[o];
            if(o!=s-1)mc+='_';}
        return mc;
    }
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
    private Point [] find_small_rec(Mat mat){
        ArrayList<MatOfPoint> contours = findContours2(mat);

        double s2=100000000000.0;
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

                if(area<s2&&area>100.0)
                {
                    c1=i;

                    s2=area;

                    pointsm=points;

                }
            }}








        return pointsm;
    }



public Mat numberOferrors(Mat m,Point [] max)
{



Imgproc.erode(m,m,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(15,15)));
    Imgproc.dilate(m,m,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(8,8)));
  Imgproc.medianBlur(m,m,  3);

            Mat circles = new Mat();
            Imgproc.HoughCircles(m, circles, Imgproc.CV_HOUGH_GRADIENT,
                    1, 20, 25, 10, 20,100 );
int i=0;
            if (circles.cols() > 0)
                for (int x = 0; x < circles.cols(); x++)
                {
                    double vCircle[] = circles.get(0,x);

                    if (vCircle == null)
                        break;

                    Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                    int radius = (int) Math.round(vCircle[2]);

                    if(!(pt.x>max[0].x&&pt.x<max[2].x&&pt.y>max[1].y&&pt.y<max[3].y)){
                        circle(m, pt, 3,new Scalar(0,255,0), -1, 8, 0 );
                    // draw the circle outline
                    circle( m,pt, radius, new Scalar(0,0,255), 3, 8, 0 );
                   i++;
                }}


    mMainActivity.pr(i);

return m;
}


    private Point [] find_id_rec(Mat mat){
        ArrayList<MatOfPoint> contours = findContours2(mat);
        double s=0.0;
int t=0;
        Rect r=new Rect();
       Point[] large = new Point[0];
        Mat doc=new Mat();
        Point[] pointsm=new Point[4];
        for (int i=0;i<contours.size();i++ ) {
            Imgproc.drawContours(mat, contours, i, new Scalar(255, 0, 0), 2);

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
                   r=  boundingRect(contours.get(i));
                   s=area;
                   t=i;
                   Point[] foundPoints = sortPoints(points);
pointsm=foundPoints;
               }
            }


        }


        Imgproc.drawContours(mat, contours, t, new Scalar(255, 0, 0), 2);

        return pointsm;
    }
    public Point[] find_id(Mat m,Point [] max)
    {
        Point top_left=max[0];
        Point top_right=max[1];
        Point bottom_right=max[2];
        Point bottom_left=max[3];
        Imgproc.medianBlur(m,m,  3);
Point [] id_stu_centers=new Point[8];
        Mat circles = new Mat();
        Imgproc.HoughCircles(m, circles, Imgproc.CV_HOUGH_GRADIENT,
                1, 20, 25, 10, 10,100 );
        int i=0;
        if (circles.cols() > 0)
            for (int x = 0; x < circles.cols(); x++)
            {
                double vCircle[] = circles.get(0,x);

                if (vCircle == null)
                    break;

                Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                int radius = (int) Math.round(vCircle[2]);

                if(pt.x>max[0].x&&pt.x<max[2].x&&pt.y>max[1].y&&pt.y<max[3].y){
                    circle(m, pt, 3,new Scalar(0,255,0), -1, 8, 0 );

                    circle( m,pt, radius, new Scalar(0,0,255), 3, 8, 0 );
                    id_stu_centers[i]=pt;//8 points for id
                    i++;
                }}
            for(int j=0;j<10;j++)
        mMainActivity.pr2(i);
        return id_stu_centers;
    }

public String student_id(Mat id_mat, Point id_circles[], Point id_rec[])
{

    String id_ID="";
    int id_array[]=new int[8];

    double coldis=(id_rec[2].y-id_rec[3].y)/8;
    double rowdis=(id_rec[0].x-id_rec[3].x)/10;
    Point start=new Point();
    start=id_rec[2];
    for(int c=0;c<8;c++)

        for(int r=0;r<10;r++)
        {
            double height=start.y-(c+1)*coldis;
            double width=start.x+(r+1)*rowdis;


            for(int circle=0;circle<8;circle++)
            {
                if(id_circles[circle].y<start.y-c*coldis&&id_circles[circle].y>height&&id_circles[circle].x>start.x+r*rowdis&&id_circles[circle].x<width)
                    id_array[c]=r;
            }

        }
        for(int o=0;o<8;o++)
        id_ID+=id_array[o];
    return id_ID;
}
    private HashMap<String,Long> pageHistory = new HashMap<>();

    private boolean checkQR(String qrCode) {

        return ! ( pageHistory.containsKey(qrCode) &&
                pageHistory.get(qrCode) > new Date().getTime()/1000-15) ;

    }
   private Mat subtracttwoimages(Mat im1,Mat im2)
   {
       Mat im3=new Mat();
       resize(im2,im3,im1.size());
       int width=im1.rows();
       int height=im1.cols();
       int ch=im1.channels();

       for(int i=0;i<width;i++)
       for(int j=0;j<height;j++)
       {
           double[] data1=im1.get(i,j);

           double[] data2=im3.get(i,j);
           for(int k=0;k<ch;k++)
           data1[k]=data1[k]-data2[k];
           im1.put(i,j,data1);
       }
       return  im1;
   }

private Mat readTrueAnswer()
{  float cx,cy;

    Mat trueanswer=new Mat();
    String photopath= Environment.getExternalStorageDirectory()+"/MarksReaderDoctors"+"/trueImage.jpg";
            Bitmap bitmap= BitmapFactory.decodeFile(photopath);

    Bitmap true2=bitmap.copy(Bitmap.Config.ARGB_8888,true);
    cx=true2.getWidth()/2;
    cy=true2.getHeight()/2;
   Matrix matrix=new Matrix();
 matrix.postRotate(270);
Bitmap true3= Bitmap.createBitmap(true2,0,0,true2.getWidth(),true2.getHeight(),matrix,true);
    Utils.bitmapToMat(true3,trueanswer);

    return trueanswer;

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
                    rescaledPoints[(i+2)%4] = new Point( abs(x- mPreviewSize.width), abs(y- mPreviewSize.height));
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
    private void drawDocumentBox2(Point[] points, Size stdSize) {

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
        paint.setColor(Color.argb(64, 0, 255, 0));

        Paint border = new Paint();
        border.setColor(Color.rgb(0, 255, 0));
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
                    pointsquad = foundPoints;
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
          //  Imgproc.cvtColor(src,src,Imgproc.COLOR_RGBA2GRAY);
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

        double widthA = Math.sqrt(pow(br.x - bl.x, 2) + pow(br.y - bl.y, 2));
        double widthB = Math.sqrt(pow(tr.x - tl.x, 2) + pow(tr.y - tl.y, 2));

        double dw = Math.max(widthA, widthB)*ratio;
        int maxWidth = Double.valueOf(dw).intValue();


        double heightA = Math.sqrt(pow(tr.x - br.x, 2) + pow(tr.y - br.y, 2));
        double heightB = Math.sqrt(pow(tl.x - bl.x, 2) + pow(tl.y - bl.y, 2));

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
    private ArrayList<MatOfPoint> findContours2(Mat src) {

        Mat grayImage = null;
        Mat cannedImage = null;



        grayImage = new Mat();
        cannedImage = new Mat();


        Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
        GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
        Imgproc.Canny(grayImage, cannedImage, 10, 100);
      /*  Imgproc.dilate(cannedImage,cannedImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
        Imgproc.erode(cannedImage,cannedImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));*/
       ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannedImage, contours,hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        hierarchy.release();

        grayImage.release();
        cannedImage.release();

        return contours;}
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
        GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
        Imgproc.Canny(grayImage, cannedImage, 75, 200);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannedImage, contours,hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

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
