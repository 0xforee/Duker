package org.foree.duker.net;

import java.util.List;

/**
 * Created by foree on 16-7-19.
 * 数据回调函数
 */
public interface NetCallback<T> {
    void onSuccess(List<T> data);
    void onFail(String msg);
}
