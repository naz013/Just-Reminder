package com.cray.software.justreminder.dialogs;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;

public class ExchangeLogIn extends AppCompatActivity {

    ColorSetter cs = new ColorSetter(ExchangeLogIn.this);
    SharedPrefs sPrefs = new SharedPrefs(ExchangeLogIn.this);
    Toolbar toolbar;
    TextView title;
    EditText loginEdit, domainEdit, passwordEdit;
    CheckBox check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.exchange_login_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle("Exchange");

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_add:
                                addUser();
                                break;
                        }
                        return true;
                    }
                });

        toolbar.inflateMenu(R.menu.save_menu);

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        title = (TextView) findViewById(R.id.title);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)){
            title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.exchange_white, 0, 0, 0);
        } else {
            title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.exchange_grey, 0, 0, 0);
        }

        loginEdit = (EditText) findViewById(R.id.loginEdit);
        domainEdit = (EditText) findViewById(R.id.domainEdit);
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);

        check = (CheckBox) findViewById(R.id.check);
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordEdit.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    passwordEdit.setInputType(129);
                }
            }
        });
    }

    private void addUser() {
        String mail = loginEdit.getText().toString().trim().toLowerCase();
        String password = passwordEdit.getText().toString().trim();
        String domain = domainEdit.getText().toString().trim().toLowerCase();
        /*ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        WebCredentials credentials = new WebCredentials(mail, password);
        service.setCredentials(credentials);
        if (!domain.matches("") || !domain.startsWith("https://")) {
            try {
                service.setUrl(new URI("https://" + domain + "/EWS/Exchange.asmx"));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            try {
                service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        try {
            service.autodiscoverUrl(mail);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (service.getUrl() == null){
            Toast.makeText(ExchangeLogIn.this, "Connection error", Toast.LENGTH_LONG).show();
            return;
        }

        //Log.d(Constants.LOG_TAG, service.getUrl().getUserInfo());
        SyncHelper helper = new SyncHelper(ExchangeLogIn.this);
        TasksData data = new TasksData(ExchangeLogIn.this);
        data.open();
        data.addAccount(helper.encrypt(mail), helper.encrypt(password), domain);
        new GetExchangeTasksAsync(ExchangeLogIn.this, null).execute();*/
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
