package org.foree.duker.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;

import org.foree.duker.R;
import org.foree.duker.api.AbsApiFactory;
import org.foree.duker.api.AbsApiHelper;
import org.foree.duker.api.ApiFactory;
import org.foree.duker.api.FeedlyApiHelper;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssItem;

import java.util.List;

/**
 * Created by foree on 16-7-20.
 */
public class ItemListFragment extends Fragment{
    private static final String KEY_FEEDID = "feedId";
    private static final String TAG = ItemListFragment.class.getSimpleName();

    private Drawer result;
    private RecyclerView mRecyclerView;
    private ItemListAdapter mAdapter;
    private AbsApiHelper mApiHelper;
    private List<RssItem> itemList;

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

        mRecyclerView = (RecyclerView) linearLayout.findViewById(R.id.rv_item_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));

        // getItemList
        AbsApiFactory absApiFactory = new ApiFactory();
        mApiHelper = absApiFactory.createApiHelper(FeedlyApiHelper.class);

        mApiHelper.getStream("", getArguments().getString(KEY_FEEDID), new NetCallback<RssItem>() {
            @Override
            public void onSuccess(List<RssItem> data) {
                itemList = data;
                mAdapter = new ItemListAdapter(getActivity(),itemList);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onFail(String msg) {

            }
        });
        return linearLayout;
    }

    class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.MyViewHolder>{
        private LayoutInflater mLayoutInflater;
        private List<RssItem> mItemList;

        public ItemListAdapter(Context context, List<RssItem> itemList){
            mLayoutInflater = LayoutInflater.from(context);
            mItemList = itemList;
        }

        @Override
        public ItemListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(mLayoutInflater.inflate(R.layout.item_list_holder, parent, false));

            return holder;
        }

        @Override
        public void onBindViewHolder(ItemListAdapter.MyViewHolder holder, int position) {
            holder.tvTitle.setText(mItemList.get(position).getTitle());
            if (mItemList.get(position).getSummary() != null) {
                holder.tvSummary.setVisibility(View.VISIBLE);
                holder.tvSummary.setText(mItemList.get(position).getSummary());
            }
            holder.tvPublished.setText(mItemList.get(position).getPubDate().toString());
        }

        @Override
        public int getItemCount() {
            return mItemList.size();
        }


        class MyViewHolder extends RecyclerView.ViewHolder{
            TextView tvTitle;
            TextView tvSummary;
            TextView tvPublished;

            public MyViewHolder(View view){
                super(view);
                tvTitle = (TextView)view.findViewById(R.id.tv_item_title);
                tvSummary = (TextView)view.findViewById(R.id.tv_item_summary);
                tvPublished = (TextView)view.findViewById(R.id.tv_item_published);
            }
        }
    }

    class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

        public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

        private Drawable mDivider;

        private int mOrientation;

        public DividerItemDecoration(Context context, int orientation) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
                throw new IllegalArgumentException("invalid orientation");
            }
            mOrientation = orientation;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent) {
            Log.v("recyclerview", "onDraw()");

            if (mOrientation == VERTICAL_LIST) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }

        }


        public void drawVertical(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                android.support.v7.widget.RecyclerView v = new android.support.v7.widget.RecyclerView(parent.getContext());
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            }
        }
    }
}
