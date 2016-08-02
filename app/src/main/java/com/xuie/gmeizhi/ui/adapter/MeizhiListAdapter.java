/*
 * Copyright (C) 2015 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of Meizhi
 *
 * Meizhi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Meizhi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Meizhi.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xuie.gmeizhi.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xuie.gmeizhi.R;
import com.xuie.gmeizhi.data.entity.Meizhi;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by drakeet on 6/20/15.
 */
public class MeizhiListAdapter extends BaseAdapter<MeizhiListAdapter.ViewHolder> {
    private List<Meizhi> mList;
    private Context mContext;

    public MeizhiListAdapter(Context context, List<Meizhi> meizhiList) {
        mList = meizhiList;
        mContext = context;
    }


    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.meizhi_item, parent, false);
        return new ViewHolder(v);
    }


    @Override public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Meizhi meizhi = mList.get(position);
        int limit = 48;
        String text = meizhi.desc.length() > limit ? meizhi.desc.substring(0, limit) +
                "..." : meizhi.desc;
        viewHolder.titleView.setText(text);

        Glide.with(mContext)
                .load(meizhi.url)
                .centerCrop()
                .into(viewHolder.meizhiView);

        viewHolder.meizhiView.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, position);
            }
        });
        viewHolder.titleView.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, position);
            }
        });
    }

    @Override public int getItemCount() {
        return mList.size();
    }

    public Meizhi getMeizhi(int position) {
        return mList.get(position);
    }

    class ViewHolder extends BaseAdapter.ViewHolder {
        @BindView(R.id.iv_meizhi) ImageView meizhiView;
        @BindView(R.id.tv_title) TextView titleView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
