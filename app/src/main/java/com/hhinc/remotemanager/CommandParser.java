package com.hhinc.remotemanager;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
/**
 * Created by GKX100212 on 2017/9/11.
 *
 */

class CommandParser {
    private static final String TAG="CommandParser";
    static void parseCommand(OutputStream outputStream,byte[] bytes)
        throws IOException{
        if(bytes[0]==0x08){
            parseRequestCommand(outputStream,bytes);
        }
    }

    private static void parseRequestCommand(OutputStream outputStream,byte[] bytes)
            throws IOException{
        if(bytes[1] == (0x08|0x01)){
            Log.d(TAG,"request file list");
            int size = (bytes[2]|bytes[3]<<8);//get size??
            String path = "test";
            requestFileList(outputStream,bytes,path);
        }else if(bytes[1]==(0x08|0x02)){
            Log.d(TAG,"request pull file");
        }
    }

    private static void requestFileList(OutputStream outputStream,byte[] bytes,String path)
            throws IOException{
        responseFileStartCommand(outputStream,bytes);
        responseFileCommand(outputStream,bytes,path);
        responseFileEndCommand(outputStream,bytes);
    }

    private static void responseFileStartCommand(OutputStream outputStreams,
                                         byte[] bytes) throws IOException{
        bytes[0] = 0x70;
        bytes[1] = 0x70|0x01;
        outputStreams.write(bytes);
    }

    private static void responseFileEndCommand(OutputStream outputStreams,
                                       byte[] bytes) throws IOException{
        bytes[0] = 0x70;
        bytes[1] = 0x70|0x02;
        outputStreams.write(bytes);
    }

    private static void responseFileCommand(OutputStream outputStream,
                                    byte[] bytes,
                                    String absolutePath) throws IOException{
        File root = new File(absolutePath);
        if(!root.exists()||!root.isDirectory()){
            return;
        }
        File[] files = root.listFiles();
        if(files==null){
            return;
        }
        for(File file:files){
            String name = file.getName();
            String path = file.getAbsolutePath();
            bytes[0] = 0x70;
            if(file.isFile()){
                bytes[1] = 0x70|0x03;
            }else{
                bytes[1] = 0x70|0x04;
            }
            bytes[2] = (byte)(name.getBytes().length&0xff);
            bytes[3] = (byte)((name.getBytes().length>>8)&0xff);
            bytes[4] = (byte)(path.getBytes().length&0xff);
            bytes[5] = (byte)((path.getBytes().length>>8)&0xff);
            System.arraycopy(name.getBytes(),0,bytes,
                    7,name.getBytes().length);
            System.arraycopy(absolutePath.getBytes()
                    ,0
                    ,bytes
                    ,7+name.getBytes().length
                    ,path.getBytes().length);
            outputStream.write(bytes);
        }
    }
}
