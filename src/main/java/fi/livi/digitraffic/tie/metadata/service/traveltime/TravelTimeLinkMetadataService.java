package fi.livi.digitraffic.tie.metadata.service.traveltime;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.metadata.converter.Link2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.LinkRepository;
import fi.livi.digitraffic.tie.metadata.geojson.traveltime.LinkFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.Link;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;

@Service
public class TravelTimeLinkMetadataService {

    private final LinkRepository linkRepository;
    private final Link2FeatureConverter link2FeatureConverter;
    private final DataStatusService dataStatusService;

    @Autowired
    public TravelTimeLinkMetadataService(final LinkRepository linkRepository,
                                         final Link2FeatureConverter link2FeatureConverter,
                                         final DataStatusService dataStatusService) {
        this.linkRepository = linkRepository;
        this.link2FeatureConverter = link2FeatureConverter;
        this.dataStatusService = dataStatusService;
    }

    public LinkFeatureCollection getLinkMetadata() {
        final List<Link> links = linkRepository.findByObsoleteDateIsNullOrderByNaturalId();

        final MetadataUpdated updated = dataStatusService.findMetadataUpdatedByMetadataType(MetadataType.TRAVEL_TIME_LINKS);

        return link2FeatureConverter.convert(links, updated != null ? updated.getUpdatedTime() : null);
    }
}
