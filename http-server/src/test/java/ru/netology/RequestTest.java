package ru.netology;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class RequestTest {

    @Test
    public void testQueryParamParsing() {
        Request request = new Request("GET", "/test?param1=value1&param2=value2&param1=value3",
                List.of("Content-Type: text/plain"), "");

        assertEquals("/test", request.getPath());
        assertEquals("param1=value1&param2=value2&param1=value3", request.getQueryString());

        Optional<String> param1 = request.getQueryParam("param1");
        assertTrue(param1.isPresent());
        assertEquals("value1", param1.get());

        List<String> param1All = request.getQueryParams("param1");
        assertEquals(2, param1All.size());
        assertTrue(param1All.contains("value1"));
        assertTrue(param1All.contains("value3"));

        assertEquals(2, request.getQueryParams().size());
    }

    @Test
    public void testNoQueryParams() {
        Request request = new Request("GET", "/test", List.of(), "");

        assertEquals("/test", request.getPath());
        assertTrue(request.getQueryString().isEmpty());
        assertFalse(request.getQueryParam("any").isPresent());
        assertTrue(request.getQueryParams().isEmpty());
    }

    @Test
    public void testMultipleSameParams() {
        Request request = new Request("GET", "/?color=red&color=blue&color=green", List.of(), "");

        List<String> colors = request.getQueryParams("color");
        assertEquals(3, colors.size());
        assertTrue(colors.contains("red"));
        assertTrue(colors.contains("blue"));
        assertTrue(colors.contains("green"));
    }

    @Test
    public void testSpecialCharacters() {
        Request request = new Request("GET", "/search?q=hello+world&page=1", List.of(), "");

        Optional<String> query = request.getQueryParam("q");
        assertTrue(query.isPresent());
        assertEquals("hello world", query.get());

        Optional<String> page = request.getQueryParam("page");
        assertTrue(page.isPresent());
        assertEquals("1", page.get());
    }

    @Test
    public void testEmptyValues() {
        Request request = new Request("GET", "/test?empty=&normal=value", List.of(), "");

        Optional<String> empty = request.getQueryParam("empty");
        assertTrue(empty.isPresent());
        assertEquals("", empty.get());

        Optional<String> normal = request.getQueryParam("normal");
        assertTrue(normal.isPresent());
        assertEquals("value", normal.get());
    }
}