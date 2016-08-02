package com.xuie.gmeizhi.ui.gank;

import com.xuie.gmeizhi.data.entity.Gank;
import com.xuie.gmeizhi.ui.BasePresenter;
import com.xuie.gmeizhi.ui.BaseView;

import java.util.List;

import okhttp3.OkHttpClient;

/**
 * Created by xuie on 16-7-25.
 */
public interface GankContracts {
    interface View extends BaseView<Presenter> {
        void showEmptyView();

        void showView(List<Gank> ganks);

        void startPreview(String preview);

        void getOldVideoPreview(OkHttpClient client);
    }

    interface Presenter extends BasePresenter {
        void getGank(int year, int month, int day);

        void getDGank(String where);

        void getVideoPreview();
    }
}
