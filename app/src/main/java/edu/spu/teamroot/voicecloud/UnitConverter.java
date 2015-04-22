package edu.spu.teamroot.voicecloud;

import android.content.Context;
import android.util.TypedValue;

public class UnitConverter {
    /*
     * Static members
     */

    private static UnitConverter instance;
    private Context context;

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

    /*
     * Constructors
     */

    private UnitConverter(Context context) {
        this.context = context;
    }

    /*
     * Methods
     */

    public Context getContext() {
        return context;
    }

    public int toPx(float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    public int toDp(float px) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics()));
    }

    public float toPxFloat(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public float toDpFloat(float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics());
    }
}
