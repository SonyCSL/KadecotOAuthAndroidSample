package com.sonycsl.kadecotoauthandroidsample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampResultMessage;
import com.sonycsl.wamp.transport.WampWebSocketTransport;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private String mToken;
    private WampWebSocketTransport mWampTransport;

    private String getOrigin() {
        return getPackageName();
    }

    private void doAuth() {
        Uri uri = Uri.parse("http://localhost:31413/login.html?redirect_uri=kadecot://" + getOrigin() + "&scope=com.sonycsl.kadecot");
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(i);
    }

    // OAuthの結果はonResumeで受け取ります。
    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                String uriStr = uri.toString();
                mToken = uriStr.substring(uriStr.indexOf("#") + 1);

                connectWAMP();
            }
        }
    }

    private void connectWAMP() {
        mWampTransport = new WampWebSocketTransport();
        mWampTransport.setOnWampMessageListener(new WampWebSocketTransport.OnWampMessageListener() {

            @Override
            public void onMessage(WampMessage msg) {
                switch( msg.getMessageType() ){
                    case WampMessageType.WELCOME :
                        // 機器一覧を問い合わせるメッセージを送ります。
                        mWampTransport.send(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),new JSONObject()
                                ,"com.sonycsl.kadecot.provider.procedure.getDeviceList")) ;
                        break ;
                    case WampMessageType.RESULT:
                        WampResultMessage wrm = msg.asResultMessage() ;

                        JSONObject arg = wrm.getArgumentsKw() ;

                        // argの内部構造は、どんな呼び出しに対する返答かによって異なります。
                        // この例では、getDeviceListの返答が来たと仮定して、機器一覧を得るようにしています。
                        // (それしか送っていないので）
                        // 様々なリクエストを行い、様々な返答が返ってくるときは、wrm.getRequestId()によって
                        // IDを得て、どの呼び出しに対する返答かというのを調べるようにしてください。

                        try {
                            JSONArray devs = null;
                            devs = arg.getJSONArray("deviceList");
                            for( int di=0 ; di<devs.length() ; ++di ){
                                JSONObject dev = devs.getJSONObject(di) ;
                                System.out.println( dev.getInt("deviceId")+":"+dev.getString("protocol")+"/"+dev.getString("deviceType")) ;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break ;
                }
            }

            @Override
            public void onError(Exception e) {
                //stopSelf();
            }

            @Override
            public void onClose() {
                //stopSelf();
            }
        });

        mWampTransport.open("localhost", 41314, "kadecot://" + getOrigin(), mToken);
        mWampTransport.send(WampMessageFactory.createHello("realm", new JSONObject()));
    }

    //manifestで、activityをsingleTaskにしているため、onNewIntent内でsetIntent()しておいた方がよいようです。
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //buttonを取得
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doAuth();
            }
        });
    }

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
