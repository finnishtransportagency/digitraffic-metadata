package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.LamFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dao.LinkFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationService;

@Service
public class FreeFlowSpeedService {
    private final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository;
    private final LamFreeFlowSpeedRepository lamFreeFlowSpeedRepository;
    private final LamStationService lamStationService;

    @Autowired
    public FreeFlowSpeedService(final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository,
                                final LamFreeFlowSpeedRepository lamFreeFlowSpeedRepository,
                                final LamStationService lamStationService) {
        this.linkFreeFlowSpeedRepository = linkFreeFlowSpeedRepository;
        this.lamFreeFlowSpeedRepository = lamFreeFlowSpeedRepository;
        this.lamStationService = lamStationService;
    }


    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listLinksPublicFreeFlowSpeeds(final boolean onlyUpdateInfo) {

        // TODO: where to read update info?
        final LocalDateTime updated = LocalDateTime.now();
        if (onlyUpdateInfo) {
            return new FreeFlowSpeedRootDataObjectDto(updated);
        } else {
            return new FreeFlowSpeedRootDataObjectDto(
                    linkFreeFlowSpeedRepository.listAllLinkFreeFlowSpeeds(),
                    lamFreeFlowSpeedRepository.listAllPublicLamFreeFlowSpeeds(),
                    updated);
        }
    }

    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listLinksPublicFreeFlowSpeeds(final long linkId) {
        if (1 != linkFreeFlowSpeedRepository.linkExists(linkId)) {
            throw new ObjectNotFoundException("Link", linkId);
        }
        // TODO: where to read update info?
        final LocalDateTime updated = LocalDateTime.now();
        return new FreeFlowSpeedRootDataObjectDto(
                linkFreeFlowSpeedRepository.listAllLinkFreeFlowSpeeds(linkId),
                Collections.emptyList(),
                updated);

    }

    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listLamsPublicFreeFlowSpeeds(final long roadStationNaturalId) {

        // TODO: where to read update info?
        final LocalDateTime updated = LocalDateTime.now();
        if (!lamStationService.lamStationExistsWithRoadStationNaturalId(roadStationNaturalId)) {
            throw new ObjectNotFoundException("LamStation", roadStationNaturalId);
        }
        return new FreeFlowSpeedRootDataObjectDto(
                Collections.emptyList(),
                lamFreeFlowSpeedRepository.listAllPublicLamFreeFlowSpeeds(roadStationNaturalId),
                updated);

    }
}
