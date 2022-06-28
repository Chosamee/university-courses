package edu.skku.map.dnf;

public class MyGlobals {

    private LoginUser data;
    public LoginUser getData()
    {
        return data;
    }

    public void setData(LoginUser data)
    {
        this.data = data;
    }

    private static MyGlobals instance = null;

    public static synchronized MyGlobals getInstance(){
        if(null == instance){
            instance = new MyGlobals();
        }
        return instance;
    }

}