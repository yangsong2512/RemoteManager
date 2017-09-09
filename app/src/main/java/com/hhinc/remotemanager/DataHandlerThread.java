package com.hhinc.remotemanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.util.Log;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Array;
import java.sql.Time;
import java.util.concurrent.TimeoutException;

/**
 * Created by GKX100212 on 2017/9/9.
 *
 */

class DataHandlerThread extends Thread {
    private String TAG="DataHandlerThread";
    private final Object mLock = new Object();
    private Context mContext;
    private boolean mQuit = false;
    private File mRoot = null;
    DataHandlerThread(Context context){
        mContext = context;
    }

    private boolean isWifiConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo.getState()== NetworkInfo.State.CONNECTED){
            if(networkInfo.getType()==ConnectivityManager.TYPE_WIFI){
                return true;
            }
        }
        return false;
    }

    private void connect2Server(){
        Socket socket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        int ret = 0;
        try{
            socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress("69.171.73.60",8901);
            socket.connect(socketAddress,5000);
            if(socket.isConnected()){
                byte[] buffer = new byte[512];
                outputStream = socket.getOutputStream();

                buffer[0] = 0;//client info
                buffer[1] = 0;//client is a master
                outputStream.write(buffer);
            }
            //always check wifi state
            while(socket.isConnected()){
                byte[] buffer = new byte[512];
                if(inputStream == null){
                    inputStream = socket.getInputStream();
                }
                if(outputStream == null){
                    outputStream = socket.getOutputStream();
                }

                ret = inputStream.read(buffer);
                if(ret == 0){
                    Log.d(TAG,"closed?");
                }else{
                    switch (buffer  [0]){
                        case (0x08)://type request
                            if(buffer[1] == (0x08|0x01)){
                                if(mRoot == null){
                                    mRoot = Environment.getExternalStorageDirectory();
                                }

                                if(mRoot.isDirectory()){
                                    File[] files = mRoot.listFiles();
                                    if(files == null){
                                        break;
                                    }
                                    buffer[0] = (0x70);
                                    buffer[1] = (0x70|0x01);
                                    outputStream.write(buffer);
                                    for(File file:files){
                                        String name = file.getName();
                                        String absolutePath = file.getAbsolutePath();
                                        if(file.isDirectory()){
                                            buffer[1] = (0x70|0x03);
                                        }else{
                                            buffer[1] = (0x70|0x04);
                                        }
                                        buffer[2] = (byte)(name.getBytes().length&0xff);
                                        buffer[3] = (byte)((name.getBytes().length>>8)&0xff);
                                        buffer[4] = (byte)(absolutePath.getBytes().length&0xff);
                                        buffer[5] = (byte)((absolutePath.getBytes().length>>8)&0xff);
                                        System.arraycopy(name.getBytes(),0,buffer,
                                                7,name.getBytes().length);
                                        System.arraycopy(absolutePath.getBytes()
                                                ,0
                                                ,buffer
                                                ,7+name.getBytes().length
                                                ,absolutePath.getBytes().length);
                                        outputStream.write(buffer);
                                    }
                                    buffer[0] = (0x70);
                                    buffer[1] = (0x70|0x02);
                                    outputStream.write(buffer);
                                }
                            }else if(buffer[1] == (0x08|0x02)){
                                //pull file
                            }else if(buffer[1] == (0x08|0x03)){
                                //change file dir
                                String file = "";
                                mRoot = new File(file);
                            }

                            break;
                    }
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void disconnectFromServer(){

    }

    @Override
    public void run() {
        super.run();
        while(!mQuit){
            if(isWifiConnected()){
                Log.d(TAG,"connected");
                connect2Server();
            }else{
                try{
                    mLock.wait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    void onWifiEnabled(){
        Log.d(TAG,"wifi enabled");
    }

    void onWifiConnected(){
        Log.d(TAG,"wifi connected");
        synchronized (mLock){
            mLock.notifyAll();
        }
    }

    void doQuit(){
        mQuit = true;
    }

    void onWifiDisconnected(){
        Log.d(TAG,"WIFI disconnected");
    }
}
