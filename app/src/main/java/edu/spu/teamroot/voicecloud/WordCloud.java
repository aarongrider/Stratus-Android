package edu.spu.teamroot.voicecloud;

import android.widget.RelativeLayout;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class WordCloud {
    /*
     * Static members
     */

    private static WordCloud instance;
    private static Context context;
    private static RelativeLayout layout; // The layout containing all word views

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
    }

    /*
     * Methods
     */

    public void processWord(String word) {
        processWord(word, 1);
    }

    public void processWord(String word, int count) {

    }

    // Adds a word to the word cloud. Finds a free group, and increases group size if needed.
    private void addWord(Word word) {

    }

    // Repositions a word inside a group.
    private void updateWord(Word word) {

    }

    // Removes a word, deleting it from the word cloud.
    public void removeWord(Word word) {

    }

    // Clears the word cloud.
    public void clear() {

    }

    public boolean isWordInCloud(String word) {
        return false;
    }
}
