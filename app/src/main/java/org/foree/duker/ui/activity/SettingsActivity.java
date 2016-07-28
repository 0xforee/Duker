package org.foree.duker.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.foree.duker.R;
import org.foree.duker.base.BaseActivity;
import org.foree.duker.ui.fragment.SettingsFragment;

/**
 * Created by foree on 16-7-28.
 */
public class SettingsActivity extends BaseActivity{
    private static final String TAG = SettingsActivity.class.getSimpleName();

    /**
     * keys for Application preference
     */
    //key: change theme
    public static final String KEY_CHANGE_THEME = "pf_change_theme";
    //key: dark theme
    public static final String KEY_DARK_THEME = "lp_dark_theme";
    //key: download only on wifi
    public static final String KEY_DOWNLOAD_ON_WIFI = "lp_download_only_wifi";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        getFragmentManager().beginTransaction()
                .replace(R.id.fr_settings, new SettingsFragment())
                .commit();

    }
}
