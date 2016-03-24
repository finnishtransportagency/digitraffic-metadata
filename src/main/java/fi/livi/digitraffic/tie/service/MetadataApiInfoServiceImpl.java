package fi.livi.digitraffic.tie.service;

import fi.livi.digitraffic.tie.documentation.MetadataApiInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.service.ApiInfo;

@Service
public class MetadataApiInfoServiceImpl implements MetadataApiInfoService {

    private static final Logger LOG = Logger.getLogger(MetadataApiInfoServiceImpl.class);

    private final MessageService messageService;

    private final BuildVersionService buildVersionService;

    @Autowired
    public MetadataApiInfoServiceImpl(final MessageService messageService,
                                      final BuildVersionService buildVersionService) {
        this.messageService = messageService;
        this.buildVersionService = buildVersionService;
    }

    @Override
    public ApiInfo getApiInfo() {
        return new MetadataApiInfo(messageService, buildVersionService);
    }
}
