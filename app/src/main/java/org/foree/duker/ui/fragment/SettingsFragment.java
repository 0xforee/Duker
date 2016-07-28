package org.foree.duker.ui.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.foree.duker.R;

/**
 * Created by foree on 16-7-28.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_all);
    }
}
