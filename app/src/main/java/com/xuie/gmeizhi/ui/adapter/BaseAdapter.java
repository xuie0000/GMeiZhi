package com.xuie.gmeizhi.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class BaseAdapter<T extends BaseAdapter.ViewHolder> extends RecyclerView.Adapter<T> {

    protected OnItemClickListener mOnItemClickListener;
    protected OnItemLongClickListener mOnItemLongClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override public void onClick(View v) {
            if (mOnItemClickListener != null && getAdapterPosition() != -1) {
                mOnItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null && getAdapterPosition() != -1) {
                mOnItemLongClickListener.onItemLongClick(v, getAdapterPosition());
                return true;
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View v, int position);
    }

}
