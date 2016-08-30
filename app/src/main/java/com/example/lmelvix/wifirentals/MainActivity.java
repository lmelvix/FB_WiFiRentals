package com.example.lmelvix.wifirentals;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Button;
import java.io.ByteArrayOutputStream;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.Properties;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import android.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton addUserButton;
    public DBAdapter myDB;


    /**
     * Main Activity to display list of registered users and onClick Listener to add new user
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addUserButton = (ImageButton) findViewById(R.id.addUser);
        addUserButton.setOnClickListener(this);
        openDB();
        populateListViewFromDB();
        registerListClickCallback();
    }

    /**
     * Close database when application is closed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    /**
     * onClick action to inflate new user
     * @param view : Click on Main Activity View
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addUser :
                addUserButtonClick();
                break;
        }
    }

    /**
     * Open database to maintain list of active users
     */
    private void openDB() {
        myDB = new DBAdapter(this);
        myDB.open();
    }

    /**
     * Close database upon application termination.
     */
    private void closeDB() {
        myDB.close();
    }

    /**
     * Inflate dialog box to get New user input and add it to user database
     */
    private void addUserButtonClick() {
        LayoutInflater layoutInflater
                = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        //Inflate dialog box
        final View popupView = layoutInflater.inflate(R.layout.popup, null);

        //Populate dialog box
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);
        Button btnConfirm = (Button)popupView.findViewById(R.id.confirm);
        btnDismiss.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
            }});

        // Set listener to add user
        btnConfirm.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                final EditText userNameText = (EditText)popupView.findViewById(R.id.username);
                final EditText userMacText = (EditText)popupView.findViewById(R.id.usermobile);
                String userName = userNameText.getText().toString();
                String userMAC = userMacText.getText().toString();
                onClick_AddRecord(userName, userMAC);
                connectToRouter();
                popupWindow.dismiss();
            }});
        popupWindow.showAsDropDown(addUserButton, -320, 5);
    }

    /**
     * Add user details to database
     * @param userName : New user name
     * @param userMobile : New user mobile number
     */
    public void onClick_AddRecord(String userName, String userMobile) {

        String message = "Adding User \n"+"Name: " + userName + "\n"+"Mobile: "+userMobile;
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

        // Add it to the DB and re-draw the ListView
        myDB.insertRow(userName, userMobile);
        populateListViewFromDB();
    }

    /**
     * Update Main activity view with list of latest users
     */
    private void populateListViewFromDB() {

        Cursor cursor = myDB.getAllRows();
        startManagingCursor(cursor);

        // Setup mapping from cursor to view fields:
        String[] fromFieldNames = new String[]
                {DBAdapter.KEY_NAME, DBAdapter.KEY_MOBILENUM};
        int[] toViewIDs = new int[]
                {R.id.item_name, R.id.item_favcolour};

        // Create adapter to may columns of the DB onto elemesnt in the UI.
        SimpleCursorAdapter myCursorAdapter =
                new SimpleCursorAdapter(
                        this,		            // Context
                        R.layout.item_layout,	// Row layout template
                        cursor,					// cursor (set of DB records to map)
                        fromFieldNames,			// DB Column names
                        toViewIDs				// View IDs to put information in
                );

        // Set the adapter for the list view
        ListView myList = (ListView) findViewById(R.id.listView);
        myList.setAdapter(myCursorAdapter);
    }

    /**
     * Register new user to Router's MAC filter list
     */
    private void registerListClickCallback() {
        ListView myList = (ListView) findViewById(R.id.listView);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long idInDB) {

                displayToastForId(idInDB);
                updateItemForId(idInDB);
                connectToRouter();

            }
        });
    }

    /**
     * Remove user from registered list
     * @param idInDB : user to be deleted
     */
    private void updateItemForId(long idInDB) {
        Cursor cursor = myDB.getRow(idInDB);
        if (cursor.moveToFirst()) {
            myDB.deleteRow(idInDB);
        }
        cursor.close();
        populateListViewFromDB();
    }

    private void displayToastForId(long idInDB) {
        Cursor cursor = myDB.getRow(idInDB);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(DBAdapter.COL_NAME);
            String mobile_num = cursor.getString(DBAdapter.COL_MOBILE);

            String message = "Deleting User \n"
                    + "User Name: " + name + "\n"
                    + "Phone : " + mobile_num;
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }
        cursor.close();
    }

    /**
     * Establish SSH connection to router
     */
    private void connectToRouter() {

        // Spin out a background task to create SSH link to router.
        new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer... integers) {
                try {
                    String username = "root";
                    String password = "password";
                    String hostname = "192.168.11.1";
                    int port = 22;
                    executeRemoteCommand(username, password, hostname, port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1);


    }

    /**
     * Update MAC filter in router with the new list of authorized users.
     * @param username : Router's user name
     * @param password : Router's password
     * @param hostname : Router's IP Address
     * @param port : Router's remote port
     * @return
     * @throws Exception
     */
    public String executeRemoteCommand(String username,String password,String hostname,int port)
            throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        // SSH Channel
        ChannelExec channelssh = (ChannelExec)session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        //Generate MAC Filter Script
        Cursor cursor = myDB.getAllRows();
        startManagingCursor(cursor);

        // MAC address of Service phone and Computer hardcoded to pull out in emergency
        String name = "nvram set wl0_maclist=\"F4:5C:89:C2:D1:A5 24:DF:6A:25:CE:A1 ";
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            /**
             * Registered Phone-to-MAC map
             *
             * For demo purpose, this is hard coded.
             * In ideal scenario, this map would be fetched from cloud database that is maintained
             * by carrier services.
             *
             * Mobile Number --> Modem MAC --> IMEI Number --> WiFi MAC
             */

            switch (cursor.getString(DBAdapter.COL_MOBILE)) {
                case "8582659143":
                    name += "24:DF:6A:25:CE:A1" + " ";
                    break;
                case "8582659141":
                    name += "E8:50:8B:55:17:FD" + " ";
                    break;
                default:
                    Toast.makeText(MainActivity.this,"Phone not recognized",Toast.LENGTH_LONG).show();
            }
            cursor.moveToNext();
        }
        name += "\" && nvram commit && reboot";

        // Execute command
        channelssh.setCommand(name);
        channelssh.connect();
        channelssh.disconnect();
        return baos.toString();
    }
}
