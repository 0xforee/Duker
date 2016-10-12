package org.foree.imageloader.core;

import android.util.Log;

import org.foree.imageloader.request.BitMapRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by foree on 16-10-8.
 * 加载图片请求队列
 */

public class RequestQueue {
    private static final String TAG = RequestQueue.class.getSimpleName();
    /**
     * 请求队列
     */
    private BlockingQueue<BitMapRequest> mQueue = new PriorityBlockingQueue<>();

    /**
     * 请求的序列号
     */
    private AtomicInteger mSerialNumGenerator = new AtomicInteger(0);

    private RequestDispatcher[] mDispatcher;

    private int threadCount = Runtime.getRuntime().availableProcessors() + 1;

    public RequestQueue(int count){
        threadCount = count;
    }

    public void add(BitMapRequest bitmaprequest) {
        if (!mQueue.contains(bitmaprequest)) {
            bitmaprequest.setSerialNum(this.generateSerialNumber());
            mQueue.add(bitmaprequest);
        } else {
            Log.d(TAG, "Already Contain This Request");
        }
    }
    
    public void start(){
        stop();
        startDispatcher();
    }

    private void startDispatcher(){
        mDispatcher = new RequestDispatcher[threadCount];
        for( int i=0; i < threadCount; i++){
            mDispatcher[i] = new RequestDispatcher(mQueue);
            mDispatcher[i].start();
        }
    }

    public void stop(){
        if( mDispatcher != null) {
            for (RequestDispatcher dispatcher : mDispatcher) {
                if (!dispatcher.isInterrupted()) {
                    dispatcher.interrupt();
                }
            }
        }
    }

    private int generateSerialNumber(){
        return mSerialNumGenerator.incrementAndGet();
    }
}
