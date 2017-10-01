package archer.handietalkie;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
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
import com.gardencoder.shooter.ShooterAppCompactActivity;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import archer.handietalkie.adapters.AoAdapter;
import archer.handietalkie.components.sync.SyncAdapter;
import archer.handietalkie.database.DataBaseController;
import archer.handietalkie.models.AoModel;
import archer.handietalkie.models.CpModel;
import archer.handietalkie.views.AboutActivity;
import archer.handietalkie.views.CityActivity;
import archer.handietalkie.views.MapsActivity;
import archer.handietalkie.views.RecentCapturesActivity;
import archer.handietalkie.views.SettingsActivity;


public class MainActivity extends ShooterAppCompactActivity implements ExpandableListView.OnChildClickListener {

    // Sync interval constants
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 2L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;
    private static final String TAG = "MainActivity";
    ArrayList<AoModel> Axis, Allied;
    // Global variables
    // A content resolver for accessing the provider
    private Toolbar toolbar;
    private CoordinatorLayout coordinatorLayout;
    private TextView status;
    private ExpandableListView mListView;
    private AoAdapter listAdapter;
    private ArrayList<String> listDataHeader;
    private HashMap<String, List<AoModel>> listDataChild;
    private String mAos;
    private AppCompatImageView statusImage;
    private View loading;
    private Timer myTimer;
    private Handler myhandler;
    private View statusBar;
    private BottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.list);
        coordinatorLayout = findViewById(R.id.CoordinatorLayout);
        loading = findViewById(R.id.loading);
        toolbar = findViewById(R.id.toolbar);
        status = findViewById(R.id.status);
        statusImage = findViewById(R.id.status_image);
        statusBar = findViewById(R.id.status_bar);


        setSupportActionBar(toolbar);

        bottomBar = findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(int tabId) {
                if (tabId == R.id.tab_map) {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putParcelableArrayListExtra(MapsActivity.ALLIED, Allied);
                    intent.putParcelableArrayListExtra(MapsActivity.AXIS, Axis);
                    startActivity(intent);
                } else if (tabId == R.id.tab_recent) {
                    Intent intent = new Intent(MainActivity.this, RecentCapturesActivity.class);
                    startActivity(intent);
                } else if (tabId == R.id.tab_home) {

                }
            }
        });
        // Set the adapter
        // preparing list data
        prepareListData();

        listAdapter = new AoAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        mListView.setAdapter(listAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnChildClickListener(this);
        //
        for (int i = 0; i < listDataChild.size(); i++) {
            mListView.expandGroup(i);
        }
        getServerStatus();
        //
        SyncAdapter.initializeSyncAdapter(this);

        myhandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                getServerStatus();
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
                .putContentName("MainView")
                .putContentType("View")
                .putContentId("1"));
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            try {
                intent = new Intent(this, CityActivity.class);
                CpModel city = new DataBaseController(this).getCp(data.toString());
                intent.putExtra(CityActivity.CITY_ID, String.valueOf(city.getId()));
                intent.putExtra(CityActivity.CITY_NAME, city.getName());
                intent.putExtra(CityActivity.CITY_OWN, String.valueOf(city.getController()));
                startActivity(intent);
            } catch (NumberFormatException e) {

            }
        }
    }

    private void getServerStatus() {
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
        String url = "http://wiretap.wwiionline.com/json/servers.json";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String pop = jsonObject.getJSONObject("1").getString("pop");
                            String state = jsonObject.getJSONObject("1").getString("state");

                            //
                            if (state.equals("Online")) {
                                statusImage.setImageResource(R.drawable.ic_check_circle_black_24dp);
                                //
                                if (pop.equals("Average")) {
                                    status.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorOrange));
                                    statusImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorOrange));
                                    statusBar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorOrangeTransparent));
                                } else if (pop.equals("Low") || pop.equals("Very Light") || pop.equals("Empty")) {
                                    status.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorRed));
                                    statusImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorRed));
                                    statusBar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorRedTransparent));
                                } else {
                                    statusImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorGreen));
                                    status.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorGreen));
                                    statusBar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorGreenTransparent));
                                }
                                status.setText("Server pop : " + pop);
                                //
                            } else {
                                status.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorRed));
                                statusImage.setImageResource(R.drawable.ic_highlight_off_black_24dp);
                                statusImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorRed));
                                statusBar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorRedTransparent));
                                status.setText("Server is Offline.");
                            }


                        } catch (JSONException e) {
                            Snackbar.make(coordinatorLayout, e.toString(), Snackbar.LENGTH_LONG).show();
                        }
                        //
                        getCurrentAo();
                        //
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(coordinatorLayout, error.toString(), Snackbar.LENGTH_LONG).show();
                loading.setVisibility(View.INVISIBLE);


            }
        });

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        //
        Volley.newRequestQueue(MainActivity.this).add(stringRequest);

    }

    private void getCurrentAo() {
        String url = "http://wiretap.wwiionline.com/xmlquery/cps.xml?aos=true";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            final InputStream in = new ByteArrayInputStream(response.getBytes("UTF-8"));
                            XmlPullParser parser = Xml.newPullParser();
                            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                            parser.setInput(in, null);
                            parser.nextTag();
                            ArrayList<AoModel> aos = readFeed(parser);
                            in.close();
                            //
                            Allied.clear();
                            Axis.clear();
                            //
                            DataBaseController dataBaseController = new DataBaseController(MainActivity.this);
                            for (int i = 0; i < aos.size(); i++) {
                                CpModel cpModel = dataBaseController.getCp(aos.get(i).getCpId());
                                aos.get(i).setName(cpModel.getName());
                                if (aos.get(i).getOwn() == 4) {
                                    //allied
                                    Allied.add(aos.get(i));
                                } else {
                                    //axis
                                    Axis.add(aos.get(i));
                                }
                            }
                            dataBaseController.insertAoCpList(aos);
                            listAdapter.notifyDataSetChanged();
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
                //
                getSupportActionBar().setSubtitle(new java.util.Date().toString());

            }
        });

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        //
        Volley.newRequestQueue(MainActivity.this).add(stringRequest);

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
        String contention = parser.getAttributeValue(null, "contention");

        AoModel aoModel = new AoModel();
        aoModel.setAoId(hcUnit);
        aoModel.setCpId(cityId);
        aoModel.setOwn(Integer.parseInt(own.substring(0, 1)));
        aoModel.setSide(Integer.parseInt(own.substring(2, 3)));
        aoModel.setContention(Boolean.parseBoolean(contention));
        parser.nextTag();
        return aoModel;
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<AoModel>>();

        // Adding child data
        listDataHeader.add("Axis");
        listDataHeader.add("Allied");

        // Adding child data
        Axis = new ArrayList<AoModel>();


        Allied = new ArrayList<AoModel>();

        listDataChild.put(listDataHeader.get(0), Axis); // Header, Child data
        listDataChild.put(listDataHeader.get(1), Allied);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
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
            getServerStatus();
            return true;
        } else if (id == R.id.action_info) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

        Intent intent = new Intent(this, CityActivity.class);
        if (groupPosition == 0) {
            intent.putExtra(CityActivity.CITY_ID, Axis.get(childPosition
            ).getCpId());
            intent.putExtra(CityActivity.CITY_NAME, Axis.get(childPosition
            ).getName());
            intent.putExtra(CityActivity.CITY_OWN, Axis.get(childPosition
            ).getOwn());
        } else {
            intent.putExtra(CityActivity.CITY_ID, Allied.get(childPosition
            ).getCpId());
            intent.putExtra(CityActivity.CITY_NAME, Allied.get(childPosition
            ).getName());
            intent.putExtra(CityActivity.CITY_OWN, Allied.get(childPosition
            ).getOwn());
        }
        startActivity(intent);
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        bottomBar.selectTabAtPosition(0);

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
