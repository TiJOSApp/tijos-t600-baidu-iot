package net.tijos.tikit;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import net.tijos.tikit.service.MonitorService;

/**
 * Created by Mars on 2017/10/26.
 */

public class MyApplication extends Application {

    private MonitorService monitorService;

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            monitorService = ((MonitorService.LocalBinder)binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        bindService(new Intent(this, MonitorService.class), conn, Context.BIND_AUTO_CREATE);


    }


    public MonitorService getMonitorService() {
        return monitorService;
    }
}
