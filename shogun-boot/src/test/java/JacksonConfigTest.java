import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.terrestris.shogun.boot.config.ApplicationConfig;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ApplicationConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class JacksonConfigTest {

    @Value("${shogun.srid}")
    private int srid;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void assertThatObjectMapperHasJtsModuleRegistered() {
        var registeredModules = objectMapper.getRegisteredModuleIds();
        boolean hasJtsModuleRegistered = false;
        for (Object module : registeredModules) {
            if (StringUtils.equalsIgnoreCase((CharSequence) module, "com.bedatadriven.jackson.datatype.jts.JtsModule")) {
                hasJtsModuleRegistered = true;
            }
        }
        Assertions.assertTrue(hasJtsModuleRegistered, "JTS Module is not registrered in ObjectMapper");
    }

    @Test
    public void assertThatObjectMapperHasCorrectlyConfiguredJtsModule() throws JsonProcessingException {
        String LINESTRING_25832 = "{" +
            "\"type\": \"LineString\"," +
            "\"coordinates\": [" +
                "[" +
                    "711957.369742162," +
                    "5637657.304058334" +
                "]," +
                "[" +
                    "711979.3599907478," +
                    "5637629.050147795" +
                "]," +
                "[" +
                    "712000.5099907471," +
                    "5637596.860147787" +
                "]" +
            "]" +
        "}";
        LineString parsedLineString = objectMapper.readValue(LINESTRING_25832, LineString.class);
        Assertions.assertEquals(parsedLineString.getSRID(), srid, "Coordinate reference system of parsed " +
            "geometry does not match the one configured in JTS Module");
    }

}
