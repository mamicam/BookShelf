package com.example.bookshelf.controller;

import com.example.bookshelf.storage.BookStorage;
import com.example.bookshelf.storage.impl.PostgresBookStorageImpl;
import com.example.bookshelf.type.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.iki.elonen.NanoHTTPD;

import java.util.List;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.Response.Status.*;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class BookShelfController {
    private final static String BOOK_ID_PARAM_NAME = "bookId";

    private BookStorage bookStorage = new PostgresBookStorageImpl();

    public NanoHTTPD.Response serveGetBookRequest(NanoHTTPD.IHTTPSession session) {
        Map<String, List<String>> requestParameters = session.getParameters();
        if (requestParameters.containsKey(BOOK_ID_PARAM_NAME)) {
            List<String> bookIdParams = requestParameters.get(BOOK_ID_PARAM_NAME);
            String bookIdParam = bookIdParams.get(0);
            long bookId = 0;
            try {
                bookId = Long.parseLong(bookIdParam);
            } catch (NumberFormatException nfe) {
                System.err.println("Error during convert request param: \n" + nfe);
                return newFixedLengthResponse(BAD_REQUEST, "text/plain", "Request param 'bookId' have to be a number");
            }

            Book book = bookStorage.getBook(bookId);
            if (book != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String response = objectMapper.writeValueAsString(book);
                    return newFixedLengthResponse(OK, "application/json", response);
                } catch (JsonProcessingException e) {
                    System.err.println("Error durig process request: \n" + e);
                    return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error can't read all books");
                }
            }
            return newFixedLengthResponse(NOT_FOUND, "application/json", "");
        }
        return newFixedLengthResponse(BAD_REQUEST, "text/plain", "Uncorrected request params");
    }

    public NanoHTTPD.Response serveGetBooksResponse(NanoHTTPD.IHTTPSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        String response = "";
        try {
            response = objectMapper.writeValueAsString(bookStorage.getAllBooks());
        } catch (JsonProcessingException e) {
            System.err.println("Error during process request: \n" + e);
            return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error can't read all book");
        }
        return newFixedLengthResponse(OK, "application/json", response);
    }

    public NanoHTTPD.Response serveAddBookRequest(NanoHTTPD.IHTTPSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        long randomBookId = System.currentTimeMillis();

        String lengthHeader = session.getHeaders().get("content-length");
        int contentLength = Integer.parseInt(lengthHeader);
        byte[] buffer = new byte[contentLength];

        try {
            session.getInputStream().read(buffer, 0, contentLength);
            String requestBody = new String(buffer).trim();
            Book requestBook = objectMapper.readValue(requestBody, Book.class);
            requestBook.setId(randomBookId);

            bookStorage.addBook(requestBook);

        } catch (Exception e) {
            System.err.println("Error during process request: \n" + e);
            return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error, book hasn't be added.");
        }
        return newFixedLengthResponse(OK, "text/plain", "Book has been successfully added, id=" + randomBookId);
    }
}