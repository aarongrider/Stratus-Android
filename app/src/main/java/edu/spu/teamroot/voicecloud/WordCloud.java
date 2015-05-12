package edu.spu.teamroot.voicecloud;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class WordCloud {
    /*
     * Static members
     */

    private static WordCloud instance;
    public static Context context;
    public static RelativeLayout layout; // The layout containing all word views

    /*
     * Static methods
     */

    public static WordCloud createInstance(Context context, RelativeLayout layout) {
        if (instance == null) {
            instance = new WordCloud(context, layout);
        }

        return instance;
    }

    public static WordCloud getInstance() {
        return instance;
    }
    public static void deleteInstance() {
        instance = null;
    }

    /*
     * Member variables
     */

    public WordWeighter weighter;

    private Map<String, Word> wordList;

    private Deque<WordGroup> freeGroups;
    private int groupSize;

    private WordGroup wordTreeRoot;
    private int treeSize; // Number of (attached) words in the tree

    private long timestamp;

    private boolean showOutline = false;

    /*
     * Constructors
     */

    private WordCloud(Context context, RelativeLayout layout) {
        WordCloud.context = context;
        WordCloud.layout = layout;

        weighter = new SimpleWeighter(); // TODO Adjust weighter based on settings

        wordList = new TreeMap<>();
        freeGroups = new LinkedList<>();
        groupSize = 0;
        wordTreeRoot = new WordGroup();
        timestamp = System.currentTimeMillis();

        int width, height;

        if (layout.getWidth() == 0 && layout.getHeight() == 0) {
            // The layout does not have a size... it likely has not been drawn yet.
            // We can use the layout params to get the initial size at least
            width = UnitConverter.getInstance().toDp(layout.getLayoutParams().width);
            height = UnitConverter.getInstance().toDp(layout.getLayoutParams().height);
        } else {
            width = UnitConverter.getInstance().toDp(layout.getWidth());
            height = UnitConverter.getInstance().toDp(layout.getHeight());
        }

        wordTreeRoot.setBounds(new Rect(width / 2, height / 2, width / 2, height / 2));
        Log.d("wordTreeRoot", "Width: " + width + " Height: " + height);
    }

    /*
     * Methods
     */

    public void addWord(String word) {
        addWord(word, 1);
    }

    public void addWord(String name, int count) {
        // Get the word from the list
        Word word = wordList.get(name);

        if (word == null) {
            // Create a new word and add to master word list
            word = new Word(name, count);
            wordList.put(word.getName(), word);
        } else {
            // Increment only the count
            word.incrementCount(count);
        }

        // Update the word
        boolean isAttached = word.isAttached();
        boolean shouldShow = weighter.shouldShow(word);

        if (isAttached) {
            if (shouldShow) {
                // Word already exists in the tree, position relative to self
                repositionWord(word, false);
            } else {
                // Word needs to be removed from cloud
                hideWord(word);
            }
        } else {
            if (shouldShow) {
                // Word needs to be added to cloud
                showWord(word);
            }
        }

        // Check if we need to refresh all words
        if (weighter.refreshAll(word)) {
            evaluateAllWords();
        }
    }

    private void showWord(Word word) {
        Log.d("WordCloud", word.getName() + ": showWord");

        attachWord(word);
        repositionWord(word, true);
        word.show();

        treeSize++;
    }

    private void hideWord(Word word) {
        Log.d("WordCloud", word.getName() + ": hideWord");

        word.hide();
        detachWord(word);

        treeSize--;
    }

    private void evaluateAllWords() {
        for (Map.Entry pair : wordList.entrySet()) {
            Word word = (Word)pair.getValue();

            boolean isAttached = word.isAttached();
            boolean shouldShow = weighter.shouldShow(word);

            if (isAttached && !shouldShow) {
                hideWord(word);
            } else if (!isAttached && shouldShow) {
                showWord(word);

                // TODO Re-evaluate size too? (Requires reposition for all words)
            }
        }
    }

    public Word getWord(String name) {
        return wordList.get(name);
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    protected WordGroup getFreeGroup() {
        // Calculate new group size ( 1 + floor( sqrt( n-1 ) ) )
        // e.g. n=4, size = 2 groups of 2; n=8, size = 3 groups of 3
        int newSize = 1 + (int)Math.floor(Math.sqrt(treeSize - 1));

        // If size needs to be increased, or there are no free groups (for some reason)
        if (newSize > groupSize || freeGroups.isEmpty()) {
            // Update to newSize, or increment count if we need a new group
            if (newSize > groupSize) {
                groupSize = newSize;
            } else {
                groupSize++;
            }

            // Update free groups list (group size has changed)
            for (WordGroup group : wordTreeRoot.children) {
                if (group.children.size() < groupSize) {
                    freeGroups.addFirst(group);
                }
            }

            // Create new word group and link to tree hierarchy
            WordGroup newGroup = new WordGroup();
            wordTreeRoot.addChild(newGroup);

            return newGroup;
        } else {
            // Get a free group and add the word to it
            return freeGroups.removeFirst();
        }
    }

    // Adds a word to the word cloud. Finds a free group, and increases group size if needed.
    protected void attachWord(Word word) {
        Log.d("WordCloud", word.getName() + ": attachWord");

        // Do not attach word if already parented!
        if (word.isAttached()) return;

        // Add the word to the view
        WordCloud.layout.addView(word.button, word.layoutParams);

        WordGroup group = getFreeGroup();
        group.addChild(word);

        // Stick group back if still has room
        if (group.children.size() < groupSize) {
            freeGroups.addFirst(group);
        }
    }

    protected void detachWord(Word word) {
        Log.d("WordCloud", word.getName() + ": detachWord");

        // Do not detach if already detached.
        if (!word.isAttached()) return;

        word.button.clearAnimation();

        // Remove the word from the view
        WordCloud.layout.removeView(word.button);

        // Remove the word from the word tree
        WordGroup group = word.parent;

        group.removeChild(word);
        group.refreshBounds();

        // Add parent to list of free groups
        if (group.children.size() < groupSize && !freeGroups.contains(group)) {
            freeGroups.addFirst(group);
        }
    }

    // Repositions a word inside a group.
    protected void repositionWord(Word word, boolean initialPlacement) {
        Log.d("WordCloud", word.getName() + ": repositionWord");

        if (!word.isAttached()) return;

        // Initially, reposition word in parent group (otherwise relative to current position)
        word.parent.repositionChild(word, initialPlacement);

        // Now, reposition parent group within the root.
        // If size <= 1, give it an initial position (once we get more children, we cannot move around as much)
        wordTreeRoot.repositionChild(word.parent, word.parent.children.size() <= 1);
    }

    // Removes a word, permanently deleting it from the word cloud.
    public void removeWord(Word word) {
        removeWord(word, true);
    }

    protected void removeWord(Word word, boolean deleteFromList) {
        Log.d("WordCloud", word.getName() + ": removeWord");

        if (deleteFromList) {
            wordList.remove(word.getName()); // Remove from word list
        }

        WordGroup group = word.parent; // Cache word parent group
        word.delete(); // This will remove from tree and delete button

        if (group != null) {
            // Add parent to list of free groups
            if (group.children.size() < groupSize && !freeGroups.contains(group)) {
                freeGroups.addFirst(group);
            }
        }
    }

    // Clears the word cloud.
    public void clear() {
        Iterator it = wordList.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            // Do not remove it from the list here... this will cause an exception!
            removeWord((Word)pair.getValue(), false);

            it.remove(); // Proper way to delete when using iterators
        }

        // Update timestamp
        timestamp = System.currentTimeMillis();
    }

    public boolean isWordInCloud(String name) {
        return wordList.containsKey(name);
    }

    public String saveWordCloud() {

        Log.d("wordCloud", "Saving word cloud");

        // Create Master JSON Object
        JSONObject toSend = new JSONObject();

        // Create layout JSON Object
        JSONObject layout = new JSONObject();
        int width = UnitConverter.getInstance().toDp(this.layout.getLayoutParams().width);
        int height = UnitConverter.getInstance().toDp(this.layout.getLayoutParams().height);
        String timestamp = new Timestamp(this.getTimestamp()).toString();

        try {
            layout.put("width", width);
            layout.put("height", height);
            layout.put("timestamp", timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create Words JSON Array
        JSONArray words = new JSONArray();

        // Iterate through WordList and add to Json object
        for (Map.Entry pair : wordList.entrySet()) {
            // Get current word
            Word currWord = (Word) pair.getValue();

            // Create JSON Object for word
            JSONObject word = new JSONObject();

            // Put current word cloud data into json object
            try {
                // Add all word properties
                word.put("name", currWord.getName());
                word.put("count", currWord.getCount());
                word.put("timestamp", new Timestamp(currWord.getTimestamp()).toString());

                // Convert rect to JSON
                JSONObject bounds = new JSONObject();
                bounds.put("bottom", currWord.getBounds().bottom);
                bounds.put("left", currWord.getBounds().left);
                bounds.put("right", currWord.getBounds().right);
                bounds.put("top", currWord.getBounds().top);
                word.put("bounds", bounds);

                // Add word json to words array
                words.put(word);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        try {

            // Add layout and words array to master json object
            toSend.put("layout", layout);
            toSend.put("words", words);

            // Transmit object to web server
            JSONTransmitter transmitter = new JSONTransmitter();
            transmitter.execute(toSend);

            JSONObject result = transmitter.get();
            String cloudID = result.getString("id");

            if (cloudID != "") return cloudID;
            else return "none";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "none";
    }

    public boolean loadWordCloud(String id) {

        // Make request
        Log.d("wordCloud", "Loading CLOUDID " + id);

        // Create Master JSON Object
        JSONObject toSend = new JSONObject();

        try {
            toSend.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            // Transmit object to web server
            JSONTransmitter transmitter = new JSONTransmitter();
            transmitter.execute(toSend);

            // Ping API and get JSON object
            JSONObject result = transmitter.get();
            result = new JSONObject(result.getString("id"));

            // Clear out current cloud
            this.clear();

            // Populate words
            JSONArray wordArray = result.getJSONObject("cloud").getJSONArray("words");

            for (int i = 0; i < wordArray.length(); i++) {
                JSONObject word = wordArray.getJSONObject(i);
                String name = word.getString("name");
                Integer count = Integer.parseInt(word.getString("count"));
                Long timestamp = Timestamp.valueOf(word.getString("timestamp")).getTime();

                addWord(name, count);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /*
     * Drawing
     */

    public boolean getShowOutline() {
        return showOutline;
    }

    public void setShowOutline(boolean showOutline) {
        this.showOutline = showOutline;
    }

    public void onDraw(Canvas canvas) {
        if (!showOutline) return;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4.0f);

        for (WordGroup group : wordTreeRoot.children) {
            canvas.drawRect(
                    UnitConverter.getInstance().toPx(group.bounds.left),
                    UnitConverter.getInstance().toPx(group.bounds.top),
                    UnitConverter.getInstance().toPx(group.bounds.right),
                    UnitConverter.getInstance().toPx(group.bounds.bottom),
                    paint
            );
        }

        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(8.0f);

        canvas.drawRect(
                UnitConverter.getInstance().toPx(wordTreeRoot.bounds.left),
                UnitConverter.getInstance().toPx(wordTreeRoot.bounds.top),
                UnitConverter.getInstance().toPx(wordTreeRoot.bounds.right),
                UnitConverter.getInstance().toPx(wordTreeRoot.bounds.bottom),
                paint
        );

        paint.setColor(Color.RED);
        paint.setStrokeWidth(16.0f);

        canvas.drawRect(
                layout.getLeft(),
                layout.getTop(),
                layout.getRight(),
                layout.getBottom(),
                paint
        );
    }
}
