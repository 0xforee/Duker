package org.foree.duker.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.foree.duker.R;
import org.foree.duker.api.AbsApiFactory;
import org.foree.duker.api.AbsApiHelper;
import org.foree.duker.api.ApiFactory;
import org.foree.duker.api.FeedlyApiArgs;
import org.foree.duker.api.LocalApiHelper;
import org.foree.duker.dao.RssDaoHelper;
import org.foree.duker.net.NetCallback;
import org.foree.duker.provider.ItemListObserver;
import org.foree.duker.provider.RssObserver;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.ui.activity.ArticleActivity;
import org.foree.duker.ui.fragment.ItemListAdapter.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 16-7-20.
 */
public class ItemListFragment extends Fragment {
    private static final String TAG = ItemListFragment.class.getSimpleName();

    private static final String KEY_FEEDID = "feedId";

    private RecyclerView mRecyclerView;
    private ItemListAdapter mAdapter;
    private AbsApiHelper localApiHelper;

    private List<RssItem> itemList = new ArrayList<>();

    RssDaoHelper rssDaoHelper;

    private ContentObserver mContentObserver;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_SYNC_START:
                    Log.d(TAG, "update recycle view");
                    syncDate();
                    break;
            }
        }
    };
    public static final int MSG_SYNC_START = 0;

    public ItemListFragment() {
        // Required empty public constructor
    }

    public static ItemListFragment newInstance(String feedId) {
        ItemListFragment f = new ItemListFragment();

        Bundle args = new Bundle();

        args.putString(KEY_FEEDID, feedId);
        f.setArguments(args);

        return (f);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AbsApiFactory absApiFactory = new ApiFactory();
        localApiHelper = absApiFactory.createApiHelper(LocalApiHelper.class);

        mContentObserver = new ItemListObserver(mHandler);

        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        getActivity().getContentResolver().unregisterContentObserver(mContentObserver);
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView" );
        LinearLayout linearLayout = (LinearLayout)inflater.inflate(R.layout.fragment_itemlist, container, false);

        mRecyclerView = (RecyclerView) linearLayout.findViewById(R.id.rv_item_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));

        rssDaoHelper = new RssDaoHelper(getActivity());

        getActivity().getContentResolver().registerContentObserver(RssObserver.URI_ENTRY, true, mContentObserver);

        initAdapter();

        syncDate();



        return linearLayout;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void initAdapter() {
        mAdapter = new ItemListAdapter(getActivity(), itemList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getActivity(), position + "", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), ArticleActivity.class);
                intent.putExtra("entry", itemList.get(position));
                rssDaoHelper.updateUnreadByEntryId(itemList.get(position).getEntryId(), false);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void syncDate(){

        String feedId = getArguments().getString(KEY_FEEDID);
        FeedlyApiArgs args = new FeedlyApiArgs();
        // getItemList
        localApiHelper.getStream("", feedId, args, new NetCallback<List<RssItem>>() {
            @Override
            public void onSuccess(List<RssItem> data) {
                itemList.clear();
                itemList.addAll(data);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(String msg) {

            }
        });
    }

}
