package cn.cj.dlna;

import android.app.Application;

/**
 * Created by June on 2018/6/14.
 */
public class AppContext extends Application {

    private static AppContext sInstance;

    public static AppContext getsInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

}

