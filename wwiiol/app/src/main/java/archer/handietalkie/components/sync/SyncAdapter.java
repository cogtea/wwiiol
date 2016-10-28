package archer.handietalkie.components.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.Xml;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import archer.handietalkie.MainActivity;
import archer.handietalkie.R;
import archer.handietalkie.database.DataBaseController;
import archer.handietalkie.models.AoModel;
import archer.handietalkie.models.CpModel;

/**
 * Created by ramy on 10/28/16.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 5;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "archer.handietalkie.components.sync";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "open";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    private static final String TAG = "SyncAdapter";
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    public final String LOG_TAG = SyncAdapter.class.getSimpleName();
    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    private String mAos;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, AUTHORITY).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    AUTHORITY, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                AUTHORITY, bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);


        int sync_interval = Integer.parseInt(preference.getString("sync_frequency", "2")) * 60;
        int sync_flextime = sync_interval / 3;

        SyncAdapter.configurePeriodicSync(context, sync_interval, sync_flextime);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, AUTHORITY, true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.e(TAG, "onPerformSync: Sync");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String side = prefs.getString("sync_side", "1");
        String url = "http://wiretap.wwiionline.com/xmlquery/cps.xml?aos=true";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            InputStream in = new ByteArrayInputStream(response.getBytes("UTF-8"));
                            XmlPullParser parser = Xml.newPullParser();
                            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                            parser.setInput(in, null);
                            parser.nextTag();
                            ArrayList<AoModel> aos = readFeed(parser);
                            in.close();
                            //
                            DataBaseController dataBaseController = new DataBaseController(getContext());

                            for (int i = 0; i < aos.size(); i++) {
                                if (!dataBaseController.getAoCp(aos.get(i).getAoId())) {
                                    //new Ao
                                    CpModel cpModel = dataBaseController.getCp(aos.get(i).getCpId());
                                    String title = "";
                                    String message = "";
                                    int flag = 0;
                                    if (aos.get(i).getSide() == 2) {
                                        // Allied
                                        if (Integer.parseInt(side) == aos.get(i).getSide()) {
                                            message = "Allied has new AO (" + cpModel.getName() + ")";
                                            title = cpModel.getName();
                                        } else {
                                            message = "Allied has new AO (" + cpModel.getName() + ")";
                                            title = cpModel.getName();
                                        }
                                    } else {
                                        //Axis
                                        if (Integer.parseInt(side) == aos.get(i).getSide()) {
                                            message = "Axis has new AO (" + cpModel.getName() + ")";
                                            title = cpModel.getName();
                                        } else {
                                            message = "Axis has new AO (" + cpModel.getName() + ")";
                                            title = cpModel.getName();
                                        }
                                    }
                                    if (aos.get(i).getOwn() == 1) {
                                        flag = R.drawable.britain;
                                    } else if (aos.get(i).getOwn() == 3) {
                                        flag = R.drawable.france;
                                    } else if (aos.get(i).getOwn() == 2) {
                                        flag = R.drawable.unitedstates;
                                    } else if (aos.get(i).getOwn() == 4) {
                                        flag = R.drawable.german;
                                    }
                                    sendNotification(Integer.parseInt(aos.get(i).getAoId()), title, message, flag);
                                }
                            }
                            //
                            dataBaseController.insertAoCpList(aos);
                            //
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        //
        Volley.newRequestQueue(getContext()).add(stringRequest);

    }

    private void sendNotification(int mId, String mTitle, String mContent, int flag) {
        Bitmap largeIcon = BitmapFactory.decodeResource(getContext().getResources(), flag);

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean notification = preference.getBoolean("notifications_new_message", true);
        if (!notification) {
            return;
        }
        String strRingtonePreference = preference.getString("notifications_new_message_ringtone", "DEFAULT_SOUND");
        boolean vibrate = preference.getBoolean("notifications_new_message_vibrate", true);

        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(largeIcon)
                        .setColor(getContext().getResources().getColor(R.color.accent))
                        .setContentTitle(mTitle)

                        .setSound(Uri.parse(strRingtonePreference))
                        .setContentText(mContent);
        if (vibrate) {
            mBuilder.setVibrate(new long[]{1000});
        }
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(getContext(), MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
    }

    private ArrayList<AoModel> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<AoModel> entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, mAos, "cps");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("cp")) {
                entries.add(readCp(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    // Processes link tags in the feed.
    private AoModel readCp(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, mAos, "cp");
        String cityId = parser.getAttributeValue(null, "id");
        String own = parser.getAttributeValue(null, "own");
        String hcUnit = parser.getAttributeValue(null, "ao");
        AoModel aoModel = new AoModel();
        aoModel.setAoId(hcUnit);
        aoModel.setCpId(cityId);
        aoModel.setOwn(Integer.parseInt(own.substring(0, 1)));
        aoModel.setSide(Integer.parseInt(own.substring(2, 3)));
        parser.nextTag();
        return aoModel;
    }
}
