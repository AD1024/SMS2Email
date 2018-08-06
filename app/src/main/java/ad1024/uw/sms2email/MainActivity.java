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
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
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
                    prefEditor.putString("Email", email);
                    prefEditor.putString("ServerAddr", serverAddrEdit.getText().toString());
                    prefEditor.putString("ServerPort", serverPortEdit.getText().toString());
                    prefEditor.putString("Password", passwordEdit.getText().toString());
                    prefEditor.apply();
                    Toast.makeText(this, "Email: " + email + " Saved!", Toast.LENGTH_LONG).show();
                    ed_email.clearComposingText();
                } else {
                    Toast.makeText(this, "Are u sure this is an Email address? O_O",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Email Should not be EMPTY! Q3Q", Toast.LENGTH_SHORT).show();
            }
        } else if(view.getId() == R.id.btn_toggle_service) {
            if(serviceRunning) {
                btn_toggle_service.setText("LINK START!");
                stopService(new Intent(MainActivity.this, SMSTransferService.class));
                Toast.makeText(this, "Service STOPPED", Toast.LENGTH_SHORT).show();
                serviceRunning = false;
            } else {
                btn_toggle_service.setText("STOP SERVICE!");
                startService(new Intent(MainActivity.this, SMSTransferService.class));
                Toast.makeText(this, "Service STARTED", Toast.LENGTH_SHORT).show();
                serviceRunning = true;
            }
        }
    }
}
