package edu.spu.teamroot.voicecloud;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

/**
 * Created by aarongrider on 5/2/15.
 */

public class Preprocessor {
    public static class ResultTask {
        protected String result;
        protected int type;

        public ResultTask(String result, int type) {
            this.result = result;
            this.type = type;
        }
    }

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

    private class ProcWord {
        private int count = 1;

        public void increment() {
            count++;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }
    }

    private Map<String, ProcWord> prevMap = new TreeMap<>();

    protected int mWordWeight;

    private Handler mainHandler = new Handler();
    private Handler taskHandler;

    private Thread taskThread;

    private class ProcTask implements Runnable {

        private final String RESULT;
        private final int TYPE;

        public ProcTask(String result, int type) {
            RESULT = result;
            TYPE = type;
        }

        @Override
        public void run() {
            Map<String, ProcWord> curMap = new TreeMap<>();

            // Break string into separate words
            String[] currBuff = RESULT.toLowerCase().split(" ");

            // TODO Check against blacklist
            // TODO Part of speech identification

            // Make map of words
            for (String curPart : currBuff) {
                final String word = curPart.trim();

                if (!word.isEmpty()) {
                    ProcWord wordObj = curMap.get(word);

                    if (wordObj == null) {
                        curMap.put(word, new ProcWord());
                    } else {
                        wordObj.increment();
                    }
                }
            }

            synchronized (prevMap) {
                // Update word counts
                for (Map.Entry pair : curMap.entrySet()) {
                    final String word = (String) pair.getKey();
                    final int curCount = ((ProcWord) pair.getValue()).getCount();

                    ProcWord prevWord = prevMap.get(word);

                    final int prevCount;

                    if (prevWord != null) {
                        prevCount = prevWord.getCount();
                    } else {
                        prevCount = 0;
                    }

                    if (curCount != prevCount) {
                        // Increment the word
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                WordCloud.getInstance().addWord(word, (curCount - prevCount) * mWordWeight);
                            }
                        });
                    }
                }

                // Remove words that are no more
                for (Map.Entry pair : prevMap.entrySet()) {
                    final String word = (String)pair.getKey();
                    final int count = ((ProcWord)pair.getValue()).getCount();

                    if (!curMap.containsKey(word)) {
                        // Decrement the word
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                WordCloud.getInstance().addWord(word, -1 * count * mWordWeight);
                            }
                        });
                    }
                }

                if (TYPE == PARTIAL_RESULTS) {
                    prevMap = curMap;
                } else if (TYPE == FINAL_RESULTS) {
                    prevMap.clear();
                }
            }
        }
    }

    /*
     * Constructors
     */

    private Preprocessor() {
        // Set up member variables
        mWordWeight = 1;

        Log.d("Preprocessor", "Preprocessor created");

        taskThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                taskHandler = new Handler();
                Looper.loop();
            }
        };

        taskThread.start();
    }

    /*
     * Methods
     */

    public void processString(String resultString, int type) {
        if (taskHandler == null) {
            Log.d("Preprocessor", "Null taskHandler!");
            return;
        }

        // Add to queue
        taskHandler.post(new ProcTask(resultString, type));
    }

    public void clearPrevious() {
        synchronized (prevMap) {
            prevMap.clear();
        }
    }
}
