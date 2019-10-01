package fi.livi.digitraffic.tie.converter;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.ConfidentialityValueEnum;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.CountryEnum;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Exchange;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.HeaderInformation;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.InformationStatusEnum;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.InternationalIdentifier;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualString;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualStringValue;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PayloadPublication;

@ConditionalOnWebApplication
@Component
public final class Datex2Util {
    private final InformationStatusEnum informationStatus;

    public Datex2Util(@Value("${dt.domain.url}") final String camUrl) {
        this.informationStatus = camUrl.toLowerCase().contains("test") ? InformationStatusEnum.TEST : InformationStatusEnum.REAL;
    }

    public <T extends PayloadPublication> T publication(final T publication, final ZonedDateTime updated) {
        return (T) publication
            .withPublicationTime(DateHelper.toXMLGregorianCalendar(updated))
            .withPublicationCreator(new InternationalIdentifier()
                .withCountry(CountryEnum.FI)
                .withNationalIdentifier("FI"))
            .withLang("Finnish");
    }

    public D2LogicalModel logicalModel(final PayloadPublication publication) {
        return new D2LogicalModel()
            .withPayloadPublication(publication)
            .withExchange(new Exchange().withSupplierIdentification(new InternationalIdentifier().withCountry(CountryEnum.FI).withNationalIdentifier("FI")));
    }

    public HeaderInformation headerInformation() {
        return new HeaderInformation()
            .withConfidentiality(ConfidentialityValueEnum.NO_RESTRICTION)
            .withInformationStatus(informationStatus);
    }

    public MultilingualString multiLingualString(final String value) {
        return new MultilingualString().withValues(new MultilingualString.Values().withValue(
            new MultilingualStringValue()
                .withLang("FI")
                .withValue(value)));
    }

}
