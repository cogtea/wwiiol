package archer.handietalkie.views;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import archer.handietalkie.R;
import archer.handietalkie.adapters.CaptureAdapter;
import archer.handietalkie.database.DataBaseController;
import archer.handietalkie.models.CaptureModel;
import archer.handietalkie.models.CpModel;

public class CityActivity extends AppCompatActivity {

    public static final String CITY_NAME = "name";
    public static final String CITY_ID = "cityid";
    public static final String CITY_OWN = "cityOwn";
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<CaptureModel> myDataset;
    private CaptureAdapter mAdapter;
    private CoordinatorLayout coordinatorLayout;
    private LinearLayout loading;
    private String mAos;
    private String cityId;
    private String cityName;
    private TextView status;
    private ImageView statusImageOrigin, statusImageOwn;
    private int cityOwn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
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
        myDataset = new ArrayList<CaptureModel>();
        //
        mAdapter = new CaptureAdapter(myDataset, this);
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
        getCurrentAo();
    }

    private void getCurrentAo() {
        loading.setVisibility(View.VISIBLE);
        String url = "http://wiretap.wwiionline.com/xmlquery/captures.xml?cp=" + cityId;
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
                            DataBaseController dataBaseController = new DataBaseController(CityActivity.this);
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
        Volley.newRequestQueue(CityActivity.this).add(stringRequest);

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
        getMenuInflater().inflate(R.menu.menu_city, menu);
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
        super.onPause();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
