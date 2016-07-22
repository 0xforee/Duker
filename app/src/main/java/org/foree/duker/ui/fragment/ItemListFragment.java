package org.foree.duker.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    private RecyclerView mRecyclerView;
    private ItemListAdapter mAdapter;

    public ItemListFragment() {
        // Required empty public constructor
    }

    public static ItemListFragment newInstance(String feedId) {
        ItemListFragment f = new ItemListFragment();

        Bundle args = new Bundle();

        args.putString(KEY_TITLE, feedId);
        f.setArguments(args);

        return (f);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout)inflater.inflate(R.layout.fragment_itemlist, container, false);

        TextView tv = (TextView)linearLayout.findViewById(R.id.tv_test_feed_id);
        tv.setText(getArguments().getString(KEY_TITLE));

        mRecyclerView = (RecyclerView) linearLayout.findViewById(R.id.rv_item_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mRecyclerView.setAdapter(mAdapter = new ItemListAdapter());

        return linearLayout;
    }

    class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.MyViewHolder>{

        @Override
        public ItemListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.item_list_holder, parent, false));

            return holder;
        }

        @Override
        public void onBindViewHolder(ItemListAdapter.MyViewHolder holder, int position) {
            holder.tv.setText("hah");
        }

        @Override
        public int getItemCount() {
            return 10;
        }


        class MyViewHolder extends RecyclerView.ViewHolder{
            TextView tv;
            public MyViewHolder(View view){
                super(view);
                tv = (TextView)view.findViewById(R.id.tv_item);
            }
        }
    }
}
