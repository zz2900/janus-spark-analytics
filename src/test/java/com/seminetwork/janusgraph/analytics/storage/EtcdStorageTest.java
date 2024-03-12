package com.seminetwork.janusgraph.analytics.storage;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.seminetwork.janusgraph.analytics.model.Result;
import com.seminetwork.janusgraph.analytics.model.Status;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class EtcdStorageTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Test
    public void store() throws StorageException {
        int port = wireMockRule.port();
        String host = "localhost";

        EtcdStorageConfiguration config = new EtcdStorageConfiguration();
        config.setHost(host);
        config.setPort(port);

        stubFor(any(urlPathEqualTo("/v3alpha/kv/put"))
                .willReturn(aResponse().withStatus(200)));


        Result result = new Result("some-id", Status.INPROGRESS, null, "some-query");

        final Client client = new JerseyClientBuilder().build();
        Storage storage = new EtcdStorage(client, config);
        storage.store(result);

        String expectedBody = "{" +
                "  \"key\" : \"L3dlYXZpYXRlL2phbnVzZ3JhcGgtY29ubmVjdG9yL2FuYWx5dGljcy1jYWNoZS9zb21lLWlk\"," +
                "  \"value\" : \"eyJpZCI6InNvbWUtaWQiLCJzdGF0dXMiOiJJTlBST0dSRVNTIiwicmVzdWx0IjpudWxsLCJvcmlnaW5hbFF1ZXJ5Ijoic29tZS1xdWVyeSJ9\"" +
                "}";

        verify(postRequestedFor(urlPathEqualTo("/v3alpha/kv/put"))
                .withRequestBody(equalToJson(expectedBody )));

    }
}
