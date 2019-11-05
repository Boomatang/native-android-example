package com.m.androidNativeApp;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.ResponseField;
import com.apollographql.apollo.cache.normalized.CacheKey;
import com.apollographql.apollo.cache.normalized.CacheKeyResolver;
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy;
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class Client {

    private static final String SERVER_URL = "https://ionic-showcase-server-customer-a-shar-b4c5.apps.ire-85ac.open.redhat.com/graphql";

    public static final ApolloClient apolloClient = setupApollo();

    public static NormalizedCacheFactory normalizedCacheFactory(){
        return new LruNormalizedCacheFactory(EvictionPolicy.builder().maxSizeBytes(10 * 1024).build());
    }

    public static CacheKeyResolver cacheKeyResolver(){
        return new CacheKeyResolver() {
            @Override
            public CacheKey fromFieldRecordSet(ResponseField field, Map<String, Object> recordSet) {
                if (recordSet.containsKey("id")) {
                    String id = (String) recordSet.get("id");
                    return CacheKey.from(id);
                }
                return CacheKey.NO_KEY;
            }

            @Override
            public CacheKey fromFieldArguments(ResponseField field, Operation.Variables variables) {
                return CacheKey.NO_KEY;
            }
        };
    }

    private static ApolloClient setupApollo() {

        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .build();

        Map<String, Object> connectionParams = new HashMap<>();

        return ApolloClient.builder()
                .serverUrl(SERVER_URL)
                .normalizedCache(normalizedCacheFactory(), cacheKeyResolver())
                .subscriptionConnectionParams(connectionParams)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(SERVER_URL, okHttpClient))
                .okHttpClient(okHttpClient)
                .build();
    }
}
