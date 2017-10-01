package archer.handietalkie.views;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import archer.handietalkie.R;
import archer.handietalkie.database.DataBaseController;
import archer.handietalkie.models.AoModel;
import archer.handietalkie.models.CpModel;
import archer.handietalkie.tools.GameLocation;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String AXIS = "axis";
    public static final String ALLIED = "allied";
    ArrayList<AoModel> Axis, Allied;
    private Bitmap bitmapBrition, bitmapUnitedStates, bitmapFrance, bitmapGermany, bitmapArmy;
    private ArrayList<CpModel> cities;
    private GoogleMap googleMap;
    private Handler myhandler;
    private CoordinatorLayout coordinatorLayout;
    private Timer myTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.CoordinatorLayout);

        //
        Allied = getIntent().getParcelableArrayListExtra(ALLIED);
        Axis = getIntent().getParcelableArrayListExtra(AXIS);
        cities = new DataBaseController(this).getCpList();
        //
        bitmapBrition = resizeMapIcons(R.drawable.britain, 80, 40);
        bitmapFrance = resizeMapIcons(R.drawable.france, 80, 40);
        bitmapUnitedStates = resizeMapIcons(R.drawable.unitedstates, 80, 40);
        bitmapGermany = resizeMapIcons(R.drawable.german, 80, 40);
        bitmapArmy = resizeMapIcons(R.drawable.army_pattern, 80, 40);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));
        } catch (Resources.NotFoundException e) {
        }
        UiSettings mUiSettings = googleMap.getUiSettings();

        // Keep the UI Settings state in sync with the checkboxes.
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);

        //
        //googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.resetMinMaxZoomPreference();
        //googleMap.setMinZoomPreference(10.0f);
        googleMap.setMaxZoomPreference(14.0f);
        //
        //renderAllCities();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(GameLocation.getLatLonFromOctetXY(cities.get(420).getOx(), cities.get(420).getOy()), 15.0f));


        myhandler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                updateCpListStatus();
                return false;
            }
        });

        myTimer = new Timer();
        int delay = 0;   // delay for 30 sec.
        int period = 600000;  // repeat every 60*10 sec , 10 min.
        myTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                myhandler.sendEmptyMessage(0);
            }
        }, delay, period);
    }

    public Bitmap resizeMapIcons(int icon, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), icon);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void updateCpListStatus() {
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
        String url = "http://wiretap.wwiionline.com/json/cpstates.citys.json";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray data = jsonObject.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                for (int j = 0; j < cities.size(); j++) {
                                    if (cities.get(j).getId() == data.getJSONObject(i).getInt("id")) {
                                        cities.get(j).setController(data.getJSONObject(i).getInt("controller"));
                                        break;
                                    }
                                }
                            }

                            renderAllCities();

                            // ALL AOs
                            IconGenerator iconGenerator = new IconGenerator(MapsActivity.this);
                            iconGenerator.setStyle(IconGenerator.STYLE_DEFAULT);

                            iconGenerator.setTextAppearance(R.style.axis_Bubble_TextAppearance);
                            for (AoModel ao : Axis) {
                                // Add a marker axis
                                CpModel cpModel = new DataBaseController(MapsActivity.this).getCp(ao.getCpId());
                                Marker marker = googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(Html.fromHtml("<b>" + cpModel.getName() + "</b> has AO!"))))
                                        .position(GameLocation.getLatLonFromOctetXY(cpModel.getOx(), cpModel.getOy()))
                                        .anchor(0.45f, 1.4f)
                                        .zIndex(1.0f));
                                marker.showInfoWindow();
                            }

                            iconGenerator.setTextAppearance(R.style.allied_Bubble_TextAppearance);
                            for (AoModel ao : Allied) {
                                // Add a marker allied
                                CpModel cpModel = new DataBaseController(MapsActivity.this).getCp(ao.getCpId());

                                Marker marker = googleMap.
                                        addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(Html.fromHtml("<b>" + cpModel.getName() + "</b> has AO!"))))
                                                .position(GameLocation.getLatLonFromOctetXY(cpModel.getOx(), cpModel.getOy()))
                                                .anchor(0.45f, 1.4f)
                                                .zIndex(1.0f));
                                marker.showInfoWindow();

                            }


                            CpModel cpModel = new DataBaseController(MapsActivity.this).getCp(Allied.get(0).getCpId());
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(GameLocation.getLatLonFromOctetXY(cpModel.getOx(), cpModel.getOy()), 9.0f), new GoogleMap.CancelableCallback() {
                                @Override
                                public void onFinish() {
                                    findViewById(R.id.loading).setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onCancel() {
                                    findViewById(R.id.loading).setVisibility(View.INVISIBLE);
                                }
                            });

                            //
                            getSupportActionBar().setSubtitle(new java.util.Date().toString());

                        } catch (JSONException e) {
                            Snackbar.make(coordinatorLayout, e.toString(), Snackbar.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(coordinatorLayout, error.toString(), Snackbar.LENGTH_LONG).show();
                findViewById(R.id.loading).setVisibility(View.INVISIBLE);
            }
        });

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        //
        Volley.newRequestQueue(this).add(stringRequest);

    }

    private void renderAllCities() {
        // ALL cities
        for (CpModel cpModel : cities) {
            BitmapDescriptor bitmapDescriptor;
            if (cpModel.getController() == 0)
                cpModel.setController(cpModel.getOrig());
            if (cpModel.getController() == 1) {
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmapBrition);
            } else if (cpModel.getController() == 3) {
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmapFrance);
            } else if (cpModel.getController() == 2) {
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmapUnitedStates);
            } else if (cpModel.getController() == 4) {
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmapGermany);
            } else {
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmapArmy);
            }
            Marker marker = googleMap.
                    addMarker(new MarkerOptions().icon(bitmapDescriptor)
                            .position(GameLocation.getLatLonFromOctetXY(cpModel.getOx(), cpModel.getOy()))
                            .anchor(0.0f, 1.0f)
                            .zIndex(1.0f)
                            .title(cpModel.getName()));
            marker.showInfoWindow();
        }
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
            updateCpListStatus();
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
