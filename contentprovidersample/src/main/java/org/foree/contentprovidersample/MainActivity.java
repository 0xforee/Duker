package org.foree.contentprovidersample;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // 切换浏览模式
    RecyclerView recyclerView;
    ImageAdapter imageAdapter;
    Button bt_item;
    ContentResolver contentResolver;
    Uri entryUri = Uri.parse("content://org.foree.contentprovidersample/entry");
    List<String> itemTitles;
    RssObserver rssObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        bt_item = (Button) findViewById(R.id.bn_item);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(imageAdapter = new ImageAdapter());

        bt_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // insert data
                insert();
            }
        });

        contentResolver = getContentResolver();

        itemTitles = new ArrayList<>();

        rssObserver = new RssObserver(mHandler);

        contentResolver.registerContentObserver(entryUri, true, rssObserver);
    }

    @Override
    protected void onDestroy() {
        contentResolver.unregisterContentObserver(rssObserver);
    }

    private android.os.Handler mHandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){

            }
        }
    };

    class RssObserver extends ContentObserver{

        public RssObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            queryAll();
            imageAdapter.notifyDataSetChanged();
        }

        private void queryAll(){
            Cursor cursor = contentResolver.query(entryUri,null,null,null,null,null);
            if ( cursor != null){
                itemTitles.clear();
                while(cursor.moveToNext()){
                    itemTitles.add(cursor.getString(cursor.getColumnIndex("title")));
                }

                cursor.close();
            }
        }
    }

    private void insert() {
        ContentValues values = new ContentValues();
        values.put("title", "hahahahah");
        contentResolver.insert(entryUri, values);
    }


    class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ImageHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.image_holder_linearlayout, parent, false));
        }

        @Override
        public void onBindViewHolder(ImageHolder holder, int position) {
            //set Text
            holder.textView.setText(itemTitles.get(position));
        }

        @Override
        public int getItemCount() {
            return itemTitles.size();
        }

    }

    class ImageHolder extends RecyclerView.ViewHolder{
        private TextView textView;

        public ImageHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv_item);
        }
    }
}
