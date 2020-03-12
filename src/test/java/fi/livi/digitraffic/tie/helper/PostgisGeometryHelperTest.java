package fi.livi.digitraffic.tie.helper;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import fi.livi.digitraffic.tie.AbstractTest;

@RunWith(JUnit4.class)
public class PostgisGeometryHelperTest extends AbstractTest {

    private final double TAMPERE_WGS84_X = 23.774862;
    private final double TAMPERE_WGS84_Y = 61.486365;
    private final double TAMPERE_TM35FIN_X = 328288.5;
    private final double TAMPERE_TM35FIN_Y = 6821211;
    private final double Z = 6821211;
    private final double ALLOWED_DELTA = 0.00001;

    @Test
    public void createCoordinateWithZFromETRS89ToWGS84() {
        final Coordinate created = PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(TAMPERE_TM35FIN_X, TAMPERE_TM35FIN_Y, Z);
        checkCoordinate(created, TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
    }

    @Test
    public void createCoordinateWithZ() {
        final Coordinate created = PostgisGeometryHelper.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
        checkCoordinate(created, TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
    }

    @Test
    public void createLineStringWithZ() {
        final double diff_1 = 1.1;
        final double diff_2 = 2.2;
        final ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(PostgisGeometryHelper.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z));
        coords.add(PostgisGeometryHelper.createCoordinateWithZ(TAMPERE_WGS84_X + diff_1, TAMPERE_WGS84_Y + diff_1, Z + diff_1));
        coords.add(PostgisGeometryHelper.createCoordinateWithZ(TAMPERE_WGS84_X + diff_2, TAMPERE_WGS84_Y + diff_2, Z + diff_2));

        final LineString lineString = PostgisGeometryHelper.createLineStringWithZ(coords);
        checkCoordinate(lineString.getCoordinateN(0), TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
        checkCoordinate(lineString.getCoordinateN(1), TAMPERE_WGS84_X + diff_1, TAMPERE_WGS84_Y + diff_1, Z + diff_1);
        checkCoordinate(lineString.getCoordinateN(2), TAMPERE_WGS84_X + diff_2, TAMPERE_WGS84_Y + diff_2, Z + diff_2);
    }

    private void checkCoordinate(Coordinate coordinate, final double x, final double y, final double z) {
        Assert.assertEquals(x, coordinate.getX(), ALLOWED_DELTA);
        Assert.assertEquals(y, coordinate.getY(), ALLOWED_DELTA);
        Assert.assertEquals(z, coordinate.getZ(), ALLOWED_DELTA);
    }


    //    @org.junit.Test
//    public void testKameraToString() {
//        final KameraVO kamera = new KameraVO();
//        kamera.setId(741L);
//        kamera.setVanhaId(1501);
//        kamera.setNimi("OLD_vt6_Lapinjärvi");
//        Assert.assertEquals("KameraVO: {\"lotjuId\":741,\"vanhaId\":1501,\"nimi\":\"OLD_vt6_Lapinj\\u00E4rvi\"}", ToStringHelper.toString(kamera));
//    }
//
//    @org.junit.Test
//    public void testLamasemaToString() {
//        final LamAsemaVO lam = new LamAsemaVO();
//        lam.setId(607L);
//        lam.setVanhaId(23210);
//        lam.setNimi("L_vt12_Vammala");
//        Assert.assertEquals("LamAsemaVO: {\"lotjuId\":607,\"vanhaId\":23210,\"nimi\":\"L_vt12_Vammala\"}", ToStringHelper.toString(lam));
//    }
//
//    @org.junit.Test
//    public void testTiesaaasemaToString() {
//        final TiesaaAsemaVO tsa = new TiesaaAsemaVO();
//        tsa.setId(607L);
//        tsa.setVanhaId(23210);
//        tsa.setNimi("L_vt12_Vammala");
//        Assert.assertEquals("TiesaaAsemaVO: {\"lotjuId\":607,\"vanhaId\":23210,\"nimi\":\"L_vt12_Vammala\"}", ToStringHelper.toString(tsa));
//    }
//
//    @org.junit.Test
//    public void testToStringFull() {
//        final TiesaaAsemaVO tsa = new TiesaaAsemaVO();
//        tsa.setId(607L);
//        tsa.setVanhaId(23210);
//        tsa.setNimi("L_vt12_Vammala");
//        Assert.assertEquals(
//                "{\"class\":\"TiesaaAsemaVO\",\"aliasemaId\":null,\"antureillaKaapelikaivo\":null,\"antureillaPutkitukset\":null,\"anturiliitantaHuoltotarranMerkinnat\":null,\"anturiliitantaSarjanumero\":null,\"anturiliitantaValmistusviikko\":null,\"anturiliitantaValmistusvuosi\":null,\"anturiliitantayksikko\":null,\"ip\":null,\"kaapelikaivonKunto\":null,\"kehikko\":null,\"kehikonHuoltotarranMerkinnat\":null,\"kehikonSarjanumero\":null,\"kehikonValmistusviikko\":null,\"kehikonValmistusvuosi\":null,\"lisakuvaus\":null,\"master\":null,\"merkittavyys\":null,\"portti\":null,\"putkienMateriaali\":null,\"tyyppi\":null,\"ymparistoKuvaus\":null,\"aikakatkaisu\":null,\"aikavyohyke\":null,\"aliverkonPeite\":null,\"alkamisPaiva\":null,\"asemanSijainti\":null,\"asemanTila\":null,\"hankeId\":null,\"huoltoPuutteet\":null,\"huoltolevikkeenEtaisyysAsemasta\":null,\"julkinen\":null,\"keruuVali\":null,\"keruunTila\":null,\"korjaushuolto\":null,\"kunta\":null,\"kuntaKoodi\":null,\"laitekaappiId\":null,\"lisatieto\":null,\"liviId\":null,\"maa\":null,\"maakunta\":null,\"maakuntaKoodi\":null,\"nimiEn\":null,\"nimiFi\":null,\"nimiSe\":null,\"ohjelmistoversio\":null,\"paattymisPaiva\":null,\"riittavanKokoinenHuoltolevike\":null,\"synkronoituTierekisteriin\":null,\"vanhaId\":23210,\"verkkolaiteId\":null,\"vuosihuolto\":null,\"yhdyskaytava\":null,\"yhteysTapa\":null,\"korkeus\":null,\"latitudi\":null,\"longitudi\":null,\"tieosoite\":null,\"kuvaus\":null,\"nimi\":\"L_vt12_Vammala\",\"takuunPaattymisPvm\":null,\"id\":607,\"luonut\":null,\"luotu\":null,\"muokattu\":null,\"muokkaaja\":null}",
//                ToStringHelper.toStringFull(tsa));
//    }

}
