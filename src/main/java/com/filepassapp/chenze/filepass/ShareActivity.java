package com.filepassapp.chenze.filepass;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.filepassapp.chenze.filepass.Broadcast.NetState;
import com.filepassapp.chenze.filepass.FilePassUtil.FilePathResolver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.Socket;


public class ShareActivity extends ActionBarActivity {

    private Button fileButton = null;
    private Button sendButton = null;
    private Button connectButton = null;
    private TextView filePath = null;
    private ImageView img=null;


    private Context mContext = this;

    private boolean linkServer = false; //连接是否成功的标识符

    public String getMessage = ""; //获取从服务端传回的信息
    public String linkName = null;//建立连接的名称

    private ProgressDialog pd; //进度条
    private FileHandler fileHandler; // 用于发送文件时，子线程与主线程交互，进度条调整

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        mContext = this;//初始化Context

        WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManage.setWifiEnabled(false);

        fileHandler = new FileHandler();

        // 初始化进度条
        pd = new ProgressDialog(mContext);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(true);// 设置是否可以通过点击Back键取消
        pd.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        pd.setMessage("正在上传文件...");
        pd.setMax(100);

        final WifiManager mWifiManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);

        //注册监听，实时监听网络状态
        final NetState receiver = new NetState();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(receiver, filter);
//        receiver.onReceive(mContext, null);

        filePath = (TextView)findViewById(R.id.textView6);
        img=(ImageView) findViewById(R.id.imageView);
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
                int netState = receiver.getNetState();

                if(netState==1){
                    Toast.makeText(getApplicationContext(), "请关闭WIFI连接",
                            Toast.LENGTH_SHORT).show();
                }else if(netState==2){
                    Toast.makeText(getApplicationContext(), "请关闭移动数据",
                            Toast.LENGTH_SHORT).show();
                }else if(netState==3){
                    //无网络连接
                    if(!mWifiManager.isWifiEnabled()){
                        //如果wifi不可用，打开wifi
                        linkServer = true;
                        mWifiManager.setWifiEnabled(true);
                    }
                    if(android.os.Build.VERSION.SDK_INT > 10) {
                        // 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
                        startActivity(new Intent( Settings.ACTION_WIFI_SETTINGS));
                    } else {
                        startActivity(new Intent( Settings.ACTION_WIRELESS_SETTINGS));
                    }
                }



            }

        });

        //发送按钮
        sendButton = (Button)findViewById(R.id.button13);
        sendButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if(wifiManage.isWifiEnabled()){
                    DhcpInfo info = wifiManage.getDhcpInfo();
                    WifiInfo wifiinfo = wifiManage.getConnectionInfo();
                    String ip = intToIp(wifiinfo.getIpAddress());
                    String serverAddress = intToIp(info.serverAddress);

                    if(filePath.getText().toString().equals("请选择文件路径")){
                        Toast.makeText(getApplicationContext(), "请选择文件",
                                Toast.LENGTH_SHORT).show();
                    }else{

                        if(linkServer==false){
                            Toast.makeText(getApplicationContext(), "请先连接接收方",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            pd.show();//显示文件传进度条

                            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                File uploadFile = new File(filePath.getText().toString());
                                if(uploadFile.exists()){
                                    // 文件存在，发送
                                    uploadFile(uploadFile,ip);
                                }else{
                                    Toast.makeText(getApplicationContext(), "文件不存在！",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(), "存储器发生错误！",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }

            }
        });
    }

    /**
     * 无流量，向服务器上传文件
     * @param uploadFile
     */
    public void uploadFile(final File uploadFile, final String IP) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String head = uploadFile.getName() + ";"+ uploadFile.length();
                    Socket socket = new Socket("192.168.43.1",5000);
                    DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                    outStream.writeUTF(head);

                    RandomAccessFile fileOutStream = new RandomAccessFile(uploadFile, "r");
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    long progress = 0;
                    long length = uploadFile.length();

                    while((len = fileOutStream.read(buffer)) != -1){
                        outStream.write(buffer, 0, len);
                        progress += len;

                        Message msg = new Message();
                        Bundle b = new Bundle();
                        b.putInt("size", (int)((progress*100)/length));
                        msg.setData(b);
                        fileHandler.sendMessage(msg);
                    }

                    fileOutStream.close();
                    outStream.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                Toast.makeText(getApplicationContext(), "文件上传成功！",
                        Toast.LENGTH_SHORT).show();
            }


        }
    }

    /**
     * 将获取的int转为真正的ip地址
     */
    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }


    /**
     * 选择完毕后在onActivityResult方法中回调     从data中拿到文件路径
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        filePath = (TextView)findViewById(R.id.textView6);
        String path = null;

        if (resultCode == Activity.RESULT_OK)
        {
            Uri uri = data.getData();
            FilePathResolver r = new FilePathResolver();
            path = r.getPath(mContext,uri);
            filePath.setText(path);
            filePath.setVisibility(View.INVISIBLE);
            Uri imuri=Uri.parse("file://"+filePath.getText().toString());

            if(filePath.getText().toString().indexOf(".mp4")>0){
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(uri.getPath());
                Bitmap bitmap = mmr.getFrameAtTime();//获取第一帧图片
                img.setImageBitmap(bitmap);
                mmr.release();//释放资源

            }else if(filePath.getText().toString().indexOf(".flv")>0){
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(uri.getPath());
                Bitmap bitmap = mmr.getFrameAtTime();//获取第一帧图片
                img.setImageBitmap(bitmap);
                mmr.release();//释放资源

            }else if(filePath.getText().toString().indexOf(".avi")>0){
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(uri.getPath());
                Bitmap bitmap = mmr.getFrameAtTime();//获取第一帧图片
                img.setImageBitmap(bitmap);
                mmr.release();//释放资源

            }else if(filePath.getText().toString().indexOf(".mov")>0){
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(uri.getPath());
                Bitmap bitmap = mmr.getFrameAtTime();//获取第一帧图片
                img.setImageBitmap(bitmap);
                mmr.release();//释放资源

            }else if(filePath.getText().toString().indexOf(".wmv")>0){
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(uri.getPath());
                Bitmap bitmap = mmr.getFrameAtTime();//获取第一帧图片
                img.setImageBitmap(bitmap);
                mmr.release();//释放资源

            }else{
                Bitmap bm = BitmapFactory.decodeFile(imuri.getPath());
                img.setImageBitmap(bm);
            }

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
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
