package fi.livi.digitraffic.tie;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_PATH;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import fi.livi.digitraffic.tie.service.BuildVersionService;

public class SwaggerUiWebTest extends AbstractRestWebTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private BuildVersionService versionService;

    private final MediaType restContentType = MediaType.APPLICATION_JSON;


    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testSwaggerHome() throws Exception {
        this.mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Swagger UI</title>")));
    }

    @Test
    public void testSwaggerRestApi() throws Exception {
        mockMvc.perform(get("/v2/api-docs?group=metadata-api"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(restContentType))
                .andExpect(jsonPath("$.swagger", is("2.0")))
                .andExpect(jsonPath("$.info.version", is(versionService.getAppFullVersion())))
                .andExpect(jsonPath("$.paths." + API_V1_BASE_PATH + API_METADATA_PART_PATH + TMS_STATIONS_PATH, anything()));
    }
}
