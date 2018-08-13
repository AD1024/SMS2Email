package ad1024.uw.sms2email;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    private static Toast instance;

    public static Toast getInstance(Context context) {
        if (instance != null) return instance;
        instance = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        return instance;
    }

    public static void initialize(Context context) {
        instance = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public static void makeText(String msg, int length) {
        if(instance != null) {
            instance.setText(msg);
            instance.setDuration(length);
            instance.show();
        }
    }
}
