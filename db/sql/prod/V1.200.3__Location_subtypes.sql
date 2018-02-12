--select 'insert into location_subtype(version, subtype_code, description_en, description_fi) values(''' || version || ''',''' ||
--subtype_code || ''',''' || description_en || ''',''' || description_fi || ''');'
--from location_subtype where version in ('1.11.34', '1.11.33');;

insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A1.0','Continent','Maanosa');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A10.0','4th order area','Kaupunginosa');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A11.0','5th order area','5th order area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A12.0','Application region','Application region');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A2.0','Country group','Maaryhmä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A3.0','Country','Maa');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A5.0','Water area','Vesistöalue');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A5.1','Sea','Meri');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A5.2','Lake','Järvialue');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A6.0','Fuzzy area','Fuzzy area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A6.1','Tourist area','Tourist area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A6.2','Metropolitan area','Metropolitan area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A6.3','Industrial area','Industrial area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A6.4','Traffic area','Traffic area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A6.5','Meteorological area','Meteorological area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A6.6','Carpool area','Carpool area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A6.7','Park and ride site','Park and ride site');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A6.8','Car park area','Car park area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A7.0','1st order area','Lääni');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A8.0','2nd order area','Maakunta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A9.0','3rd order area','Kunta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A9.1','3rd order area / rural','Maalaiskunta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','A9.2','3rd order area / urban','Kaupunki');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L1.1','Motorway','Moottoritie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L1.2','1st class road','Valta- tai kantatie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L1.3','2nd class road','Seututie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L1.4','3rd class road','Yhdystie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L2.1','Ring motorway','Kehämoottoritie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L2.2','Other ring-road','Muu kehätie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L3.0','1st order segment','1. asteen tiejakso');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L4.0','2nd order segment','2. asteen tiejakso');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L5.0','Urban street','Taajamatie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L6.0','Vehicular link','Vehicular link');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L6.1','Ferry','Lauttayhteys');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','L6.2','Vehicular rail link','Raideyhteys');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.1','Motorway intersection','Eritasoliittymä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.10','Traffic light','Liikennevalot');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.11','Cross-roads','Tasoliittymä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.12','T-junction','Kolmihaaraliittymä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.13','Intermediate node','Jakopiste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.14','Connection','Yhteyskaista?');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.15','Exit','Exit');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.2','Motorway triangel','Moottoriteiden haarauma');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.3','Motorway junction','Moottoritieliittymä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.4','Motorway exit','Erkanemisramppi');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.5','Motorway entrance','Liittymisramppi');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.6','Overpass','Ylikulku');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.7','Underpass','Alikulku');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P1.8','Roundabout','Kiertoliittymä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P2.1','Distance marker','Etäisyystaulu');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P2.2','Traffic monitoring station','Liikennelaskentapiste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.0','Other landmark','Muu piste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.1','Tunnel','Tunneli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.10','Kiosk with WC','kioski ja WC');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.11','Petrol station','Tankkauspiste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.12','Petrol station with kiosk','Huoltoasema');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.13','Motel','Hotelli/motelli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.14','Border','Raja');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.15','Customs post','Tulli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.16','Toll plaza','Tietulli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.17','Ferry terminal','Lauttaranta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.18','Harbour','Satama');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.19','Square','Tori');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.2','Bridge','Silta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.20','Fair','Aukio');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.21','Garage','Pysäköintihalli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.22','Underground garage','Maanalainen pysäköintihalli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.23','Retail park','Kauppakeskus');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.24','Theme park','Huvipuisto');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.25','Tourist attraction','Matkailukohde');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.26','University','Oppilaitos');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.27','Airport','Lentokenttä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.28','Station','Juna/linja-autoasema');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.29','Hospital','Sairaala');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.3','Service area','Palvelupiste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.30','Church','Kirkko');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.31','Stadium','Urheilukenttä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.33','Castle','Linna');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.34','Town hall','Kaupungintalo');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.35','Exhibition/convention centre','Exhibition/convention centre');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.36','Communities','Taajama');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.37','Place name','Place name (opasteessa?)');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.38','Dam','Pato');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.39','Dike','Penger');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.4','Rest area','Levähdysalue');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.40','Aqueduct','Vesijohto');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.41','Lock','Sulku (kanava?)');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.42','Mountain crossing/pass','Sola');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.43','Railroad crossing','Rautatien tasoristeys');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.44','Wade','Pengerrys');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.45','Ferry','Lautta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.46','Industrial area','Teollisuusalue');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.47','Viadukt','Viadukt');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.5','View point','Näköalapaikka');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.6','Carpool point','Kimppakyytipiste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.7','Park and Ride site','Liityntäpysäköintipaikka');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.8','Car park','Pysäköintialue');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P3.9','Kiosk','Kioski');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P5.0','Parking POI','Parking POI');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P5.1','Underground parking garage','Underground parking garage');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P5.2','Car Park','Car Park');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P5.3','Parking garage','Parking garage');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P5.4','Carpool point','Carpool point');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P5.5','Park and ride site','Park and ride site');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P5.6','Rest area parking','Rest area parking');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.33','P5.7','Campground','Campground');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A1.0','Continent','Maanosa');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A10.0','4th order area','Kaupunginosa');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A11.0','5th order area','5th order area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A12.0','Application region','Application region');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A2.0','Country group','Maaryhmä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A3.0','Country','Maa');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A5.0','Water area','Vesistöalue');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A5.1','Sea','Meri');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A5.2','Lake','Järvialue');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A6.0','Fuzzy area','Fuzzy area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A6.1','Tourist area','Tourist area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A6.2','Metropolitan area','Metropolitan area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A6.3','Industrial area','Industrial area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A6.4','Traffic area','Traffic area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A6.5','Meteorological area','Meteorological area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A6.6','Carpool area','Carpool area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A6.7','Park and ride site','Park and ride site');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A6.8','Car park area','Car park area');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A7.0','1st order area','Lääni');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A8.0','2nd order area','Maakunta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A9.0','3rd order area','Kunta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A9.1','3rd order area / rural','Maalaiskunta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','A9.2','3rd order area / urban','Kaupunki');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L1.1','Motorway','Moottoritie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L1.2','1st class road','Valta- tai kantatie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L1.3','2nd class road','Seututie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L1.4','3rd class road','Yhdystie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L2.1','Ring motorway','Kehämoottoritie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L2.2','Other ring-road','Muu kehätie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L3.0','1st order segment','1. asteen tiejakso');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L4.0','2nd order segment','2. asteen tiejakso');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L5.0','Urban street','Taajamatie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L6.0','Vehicular link','Vehicular link');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L6.1','Ferry','Lauttayhteys');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','L6.2','Vehicular rail link','Raideyhteys');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.1','Motorway intersection','Eritasoliittymä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.10','Traffic light','Liikennevalot');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.11','Cross-roads','Tasoliittymä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.12','T-junction','Kolmihaaraliittymä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.13','Intermediate node','Jakopiste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.14','Connection','Yhteyskaista?');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.15','Exit','Exit');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.2','Motorway triangel','Moottoriteiden haarauma');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.3','Motorway junction','Moottoritieliittymä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.4','Motorway exit','Erkanemisramppi');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.5','Motorway entrance','Liittymisramppi');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.6','Overpass','Ylikulku');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.7','Underpass','Alikulku');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P1.8','Roundabout','Kiertoliittymä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P2.1','Distance marker','Etäisyystaulu');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P2.2','Traffic monitoring station','Liikennelaskentapiste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.0','Other landmark','Muu piste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.1','Tunnel','Tunneli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.10','Kiosk with WC','kioski ja WC');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.11','Petrol station','Tankkauspiste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.12','Petrol station with kiosk','Huoltoasema');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.13','Motel','Hotelli/motelli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.14','Border','Raja');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.15','Customs post','Tulli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.16','Toll plaza','Tietulli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.17','Ferry terminal','Lauttaranta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.18','Harbour','Satama');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.19','Square','Tori');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.2','Bridge','Silta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.20','Fair','Aukio');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.21','Garage','Pysäköintihalli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.22','Underground garage','Maanalainen pysäköintihalli');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.23','Retail park','Kauppakeskus');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.24','Theme park','Huvipuisto');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.25','Tourist attraction','Matkailukohde');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.26','University','Oppilaitos');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.27','Airport','Lentokenttä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.28','Station','Juna/linja-autoasema');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.29','Hospital','Sairaala');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.3','Service area','Palvelupiste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.30','Church','Kirkko');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.31','Stadium','Urheilukenttä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.33','Castle','Linna');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.34','Town hall','Kaupungintalo');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.35','Exhibition/convention centre','Exhibition/convention centre');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.36','Communities','Taajama');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.37','Place name','Place name (opasteessa?)');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.38','Dam','Pato');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.39','Dike','Penger');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.4','Rest area','Levähdysalue');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.40','Aqueduct','Vesijohto');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.41','Lock','Sulku (kanava?)');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.42','Mountain crossing/pass','Sola');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.43','Railroad crossing','Rautatien tasoristeys');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.44','Wade','Pengerrys');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.45','Ferry','Lautta');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.46','Industrial area','Teollisuusalue');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.47','Viadukt','Viadukt');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.5','View point','Näköalapaikka');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.6','Carpool point','Kimppakyytipiste');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.7','Park and Ride site','Liityntäpysäköintipaikka');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.8','Car park','Pysäköintialue');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P3.9','Kiosk','Kioski');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P5.0','Parking POI','Parking POI');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P5.1','Underground parking garage','Underground parking garage');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P5.2','Car Park','Car Park');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P5.3','Parking garage','Parking garage');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P5.4','Carpool point','Carpool point');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P5.5','Park and ride site','Park and ride site');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P5.6','Rest area parking','Rest area parking');
insert into location_subtype(version, subtype_code, description_en, description_fi) values('1.11.34','P5.7','Campground','Campground');