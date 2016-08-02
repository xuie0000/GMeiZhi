package com.xuie.gmeizhi.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.xuie.gmeizhi.R;
import com.xuie.gmeizhi.ui.about.AboutActivity;
import com.xuie.gmeizhi.ui.web.WebActivity;
import com.xuie.gmeizhi.util.ActivityUtils;
import com.xuie.gmeizhi.util.Injection;
import com.xuie.gmeizhi.util.Once;
import com.xuie.gmeizhi.util.PreferenceUtils;
import com.xuie.gmeizhi.util.Toasts;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        if (mainFragment == null) {
            // Create the fragment
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mainFragment, R.id.main_fragment);
        }

        new MainPresenter(Injection.provideGankRepository(), mainFragment);
    }

    private void openGitHubTrending() {
        String url = getString(R.string.url_github_trending);
        String title = getString(R.string.action_github_trending);
        Intent intent = WebActivity.newIntent(this, url, title);
        startActivity(intent);
    }

    private void loginGitHub() {
        new Once(this).show(R.string.action_github_login, () -> Toasts.showLongX2(getString(R.string.tip_login_github)));
        String url = getString(R.string.url_login_github);
        Intent intent = WebActivity.newIntent(this, url,
                getString(R.string.action_github_login));
        startActivity(intent);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_notifiable);
        initNotifiableItemState(item);
        return true;
    }


    private void initNotifiableItemState(MenuItem item) {
        item.setChecked(PreferenceUtils.getBoolean(this, getString(R.string.action_notifiable), true));
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.action_login:
                loginGitHub();
                return true;
            case R.id.action_trending:
                openGitHubTrending();
                return true;
            case R.id.action_notifiable:
                boolean isChecked = !item.isChecked();
                item.setChecked(isChecked);
                PreferenceUtils.save(this, getString(R.string.action_notifiable), isChecked);
                Toasts.showShort(isChecked ? R.string.notifiable_on : R.string.notifiable_off);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
