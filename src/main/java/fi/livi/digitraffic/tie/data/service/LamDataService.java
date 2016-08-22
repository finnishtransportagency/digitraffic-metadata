package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;

public interface LamDataService {

    LamRootDataObjectDto findPublicLamData(boolean onlyUpdateInfo);

    LamRootDataObjectDto findPublicLamData(long roadStationNaturalId);

    void updateLamData(Lam data);
}
