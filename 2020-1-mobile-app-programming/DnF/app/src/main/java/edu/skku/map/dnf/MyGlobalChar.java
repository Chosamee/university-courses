package edu.skku.map.dnf;

public class MyGlobalChar {

    private CharacterInfo data;
    public CharacterInfo getData()
    {
        return data;
    }

    public void setData(CharacterInfo data)
    {
        this.data = data;
    }

    private static MyGlobalChar instance = null;

    public static synchronized MyGlobalChar getInstance(){
        if(null == instance){
            instance = new MyGlobalChar();
        }
        return instance;
    }

}