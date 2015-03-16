package edu.spu.teamroot.voicecloud;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
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
        scrollView.addView(rl, toPx(1000), toPx(1000));

        // Make buttons
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
            Random randomGenerator = new Random();  // Construct a new Random number generator
            int size = randomGenerator.nextInt(150);

            int x = toPx(20 + (Math.round(Math.random() * 100)) * 10);
            int y = toPx(20 + i * 55);

            String buttonName = String.format("Button %s", i);
            makeButton(buttonName, size, x, y);
        }

    }

    public void makeButton(String name, int size, int x, int y) {
        final Button button = new Button(this);
        button.setTextSize(size);
        button.setText(name);

        // Create new color filter and set button background color
        PorterDuffColorFilter filter = new PorterDuffColorFilter(getRandomAccentColor(), PorterDuff.Mode.SRC_ATOP);
        button.getBackground().setColorFilter(filter);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if (lastToast != null) lastToast.cancel();
                //lastToast = Toast.makeText(MainActivity.this, "Clicked " + ((Button)v).getText().toString(), Toast.LENGTH_SHORT);
                //lastToast.show();

                openAlert(v, button);

                return false;
            }
        });

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = toPx(x);
        params.topMargin = toPx(y);

        // Create animation
        ScaleAnimation scaleAnim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setInterpolator(new SpringInterpolator());

        scaleAnim.setDuration(1000);
        scaleAnim.setStartOffset(500);

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
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.word_action_dialog);
        dialog.setTitle("Title");

        ListView listView = (ListView) dialog.findViewById(R.id.listView);

        String[] values = new String[] { "Count", "Google", "Send to Quizlet"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                R.layout.word_action_row, R.id.label, values);

        listView.setAdapter(arrayAdapter);

        // set the custom dialog components - text, image and button
        //TextView text = (TextView) dialog.findViewById(R.id.text);
        //text.setText("Android custom dialog example!");
        //ImageView image = (ImageView) dialog.findViewById(R.id.image);
        //image.setImageResource(R.drawable.ic_launcher);

//        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
//        dialogButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });

        dialog.show();
    }

}