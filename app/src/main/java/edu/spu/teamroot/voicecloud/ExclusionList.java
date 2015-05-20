package edu.spu.teamroot.voicecloud;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class ExclusionList {
    /*
     * Static members
     */

    private static ExclusionList instance;

    /*
     * Static methods
     */

    public static ExclusionList createInstance() {
        if (instance == null) {
            instance = new ExclusionList();
        }

        return instance;
    }

    public static ExclusionList getInstance() {
        return instance;
    }
    public static void deleteInstance() {
        instance = null;
    }

    /*
     * Member variables
     */

    // String -- word name, Integer -- hit count
    public Map<String, Integer> excludeList;

    /*
     * Constructors
     */

    private ExclusionList() {
        excludeList = new TreeMap<>();
    }

    /*
     * Methods
     */

    // Returns an iterator to the exclusion list.
    public Iterator getIterator() {
        return excludeList.entrySet().iterator();
    }

    // Adds a word to the exclusion list.
    public void addWord(String word) {
        excludeList.put(word, 1);
    }

    // Removes a word from the exclusion list.
    public void removeWord(String word) {
        excludeList.remove(word);
    }

    // Checks if a word is in the exclusion list.
    // Also increments the hit count if a word is found.
    public boolean isWordExcluded(String word) {
        return false;
    }
}
