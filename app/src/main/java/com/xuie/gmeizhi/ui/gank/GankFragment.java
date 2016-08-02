package com.xuie.gmeizhi.ui.gank;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;
import com.xuie.gmeizhi.R;
import com.xuie.gmeizhi.data.entity.Gank;
import com.xuie.gmeizhi.ui.LoveBus;
import com.xuie.gmeizhi.ui.OnKeyBackClickEvent;
import com.xuie.gmeizhi.ui.adapter.GankListAdapter;
import com.xuie.gmeizhi.ui.web.WebActivity;
import com.xuie.gmeizhi.util.LoveStrings;
import com.xuie.gmeizhi.util.Once;
import com.xuie.gmeizhi.util.Toasts;
import com.xuie.gmeizhi.widget.LoveVideoView;
import com.xuie.gmeizhi.widget.VideoImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.google.common.base.Preconditions.checkNotNull;

public class GankFragment extends Fragment implements GankContracts.View {

    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY = "day";

    @BindView(R.id.rv_gank) RecyclerView mRecyclerView;
    @BindView(R.id.stub_empty_view) ViewStub mEmptyViewStub;
    @BindView(R.id.stub_video_view) ViewStub mVideoViewStub;
    @BindView(R.id.iv_video) VideoImageView mVideoImageView;
    private LoveVideoView mVideoView;

    private int mYear, mMonth, mDay;
    private List<Gank> mGankList;
    private String mVideoPreviewUrl;
    private boolean mIsVideoViewInflated = false;
    private GankListAdapter mAdapter;
    private GankContracts.Presenter mPresenter;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static GankFragment newInstance(int year, int month, int day) {
        GankFragment fragment = new GankFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGankList = new ArrayList<>();
        mAdapter = new GankListAdapter(mGankList);
        parseArguments();
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    private void parseArguments() {
        Bundle bundle = getArguments();
        mYear = bundle.getInt(ARG_YEAR);
        mMonth = bundle.getInt(ARG_MONTH);
        mDay = bundle.getInt(ARG_DAY);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gank, container, false);
        ButterKnife.bind(this, rootView);
        initRecyclerView();
        setVideoViewPosition(getResources().getConfiguration());
        return rootView;
    }


    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mGankList.size() == 0) loadData();
        if (mVideoPreviewUrl != null) {
            Glide.with(this).load(mVideoPreviewUrl).into(mVideoImageView);
        }
    }


    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override public void setPresenter(GankContracts.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    private void loadData() {
        loadVideoPreview();

        mPresenter.getGank(mYear, mMonth, mDay);
    }

    private void loadVideoPreview() {
        String where = String.format("{\"tag\":\"%d-%d-%d\"}", mYear, mMonth, mDay);
        mPresenter.getDGank(where);
    }


    @Override public void getOldVideoPreview(OkHttpClient client) {
        String url = "http://gank.io/" + String.format("%s/%s/%s", mYear, mMonth, mDay);
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                mVideoPreviewUrl = LoveStrings.getVideoPreviewImageUrl(body);
                startPreview(mVideoPreviewUrl);
            }
        });
    }


    @Override public void startPreview(String preview) {
        mVideoPreviewUrl = preview;
        if (preview != null && mVideoImageView != null) {
            mVideoImageView.post(() -> Glide.with(mVideoImageView.getContext()).load(preview).into(mVideoImageView));
        }
    }


    @Override public void showEmptyView() {
        mEmptyViewStub.inflate();
    }

    @Override public void showView(List<Gank> ganks) {
        mGankList.addAll(ganks);
        mAdapter.notifyDataSetChanged();
    }


    @OnClick(R.id.header_appbar) void onPlayVideo() {
        resumeVideoView();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (mGankList.size() > 0 && mGankList.get(0).type.equals("休息视频")) {
            Toasts.showLongX2(R.string.loading);
        } else {
            closePlayer();
        }
    }


    private void setVideoViewPosition(Configuration newConfig) {
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE: {
                if (mIsVideoViewInflated) {
                    mVideoViewStub.setVisibility(View.VISIBLE);
                } else {
                    mVideoView = (LoveVideoView) mVideoViewStub.inflate();
                    mIsVideoViewInflated = true;
                    String tip = getString(R.string.tip_video_play);
                    // @formatter:off
                    new Once(mVideoView.getContext()).show(tip, () ->
                            Snackbar.make(mVideoView, tip, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.i_know, v -> {
                                    })
                                    .show());
                    // @formatter:on
                }
                if (mGankList.size() > 0 && mGankList.get(0).type.equals("休息视频")) {
                    mVideoView.loadUrl(mGankList.get(0).url);
                }
                break;
            }
            case Configuration.ORIENTATION_PORTRAIT:
            case Configuration.ORIENTATION_UNDEFINED:
            default: {
                mVideoViewStub.setVisibility(View.GONE);
                break;
            }
        }
    }

    private void closePlayer() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toasts.showShort(getString(R.string.tip_for_no_gank));
    }


    @Override public void onConfigurationChanged(Configuration newConfig) {
        setVideoViewPosition(newConfig);
        super.onConfigurationChanged(newConfig);
    }


    @Subscribe public void onKeyBackClick(OnKeyBackClickEvent event) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        clearVideoView();
    }


//    @Override public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        switch (id) {
//            case R.id.action_share:
//                if (mGankList.size() != 0) {
//                    Gank gank = mGankList.get(0);
//                    String shareText = gank.desc + gank.url +
//                            getString(R.string.share_from);
//                    Shares.share(getActivity(), shareText);
//                } else {
//                    Shares.share(getContext(), R.string.share_text);
//                }
//                return true;
//            case R.id.action_subject:
//                openTodaySubject();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }


    private void openTodaySubject() {
        String url = getString(R.string.url_gank_io) +
                String.format("%s/%s/%s", mYear, mMonth, mDay);
        Intent intent = WebActivity.newIntent(getActivity(), url,
                getString(R.string.action_subject));
        startActivity(intent);
    }

    @Override public void onResume() {
        super.onResume();
        mPresenter.subscribe();
        LoveBus.getLovelySeat().register(this);
        resumeVideoView();
    }


    @Override public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
        LoveBus.getLovelySeat().unregister(this);
        pauseVideoView();
        clearVideoView();
    }


    @Override public void onDestroyView() {
        super.onDestroyView();
    }


    @Override public void onDestroy() {
        super.onDestroy();
        resumeVideoView();
    }


    private void pauseVideoView() {
        if (mVideoView != null) {
            mVideoView.onPause();
            mVideoView.pauseTimers();
        }
    }


    private void resumeVideoView() {
        if (mVideoView != null) {
            mVideoView.resumeTimers();
            mVideoView.onResume();
        }
    }


    private void clearVideoView() {
        if (mVideoView != null) {
            mVideoView.clearHistory();
            mVideoView.clearCache(true);
            mVideoView.loadUrl("about:blank");
            mVideoView.pauseTimers();
        }
    }
}
