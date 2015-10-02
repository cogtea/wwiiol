package archer.handietalkie;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
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

import archer.handietalkie.adapters.AoAdapter;
import archer.handietalkie.database.DataBaseController;
import archer.handietalkie.models.AoModel;
import archer.handietalkie.models.CpModel;
import archer.handietalkie.views.AboutActivity;
import archer.handietalkie.views.CityActivity;


public class MainActivity extends AppCompatActivity implements ExpandableListView.OnChildClickListener {

    private static final String TAG = "MainActivity";
    ArrayList<AoModel> Axis, Allied;
    private Toolbar toolbar;
    private CoordinatorLayout coordinatorLayout;
    private TextView status;
    private ExpandableListView mListView;
    private AoAdapter listAdapter;
    private ArrayList<String> listDataHeader;
    private HashMap<String, List<AoModel>> listDataChild;
    private String mAos;
    private ImageView statusImage;
    private LinearLayout loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ExpandableListView) findViewById(R.id.list);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.CoordinatorLayout);
        loading = (LinearLayout) findViewById(R.id.loading);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        status = (TextView) findViewById(R.id.status);
        statusImage = (ImageView) findViewById(R.id.status_image);

        setSupportActionBar(toolbar);

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
    }

    private void getServerStatus() {
        loading.setVisibility(View.VISIBLE);
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

                            status.setText("Server pop : " + pop);
                            //
                            if (state.equals("Online")) {
                                statusImage.setImageResource(R.drawable.ic_check_circle_white_24dp);
                            } else {
                                statusImage.setImageResource(R.drawable.ic_highlight_off_white_24dp);
                            }
                            //
                            if (pop.equals("Average")) {
                                status.setTextColor(Color.parseColor("#FF6600"));
                            } else if (pop.equals("Low") || pop.equals("VeryLight")) {
                                status.setTextColor(Color.RED);
                            } else {
                                status.setTextColor(Color.GREEN);
                            }
                            //

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
        String url = "http://web3.wwiionline.com/xmlquery/cps.xml?aos=true";
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
                            listAdapter.notifyDataSetChanged();
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
        AoModel aoModel = new AoModel();
        aoModel.setAoId(hcUnit);
        aoModel.setCpId(cityId);
        aoModel.setOwn(Integer.parseInt(own.substring(0, 1)));
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        } else {
            intent.putExtra(CityActivity.CITY_ID, Allied.get(childPosition
            ).getCpId());
            intent.putExtra(CityActivity.CITY_NAME, Allied.get(childPosition
            ).getName());
        }
        startActivity(intent);
        return false;
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
