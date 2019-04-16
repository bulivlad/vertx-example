package io.dotinc.async_rest_server.enums;

/**
 * @author vladclaudiubulimac on 2019-04-15.
 */
public enum Constants {

    FILE_READER_EVENT_ADDRESS("verticle.file-reader"),
    HTTP_CLIENT_EVENT_ADDRESS("verticle.http-client"),

    REQUESTS_FILE_PATH("src/main/resources/requests"),
    RESPONSES_FILE_PATH("src/main/resources/responses"),

    HTTP_PORT("http.port"),
    REQUEST_DELAY("request.delay"),
    CONFIG_LOCATION("src/main/resources/config.json");

    private String value;

    Constants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
