package fi.livi.digitraffic.tie.helper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import fi.livi.digitraffic.tie.AbstractSpringJUnitTest;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;

@RunWith(JUnit4.class)
public class ToStringHelperTest extends AbstractSpringJUnitTest {

    @Test
    public void testKameraToString() {
        final KameraVO kamera = new KameraVO();
        kamera.setId(741L);
        kamera.setVanhaId(1501);
        kamera.setNimi("OLD_vt6_Lapinjärvi");
        Assert.assertEquals("KameraVO: {\"lotjuId\":741,\"vanhaId\":1501,\"nimi\":\"OLD_vt6_Lapinj\\u00E4rvi\"}", ToStringHelper.toString(kamera));
    }

    @Test
    public void testLamasemaToString() {
        final LamAsemaVO lam = new LamAsemaVO();
        lam.setId(607L);
        lam.setVanhaId(23210);
        lam.setNimi("L_vt12_Vammala");
        Assert.assertEquals("LamAsemaVO: {\"lotjuId\":607,\"vanhaId\":23210,\"nimi\":\"L_vt12_Vammala\"}", ToStringHelper.toString(lam));
    }

    @Test
    public void testTiesaaasemaToString() {
        final TiesaaAsemaVO tsa = new TiesaaAsemaVO();
        tsa.setId(607L);
        tsa.setVanhaId(23210);
        tsa.setNimi("L_vt12_Vammala");
        Assert.assertEquals("TiesaaAsemaVO: {\"lotjuId\":607,\"vanhaId\":23210,\"nimi\":\"L_vt12_Vammala\"}", ToStringHelper.toString(tsa));
    }

    @Test
    public void testToStringFull() {
        final TiesaaAsemaVO tsa = new TiesaaAsemaVO();
        tsa.setId(607L);
        tsa.setVanhaId(23210);
        tsa.setNimi("L_vt12_Vammala");
        Assert.assertEquals(
                "{\"class\":\"TiesaaAsemaVO\",\"aliasemaId\":null,\"antureillaKaapelikaivo\":null,\"antureillaPutkitukset\":null,\"anturiliitantaHuoltotarranMerkinnat\":null,\"anturiliitantaSarjanumero\":null,\"anturiliitantaValmistusviikko\":null,\"anturiliitantaValmistusvuosi\":null,\"anturiliitantayksikko\":null,\"ip\":null,\"kaapelikaivonKunto\":null,\"kehikko\":null,\"kehikonHuoltotarranMerkinnat\":null,\"kehikonSarjanumero\":null,\"kehikonValmistusviikko\":null,\"kehikonValmistusvuosi\":null,\"lisakuvaus\":null,\"master\":null,\"merkittavyys\":null,\"portti\":null,\"putkienMateriaali\":null,\"tyyppi\":null,\"ymparistoKuvaus\":null,\"aikakatkaisu\":null,\"aikavyohyke\":null,\"aliverkonPeite\":null,\"alkamisPaiva\":null,\"asemanSijainti\":null,\"asemanTila\":null,\"hankeId\":null,\"huoltoPuutteet\":null,\"huoltolevikkeenEtaisyysAsemasta\":null,\"julkinen\":null,\"keruuVali\":null,\"keruunTila\":null,\"korjaushuolto\":null,\"kunta\":null,\"kuntaKoodi\":null,\"laitekaappiId\":null,\"lisatieto\":null,\"liviId\":null,\"maa\":null,\"maakunta\":null,\"maakuntaKoodi\":null,\"nimiEn\":null,\"nimiFi\":null,\"nimiSe\":null,\"ohjelmistoversio\":null,\"paattymisPaiva\":null,\"riittavanKokoinenHuoltolevike\":null,\"synkronoituTierekisteriin\":null,\"vanhaId\":23210,\"verkkolaiteId\":null,\"vuosihuolto\":null,\"yhdyskaytava\":null,\"yhteysTapa\":null,\"korkeus\":null,\"latitudi\":null,\"longitudi\":null,\"tieosoite\":null,\"kuvaus\":null,\"nimi\":\"L_vt12_Vammala\",\"takuunPaattymisPvm\":null,\"id\":607,\"luonut\":null,\"luotu\":null,\"muokattu\":null,\"muokkaaja\":null}",
                ToStringHelper.toStringFull(tsa));
    }

}
