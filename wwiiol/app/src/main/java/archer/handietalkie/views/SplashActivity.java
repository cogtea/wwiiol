package archer.handietalkie.views;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import archer.handietalkie.BuildConfig;
import archer.handietalkie.MainActivity;
import archer.handietalkie.R;
import archer.handietalkie.components.Loader;
import archer.handietalkie.database.DataBaseController;
import archer.handietalkie.tools.Animation;

public class SplashActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    private Intent mainIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        //
        ((TextView) findViewById(R.id.version)).setText(getString(R.string.version) + BuildConfig.VERSION_NAME);
        //
        if (new DataBaseController(this).getCpList().size() == 0) {
            findViewById(R.id.sub_title).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.sub_title)).setText(R.string.prepare_for_first_time);
            Animation.blinking(findViewById(R.id.sub_title));
            new loadOnBackgroundThread().execute(0);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    SplashActivity.this.startActivity(mainIntent);
                    SplashActivity.this.finish();
                }
            }, SPLASH_DISPLAY_LENGTH);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    private class loadOnBackgroundThread extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            try {
                InputStream cpListXml = getAssets().open(getString(R.string.cp_list_file));
                InputStream facilityListXml = getAssets().open(getString(R.string.facility_list_xml));
                new Loader().load(cpListXml, SplashActivity.this, Loader.CITY);
                new Loader().load(facilityListXml, SplashActivity.this, Loader.FACILITY);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }
    }
}
