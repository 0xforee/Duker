package org.foree.imageloader.core;

import org.foree.imageloader.loader.AbsLoader;
import org.foree.imageloader.loader.LoaderManager;
import org.foree.imageloader.request.BitMapRequest;

import java.util.concurrent.BlockingQueue;

/**
 * Created by foree on 16-10-8.
 * 加载图片请求分发类
 */

public class RequestDispatcher extends Thread{

    private BlockingQueue<BitMapRequest> mQueue;

    public RequestDispatcher(BlockingQueue<BitMapRequest> requestQueue){
        mQueue = requestQueue;
    }

    @Override
    public void run() {
        super.run();
        try {
            while(!this.isInterrupted()){
                BitMapRequest request = mQueue.take();
                final AbsLoader loader = LoaderManager.getInstance().getLoader(request.getImageUri());
                loader.loadImage(request);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
