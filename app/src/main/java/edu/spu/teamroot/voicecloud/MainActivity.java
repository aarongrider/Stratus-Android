package edu.spu.teamroot.voicecloud;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

public class MainActivity extends ActionBarActivity {
    static Toast lastToast;

    ImageButton mainButton;
    ImageButton resetButton;
    ImageButton menuButton;

    TwoDScrollView scrollView;
    RelativeLayout rl;

    public String[] accentColors = {
            "#20a760", // green
            "#3d83f7", // blue
            "#dc4339", // red
            "#ffe233"};// yellow

    private static float MIN_ZOOM = 1f;
    private static float MAX_ZOOM = 5f;

    private float scaleFactor = 1f;
    private ScaleGestureDetector scaleDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainButton = (ImageButton)findViewById(R.id.main_button);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Play", Toast.LENGTH_SHORT).show();
            }
        });

        resetButton = (ImageButton)findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Reset", Toast.LENGTH_SHORT).show();
            }
        });

        menuButton = (ImageButton)findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Menu", Toast.LENGTH_SHORT).show();
            }
        });

        // Get scroll view
        scrollView = (TwoDScrollView)findViewById(R.id.WordCloudScrollView);

        // Create a RelativeLayout element
        rl = new RelativeLayout(this);

        // Add the RelativeLayout element to the ScrollView
        int scrollViewWidth = 1000;
        int scrollViewHeight = 1000;
        scrollView.addView(rl, toPx(scrollViewWidth), toPx(scrollViewHeight));

        // Move to center of the ScrollView
        scrollView.scrollToWhenReady(scrollViewWidth / 2, scrollViewHeight / 2);

        // Make buttons
        // Name, Size, X, Y, Delay in Sec
        makeButton("Team", 70, 100, 360, 1);
        makeButton("Root", 50, 250, 465, 5);
        makeButton("Washingtonian", 40, 310, 400, 10);
        makeButton("Geotastic", 20, 310, 360, 15);
        makeButton("Awesome", 40, 220, 295, 20);
        makeButton("Weltz", 70, 400, 465, 25);
        makeButton("Ok", 100, 435, 255, 30);

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

    private int toPx(float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
    }

    private int toDp(float px) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, getResources().getDisplayMetrics()));
    }

    public void randomButtons() {
        // Add a bunch of buttons for testing
        for (int i = 0; i < 40; i++)
        {
            Random randomGenerator = new Random();  // Construct a new Random number generator
            int size = randomGenerator.nextInt(150);

            int x = toPx(20 + (Math.round(Math.random() * 100)) * 10);
            int y = toPx(20 + i * 55);

            String buttonName = String.format("Button %s", i);
            makeButton(buttonName, size, x, y, 500);
        }

    }

    public void makeButton(String name, int size, int x, int y, int delay) {
        final Button button = new Button(this);
        button.setTextSize(size);
        button.setText(name);

        // Create new color filter and set button background color
        PorterDuffColorFilter filter = new PorterDuffColorFilter(getRandomAccentColor(), PorterDuff.Mode.SRC_ATOP);
        button.getBackground().setColorFilter(filter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (lastToast != null) lastToast.cancel();
                //lastToast = Toast.makeText(MainActivity.this, "Clicked " + ((Button)v).getText().toString(), Toast.LENGTH_SHORT);
                //lastToast.show();

                openAlert(v, button);
            }
        });

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = toPx(x);
        params.topMargin = toPx(y);

        // Create animation
        ScaleAnimation scaleAnim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setInterpolator(new SpringInterpolator());

        scaleAnim.setDuration(1000);

        int offset = delay * 1000;
        scaleAnim.setStartOffset(offset);

        rl.addView(button, params);

        // Animate the button
        button.setAnimation(scaleAnim);
        button.animate();
    }

    public int getRandomAccentColor() {
        // Randomly select a fact
        Random randomGenerator = new Random();  // Construct a new Random number generator
        int randomNumber = randomGenerator.nextInt(accentColors.length);

        int colorAsInt = Color.parseColor(accentColors[randomNumber]);

        return colorAsInt;

    }

    private void openAlert(View view, Button button) {
        // custom dialog
        final Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog);
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