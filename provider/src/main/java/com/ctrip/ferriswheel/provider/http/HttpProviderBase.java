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

package com.ctrip.ferriswheel.provider.http;

import com.ctrip.ferriswheel.common.query.*;
import com.ctrip.ferriswheel.common.util.DataSet;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.provider.DataProviderSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author liuhaifeng
 */
public abstract class HttpProviderBase extends DataProviderSupport implements DataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(HttpProviderBase.class);

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

    private HttpClient httpClient;

    @Override
    protected QueryResult doExecute(DataQuery query) throws IOException {
        HttpResponse response = executeRequest(createRequest(query));
        DataSet dataSet = parseToDataSet(query, response);
        CacheHint cacheHint = createCacheHint(query, response);
        return new ImmutableQueryResult(ErrorCodes.OK, "Ok", cacheHint, dataSet);
    }

    /**
     * Overridable method.
     *
     * @param query
     * @return
     */
    protected HttpRequest createRequest(DataQuery query) {
        String url = query.getString(PARAM_URL);
        String method = query.getString(PARAM_METHOD);
        String body = query.getString(PARAM_BODY);

        if (method == null || method.isEmpty()) {
            method = DEFAULT_METHOD;
        } else {
            method = method.toUpperCase();
        }

        SimpleHttpRequest request = new SimpleHttpRequest(url, method, body);

        Variant headersVar = query.getParam(PARAM_HEADERS);
        for (int i = 0; headersVar != null && i < headersVar.itemCount(); i++) {
            String header = headersVar.item(i).strValue();
            int pos = header.indexOf(':');
            String name = header.substring(0, pos).trim();
            String value = header.substring(pos + 1).trim();
            request.addHeader(name, value);
        }

        return request;
    }

    protected HttpResponse executeRequest(HttpRequest request) throws IOException {
        LOG.info("Executing HTTP request: " + request.getUrl());
        long start = System.currentTimeMillis();
        try {
            return doExecuteRequest(request);
        } finally {
            LOG.info("Execution done within {} milliseconds.", System.currentTimeMillis() - start);
        }

    }

    protected HttpResponse doExecuteRequest(HttpRequest request) throws IOException {
        HttpClient client = getHttpClient();
        if (client != null) {
            return client.execute(request);
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(request.getUrl()).openConnection();
        conn.setRequestMethod(request.getMethod());

        // headers
        for (Map.Entry<String, List<String>> header : request.getHeaders().entrySet()) {
            String name = header.getKey();
            for (String value : header.getValue()) {
                conn.addRequestProperty(name, value);
            }
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

        return new SimpleHttpResponse(conn);
    }

    protected abstract DataSet parseToDataSet(DataQuery query, HttpResponse response);

    protected CacheHint createCacheHint(DataQuery query, HttpResponse response) {
        return ImmutableCacheHint.newBuilder().build(); // default cache hint, maxAge=0
    }

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

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public static class SimpleHttpRequest implements HttpRequest {
        private String url;
        private String method;
        private Map<String, List<String>> headers;
        private String body;

        public SimpleHttpRequest(String url) {
            this(url, DEFAULT_METHOD, null);
        }

        public SimpleHttpRequest(String url, String method, String body) {
            this.url = url;
            this.method = method;
            this.body = body;
        }

        public void addHeader(String name, String value) {
            if (headers == null) {
                this.headers = new LinkedHashMap<>();
            }
            name = name.toLowerCase();
            List<String> values = headers.get(name);
            if (values == null) {
                values = new LinkedList<>();
                headers.put(name, values);
            }
            values.add(value);
        }

        public void setHeader(String name, String value) {
            if (headers == null) {
                this.headers = new LinkedHashMap<>();
            }
            name = name.toLowerCase();
            List<String> values = new LinkedList<>();
            values.add(value);
            headers.put(name, values);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleHttpRequest that = (SimpleHttpRequest) o;
            return Objects.equals(url, that.url) &&
                    Objects.equals(method, that.method) &&
                    Objects.equals(headers, that.headers) &&
                    Objects.equals(body, that.body);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, method, headers, body);
        }

        @Override
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            return Collections.unmodifiableMap(headers);
        }

        @Override
        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

    public static class SimpleHttpResponse implements HttpResponse {
        private HttpURLConnection conn;
        private transient String bodyString;

        SimpleHttpResponse(HttpURLConnection conn) {
            this.conn = conn;
        }

        @Override
        public int getResponseCode() {
            try {
                return conn.getResponseCode();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getResponseMessage() {
            try {
                return conn.getResponseMessage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getHeader(String name) {
            return conn.getHeaderField(name);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return conn.getInputStream();
        }

        @Override
        public String getBodyAsString() throws IOException {
            if (bodyString != null) {
                return bodyString;
            }

            StringBuilder sb = new StringBuilder();
            char[] cbuf = new char[4096];
            int n;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                while ((n = reader.read(cbuf)) != -1) {
                    sb.append(cbuf, 0, n);
                }
                bodyString = sb.toString();
            }

            return bodyString;
        }
    }

}
