package fi.livi.digitraffic.tie.service.v1.datex2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.helper.FileGetService;

@Component
public class Datex2WeightRestrictionsHttpClient {
    private final String url;
    private final FileGetService fileGetService;

    @Autowired
    public Datex2WeightRestrictionsHttpClient(@Value("${datex2.weight.restrictions.url:}") final String url, final FileGetService
        fileGetService) {
        this.url = url;
        this.fileGetService = fileGetService;
    }

    public String getWeightRestrictionsMessage() {
        return fileGetService.getFile(Datex2MessageType.WEIGHT_RESTRICTION.name(), url, String.class);
    }
}
