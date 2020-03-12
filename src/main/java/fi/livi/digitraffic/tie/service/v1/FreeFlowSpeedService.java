package fi.livi.digitraffic.tie.service.v1;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.TmsFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.dto.v1.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.dao.v1.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@Service
public class FreeFlowSpeedService {
    private final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository;
    private final TmsStationRepository tmsStationRepository;
    private final DataStatusService dataStatusService;

    @Autowired
    public FreeFlowSpeedService(final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository, final TmsStationRepository tmsStationRepository, final DataStatusService dataStatusService) {
        this.tmsFreeFlowSpeedRepository = tmsFreeFlowSpeedRepository;
        this.tmsStationRepository = tmsStationRepository;
        this.dataStatusService = dataStatusService;
    }


    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listLinksPublicFreeFlowSpeeds(final boolean onlyUpdateInfo) {
        final ZonedDateTime tmsUpdated = dataStatusService.findDataUpdatedTime(DataType.TMS_FREE_FLOW_SPEEDS_DATA);

        if (onlyUpdateInfo) {
            return new FreeFlowSpeedRootDataObjectDto(tmsUpdated);
        } else {
            return new FreeFlowSpeedRootDataObjectDto(
                    tmsFreeFlowSpeedRepository.listAllPublicTmsFreeFlowSpeeds(),
                    tmsUpdated);
        }
    }

    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listTmsPublicFreeFlowSpeeds(final long roadStationNaturalId) {
        if (!tmsStationRepository.tmsExistsWithRoadStationNaturalId(roadStationNaturalId)) {
            throw new ObjectNotFoundException("TmsStation", roadStationNaturalId);
        }

        return new FreeFlowSpeedRootDataObjectDto(
                tmsFreeFlowSpeedRepository.listAllPublicTmsFreeFlowSpeeds(roadStationNaturalId),
                dataStatusService.findDataUpdatedTime(DataType.TMS_FREE_FLOW_SPEEDS_DATA));

    }
}
