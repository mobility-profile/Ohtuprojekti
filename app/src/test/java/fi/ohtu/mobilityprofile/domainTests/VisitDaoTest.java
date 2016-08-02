package fi.ohtu.mobilityprofile.domainTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import fi.ohtu.mobilityprofile.BuildConfig;
import fi.ohtu.mobilityprofile.MainActivityStub;
import fi.ohtu.mobilityprofile.data.VisitDao;
import fi.ohtu.mobilityprofile.domain.Coordinate;
import fi.ohtu.mobilityprofile.domain.SignificantPlace;
import fi.ohtu.mobilityprofile.domain.Visit;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifestTest.xml", constants = BuildConfig.class, sdk = 21)
public class VisitDaoTest {
    private VisitDao visitDao;

    @Before
    public void setUp() {
        this.visitDao = new VisitDao();
        Robolectric.setupActivity(MainActivityStub.class);
    }

    @Test
    public void testInsert() {
        visitDao.insertVisit(new Visit(123, new SignificantPlace("Koulu", "Kumpula", new Coordinate(new Float(1), new Float(1)))));
        assertEquals("Kumpula", visitDao.getAllVisitsToSignificantPlaces().get(0).getAddress());
    }

    @Test
    public void testInsertMultiple() {
        visitDao.insertVisit(new Visit(123, new SignificantPlace("Koulu", "Kumpula", new Coordinate(new Float(1), new Float(1)))));
        visitDao.insertVisit(new Visit(345, new SignificantPlace("Kauppa", "Kauppakatu", new Coordinate(new Float(1), new Float(1)))));
        visitDao.insertVisit(new Visit(567, new SignificantPlace("Toimisto", "Töölö", new Coordinate(new Float(1), new Float(1)))));

        assertEquals("Töölö", visitDao.getLastVisit().getAddress());
        assertEquals(3, visitDao.getAllVisitsToSignificantPlaces().size());
    }

    @Test
    public void testDeleteAll() {
        visitDao.insertVisit(new Visit(123, new SignificantPlace("Koulu", "Kumpula", new Coordinate(new Float(1), new Float(1)))));
        visitDao.insertVisit(new Visit(345, new SignificantPlace("Kauppa", "Kauppakatu", new Coordinate(new Float(1), new Float(1)))));
        visitDao.insertVisit(new Visit(567, new SignificantPlace("Toimisto", "Töölö", new Coordinate(new Float(1), new Float(1)))));
        assertEquals(3, visitDao.getAllVisitsToSignificantPlaces().size());
        visitDao.deleteAllData();
        assertEquals(0, visitDao.getAllVisitsToSignificantPlaces().size());

    }

}
