package com.xuie.gmeizhi.model.source;

import com.xuie.gmeizhi.data.entity.DGank;
import com.xuie.gmeizhi.data.entity.Gank;
import com.xuie.gmeizhi.data.entity.Meizhi;

import java.util.List;

import rx.Observable;

/**
 * Created by xuie on 16-7-23.
 */

public interface GankSource {
    Observable<List<Meizhi>> getMeiZhis(int page);
    Observable<List<Meizhi>> getRealmMeiZhis();
    void saveMeizhis(List<Meizhi> meizhis);
    void clearMeiZhi();

    Observable<List<Gank>> getGankData(int year, int month, int day);
    Observable<DGank> getDGank(String where);
}
