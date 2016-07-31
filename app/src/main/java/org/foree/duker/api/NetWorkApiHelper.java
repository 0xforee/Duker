package org.foree.duker.api;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.foree.duker.base.BaseApplication;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by foree on 16-7-18.
 */
public class NetWorkApiHelper {
    private static NetWorkApiHelper INSTANCE = null;
    RequestQueue queue;
    private int mStatusCode;

    public static NetWorkApiHelper newInstance() {

        if ( INSTANCE == null)
            INSTANCE = new NetWorkApiHelper();

        return INSTANCE;
    }

    public int getStatusCode(){
        return mStatusCode;
    }
    public NetWorkApiHelper(){
        queue = Volley.newRequestQueue(BaseApplication.getInstance().getApplicationContext());
    }

    public void getRequest(String requestUrl, final Map<String, String> headers, Response.Listener<String> listener, Response.ErrorListener errorListener){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl, listener, errorListener){
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return headers;
        }
        };
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
    }

    public void postRequest(String requestUrl, final JSONObject params, final Map<String, String> headers, Response.Listener<String> listener, Response.ErrorListener errorListener){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl, listener, errorListener){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                return params.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };

        queue.add(stringRequest);
    }


}
