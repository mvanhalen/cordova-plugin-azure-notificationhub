package msopentech.azure;

import java.util.Set;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.media.RingtoneManager;
import android.graphics.Color;
import android.net.Uri;
import com.google.android.gms.gcm.*;
import com.microsoft.windowsazure.messaging.*;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;

import com.microsoft.windowsazure.notifications.NotificationsHandler;

import com.microsoft.windowsazure.notifications.NotificationsManager;

/**
 * Apache Cordova plugin for Windows Azure Notification Hub
 */
public class NotificationHub extends CordovaPlugin {

    /**
     * The callback context from which we were invoked.
     */
    protected static CallbackContext _callbackContext = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        _callbackContext = callbackContext;
        try {

          if (action.equals("registerApplication")) {
                  String hubName = args.getString(0);
                  String connectionString = args.getString(1);
                  String userId = args.getString(3);
                  String senderId = args.getString(4);
                  registerApplication(hubName, connectionString, senderId,userId);
                  return true;
          }

            if (action.equals("unregisterApplication")) {
                String hubName = args.getString(0);
                String connectionString = args.getString(1);
                unregisterApplication(hubName, connectionString);
                return true;
            }

            return false; // invalid action
        } catch (Exception e) {
            _callbackContext.error(e.getMessage());
        }
        return true;
    }

    /**
     * Asynchronously registers the device for native notifications.
     */
    @SuppressWarnings("unchecked")
    private void registerApplication(final String hubName, final String connectionString, final String senderId,final String userId) {

        try {
            final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(cordova.getActivity());
            final com.microsoft.windowsazure.messaging.NotificationHub hub =
                    new com.microsoft.windowsazure.messaging.NotificationHub(hubName, connectionString, cordova.getActivity());

                    NotificationsManager.handleNotifications(cordova.getActivity(), senderId, PushNotificationReceiver.class);

            new AsyncTask() {
                @Override
                protected Object doInBackground(Object... params) {
                    try {

                      String[] tags = {userId};
                      String regid = gcm.register(senderId);
                      Registration registrationInfo = hub.register(regid);


                      JSONObject registrationResult = new JSONObject();
                      registrationResult.put("registrationId", registrationInfo.getRegistrationId());
                      //registrationResult.put("channelUri", registrationInfo.getGCMRegistrationId());
                      //registrationResult.put("notificationHubPath", registrationInfo.getNotificationHubPath());
                      registrationResult.put("event", "registerApplication");

                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, registrationResult);
                        // keepKallback is used to continue using the same callback to notify about push notifications received
                        pluginResult.setKeepCallback(true);

                        NotificationHub.getCallbackContext().sendPluginResult(pluginResult);

                    } catch (Exception e) {
                        NotificationHub.getCallbackContext().error(e.getMessage());
                    }
                    return null;
                }
            }.execute(null, null, null);
        } catch (Exception e) {
            NotificationHub.getCallbackContext().error(e.getMessage());
        }
    }

    /**
     * Unregisters the device for native notifications.
     */
    private void unregisterApplication(final String hubName, final String connectionString) {
        try {
            final com.microsoft.windowsazure.messaging.NotificationHub hub =
                    new com.microsoft.windowsazure.messaging.NotificationHub(hubName, connectionString, cordova.getActivity());
            hub.unregister();
            NotificationHub.getCallbackContext().success();
        } catch (Exception e) {
            NotificationHub.getCallbackContext().error(e.getMessage());
        }
    }



    /**
     * Handles push notifications received.
     */
    public static class PushNotificationReceiver extends NotificationsHandler {

      public static final int NOTIFICATION_ID = 1;
      NotificationCompat.Builder builder;
      NotificationManager mNotificationManager;
      Context ctx;

        @Override
        public void onReceive(Context context, Bundle bundle) {

            ctx = context;
            String nhMessage = bundle.getString("msg");
            //bundle.getString("msg");

            sendNotification(nhMessage);

            if (NotificationHub.getCallbackContext() == null){
                return;
            }

            JSONObject json = new JSONObject();
            try {

                Set<String> keys = bundle.keySet();
                for (String key : keys) {
                    json.put(key, bundle.get(key));
                }
                PluginResult result = new PluginResult(PluginResult.Status.OK, json);
                result.setKeepCallback(true);
                NotificationHub.getCallbackContext().sendPluginResult(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void sendNotification(String msg) {

           mNotificationManager = (NotificationManager)
                         ctx.getSystemService(Context.NOTIFICATION_SERVICE);

                 PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                         new Intent(ctx, ctx.getApplicationContext().getClass()), 0);

                 NotificationCompat.Builder mBuilder =
                         new NotificationCompat.Builder(ctx)
                                 .setSmallIcon(getDrawableIcon())
                                 .setContentTitle("Notification Hub Demo")
                                 .setStyle(new NotificationCompat.BigTextStyle()
                                         .bigText(msg))
                                 .setContentText(msg);

                 mBuilder.setContentIntent(contentIntent);

                 mBuilder.setAutoCancel(true);

                 long[] pattern = {500,500};//{500,500,500,500,500,500,500,500,500};
                 mBuilder.setVibrate(pattern);

                 Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                 mBuilder.setSound(alarmSound);

                 mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        }


        private int getDrawableIcon () {
           Context context = ctx.getApplicationContext();
           String pkgName  = context.getPackageName();

           int resId;
           resId = context.getResources().getIdentifier("icon", "drawable", pkgName);

           return resId;
       }

  }
    /**
     * Returns plugin callback.
     */
    protected static CallbackContext getCallbackContext() {
        return _callbackContext;
    }
}
