package red.mvc.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonResources {
    private static final ObjectMapper objectMapper;
    private static final ObjectWriter objectWriter;

    public static ObjectWriter getObjectWriter(){
        return objectWriter;
    }
    public static ObjectMapper getObjectMapper(){
        return objectMapper;
    }

    static{
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
    }
}
