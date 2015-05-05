package edu.spu.teamroot.voicecloud;

import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends ActionBarActivity {
    private static Toast lastToast;

    private ImageButton mainButton;
    private ImageButton resetButton;
    private ImageButton menuButton;

    private Context context = this;

    private TwoDScrollView scrollView;
    private RelativeLayout rl;

    private boolean isRunning = true;

    private static float MIN_ZOOM = 1f;
    private static float MAX_ZOOM = 5f;

    private float scaleFactor = 1f;
    private ScaleGestureDetector scaleDetector;

    private int mBindFlag;
    private Messenger mServiceMessenger;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mServiceMessenger = new Messenger(service);
            Message startMessage = Message.obtain(null, SpeechRecognitionService.MSG_RECOGNIZER_START_LISTENING);

            try {
                mServiceMessenger.send(startMessage);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceMessenger = null;
        }

    };

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, SpeechRecognitionService.class), mServiceConnection, mBindFlag);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mServiceMessenger != null) {
            unbindService(mServiceConnection);
            mServiceMessenger = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get scroll view
        scrollView = (TwoDScrollView)findViewById(R.id.WordCloudScrollView);

        // Create UnitConverter
        UnitConverter.createInstance(this);

        // Create a RelativeLayout element
        rl = new RelativeLayout(this);

        // Add the RelativeLayout element to the ScrollView
        int scrollViewWidth = 3000;
        int scrollViewHeight = 3000;
        scrollView.addView(rl, UnitConverter.getInstance().toPx(scrollViewWidth), UnitConverter.getInstance().toPx(scrollViewHeight));

        // Move to center of the ScrollView
        scrollView.scrollToWhenReady(UnitConverter.getInstance().toPx(scrollViewWidth / 2), UnitConverter.getInstance().toPx(scrollViewHeight / 2));

        // Create new instances
        Blacklist.createInstance();
        WordCloud.createInstance(context, rl);
        Preprocessor.createInstance();

        // Start SpeechRecognitionService
        final Intent speechRecognitionService = new Intent(this, SpeechRecognitionService.class);
        this.startService(speechRecognitionService);
        mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;

        mainButton = (ImageButton)findViewById(R.id.main_button);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Play", Toast.LENGTH_SHORT).show();
                ImageButton button = (ImageButton)v;
                if (isRunning) {
                    isRunning = false; // Now we are paused
                    //button.setImageResource(R.mipmap.play_icon);

                    // Stop listening
                    Message stopMessage = Message.obtain(null, SpeechRecognitionService.MSG_RECOGNIZER_STOP_LISTENING);
                    try {
                        mServiceMessenger.send(stopMessage);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    button.setImageResource(R.drawable.avd_pause2play);

                    Animatable anim = (Animatable)button.getDrawable();
                    if (anim != null) {
                        anim.start();
                    }
                } else {
                    isRunning = true; // Now we are running
                    //button.setImageResource(R.mipmap.pause_icon);
                    Message startMessage = Message.obtain(null, SpeechRecognitionService.MSG_RECOGNIZER_START_LISTENING);
                    try {
                        mServiceMessenger.send(startMessage);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    button.setImageResource(R.drawable.avd_play2pause);

                    Animatable anim = (Animatable)button.getDrawable();
                    if (anim != null) {
                        anim.start();
                    }
                }
            }
        });

        resetButton = (ImageButton)findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                WordCloud.getInstance().clear();
                //WordCloud.deleteInstance();
                //Blacklist.deleteInstance();
                //UnitConverter.deleteInstance();

                //Intent intent = getIntent();
                //finish();
                //startActivity(intent);
            }
        });

        menuButton = (ImageButton)findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.menu_main);
                popup.show();
            }
        });

        /*
        rl.postDelayed(new Runnable() {
            @Override
            public void run() {
                wordCloud.processWord("Team", 1);
            }
        }, 2000);

        rl.postDelayed(new Runnable() {
            @Override
            public void run() {
                wordCloud.processWord("Root", 1);
            }
        }, 4000);

        rl.postDelayed(new Runnable() {
            @Override
            public void run() {
            wordCloud.processWord("Washingtonian", 1);
            }
        }, 6000);

        rl.postDelayed(new Runnable() {
            @Override
            public void run() {
                wordCloud.processWord("Geotastic", 1);
            }
        }, 8000);

        rl.postDelayed(new Runnable() {
            @Override
            public void run() {
                wordCloud.processWord("Awesome", 1);
            }
        }, 10000);

        rl.postDelayed(new Runnable() {
            @Override
            public void run() {
                wordCloud.processWord("Weltz", 1);
            }
        }, 12000);

        rl.postDelayed(new Runnable() {
            @Override
            public void run() {
                wordCloud.processWord("Ok", 1);
            }
        }, 14000);

        rl.postDelayed(new Runnable() {
            @Override
            public void run() {
                wordCloud.processWord("Geotastic", 1);
            }
        }, 16000);

        rl.postDelayed(new Runnable() {
            @Override
            public void run() {
                wordCloud.processWord("Geotastic", 3);
            }
        }, 17000);

        rl.postDelayed(new Runnable() {
            @Override
            public void run() {
                wordCloud.processWord("Team", 20);
                wordCloud.processWord("Root", 50);
                wordCloud.processWord("Awesome", 10);
            }
        }, 18000);

        rl.postDelayed(new Runnable() {
            @Override
            public void run() {
                wordCloud.processWord("Washingtonian", 15);
                wordCloud.processWord("Weltz", 20);
            }
        }, 20000);
        //*/
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openAlert(View view, Button button) {
        // custom dialog
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.word_action_dialog);
        String dialogTitle = "\"" + button.getText() + "\"";
        dialog.setTitle(dialogTitle);

        ListView listView = (ListView) dialog.findViewById(R.id.listView);

        ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> curItemMap;

        curItemMap = new HashMap<>();
        curItemMap.put("icon", String.valueOf(R.mipmap.count_icon));
        curItemMap.put("iconText", "12");
        curItemMap.put("label", "Occurrences");
        dataList.add(curItemMap);

        curItemMap = new HashMap<>();
        curItemMap.put("icon", String.valueOf(R.mipmap.count_icon));
        curItemMap.put("iconText", "6");
        curItemMap.put("label", "Occurrences Per Minute");
        dataList.add(curItemMap);

        curItemMap = new HashMap<>();
        curItemMap.put("icon", String.valueOf(R.mipmap.google_icon));
        curItemMap.put("iconText", "");
        curItemMap.put("label", "Search with Google");
        dataList.add(curItemMap);

        curItemMap = new HashMap<>();
        curItemMap.put("icon", String.valueOf(R.mipmap.wiki_icon));
        curItemMap.put("iconText", "");
        curItemMap.put("label", "Lookup on Wikipedia");
        dataList.add(curItemMap);

        curItemMap = new HashMap<>();
        curItemMap.put("icon", String.valueOf(R.mipmap.remove_icon));
        curItemMap.put("iconText", "");
        curItemMap.put("label", "Remove from Word Cloud");
        dataList.add(curItemMap);

        //curItemMap = new HashMap<>();
        //curItemMap.put("icon", String.valueOf(R.mipmap.quizlet_icon));
        //curItemMap.put("iconText", "");
        //curItemMap.put("label", "Send to Quizlet");
        //dataList.add(curItemMap);

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, dataList, R.layout.word_action_row, new String[] {"icon", "iconText", "label"}, new int[] {R.id.icon, R.id.iconText ,R.id.label});

        listView.setAdapter(simpleAdapter);

        dialog.show();
    }


}