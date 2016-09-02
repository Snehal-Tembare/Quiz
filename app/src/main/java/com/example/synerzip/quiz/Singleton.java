package com.example.synerzip.quiz;

/**
 * Created by synerzip on 25/7/16.
 */
public class Singleton {

    private static Singleton instance =null;
    private boolean CFlag;
    private boolean CppFlag;
    private boolean JavaFlag;
    private boolean AndroidFlag;
    private boolean JavaScriptFlag;

    public static Singleton getInstance() {
        if (instance ==null){
            instance =new Singleton();
        }
        return instance;
    }

    private Singleton(){
        CFlag =false;
        CppFlag =false;
        JavaFlag =false;
        AndroidFlag =false;
        JavaScriptFlag =false;
    }

    public boolean isCFlag() {
        return CFlag;
    }

    public void setCFlag(boolean CFlag) {
        this.CFlag = CFlag;
    }

    public boolean isCppFlag() {
        return CppFlag;
    }

    public void setCppFlag(boolean cppFlag) {
        this.CppFlag = cppFlag;
    }

    public boolean isJavaFlag() {
        return JavaFlag;
    }

    public void setJavaFlag(boolean javaFlag) {
        this.JavaFlag = javaFlag;
    }

    public boolean isAndroidFlag() {
        return AndroidFlag;
    }

    public void setAndroidFlag(boolean androidFlag) {
        this.AndroidFlag = androidFlag;
    }

    public boolean isJavaScriptFlag() {
        return JavaScriptFlag;
    }

    public void setJavaScriptFlag(boolean javaScriptFlag) {
        this.JavaScriptFlag = javaScriptFlag;
    }









}
