package org.foree.imageloader.loader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by foree on 16-9-30.
 * 管理加载器的注册与注销
 */

public class LoaderManager {

    private static LoaderManager mInstance;

    private Map<String, AbsLoader> mLoaderList;

    private final String SCHEMA_NET = "http";
    private final String SCHEMA_NET2 = "https";
    private final String SCHEMA_LOCAL = "file";

    private LoaderManager(){
        mLoaderList = new HashMap<>();
        registerLoader(SCHEMA_NET, new NetLoader());
        registerLoader(SCHEMA_NET2, new NetLoader());
        registerLoader(SCHEMA_LOCAL, new LocalLoader());
    }

    // singleton
    public static LoaderManager getInstance(){
        if( mInstance == null){
           synchronized (LoaderManager.class){
               if( mInstance == null){
                   mInstance = new LoaderManager();
               }
           }
        }

        return mInstance;
    }
    /**
     * 注册图片加载器
     */
    public void registerLoader(String schema, AbsLoader loader){
        mLoaderList.put(schema, loader);
    }

    /**
     * 取消注册图片加载器
     */
    public void unRegisterLoader(String schema){
        mLoaderList.remove(schema);
    }

    /**
     * 根据Uri选择合适的图片加载器
     */
    public AbsLoader getLoader(String Uri){

        // 提取schema
        String schema = Uri.split("://")[0];

        if( mLoaderList.containsKey(schema))
            return mLoaderList.get(schema);
        else
            return new NullLoader();

    }

}
