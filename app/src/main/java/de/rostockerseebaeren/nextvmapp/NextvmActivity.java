package de.rostockerseebaeren.nextvmapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

//import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

//import io.fabric.sdk.android.Fabric;

public class NextvmActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "NextvmActivity";
    private static int CONNECTION_TIMEOUT = 4000;
    TextView txtWelcome;
    User mUser;
    ArrayList<TvmEvent> events;
    private ArrayList<TvmCategory> categories;
    ArrayList<View> mEventViews;
    LinearLayout lastSelectedEventView;

    private String mError = "";
    LinearLayout rootView = null;
    LayoutInflater inflater = null;
    // Our created menu to use
    private Menu mymenu;
    protected NavigationView navigationView;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    Toolbar toolbar;
    ArrayList<String> alarmIDsArray = new ArrayList<String>();
    private int currentCategory = 1; // default: 1 for training
    private int currentCategoriesIndex = 0;
    private ArrayList<BoardPost> boardPosts;
    private int boardViewId = -1;
    private ArrayList<View>  mBoardViews;
    private boolean newBoardPosts = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /** Only Lollipop */
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        CONNECTION_TIMEOUT = Integer.parseInt(settings.getString("networkTimeout","4000"));
        Set<String> alarmIDs = settings.getStringSet("alarms",null);

        if (alarmIDs != null && !alarmIDs.isEmpty()) {
            Object[] alarmIDsStringArray = (Object[]) alarmIDs.toArray();
            for(int i = 0 ; i < alarmIDsStringArray.length ; i++) {
                alarmIDsArray.add(alarmIDsStringArray[i].toString());
            }
        }

        Intent intent = getIntent();
        mUser = (User) intent.getSerializableExtra("user");

        new getCategoriesTask().execute();

        inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        rootView = (LinearLayout) findViewById(R.id.linlayRoot);

        txtWelcome = (TextView) findViewById(R.id.txtWelcome);
        txtWelcome.setText("Hallo " + mUser.mName);
        txtWelcome.setPadding((int)pxFromDp(5),(int)pxFromDp(5),(int)pxFromDp(5),(int)pxFromDp(5));

        TextView txtNav = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        txtNav.setText("Hallo " + mUser.mName);
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new UpdateEvents(getApplicationContext()).execute();
                    }
                }
        );

        new getEventsTask(rootView).execute();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // We should save our menu so we can use it to reset our updater.
        mymenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_refresh:
                // Do animation start
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ImageView iv = (ImageView)inflater.inflate(R.layout.iv_refresh, null);
                Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
                rotation.setRepeatCount(Animation.INFINITE);
                iv.startAnimation(rotation);
                item.setActionView(iv);
                new UpdateEvents(this).execute();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_logout:
                finish();
                return true;
            case R.id.action_showVersion:
                Toast.makeText(NextvmActivity.this,"TVM App, Version " + BuildConfig.VERSION_NAME, Toast.LENGTH_SHORT).show();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    public void resetUpdating()
    {
        // Get our refresh item from the menu
        MenuItem m = mymenu.findItem(R.id.action_refresh);
        if(m.getActionView()!=null)
        {
            // Remove the animation.
            m.getActionView().clearAnimation();
            m.setActionView(null);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(categories != null) {
            for (int i = 0; i < categories.size() ; i++) {
                if(categories.get(i).mViewID == id) {
                    currentCategoriesIndex = i;
                    currentCategory = categories.get(i).mID;
                    if(!mySwipeRefreshLayout.isRefreshing()){
                        mySwipeRefreshLayout.setRefreshing(true);
                    }
                    new getEventsTask(null).execute();
                }
            }
        }

        if(id == boardViewId) {
            currentCategory = -1; // special for board posts
            showBoardPosts();
        }
        /*
        if (id == R.id.nav_board) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://rostockerseebaeren.de/forum"));
            ActivityInfo activityInfo = browserIntent.resolveActivityInfo(getPackageManager(), browserIntent.getFlags());
            if (activityInfo.exported) {
                startActivity(browserIntent);
            }

        } else if (id == R.id.nav_doodle) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://doodle.com/poll/dmt7aqgkkzc7caez"));
            ActivityInfo activityInfo = browserIntent.resolveActivityInfo(getPackageManager(), browserIntent.getFlags());
            if (activityInfo.exported) {
                startActivity(browserIntent);
            }
        }
    */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class UpdateEvents extends AsyncTask<Void, Void, Void> {

        private Context mCon;

        public UpdateEvents(Context con)
        {
            mCon = con;
        }

        @Override
        protected Void doInBackground(Void... nope) {
            try {
                // Set a time to simulate a long update process.
                if(currentCategory >= 0) {
                    getEvents(mUser.mID, mUser.mJoomlaPassword, currentCategory);
                } else {
                    // board post special
                    getLatestBoardPosts(mUser.mID, mUser.mJoomlaPassword);
                }
                return null;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void nope) {
            // Change the menu back
            resetUpdating();
            if(mySwipeRefreshLayout.isRefreshing()){
                mySwipeRefreshLayout.setRefreshing(false);
            }
            if(currentCategory >= 0) {
                updateEventList();
            } else {
                showBoardPosts();
            }
        }
    }

    private class getCategoriesTask extends AsyncTask<String, String, String> {

        public getCategoriesTask() {
        }
        @Override
        protected String doInBackground(String... params) {
            getCategories(mUser.mID, mUser.mJoomlaPassword);
            getLatestBoardPosts(mUser.mID, mUser.mJoomlaPassword);
            return "Done";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            updateCategoriesMenu();
        }
    }


    protected void updateCategoriesMenu() {
        if(navigationView != null) {
            SubMenu navSub = navigationView.getMenu().getItem(0).getSubMenu();
            navSub.removeItem(R.id.nav_categories_dummy); // remove dummy
            if (categories != null) {
                for (int i = 0; i < categories.size(); i++) {
                    categories.get(i).mViewID = View.generateViewId();
                    navSub.add(0, categories.get(i).mViewID, 0, categories.get(i).mName);
                }
            }
            if (boardPosts != null) {
                boardViewId = View.generateViewId();
                SubMenu navSubCom = navigationView.getMenu().getItem(1).getSubMenu();
                navSubCom.removeItem(R.id.nav_board);
                //navigationView.getMenu().addSubMenu(0, boardViewId, 0, getString(R.string.get_latest_posts));
                navSubCom.add(0, boardViewId, 0, getString(R.string.get_latest_posts));
                if(newBoardPosts) {
                    navSubCom.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.stat_notify_chat));
                }
            }
        }
    }


    private class getEventsTask extends AsyncTask<String, String, String> {

        LinearLayout parent = null;

        public getEventsTask(LinearLayout v) {
            this.parent = v;
        }
        @Override
        protected String doInBackground(String... params) {
            if(currentCategory >= 0) {
                getEvents(mUser.mID, mUser.mJoomlaPassword, currentCategory);
            } else {
                // board post special
                getLatestBoardPosts(mUser.mID, mUser.mJoomlaPassword);
            }

            return "Done";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(currentCategory >= 0) {
                updateEventList();
            } else {
                showBoardPosts();
            }
            if(mySwipeRefreshLayout.isRefreshing()){
                mySwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    @Deprecated
    protected int generateViewId(){
        int seed = (int)(Math.random()*1000);
        int id = seed + (int)0xf7050000;
        // serach if ID is already used
        if(findViewById(id) == null) {
            return id;
        } else return generateViewId();
    }

    private void showBoardPosts() {
        if (inflater != null && boardPosts != null) {
            if (boardPosts.size() > 0) {
                if(mBoardViews == null) {
                    mBoardViews = new ArrayList<View>();
                } else {
                    mBoardViews.clear();
                }

                // clear list on screen
                rootView.removeAllViews();

                // toolbar title
                getSupportActionBar().setTitle(getString(R.string.nav_title_board));



                // add new views
                for (int i = 0 ; i < boardPosts.size() ; i++) {
                    final BoardPost post = boardPosts.get(i);

                    ViewGroup vg = (LinearLayout) getLayoutInflater().inflate(R.layout.board_post, rootView,true);
                    View v = vg.getChildAt(vg.getChildCount()-1); // get last added child
                    int currID = View.generateViewId();
                    //int currID = generateViewId();
                    v.setId(currID);
                    mBoardViews.add(v);

                    View.OnClickListener boardListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://rostockerseebaeren.de/index.php?option=com_kunena&view=topic&id=" + post.mTopicID));
                            ActivityInfo activityInfo = browserIntent.resolveActivityInfo(getPackageManager(), browserIntent.getFlags());
                            if (activityInfo.exported) {
                                startActivity(browserIntent);
                            }
                        }
                    };

                    TextView txtTopic = (TextView) mBoardViews.get(i).findViewById(currID).findViewById(R.id.txtBoardTopic);
                    txtTopic.setText(post.mTopic);
                    txtTopic.setPadding((int)pxFromDp(5.0f),0,(int)pxFromDp(5.0f),0);
                    txtTopic.setOnClickListener(boardListener);
                    if(mUser.mLastLogin < post.mTopicLastUpdate) {
                        // post is new
                        txtTopic.setTextColor(Color.parseColor("#42f465"));
                    }

                    TextView txtUser = (TextView) mBoardViews.get(i).findViewById(currID).findViewById(R.id.txtBoardUser);
                    txtUser.setText(post.mUser);
                    txtUser.setPadding((int)pxFromDp(5.0f),0,(int)pxFromDp(5.0f),0);
                    txtUser.setOnClickListener(boardListener);

                    TextView txtMessage = (TextView) mBoardViews.get(i).findViewById(currID).findViewById(R.id.txtBoardMsg);
                    txtMessage.setText(post.mMessage);
                    txtMessage.setPadding((int)pxFromDp(5.0f),0,(int)pxFromDp(5.0f),0);
                    txtMessage.setOnClickListener(boardListener);


                }

            }
        }
    }


    private void updateEventList(){
        if(inflater != null && events != null) {
            if(events.size() > 0) {
                if(mEventViews == null) {
                    mEventViews = new ArrayList<View>();
                } else {
                    mEventViews.clear();
                }
                // clear list on screen
                rootView.removeAllViews();

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

//                 remove all pendingIntents and alarms
                if(settings.getBoolean("rememberMe",false)){
                    if(alarmIDsArray != null) {
                        for (int i = 0; i < alarmIDsArray.size(); i++) {
                            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            Intent alarmIntent = new Intent(NextvmActivity.this, ShowNotification.class);
                            alarmIntent.setAction("ShowNotificationAction");
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(NextvmActivity.this, Integer.parseInt(alarmIDsArray.get(i)), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT );
                            pendingIntent.cancel();
                            alarmManager.cancel(pendingIntent);
                        }
                        alarmIDsArray.clear();
                    }
                }

                // add new views
                for (int i = 0 ; i < events.size() ; i++) {

                    final TvmEvent event = events.get(i);

                    ViewGroup vg = (LinearLayout) getLayoutInflater().inflate(R.layout.single_tvm_entry, rootView,true);
                    View v = vg.getChildAt(vg.getChildCount()-1); // get last added child
                    int currID = View.generateViewId();
                    //int currID = generateViewId();
                    v.setId(currID);
                    mEventViews.add(i,v);

                    // toolbar title
                    getSupportActionBar().setTitle(categories.get(currentCategoriesIndex).mName);

                    TextView txtWhatAndWho = (TextView) mEventViews.get(i).findViewById(currID).findViewById(R.id.txtWhatAndWho);
                    txtWhatAndWho.setText(event.mTitle + " - " + event.mUsername);
                    txtWhatAndWho.setPadding((int)pxFromDp(5.0f),0,(int)pxFromDp(5.0f),0);
                    TextView txtWhere = (TextView)  mEventViews.get(i).findViewById(currID).findViewById(R.id.txtWhere);
                    txtWhere.setText(Html.fromHtml("<b>Ort:</b> ") + event.mLocation);
                    txtWhere.setPadding((int)pxFromDp(5.0f),0,(int)pxFromDp(5.0f),0);
                    TextView txtWhen = (TextView)  mEventViews.get(i).findViewById(currID).findViewById(R.id.txtWhen);
                    txtWhen.setText(Html.fromHtml("<b>Wann:</b> ") + event.mDateString);
                    txtWhen.setPadding((int)pxFromDp(5.0f),0,(int)pxFromDp(5.0f),0);
                    TextView txtComment = (TextView)  mEventViews.get(i).findViewById(currID).findViewById(R.id.txtComment);
                    if (event.mComment.length() > 0) {
                        txtComment.setText(Html.fromHtml("<b>>></b> ") + event.mComment);
                        txtComment.setVisibility(View.VISIBLE);
                        txtComment.setPadding((int)pxFromDp(5.0f),0,(int)pxFromDp(5.0f),0);
                    }

                    RadioGroup rg = (RadioGroup) mEventViews.get(i).findViewById(currID).findViewById(R.id.rgChoice);

                    RadioButton btnYes =  (RadioButton)mEventViews.get(i).findViewById(currID).findViewById(R.id.radioButtonYes);
                    RadioButton btnNo =  (RadioButton)mEventViews.get(i).findViewById(currID).findViewById(R.id.radioButtonNo);
                    RadioButton btnMaybe =  (RadioButton)mEventViews.get(i).findViewById(currID).findViewById(R.id.radioButtonMaybe);

                    if(event.mClosed) {
                        rg.setVisibility(View.GONE);
                        btnYes.setEnabled(false);
                        btnNo.setEnabled(false);
                        btnMaybe.setEnabled(false);
                    }

                    TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");

                    long now = (System.currentTimeMillis() + tz.getRawOffset() + tz.getDSTSavings())/1000;
                    long eventTimeout = (event.mDate.getTime() +  tz.getRawOffset() + tz.getDSTSavings())/1000 - (event.mDeadline * 60 );

                    if(now > eventTimeout) {
                        rg.setVisibility(View.GONE);
                        btnYes.setEnabled(false);
                        btnNo.setEnabled(false);
                        btnMaybe.setEnabled(false);
                    }
                    Button btnCal = (Button) mEventViews.get(i).findViewById(R.id.btnAddToCal);
                    btnCal.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent calIntent = new Intent(Intent.ACTION_INSERT);
                            calIntent.setType("vnd.android.cursor.item/event");
                            calIntent.putExtra(CalendarContract.Events.TITLE, event.mTitle);
                            calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.mLocation);
                            calIntent.putExtra(CalendarContract.Events.DESCRIPTION, event.mComment);

                            //GregorianCalendar calDate = new GregorianCalendar();
                            calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
                            calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.mDate.getTime());
                            calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.mDate.getTime() + (event.mDuration*1000*60));
                            startActivity(calIntent);
                        }
                    });

                    Button btnComment = (Button) mEventViews.get(i).findViewById(currID).findViewById(R.id.btnSetComment);
                    // btnToggle.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.tvm_grey));

                    switch (event.mUserState){
                        case NONE:
                            mEventViews.get(i).findViewById(currID).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_white));
                            break;
                        case YES_NOT_ACK:
                            mEventViews.get(i).findViewById(currID).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_grey));
                            btnYes.setChecked(true);
                            btnComment.setEnabled(true);
                            btnComment.setAlpha(1.0f);
                            if(settings.getBoolean("rememberMe",false)) {
                                createUserNotification(event);
                            }
                            break;
                        case YES_ACK:
                            mEventViews.get(i).findViewById(currID).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_green));
                            btnYes.setChecked(true);
                            btnComment.setEnabled(true);
                            btnComment.setAlpha(1.0f);
                            if(settings.getBoolean("rememberMe",false)) {
                                createUserNotification(event);
                            }
                            break;
                        case NO:
                            mEventViews.get(i).findViewById(currID).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_red));
                            btnNo.setChecked(true);
                            btnComment.setEnabled(true);
                            btnComment.setAlpha(1.0f);
                            break;
                        case NO_FORCED:
                            mEventViews.get(i).findViewById(currID).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_red));
                            btnNo.setChecked(true);
                            btnComment.setEnabled(true);
                            btnComment.setAlpha(1.0f);
                            break;
                        case MAYBE:
                            mEventViews.get(i).findViewById(currID).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_yellow));
                            btnMaybe.setChecked(true);
                            btnComment.setEnabled(true);
                            btnComment.setAlpha(1.0f);
                            if(settings.getBoolean("rememberMe",false)) {
                                createUserNotification(event);
                            }
                            break;
                        default:
                            mEventViews.get(i).findViewById(currID).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_white));
                            btnComment.setEnabled(false);
                            btnComment.setAlpha(0.5f);
                            break;
                    }

                    final LinearLayout llUsers = (LinearLayout) mEventViews.get(i).findViewById(currID).findViewById(R.id.linlayEventUsers);

                    llUsers.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

                    ToggleButton btnToggle = (ToggleButton) mEventViews.get(i).findViewById(currID).findViewById(R.id.btnExpandUsers);
                    btnToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked) {
                                llUsers.setVisibility(View.VISIBLE);
                            } else {
                                llUsers.setVisibility(View.GONE);
                            }
                        }
                    });

                    btnComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(NextvmActivity.this);
                            builder.setTitle(getString(R.string.info_set_comment));
                            // Set up the input
                            final EditText input = new EditText(NextvmActivity.this);
                            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            input.setText(event.mUserComment);
                            builder.setView(input);

                            // Set up the buttons
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    lastSelectedEventView = (LinearLayout) v.getParent().getParent();
                                    new updateEventState(mUser.mID, mUser.mJoomlaPassword, event.mID, mUser.mID,getUserState(event.mID), input.getText().toString()).execute();

                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();
                        }
                    });

                    int usersYes = 0, usersNo = 0, usersMaybe = 0, usersYesNACK = 0;

                    for(int j = 0 ; j < event.mUsers.size() ; j++) {
                        LinearLayout llSingleEventUserEntry = new LinearLayout(llUsers.getContext());

                        llSingleEventUserEntry.setOrientation(LinearLayout.HORIZONTAL);
                        llSingleEventUserEntry.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_transparent));
                        llSingleEventUserEntry.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

                        final EventUser cur = event.mUsers.get(j);

                        TextView tv = new TextView(getApplicationContext());
                        tv.setText(cur.mName);
                        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_text));
                        tv.setPadding((int)pxFromDp(5f),2,0,2);
                        tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT));
                        llSingleEventUserEntry.addView(tv);

                        TextView txtUserComment = null;
                        CheckBox user_ack = null;
                        Button user_rem = null;
                        View lblSpacer = null;
                        if(mUser.isMara()) {
                            LinearLayout.LayoutParams llParamButton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
                            llParamButton.bottomMargin = 0;
                            llParamButton.leftMargin = 0;
                            llParamButton.topMargin = 0;
                            llParamButton.rightMargin= 0;
                            llParamButton.gravity = Gravity.CENTER_HORIZONTAL | Gravity.FILL_VERTICAL;
                            llParamButton.weight = 0;
                            llParamButton.width = (int)pxFromDp(35);

                            user_ack = new CheckBox(getApplicationContext());
                            user_ack.setText(getString(R.string.ack_user));
                            user_ack.setPadding((int)pxFromDp(5f),2,0,2);
                            user_ack.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                            user_ack.setLayoutParams(llParamButton);

                            user_rem = new Button(getApplicationContext());
                            user_rem.setText(getString(R.string.rem_user));
                            user_rem.setPadding((int)pxFromDp(5f),2,0,2);
                            user_rem.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                            user_rem.setLayoutParams(llParamButton);

                            lblSpacer = new View(getApplicationContext());
                            lblSpacer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                            switch (cur.state ) {
                                case YES_ACK:
                                    user_ack.setChecked(true);
                                    user_ack.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_green));
                                    user_rem.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_green));
                                    lblSpacer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_green));
                                    break;
                                case YES_NOT_ACK:
                                    user_ack.setChecked(false);
                                    user_ack.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_grey));
                                    user_rem.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_grey));
                                    lblSpacer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_grey));
                                    break;
                                case MAYBE:
                                    user_ack.setChecked(false);
                                    user_ack.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_yellow));
                                    user_rem.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_yellow));
                                    lblSpacer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_yellow));
                                    break;
                                case NO_FORCED:
                                case NO:
                                    user_ack.setChecked(false);
                                    user_ack.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_red));
                                    user_rem.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_red));
                                    lblSpacer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_red));
                                    break;
                                default:
                                    user_ack.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_grey));
                                    user_ack.setChecked(false);
                                    user_ack.setEnabled(false);
                                    lblSpacer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_grey));
                                    user_rem.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_grey));
                                    user_rem.setEnabled(false);
                                    break;
                            }
                            user_ack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    int retVal = 0;
                                    if(isChecked) {
                                        //if(cur.state == EventUser.EVENT_USER_STATE.YES_NOT_ACK) {
                                            // acknowledge user
                                            new updateEventUserState(cur.mID, mUser.mJoomlaPassword ,event.mID , (int)1, mUser.mID).execute();

                                        //}
                                    } else {
                                        //if((cur.state == EventUser.EVENT_USER_STATE.YES_ACK) || (cur.state == EventUser.EVENT_USER_STATE.NO)) {
                                            // not acknowledge user
                                            new updateEventUserState(cur.mID, mUser.mJoomlaPassword ,event.mID , (int)0, mUser.mID).execute();
                                        //}
                                    }
                                }
                            });

                            user_rem.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(final View v)
                                {
                                    new AlertDialog.Builder(NextvmActivity.this)
                                            .setTitle("")
                                            .setMessage(getString(R.string.ask_del_user))

                                            // Specifying a listener allows you to take an action before dismissing the dialog.
                                            // The dialog is automatically dismissed when a dialog button is clicked.
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    lastSelectedEventView = (LinearLayout) v.getParent().getParent();
                                                    new updateEventState( mUser.mID, mUser.mJoomlaPassword, event.mID, cur.mID,(int) 4, cur.mComment).execute();
                                                }
                                            })

                                            // A null listener allows the button to dismiss the dialog and take no further action.
                                            .setNegativeButton(android.R.string.no, null)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                }
                            });

                            llSingleEventUserEntry.addView(user_ack);
                            llSingleEventUserEntry.addView(user_rem);
                            llSingleEventUserEntry.addView(lblSpacer);


                            //TODO user aus event werfen einbauen
                        }

                        llUsers.addView(llSingleEventUserEntry);

                        if(cur.mComment.length() > 0) {
                            txtUserComment = new TextView(getApplicationContext());
                            txtUserComment.setText(cur.mComment);
                            txtUserComment.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_text));
                            txtUserComment.setPadding((int)pxFromDp(10f),2,0,2);


                            // Read your drawable from somewhere
                            Drawable dr = ContextCompat.getDrawable(getApplicationContext(), R.drawable.arrow_right_transp);
                            Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                            // Scale it to 50 x 50
                            Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap,(int) pxFromDp(10f), (int) pxFromDp(15f), true));
                            // Set your new, scaled drawable "d"
                            txtUserComment.setCompoundDrawablesWithIntrinsicBounds(d,null,null,null);
                            llUsers.addView(txtUserComment);
                        }


                        switch (cur.state) {
                            case NONE:
                                tv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_white));
                                if(txtUserComment != null) txtUserComment.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_white));
                                break;
                            case YES_NOT_ACK:
                                usersYesNACK++;
                                tv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_grey));
                                if(txtUserComment != null) txtUserComment.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_grey));
                                break;
                            case YES_ACK:
                                usersYes++;
                                tv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_green));
                                if(txtUserComment != null) txtUserComment.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_green));
                                break;
                            case NO:
                                usersNo++;
                                tv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_red));
                                if(txtUserComment != null) txtUserComment.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_red));
                                break;
                            case NO_FORCED:
                                usersNo++;
                                tv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_red));
                                if(txtUserComment != null) txtUserComment.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_red));
                                break;
                            case MAYBE:
                                usersMaybe++;
                                tv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_yellow));
                                if(txtUserComment != null) txtUserComment.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_yellow));
                                break;
                            default:
                                tv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_white));
                                if(txtUserComment != null) txtUserComment.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.tvm_white));
                                break;
                        }

//                        tv.setPadding((int)pxFromDp(5f),2,0,2);
//                        if(txtUserComment != null) txtUserComment.setPadding((int)pxFromDp(15f),2,0,2);
                    }



                    TextView txtStatistics = (TextView)  mEventViews.get(i).findViewById(currID).findViewById(R.id.txtStatistics);
                    txtStatistics.setText(String.format(getString(R.string.tvm_statistics), usersYes, usersYesNACK+usersYes , usersNo, usersMaybe));

                    CompoundButton.OnCheckedChangeListener btnListener = new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked) {
                                switch (buttonView.getId()) {
                                    case R.id.radioButtonYes:

                                        lastSelectedEventView = (LinearLayout)buttonView.getParent().getParent();
                                        new updateEventState(mUser.mID, mUser.mJoomlaPassword, event.mID,mUser.mID,  1, "").execute();
                                        break;
                                    case R.id.radioButtonNo:
                                        lastSelectedEventView = (LinearLayout)buttonView.getParent().getParent();
                                        new updateEventState(mUser.mID, mUser.mJoomlaPassword, event.mID,mUser.mID,  3, "").execute();
                                        break;
                                    case R.id.radioButtonMaybe:
                                        lastSelectedEventView = (LinearLayout)buttonView.getParent().getParent();
                                        new updateEventState(mUser.mID, mUser.mJoomlaPassword, event.mID, mUser.mID,2, "").execute();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    };
                    try {
                        btnYes.setOnCheckedChangeListener(btnListener);
                        btnYes.setTag(event.mID);
                        btnNo.setOnCheckedChangeListener(btnListener);
                        btnNo.setTag(event.mID);
                        btnMaybe.setOnCheckedChangeListener(btnListener);
                        btnMaybe.setTag(event.mID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein_view);

                for( int i = 0 ; i < mEventViews.size() ; i++) {
                    // Now Set your animation
                    mEventViews.get(i).startAnimation(fadeInAnimation);
                }
            }
        }
    }

    private void createUserNotification(TvmEvent event) {


        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent alarmIntent = new Intent(NextvmActivity.this, ShowNotification.class);
        alarmIntent.setAction("ShowNotificationAction");
        alarmIntent.putExtra("event", event);

        int newIntentID = (int) (Math.random()*100000);
        alarmIDsArray.add(String.valueOf(newIntentID));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(  NextvmActivity.this, newIntentID , alarmIntent, 0);

        Calendar alarmStartTime = Calendar.getInstance(TimeZone.getDefault());
        alarmStartTime.setTime(event.mDate);

        String time = settings.getString("rememberTime","0");
        if(time.length() < 1) {
            time = "0";
        }
        alarmStartTime.add(Calendar.MINUTE, -Integer.parseInt(time));
//        alarmStartTime.add(Calendar.SECOND, event.mUsers.size());

        if(alarmStartTime.getTimeInMillis() < System.currentTimeMillis()){
            // time was already, skip alarm
            return;
        } else {
            SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSZ");

            Log.d(TAG, "createUserNotification: " + "Alarm on : " + s.format(alarmStartTime.getTime()));

            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(), pendingIntent);
        }
    }

    private int getUserState(int eventID){
        int ret = -1;
        for(int i = 0 ; i < events.size() ; i++) {
            if(events.get(i).mID == eventID) {
                switch (events.get(i).mUserState) {
                    case NONE:
                        ret = 0;
                        break;
                    case YES_NOT_ACK:
                        ret =  1;
                        break;
                    case YES_ACK:
                        ret =  1;
                        break;
                    case NO:
                        ret =  3;
                        break;
                    case NO_FORCED:
                        ret =  3;
                        break;
                    case MAYBE:
                        ret =  2;
                        break;
                    default:
                        ret =  0;
                        break;
                }
            }
        }
        System.out.println(ret);
        return ret;
    }

    private void setUserState(int eventID, EventUser.EVENT_USER_STATE state){
        for(int i = 0 ; i < events.size() ; i++) {
            if(events.get(i).mID == eventID) {
                events.get(i).mUserState = state;
                if(state == EventUser.EVENT_USER_STATE.YES_ACK) {
                    events.get(i).mUserStateAck = true;
                } else {
                    events.get(i).mUserStateAck = false;
                }
                return;
            }
        }
    }

    private class updateEventUserState extends AsyncTask<Integer, Void , Integer> {

        int eventID;
        int ack;
        int userID;
        String pass;
        int editorID;
        JSONObject responseObj;

        private ProgressDialog dialog;

        /**
         *
         * @param userid    (int)     ID of the current user
         * @param pass      (String)  JOOMLA password of the current User
         * @param eventid        (int)     ID of the event
         * @param ack         (int)     new registration state
         * @param editorid         (int)     ID od editing user
         */
        public updateEventUserState(int userid, String pass, int eventid, int ack, int editorid) {
            this.userID = userid;
            this.pass = pass;
            this.eventID = eventid;
            this.ack = ack;
            this.editorID = editorid;
            dialog = new ProgressDialog(NextvmActivity.this);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.info_get_results_msg));
            dialog.setIndeterminate(true);
            if(!dialog.isShowing()){
                dialog.show();
            }
        }
        /**
         *
         * @param params
         * @return state
         */
        @Override
        protected Integer doInBackground(Integer... params) {
            HttpsURLConnection client = null;
            URL url = null;
            StringBuilder paramStr = new StringBuilder();

            // initiale SSL
            SSLContext sc = null;

            try {
                sc = SSLContext.getInstance("TLS");
                sc.init(null, null, new java.security.SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                mError = getString(R.string.error_ssl_failed);
            } catch (KeyManagementException e) {
                e.printStackTrace();
                mError = getString(R.string.error_ssl_failed);
            }


            try {
                paramStr.append("event_id="+ URLEncoder.encode(String.valueOf(eventID), "UTF-8"));
                paramStr.append("&pass="+ URLEncoder.encode(pass,"UTF-8"));
                paramStr.append("&user_id="+ URLEncoder.encode(String.valueOf(userID),"UTF-8"));
                paramStr.append("&ack="+ URLEncoder.encode(String.valueOf(ack),"UTF-8"));
                paramStr.append("&editor_id="+ URLEncoder.encode(String.valueOf(editorID),"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                url = new URL("https://rostockerseebaeren.de/?option=com_tvm&task=setEventUserAck&format=json");
                client = (HttpsURLConnection)url.openConnection();
                client.setConnectTimeout(CONNECTION_TIMEOUT);
                client.setReadTimeout(CONNECTION_TIMEOUT);
                client.setSSLSocketFactory(sc.getSocketFactory());
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                client.setRequestProperty("Content-Length", "" + Integer.toString(paramStr.length()));
                client.setRequestProperty("Content-Language", "en-US");

                client.setUseCaches (false);
                client.setDoInput(true);
                client.setDoOutput(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                //paramStr = URLEncoder.encode(paramStr,"UTF-8");
                DataOutputStream wr = new DataOutputStream(client.getOutputStream());
                wr.write(paramStr.toString().getBytes("UTF-8"));
                wr.flush();
                wr.close();

                int responseCode = client.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + paramStr);
                System.out.println("Response Code : " + responseCode);

            } catch (Exception e){
                e.printStackTrace();
            }

            StringBuffer response = new StringBuffer();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\r");
                }
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ioe){
                ioe.printStackTrace();
            }

            if(response.toString().equals("Access denied\r")){
                return -3;
            } else if(response.toString().equals("1\r")){
                return 1;
            } else{
                return -1;
            }
        }

        protected void onPostExecute(Integer s) {
            switch (s) {
                case 1:// entry successfully updated
                    break;
                case -3: // invalid password
                    Toast.makeText(getApplicationContext(), getString(R.string.error_not_authorized),Toast.LENGTH_LONG).show();
                    break;
                default: // invalid input values
                    Toast.makeText(getApplicationContext(), getString(R.string.error_unknown),Toast.LENGTH_LONG).show();
                    break;
            }

            if(dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }


    private class updateEventState extends AsyncTask<Integer, Void , Integer> {

        int eventID;
        int state;
        int userID;
        int eventUserID;
        String pass;
        String comment;
        JSONObject responseObj;

        private ProgressDialog dialog;

        /**
         *
         * @param userid    (int)     ID of the current user
         * @param pass      (String)  JOOMLA password of the current User
         * @param uid (int)  ID to the user to be modified
         * @param eid        (int)     ID of the event
         * @param s         (int)     new registration state
         * @param c         (String)  new comment of this entry
         */
        public updateEventState(int userid, String pass, int eid, int uid,  int s, String c) {
            this.userID = userid;
            this.pass = pass;
            this.eventUserID = uid;
            this.eventID = eid;
            this.state = s;
            this.comment = c;

            dialog = new ProgressDialog(NextvmActivity.this);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.info_get_results_msg));
            dialog.setIndeterminate(true);
            if(!dialog.isShowing()){
                dialog.show();
            }
        }
        /**
         *
         * @param params
         * @return state
         */
        @Override
        protected Integer doInBackground(Integer... params) {
            HttpsURLConnection client = null;
            URL url = null;
            StringBuilder paramStr = new StringBuilder();

            // initiale SSL
            SSLContext sc = null;

            try {
                sc = SSLContext.getInstance("TLS");
                sc.init(null, null, new java.security.SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                mError = getString(R.string.error_ssl_failed);
            } catch (KeyManagementException e) {
                e.printStackTrace();
                mError = getString(R.string.error_ssl_failed);
            }


            try {
                paramStr.append("id="+ URLEncoder.encode(String.valueOf(userID), "UTF-8"));
                paramStr.append("&pass="+ URLEncoder.encode(pass,"UTF-8"));
                paramStr.append("&userid="+ URLEncoder.encode(String.valueOf(eventUserID),"UTF-8"));
                paramStr.append("&event="+ URLEncoder.encode(String.valueOf(eventID),"UTF-8"));
                paramStr.append("&state="+ URLEncoder.encode(String.valueOf(state),"UTF-8"));
                paramStr.append("&comment="+ URLEncoder.encode(comment,"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                url = new URL("https://rostockerseebaeren.de/?option=com_tvm&task=updateEventRegistrationJSON&format=json");
                client = (HttpsURLConnection)url.openConnection();
                client.setConnectTimeout(CONNECTION_TIMEOUT);
                client.setReadTimeout(CONNECTION_TIMEOUT);
                client.setSSLSocketFactory(sc.getSocketFactory());
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                client.setRequestProperty("Content-Length", "" + Integer.toString(paramStr.length()));
                client.setRequestProperty("Content-Language", "en-US");

                client.setUseCaches (false);
                client.setDoInput(true);
                client.setDoOutput(true);
                //client.setChunkedStreamingMode(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                //paramStr = URLEncoder.encode(paramStr,"UTF-8");
                DataOutputStream wr = new DataOutputStream(client.getOutputStream());
                wr.write(paramStr.toString().getBytes("UTF-8"));
                wr.flush();
                wr.close();

                int responseCode = client.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + paramStr);
                System.out.println("Response Code : " + responseCode);

            } catch (Exception e){
                e.printStackTrace();
            }

            StringBuffer response = new StringBuffer();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\r");
                }
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ioe){
                ioe.printStackTrace();
            }

            if(response.equals("Access denied\r")){
                return -3;
            } else {
                // hier den status der antwort prüfen
                try {
                    responseObj = new JSONObject(response.toString());
                    return responseObj.getInt("updateState");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        }

        protected void onPostExecute(Integer s) {
            // DEBUG ONLY:
            if(responseObj == null) {
                //Crashlytics.setString("JSON Response", "null");
                //Crashlytics.log("empty response from server");
                Toast.makeText(getApplicationContext(), getString(R.string.error_response_empty),Toast.LENGTH_LONG).show();
                return;
            }
            //Crashlytics.setString("JSON Response", responseObj.toString());
            // END DEBUG
            // hier anzeige aktualisieren
            Button btnComment = (Button) lastSelectedEventView.findViewById(R.id.btnSetComment);
            switch (s) {
                case 0:// entry successfully updated
                    try {

                        switch (responseObj.getInt("userState")) {
                            case 0:
                                btnComment.setEnabled(false);
                                btnComment.setAlpha(0.5f);
                                lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_white));
                                setUserState(eventID, EventUser.EVENT_USER_STATE.NONE);
                                break;
                            case 1: // yes --> not ack --> grey
                                btnComment.setEnabled(true);
                                btnComment.setAlpha(1.0f);
                                if(responseObj.getInt("acknowledged") == 1) {
                                    lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_green));
                                    setUserState(eventID, EventUser.EVENT_USER_STATE.YES_ACK);
                                } else {
                                    lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_grey));
                                    setUserState(eventID, EventUser.EVENT_USER_STATE.YES_NOT_ACK);
                                }
                                break;
                            case 2: // maybe --> yellow
                                btnComment.setEnabled(true);
                                btnComment.setAlpha(1.0f);
                                lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_yellow));
                                setUserState(eventID, EventUser.EVENT_USER_STATE.MAYBE);
                                break;
                            case 3: // (forced) no --> red
                                btnComment.setEnabled(true);
                                btnComment.setAlpha(1.0f);
                                setUserState(eventID, EventUser.EVENT_USER_STATE.NO);
                                lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_red));
                                break;
                            case 4:
                                btnComment.setEnabled(true);
                                btnComment.setAlpha(1.0f);
                                setUserState(eventID, EventUser.EVENT_USER_STATE.NO_FORCED);
                                lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_red));
                                break;
                            default:
                                btnComment.setEnabled(false);
                                btnComment.setAlpha(0.5f);
                                lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_white));
                                setUserState(eventID, EventUser.EVENT_USER_STATE.NONE);
                                break;
                        }
                        break;
                    } catch (JSONException jex) {
                        jex.printStackTrace();
                    } catch (NullPointerException nex) {
                        nex.printStackTrace();
                    }
                    break;
                case 1:// new entry created
                    try {
                        switch (responseObj.getInt("userState")) {
                            case 0:
                                btnComment.setEnabled(false);
                                btnComment.setAlpha(0.5f);
                                lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_white));
                                setUserState(eventID, EventUser.EVENT_USER_STATE.NONE);
                                break;
                            case 1: // yes --> not ack --> grey
                                btnComment.setEnabled(true);
                                btnComment.setAlpha(1.0f);
                                lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_grey));
                                setUserState(eventID, EventUser.EVENT_USER_STATE.YES_NOT_ACK);
                                break;
                            case 2: // maybe --> yellow
                                btnComment.setEnabled(true);
                                btnComment.setAlpha(1.0f);
                                lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_yellow));;
                                setUserState(eventID, EventUser.EVENT_USER_STATE.MAYBE);
                                break;
                            case 3: // (forced) no --> red
                                btnComment.setEnabled(true);
                                btnComment.setAlpha(1.0f);
                                setUserState(eventID, EventUser.EVENT_USER_STATE.NO);
                                lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_red));
                                break;
                            case 4:
                                btnComment.setEnabled(true);
                                btnComment.setAlpha(1.0f);
                                setUserState(eventID, EventUser.EVENT_USER_STATE.NO_FORCED);
                                lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_red));
                                break;
                            default:
                                btnComment.setEnabled(false);
                                btnComment.setAlpha(0.5f);
                                lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_white));
                                setUserState(eventID, EventUser.EVENT_USER_STATE.NONE);
                                break;
                        }
                        break;
                    } catch (JSONException jex) {
                        jex.printStackTrace();
                    }
                    break;
                case -1: // event closed
                    lastSelectedEventView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_red));
                    if(lastSelectedEventView.findViewById(R.id.rgChoice) != null) {
                        lastSelectedEventView.findViewById(R.id.rgChoice).setVisibility(View.GONE);
                    } else {
                        ((LinearLayout)lastSelectedEventView.getParent()).findViewById(R.id.rgChoice).setVisibility(View.GONE);
                    }
                    break;
                case -2: // event not available
                    Toast.makeText(getApplicationContext(),getString(R.string.error_event_not_found),Toast.LENGTH_LONG);
                    break;
                case -3: // invalid input values
                    Toast.makeText(getApplicationContext(),getString(R.string.error_event_invalid_input),Toast.LENGTH_LONG);
                    break;
            }

            if(dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private float pxFromDp(float dp)
    {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float dpFromPx(float px)
    {
        return px / getResources().getDisplayMetrics().density;
    }

    private void getCategories(int userID, String password){
        HttpsURLConnection client = null;
        URL url = null;
        String paramStr = null;

        // initiale SSL
        SSLContext sc = null;

        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            mError = getString(R.string.error_ssl_failed);
        } catch (KeyManagementException e) {
            e.printStackTrace();
            mError = getString(R.string.error_ssl_failed);
        }

        try {
            paramStr = "id="+ URLEncoder.encode(String.valueOf(userID), "UTF-8") + "&pass="+ URLEncoder.encode(password,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            url = new URL("https://rostockerseebaeren.de/?option=com_tvm&task=getTVMCategoriesJSON&format=json");
            client = (HttpsURLConnection)url.openConnection();
            client.setSSLSocketFactory(sc.getSocketFactory());
            client.setRequestMethod("POST");
            client.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            client.setRequestProperty("Content-Length", "" + Integer.toString(paramStr.getBytes().length));
            client.setRequestProperty("Content-Language", "en-US");

            client.setUseCaches (false);
            client.setDoInput(true);
            client.setDoOutput(true);
            //client.setChunkedStreamingMode(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //paramStr = URLEncoder.encode(paramStr,"UTF-8");
            DataOutputStream wr = new DataOutputStream(client.getOutputStream());
            wr.write(paramStr.getBytes("UTF-8"));
            wr.flush();
            wr.close();

            int responseCode = client.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + paramStr);
            System.out.println("Response Code : " + responseCode);

        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),getString(R.string.error_no_internet),Toast.LENGTH_LONG);
        }

        StringBuffer response = new StringBuffer();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\r");
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }

        if(response.equals("Access denied\r")){
            return;
        } else {
            if(categories != null) {
                categories = null;
            }
            categories = new ArrayList<TvmCategory>();
            try {
                JSONArray jasonArray = new JSONArray(response.toString());
                for(int i = 0 ; i < jasonArray.length() ; i++) {
                    try{
                        categories.add(new TvmCategory(jasonArray.getJSONObject(i)));
                    } catch (JSONException jex) {
                        jex.printStackTrace();
                        mError = getString(R.string.error_broken_category);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    private void getLatestBoardPosts(int userID, String password){
        HttpsURLConnection client = null;
        URL url = null;
        String paramStr = null;

        // initiale SSL
        SSLContext sc = null;

        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            mError = getString(R.string.error_ssl_failed);
        } catch (KeyManagementException e) {
            e.printStackTrace();
            mError = getString(R.string.error_ssl_failed);
        }

        try {
            paramStr = "id="+ URLEncoder.encode(String.valueOf(userID), "UTF-8") + "&pass="+ URLEncoder.encode(password,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            url = new URL("https://rostockerseebaeren.de/?option=com_tvm&task=getTVMBoardPostsJSON&format=json");
            client = (HttpsURLConnection)url.openConnection();
            client.setSSLSocketFactory(sc.getSocketFactory());
            client.setRequestMethod("POST");
            client.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            client.setRequestProperty("Content-Length", "" + Integer.toString(paramStr.getBytes().length));
            client.setRequestProperty("Content-Language", "en-US");

            client.setUseCaches (false);
            client.setDoInput(true);
            client.setDoOutput(true);
            //client.setChunkedStreamingMode(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //paramStr = URLEncoder.encode(paramStr,"UTF-8");
            DataOutputStream wr = new DataOutputStream(client.getOutputStream());
            wr.write(paramStr.getBytes("UTF-8"));
            wr.flush();
            wr.close();

            int responseCode = client.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + paramStr);
            System.out.println("Response Code : " + responseCode);

        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),getString(R.string.error_no_internet),Toast.LENGTH_LONG);
        }

        StringBuffer response = new StringBuffer();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\r");
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }

        if(response.equals("Access denied\r")){
            return;
        } else {

            if(boardPosts != null) {
                boardPosts = null;
            }
            boardPosts = new ArrayList<BoardPost>();
            try {
                JSONArray jasonArray = new JSONArray(response.toString());
                for(int i = 0 ; i < jasonArray.length() ; i++) {
                    try{
                        boardPosts.add(new BoardPost(jasonArray.getJSONObject(i)));
                        if(boardPosts.get(i).mTopicLastUpdate > mUser.mLastLogin) {
                            newBoardPosts = true;
                        }
                    } catch (JSONException jex) {
                        jex.printStackTrace();
                        mError = getString(R.string.error_broken_lastposts);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    private void getEvents(int userID, String password, int category){
        HttpsURLConnection client = null;
        URL url = null;
        String paramStr = null;

        // initiale SSL
        SSLContext sc = null;

        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            mError = getString(R.string.error_ssl_failed);
        } catch (KeyManagementException e) {
            e.printStackTrace();
            mError = getString(R.string.error_ssl_failed);
        }

        try {
            paramStr = "id="+ URLEncoder.encode(String.valueOf(userID), "UTF-8") + "&pass="+ URLEncoder.encode(password,"UTF-8") + "&category=" + URLEncoder.encode(String.valueOf(category),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            url = new URL("https://rostockerseebaeren.de/?option=com_tvm&task=getTVMEventsJSON&format=json");
            client = (HttpsURLConnection)url.openConnection();
            client.setSSLSocketFactory(sc.getSocketFactory());
            client.setRequestMethod("POST");
            client.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            client.setRequestProperty("Content-Length", "" + Integer.toString(paramStr.getBytes().length));
            client.setRequestProperty("Content-Language", "en-US");

            client.setUseCaches (false);
            client.setDoInput(true);
            client.setDoOutput(true);
            ////client.setChunkedStreamingMode(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //paramStr = URLEncoder.encode(paramStr,"UTF-8");
            DataOutputStream wr = new DataOutputStream(client.getOutputStream());
            wr.write(paramStr.getBytes("UTF-8"));
            wr.flush();
            wr.close();

            int responseCode = client.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + paramStr);
            System.out.println("Response Code : " + responseCode);

        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),getString(R.string.error_no_internet),Toast.LENGTH_LONG);
        }

        StringBuffer response = new StringBuffer();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\r");
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }

        if(response.equals("Access denied\r")){
            return;
        } else {
            if(events != null) {
                events = null;
            }
            events = new ArrayList<TvmEvent>();
            try {
                JSONArray jasonArray = new JSONArray(response.toString());
                for(int i = 0 ; i < jasonArray.length() ; i++) {
                    try{
                        events.add(new TvmEvent(jasonArray.getJSONObject(i)));
                    } catch (JSONException jex) {
                        jex.printStackTrace();
                        mError = getString(R.string.error_broken_event);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = settings.edit();

        HashSet<String> intentIDs = new HashSet<String>();
        if(alarmIDsArray != null){
            intentIDs.addAll(alarmIDsArray);
        }
        //TODO Hier die alten forenposts abspeichern oder deren zeitstempel...
        editor.putStringSet("alarms", intentIDs);
        editor.commit();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Nextvm Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://de.rostockerseebaeren.nextvmapp/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
//        client.disconnect();
    }
}
