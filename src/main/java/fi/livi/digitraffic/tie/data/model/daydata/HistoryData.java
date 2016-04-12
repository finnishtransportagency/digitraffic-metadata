package fi.livi.digitraffic.tie.data.model.daydata;

import java.util.List;

import fi.livi.digitraffic.tie.data.model.DataObject;

public class HistoryData extends DataObject {
    private final List<LinkDynamicData> linkDynamicData;

    public HistoryData(List<LinkDynamicData> linkDynamicData) {
        this.linkDynamicData = linkDynamicData;
    }

    public List<LinkDynamicData> getLinkDynamicData() {
        return linkDynamicData;
    }
}
