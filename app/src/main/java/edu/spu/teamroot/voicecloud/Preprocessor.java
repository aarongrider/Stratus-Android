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

    static final int PARTIAL_RESULTS = 0;
    static final int FINAL_RESULTS = 1;


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

    private String[] prevBuff = {};

    /*
     * Constructors
     */

    private Preprocessor() {

        // Set up member variables
        mWordCloud = WordCloud.getInstance();
        mWordWeight = 1;

        Log.d("Preprocessor", "Preprocessor created");
    }

    /*
     * Methods
     */

    public void processString(String resultString, int type) {

        // Break string into separate words
        String[] currBuff = resultString.split(" ");

        // TODO Check against blacklist
        // TODO Part of speech identification

        // TODO Refactor how we handle partial results to make more efficient
        if (type == PARTIAL_RESULTS) {

            // Iterate through partial results and send to cloud
            for (String word : currBuff) {

                word = word.toLowerCase().trim();

                if (!word.isEmpty()) {
                    mWordCloud.processWord(word, mWordWeight);
                }
            }


            for (String word : prevBuff) {

                word = word.toLowerCase().trim();

                if (!word.isEmpty()) {
                    mWordCloud.processWord(word, -1 * mWordWeight);
                }
            }

            prevBuff = currBuff;

        } else if (type == FINAL_RESULTS) {

            // Iterate through partial results and send to cloud
            for (String word : currBuff) {
                if (!word.isEmpty()) mWordCloud.processWord(word, mWordWeight);
            }
            for (String word : prevBuff) {

                word = word.toLowerCase().trim();

                if (!word.isEmpty()) {
                    mWordCloud.processWord(word, -1 * mWordWeight);
                }
            }

            prevBuff = new String[]{};

        }

    }
}