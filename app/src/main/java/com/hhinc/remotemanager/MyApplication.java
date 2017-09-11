package com.hhinc.remotemanager;

import android.app.Application;

/**
 * Created by GKX100212 on 2017/9/11.
 *
 */

public class MyApplication extends Application {
    public static final byte INFO_CLIENT_TYPE=0;
    public static final byte INFO_CLIENT_TYPE_MASTER=(INFO_CLIENT_TYPE|0x01);
    public static final byte INFO_CLIENT_TYPE_SLAVE=(INFO_CLIENT_TYPE|0x02);

    public static final byte INFO_REQUEST = 0x08;
    public static final byte INFO_REQUEST_FILE_LIST = (INFO_REQUEST|0x01);
    public static final byte INFO_REQUEST_META_FILE = (INFO_REQUEST|0X02);

    public static final byte INFO_RESPONSE = 0x70;
    public static final byte INFO_RESPONSE_FILE_START = (INFO_RESPONSE|0x01);
    public static final byte INFO_RESPONSE_FILE_END = (INFO_RESPONSE|0x02);
    public static final byte INFO_RESPONSE_FILE_TYPE_FILE=(INFO_RESPONSE|0x03);
    public static final byte INFO_RESPONSE_FILE_TYPE_DIR=(INFO_RESPONSE|0x04);
    public static final byte INFO_RESPONSE_SLAVE_START = (INFO_RESPONSE|0x05);
    public static final byte INFO_RESPONSE_SLAVE_END = (INFO_RESPONSE|0X06);
    public static final byte INFO_RESPONSE_SLAVE = (INFO_RESPONSE|0X07);

    private int mClientType = INFO_CLIENT_TYPE_MASTER;

    public void setClientType(int type){
        mClientType = type;
    }

    public int getClientType(){
        return mClientType;
    }
}
