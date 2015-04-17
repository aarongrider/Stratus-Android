package edu.spu.teamroot.voicecloud;

import java.util.Iterator;
import java.util.Map;

public class Blacklist {
    // String -- word name, Integer -- hit count
    private Map<String, Integer> excludeList;

    public Blacklist() {

    }

    // Returns an iterator to the exclusion list.
    public Iterator getIterator() {
        return excludeList.entrySet().iterator();
    }

    // Adds a word to the exclusion list.
    public void addWord(String word) {

    }

    // Removes a word from the exclusion list.
    public void removeWord(String word) {

    }

    // Checks if a word is in the exclusion list.
    // Also increments the hit count if a word is found.
    public boolean isWordBlacklisted(String word) {
        return false;
    }
}
