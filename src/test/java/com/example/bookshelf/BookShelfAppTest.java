package com.example.bookshelf;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class BookShelfAppTest {
    private static final String BOOK1 = "{\"title\":\"Java. Kompendium programisty\",\"author\":\"Herbert Shildt\"," +
            "\"pagesSum\":1152,\"yearOfPublished\":2019,\"publishingHouse\":\"Helion\"}";

    private static final String BOOK2 = "{\"title\":\"Python. Wprowadzenie.\",\"author\":\"Mark Lutz\"," +
            "\"pagesSum\":1184,\"yearOfPublished\":2017,\"publishingHouse\":\"Helion\"}";

    private static final int APP_PORT = 8090;

    private BookshelfApp bookshelfApp;

    @BeforeAll
    public static void beforeAll() {
        RestAssured.port = APP_PORT;
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        bookshelfApp = new BookshelfApp(APP_PORT);
    }

    @AfterEach
    public void afterEach() {
        bookshelfApp.stop();
    }

    @Test
    public void addMethod_correctBody_shouldReturnStatus200() throws Exception {
        with().body(BOOK1).when().post("/book/add").then().statusCode(200).body(startsWith("Book has been successfully added, id="));
    }

    @Test
    public void addMethod_fieldTypeMismatch_shouldReturnStatus500() {
        String bookWithFieldTypeMismatch = "{\"title\":\"Python. Wprowadzenie.\",\"author\":\"Mark Lutz\"," +
                "\"pagesSum\":\"1184 pages\",\"yearOfPublished\":2017,\"publishingHouse\":\"Helion\"}";

        with().body(bookWithFieldTypeMismatch).when().post("/book/add").then().statusCode(500);
    }

    @Test
    public void addMethod_unexpectedField_shouldReturnStatus500() {
        with().body("{\"numberOfChapters\":10}").when().post("/book/add").then().statusCode(500);
    }

    private long addBookAndGetId(String json) {
        String responseText = with().body(json)
                .when().post("/book/add")
                .then().statusCode(200).body(startsWith("Book has been successfully added, id="))
                .extract().body().asString();

        String idStr = responseText.substring(responseText.indexOf("=")+1);

        return Long.parseLong(idStr);
    }

    @Test
    public void getMethod_correctBookIdParam_shouldReturnStatus200() {
        long bookId1 = addBookAndGetId(BOOK1);
        long bookId2 = addBookAndGetId(BOOK2);

        with().param("bookId", bookId1)
                .when().get("/book/get")
                .then().statusCode(200)
                .body("id", equalTo(bookId1))
                .body("title", equalTo("Java. Kompendium programisty"))
                .body("author", equalTo("Herbert Shildt"))
                .body("pagesSum", equalTo(1152))
                .body("yearOfPublished", equalTo(2019))
                .body("publishingHouse", equalTo("Helion"));
    }

    @Test
    public void getMethod_noBookIdParameter_shouldReturnStatus400() {
        when().get("/book/get").then().statusCode(400).body(equalTo("Uncorrected request params"));
    }

    @Test
    public void getMethod_wrongTypeOfBookIdParameter_shouldReturnStatus400() {
        with().param("bookId", "abc").when().get("/book/get").then().statusCode(400).body(equalTo("Request param 'bookId' have to be a number"));
    }

    @Test
    public void getMethod_bookDoesNotExist_shouldReturnStatus404() {
        with().param("bookId", 12345).when().get("/book/get").then().statusCode(404);
    }

    @Test
    public void getAllMethod_1Book_shouldReturnStatus200() {
        long bookId1 = addBookAndGetId(BOOK1);

        when().get("/book/getAll")
                .then().statusCode(200)
                .body("", hasSize(1))
                .body("id", hasItem(bookId1))
                .body("title", hasItem("Java. Kompendium programisty"))
                .body("author", hasItem("Herbert Shildt"))
                .body("pagesSum", hasItem(1152))
                .body("yearOfPublished", hasItem(2019))
                .body("publishingHouse", hasItem("Helion"));
    }

    @Test
    public void getAllMethod_2Books_shouldReturnStatus200() {
        long bookId1 = addBookAndGetId(BOOK1);
        long bookId2 = addBookAndGetId(BOOK2);

        when().get("/book/getAll")
                .then().statusCode(200)
                .body("", hasSize(2))
                .body("id", hasItems(bookId1, bookId2))
                .body("title", hasItems("Java. Kompendium programisty", "Python. Wprowadzenie."))
                .body("author", hasItems("Herbert Shildt", "Mark Lutz"))
                .body("pagesSum", hasItems(1152, 1184))
                .body("yearOfPublished", hasItems(2019, 2017))
                .body("publishingHouse", hasItem("Helion"));
    }
}