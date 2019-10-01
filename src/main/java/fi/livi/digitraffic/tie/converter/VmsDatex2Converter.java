package fi.livi.digitraffic.tie.converter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.trafficsigns.Device;
import fi.livi.digitraffic.tie.data.model.trafficsigns.DeviceData;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.DistanceFromLinearElementStart;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.LinearElementByCode;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.LinearElementNatureEnum;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Point;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PointAlongLinearElement;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PointByCoordinates;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PointCoordinates;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TextPage;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Vms;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsDatexPictogramEnum;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsMessage;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsMessageIndexVmsMessage;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsMessagePictogramDisplayAreaIndexVmsPictogramDisplayArea;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsPictogram;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsPictogramDisplayArea;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsPictogramDisplayAreaPictogramSequencingIndexVmsPictogram;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsRecord;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsTablePublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsText;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsTextLine;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsTextLineIndexVmsTextLine;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsUnit;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsUnitRecord;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsUnitRecordVersionedReference;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsUnitRecordVmsIndexVmsRecord;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsUnitTable;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsUnitTableVersionedReference;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsUnitVmsIndexVms;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;

@ConditionalOnWebApplication
@Component
public class VmsDatex2Converter {
    private final Datex2Util datex2Util;
    private final CoordinateConverter coordinateConverter;

    private static final String UNIT_TABLE_IDENTIFIER = "TMFG_VMS";

    public VmsDatex2Converter(final Datex2Util datex2Util, final CoordinateConverter coordinateConverter) {
        this.datex2Util = datex2Util;
        this.coordinateConverter = coordinateConverter;
    }

    public D2LogicalModel convertVmsTable(final List<Device> devices, final ZonedDateTime updated) {
        final VmsTablePublication publication = datex2Util.publication(new VmsTablePublication(), updated)
            .withHeaderInformation(datex2Util.headerInformation())
            .withVmsUnitTable(new VmsUnitTable()
                .withId(UNIT_TABLE_IDENTIFIER)
                .withVersion("1")
                .withVmsUnitRecord(devices.stream().map(this::getVmsUnitRecord).collect(Collectors.toList())))
            ;

        return datex2Util.logicalModel(publication);
    }

    public D2LogicalModel convertVmsData(final List<DeviceData> data, final Map<String, Device> deviceMap, final ZonedDateTime updated) {
        final VmsPublication publication = datex2Util.publication(new VmsPublication(), updated)
            .withHeaderInformation(datex2Util.headerInformation())
            .withVmsUnit(data.stream().map(d -> getVmsUnit(deviceMap, d)).collect(Collectors.toList()))
            ;

        return datex2Util.logicalModel(publication);
    }

    private VmsUnit getVmsUnit(final Map<String, Device> deviceMap, final DeviceData data) {
        final Device device = deviceMap.get(data.getDeviceId());

        final VmsPictogram vp = new VmsPictogram();

        final VmsMessage m = new VmsMessage()
            .withVmsPictogramDisplayArea(new VmsMessagePictogramDisplayAreaIndexVmsPictogramDisplayArea()
                .withPictogramDisplayAreaIndex(0)
                .withVmsPictogramDisplayArea(new VmsPictogramDisplayArea()
                    .withVmsPictogram(new VmsPictogramDisplayAreaPictogramSequencingIndexVmsPictogram()
                        .withPictogramSequencingIndex(0)
                        .withVmsPictogram(vp)
                    )
                )
            );

        // if speed limit, then set speed and type
        if(device.getType().equals("NOPEUSRAJOITUS")) {
            vp.getPictogramDescription().add(VmsDatexPictogramEnum.MAXIMUM_SPEED_LIMITED_TO_THE_FIGURE_INDICATED);
            vp.setSpeedAttribute(Float.valueOf(data.getDisplayValue()));
        } else { // VAIHTUVAVAROITUSMERKKI
            m.withTextPage(new TextPage()
                .withPageNumber(0)
                .withVmsText(new VmsText().withVmsTextLine(new VmsTextLineIndexVmsTextLine()
                    .withLineIndex(0)
                    .withVmsTextLine(new VmsTextLine().withVmsTextLine(data.getDisplayValue())))));
        }

        if(data.getEffectDate() != null) {
            m.withTimeLastSet(DateHelper.toXMLGregorianCalendar(data.getEffectDate()));
        }

        if(StringUtils.isNotEmpty(data.getCause())) {
            m.withReasonForSetting(datex2Util.multiLingualString(data.getCause()));
        }

        return new VmsUnit()
            .withVmsUnitTableReference(new VmsUnitTableVersionedReference().withId(UNIT_TABLE_IDENTIFIER).withVersion("1"))
            .withVmsUnitReference(new VmsUnitRecordVersionedReference().withId(data.getDeviceId()).withVersion("1"))
            .withVms(new VmsUnitVmsIndexVms().withVmsIndex(0).withVms(new Vms()
                .withVmsWorking(!data.getReliability().equals("LAITEVIKA"))
                .withVmsMessage(new VmsMessageIndexVmsMessage()
                    .withMessageIndex(0)
                    .withVmsMessage(m)
            )));
    }

    private VmsUnitRecord getVmsUnitRecord(final Device device) {
        final VmsRecord record = new VmsRecord();

        if(device.getEtrsTm35FinX() != null && device.getEtrsTm35FinY() != null) {
            final fi.livi.digitraffic.tie.metadata.geojson.Point point = coordinateConverter.convertFromETRS89ToWGS84(
                new fi.livi.digitraffic.tie.metadata.geojson.Point(device.getEtrsTm35FinX(), device.getEtrsTm35FinY()));

            final RoadAddressHelper.RoadAddress ra = RoadAddressHelper.parseRoadAddress(device.getRoadAddress());

            record.withVmsLocation(new Point()
                .withPointAlongLinearElement(new PointAlongLinearElement()
                    .withLinearElement(new LinearElementByCode()
                        .withRoadNumber(ra.roadNumber)
                        .withLinearElementIdentifier(ra.roadSection)
                        .withLinearElementNature(LinearElementNatureEnum.ROAD_SECTION)
                    )
                    .withDistanceAlongLinearElement(new DistanceFromLinearElementStart()
                        .withDistanceAlong(ra.distance)
                    )
                )
                .withPointByCoordinates(new PointByCoordinates().withPointCoordinates(
                    new PointCoordinates()
                        .withLatitude(point.getLatitude().floatValue())
                        .withLongitude(point.getLongitude().floatValue())
            )));
        }

        return new VmsUnitRecord()
                .withId(device.getId())
                .withVersion("1")
                .withVmsUnitIdentifier(device.getId())
                .withVmsRecord(new VmsUnitRecordVmsIndexVmsRecord()
                    .withVmsIndex(0)
                    .withVmsRecord(record)
                )
            ;
    }

}
