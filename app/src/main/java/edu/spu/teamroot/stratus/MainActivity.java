package edu.spu.teamroot.stratus;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends ActionBarActivity implements PopupMenu.OnMenuItemClickListener {
    public static final String VC_EXT_PATH = Environment.getExternalStorageDirectory() + "/Stratus/";
    public static final String VC_SCREENSHOTS_PATH = VC_EXT_PATH + "/Screenshots/";

    private static Toast lastToast;

    private static final int EXIT_DELAY = 3000;
    private boolean exitPressed = false;

    private ImageButton mainButton;
    private ImageButton resetButton;
    private ImageButton menuButton;

    private Context context = this;

    private TwoDScrollView scrollView;
    private WordCloudLayout cloudLayout;

    private static boolean destroyInstances = false;
    private boolean isRunning = true;

    private Messenger mServiceMessenger;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ServiceConnection", "onServiceConnected(" + name + "," + service + ")");

            Log.d("MainActivity", "Old mServiceMessenger is " + ((mServiceMessenger == null) ? "null" : mServiceMessenger.toString()));
            mServiceMessenger = new Messenger(service);
            Log.d("MainActivity", "New mServiceMessenger is " + mServiceMessenger.toString());

            try {
                if (isRunning) {
                    mServiceMessenger.send(Message.obtain(null, SpeechRecognitionService.MSG_RECOGNIZER_START_LISTENING));
                } else {
                    mServiceMessenger.send(Message.obtain(null, SpeechRecognitionService.MSG_RECOGNIZER_STOP_LISTENING));
                }
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

    /*
     * Activity Lifecycle
     */

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save play/pause state
        outState.putBoolean("isRunning", isRunning);

        // Store WordCloud
        /*
        JSONObject obj = WordCloud.getInstance().toJSON();
        outState.putString("JSON", obj.toString());
        */

        // Store layout positions
        Log.d("onSaveInstanceState", "ScaleFactor: " + scrollView.getScaleFactor() + " Scroll: (" + scrollView.getScrollX() + "," + scrollView.getScrollY() + ")");
        Log.d("onSaveInstanceState", "Pivot: " + cloudLayout.getPivotX() + "," + cloudLayout.getPivotY());

        outState.putFloat("PivotX", cloudLayout.getPivotX());
        outState.putFloat("PivotY", cloudLayout.getPivotY());
        outState.putFloat("ScaleFactor", scrollView.getScaleFactor());

        // Save scroll position relative to center
        outState.putInt("ScrollX", scrollView.getScrollX());
        outState.putInt("ScrollY", scrollView.getScrollY());

        Log.d("MainActivity", "onSaveInstanceState(" + outState + ")");

        // Cache bundle in cloud instance for safe keeping
        WordCloud.getInstance().pushSavedBundle(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("MainActivity", "onRestoreInstanceState(" + savedInstanceState + ")");

        super.onRestoreInstanceState(savedInstanceState);

        // Load play/pause state (do not animate when loading state)
        setRunning(savedInstanceState.getBoolean("isRunning"), false);

        // Load WordCloud
        /*
        String json = savedInstanceState.getString("JSON");

        try {
            WordCloud.getInstance().fromJSON(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */

        // Load layout positions
        cloudLayout.setPivotX(savedInstanceState.getFloat("PivotX"));
        cloudLayout.setPivotY(savedInstanceState.getFloat("PivotY"));
        scrollView.setScaleFactor(savedInstanceState.getFloat("ScaleFactor"));

        scrollView.scrollToWhenReady(
                savedInstanceState.getInt("ScrollX"),
                savedInstanceState.getInt("ScrollY"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate(" + savedInstanceState + "):" + destroyInstances);
        destroyInstances = false;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // TODO Refactor onCreate to be cleaner (organize by initialize views, save/load, SRS, etc.)

        // Get screen size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Get scroll view
        scrollView = (TwoDScrollView)findViewById(R.id.WordCloudScrollView);
        scrollView.setScreenSize(size);

        // Create UnitConverter
        UnitConverter.createInstance(this);

        // Create a RelativeLayout element
        cloudLayout = new WordCloudLayout(this);
        // Add extra padding along the bottom and right so words are not squished
        cloudLayout.setPadding(0, 0, UnitConverter.getInstance().toPx(WordCloud.PADDING), UnitConverter.getInstance().toPx(WordCloud.PADDING * 4));

        // Add the RelativeLayout element to the ScrollView
        int sideLength = Math.max(size.x, size.y);
        int scrollViewWidth = (int)(sideLength * 1.5);
        int scrollViewHeight = (int)(sideLength * 1.5);

        scrollView.removeAllViews();
        scrollView.addView(cloudLayout, scrollViewWidth, scrollViewHeight);

        // Move to center of the ScrollView
        if (savedInstanceState == null) {
            scrollView.scrollToWhenReady(
                    (scrollViewWidth / 2) - (size.x / 2),
                    (scrollViewHeight / 2) - (size.y / 2));
        }

        // Are we creating a new WordCloud instance?
        boolean isNewCloud = (WordCloud.getInstance() == null);

        // Create new instances
        WordCloud.createInstance(context, cloudLayout);
        ExclusionList.createInstance(context);

        if (savedInstanceState == null && isNewCloud) {
            ExclusionList.getInstance().load();
        }

        // Set button onClick handlers
        mainButton = (ImageButton)findViewById(R.id.main_button);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.setRunning(!isRunning);
            }
        });

        resetButton = (ImageButton)findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WordCloud.getInstance().clear();
            }
        });

        menuButton = (ImageButton)findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });

        /* Tests for running on emulator
        if (savedInstanceState == null && isNewCloud) {
            final String words[] = {
                    "voice", "cloud", "is", "an", "android", "application", "designed", "to",
                    "visualize", "conversation", "analyze", "communication", "and", "enhance", "learning",
                    "this", "is", "an", "example", "of", "a", "word", "cloud",
                    "size", "based", "on", "count"
            };

            for (int i = 0; i < words.length; i++) {
                final String word = words[i];

                cloudLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WordCloud.getInstance().addWord(word, new Random().nextInt(1) + 1);
                    }
                }, i);
            }
        }
        //*/

        // Load state from saved bundle
        if (savedInstanceState == null && !isNewCloud) {
            Bundle savedBundle = WordCloud.getInstance().popSavedBundle();

            if (savedBundle != null) {
                onRestoreInstanceState(savedBundle);
            }
        }

        // Start SpeechRecognitionService
        ///*
        final Intent speechRecognitionService = new Intent(this, SpeechRecognitionService.class);
        this.startService(speechRecognitionService);
        //mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;
        //*/

        bindService(new Intent(this, SpeechRecognitionService.class), mServiceConnection, BIND_AUTO_CREATE | BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onStart() {
        Log.d("MainActivity", "onStart():" + destroyInstances);
        destroyInstances = false;
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d("MainActivity", "onRestart():" + destroyInstances);
        destroyInstances = false;
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.d("MainActivity", "onResume():" + destroyInstances);
        destroyInstances = false;
        super.onResume();

        // Restore running state (sometimes can get out of sync)
        setRunning(isRunning, false);
    }

    @Override
    protected void onPause() {
        Log.d("MainActivity", "onPause():" + destroyInstances);

        // Set destroyInstances to true... this will allow onDestroy to destroy instances
        destroyInstances = true;

        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("MainActivity", "onStop():" + destroyInstances);

        if (ExclusionList.getInstance() != null) {
            ExclusionList.getInstance().save();
        } else {
            Log.d("MainActivity", "Exclusion list instance was destroyed!!!");
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("MainActivity", "onDestroy():" + destroyInstances);

        /*
        if (mServiceMessenger != null) {
            Log.d("MainActivity", "mServiceMessenger is " + mServiceMessenger.toString());

            // Try to stop the service
            try {
                mServiceMessenger.send(Message.obtain(null, SpeechRecognitionService.MSG_SERVICE_KILL));
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            mServiceMessenger = null;
        } else {
            Log.d("MainActivity", "mServiceMessenger is already null!");
        }
        //*/

        // Unbind our connection (must be done everytime--avoids leaks!)
        unbindService(mServiceConnection);

        // Perform final cleanup
        if (isFinishing() && destroyInstances) {
            Log.d("MainActivity", "Performing final cleanup...");
            Log.d("MainActivity", "Deleting singleton instances");

            WordCloud.deleteInstance();
            ExclusionList.deleteInstance();
            UnitConverter.deleteInstance();

            Log.d("MainActivity", "Stopping service");
            stopService(new Intent(this, SpeechRecognitionService.class));
        }

        super.onDestroy();
    }

    /*
     * Methods
     */

    @Override
    public void onBackPressed() {
        if (exitPressed) {
            lastToast.cancel();
            super.onBackPressed();
        } else {
            exitPressed = true;
            showToast("Press again to exit", Toast.LENGTH_LONG);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exitPressed = false;
                }
            }, EXIT_DELAY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.quick_menu, menu);
        showMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass selected items to menu item click listener
        return onMenuItemClick(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            openSettings();
        } else if (id == R.id.save_cloud) {
            showToast("Saving Cloud...", Toast.LENGTH_SHORT);

            String title;
            String message = "Save Word Cloud";
            final String cloudID = WordCloud.getInstance().saveWordCloud();

            if (cloudID.equals("none")) {
                title = "Not Saved :(";
                message = "We couldn't talk with the server. Do you have an internet connection?";
            }
            else {
                title = "Saved Successfully!";
                message = cloudID;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(WordCloud.context);
            builder.setMessage(message)
                    .setTitle(title)
                    .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("View on Web", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String url = "http://voicecloudapp.com/WordCloud/cloudview/" + cloudID;
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            context.startActivity(i);
                            dialog.dismiss();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();

            // Set text size
            TextView textView = (TextView) alert.findViewById(android.R.id.message);
            textView.setTextSize(40);

            return true;
        } else if (id == R.id.load_cloud) {
            // Get ID
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Load Word Cloud");

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
            input.setHint("CLOUDID");
            input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
            input.setPadding(50, 50, 50, 50);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Load cloud
                    showToast("Loading Cloud...", Toast.LENGTH_SHORT);

                    if (!WordCloud.getInstance().loadWordCloud(input.getText().toString())) {
                        showToast("Loading Cloud Failed", Toast.LENGTH_SHORT);
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else if (id == R.id.save_screen) {
            showToast("Saving screenshot...", Toast.LENGTH_LONG);

            final Handler mainHandler = new Handler();

            new Thread() {
                @Override
                public void run() {
                    try {
                        // Draw the word cloud to a bitmap
                        Bitmap bmp = Bitmap.createBitmap(
                                UnitConverter.getInstance().toDp(cloudLayout.getWidth()),
                                UnitConverter.getInstance().toDp(cloudLayout.getHeight()),
                                Bitmap.Config.ARGB_8888);

                        float density = 1 / UnitConverter.getInstance().toPxFloat(1);

                        Canvas canvas = new Canvas(bmp);
                        canvas.scale(density, density);
                        canvas.drawColor(Color.WHITE);

                        cloudLayout.draw(canvas);

                        // Cook up filename
                        final String date = new SimpleDateFormat("MM-dd-yy-kkmmss").format(Calendar.getInstance().getTime());
                        final String filename = "Cloud_" + date + ".png";

                        // Try to create directory
                        File folder = new File(VC_SCREENSHOTS_PATH);
                        folder.mkdirs();

                        // Try to create file
                        File file = new File(VC_SCREENSHOTS_PATH + "Cloud-" + date + ".png");
                        file.createNewFile();

                        FileOutputStream outStream = new FileOutputStream(file);
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.close();

                        // Add screenshot to gallery
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(file);
                        mediaScanIntent.setData(contentUri);
                        sendBroadcast(mediaScanIntent);

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showToast("Saved screenshot to photo gallery:\n" + filename, Toast.LENGTH_LONG);
                            }
                        });
                    } catch (Exception e) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showToast("Error saving screenshot!", Toast.LENGTH_SHORT);
                            }
                        });
                    }
                }
            }.start();

        } else if (id == R.id.view_exclude_list) {
            openExclusion();
        }

        return false;
    }

    // Shows a toast message, removing old toasts if needed.
    public Toast showToast(String message, int duration) {
        if (lastToast != null) lastToast.cancel();
        lastToast = Toast.makeText(this, message, duration);
        lastToast.show();

        return lastToast;
    }

    // Shows the settings menu.
    public void showMenu() {
        PopupMenu popup = new PopupMenu(menuButton.getContext(), menuButton);
        popup.inflate(R.menu.quick_menu);
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.show();
    }

    // Opens the exclusion list activity.
    public void openExclusion() {
        Intent intent = new Intent(this, ExclusionActivity.class);
        startActivity(intent);
    }

    // Opens the settings activity.
    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // Sets the running state of the app. Defaults to show button animation.
    public void setRunning(boolean running) {
        setRunning(running, true);
    }

    // Sets the running state of the app. The animate flag determines if the button will animate.
    public void setRunning(boolean running, boolean animate) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Vector transformation animations not supported!
            Log.d("setRunning", "Vector animations not supported by SDK version!");
            animate = false;
        }

        isRunning = running;

        if (isRunning) {
            // Start SRS
            if (mServiceMessenger != null) {
                try {
                    mServiceMessenger.send(Message.obtain(null, SpeechRecognitionService.MSG_RECOGNIZER_START_LISTENING));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            if (animate) {
                mainButton.setImageResource(R.drawable.avd_play2pause);

                Animatable anim = (Animatable) mainButton.getDrawable();
                if (anim != null) {
                    anim.start();
                }
            } else {
                mainButton.setImageResource(R.drawable.vc_pause);
            }
        } else {
            // Stop SRS
            if (mServiceMessenger != null) {
                try {
                    mServiceMessenger.send(Message.obtain(null, SpeechRecognitionService.MSG_RECOGNIZER_STOP_LISTENING));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            if (animate) {
                mainButton.setImageResource(R.drawable.avd_pause2play);

                Animatable anim = (Animatable) mainButton.getDrawable();
                if (anim != null) {
                    anim.start();
                }
            } else {
                mainButton.setImageResource(R.drawable.vc_play);
            }
        }
    }
}