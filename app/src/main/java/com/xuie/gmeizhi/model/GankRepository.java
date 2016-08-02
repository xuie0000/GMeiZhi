package com.xuie.gmeizhi.model;

import com.xuie.gmeizhi.data.MeizhiData;
import com.xuie.gmeizhi.data.entity.DGank;
import com.xuie.gmeizhi.data.entity.Gank;
import com.xuie.gmeizhi.data.entity.Meizhi;
import com.xuie.gmeizhi.data.休息视频Data;
import com.xuie.gmeizhi.model.api.DrakeetApi;
import com.xuie.gmeizhi.model.api.GankApi;
import com.xuie.gmeizhi.model.api.ServiceGenerator;
import com.xuie.gmeizhi.model.source.GankSource;
import com.xuie.gmeizhi.util.Dates;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by xuie on 16-7-23.
 */
public class GankRepository implements GankSource {

    private static GankRepository INSTANCE = null;
    private GankApi mGankApi = ServiceGenerator.createService(GankApi.class);
    private DrakeetApi mDrakeetApi = ServiceGenerator.createService(DrakeetApi.class);
    private Realm realm;

    public GankRepository() {
        realm = Realm.getDefaultInstance();
    }

    public static GankRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (GankRepository.class) {
                INSTANCE = new GankRepository();
            }
        }
        return INSTANCE;
    }

    @Override public Observable<List<Meizhi>> getMeiZhis(int page) {
        return Observable
                .zip(mGankApi.getMeizhiData(page),
                        mGankApi.get休息视频Data(page),
                        this::createMeizhiDataWith休息视频Desc)
                .subscribeOn(Schedulers.newThread())
                .map(meizhiData -> meizhiData.results)
                .flatMap(Observable::from)
                .toSortedList((meizhi1, meizhi2) -> meizhi2.publishedAt.compareTo(meizhi1.publishedAt));
    }

    @Override public Observable<List<Meizhi>> getRealmMeiZhis() {
        return Realm.getDefaultInstance().where(Meizhi.class)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .map(meizhis -> {
                    List<Meizhi> list = new ArrayList<>();
                    for (Meizhi m : meizhis) list.add(m);
                    return list;
                });
    }

    @Override public void saveMeizhis(List<Meizhi> meizhis) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(meizhis);
        realm.commitTransaction();
//        realm.close();
    }

    @Override public void clearMeiZhi() {
        realm.beginTransaction();
        realm.delete(Meizhi.class);
        realm.commitTransaction();
//        realm.close();
    }

    @Override public Observable<List<Gank>> getGankData(int year, int month, int day) {
        return mGankApi
                .getGankData(year, month, day)
                .subscribeOn(Schedulers.newThread())
                .map(data -> data.results)
                .map(results -> {
                    List<Gank> gankList = new ArrayList<>();
                    if (results.androidList != null) gankList.addAll(results.androidList);
                    if (results.iOSList != null) gankList.addAll(results.iOSList);
                    if (results.appList != null) gankList.addAll(results.appList);
                    if (results.拓展资源List != null) gankList.addAll(results.拓展资源List);
                    if (results.瞎推荐List != null) gankList.addAll(results.瞎推荐List);
                    if (results.休息视频List != null) gankList.addAll(0, results.休息视频List);
                    return gankList;
                });
    }

    @Override public Observable<DGank> getDGank(String where) {
        return mDrakeetApi.getDGankData(where)
                .map(dGankData -> dGankData.results)
                .single(dGanks -> dGanks.size() > 0)
                .map(dGanks -> dGanks.get(0));
    }

    private MeizhiData createMeizhiDataWith休息视频Desc(MeizhiData data, 休息视频Data love) {
        for (Meizhi meizhi : data.results) {
            meizhi.desc = meizhi.desc + " " + getFirstVideoDesc(meizhi.publishedAt, love.results);
        }
        return data;
    }


    private String getFirstVideoDesc(Date publishedAt, List<Gank> results) {
        String videoDesc = "";
        for (int i = 0; i < results.size(); i++) {
            Gank video = results.get(i);
            if (video.publishedAt == null) video.publishedAt = video.createdAt;
            if (Dates.isTheSameDay(publishedAt, video.publishedAt)) {
                videoDesc = video.desc;
                break;
            }
        }
        return videoDesc;
    }


}
