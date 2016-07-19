package org.foree.duker.api;



/**
 * Created by foree on 16-7-15.
 */
public abstract class AbsApiHelper {
    public abstract void getCategoriesList(String token);
    public abstract void getSubscriptions(String token);
    public abstract void getStream(String token);
}
