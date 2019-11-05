package com.m.androidNativeApp;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class Client {

    private static final String SERVER_URL = "https://ionic-showcase-server-customer-a-shar-b4c5.apps.ire-85ac.open.redhat.com/graphql";

    public static final ApolloClient apolloClient = setupApollo();

    private static ApolloClient setupApollo() {

        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .build();

        Map<String, Object> connectionParams = new HashMap<>();

        return ApolloClient.builder()
                .serverUrl(SERVER_URL)
                .subscriptionConnectionParams(connectionParams)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(SERVER_URL, okHttpClient))
                .okHttpClient(okHttpClient)
                .build();
    }
}
