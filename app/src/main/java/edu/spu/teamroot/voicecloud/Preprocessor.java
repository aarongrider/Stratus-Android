package edu.spu.teamroot.voicecloud;

import android.util.Log;

/**
 * Created by aarongrider on 5/2/15.
 */

public class Preprocessor {

    /*
     * Static members
     */

    private static Preprocessor instance;

    /*
     * Static methods
     */

    public static Preprocessor createInstance() {
        if (instance == null) {
            instance = new Preprocessor();
        }

        return instance;
    }

    public static Preprocessor getInstance() {
        return instance;
    }
    public static void deleteInstance() {
        instance = null;
    }

    /*
     * Member variables
     */

    protected WordCloud mWordCloud;
    protected int mWordWeight;

    /*
     * Constructors
     */

    private Preprocessor() {

        // Set up member variables
        mWordCloud = WordCloud.getInstance();
        mWordWeight = 1;

        Log.d("Preprocessor", "Preprocessor created.");
    }

    /*
     * Methods
     */

    public void processString(String words) {

        // TODO Break string into words and process word
    }

    public void processWord(String word, int weight) {

        // TODO Partial aggregation (for partial results)
        // TODO Check against blacklist
        // TODO Part of speech identification

        // Add to word cloud
        mWordCloud.processWord(word, weight);

    }

}
