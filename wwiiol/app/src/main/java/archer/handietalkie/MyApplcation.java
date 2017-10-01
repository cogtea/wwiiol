package archer.handietalkie;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import com.crashlytics.android.Crashlytics;
import com.gardencoder.shooter.Shooter;
import com.gardencoder.shooter.models.ShooterModel;
import com.gardencoder.shooter.utilites.ConnectionHub;

import io.fabric.sdk.android.Fabric;

/**
 * Created by ramy on 11/3/16.
 */

public class MyApplcation extends Application implements ConnectionHub {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Shooter.install(this).enableScreenShot(false);

    }

    @Override
    public String getUserAccessToken() {
        return "your name";
    }

    @Override
    public boolean sendScreenShot(ShooterModel screenshot) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //emailIntent.setType("png/image");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{
                "ramyatgarden@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Issue");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Please check this issue");
        Uri uri = Uri.parse(screenshot.getPath());
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        emailIntent.setType("text/plain");
        startActivity(emailIntent);
        return true;
    }
}
