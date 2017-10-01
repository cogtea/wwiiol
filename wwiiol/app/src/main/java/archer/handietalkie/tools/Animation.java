package archer.handietalkie.tools;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;

/**
 * Created by Ramy on 10/1/17.
 */

public class Animation {
    public static void blinking(View view) {
        final android.view.animation.Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(android.view.animation.Animation.INFINITE);
        animation.setRepeatMode(android.view.animation.Animation.REVERSE);
        view.startAnimation(animation);
    }
}
