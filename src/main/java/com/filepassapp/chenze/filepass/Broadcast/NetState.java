package com.filepassapp.chenze.filepass.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by chenze on 2017/2/2.
 */
public class NetState extends BroadcastReceiver {
    private int netState = 0;//网络状态标识 1=wifi，2=数据，3=无网络

    @Override
    public void onReceive(Context context, Intent arg1) {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(!gprs.isConnected() && wifi.isConnected())
        {
//            AlertDialog.Builder ab = new AlertDialog.Builder(context);
//            ab.setMessage("当前使用的是wifi网络").setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    // TODO Auto-generated method stub
//                    dialog.dismiss();
//                }
//            }).show();
            Toast.makeText(context.getApplicationContext(), "当前使用的是wifi网络",
                    Toast.LENGTH_SHORT).show();

            netState = 1;
        }else if(gprs.isConnected() && !wifi.isConnected()){
//            AlertDialog.Builder ab = new AlertDialog.Builder(context);
//            ab.setMessage("当前使用的是移动数据网络").setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    // TODO Auto-generated method stub
//                    dialog.dismiss();
//                }
//            }).show();

            Toast.makeText(context.getApplicationContext(), "当前使用的是移动数据网络",
                    Toast.LENGTH_SHORT).show();
            netState = 2;
        }else if(!gprs.isConnected() && !wifi.isConnected()){
//            AlertDialog.Builder ab = new AlertDialog.Builder(context);
//            ab.setMessage("当前无网络连接").setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    // TODO Auto-generated method stub
//                    dialog.dismiss();
//                }
//            }).show();
            Toast.makeText(context.getApplicationContext(), "当前无网络连接",
                    Toast.LENGTH_SHORT).show();
            netState = 3;
        }
    }

    public int getNetState(){
        return netState;
    }
}
