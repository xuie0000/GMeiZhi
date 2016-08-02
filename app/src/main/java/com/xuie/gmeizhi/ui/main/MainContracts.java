package com.xuie.gmeizhi.ui.main;

import com.xuie.gmeizhi.ui.BasePresenter;
import com.xuie.gmeizhi.ui.BaseView;
import com.xuie.gmeizhi.data.entity.Meizhi;

import java.util.List;

/**
 * Created by xuie on 16-7-23.
 */
public interface MainContracts {
    interface View extends BaseView<Presenter> {
        void refreshMeiZhis(List<Meizhi> meizhis, boolean clean);

        void setRefresh(boolean refresh);

        void loadError(Throwable throwable);
    }

    interface Presenter extends BasePresenter {
        void getRemoteMeiZhis(int page);
        void getLocalMeiZhis();
    }
}
