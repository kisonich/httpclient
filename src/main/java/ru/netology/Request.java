package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Request {
    private final String method;
    private final String path;
    private final String queryString;
    private final Map<String, List<String>> queryParams;
    private final List<String> headers;
    private final String body;

    public Request(String method, String path, List<String> headers, String body) {
        this.method = method;
        this.headers = headers != null ? headers : Collections.emptyList();
        this.body = body != null ? body : "";

        // Разделяем путь и query string
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1) {
            this.path = path.substring(0, queryIndex);
            this.queryString = path.substring(queryIndex + 1);
        } else {
            this.path = path;
            this.queryString = "";
        }

        this.queryParams = parseQueryParams();
    }

    private Map<String, List<String>> parseQueryParams() {
        if (queryString == null || queryString.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<NameValuePair> params = URLEncodedUtils.parse("?" + queryString, StandardCharsets.UTF_8);

            return params.stream()
                    .collect(Collectors.groupingBy(
                            NameValuePair::getName,
                            Collectors.mapping(NameValuePair::getValue, Collectors.toList())
                    ));
        } catch (Exception e) {
            System.err.println("Error parsing query parameters: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Optional<String> getQueryParam(String name) {
        if (name == null) {
            return Optional.empty();
        }
        List<String> values = queryParams.get(name);
        if (values != null && !values.isEmpty()) {
            return Optional.of(values.get(0));
        }
        return Optional.empty();
    }

    public List<String> getQueryParams(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        return queryParams.getOrDefault(name, Collections.emptyList());
    }

    public Map<String, List<String>> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public List<String> getHeaders() {
        return Collections.unmodifiableList(headers);
    }

    public String getBody() {
        return body;
    }

    public String getQueryString() {
        return queryString;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", queryParams=" + queryParams +
                '}';
    }
}