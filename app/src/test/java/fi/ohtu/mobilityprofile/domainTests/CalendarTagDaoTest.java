package fi.ohtu.mobilityprofile.domainTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import fi.ohtu.mobilityprofile.BuildConfig;
import fi.ohtu.mobilityprofile.MainActivityStub;
import fi.ohtu.mobilityprofile.domain.CalendarTag;
import fi.ohtu.mobilityprofile.data.CalendarTagDao;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifestTest.xml", constants = BuildConfig.class, sdk = 21)
public class CalendarTagDaoTest {
    private static CalendarTagDao calendarTagDao;

    @Before
    public void setUp() {
        calendarTagDao = new CalendarTagDao();
        Robolectric.setupActivity(MainActivityStub.class);
    }

    @Test
    public void testInsertAndFind() {
        calendarTagDao.insertCalendarTag(new CalendarTag("Kumpula", "Helsinki"));

        assertEquals("Helsinki", calendarTagDao.findTheMostUsedTag("Kumpula").getValue());
    }

    @Test
    public void testFindUnknown() {
        assertEquals(null, calendarTagDao.findTheMostUsedTag("Kumpula"));

        calendarTagDao.insertCalendarTag(new CalendarTag("Kumpula", "Helsinki"));

        assertEquals(null, calendarTagDao.findTheMostUsedTag("asdf"));
    }

    @Test
    public void testFindMultiple() {
        calendarTagDao.insertCalendarTag(new CalendarTag("Kumpula", "Helsinki"));
        calendarTagDao.insertCalendarTag(new CalendarTag("Kumpula", "Keskusta"));
        calendarTagDao.insertCalendarTag(new CalendarTag("Kumpula", "Keskusta"));

        assertEquals("Keskusta", calendarTagDao.findTheMostUsedTag("Kumpula").getValue());
    }

    @Test
    public void testFindMultiple2() {
        calendarTagDao.insertCalendarTag(new CalendarTag("Oulunkylä", "Helsinki"));
        calendarTagDao.insertCalendarTag(new CalendarTag("Oulunkylä", "Kaupunki"));
        calendarTagDao.insertCalendarTag(new CalendarTag("Kumpula", "Kaupunki"));
        calendarTagDao.insertCalendarTag(new CalendarTag("Kumpula", "Tali"));
        calendarTagDao.insertCalendarTag(new CalendarTag("Kumpula", "Tali"));
        calendarTagDao.insertCalendarTag(new CalendarTag("Oulunkylä", "Myllypuro"));
        calendarTagDao.insertCalendarTag(new CalendarTag("Oulunkylä", "Myllypuro"));
        calendarTagDao.insertCalendarTag(new CalendarTag("Kumpula", "Myllypuro"));

        assertEquals("Tali", calendarTagDao.findTheMostUsedTag("Kumpula").getValue());
        assertEquals("Myllypuro", calendarTagDao.findTheMostUsedTag("Oulunkylä").getValue());
    }

    @Test
    public void testReset() {
        calendarTagDao.insertCalendarTag(new CalendarTag("Oulunkylä", "Helsinki"));
        calendarTagDao.insertCalendarTag(new CalendarTag("Kumpula", "Helsinki"));

        assertEquals("Helsinki", calendarTagDao.findTheMostUsedTag("Oulunkylä").getValue());

        CalendarTagDao.deleteAllData();

        assertEquals(null, calendarTagDao.findTheMostUsedTag("Oulunkylä"));
    }
}
