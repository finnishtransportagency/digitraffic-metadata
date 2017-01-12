package fi.livi.digitraffic.tie.metadata.model;

import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LaiteTyyppi;

public enum CalculatorDeviceType {

    DSL_3("DSL_3"),
    DSL_4_L("DSL_4L"),
    DSL_4_G("DSL_4G"),
    DSL_5("DSL_5"),
    OTHER("OTHER");

    private final String value;

    CalculatorDeviceType(final String v) {
        value = v;
    }

    public String getValue() {
        return value;
    }

    public static CalculatorDeviceType convertFromLaiteTyyppi(final LaiteTyyppi laskinlaite) {
        if (laskinlaite != null) {
            for (final CalculatorDeviceType calculatorDeviceType : values()) {
                if (calculatorDeviceType.getValue().equals(laskinlaite.value())) {
                    return calculatorDeviceType;
                }
            }
        }
        return null;
    }
}
