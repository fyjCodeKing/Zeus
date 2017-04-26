package com.filepassapp.chenze.filepass;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.filepassapp.chenze.filepass.FilePassUtil.StreamTool;

import java.io.File;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;


public class SendProcessActivity extends ActionBarActivity {

    private String filename = null;
    private TextView resultView = null;
    private ProgressBar uploadbar = null;
    private Button stopButton = null;
    private Button cancleButton = null;

    private boolean start=true;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            int length = msg.getData().getInt("size");
            uploadbar.setProgress(length);
            float num = (float)uploadbar.getProgress()/(float)uploadbar.getMax();
            int result = (int)(num * 100);
            resultView.setText(result+ "%");
            if(uploadbar.getProgress()==uploadbar.getMax()){
                Toast.makeText(getApplicationContext(), "发送成功",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_process);

        Bundle bundle = this.getIntent().getExtras();// 获取传递过来的封装了数据的Bundle
        filename = bundle.getString("filename");// 获取对应值
        uploadbar = (ProgressBar) this.findViewById(R.id.progressBar2);
        resultView = (TextView)this.findViewById(R.id.textView2);

        //跳转到此页面即开始传输文件
        start=true;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File uploadFile = new File(filename);
            if(uploadFile.exists()){
                uploadFile(uploadFile);
            }else{
                Toast.makeText(getApplicationContext(), "文件不存在！",
                        Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "存储器发生错误！",
                    Toast.LENGTH_SHORT).show();
        }

        //停止传输
        stopButton =(Button)this.findViewById(R.id.button10);
        stopButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                start = false;
            }
        });

        //取消传输
        cancleButton =(Button)this.findViewById(R.id.button11);
        cancleButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setClass(SendProcessActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 上传文件
     * @param uploadFile
     */
    private void uploadFile(final File uploadFile) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    uploadbar.setMax((int)uploadFile.length());
                    String head = "Content-Length="+ uploadFile.length() + ";filename="+ uploadFile.getName() + "\r\n";
                    Socket socket = new Socket("192.168.1.20",7878);
                    OutputStream outStream = socket.getOutputStream();
                    outStream.write(head.getBytes());

                    PushbackInputStream inStream = new PushbackInputStream(socket.getInputStream());
                    String response = StreamTool.readLine(inStream);
                    String[] items = response.split(";");
                    String responseid = items[0].substring(items[0].indexOf("=")+1);
                    String position = items[1].substring(items[1].indexOf("=")+1);

                    RandomAccessFile fileOutStream = new RandomAccessFile(uploadFile, "r");
                    fileOutStream.seek(Integer.valueOf(position));
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    int length = Integer.valueOf(position);
                    while(start&&(len = fileOutStream.read(buffer)) != -1){
                        outStream.write(buffer, 0, len);
                        length += len;
                        Message msg = new Message();
                        msg.getData().putInt("size", length);
                        handler.sendMessage(msg);
                    }
                    fileOutStream.close();
                    outStream.close();
                    inStream.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_process, menu);
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
