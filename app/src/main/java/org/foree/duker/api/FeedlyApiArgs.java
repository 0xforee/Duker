package org.foree.duker.api;

/**
 * Created by foree on 16-8-9.
 */
public class FeedlyApiArgs {
    private int count;

    public void setCount(int count) {
        this.count = count;
    }

    public void setRanked(String ranked) {
        this.ranked = ranked;
    }

    public void setUnreadOnly(boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
    }

    public void setNewerThan(long newerThan) {
        this.newerThan = newerThan;
    }

    public void setContinuation(String continuation) {
        this.continuation = continuation;
    }

    private String ranked;
    private boolean unreadOnly;
    private long newerThan;
    private String continuation;

    public FeedlyApiArgs(){
        count = 20;
        ranked = "newest";
        unreadOnly = true;
        newerThan = 0;
        continuation = "";
    }

    public String generateUrl(String url){

        url = url + "&count=" + count +
                "&ranked=" + ranked +
                "&unreadOnly=" + unreadOnly;

        if ( newerThan != 0 && newerThan > 0 )
            url = url + "&newerThan=" + newerThan;
        if ( !continuation.isEmpty())
            url = url + "&continuation=" + continuation;

        return url;
    }
}
