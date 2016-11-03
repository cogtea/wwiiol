package archer.handietalkie.views;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

import archer.handietalkie.MainActivity;
import archer.handietalkie.R;
import archer.handietalkie.components.Loader;
import archer.handietalkie.database.DataBaseController;

public class SplashActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 2000;
    private Intent mainIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        //
        if (new DataBaseController(this).getCpList().size() == 0) {
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

    class loadOnBackgroundThread extends AsyncTask<Integer, Void, Void> {
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
