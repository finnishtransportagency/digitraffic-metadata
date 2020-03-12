package fi.livi.digitraffic.tie.service.v1;

public class LotjuAnturiWrapper<T> {

    private final T anturi;
    private final long anturiLotjuId;
    private final long aika;
    private final long asemaLotjuId;

    public LotjuAnturiWrapper(final long asemaLotjuId, final long anturiLotjuId, final T anturi, final long aika) {
        this.asemaLotjuId = asemaLotjuId;
        this.anturiLotjuId = anturiLotjuId;
        this.anturi = anturi;
        this.aika = aika;
    }

    public long getAsemaLotjuId() {
        return asemaLotjuId;
    }

    public long getAnturiLotjuId() {
        return anturiLotjuId;
    }

    public T getAnturi() {
        return anturi;
    }

    public long getAika() {
        return aika;
    }
}