package edu.spu.teamroot.voicecloud;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;
import android.widget.RelativeLayout;

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

    private Map<String, Word> wordList;

    private Deque<WordGroup> freeGroups;
    private int groupSize;

    private WordGroup wordTreeRoot;

    /*
     * Constructors
     */

    private WordCloud(Context context, RelativeLayout layout) {
        WordCloud.context = context;
        WordCloud.layout = layout;

        wordList = new TreeMap<>();
        freeGroups = new LinkedList<>();
        groupSize = 0;
        wordTreeRoot = new WordGroup();

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

        wordTreeRoot.setBounds(new Rect(0, 0, width, height));
        Log.d("wordTreeRoot", "Width: " + width + " Height: " + height);
    }

    /*
     * Methods
     */

    public void processWord(String word) {
        processWord(word, 1);
    }

    public void processWord(String name, int count) {
        // TODO: Check blacklist for word

        Word word = wordList.get(name);

        if (word == null) {
            // This will also create a new button and set the initial size of the word
            word = new Word(name, count);

            addWord(word);
        } else {
            // Increment only the count
            word.incrementCount(count);
        }

        updateWord(word);
        word.show(); // Make sure the word is shown (can be hidden on create)
    }

    // Adds a word to the word cloud. Finds a free group, and increases group size if needed.
    private void addWord(Word word) {
        // Add to master word list
        wordList.put(word.getName(), word);

        // Calculate new group size ( 1 + floor( sqrt( n-1 ) ) )
        // e.g. n=4, size = 2 groups of 2; n=8, size = 3 groups of 3
        int newSize = 1 + (int)Math.floor(Math.sqrt(wordList.size() - 1));

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
            newGroup.addChild(word);
            wordTreeRoot.addChild(newGroup);

            // Add new group to front of free group queue (most likely has extra space)
            if (newGroup.children.size() < groupSize) {
                freeGroups.addFirst(newGroup);
            }
        } else {
            // Get a free group and add the word to it
            WordGroup group = freeGroups.removeFirst();
            group.addChild(word);

            // Stick group back if still has room
            if (group.children.size() < groupSize) {
                freeGroups.addFirst(group);
            }
        }
    }

    // Repositions a word inside a group.
    private void updateWord(Word word) {
        // Reposition word in parent group
        word.parent.repositionChild(word);

        // Now, reposition parent groups within the root
        wordTreeRoot.repositionChild(word.parent);
    }

    // Removes a word, deleting it from the word cloud.
    public void removeWord(Word word) {
        removeWord(word, false);
    }

    private void removeWord(Word word, boolean deleteFromList) {
        WordGroup group = word.parent;

        if (deleteFromList) {
            wordList.remove(word.getName()); // Remove from word list
        }

        word.delete(); // This will remove from tree and delete button

        // Add parent to list of free groups
        if (group.children.size() < groupSize && !freeGroups.contains(group)) {
            freeGroups.addFirst(group);
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
    }

    public boolean isWordInCloud(String name) {
        return wordList.containsKey(name);
    }
}
