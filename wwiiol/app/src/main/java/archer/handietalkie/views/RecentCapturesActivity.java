package archer.handietalkie.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import archer.handietalkie.R;
import archer.handietalkie.adapters.CaptureAdapter;
import archer.handietalkie.database.DataBaseController;
import archer.handietalkie.models.CaptureModel;
import archer.handietalkie.models.CpModel;
import archer.handietalkie.utilities.RecyclerViewEmptySupport;

public class RecentCapturesActivity extends AppCompatActivity {
    private ArrayList<CaptureModel> myDataset;
    private CaptureAdapter mAdapter;
    private CoordinatorLayout coordinatorLayout;
    private LinearLayout loading;
    private Handler myhandler;
    private Timer myTimer;
    private String mAos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_captures);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //
        coordinatorLayout = findViewById(R.id.CoordinatorLayout);
        loading = findViewById(R.id.loading);
        RecyclerViewEmptySupport mRecyclerView = findViewById(R.id.list);
        mRecyclerView.setEmptyView(findViewById(R.id.list_empty));
        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        myDataset = new ArrayList<CaptureModel>();
        //
        mAdapter = new CaptureAdapter(myDataset, this);
        //
        mRecyclerView.setAdapter(mAdapter);

        //
        getCurrentAo();

        myhandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                getCurrentAo();
                return false;
            }
        });

        myTimer = new Timer();
        int delay = 0;   // delay for 30 sec.
        int period = 60000;  // repeat every 60 sec.
        myTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                myhandler.sendEmptyMessage(0);
            }
        }, delay, period);

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("CityView")
                .putContentType("View")
                .putContentId("2"));
    }

    private void getCurrentAo() {
        loading.setVisibility(View.VISIBLE);
        String url = "http://wiretap.wwiionline.com/xmlquery/captures.xml?hours=10&limit=20";
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
                            ArrayList<CaptureModel> aos = readFeed(parser);
                            in.close();
                            //
                            myDataset.clear();
                            //
                            DataBaseController dataBaseController = new DataBaseController(RecentCapturesActivity.this);
                            for (int i = 0; i < aos.size(); i++) {
                                CpModel cpModel = dataBaseController.getFacility(aos.get(i).getFacilityId());
                                aos.get(i).setName(cpModel.getName());
                                //
                                aos.get(i).setDate(new java.util.Date(aos.get(i).getAt() * 1000).toString());
                                myDataset.add(aos.get(i));
                            }
                            mAdapter.notifyDataSetChanged();
                            //
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        }
                        loading.setVisibility(View.INVISIBLE);
                        getSupportActionBar().setSubtitle(new java.util.Date().toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(coordinatorLayout, error.toString(), Snackbar.LENGTH_LONG).show();
                loading.setVisibility(View.INVISIBLE);
                getSupportActionBar().setSubtitle(new java.util.Date().toString());

            }
        });

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        //
        Volley.newRequestQueue(RecentCapturesActivity.this).add(stringRequest);

    }

    private ArrayList<CaptureModel> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<CaptureModel> entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, mAos, "captures");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("cap")) {
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
    private CaptureModel readCp(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, mAos, "cap");
        String id = parser.getAttributeValue(null, "id");
        String at = parser.getAttributeValue(null, "at");
        String fac = parser.getAttributeValue(null, "fac");
        String from = parser.getAttributeValue(null, "from");
        String by = parser.getAttributeValue(null, "by");
        String to = parser.getAttributeValue(null, "to");

        CaptureModel captureModel = new CaptureModel();
        captureModel.setId(Integer.parseInt(id));
        captureModel.setAt(Long.parseLong(at));
        captureModel.setFacilityId(fac);
        captureModel.setFrom(Integer.parseInt(from));
        captureModel.setTo(Integer.parseInt(to));
        captureModel.setBy(by);
        parser.nextTag();
        return captureModel;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recent_captures, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            getCurrentAo();
            return true;
        } else if (id == R.id.action_city_status) {
            Intent intent = new Intent(this, CityFacilities.class);
            intent.putExtras(getIntent());
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    @Override
    protected void onPause() {
        //cancel timer
        if (myTimer != null)
            myTimer.cancel();
        super.onPause();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
