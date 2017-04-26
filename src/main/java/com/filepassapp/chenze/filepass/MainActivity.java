package com.filepassapp.chenze.filepass;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {
    private Button sendButton = null;
    private Button receiveButton = null;
    private Button shareButton  = null;
    private Button fileManagerButton=null;
    private Button loginButton = null;
    private String ip = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //跳转到发送文件页面
        sendButton = (Button)findViewById(R.id.button9);
        sendButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //wifi或者移动数据
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,SendActivity.class);
                startActivity(intent);
            }
        });

        //跳转到接收文件页面
        receiveButton = (Button)findViewById(R.id.button2);
        receiveButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,ReceiveActivity.class);
                startActivity(intent);
            }
        });

        //跳转无流量发送
        shareButton = (Button)findViewById(R.id.button3);
        shareButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,ShareActivity.class);
                startActivity(intent);
            }
        });
        fileManagerButton = (Button)findViewById(R.id.button);
        fileManagerButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, com.filepassapp.chenze.filepass.FileListActivity.class);
                startActivity(intent);
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
