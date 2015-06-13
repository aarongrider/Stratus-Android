package edu.spu.teamroot.voicecloud;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Handler;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SpeechRecognitionService extends Service {

    protected static AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new VoiceHandler(this));

    protected Preprocessor mPreprocessor;

    public boolean mIsListening = false;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_PAUSE_LISTENING = 2;
    static final int MSG_RECOGNIZER_STOP_LISTENING = 3;
    static final int MSG_SERVICE_KILL = 4;

    static final int PARTIAL_RESULTS = 0;
    static final int FINAL_RESULTS = 1;

    private static final int NOTIFY_ID = 1;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    private static final String TAG = "SRS";

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        mPreprocessor = Preprocessor.createInstance();

        // Create notification
        mBuilder = new NotificationCompat.Builder(this)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.vc_icon_solid)
                        .setColor(getResources().getColor(R.color.button))
                        .setContentTitle("Stratus is running")
                        .setContentText("Touch to open app.");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
            );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static class VoiceHandler extends Handler {
        private WeakReference<SpeechRecognitionService> mTarget;

        VoiceHandler(SpeechRecognitionService target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            final SpeechRecognitionService target = mTarget.get();

            switch (msg.what) {
                case MSG_RECOGNIZER_START_LISTENING:
                    Log.d(TAG, "Start Recognizer");
                    target.showNotification();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        // Turn off beep sound
                        mAudioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
                        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    }

                    Log.d(TAG, "Start listening");
                    target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                    target.mIsListening = true;

                    break;
                case MSG_RECOGNIZER_STOP_LISTENING:
                    target.hideNotification();
                case MSG_RECOGNIZER_PAUSE_LISTENING:
                    Log.d(TAG, "Stopped Recognizer");

                    // Clear previous results
                    mTarget.get().mPreprocessor.clearPrevious();

                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;

                    break;
                case MSG_SERVICE_KILL:
                    Log.d(TAG, "Killing service...");
                    target.stopSelf();
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("SRS", "onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "SRS destroy");
        hideNotification();

        // Turn sounds back on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }

        super.onDestroy();

        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }

        if (mPreprocessor != null) {
            mPreprocessor = null;
            Preprocessor.deleteInstance();
        }
    }

    // Sends a start message to the service.
    public void startVoiceRecognitionCycle() {
        try {
            mServerMessenger.send(Message.obtain(null, MSG_RECOGNIZER_START_LISTENING));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected class SpeechRecognitionListener implements RecognitionListener
    {

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech");
        }

        @Override
        public void onError(int error) {
            String message;
            boolean restart = true;

            switch (error) {
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
            try {
                mServerMessenger.send(Message.obtain(null, MSG_RECOGNIZER_PAUSE_LISTENING));
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            // Clear the previous results
            mPreprocessor.clearPrevious();

            if (restart) {
                startVoiceRecognitionCycle();
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG,"onEvent");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG,"onPartialResults: " + partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));

            // Return to the container activity dictation results
            if (partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) != null) {
                ArrayList resultArray = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String resultString = resultArray.get(0).toString();
                mPreprocessor.processString(resultString, PARTIAL_RESULTS);
            } else {
                Log.d(TAG, "onPartialResults failed to convert");
            }
        }

        @Override
        public void onResults(Bundle results) {
            Log.d(TAG,"onResults: " + results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));

            // Return to the container activity dictation results
            if (results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) != null) {
                ArrayList resultArray = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String resultString = resultArray.get(0).toString();
                mPreprocessor.processString(resultString, FINAL_RESULTS);
            } else {
                Log.d(TAG, "onResults failed to convert");
            }

            // Restart new dictation cycle
            startVoiceRecognitionCycle();
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("speech", "onBind");

        return mServerMessenger.getBinder();
    }

    // Shows the cloud notification in the notification bar.
    public void showNotification() {
        // mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    // Removes the cloud notification from the notification bar.
    public void hideNotification() {
        mNotificationManager.cancel(NOTIFY_ID);
    }
}
