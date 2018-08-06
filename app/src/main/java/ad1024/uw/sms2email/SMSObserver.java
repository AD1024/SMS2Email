package ad1024.uw.sms2email;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

public class SMSObserver extends ContentObserver {
    private Handler mHandler;

    public SMSObserver(Handler handler) {
        super(handler);
        this.mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.i("SMSObserver", "Change Detected");
        this.mHandler.obtainMessage(0).sendToTarget();
    }
}
