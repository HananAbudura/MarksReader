package com.example.hanan.marksreader;

/**
 * Created by hanan on 03/10/2018.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Markstables";
    private static final int DATABASE_VERSION = 1;
    private HashMap hp;
    public String table_name = "students";
    public String table_name2 = "multichoicedetails";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + table_name +
                        "(id integer primary key, Student_ID text, Student_Mark text, Student_Name text)"

        );
        db.execSQL(
                "CREATE TABLE " + table_name2 +
                        "(id integer primary key, Student_ID text, a1 text, a2 text, a3 text, a4 text, a5 text, a6 text, a7 text, a8 text, a9 text, a10 text, a11 text, a12 text, a13 text, a14 text, a15 text, a16 text, a17 text, a18 text, a19 text, a20 text, a21 text, a22 text, a23 text, a24 text, a25 text, a26 text, a27 text, a28 text, a29 text, a30 text, a31 text, a32 text, a33 text, a34 text, a35 text, a36 text, a37 text, a38 text, a39 text, a40 text, a41 text, a42 text, a43 text, a44 text, a45 text, a46 text, a47 text, a48 text, a49 text, a50 text)"
        );

    }

public void deleteData()
{
    SQLiteDatabase db1 = this.getWritableDatabase();
    db1.execSQL("delete from "+table_name);

}
    public void deleteDatat(String tablename)
    {
        SQLiteDatabase db1 = this.getWritableDatabase();
        db1.execSQL("delete from "+tablename);

    }
    public void deleteData2()
    {
        SQLiteDatabase db1 = this.getWritableDatabase();
        db1.execSQL("delete from "+table_name2);
    }
    public void deleteData3()
    { String d="checker";
        SQLiteDatabase db1 = this.getWritableDatabase();
        db1.execSQL("delete from "+d);
    }
    public void insertData(String id, int mark, String name )//take two strings id and mark
    {
        SQLiteDatabase db1 = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Student_ID", id);
        contentValues.put("Student_Mark", +mark);
        contentValues.put("Student_Name", name);
        db1.insert(table_name, null, contentValues);
    }
    public void insertData3(String id, String name, String tablename )//take two strings id and mark
    {
        SQLiteDatabase db1 = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Student_ID", id);
        contentValues.put("Student_Name", name);
        db1.insert(tablename, null, contentValues);
    }
    public void insertData4(String cn, String cn1, String nq )//take two strings id and mark
    {
        SQLiteDatabase db1 = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("coursename", cn);
        contentValues.put("classnumber", cn1);
        contentValues.put("nq", nq);
        db1.insert("checker", null, contentValues);
    }
    public void insertData2(String id, String a1, String a2, String a3, String a4, String a5, String a6, String a7, String a8, String a9, String a10, String a11, String a12, String a13, String a14, String a15, String a16, String a17, String a18, String a19, String a20, String a21, String a22, String a23, String a24, String a25, String a26, String a27, String a28, String a29, String a30, String a31, String a32, String a33, String a34, String a35, String a36, String a37, String a38, String a39, String a40, String a41, String a42, String a43, String a44, String a45, String a46, String a47, String a48, String a49, String a50)//take two strings id and mark
    {
        SQLiteDatabase db1 = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Student_ID", id);
        contentValues.put("a1",a1);
        contentValues.put("a2",a2);
        contentValues.put("a3",a3);
        contentValues.put("a4",a4);
        contentValues.put("a5",a5);
        contentValues.put("a6",a6);
        contentValues.put("a7",a7);
        contentValues.put("a8",a8);
        contentValues.put("a9",a9);
        contentValues.put("a10",a10);
        contentValues.put("a11",a11);
        contentValues.put("a12",a12);
        contentValues.put("a13",a13);
        contentValues.put("a14",a14);
        contentValues.put("a15",a15);
        contentValues.put("a16",a16);
        contentValues.put("a17",a17);
        contentValues.put("a18",a18);
        contentValues.put("a19",a19);
        contentValues.put("a20",a20);
        contentValues.put("a21",a21);
        contentValues.put("a22",a22);
        contentValues.put("a23",a23);
        contentValues.put("a24",a24);
        contentValues.put("a25",a25);
        contentValues.put("a26",a26);
        contentValues.put("a27",a27);
        contentValues.put("a28",a28);
        contentValues.put("a29",a29);
        contentValues.put("a30",a30);
        contentValues.put("a31",a31);
        contentValues.put("a32",a32);
        contentValues.put("a33",a33);
        contentValues.put("a34",a34);
        contentValues.put("a35",a35);
        contentValues.put("a36",a36);
        contentValues.put("a37",a37);
        contentValues.put("a38",a38);
        contentValues.put("a39",a39);
        contentValues.put("a40",a40);
        contentValues.put("a41",a41);
        contentValues.put("a42",a42);
        contentValues.put("a43",a43);
        contentValues.put("a44",a44);
        contentValues.put("a45",a45);
        contentValues.put("a46",a46);
        contentValues.put("a47",a47);
        contentValues.put("a48",a48);
        contentValues.put("a49",a49);
        contentValues.put("a50",a50);
        db1.insert(table_name2, null, contentValues);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + table_name);
        db.execSQL("DROP TABLE IF EXISTS " + table_name2);
    }
public void creattable(String tablename)
{
    SQLiteDatabase db = this.getReadableDatabase();
    db.execSQL(
            "CREATE TABLE " +tablename+
                    "(id integer primary key, Student_ID text, Student_Name text)"

    );
}

    public Cursor getuser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + table_name + " ",
                null);
        return res;
    }
    public Cursor getuser2() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res2 = db.rawQuery("select * from " + table_name2 + " ",
                null);
        return res2;
    }
    public Cursor getuser3(String tablename) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + tablename + " ",
                null);
        return res;
    }
    public Cursor getuser4() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + "checker" + " ",
                null);
        return res;
    }
}

