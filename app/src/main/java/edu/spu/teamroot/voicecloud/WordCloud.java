package edu.spu.teamroot.voicecloud;

import android.widget.RelativeLayout;

import java.util.Deque;
import java.util.Map;

public class WordCloud {
    // The layout containing all word views
    private RelativeLayout layout;

    private Map<String, Word> wordList;
    private Deque<WordGroup> freeGroups;

    private WordGroup wordTreeRoot;

    public WordCloud(RelativeLayout rl) {
        layout = rl;
    }

    public void processWord(String word) {
        processWord(word, 1);
    }

    public void processWord(String word, int count) {

    }

    private void addWord(Word word) {

    }

    private void updateWord(Word word) {

    }

    public void removeWord(Word word) {

    }

    public void clear() {

    }

    public boolean isWordInCloud(String word) {
        return false;
    }
}
