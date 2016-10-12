package org.foree.imageloader.policy;

import org.foree.imageloader.request.BitMapRequest;

/**
 * Created by foree on 16-9-30.
 * 图片加载策略
 */

public interface LoadPolicy {
    int compare(BitMapRequest request1, BitMapRequest request2);
}
