package org.foree.duker.api;

/**
 * Created by foree on 16-7-15.
 */
public class ApiFactory extends AbsApiFactory{
    @Override
    public <T extends AbsApiHelper> T createApiHelper(Class<T> clz) {
        AbsApiHelper apiHelper = null;
        try {
            apiHelper = (AbsApiHelper)Class.forName(clz.getName()).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) apiHelper;
    }
}
