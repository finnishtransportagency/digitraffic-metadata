package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.camera.CameraRootDataObjectDto;

public interface CameraDataService {

    CameraRootDataObjectDto findAllNonObsoleteCameraStationsData();
}
