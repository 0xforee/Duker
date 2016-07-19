package org.foree.duker.api;

/**
 * Created by foree on 16-7-15.
 */
public abstract class AbsApiFactory {
    public abstract <T extends AbsApiHelper> T createApiHelper(Class<T> apiHelper);
}
