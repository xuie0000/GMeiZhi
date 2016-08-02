package com.xuie.gmeizhi.ui.main;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.orhanobut.logger.Logger;
import com.xuie.gmeizhi.R;
import com.xuie.gmeizhi.data.entity.Meizhi;
import com.xuie.gmeizhi.ui.adapter.MeizhiListAdapter;
import com.xuie.gmeizhi.ui.gank.GankActivity;
import com.xuie.gmeizhi.util.Once;
import com.zzt.KugouLayout.KugouLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements MainContracts.View {

    private static final int PRELOAD_SIZE = 6;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.kugou_layout) KugouLayout kugouLayout;
    @BindView(R.id.main_fab) FloatingActionButton mainFab;

    private MeizhiListAdapter mMeizhiListAdapter;
    private List<Meizhi> mMeizhiList = new ArrayList<>();
    private boolean mIsFirstTimeTouchBottom = true;
    private int mPage = 1;

    private MainContracts.Presenter presenter;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d("onViewCreated");
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        kugouLayout.setLayoutCloseListener(() -> kugouLayout.setVisibility(View.GONE));

        recyclerView.setLayoutManager(layoutManager);
        mMeizhiListAdapter = new MeizhiListAdapter(getActivity(), mMeizhiList);

        recyclerView.setAdapter(mMeizhiListAdapter);
        new Once(getActivity()).show("tip_guide_6", () ->
                Snackbar.make(recyclerView, getString(R.string.tip_guide), Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.i_know, v -> System.out.println(getString(R.string.i_know)))
                        .show());

        recyclerView.addOnScrollListener(getOnBottomListener(layoutManager));
        mMeizhiListAdapter.setOnItemClickListener((v, position) -> {
            if (v.getId() == R.id.tv_title) {
                startGankActivity(mMeizhiListAdapter.getMeizhi(position).publishedAt);
            } else if (v.getId() == R.id.iv_meizhi) {
                ImageView meizhi = new ImageView(getActivity());
                meizhi.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                meizhi.setBackgroundColor(Color.WHITE);
                Glide.with(this).load(mMeizhiListAdapter.getMeizhi(position).url).asBitmap().into(new BitmapImageViewTarget(meizhi) {
                    @Override protected void setResource(Bitmap resource) {
                        super.setResource(resource);
                        meizhi.setImageBitmap(resource);
                    }
                });
                kugouLayout.setContentView(meizhi);
                kugouLayout.setVisibility(View.VISIBLE);
                kugouLayout.setAnimType(KugouLayout.REBOUND_ANIM);
                kugouLayout.show();
            }
        });

        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        swipeRefreshLayout.setOnRefreshListener(this::requestDataRefresh);

        mainFab.setOnClickListener(v -> {
            if (mMeizhiList != null && mMeizhiList.size() > 0) {
                startGankActivity(mMeizhiList.get(0).publishedAt);
            }
        });
    }

    @Override public void setPresenter(MainContracts.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    @Override public void onResume() {
        super.onResume();
        presenter.subscribe();
    }

    @Override public void onPause() {
        super.onPause();
        presenter.unsubscribe();
    }

    private void loadData() {
        presenter.getRemoteMeiZhis(mPage);
    }

    private void requestDataRefresh() {
        mPage = 1;
        loadData();
    }

    private void startGankActivity(Date publishedAt) {
        Intent intent = new Intent(getActivity(), GankActivity.class);
        intent.putExtra(GankActivity.EXTRA_GANK_DATE, publishedAt);
        startActivity(intent);
    }

    RecyclerView.OnScrollListener getOnBottomListener(StaggeredGridLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView rv, int dx, int dy) {
                boolean isBottom =
                        layoutManager.findLastCompletelyVisibleItemPositions(new int[2])[1] >=
                                mMeizhiListAdapter.getItemCount() - PRELOAD_SIZE;
                if (!swipeRefreshLayout.isRefreshing() && isBottom) {
                    if (!mIsFirstTimeTouchBottom) {
                        swipeRefreshLayout.setRefreshing(true);
                        mPage += 1;
                        loadData();
                    } else {
                        mIsFirstTimeTouchBottom = false;
                    }
                }
            }
        };
    }

    @Override public void refreshMeiZhis(List<Meizhi> meizhis, boolean clean) {
        if (meizhis.size() == 0) return;
        if (clean) mMeizhiList.clear();
        mMeizhiList.addAll(meizhis);
        mMeizhiListAdapter.notifyDataSetChanged();
    }

    @Override public void setRefresh(boolean refresh) {
        if (swipeRefreshLayout == null) {
            return;
        }
        if (!refresh) {
            // 让子弹飞一会儿.
            swipeRefreshLayout.postDelayed(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1000);
        } else {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override public void loadError(Throwable throwable) {
        throwable.printStackTrace();
        Snackbar.make(recyclerView, R.string.snap_load_fail, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> requestDataRefresh())
                .show();
    }

}
