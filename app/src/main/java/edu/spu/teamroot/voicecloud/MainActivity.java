package edu.spu.teamroot.voicecloud;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Animatable;
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
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
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
import java.util.Random;

public class MainActivity extends ActionBarActivity implements PopupMenu.OnMenuItemClickListener {
    public static final String VC_PATH = Environment.getExternalStorageDirectory() + "/VoiceCloud/";

    private static Toast lastToast;

    private ImageButton mainButton;
    private ImageButton resetButton;
    private ImageButton menuButton;

    private Context context = this;

    private TwoDScrollView scrollView;
    private WordCloudLayout cloudLayout;

    private boolean isRunning = true;

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
        Log.d("MainActivity", "onStart");

        super.onStart();
        bindService(new Intent(this, SpeechRecognitionService.class), mServiceConnection, mBindFlag);
    }

    @Override
    protected void onStop() {
        Log.d("MainActivity", "onStop");

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("MainActivity", "onDestroy");

        super.onDestroy();

        try {
            mServiceMessenger.send(Message.obtain(null, SpeechRecognitionService.MSG_SERVICE_KILL));
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

        if (mServiceMessenger != null) {
            unbindService(mServiceConnection);
            mServiceMessenger = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Add the RelativeLayout element to the ScrollView
        int scrollViewWidth = 3000;
        int scrollViewHeight = 3000;
        scrollView.addView(cloudLayout, UnitConverter.getInstance().toPx(scrollViewWidth), UnitConverter.getInstance().toPx(scrollViewHeight));

        // Move to center of the ScrollView
        scrollView.scrollToWhenReady(
                UnitConverter.getInstance().toPx(scrollViewWidth / 2) - (size.x / 2),
                UnitConverter.getInstance().toPx(scrollViewHeight / 2) - (size.y / 2) );

        // Create new instances
        Blacklist.createInstance();
        WordCloud.createInstance(context, cloudLayout);
        Preprocessor.createInstance();

        // Start SpeechRecognitionService
        final Intent speechRecognitionService = new Intent(this, SpeechRecognitionService.class);
        this.startService(speechRecognitionService);
        mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;

        mainButton = (ImageButton)findViewById(R.id.main_button);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton button = (ImageButton)v;
                if (isRunning) {
                    isRunning = false; // Now we are paused

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
            popup.setOnMenuItemClickListener(MainActivity.this);
            }
        });

        /* Tests for running on emulator
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
                    WordCloud.getInstance().addWord(word, new Random().nextInt(10) + 2);
                }
            }, i);
        }
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.save_cloud) {

            Toast.makeText(this, "Saving Cloud...", Toast.LENGTH_SHORT).show();

            String title;
            String message;
            String cloudID = WordCloud.getInstance().saveWordCloud();

            if (cloudID.equals("none")) {
                title = "Not Saved :(";
                message = "Your word cloud could not be saved. We couldn't talk with the server. Do you have an internet connection?";
            }
            else {
                title = "Saved Successfully!";
                message = "CLOUDID: " + cloudID;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(WordCloud.context);
            builder.setMessage(message)
                    .setTitle(title)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

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
                    Toast.makeText(MainActivity.this, "Loading Cloud...", Toast.LENGTH_SHORT).show();
                    if (!WordCloud.getInstance().loadWordCloud(input.getText().toString()))
                        Toast.makeText(MainActivity.this, "Loading Cloud Failed", Toast.LENGTH_SHORT).show();
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
            final Toast saveToast = Toast.makeText(this, "Saving screenshot...", Toast.LENGTH_LONG);
            saveToast.show();

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
                        File folder = new File(VC_PATH);
                        folder.mkdir();

                        // Try to create file
                        File file = new File(VC_PATH + "Cloud-" + date + ".png");
                        file.createNewFile();

                        FileOutputStream outStream = new FileOutputStream(file);
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.close();

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                saveToast.cancel();
                                Toast.makeText(MainActivity.this, "Saved screenshot to:\n" + VC_PATH + filename, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                saveToast.cancel();
                                Toast.makeText(MainActivity.this, "Error saving screenshot!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }.start();

        } else if (id == R.id.toggle_outlines) {
            WordCloud.getInstance().setShowOutline(!WordCloud.getInstance().getShowOutline());
            cloudLayout.invalidate();
        } else if (id == R.id.view_exclude_list){
            Toast.makeText(this, "View Exclusion List", Toast.LENGTH_SHORT).show();
        }else if(id == R.id.action_about) {
            final TextView textView = new TextView(this);
            textView.setText(R.string.agreement);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("About");
            builder.setIcon(R.drawable.ic_launcher);
            builder.setView(textView);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    dialog.cancel();
                }
            });

            builder.show();
        }

        return false;
    }

}