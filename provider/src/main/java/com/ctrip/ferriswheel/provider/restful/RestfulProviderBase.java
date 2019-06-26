/*
 * MIT License
 *
 * Copyright (c) 2018 Ctrip.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.ctrip.ferriswheel.provider.restful;

import com.ctrip.ferriswheel.common.query.DataProvider;
import com.ctrip.ferriswheel.common.query.DataQuery;
import com.ctrip.ferriswheel.common.util.DataSet;
import com.ctrip.ferriswheel.common.variant.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author liuhaifeng
 */
public abstract class RestfulProviderBase implements DataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(RestfulProviderBase.class);

    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String DEFAULT_METHOD = HTTP_METHOD_GET;

    public static final String PARAM_URL = "url";
    public static final String PARAM_METHOD = "method";
    public static final String PARAM_HEADERS = "headers";
    public static final String PARAM_BODY = "body";

    public static final int DEFAULT_CONN_TIMEOUT_IN_MILLIS = 1000 * 30;
    public static final int DEFAULT_READ_TIMEOUT_IN_MILLIS = 1000 * 90;

    private volatile int connTimeoutInMillis = DEFAULT_CONN_TIMEOUT_IN_MILLIS;
    private volatile int readTimeoutInMillis = DEFAULT_READ_TIMEOUT_IN_MILLIS;

    // TODO use LRU cache instead
    // TODO cache should be expired after a certain duration.
    private transient LinkedHashMap<RestfulRequest, String> cachedResponse = new LinkedHashMap<>();

    @Override
    public DataSet execute(DataQuery query) throws IOException {
        String response = executeRestfulRequest(createRequest(query));
        return parseToDataSet(query, response);
    }

    /**
     * Overridable method.
     *
     * @param query
     * @return
     */
    protected RestfulRequest createRequest(DataQuery query) {
        String url = query.getString(PARAM_URL);
        String method = query.getString(PARAM_METHOD);
        String body = query.getString(PARAM_BODY);

        if (method == null || method.isEmpty()) {
            method = DEFAULT_METHOD;
        } else {
            method = method.toUpperCase();
        }

        Variant headersVar = query.getParam(PARAM_HEADERS);
        List<RequestHeader> headerList = new ArrayList<>(headersVar == null ? 0 : headersVar.itemCount());
        for (int i = 0; headersVar != null && i < headersVar.itemCount(); i++) {
            String header = headersVar.item(i).strValue();
            int pos = header.indexOf(':');
            String name = header.substring(0, pos).trim();
            String value = header.substring(pos + 1).trim();
            headerList.add(new RequestHeader(name, value));
        }
        return new RestfulRequest(url, method, headerList, body);
    }

    protected String executeRestfulRequest(RestfulRequest request) throws IOException {
        String response = getCachedResponse(request);
        if (response != null) {
            LOG.info("Serve cached response of request: " + request.getUrl());
            return response;
        }

        LOG.info("Executing RESTful request: " + request.getUrl());
        long start = System.currentTimeMillis();
        try {
            return executeRestfulRequestWithoutCache(request);
        } finally {
            LOG.info("Execution done within {} milliseconds.", System.currentTimeMillis() - start);
        }

    }

    protected String executeRestfulRequestWithoutCache(RestfulRequest request) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(request.getUrl()).openConnection();
        conn.setRequestMethod(request.getMethod());

        // headers
        for (RequestHeader header : request.getHeaders()) {
            conn.setRequestProperty(header.getName(), header.getValue());
        }

        // output/input flags
        if (request.getBody() != null) {
            conn.setDoOutput(true);
        }
        conn.setDoInput(true);

        // other options
        conn.setConnectTimeout(connTimeoutInMillis);
        conn.setReadTimeout(readTimeoutInMillis);

        // connect
        conn.connect();
        if (request.getBody() != null) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
                writer.write(request.getBody());
            }
        }

        // check response code
        int sc = conn.getResponseCode();
        if (sc < 200 || sc >= 300) {
            throw new IOException("Failed to proceed HTTP request, response code: " + sc);
        }

        // read data
        StringBuilder sb = new StringBuilder();
        char[] cbuf = new char[4096];
        int n;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            while ((n = reader.read(cbuf)) != -1) {
                sb.append(cbuf, 0, n);
            }
            String response = sb.toString();
            cacheResponseIfPossible(request, response);
            return response;
        }
    }

    protected String getCachedResponse(RestfulRequest request) {
        synchronized (cachedResponse) {
            return cachedResponse.get(request);
        }
    }

    protected void cacheResponseIfPossible(RestfulRequest request, String response) {
        if (!HTTP_METHOD_GET.equalsIgnoreCase(request.getMethod())) {
            return;
        }
        synchronized (cachedResponse) {
            while (cachedResponse.size() >= 10) {
                Iterator<Map.Entry<RestfulRequest, String>> it = cachedResponse.entrySet().iterator();
                it.next();
                it.remove();
            }
            cachedResponse.put(request, response);
        }
    }

    protected abstract DataSet parseToDataSet(DataQuery query, String response);

    public int getConnTimeoutInMillis() {
        return connTimeoutInMillis;
    }

    public void setConnTimeoutInMillis(int connTimeoutInMillis) {
        this.connTimeoutInMillis = connTimeoutInMillis;
    }

    public int getReadTimeoutInMillis() {
        return readTimeoutInMillis;
    }

    public void setReadTimeoutInMillis(int readTimeoutInMillis) {
        this.readTimeoutInMillis = readTimeoutInMillis;
    }

    public static class RestfulRequest {
        private String url;
        private String method;
        private List<RequestHeader> headers;
        private String body;

        public RestfulRequest(String url) {
            this(url, DEFAULT_METHOD, Collections.emptyList(), null);
        }

        public RestfulRequest(String url,
                              String method,
                              List<RequestHeader> headers,
                              String body) {
            this.url = url;
            this.method = method;
            this.headers = headers;
            this.body = body;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RestfulRequest that = (RestfulRequest) o;
            return Objects.equals(url, that.url) &&
                    Objects.equals(method, that.method) &&
                    Objects.equals(headers, that.headers) &&
                    Objects.equals(body, that.body);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, method, headers, body);
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public List<RequestHeader> getHeaders() {
            return headers;
        }

        public void setHeaders(List<RequestHeader> headers) {
            this.headers = headers;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

    public static class RequestHeader {
        private String name;
        private String value;

        public RequestHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RequestHeader that = (RequestHeader) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {

            return Objects.hash(name, value);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
