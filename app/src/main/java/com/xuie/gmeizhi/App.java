package com.xuie.gmeizhi;

import android.app.Application;

import com.orhanobut.logger.Logger;
import com.xuie.gmeizhi.util.Toasts;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by xuie on 16-7-13.
 */
public class App extends Application {
    @Override public void onCreate() {
        super.onCreate();
        Logger.init();

        RealmConfiguration configuration = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(configuration);

        Toasts.register(this);
    }
}
