package com.hhinc.remotemanager;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static com.hhinc.remotemanager.MyApplication.INFO_REQUEST;
import static com.hhinc.remotemanager.MyApplication.INFO_REQUEST_FILE_LIST;
import static com.hhinc.remotemanager.MyApplication.INFO_REQUEST_META_FILE;
import static com.hhinc.remotemanager.MyApplication.INFO_RESPONSE;
import static com.hhinc.remotemanager.MyApplication.INFO_RESPONSE_FILE_END;
import static com.hhinc.remotemanager.MyApplication.INFO_RESPONSE_FILE_START;
import static com.hhinc.remotemanager.MyApplication.INFO_RESPONSE_FILE_TYPE_DIR;
import static com.hhinc.remotemanager.MyApplication.INFO_RESPONSE_FILE_TYPE_FILE;
import static com.hhinc.remotemanager.MyApplication.INFO_RESPONSE_SLAVE;
import static com.hhinc.remotemanager.MyApplication.INFO_RESPONSE_SLAVE_END;
import static com.hhinc.remotemanager.MyApplication.INFO_RESPONSE_SLAVE_START;

/**
 * Created by GKX100212 on 2017/9/11.
 *
 */

class CommandParser {
    private static final String TAG="CommandParser";

    private static CommandParser commandParser;

    static public CommandParser getInstance(){
        if(commandParser == null){
            commandParser = new CommandParser();
        }
        return commandParser;
    }

    void parseCommand(OutputStream outputStream,byte[] bytes)
        throws IOException{
        if(bytes[0]==INFO_REQUEST){//request command
            parseRequestCommand(outputStream,bytes);
        }else if(bytes[0]==INFO_RESPONSE){
            parseResponseCommand(bytes);
        }
    }

    private void parseResponseCommand(byte[] bytes){
        if(bytes[1] == (INFO_RESPONSE_SLAVE_START)){
            //slave start
            Log.d(TAG,"slave start");
        }else if(bytes[1] == (INFO_RESPONSE_SLAVE_END)){
            Log.d(TAG,"slave end");
        }else if(bytes[1] == (INFO_RESPONSE_SLAVE)){
            Log.d(TAG,"slave");
        }
    }

    private void parseRequestCommand(OutputStream outputStream,byte[] bytes)
            throws IOException{
        if(bytes[1] == (INFO_REQUEST_FILE_LIST)){
            Log.d(TAG,"request file list");
            int size = (bytes[2]|bytes[3]<<8);//get size??
            String path = "test";
            responseFileList(outputStream,bytes,path);
        }else if(bytes[1]==INFO_REQUEST_META_FILE){
            Log.d(TAG,"request pull file");
        }
    }

    private void responseFileList(OutputStream outputStream,byte[] bytes,String path)
            throws IOException{
        responseFileStartCommand(outputStream,bytes);
        responseFileCommand(outputStream,bytes,path);
        responseFileEndCommand(outputStream,bytes);
    }

    private void responseFileStartCommand(OutputStream outputStreams,
                                         byte[] bytes) throws IOException{
        bytes[0] = INFO_RESPONSE;
        bytes[1] = INFO_RESPONSE_FILE_START;
        outputStreams.write(bytes);
    }

    private void responseFileEndCommand(OutputStream outputStreams,
                                       byte[] bytes) throws IOException{
        bytes[0] = INFO_RESPONSE;
        bytes[1] = INFO_RESPONSE_FILE_END;
        outputStreams.write(bytes);
    }

    private void responseFileCommand(OutputStream outputStream,
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
            bytes[0] = INFO_RESPONSE;
            if(file.isFile()){
                bytes[1] = INFO_RESPONSE_FILE_TYPE_FILE;
            }else{
                bytes[1] = INFO_RESPONSE_FILE_TYPE_DIR;
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
