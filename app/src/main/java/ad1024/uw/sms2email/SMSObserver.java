package ad1024.uw.sms2email;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static android.content.Context.MODE_PRIVATE;

public class SMSObserver extends ContentObserver {
    private Handler mHandler;
    private Context context;

    public SMSObserver(Handler handler, Context context) {
        super(handler);
        this.mHandler = handler;
        this.context = context;
    }

    private class EmailSendTask extends AsyncTask<Void, Void, Void> {

        private Context context;

        public EmailSendTask(Context parent) {
            this.context = parent;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor cursor = null;
            Log.i("SMSService", "Handling new message");
            try {
                String where = "date > "
                        + (System.currentTimeMillis() - 5 * 60 * 1000);
                cursor = this.context.getContentResolver().query(
                        Uri.parse("content://sms/inbox"),
                        new String[]{"_id", "address", "body", "date"},
                        where, null, "date desc");
                final String title = "New Possible Verification Message!";
                String content = "";
                if (cursor != null) {
                    String body = "";
                    String sender = "";
                    String date = "";
                    while (cursor.moveToNext()) {
                        body = cursor.getString(cursor.getColumnIndex("body"));
                        sender = cursor.getString(cursor.getColumnIndex("address"));
                        date = cursor.getString(cursor.getColumnIndex("date"));
                        if (body.contains("验证码") || Pattern.matches("[A-Za-z0-9]+", body)) {
                            body = "Message from " + sender + "@" +
                                    new Date(Long.parseLong(date)).toString() + ": <br>" + body;
                            content += body + "<br><br>";
                        }
                    }
                    if(content.equals("")) {
                        return null;
                    }
                    final SharedPreferences preferences = this.context.getSharedPreferences("email_storage", MODE_PRIVATE);
                    Properties props = new Properties();
                    props.put("mail.transport.protocol", "smtp");
                    props.put("mail.smtp.host", preferences.getString(Consts.Preference.SERVER_ADDRESS,
                            "smtp.163.com"));
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.port", Integer.parseInt(preferences.getString(Consts.Preference.SERVER_PORT, "")));
                    Log.i("SMSService", "Properities: " + props.toString());
                    Session session = Session.getDefaultInstance(props);
                    session.setDebug(true);
                    MimeMessage newEmail = MailUtils.createNewEmail(session,
                            preferences.getString(Consts.Preference.EMAIL, ""),
                            title, content,
                            preferences.getString(Consts.Preference.EMAIL, ""));
                    try {
                        MailUtils.sendEmail(preferences.getString(Consts.Preference.EMAIL, ""),
                                preferences.getString(Consts.Preference.PASSWORD, ""),
                                session, newEmail);
                        ToastUtils.makeText("Email sent!", Toast.LENGTH_SHORT);
                    } catch (Exception e) {
                        ToastUtils.makeText("Error occured, please check you config",
                                Toast.LENGTH_LONG);
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
            return null;
        }
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.i("SMSObserver", "Change Detected");
        new EmailSendTask(this.context).execute();
        super.onChange(selfChange);
    }
}
