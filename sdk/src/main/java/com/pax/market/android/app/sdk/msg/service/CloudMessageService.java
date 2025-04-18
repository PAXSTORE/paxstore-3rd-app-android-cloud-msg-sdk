package com.pax.market.android.app.sdk.msg.service;

import static com.pax.market.android.app.sdk.msg.dto.PushConstants.ACTION_DATA_MESSAGE_RECEIVED;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.ACTION_NOTIFICATION_MESSAGE_RECEIVED;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.ACTION_NOTIFY_DATA_MESSAGE_RECEIVED;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.ACTION_NOTIFY_MEDIA_MESSAGE_RECEIVED;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.EXTRA_MEIDA;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.EXTRA_MESSAGE_CONTENT;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.EXTRA_MESSAGE_DATA;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.EXTRA_MESSAGE_NID;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.EXTRA_MESSAGE_TITLE;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.MEDIA_MESSAGE;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.PUSH_MESSAGE;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.PUSH_MESSAGE_ID;
import static com.pax.market.android.app.sdk.msg.dto.PushConstants.PUSH_MESSAGE_TYPE;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pax.market.android.app.sdk.msg.dto.CloudMessage;
import com.pax.market.android.app.sdk.msg.dto.MediaMesageInfo;
import com.pax.market.android.app.sdk.msg.utils.CloudMsgCryptoUtils;
import com.pax.market.android.app.sdk.msg.utils.NotificationUtils;
import com.pax.market.android.app.sdk.msg.utils.Notifications;
import com.pax.market.android.app.sdk.msg.utils.PreferencesUtils;
import com.pax.market.api.sdk.java.api.sync.CloudMessageApi;
import com.pax.market.api.sdk.java.base.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fojut on 2019/5/17.
 */
public class CloudMessageService extends IntentService {
    private static final String TAG = CloudMessageService.class.getSimpleName();
    private static final Logger logger = LoggerFactory.getLogger(CloudMessageService.class);


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public CloudMessageService(String name) {
        super(name);
    }

    public CloudMessageService() {
        super(TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationUtils.showForeGround(this, "Cloud message");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(PUSH_MESSAGE)) {
            String msgId = intent.getStringExtra(PUSH_MESSAGE_ID);
            int msgType = intent.getIntExtra(PUSH_MESSAGE_TYPE, 0);
            logger.debug(">>> Received new CloudMessage form STORE client. msgId=%s, msgTpye=%d", msgId, msgType);
            String jsonString = decrypt(intent.getStringExtra(PUSH_MESSAGE));
            CloudMessage cloudMessage = CloudMessage.fromJson(jsonString);
            if (cloudMessage != null) {
                Intent messageIntent = new Intent();
                switch (msgType) {
                    case 1:
                        messageIntent.setAction(ACTION_NOTIFICATION_MESSAGE_RECEIVED);
                        break;
                    case 3:
                        messageIntent.setAction(ACTION_NOTIFY_DATA_MESSAGE_RECEIVED);
                        break;
                    case 4:
                        messageIntent.setAction(ACTION_NOTIFY_MEDIA_MESSAGE_RECEIVED);
                        break;
                    default:
                        messageIntent.setAction(ACTION_DATA_MESSAGE_RECEIVED);
                        break;
                }
                messageIntent.putExtra(PUSH_MESSAGE_ID, msgId);
                messageIntent.putExtra(PUSH_MESSAGE_TYPE, msgType);

                if (cloudMessage.getNotification() != null) {
                    messageIntent.putExtra(EXTRA_MESSAGE_NID, cloudMessage.getNotification().getNid());
                    messageIntent.putExtra(EXTRA_MESSAGE_TITLE, cloudMessage.getNotification().getTitle());
                    messageIntent.putExtra(EXTRA_MESSAGE_CONTENT, cloudMessage.getNotification().getContent());
                    if (Notifications.I.getEnabled()) { // default is true
                        if (!Notifications.I.hasInit()) {
                            Notifications.I.init(getApplicationContext());
                        }
                        Notifications.I.notify(cloudMessage.getNotification(), cloudMessage.getDataJson());
                    }
                }
                if (!cloudMessage.isDataEmpty()) {
                    messageIntent.putExtra(EXTRA_MESSAGE_DATA, cloudMessage.getDataJson());
                }
                if (msgType == 4) {
                    messageIntent.putExtra(EXTRA_MEIDA, cloudMessage.getMediaJson());
                    saveMediaMessage(cloudMessage);
                }
                messageIntent.setPackage(getPackageName());
                messageIntent.addCategory(getPackageName());
                sendBroadcast(messageIntent);
            }
        }
    }

    private void saveMediaMessage(CloudMessage cloudMessage) {
        Log.e(TAG, "Add new media message: " + cloudMessage.toString());
        Gson gson = new GsonBuilder().create();
        MediaMesageInfo mediaMesageInfo = gson.fromJson(cloudMessage.getMediaJson(), MediaMesageInfo.class);
        PreferencesUtils.putObject(getApplicationContext(), MEDIA_MESSAGE, mediaMesageInfo);
    }

    private String decrypt(String encryptedData) {
        if (StringUtils.isEmpty(encryptedData)) {
            logger.error("encryptedData > no data");
            return null;
        }

        int maxRetries = 10;
        int retryDelay = 1000;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            if (CloudMessageApi.getInstance() == null || CloudMessageApi.getInstance().getAppSecret() == null) {
                logger.warn("AppSecret is null, retrying in " + retryDelay + " ms...");
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException e) {
                    return null;
                }
                retryCount++;

            } else {
                return CloudMsgCryptoUtils.aesDecrypt(encryptedData, CloudMessageApi.getInstance().getAppSecret());
            }
        }
        logger.error("Store sdk not initialized");
        return null;
    }
}
