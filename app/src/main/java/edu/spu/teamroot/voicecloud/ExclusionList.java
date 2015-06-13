package edu.spu.teamroot.voicecloud;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ExclusionList {
    /*
     * Static members
     */

    private static ExclusionList instance;
    public static Context context;

    public static final String VC_BLACKLIST_FILENAME = "exclusionlist";

    /*
     * Static methods
     */

    // Creates a static instance of the class.
    public static ExclusionList createInstance(Context context) {
        Log.d("ExclusionList", "createInstance(" + context + ")");

        if (instance == null) {
            instance = new ExclusionList(context);
        } else {
            Log.d("ExclusionList", "createInstance -- Existing instance found; reloading...");
            instance.reloadInstance(context);
        }

        return instance;
    }

    // Returns a static instance of the class.
    public static ExclusionList getInstance() {
        return instance;
    }

    // Deletes a static instance of the class.
    public static void deleteInstance() {
        Log.d("ExclusionList", "deleteInstance()");
        instance = null;
    }

    /*
     * Member variables
     */

    // String -- word name, Integer -- hit count
    protected Map<String, Integer> excludeList;

    /*
     * Constructors
     */

    private ExclusionList(Context context) {
        ExclusionList.context = context;

        excludeList = new TreeMap<>();
    }

    /*
     * Instance state helpers
     */

    // Reloads a static instance of the class.
    public void reloadInstance(Context newContext) {
        context = newContext;
    }

    /*
     * Methods
     */

    // Loads the exclusion list from file.
    public void load() {
        try {
            FileInputStream fin = context.openFileInput(VC_BLACKLIST_FILENAME);
            load(fin);
            fin.close();
        } catch (FileNotFoundException e) {
            Log.d("ExclusionList", "No saved list found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Saves the exclusion list to file.
    public void save() {
        try {
            FileOutputStream fout = context.openFileOutput(VC_BLACKLIST_FILENAME, Context.MODE_PRIVATE);
            save(fout);
            fout.close();
        } catch (Exception e) {
            Log.d("ExclusionList", "Error saving exclusion list!");
            e.printStackTrace();
        }
    }

    // Loads exclusion list from stream; in must be opened and closed by calling method.
    public void load(FileInputStream in) {
        Log.d("ExclusionList", "load(" + in + ")");

        excludeList.clear();

        Scanner scanner = new Scanner(in);

        while (scanner.hasNextLine()) {
            String word = scanner.nextLine().trim();
            if (word.length() > 0) addWord(word);
        }

        Log.d("ExclusionList", "Count: " + excludeList.size());
    }

    // Saves exclusion list to stream; out must be opened and closed by calling method.
    public void save(FileOutputStream out) {
        Log.d("ExclusionList", "save(" + out + ")");

        try {
            for (String word : excludeList.keySet()) {
                out.write((word + "\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("ExclusionList", "Count: " + excludeList.size());
    }

    // Returns an iterator to the exclusion list.
    public Iterator getIterator() {
        return excludeList.entrySet().iterator();
    }

    // Adds a word to the exclusion list. Returns false if word was already in the list.
    public boolean addWord(String word) {
        if (excludeList.containsKey(word)) {
            return false;
        }

        excludeList.put(word, 1);
        return true;
    }

    // Removes a word from the exclusion list.
    public void removeWord(String word) {
        excludeList.remove(word);
    }

    // Checks if a word is in the exclusion list.
    // Also increments the hit count if a word is found.
    public boolean isWordExcluded(String word) {
        boolean wordFound = excludeList.containsKey(word);

        if (wordFound) {
            int newCount = excludeList.get(word) + 1;
            excludeList.put(word, newCount);
        }

        return wordFound;
    }
}
