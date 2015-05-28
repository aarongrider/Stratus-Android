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

    public static UnitConverter createInstance(Context context) {
        Log.d("UnitConverter", "createInstance(" + context + ")");

        if (instance != null) {
            Log.d("UnitConverter", "createInstance -- Existing instance destroyed");
            deleteInstance();
        }

        instance = new UnitConverter(context);
        return instance;
    }

    public static UnitConverter getInstance() {
        return instance;
    }
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

    public Context getContext() {
        return context;
    }

    public int toPx(float dp) {
        //int value = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
        //Log.d("UnitConverter", "Converting " + dp + "dp to " + value + "px");
        return Math.round(dp * density);
    }

    public int toDp(float px) {
        //int value = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics()));
        //Log.d("UnitConverter", "Converting " + px + "px to " + value + "dp");
        return Math.round(px / density);
    }

    public float toPxFloat(float dp) {
        //float value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        //Log.d("UnitConverter", "Converting " + dp + "dp to " + value + "px");
        return dp * density;
    }

    public float toDpFloat(float px) {
        //float value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics());
        //Log.d("UnitConverter", "Converting " + px + "px to " + value + "dp");
        return px / density;
    }
}
