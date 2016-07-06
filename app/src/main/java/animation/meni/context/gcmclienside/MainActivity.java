package animation.meni.context.gcmclienside;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import animation.meni.context.gcmclienside.model.JSONresponse;
import animation.meni.context.gcmclienside.util.GetServiceCall;
import animation.meni.context.gcmclienside.util.PrefUtils;

public class MainActivity extends AppCompatActivity {
    private GoogleCloudMessaging gcm;
    private String regid;
    private String PROJECT_NUMBER = "537875107274";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (PrefUtils.getNotificationId(MainActivity.this).length() == 0) {
            getRegId();
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void getRegId() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                    }
                    regid = gcm.register(PROJECT_NUMBER);
                    Log.e("GCM ID :", regid);
                    if (regid == null || regid == "") {
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle(getString(R.string.ERROR));
                        alert.setMessage(getString(R.string.INTERNALSERVERERROR));
                        alert.setPositiveButton(getString(R.string.TRYAGIN), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getRegId();
                                dialog.dismiss();
                            }
                        });
                        alert.setNegativeButton(getString(R.string.EXIT), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                MainActivity.this.finish();
                            }
                        });
                        alert.show();
                    } else {
                        Log.e("registration id:", regid);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setMessage("Registering Device...");
                                progressDialog.show();
                                new GetServiceCall("http://192.168.0.237/GCMServerSide/add_registration_id.php?registration_id=" + regid, GetServiceCall.TYPE_JSONOBJECT) {

                                    @Override
                                    public void response(String response) {

                                        progressDialog.dismiss();
                                        JSONresponse jsoNresponse = new GsonBuilder().create().fromJson(response, JSONresponse.class);
                                        if (jsoNresponse.getStatus() == 1) {
                                            PrefUtils.setNotificationId(regid, MainActivity.this);
                                            Toast.makeText(MainActivity.this, jsoNresponse.getMessage(), Toast.LENGTH_LONG).show();
                                        } else if (jsoNresponse.getStatus() == 2) {
                                            PrefUtils.setNotificationId(regid, MainActivity.this);
                                            Toast.makeText(MainActivity.this, jsoNresponse.getMessage(), Toast.LENGTH_LONG).show();
                                        } else if (jsoNresponse.getStatus() == 0) {
                                            Toast.makeText(MainActivity.this, jsoNresponse.getMessage(), Toast.LENGTH_LONG).show();
                                        }

                                    }

                                    @Override
                                    public void error(VolleyError error) {
                                        progressDialog.dismiss();
                                        error.printStackTrace();
                                    }

                                }.call();
                            }
                        });


                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Log.e("error", ex.toString());
                }
                return null;
            }
        }.execute();
    } // end of getRegId

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
