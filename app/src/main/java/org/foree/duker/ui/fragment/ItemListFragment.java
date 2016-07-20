package org.foree.duker.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;

import org.foree.duker.R;

/**
 * Created by foree on 16-7-20.
 */
public class ItemListFragment extends Fragment{
    private static final String KEY_TITLE = "title";

    private Drawer result;

    public ItemListFragment() {
        // Required empty public constructor
    }

    public static ItemListFragment newInstance(String title) {
        ItemListFragment f = new ItemListFragment();

        Bundle args = new Bundle();

        args.putString(KEY_TITLE, title);
        f.setArguments(args);

        return (f);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout)inflater.inflate(R.layout.fragment_itemlist, container, false);

        TextView tv = (TextView)linearLayout.findViewById(R.id.tv_test_feed_id);
        tv.setText(getArguments().getString(KEY_TITLE));

        return linearLayout;
    }
}
