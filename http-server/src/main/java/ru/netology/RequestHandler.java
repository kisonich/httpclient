package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;

@FunctionalInterface
public interface RequestHandler {
    void handle(Request request, BufferedOutputStream out) throws IOException;
}