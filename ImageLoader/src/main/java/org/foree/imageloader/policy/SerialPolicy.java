package org.foree.imageloader.policy;

import org.foree.imageloader.request.BitMapRequest;

/**
 * Created by foree on 16-10-8.
 */

public class SerialPolicy implements LoadPolicy {
    @Override
    public int compare(BitMapRequest request1, BitMapRequest request2) {
        return request1.getSerialNum() - request2.getSerialNum();
    }
}
