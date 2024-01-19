package com.pax.android.demoapp.cloudmsg;

import android.app.Application;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.pax.market.android.app.sdk.BaseApiService;
import com.pax.market.android.app.sdk.Notifications;
import com.pax.market.android.app.sdk.StoreSdk;


/**
 * Created by fojut on 2017/8/24.
 */

public class BaseApplication extends Application {

    private static final String TAG = BaseApplication.class.getSimpleName();
    //todo make sure to replace with your own app's appkey and appsecret
    private static final String appkey = "MUHAKKHUAL59D15EKVIV";
    private static final String appSecret = "7JLCWEIITIFTYPLTLRGN32ZI1BCZ4L6V9U6XL30D";
    private boolean isReadyToUpdate = true;

    @Override
    public void onCreate() {
        super.onCreate();
        //initial the SDK
        initStoreSdk();

    }

    private void initStoreSdk() {
        //todo 1. Init AppKey，AppSecret and SN, make sure the appkey and appSecret is corret.
        StoreSdk.getInstance().init(getApplicationContext(), appkey, appSecret, new BaseApiService.Callback() {
            @Override
            public void initSuccess() {
                Log.i(TAG, "initSuccess.");
                initInquirer();
            }

            @Override
            public void initFailed(RemoteException e) {
                Log.i(TAG, "initFailed: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Cannot get API URL from STORE client," +
                        " Please install STORE client first.", Toast.LENGTH_LONG).show();
            }
        });
        //if you want to customize the notification for Cloud Message.
        Notifications.I.init(getApplicationContext())
                .setSmallIcon(R.drawable.logo_white)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.logo_demo_ext));
    }


    private void initInquirer() {

        //todo 2. Init checking of whether app can be updated
        StoreSdk.getInstance().initInquirer(new StoreSdk.Inquirer() {
            @Override
            public boolean isReadyUpdate() {
                Log.i(TAG, "call business function....isReadyUpdate = " + isReadyToUpdate);
                //todo call your business function here while is ready to update or not
                return isReadyToUpdate;
            }
        });
    }

    public boolean isReadyToUpdate() {
        return isReadyToUpdate;
    }

    public void setReadyToUpdate(boolean readyToUpdate) {
        isReadyToUpdate = readyToUpdate;
        if (isReadyToUpdate) {
            Toast.makeText(getApplicationContext(), getString(R.string.label_ready_to_update), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.label_not_ready_to_update), Toast.LENGTH_SHORT).show();
        }
    }
}