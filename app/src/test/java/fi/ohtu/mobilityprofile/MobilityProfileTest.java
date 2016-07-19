package fi.ohtu.mobilityprofile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import fi.ohtu.mobilityprofile.data.CalendarTagDao;
import fi.ohtu.mobilityprofile.data.FavouritePlaceDao;
import fi.ohtu.mobilityprofile.data.RouteSearchDao;
import fi.ohtu.mobilityprofile.data.UserLocationDao;
import fi.ohtu.mobilityprofile.domain.Visit;
import fi.ohtu.mobilityprofile.data.VisitDao;
import fi.ohtu.mobilityprofile.suggestions.DestinationLogic;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifestTest.xml", constants = BuildConfig.class, sdk = 21)
public class MobilityProfileTest {

    private DestinationLogic mp;
    private CalendarTagDao calendarTagDao;
    private VisitDao visitDao;
    private RouteSearchDao routeSearchDao;
    private String eventLocation;
    private FavouritePlaceDao favouritePlaceDao;

    @Before
    public void setUp() throws Exception {
        calendarTagDao = mock(CalendarTagDao.class);
        visitDao = new VisitDao(mock(UserLocationDao.class));
        routeSearchDao = mock(RouteSearchDao.class);
        favouritePlaceDao = mock(FavouritePlaceDao.class);

        mp = new DestinationLogic(Robolectric.setupActivity(MainActivityStub.class), calendarTagDao, visitDao, routeSearchDao, favouritePlaceDao);
        eventLocation = "Rautatieasema";

        when(calendarTagDao.findTheMostUsedTag(anyString())).thenReturn(null);
    }

    @Test
    public void suggestsFirstLocationFromTheCalendar() throws Exception {
        mp.setCalendarEventLocation(eventLocation);

        String nextLocation = mp.getMostLikelyDestination("Kumpula");
        assertEquals("Rautatieasema", nextLocation);
    }

    @Test
    public void suggestHomeIfNoVisitsMade() {
        mp.setCalendarEventLocation(null);

        String nextLocation = mp.getMostLikelyDestination("Rovaniemi");
        assertEquals("Home", nextLocation);

    }

    @Test
    public void suggestTheFirstVisitFromAllVisits() {
        mp.setCalendarEventLocation(null);
        visitDao.insertVisit(new Visit(System.currentTimeMillis(), "Kumpula"));

        String nextLocation = mp.getMostLikelyDestination("Kumpula");
        assertEquals("Kumpula", nextLocation);
    }

}
