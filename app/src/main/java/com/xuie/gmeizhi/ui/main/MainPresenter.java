package com.xuie.gmeizhi.ui.main;

import com.orhanobut.logger.Logger;
import com.xuie.gmeizhi.model.GankRepository;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by xuie on 16-7-23.
 */

public class MainPresenter implements MainContracts.Presenter {
    private final GankRepository mGankRepository;
    private final MainContracts.View mMainView;
    private CompositeSubscription mSubscriptions;

    public MainPresenter(GankRepository gankRepository, MainContracts.View mainView) {
        mGankRepository = checkNotNull(gankRepository, "gankRepository cannot be null");
        mMainView = checkNotNull(mainView, "mainView cannot be null");
        mSubscriptions = new CompositeSubscription();
        mMainView.setPresenter(this);
    }

    @Override public void subscribe() {
        getLocalMeiZhis();
        getRemoteMeiZhis(1);
    }

    @Override public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override public void getRemoteMeiZhis(int page) {
        Subscription s = mGankRepository.getMeiZhis(page)
                .doAfterTerminate(() -> mMainView.setRefresh(false))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(meizhis -> {
                    boolean clean = page == 1;
                    if (clean) mGankRepository.clearMeiZhi();
                    mGankRepository.saveMeizhis(meizhis);
                    mMainView.refreshMeiZhis(meizhis, page == 1);
                }, mMainView::loadError);

        mSubscriptions.add(s);
    }

    @Override public void getLocalMeiZhis() {
        mGankRepository.getRealmMeiZhis()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(meizhis -> {
                    if (meizhis.size() == 0) return;
                    Logger.d(meizhis.size() + ", " + meizhis.toString());
                    mMainView.refreshMeiZhis(meizhis, true);
                }, mMainView::loadError);
    }
}
