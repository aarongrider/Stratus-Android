package edu.spu.teamroot.voicecloud;

import android.content.Context;
import android.util.Log;

public class UnitConverter {
    /*
     * Static members
     */

    private static UnitConverter instance;
    private Context context;
    private float density;

    /*
     * Static methods
     */

    // Creates a static instance of the class.
    public static UnitConverter createInstance(Context context) {
        Log.d("UnitConverter", "createInstance(" + context + ")");

        if (instance != null) {
            Log.d("UnitConverter", "createInstance -- Existing instance destroyed");
            deleteInstance();
        }

        instance = new UnitConverter(context);
        return instance;
    }

    // Returns a static instance of the class.
    public static UnitConverter getInstance() {
        return instance;
    }

    // Deletes a static instance of the class.
    public static void deleteInstance() {
        Log.d("UnitConverter", "deleteInstance()");

        instance = null;
    }

    /*
     * Constructors
     */

    private UnitConverter(Context context) {
        this.context = context;
        this.density = context.getResources().getDisplayMetrics().density;
    }

    /*
     * Methods
     */

    // Returns the current context used.
    public Context getContext() {
        return context;
    }

    // Converts a dp value to px.
    public int toPx(float dp) {
        //int value = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
        //Log.d("UnitConverter", "Converting " + dp + "dp to " + value + "px");
        return Math.round(dp * density);
    }

    // Converts a px value to dp.
    public int toDp(float px) {
        //int value = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics()));
        //Log.d("UnitConverter", "Converting " + px + "px to " + value + "dp");
        return Math.round(px / density);
    }

    // Converts a dp value to px.
    public float toPxFloat(float dp) {
        //float value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        //Log.d("UnitConverter", "Converting " + dp + "dp to " + value + "px");
        return dp * density;
    }

    // Converts a px value to dp.
    public float toDpFloat(float px) {
        //float value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics());
        //Log.d("UnitConverter", "Converting " + px + "px to " + value + "dp");
        return px / density;
    }
}
