package edu.spu.teamroot.voicecloud;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Handler;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by aarongrider on 4/16/15.
 */

public class SpeechRecognitionService extends Service {

    protected static AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new VoiceHandler(this));

    public boolean mIsListening = false;
    private static boolean mIsStreamSolo;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
    private static final String TAG = null;

    private Timer speechTimeout = null;

    public class SilenceTimer extends TimerTask {

        @Override
        public void run() {
            //onError(SpeechRecognizer.ERROR_SPEECH_TIMEOUT);
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
    }

    public static class VoiceHandler extends Handler {
        private WeakReference<SpeechRecognitionService> mtarget;

        VoiceHandler(SpeechRecognitionService target)
        {
            mtarget = new WeakReference<SpeechRecognitionService>(target);
        }

        @Override
        public void handleMessage(Message msg)
        {

            final SpeechRecognitionService target = mtarget.get();

            switch (msg.what)
            {
                case MSG_RECOGNIZER_START_LISTENING:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    {
                        // Turn off beep sound
                        if (!mIsStreamSolo)
                        {
                            mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
                            mIsStreamSolo = true;
                        }
                    }
                    if (!target.mIsListening)
                    {
                        Log.d(TAG, "Start listening");
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:

                    if (mIsStreamSolo)
                    {
                        mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
                        mIsStreamSolo = false;
                    }

                    Log.d(TAG, "Canceled Recognizer");
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.destroy();
        }
    }

    public void startVoiceRecognitionCycle() {
        Message startMessage = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
        try {
            mServerMessenger.send(startMessage);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected class SpeechRecognitionListener implements RecognitionListener
    {

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");

            speechTimeout = new Timer();
            speechTimeout.schedule(new SilenceTimer(), 3000);
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");

            speechTimeout.cancel();
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndOfSpeech");

            // Restart new dictation cycle
            startVoiceRecognitionCycle();
        }

        @Override
        public void onError(int error) {
            String message;
            boolean restart = true;
            switch (error)
            {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "Audio recording error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "Client side error";
                    restart = false;
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "Insufficient permissions";
                    restart = false;
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "Network error";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "Network timeout";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "No match";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RecognitionService busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "error from server";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "No speech input";
                    break;
                default:
                    message = "Not recognised";
                    break;
            }
            Log.d(TAG,"onError code:" + error + " message: " + message);

            // Cancel any current recognition processes and start over
            Message stopMessage = Message.obtain(null, MSG_RECOGNIZER_CANCEL);

            try {
                mServerMessenger.send(stopMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (restart) {
                startVoiceRecognitionCycle();
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG,"onEvent");
        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG,"onPartialResults");
        }

        @Override
        public void onResults(Bundle results)
        {

            StringBuilder scores = new StringBuilder();
            for (int i = 0; i < results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES).length; i++) {
                scores.append(results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)[i] + " ");
            }
            Log.d(TAG,"onResults: " + results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) + " scores: " + scores.toString());

            // Return to the container activity dictation results
            if (results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) != null) {
                //mCallback.onResults(this, results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
            }
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {

        }

    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.d("speech", "onBind");

        return mServerMessenger.getBinder();
    }
}
