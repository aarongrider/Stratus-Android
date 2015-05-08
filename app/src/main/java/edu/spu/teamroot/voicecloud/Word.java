package edu.spu.teamroot.voicecloud;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public class Word extends WordGroup {
    private String name;
    private int count;
    private long timestamp;

    private AnimatorSet animatorSet;

    private int[] accentColors = {
            R.color.accentBlue,
            R.color.accentGreen,
            R.color.accentYellow,
            R.color.accentRed};

    public Button button;
    public RelativeLayout.LayoutParams layoutParams;

    public Word(String name, int count) {
        super();

        this.name = name;
        this.count = count;
        this.timestamp = System.currentTimeMillis();

        this.animatorSet = null;

        if (needsCreate()) {
            createButton();
        } else {
            button = null;
            layoutParams = null;
        }
    }

    // Checks if the actual word button is created.
    // If the button is not created, the word is considered detached.
    public boolean isButtonCreated() {
        return button != null;
    }

    public boolean isWordAttached() {
        return parent != null;
    }

    // Checks if the button needs to be created (and added to the cloud).
    private boolean needsCreate() {
        boolean eligible = (count >= 2);
        return !isButtonCreated() && eligible;
    }

    private void createButton() {
        button = new Button(WordCloud.context);
        button.setVisibility(View.INVISIBLE);

        button.setText(name);
        button.setTextColor(WordCloud.context.getResources().getColor(android.R.color.white));

        button.getBackground().setColorFilter(WordCloud.context.getResources().getColor(R.color.accentBlue), PorterDuff.Mode.SRC_ATOP);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordActions(WordCloud.getInstance().getWord(button.getText().toString()));
            }
        });

        // Add the button to the word cloud layout
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = 0;
        layoutParams.leftMargin = 0;
        WordCloud.layout.addView(button, layoutParams);

        // Set initial size (do not animate)
        refreshSize(false);
    }

    // Removes the word from the tree and deletes the associated button.
    public void delete() {
        if (parent != null) {
            parent.removeChild(this);
        }

        if (isButtonCreated()) {
            if (animatorSet != null) {
                animatorSet.cancel();
            }

            WordCloud.layout.removeView(button);
        }

        button = null;
        layoutParams = null;
    }

    public void show() {
        show(button.getVisibility() == View.INVISIBLE);
    }

    public void show(boolean animate) {
        if (!isButtonCreated()) return;

        button.setVisibility(View.VISIBLE);

        if (animate) {
            ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            anim.setInterpolator(new SpringInterpolator());

            anim.setDuration(1000);

            button.clearAnimation();
            button.setAnimation(anim);
            button.animate();
        }
    }

    public void hide() {
        hide(button.getVisibility() == View.VISIBLE);
    }

    public void hide(boolean animate) {
        if (!isButtonCreated()) return;

        button.setVisibility(View.INVISIBLE);

        if (animate) {
            AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setInterpolator(new LinearInterpolator());

            anim.setDuration(500);

            button.clearAnimation();
            button.setAnimation(anim);
            button.animate();
        }
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean incrementCount(int value) {

        // Increment count
        count += value;

        // Update timestamp
        this.timestamp = System.currentTimeMillis();

        if (count <= 0) {
            return false;
        }

        if (isButtonCreated()) {
            // Button size has changed; animate
            refreshSize(true);
        } else if (needsCreate()) {
            createButton();
        }

        return true;
    }

    private void refreshSize(boolean animate) {
        if (!isButtonCreated()) return;

        // Cache old size
        Rect oldBounds = new Rect(bounds);

        Log.d(name, "Left: " + UnitConverter.getInstance().toDp(button.getLeft()) + " Top: " + UnitConverter.getInstance().toDp(button.getTop()));

        // Calculate new size based on count
        button.setTextSize(getTextSize(count));
        Log.d(name, "Count: " + count + " Size: " + getTextSize(count));

        // Calculate new color based on count (or part of speech)
        button.getBackground().setColorFilter(WordCloud.context.getResources().getColor(accentColors[(count / 5) % accentColors.length]), PorterDuff.Mode.SRC_ATOP);

        // Update bounds with new button size
        button.measure(WordCloud.layout.getWidth(), WordCloud.layout.getHeight());

        bounds.set(bounds.left, bounds.top,
                bounds.left + UnitConverter.getInstance().toDp(button.getMeasuredWidth()),
                bounds.top + UnitConverter.getInstance().toDp(button.getMeasuredHeight()));

        /*
        if (button.getWidth() == 0 && button.getHeight() == 0) {
            // The button has likely not been drawn in the View yet...
            // We need to fallback and get a calculated size based on the layout
            button.measure(WordCloud.layout.getWidth(), WordCloud.layout.getHeight());

            bounds.set(bounds.left, bounds.top,
                    bounds.left + UnitConverter.getInstance().toDp(button.getMeasuredWidth()),
                    bounds.top + UnitConverter.getInstance().toDp(button.getMeasuredHeight()));
        } else {
            bounds.set(bounds.left, bounds.top,
                    bounds.left + UnitConverter.getInstance().toDp(button.getWidth()),
                    bounds.top + UnitConverter.getInstance().toDp(button.getHeight()));
        }
        //*/

        Log.d(name, "Bounds: " + bounds.toString());

        center.x = bounds.centerX();
        center.y = bounds.centerY();

        Log.d(name, "Center: " + center.toString());

        if (animate) {
            float prevX = (float) oldBounds.width() / bounds.width();
            float prevY = (float) oldBounds.height() / bounds.height();

            ScaleAnimation anim = new ScaleAnimation(prevX, 1.0f, prevY, 1.0f);
            anim.setInterpolator(new SpringInterpolator());

            anim.setDuration(500);

            button.clearAnimation();
            button.setAnimation(anim);
            button.animate();
        }
    }

    private int getTextSize(int count) {
        return 20 + count;
    }

    @Override
    public void moveBy(int dx, int dy) {
        // Only animate if the button is visible, otherwise, just move it
        moveBy(dx, dy, button.getVisibility() == View.VISIBLE);
    }

    private void moveBy(int dx, int dy, boolean animate) {
        center.offset(dx, dy);
        bounds.offset(dx, dy);

        // Cancel existing animation (we are moving the word again)
        if (animatorSet != null) {
            if (animatorSet.isStarted()) {
                animatorSet.cancel();
            }
        }

        if (animate) {
            // Create animators (using updated destination value)
            ValueAnimator xAnim = new MarginAnimationListener(
                    WordCloud.layout, layoutParams, MarginAnimationListener.LEFT_MARGIN,
                    UnitConverter.getInstance().toPx(bounds.left)).getValueAnimator();
            ValueAnimator yAnim = new MarginAnimationListener(
                    WordCloud.layout, layoutParams, MarginAnimationListener.TOP_MARGIN,
                    UnitConverter.getInstance().toPx(bounds.top)).getValueAnimator();

            // Combine animators into one set
            animatorSet = new AnimatorSet();

            animatorSet.playTogether(xAnim, yAnim);
            animatorSet.setInterpolator(new SpringInterpolator());
            animatorSet.setDuration(1000);

            animatorSet.start();
        } else {
            layoutParams.leftMargin = UnitConverter.getInstance().toPx(bounds.left);
            layoutParams.topMargin = UnitConverter.getInstance().toPx(bounds.top);

            WordCloud.layout.requestLayout();
        }

        Log.d(name, "moveBy: (" + dx + "," + dy + ") Bounds: " + bounds.toString() + " Center: " + center.toString());
    }

    @Override
    public void moveTo(int x, int y) {
        // Only animate if the button is visible, otherwise, just move it
        moveTo(x, y, button.getVisibility() == View.VISIBLE);
    }

    private void moveTo(int x, int y, boolean animate) {
        int dx = x - center.x;
        int dy = y - center.y;

        moveBy(dx, dy, animate);
    }

    @Override
    public String toString() {
        return name;
    }

    private void wordActions(final Word word) {

        // WordActions custom dialog
        final Dialog dialog = new Dialog(WordCloud.context);
        dialog.setContentView(R.layout.word_action_dialog);
        String dialogTitle = "\"" + word.getName() + "\"";
        dialog.setTitle(dialogTitle);

        ListView listView = (ListView) dialog.findViewById(R.id.listView);

        ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> curItemMap;

        curItemMap = new HashMap<>();
        curItemMap.put("id", "count");
        curItemMap.put("icon", String.valueOf(R.mipmap.count_icon));
        curItemMap.put("iconText", Integer.toString(word.getCount()));
        curItemMap.put("label", word.getCount() == 1 ? "Occurrence" : "Occurrences");
        dataList.add(curItemMap);

        curItemMap = new HashMap<>();

        // Find count per minute
        double countPerMinute = word.getCount() / ((System.currentTimeMillis() - WordCloud.getInstance().getTimestamp()) / 60000.0);

        curItemMap.put("icon", String.valueOf(R.mipmap.count_icon));
        curItemMap.put("iconText", countPerMinute>=10?String.format("%d", (int)countPerMinute):String.format("%.1f", countPerMinute));
        curItemMap.put("label", countPerMinute==1?"Occurrence Per Minute":"Occurrences Per Minute");
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
        curItemMap.put("icon", String.valueOf(R.mipmap.count_icon));
        curItemMap.put("iconText", "");
        curItemMap.put("label", "Define in Dictionary");
        dataList.add(curItemMap);

        curItemMap = new HashMap<>();
        curItemMap.put("icon", String.valueOf(R.mipmap.quizlet_icon));
        curItemMap.put("iconText", "");
        curItemMap.put("label", "Create Flash Card");
        dataList.add(curItemMap);

        curItemMap = new HashMap<>();
        curItemMap.put("icon", String.valueOf(R.mipmap.remove_icon));
        curItemMap.put("iconText", "");
        curItemMap.put("label", "Remove from Word Cloud");
        dataList.add(curItemMap);

        SimpleAdapter simpleAdapter = new SimpleAdapter(WordCloud.context, dataList, R.layout.word_action_row, new String[]{"icon", "iconText", "label"}, new int[]{R.id.icon, R.id.iconText, R.id.label});

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(WordCloud.context, Integer.toString(position), Toast.LENGTH_SHORT);

                // Count
                if (position == 0) { }

                // Count per minute
                if (position == 1) { }

                // Search with Google
                if (position == 2) {
                    String url = "https://google.com/#q=" + word.getName();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    WordCloud.context.startActivity(i);
                }

                // Lookup on Wikipedia
                if (position == 3) {
                    String url = "http://wikipedia.org/wiki/" + word.getName();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    WordCloud.context.startActivity(i);
                }

                if (position == 4) {
                    String url = "http://dictionary.reference.com/browse/" + word.getName();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    WordCloud.context.startActivity(i);
                }

                // Create Flash Card
                if (position == 5) {
                    dialog.dismiss();
                }

                // Remove from Word Cloud
                if (position == 6) {

                    // Add word to exclusion list

                    // Remove word
                    WordCloud.getInstance().removeWord(word);
                    dialog.dismiss();

                }
            }
        };

        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(listener);

        dialog.show();
    }

}