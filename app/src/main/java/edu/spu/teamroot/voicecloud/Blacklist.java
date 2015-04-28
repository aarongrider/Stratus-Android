package edu.spu.teamroot.voicecloud;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Blacklist {
    /*
     * Static members
     */

    private static Blacklist instance;

    /*
     * Static methods
     */

    public static Blacklist createInstance() {
        if (instance == null) {
            instance = new Blacklist();
        }

        return instance;
    }

    public static Blacklist getInstance() {
        return instance;
    }
    public static void deleteInstance() {
        instance = null;
    }

    /*
     * Member variables
     */

    // String -- word name, Integer -- hit count
    private Map<String, Integer> excludeList;

    /*
     * Constructors
     */

    private Blacklist() {
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
