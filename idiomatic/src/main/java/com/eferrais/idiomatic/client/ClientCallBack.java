package com.eferrais.idiomatic.client;

/**
 * Created by elodieferrais on 2/27/14.
 */
public interface ClientCallBack<T> {
    public void onResult(T result, Error error);
}
