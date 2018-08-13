package ad1024.uw.sms2email;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private Button btn_save_email;
    private Button btn_toggle_service;
    private EditText ed_email;
    private EditText serverAddrEdit;
    private EditText serverPortEdit;
    private EditText passwordEdit;
    private boolean serviceRunning = false;
    private SharedPreferences emailInformationStorage;
    private static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Toast tool
        ToastUtils.initialize(this);

        // Use strict mode
        // StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        // StrictMode.setThreadPolicy(policy);

        btn_save_email = findViewById(R.id.btn_save_email);
        btn_toggle_service = findViewById(R.id.btn_toggle_service);
        serverAddrEdit = findViewById(R.id.server_addr_edit);
        serverPortEdit = findViewById(R.id.server_port_edit);
        passwordEdit = findViewById(R.id.password_edit);
        ed_email = findViewById(R.id.email_edit);
        emailInformationStorage = getSharedPreferences("email_storage", MODE_PRIVATE);

        int currentUid = android.os.Process.myUid();
        serviceRunning = isServiceRunning(currentUid, "SMSTransferService");
        if(serviceRunning) {
            btn_toggle_service.setText("STOP SERVICE!");
        } else {
            btn_toggle_service.setText("LINK START!");
        }

        btn_save_email.setOnClickListener(this);
        btn_toggle_service.setOnClickListener(this);
        loadConfiguration();

    }

    private void loadConfiguration() {
        String storedEmail = emailInformationStorage.getString("Email", "");
        String storedServerAddr = emailInformationStorage.getString("ServerAddr", "");
        String storedServerPort = emailInformationStorage.getString("ServerPort", "");
        String storedPassword = emailInformationStorage.getString("Password", "");
        ed_email.setText(storedEmail);
        serverAddrEdit.setText(storedServerAddr);
        serverPortEdit.setText(storedServerPort);
        passwordEdit.setText(storedPassword);
    }

    /*
    * Whether a service named `name` is running
    * @return: true: is running false: otherwise
    * */
    private boolean isServiceRunning(int uid, String name) {
        List<ActivityManager.RunningServiceInfo> serviceList = ((ActivityManager)getSystemService
                (Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE);
        for(ActivityManager.RunningServiceInfo info: serviceList) {
            if(info.uid == uid) {
                return true;
            }
        }
        return false;
    }

    private boolean checkEmailConfiguration() {
        return (emailInformationStorage.getString(Consts.Preference.SERVER_ADDRESS, "").isEmpty() ||
                emailInformationStorage.getString(Consts.Preference.SERVER_PORT, "").isEmpty() ||
                emailInformationStorage.getString(Consts.Preference.PASSWORD, "").isEmpty());
    }

    /*
    * Listener for button clicks
    * */
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_save_email) {
            String email = ed_email.getText().toString();
            if (!email.isEmpty()) {
                if (Pattern.matches(REGEX_EMAIL, email)) {
                    // Write current email into shared preferences
                    SharedPreferences.Editor prefEditor = emailInformationStorage.edit();
                    prefEditor.putString(Consts.Preference.EMAIL, email);
                    prefEditor.putString(Consts.Preference.SERVER_ADDRESS,
                            serverAddrEdit.getText().toString());
                    prefEditor.putString(Consts.Preference.SERVER_PORT,
                            serverPortEdit.getText().toString());
                    prefEditor.putString(Consts.Preference.PASSWORD,
                            passwordEdit.getText().toString());
                    prefEditor.apply();
                    ToastUtils.makeText("Email: " + email + " Saved!",
                            Toast.LENGTH_LONG);
                } else {
                    ToastUtils.makeText("Are u sure this is an Email address? O_O",
                            Toast.LENGTH_LONG);
                }
            } else {
                ToastUtils.makeText("Email Should not be EMPTY! Q3Q", Toast.LENGTH_SHORT);
            }
        } else if(view.getId() == R.id.btn_toggle_service) {
            if(serviceRunning) {
                btn_toggle_service.setText("LINK START!");
                stopService(new Intent(MainActivity.this, SMSTransferService.class));
                ToastUtils.makeText("Service STOPPED", Toast.LENGTH_SHORT);
                serviceRunning = false;
            } else {
                if(checkEmailConfiguration()) {
                    ToastUtils.makeText("Please fill in all settings", Toast.LENGTH_SHORT);
                }
                btn_toggle_service.setText("STOP SERVICE!");
                startService(new Intent(MainActivity.this, SMSTransferService.class));
                ToastUtils.makeText("Service STARTED", Toast.LENGTH_SHORT);
                serviceRunning = true;
            }
        }
    }
}
