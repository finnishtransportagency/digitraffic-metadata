package fi.livi.digitraffic.tie.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.AjoneuvoluokkaTyyppi;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Vehicle class", value = "VehicleClass")
public enum VehicleClass {

    CAR(AjoneuvoluokkaTyyppi.HA.value(), AjoneuvoluokkaTyyppi.PA.value()), // Henkilöauto – Passenger car / Pakettiauto - Delivery van
    BUS(AjoneuvoluokkaTyyppi.LA.value()), // Linja-auto – Bus
    TRUCK(AjoneuvoluokkaTyyppi.KAIP.value()), // Kuorma-auto ilman perävaunua – Trucks
    TRUCKST(AjoneuvoluokkaTyyppi.KAPP.value()), // kuorma-auto ja puoliperävaunu – Semi-trailer trucks
    TRUCKT(AjoneuvoluokkaTyyppi.KATP.value()); // kuorma-auto ja täysperävaunu – Trucks with trailer

    private static final Logger LOG = LoggerFactory.getLogger(RoadStationState.class);

    private final String[] fiValues;

    VehicleClass(final String...fiValues) {
        this.fiValues = fiValues;
    }

    public static VehicleClass fromAjoneuvoluokka(AjoneuvoluokkaTyyppi ajoneuvoluokka) {
        if (ajoneuvoluokka == null) {
            return null;
        }

        for (final VehicleClass category : VehicleClass.values()) {
            for (final String fiValue : category.fiValues) {
                if (fiValue.equals(ajoneuvoluokka.value())) {
                    return category;
                }
            }
        }

        LOG.error("VehicleClass for AjoneuvoluokkaTyyppi {} not found", ajoneuvoluokka.value());
        return null;
    }
}
