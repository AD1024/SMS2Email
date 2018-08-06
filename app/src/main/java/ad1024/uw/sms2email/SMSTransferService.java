package ad1024.uw.sms2email;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class SMSTransferService extends Service {
    private SMSObserver mObserver;
    private Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handleNewMessage();
                        }
                    }).run();
                    break;
            }
        }
    };

    public SMSTransferService() {
    }

    private void handleNewMessage() {
        Cursor cursor = null;
        Log.i("SMSService", "Handling new message");
        try {
            String where = "date > "
                    + (System.currentTimeMillis() - 2 * 60 * 1000);
            cursor = getContentResolver().query(
                    Uri.parse("content://sms/inbox"),
                    new String[]{"_id", "address", "body", "date"},
                    where, null, "date desc");
            final String title = "New Possible Verification Message!";
            String content = "";
            if (cursor != null) {
                String body = "";
                String sender = "";
                while (cursor.moveToNext()) {
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    sender = cursor.getString(cursor.getColumnIndex("address"));
                    if(body.contains("验证码") || Pattern.matches("[A-Za-z0-9]+", body)) {
                        body = "Message from " + sender + ": <br>" + body;
                        content += body + "<br><br>";
                    }
                }
                final SharedPreferences preferences = getSharedPreferences("email_storage", MODE_PRIVATE);
                Properties props = new Properties();
                props.put("mail.transport.protocol", "smtp");
                props.put("mail.smtp.host", preferences.getString("ServerAddr", "smtp.163.com"));
                props.put("mail.smtp.auth", "true");
                props.put("mail.transport.protocol", "smtp");
                props.put("mail.smtp.port", Integer.parseInt(preferences.getString("ServerPort", "")));
                Log.i("SMSService", "Properities: " + props.toString());
                Session session = Session.getDefaultInstance(props);
                session.setDebug(true);
                MimeMessage newEmail = MailUtils.createNewEmail(session,
                        preferences.getString("Email", ""),
                        title, content,
                        preferences.getString("Email", ""));
                try {
                    MailUtils.sendEmail(preferences.getString("Email", ""),
                            preferences.getString("Password", ""),
                            session, newEmail);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onCreate() {
        mObserver = new SMSObserver(msgHandler);
        Log.i("SMSService", "Started");
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"),
                true, mObserver);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SMSService", "Destroyed");
        getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
