package com.lzy.okgo.cache.policy;

import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.HttpRequest;

import okhttp3.Call;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2017/5/25
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class FirstCacheRequestPolicy<T> extends BaseCachePolicy<T> {
    public FirstCacheRequestPolicy(HttpRequest<T, ? extends HttpRequest> request) {
        super(request);
    }

    @Override
    public void onSuccess(final Response<T> success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallback.onSuccess(success.body(), success);
                mCallback.onFinish(success);
            }
        });
    }

    @Override
    public void onError(final Response<T> error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallback.onError(error.getException(), error);
                mCallback.onFinish(error);
            }
        });
    }

    @Override
    public Response<T> requestSync(CacheEntity<T> cacheEntity, Call rawCall) {
        //同步请求，不能返回两次，只返回正确的数据
        Response<T> response;
        if (cacheEntity != null) {
            response = Response.success(true, cacheEntity.getData(), rawCall, null);
        }
        response = requestNetworkSync();
        if (!response.isSuccessful() && cacheEntity != null) {
            response = Response.success(true, cacheEntity.getData(), rawCall, response.getRawResponse());
        }
        return response;
    }

    @Override
    public void requestAsync(CacheEntity<T> cacheEntity, Call rawCall, Callback<T> callback) {
        mCallback = callback;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallback.onStart(httpRequest);
            }
        });
        if (cacheEntity != null) {
            final Response<T> success = Response.success(true, cacheEntity.getData(), rawCall, null);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCallback.onCacheSuccess(success.body(), success);
                }
            });
        }
        requestNetworkAsync();
    }
}