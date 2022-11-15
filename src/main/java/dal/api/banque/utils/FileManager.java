package dal.api.banque.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

public class FileManager {
    
    public static String getFileContent(String filename) {
        String json;
        try {
            json = IOUtils.toString(new ClassPathResource("static/"+filename).getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading static data");
        }
        return json;
    }
}
