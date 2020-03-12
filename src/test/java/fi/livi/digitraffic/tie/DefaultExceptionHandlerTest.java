package fi.livi.digitraffic.tie;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_DATA_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import fi.livi.digitraffic.tie.dto.v1.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.v1.TmsDataService;
import fi.livi.digitraffic.tie.service.BadRequestException;

@TestPropertySource(properties = {
    "road.datasource.hikari.maximum-pool-size=1",
})
public class DefaultExceptionHandlerTest extends AbstractRestWebTest {
    @MockBean
    private TmsDataService tmsDataService;

    @MockBean
    private Logger exceptionHandlerLogger;

    @Before
    public void setUp() {
        when(exceptionHandlerLogger.isErrorEnabled()).thenReturn(true);
        when(exceptionHandlerLogger.isInfoEnabled()).thenReturn(true);
    }

    private ResultActions performQuery() throws Exception {
        return mockMvc.perform(get(API_V1_BASE_PATH + API_DATA_PART_PATH + TMS_DATA_PATH + "/1"));
    }

    @Test
    public void ok() throws Exception {
        when(tmsDataService.findPublishableTmsData(anyLong())).thenReturn(new TmsRootDataObjectDto(ZonedDateTime.now()));

        performQuery()
            .andExpect(status().isOk());

    }

    private enum LogMode {
        ERROR, INFO, NONE
    }

    private <T extends Throwable> void testException(final Class<T> clazz, final int statuscode, final LogMode logMode) throws Exception {
        when(tmsDataService.findPublishableTmsData(anyLong())).thenThrow(mock(clazz));

        performQuery()
            .andExpect(status().is(statuscode));

        switch (logMode) {
            case ERROR:
                verify(exceptionHandlerLogger).error(anyString(), any(Throwable.class));
                verify(exceptionHandlerLogger, times(0)).info(anyString(), any(Throwable.class));
                break;
            case INFO:
                verify(exceptionHandlerLogger).info(anyString(), any(Throwable.class));
                verify(exceptionHandlerLogger, times(0)).error(anyString(), any(Throwable.class));
                break;
            case NONE:
                verify(exceptionHandlerLogger, times(0)).error(anyString(), any(Throwable.class));
                verify(exceptionHandlerLogger, times(0)).info(anyString(), any(Throwable.class));
        }
    }

    @Test
    public void notFoundException() throws Exception {
        testException(ObjectNotFoundException.class, 404, LogMode.NONE);
    }

    @Test
    public void badRequestException() throws Exception {
        testException(BadRequestException.class, 400, LogMode.INFO);
    }

    @Test
    public void resourceAccessException() throws Exception {
        testException(ResourceAccessException.class, 500, LogMode.ERROR);
    }

    @Test
    public void illegalArgumentException() throws Exception {
        testException(IllegalArgumentException.class, 400, LogMode.INFO);
    }

    @Test
    public void constraintViolationException() throws Exception {
        testException(ConstraintViolationException.class, 400, LogMode.ERROR);
    }

    @Test
    public void httpMessageNotWritableException() throws Exception {
        testException(HttpMessageNotWritableException.class, 500, LogMode.INFO);
    }

    @Test
    public void httpMessageNotReadableException() throws Exception {
        testException(HttpMessageNotReadableException.class, 400, LogMode.INFO);
    }

    @Test
    public void methodArgumentTypeMismatchException() throws Exception {
        testException(MethodArgumentTypeMismatchException.class, 400, LogMode.INFO);
    }
}
