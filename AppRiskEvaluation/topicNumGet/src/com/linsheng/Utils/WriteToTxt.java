package com.linsheng.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class WriteToTxt {
    public static void writeTxtFile(String str,String f)throws Exception{
        FileOutputStream fos = null;
        try {
            fos=new FileOutputStream(f,true);
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PrintStream p=new PrintStream(fos);
        //p.println(str);
        p.print(str);
        p.close();
        try {
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
