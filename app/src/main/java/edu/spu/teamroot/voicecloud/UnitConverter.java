package edu.spu.teamroot.voicecloud;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

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
        if (instance == null) {
            instance = new UnitConverter(context);
        }

        return instance;
    }

    public static UnitConverter getInstance() {
        return instance;
    }
    public static void deleteInstance() {
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
        int value = Math.round(dp * density);
        //Log.d("UnitConverter", "Converting " + dp + "dp to " + value + "px");
        return value;
    }

    public int toDp(float px) {
        //int value = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics()));
        int value = Math.round(px / density);
        //Log.d("UnitConverter", "Converting " + px + "px to " + value + "dp");
        return value;
    }

    public float toPxFloat(float dp) {
        //float value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        float value = dp * density;
        //Log.d("UnitConverter", "Converting " + dp + "dp to " + value + "px");
        return value;
    }

    public float toDpFloat(float px) {
        //float value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics());
        float value = px / density;
        //Log.d("UnitConverter", "Converting " + px + "px to " + value + "dp");
        return value;
    }
}
