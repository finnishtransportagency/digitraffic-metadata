package fi.livi.digitraffic.tie.model;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LaiteTyyppi;

public enum CalculatorDeviceType {

    DSL_3("DSL_3"),
    DSL_4_L("DSL_4L"),
    DSL_4_G("DSL_4G"),
    DSL_5("DSL_5"),
    OTHER("MUU");

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
