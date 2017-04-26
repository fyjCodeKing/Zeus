package com.filepassapp.chenze.filepass;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.filepassapp.chenze.filepass.Broadcast.NetState;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveActivity extends ActionBarActivity {
    private String IP = "192.168.43.106";

    private Button buildButton = null;
    private Button receiveButton = null;
    private Context mContext = null;

    private WifiManager wifiManager;
    private boolean flag=true;

    private String linkName = null;//建立连接的名称
    private boolean buildLink = false;//是否已建立连接
    private String getMessage = ""; //获取从服务器传回的信息
    private ProgressDialog pd; //进度条


    private String ad;
    private String name = null;
    private String pwd = null;

    public String getAd() {
        return ad;
    }
    public void setAd(String ad) {
        this.ad = ad;
    }

    private MyHandler myHandler;
    private FileHandler fileHandler; // 用于发送文件时，子线程与主线程交互，进度条调整

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        mContext = this;

        // 初始化进度条
        pd = new ProgressDialog(mContext);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(true);// 设置是否可以通过点击Back键取消
        pd.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        pd.setMessage("正在下载文件...");
        pd.setMax(100);

        myHandler = new MyHandler();
        fileHandler = new FileHandler();
        //获取wifi管理服务
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //注册监听，实时监听网络状态
        final NetState receiver = new NetState();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(receiver, filter);

        //连接wifi或者热点按钮
        buildButton = (Button)findViewById(R.id.button7);
        buildButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                int netState = receiver.getNetState();

                if(netState == 1){
                    //使用的是wifi网络
                    if(buildLink){
                        Toast.makeText(getApplicationContext(), "已建立连接："+linkName, Toast.LENGTH_LONG).show();
                    }else{
                        final EditText et = new EditText(mContext);

                        new AlertDialog.Builder(mContext).setTitle("创建连接名称")
                                .setIcon(R.drawable.build)
                                .setView(et)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        String input = et.getText().toString();
                                        if (input.equals("")) {
                                            Toast.makeText(getApplicationContext(), "连接名称不能为空！" + input, Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            linkName = input;
                                            //创建线程
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    try{
                                                        //建立连接到远程服务器的Socket
                                                        //服务器ip要么是公网ip 要么是和你在一个局域网下的服务器的局域网ip地址
                                                        Socket socket = new Socket(IP,8081);
                                                        // 将Socket对应的输出流包装为OutputStream
                                                        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                                                        outStream.writeUTF("build;" + linkName);

                                                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                                                socket.getInputStream()));
                                                        String get = br.readLine();

                                                        //向主线程通信
                                                        Message msg = new Message();
                                                        Bundle b = new Bundle();// 存放数据
                                                        b.putString("get",get);
                                                        msg.setData(b);
                                                        myHandler.sendMessage(msg);

                                                        outStream.close();
                                                        br.close();
                                                        socket.close();
                                                    }catch (IOException e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();

                                        }
                                    }
                                }).setNegativeButton("取消", null).show();

                    }

                }else if(netState == 2){
                    //使用的是移动网络，提醒用户是否确认传输
                    if(buildLink){
                        Toast.makeText(getApplicationContext(), "已建立连接："+linkName, Toast.LENGTH_LONG).show();
                    }else{
                        final EditText et = new EditText(mContext);

                        new AlertDialog.Builder(mContext).setTitle("创建连接名称")
                                .setIcon(R.drawable.build)
                                .setView(et)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        String input = et.getText().toString();
                                        if (input.equals("")) {
                                            Toast.makeText(getApplicationContext(), "连接名称不能为空！" + input, Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            linkName = input;
                                            //创建线程
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    try{
                                                        //建立连接到远程服务器的Socket
                                                        //服务器ip要么是公网ip 要么是和你在一个局域网下的服务器的局域网ip地址
                                                        Socket socket = new Socket(IP,8081);
                                                        // 将Socket对应的输出流包装为OutputStream
                                                        OutputStream outStream = socket.getOutputStream();
                                                        outStream.write(("build;" + linkName).getBytes());

                                                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                                                socket.getInputStream()));
                                                        String get = br.readLine();

                                                        //向主线程通信
                                                        Message msg = new Message();
                                                        Bundle b = new Bundle();// 存放数据
                                                        b.putString("get",get);
                                                        msg.setData(b);
                                                        myHandler.sendMessage(msg);

                                                        outStream.close();
                                                        br.close();

                                                        socket.close();
                                                    }catch (IOException e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();
                                        }
                                    }
                                }).setNegativeButton("取消", null).show();

                    }
                }else if(netState == 3){
                    //无网络连接，提示用户打开热点，进行无流量传输
                    if(flag==false){
                        Toast.makeText(getApplicationContext(), "热点已开启",
                                Toast.LENGTH_SHORT).show();
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveActivity.this);
                        builder.setIcon(R.drawable.build);
                        builder.setTitle("请输入用户名和密码");
                        //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
                        View view = LayoutInflater.from(ReceiveActivity.this).inflate(R.layout.dialog, null);
                        //    设置我们自己定义的布局文件作为弹出框的Content
                        builder.setView(view);

                        final EditText apName = (EditText)view.findViewById(R.id.name);
                        final EditText apPwd = (EditText)view.findViewById(R.id.password);

                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                name = apName.getText().toString();
                                pwd = apPwd.getText().toString();
                                if (name.equals("")||pwd.equals("")) {
                                    Toast.makeText(getApplicationContext(),"热点名或密码不能为空",
                                            Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    //开启热点
                                    setWifiApEnabled(true);
                                    Toast.makeText(getApplicationContext(),"热点开启成功",
                                            Toast.LENGTH_SHORT).show();
                                    buildLink = true;
                                }
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {}
                        });
                        builder.show();

                    }

                }
            }
        });

        receiveButton = (Button)findViewById(R.id.button8);
        receiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int netState = receiver.getNetState();

                if(netState == 1){
                    //wifi网络
                    if(buildLink){
                        //已经建立连接
                        pd.show();//显示文件下载进度条
                        downloadFile();
                    }
                }else if(netState == 2){
                    //移动数据网络
                    if(buildLink){
                        //已经建立连接
                        pd.show();//显示文件下载进度条
                        downloadFile();
                    }
                }else if(netState == 3){
                    //无网络连接
                    if(buildLink){
                        pd.show();//显示文件下载进度条
                        buildServerFile();
                    }
                }
            }
        });


    }

    /**
     * 从服务器下载文件
     */
    public void downloadFile(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Socket socket = new Socket(IP,8081);

                    DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                    outStream.writeUTF("receive;"+linkName);

                    InputStream inStream = socket.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
                    String get = br.readLine();

                    DataInputStream in = new DataInputStream(inStream);

                    String[] items = get.split(";");
                    String filename = items[0];
                    long length = Long.parseLong(items[1]);

                    File file = new File(Environment.getExternalStorageDirectory(),filename);

                    RandomAccessFile fileOutStream = new RandomAccessFile(
                            file, "rw");

                    byte[] buffer = new byte[1024];
                    int len = -1;
                    long progress = 0;
                    while ((len = in.read(buffer)) != -1) {// 从输入流中读取数据写入到文件中
                        fileOutStream.write(buffer, 0, 1024);
                        progress += 1024;

                        Message msg = new Message();
                        Bundle b = new Bundle();
                        b.putInt("size", (int)((progress*100)/length));
                        msg.setData(b);
                        fileHandler.sendMessage(msg);
                    }
                    Message msg = new Message();
                    Bundle b = new Bundle();
                    b.putInt("size", 100);
                    msg.setData(b);
                    fileHandler.sendMessage(msg);

                    outStream.close();
                    inStream.close();
                    fileOutStream.close();
                    socket.close();

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 无流量，自建服务器接收文件
     */
    public void buildServerFile(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    ServerSocket ss = new ServerSocket(5000);
                    Socket socket = ss.accept();

                    InputStream in = socket.getInputStream();
                    DataInputStream inStream = new DataInputStream(in);
                    String get = inStream.readUTF(); // 获取发送方传过来的信息
                    String items[] = get.split(";");

                    String filename = items[0]; //文件名
                    long length = Long.parseLong(items[1]); // 文件大小

                    //定义接受文件的路径与文件名
                    File file = new File(Environment.getExternalStorageDirectory(), filename);
                    RandomAccessFile fileOutStream = new RandomAccessFile(
                            file, "rw");

                    byte[] buffer = new byte[1024];
                    int len = -1;
                    long progress = 0;
                    while ((len = inStream.read(buffer)) != -1) {// 从输入流中读取数据写入到文件中
                        fileOutStream.write(buffer, 0, len);
                        progress += len;

                        Message msg = new Message();
                        Bundle b = new Bundle();
                        b.putInt("size", (int)((progress*100)/length));
                        msg.setData(b);
                        fileHandler.sendMessage(msg);
                    }
                    Message msg = new Message();
                    Bundle b = new Bundle();
                    b.putInt("size", 100);
                    msg.setData(b);
                    fileHandler.sendMessage(msg);

                    fileOutStream.close();
                    inStream.close();
                    socket.close();
                    ss.close();

                }catch(IOException e){

                }
            }
        }).start();

    }


    /**
     * 创建自定义Handler，实现线程间通信，更新UI
     */
    class MyHandler extends Handler {
        public MyHandler() {
        }

        public MyHandler(Looper L) {
            super(L);
        }

        // 子类必须重写此方法，接受数据
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            // 此处可以更新UI
            Bundle b = msg.getData();
            getMessage = b.getString("get");
            // 判断返回信息
            if(getMessage.equals("success")){
                //建立成功
                Toast.makeText(getApplicationContext(), "已建立连接："+linkName,
                        Toast.LENGTH_SHORT).show();
                buildLink = true;
            }else if(getMessage.equals("same")){
                Toast.makeText(getApplicationContext(), "已存在相同连接名",
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    // wifi热点开关
    public boolean setWifiApEnabled(boolean enabled) {
//        if (enabled) { // disable WiFi in any case
//
//        }

        //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
        wifiManager.setWifiEnabled(false);

        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = name;
            //配置热点的密码
            apConfig.preSharedKey= pwd;
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            return false;
        }
    }

    public String getAdd(){
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
//        if (!wifiManager.isWifiEnabled()) {
//            System.out.println("=================");
//            wifiManager.setWifiEnabled(true);
//        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String IPAddress = intToIp(wifiInfo.getIpAddress());
        //这是本机地址
        this.setAd(IPAddress);

        //System.out.println("IPAddress-->>" + IPAddress);

        DhcpInfo dhcpinfo = wifiManager.getDhcpInfo();
        //这是热点的地址
        String serverAddress = intToIp(dhcpinfo.serverAddress);
        //System.out.println("serverAddress-->>" + serverAddress);
        return serverAddress;
    }
    private String intToIp(int paramInt)
    {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_receive, menu);
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
     * 创建自定义Handler，实现线程间通信，更新UI
     */
    class FileHandler extends Handler {
        public FileHandler() {
        }

        public FileHandler(Looper L) {
            super(L);
        }

        // 子类必须重写此方法，接受数据
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            // 此处可以更新UI
            Bundle b = msg.getData();
            int size = b.getInt("size");

            // 显示传输进度
            pd.setProgress(size);
            if(size==100){
                pd.dismiss();
                Toast.makeText(getApplicationContext(), "文件已下载到："+Environment.getExternalStorageDirectory(),
                        Toast.LENGTH_SHORT).show();
            }


        }
    }
}
