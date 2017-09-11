package com.hhinc.remotemanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static com.hhinc.remotemanager.MyApplication.INFO_CLIENT_TYPE_MASTER;

/**
 * Created by GKX100212 on 2017/9/9.
 *
 */

class DataHandlerThread extends Thread {
    private String TAG="DataHandlerThread";
    private final Object mLock = new Object();
    private Context mContext;
    private boolean mQuit = false;
    private Socket mSocket;
    private SocketAddress mSocketAddress;
    private int mClientType = 0;
    DataHandlerThread(Context context){
        mContext = context;
    }

    private boolean isWifiConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo.getState()== NetworkInfo.State.CONNECTED){
            if(networkInfo.getType()==ConnectivityManager.TYPE_WIFI){
                Log.d(TAG,"Active network is under wifi");
                return true;
            }else if(networkInfo.getType()==ConnectivityManager.TYPE_ETHERNET){
                Log.d(TAG,"Active network is under ethernet");
                return true;
            }else if(networkInfo.getType()==ConnectivityManager.TYPE_MOBILE){
                return getMobileNetworkType();
            }
        }
        return false;
    }

    private boolean getMobileNetworkType(){
        TelephonyManager telephonyManager = (TelephonyManager)mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
            Log.d(TAG,"NETWORK_TYPE_LTE");
        }else if(telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE){
            Log.d(TAG,"NETWORK_TYPE_EDGE");
        }
        return false;
    }

    private void tryConnect2Server(){
        if(mSocket == null){
            mSocket = new Socket();
        }
        if(mSocketAddress == null){
            mSocketAddress = new InetSocketAddress("69.171.73.60",8901);
        }
        try {
            OutputStream outputStream;
            mSocket.connect(mSocketAddress,5000);
            if(mSocket.isConnected()){
                byte[] buffer = new byte[512];
                outputStream = mSocket.getOutputStream();

                buffer[0] = INFO_CLIENT_TYPE_MASTER;//client info
                mClientType = buffer[1] = 0;//0,client is a master,1,client is a slave
                outputStream.write(buffer);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void readFromServer(){

        InputStream inputStream = null;
        OutputStream outputStream = null;
        int ret;
        try{
            //always check wifi state
            while(mSocket.isConnected()){
                byte[] buffer = new byte[512];
                if(inputStream == null){
                    inputStream = mSocket.getInputStream();
                }
                if(outputStream == null){
                    outputStream = mSocket.getOutputStream();
                }

                ret = inputStream.read(buffer);
                if(ret == 0){
                    Log.d(TAG,"closed?");
                    break;
                }else{
                    CommandParser.getInstance().parseCommand(outputStream,buffer);
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void disconnectFromServer(){

    }

    private void writeSomething(){
        if(mSocket.isConnected()){
            try{
                OutputStream outputStream = mSocket.getOutputStream();
                outputStream.write("hell world".getBytes());
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        super.run();
        while(!mQuit){
            if(isWifiConnected()){
                Log.d(TAG,"connected");
                tryConnect2Server();
                readFromServer();
            }else{
                synchronized (mLock){
                    try{
                        mLock.wait();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        if(mSocket.isConnected()){
            try{
                if(!mSocket.isInputShutdown()){
                    mSocket.getInputStream().close();
                }
                if(!mSocket.isOutputShutdown()){
                    mSocket.getOutputStream().close();
                }
                if(!mSocket.isClosed()){
                    mSocket.close();
                }
            }catch (IOException e){
                e.printStackTrace();
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
