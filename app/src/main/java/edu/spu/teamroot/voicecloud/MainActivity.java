package edu.spu.teamroot.voicecloud;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;

import java.util.Random;

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
        scrollView.addView(rl);

        makeButton("Hello", 60, 20, 300);

        // Create random buttons
        //randomButtons();
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
            Button button = new Button(this);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12 + (Math.round(Math.random() * 100) % 50));
            button.setText("Button" + Integer.toString(i));

            // Create new color filter and set button background color
            PorterDuffColorFilter filter = new PorterDuffColorFilter(getRandomAccentColor(), PorterDuff.Mode.SRC_ATOP);
            button.getBackground().setColorFilter(filter);


            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (lastToast != null) lastToast.cancel();
                    lastToast = Toast.makeText(MainActivity.this, "Clicked " + ((Button)v).getText().toString(), Toast.LENGTH_SHORT);
                    lastToast.show();
                    return false;
                }
            });

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = toPx(20 + (Math.round(Math.random() * 100)) * 10);
            params.topMargin = toPx(20 + i * 55);

            rl.addView(button, params);
        }

    }

    public void makeButton(String name, int size, int x, int y) {

        Button button = new Button(this);
        button.setTextSize(size);
        button.setText(name);

        // Create new color filter and set button background color
        PorterDuffColorFilter filter = new PorterDuffColorFilter(getRandomAccentColor(), PorterDuff.Mode.SRC_ATOP);
        button.getBackground().setColorFilter(filter);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (lastToast != null) lastToast.cancel();
                lastToast = Toast.makeText(MainActivity.this, "Clicked " + ((Button)v).getText().toString(), Toast.LENGTH_SHORT);
                lastToast.show();
                return false;
            }
        });

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = toPx(x);
        params.topMargin = toPx(y);

        // Add animation

        rl.addView(button, params);

    }

    public int getRandomAccentColor() {

        String color = "";

        // Randomly select a fact
        Random randomGenerator = new Random();  // Construct a new Random number generator
        int randomNumber = randomGenerator.nextInt(accentColors.length);

        color = accentColors[randomNumber];
        int colorAsInt = Color.parseColor(color);

        return colorAsInt;

    }
}