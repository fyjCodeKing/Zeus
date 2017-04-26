package com.filepassapp.chenze.filepass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.filepassapp.chenze.filepass.Broadcast.NetState;
import com.filepassapp.chenze.filepass.FilePassUtil.FilePathResolver;


public class SendNoNetworkActivity extends ActionBarActivity {
    private Button fileButton = null;
    private Button sendButton = null;
    private Button connectButton = null;
    private TextView filePath = null;

    private Context mContext = this;

    private boolean linkServer = false; //连接是否成功的标识符

    public String getMessage = ""; //获取从服务端传回的信息
    public String linkName = null;//建立连接的名称

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        mContext = this;//初始化Context
        final WifiManager mWifiManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);

        //注册监听，实时监听网络状态
        final NetState receiver = new NetState();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(receiver, filter);
//        receiver.onReceive(mContext, null);

        filePath = (TextView)findViewById(R.id.textView6);
        //文件选择按钮，返回文件路径
        fileButton = (Button)findViewById(R.id.button12);
        fileButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, 1100);
            }
        });

        //连接按钮
        //wifi或者移动数据情况下，根据输入的“接收方用户名”，通过服务器进行连接
        //无流量情况下，通过搜索接收方所建立的热点，进行连接
        connectButton = (Button)findViewById(R.id.button14);
        connectButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                    //无网络连接
                    if(!mWifiManager.isWifiEnabled()){
                        //如果wifi不可用，打开wifi
                        mWifiManager.setWifiEnabled(true);
                    }
                    if(android.os.Build.VERSION.SDK_INT > 10) {
                        // 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
                        startActivity(new Intent( Settings.ACTION_WIFI_SETTINGS));
                    } else {
                        startActivity(new Intent( Settings.ACTION_WIRELESS_SETTINGS));
                    }

            }

        });

        //发送按钮
        sendButton = (Button)findViewById(R.id.button13);
        sendButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

            }
        });
    }


    /**
     * 选择完毕后在onActivityResult方法中回调     从data中拿到文件路径
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        filePath = (TextView)findViewById(R.id.textView5);
        String path = null;

        if (resultCode == Activity.RESULT_OK)
        {
            Uri uri = data.getData();
            FilePathResolver r = new FilePathResolver();
            path = r.getPath(mContext,uri);
            filePath.setText(path);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send, menu);
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


    /**
     * 解析文件名
     * @param fileUrl
     * @return
     */
    public String parseFileName(String fileUrl){
        String filename = null;
        String[] str1 = null;
        String[] str2 = null;

        str1 = fileUrl.split("/");
        str2 = str1[str1.length].split("%");

        filename = str2[1]+"."+str2[0];

        return filename;
    }

}

