package edu.spu.teamroot.stratus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

    public static final int PADDING = 20;

    /*
     * Static methods
     */

    // Creates a static instance of the class.
    public static WordCloud createInstance(Context context, RelativeLayout layout) {
        Log.d("WordCloud", "createInstance(" + context + ", " + layout + ")");

        if (instance == null) {
            instance = new WordCloud(context, layout);
        } else {
            Log.d("WordCloud", "createInstance -- Existing instance found; reloading...");
            instance.reloadInstance(context, layout);
        }

        return instance;
    }

    // Returns a static instance of the class.
    public static WordCloud getInstance() {
        return instance;
    }

    // Deletes a static instance of the class.
    public static void deleteInstance() {
        Log.d("WordCloud", "deleteInstance()");

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

    private boolean showOutline;

    private Bundle savedBundle = null;

    private Paint paint;

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

        // Get and set outline state
        showOutline = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("outline", false);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);

        initRootBounds();
    }

    /*
     * Instance state helpers
     */

    // Reloads a static instance of the class based on a new context and layout.
    public void reloadInstance(Context newContext, RelativeLayout newLayout) {
        context = newContext;

        WordCloud.layout.removeAllViews();
        WordCloud.layout = newLayout;

        for (Word word : wordList.values()) {
            if (word.isAttached()) {
                WordCloud.layout.addView(word.button, word.layoutParams);
            }
        }
    }

    // Retrieves a saved bundle from the cloud instance.
    public Bundle popSavedBundle() {
        Bundle bundle = savedBundle;
        savedBundle = null;
        return bundle;
    }

    // Stores a bundle with the cloud instance.
    public void pushSavedBundle(Bundle bundle) {
        savedBundle = new Bundle(bundle);
    }

    /*
     * Methods
     */

    // Initializes the initial bounds of the root group.
    public void initRootBounds() {
        int width, height;

        // The layout does not have a size... it likely has not been drawn yet.
        // We can use the layout params to get the initial size at least
        width = UnitConverter.getInstance().toDp(layout.getLayoutParams().width);
        height = UnitConverter.getInstance().toDp(layout.getLayoutParams().height);

        wordTreeRoot.setBounds(new Rect(width / 2, height / 2, width / 2, height / 2));
        Log.d("wordTreeRoot", "Width: " + width + " Height: " + height);
    }

    // Adds a word to the cloud.
    public void addWord(String word) {
        addWord(word, 1);
    }

    // Adds a word with specified count to the cloud.
    public synchronized void addWord(String name, int count) {
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

    // Attaches and shows a word in the cloud.
    private void showWord(Word word) {
        Log.d("WordCloud", word.getName() + ": showWord");

        attachWord(word);
        repositionWord(word, true);
        word.show();
    }

    // Detaches and hides a word from the cloud.
    private void hideWord(Word word) {
        Log.d("WordCloud", word.getName() + ": hideWord");

        word.hide();
        detachWord(word);
    }

    // Re-evaluates all words based on updated size and weighting.
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

    // Returns a word from the word list.
    public Word getWord(String name) {
        return wordList.get(name);
    }

    // Returns the timestamp of the word cloud.
    public long getTimestamp() {
        return this.timestamp;
    }

    // Updates the list of free groups.
    private void refreshFreeGroups() {
        freeGroups.clear();

        for (WordGroup group : wordTreeRoot.children) {
            if (group.children.size() < groupSize) {
                freeGroups.addFirst(group);
            }
        }
    }

    // Returns a free group from the list. Updates max group size if count changes.
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
            refreshFreeGroups();

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

        treeSize++;
    }

    // Detaches a word from the word cloud, leaving it in the list.
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

        treeSize--;
    }

    // Repositions a word inside a group.
    protected void repositionWord(Word word, boolean initialPlacement) {
        Log.d("WordCloud", word.getName() + ": repositionWord");

        if (!word.isAttached()) return;

        // Initially, reposition word in parent group (otherwise relative to current position)
        word.parent.repositionChild(word, initialPlacement);

        // Now, reposition parent group within the root.
        // If size <= 1, give it an initial position
        wordTreeRoot.repositionChild(word.parent, word.parent.children.size() <= 1);

        // Check if words are too close to edge
        boolean leftOffscreen = wordTreeRoot.bounds.left < PADDING;
        boolean topOffscreen = wordTreeRoot.bounds.top < PADDING;

        if (leftOffscreen || topOffscreen) {
            int dx = leftOffscreen ? (-wordTreeRoot.bounds.left + PADDING) : 0;
            int dy = topOffscreen ? (-wordTreeRoot.bounds.top + PADDING) : 0;

            wordTreeRoot.moveBy(dx, dy);
        }
    }

    // Removes a word, permanently deleting it from the word cloud.
    public synchronized void removeWord(Word word) {
        removeWord(word, true);
    }

    // Removes a word from the cloud. The flag determines if it is kept in the list.
    // (This is done to avoid problems when deleting during an iteration)
    protected synchronized void removeWord(Word word, boolean deleteFromList) {
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

        // Reset bounds for cloud to original layout
        initRootBounds();
    }

    // Returns true if the word is in the cloud.
    public boolean isWordInCloud(String name) {
        return wordList.containsKey(name);
    }

    // Saves the word cloud to the web, returning the cloudid.
    public String saveWordCloud() {
        try {
            JSONObject toSend = toJSON();

            // Transmit object to web server
            JSONTransmitter transmitter = new JSONTransmitter();
            transmitter.execute(toSend);

            JSONObject result = transmitter.get();
            String cloudID = result.getString("cloudid");

            if (cloudID.length() > 0) return cloudID;
            else return "none";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "none";
    }

    // Loads the word cloud from the web, given the cloudid.
    public synchronized boolean loadWordCloud(String cloudid) {
        // Make request
        Log.d("wordCloud", "Loading CLOUDID " + cloudid);

        try {
            // Create Master JSON Object
            JSONObject toSend = new JSONObject();

            toSend.put("cloudid", cloudid);

            // Transmit object to web server
            JSONTransmitter transmitter = new JSONTransmitter();
            transmitter.execute(toSend);

            // Ping API and get JSON object
            JSONObject result = transmitter.get();
            result = new JSONObject(result.getString("cloudid"));

            return fromJSON(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Saves the word cloud to a JSON representation.
    public synchronized JSONObject toJSON() {
        Log.d("WordCloud", "Saving word cloud");

        // Create Master JSON Object
        JSONObject saveObj = new JSONObject();

        // Create layout JSON Object
        JSONObject cloud = new JSONObject();
        int width = UnitConverter.getInstance().toDp(layout.getLayoutParams().width);
        int height = UnitConverter.getInstance().toDp(layout.getLayoutParams().height);
        String timestamp = new Timestamp(this.getTimestamp()).toString();

        try {
            cloud.put("width", width);
            cloud.put("height", height);
            cloud.put("timestamp", timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create Groups JSON Array
        JSONArray groups = new JSONArray();

        // Iterate through WordList and add to Json object
        for (int i = 0; i < wordTreeRoot.children.size(); i++) {
            // Get current word
            WordGroup currGroup = wordTreeRoot.children.get(i);

            // Create JSON Object for word
            JSONObject group = new JSONObject();

            // Put current word cloud data into json object
            try {
                // Add all word properties
                Log.d("wordCloud", "Group ID: " + currGroup.getId());
                group.put("groupcloudid", currGroup.getId());

                group.put("centerx", currGroup.center.x);
                group.put("centery", currGroup.center.y);

                group.put("bottom", currGroup.getBounds().bottom);
                group.put("left", currGroup.getBounds().left);
                group.put("right", currGroup.getBounds().right);
                group.put("top", currGroup.getBounds().top);

                // Add word json to words array
                groups.put(group);

            } catch (JSONException e) {
                e.printStackTrace();
            }

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
                word.put("name", currWord.getName());
                word.put("count", currWord.getCount());
                word.put("timestamp", new Timestamp(currWord.getTimestamp()).toString());
                word.put("attached", currWord.isAttached() ? 1 : 0);

                // Put parent group if it exists
                if (currWord.parent != null) word.put("group", currWord.parent.getId());
                else word.put("group", -1);

                word.put("bottom", currWord.getBounds().bottom);
                word.put("left", currWord.getBounds().left);
                word.put("right", currWord.getBounds().right);
                word.put("top", currWord.getBounds().top);

                // Add word json to words array
                words.put(word);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        // Add layout and words array to master json object
        try {
            saveObj.put("cloud", cloud);
            cloud.put("words", words);
            cloud.put("groups", groups);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return saveObj;
    }

    // Loads the word cloud from a JSON representation.
    public synchronized boolean fromJSON(JSONObject fromRecv) {
        Log.d("WordCloud", "Loading word cloud");

        try {
            // Clear out current cloud
            this.clear();
            treeSize = 0;

            // Create map for id to group instance
            Map<Integer, WordGroup> groupMap = new TreeMap<>();

            // Populate groups
            JSONArray groupArray = fromRecv.getJSONObject("cloud").getJSONArray("groups");

            for (int i = 0; i < groupArray.length(); i++) {
                JSONObject group = groupArray.getJSONObject(i);

                // Get id
                Integer groupId = Integer.parseInt(group.getString("groupcloudid"));

                // Get center
                Point center = new Point(Integer.parseInt(group.getString("centerx")), Integer.parseInt(group.getString("centery")));

                // Get bounds
                Rect bounds = new Rect(Integer.parseInt(group.getString("left")), Integer.parseInt(group.getString("top")), Integer.parseInt(group.getString("right")), Integer.parseInt(group.getString("bottom")));

                WordGroup wordGroup = new WordGroup(center, bounds);
                groupMap.put(groupId, wordGroup);
                wordTreeRoot.addChild(wordGroup);
            }

            // Populate words
            JSONArray wordArray = fromRecv.getJSONObject("cloud").getJSONArray("words");

            for (int i = 0; i < wordArray.length(); i++) {
                JSONObject word = wordArray.getJSONObject(i);

                // Get properties
                String name = word.getString("name");
                Integer count = Integer.parseInt(word.getString("count"));
                Long timestamp = Timestamp.valueOf(word.getString("timestamp")).getTime();
                Integer groupId = Integer.parseInt(word.getString("group"));

                // Get bounds
                Integer top = Integer.parseInt(word.getString("top"));
                Integer left = Integer.parseInt(word.getString("left"));
                Integer bottom = Integer.parseInt(word.getString("bottom"));
                Integer right = Integer.parseInt(word.getString("right"));
                Rect bounds = new Rect(left, top, right, bottom);

                Word newWord = new Word(name, count);
                wordList.put(newWord.getName(), newWord);

                // Check if word is attached (groupId is valid)
                if (groupId >= 0) {
                    // Get parent group
                    WordGroup parentGroup = groupMap.get(groupId);
                    parentGroup.addChild(newWord);

                    // Add the word to the view
                    WordCloud.layout.addView(newWord.button, newWord.layoutParams);

                    newWord.moveTo(bounds.centerX(), bounds.centerY());
                    newWord.show();

                    treeSize++;
                }
            }

            // Update group size and refresh free groups
            groupSize = 1 + (int)Math.floor(Math.sqrt(treeSize - 1));
            refreshFreeGroups();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /*
     * Drawing
     */

    // Returns the showOutline flag.
    public boolean getShowOutline() {
        return showOutline;
    }

    // Sets the showOutline flag.
    public void setShowOutline(boolean showOutline) {
        this.showOutline = showOutline;
    }

    // Called to draw outlines on the canvas.
    public void onDraw(Canvas canvas) {
        if (!showOutline) return;

        paint.setColor(context.getResources().getColor(R.color.accentBlackTransparent));
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

        paint.setColor(context.getResources().getColor(R.color.button));
        paint.setStrokeWidth(8.0f);

        canvas.drawRect(
                UnitConverter.getInstance().toPx(wordTreeRoot.bounds.left),
                UnitConverter.getInstance().toPx(wordTreeRoot.bounds.top),
                UnitConverter.getInstance().toPx(wordTreeRoot.bounds.right),
                UnitConverter.getInstance().toPx(wordTreeRoot.bounds.bottom),
                paint
        );

        paint.setColor(context.getResources().getColor(R.color.red));
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
