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

package com.xuie.gmeizhi.ui.gank;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.xuie.gmeizhi.util.Injection;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by drakeet on 8/11/15.
 */
public class GankPagerAdapter extends FragmentPagerAdapter {

    private Date mDate;

    public GankPagerAdapter(FragmentManager fm, Date date) {
        super(fm);
        mDate = date;
    }


    @Override public Fragment getItem(int position) {
        GankFragment gankFragment;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);
        calendar.add(Calendar.DATE, -position);
        gankFragment = GankFragment.newInstance(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));

        new GankPresenter(Injection.provideGankRepository(), gankFragment);
        return gankFragment;
    }


    @Override public int getCount() {
        return 5;
    }
}