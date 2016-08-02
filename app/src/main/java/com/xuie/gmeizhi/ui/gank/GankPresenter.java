package com.xuie.gmeizhi.ui.gank;

import com.xuie.gmeizhi.data.entity.Gank;
import com.xuie.gmeizhi.model.GankRepository;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by xuie on 16-7-25.
 */

public class GankPresenter implements GankContracts.Presenter {
    private final GankRepository mGankRepository;
    private final GankContracts.View mGankView;
    private CompositeSubscription mSubscriptions;
    private List<Gank> mGankList;

    public GankPresenter(GankRepository gankRepository, GankContracts.View mainView) {
        mGankRepository = checkNotNull(gankRepository, "gankRepository cannot be null");
        mGankView = checkNotNull(mainView, "mainView cannot be null");
        mSubscriptions = new CompositeSubscription();
        mGankView.setPresenter(this);
        mGankList = new ArrayList<>();
    }

    @Override public void getGank(int year, int month, int day) {
        mGankRepository
                .getGankData(year, month, day)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.isEmpty()) {
                        mGankView.showEmptyView();
                    } else {
                        mGankView.showView(list);
                    }
                }, Throwable::printStackTrace);
    }

    @Override public void getDGank(String where) {
        mGankRepository.getDGank(where)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dGank -> mGankView.startPreview(dGank.preview),
                        throwable -> mGankView.getOldVideoPreview(new OkHttpClient()));
    }

    @Override public void getVideoPreview() {

    }

    @Override public void subscribe() {

    }

    @Override public void unsubscribe() {
        mSubscriptions.clear();
    }
}
