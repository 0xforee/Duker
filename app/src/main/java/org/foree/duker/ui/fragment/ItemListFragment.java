package org.foree.duker.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
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
import org.foree.duker.api.FeedlyApiHelper;
import org.foree.duker.api.LocalApiHelper;
import org.foree.duker.dao.RssDao;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.service.StreamReceiverService;
import org.foree.duker.ui.activity.ArticleActivity;
import org.foree.duker.ui.fragment.ItemListAdapter.OnItemClickListener;

import java.util.List;

/**
 * Created by foree on 16-7-20.
 */
public class ItemListFragment extends Fragment implements StreamReceiverService.StreamCallBack{
    private static final String KEY_FEEDID = "feedId";
    private static final String TAG = ItemListFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ItemListAdapter mAdapter;
    private AbsApiHelper mApiHelper, localApiHelper;
    private List<RssItem> itemList;
    private ActionMode actionMode;
    RssDao rssDao;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout)inflater.inflate(R.layout.fragment_itemlist, container, false);

        AbsApiFactory absApiFactory = new ApiFactory();
        mApiHelper = absApiFactory.createApiHelper(FeedlyApiHelper.class);
        localApiHelper = absApiFactory.createApiHelper(LocalApiHelper.class);

        // database
        rssDao = new RssDao(getActivity());

        mRecyclerView = (RecyclerView) linearLayout.findViewById(R.id.rv_item_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));

        syncDate();

        return linearLayout;
    }

    private void initAdapter() {
        mAdapter = new ItemListAdapter(getActivity(), itemList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getActivity(), position + "", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), ArticleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("entryUrl", itemList.get(position).getUrl());
                bundle.putString("entryTitle", itemList.get(position).getTitle());
                rssDao.updateUnreadByEntryId(itemList.get(position).getEntryId(), false);
                intent.putExtras(bundle);
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
                itemList = data;
                initAdapter();
            }

            @Override
            public void onFail(String msg) {

            }
        });
    }
    @Override
    public void notifyUpdate() {
        syncDate();
    }

}
