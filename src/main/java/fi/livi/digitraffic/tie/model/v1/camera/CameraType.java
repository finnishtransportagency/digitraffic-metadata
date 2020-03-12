package fi.livi.digitraffic.tie.model.v1.camera;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraTyyppi;

public enum CameraType {

    VAPIX,
    VMX_MPC,
    VMX_MPH,
    D_LINK,
    ZAVIO,
    ENEO,
    BOSCH,
    SONY,
    HIKVISION,
    OLD;

    public static CameraType convertFromKameraTyyppi(final KameraTyyppi kameraTyyppi) {
        if (kameraTyyppi != null) {
            return valueOf(kameraTyyppi.name());
        }
        return null;
    }

}
