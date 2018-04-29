package com.example.hanan.marksreader;

/**
 * Created by hanan on 04/03/2018.
 */
public class person {
    private String ID;
    private String Mark;

    public person(String ID, String Mark) {
        this.ID = ID;
        this.Mark =Mark;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID= ID;
    }

    public String getMark() {
        return Mark;
    }

    public void setMark(String Mark) {
        this.Mark = Mark;
    }

}