package archer.handietalkie.views;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import archer.handietalkie.adapters.FacilityAdapter;
import archer.handietalkie.database.DataBaseController;
import archer.handietalkie.models.CpModel;
import archer.handietalkie.models.FacilityModel;

public class CityFacilities extends AppCompatActivity {
    public static final String CITY_NAME = "name";
    public static final String CITY_ID = "cityid";
    public static final String CITY_OWN = "cityOwn";
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<FacilityModel> myDataset;
    private FacilityAdapter mAdapter;
    private CoordinatorLayout coordinatorLayout;
    private LinearLayout loading;
    private String mAos;
    private String cityId;
    private String cityName;
    private TextView status;
    private ImageView statusImageOrigin, statusImageOwn;
    private int cityOwn;
    private Handler myhandler;
    private Timer myTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_facilities);
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //
        status = (TextView) findViewById(R.id.status);
        statusImageOrigin = (ImageView) findViewById(R.id.status_image_origin);
        statusImageOwn = (ImageView) findViewById(R.id.status_image_own);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.CoordinatorLayout);
        loading = (LinearLayout) findViewById(R.id.loading);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        myDataset = new ArrayList<FacilityModel>();
        //
        mAdapter = new FacilityAdapter(myDataset, this);
        //
        mRecyclerView.setAdapter(mAdapter);
        //
        if (getIntent().getExtras() != null && getIntent().getExtras().getString(CITY_ID) != null) {
            cityId = getIntent().getExtras().getString(CITY_ID);
            cityName = getIntent().getExtras().getString(CITY_NAME);
            cityOwn = getIntent().getExtras().getInt(CITY_OWN);

            getSupportActionBar().setTitle(cityName);
            //
            CpModel cpModel = new DataBaseController(this).getCp(cityId);
            status.setText(cpModel.getName());
            if (cpModel.getOrig() == 1) {
                statusImageOrigin.setImageResource(R.drawable.britain);
            } else if (cpModel.getOrig() == 3) {
                statusImageOrigin.setImageResource(R.drawable.france);
            } else if (cpModel.getOrig() == 2) {
                statusImageOrigin.setImageResource(R.drawable.unitedstates);
            } else if (cpModel.getOrig() == 4) {
                statusImageOrigin.setImageResource(R.drawable.german);
            }

            if (cityOwn == 1) {
                statusImageOwn.setImageResource(R.drawable.britain);
            } else if (cityOwn == 3) {
                statusImageOwn.setImageResource(R.drawable.france);
            } else if (cityOwn == 2) {
                statusImageOwn.setImageResource(R.drawable.unitedstates);
            } else if (cityOwn == 4) {
                statusImageOwn.setImageResource(R.drawable.german);
            }
        }
        //
        getCpFacilities();

        myhandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                getCpFacilities();
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
                .putContentName("CityFacilitiesView")
                .putContentType("View")
                .putContentId("3"));
    }

    private void getCpFacilities() {
        loading.setVisibility(View.VISIBLE);
        String url = "http://wiretap.wwiionline.com/xmlquery/facilities.xml?cp=" + cityId;
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
                            ArrayList<FacilityModel> aos = readFeed(parser);
                            in.close();
                            //
                            myDataset.clear();
                            //
                            DataBaseController dataBaseController = new DataBaseController(CityFacilities.this);
                            for (int i = 0; i < aos.size(); i++) {
                                CpModel cpModel = dataBaseController.getFacility(String.valueOf(aos.get(i).getId()));
                                aos.get(i).setName(cpModel.getName());
                                //
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
        Volley.newRequestQueue(CityFacilities.this).add(stringRequest);

    }

    private ArrayList<FacilityModel> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<FacilityModel> entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, mAos, "facilities");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("fac")) {
                entries.add(readFacility(parser));
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
    private FacilityModel readFacility(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, mAos, "fac");
        String id = parser.getAttributeValue(null, "id");
        String ctry = parser.getAttributeValue(null, "ctry");
        String side = parser.getAttributeValue(null, "side");
        String open = parser.getAttributeValue(null, "open");

        FacilityModel facilityModel = new FacilityModel();
        facilityModel.setId(Integer.parseInt(id));
        facilityModel.setCtry(Integer.parseInt(ctry));
        facilityModel.setSide(side);
        facilityModel.setOpen(open);
        parser.nextTag();
        return facilityModel;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_city_status, menu);
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
            getCpFacilities();
            return true;
        }
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
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
