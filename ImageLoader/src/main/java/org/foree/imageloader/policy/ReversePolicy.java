package org.foree.imageloader.policy;

import org.foree.imageloader.request.BitMapRequest;

/**
 * Created by foree on 16-10-11.
 */

public class ReversePolicy implements LoadPolicy {
    @Override
    public int compare(BitMapRequest request1, BitMapRequest request2) {
        return request2.getSerialNum() - request1.getSerialNum();
    }
}
