package fi.ohtu.mobilityprofile.remoteconnection;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;

import fi.ohtu.mobilityprofile.DestinationLogic;
import fi.ohtu.mobilityprofile.domain.CalendarTag;
import fi.ohtu.mobilityprofile.data.CalendarTagDao;
import fi.ohtu.mobilityprofile.data.FavouritePlaceDao;
import fi.ohtu.mobilityprofile.domain.RouteSearch;
import fi.ohtu.mobilityprofile.data.RouteSearchDao;
import fi.ohtu.mobilityprofile.domain.Visit;
import fi.ohtu.mobilityprofile.data.VisitDao;
import fi.ohtu.mobilityprofile.location.AddressConverter;

import static fi.ohtu.mobilityprofile.remoteconnection.RequestCode.*;

/**
 * Used for processing incoming requests from other apps.
 */
public class RequestHandler extends Handler {
    private Context context;
    private DestinationLogic mobilityProfile;
    private CalendarTagDao calendarTagDao;
    private VisitDao visitDao;
    private RouteSearchDao routeSearchDao;
    private FavouritePlaceDao favouritePlaceDao;

    /**
     * Creates the RequestHandler.
     *
     * @param mobilityProfile Journey planner that provides the logic for our app
     * @param calendarTagDao DAO for calendar tags
     * @param visitDao DAO for visits
     * @param routeSearchDao DAO for routeSearch
     * @param favouritePlaceDao DAO for favourite places
     */
    public RequestHandler(DestinationLogic mobilityProfile, CalendarTagDao calendarTagDao,
                          VisitDao visitDao, RouteSearchDao routeSearchDao, FavouritePlaceDao favouritePlaceDao) {
        this.mobilityProfile = mobilityProfile;
        this.calendarTagDao = calendarTagDao;
        this.visitDao = visitDao;
        this.routeSearchDao = routeSearchDao;
        this.favouritePlaceDao = favouritePlaceDao;
    }

    /**
     * Creates the RequestHandler.
     *
     * @param context for AddressConverter
     * @param mobilityProfile Journey planner that provides the logic for our app
     * @param calendarTagDao DAO for calendar tags
     * @param visitDao DAO for visits
     * @param routeSearchDao DAO for routeSearch
     * @param favouritePlaceDao DAO for favourite places
     */
    public RequestHandler(Context context, DestinationLogic mobilityProfile, CalendarTagDao calendarTagDao,
                          VisitDao visitDao, RouteSearchDao routeSearchDao, FavouritePlaceDao favouritePlaceDao) {
        this.context = context;
        this.mobilityProfile = mobilityProfile;
        this.calendarTagDao = calendarTagDao;
        this.visitDao = visitDao;
        this.routeSearchDao = routeSearchDao;
        this.favouritePlaceDao = favouritePlaceDao;
    }

    @Override
    public void handleMessage(Message msg) {
        Log.d("Remote Service", "Remote Service invoked (" + msg.what + ")");

        Message message;
        switch (msg.what) {
            case REQUEST_MOST_LIKELY_DESTINATION:
                message = processDestinationRequest();
                break;
            case SEND_USED_DESTINATION:
                processUsedRoute(msg);
                return;
            case REQUEST_START_LOCATION:
                message = processStartLocationRequest();
                return;
            case RESPOND_FAVOURITE_PLACES:
                message = getFavouritePlaces();
                break;
            default:
                message = processErrorMessage(msg.what);
        }

        try {
            // Make the RPC invocation
            msg.replyTo.send(message);
        } catch (RemoteException rme) {
            Log.d("Remote Service", "Invocation failed!");
        }
    }

    /**
     * Returns a message with start location.
     * @return Response message
     */
    private Message processStartLocationRequest() {
        return createMessage(RESPOND_START_LOCATION, getStartLocationWithCoordinates());
    }

    /**
     * Returns a message with data that tells the most likely destination calculated in Mobility Profile.
     * @return Response message
     */
  /*  private Message processDestinationRequest() {
        return createMessage(RESPOND_MOST_LIKELY_DESTINATION, mobilityProfile.getMostLikelyDestination(getStartLocation()));
    }
*/
    /**
     * Returns a message with data that tells the most likely destinations calculated in Mobility Profile.
     * @return Response message
     */
    private Message processDestinationRequest() {
        return createMessage(RESPOND_MOST_LIKELY_DESTINATION, mobilityProfile.getListOfMostLikelyDestinations(getStartLocation()));
    }

    /**
     * Processes new routes by adding them in calendarTags or RouteSearches.
     *
     * @param message Message with data that tells which destination the user inputted
     */
    private void processUsedRoute(Message message) {
        Bundle bundle = message.getData();
        String destination = bundle.getString(SEND_USED_DESTINATION+"");
        if (mobilityProfile.isCalendarDestination()) {
            CalendarTag calendarTag = new CalendarTag(mobilityProfile.getLatestDestination(), destination);
            calendarTagDao.insertCalendarTag(calendarTag);
        } else {
            if (visitDao.getLatestVisit() == null) {
                AddressConverter.convertToCoordinatesAndSave(destination, null, context);
            } else {
                AddressConverter.convertToCoordinatesAndSave(destination, visitDao.getLatestVisit(), context);
            }
        }
    }

    /**
     * Returns the start location a.k.a the last known location of user.
     *
     * @return Start location address
     */
    private String getStartLocation() {
        Visit lastKnownVisit = visitDao.getLatestVisit();
        if (lastKnownVisit == null) {
            // TODO something better
            return "None";
        } else {
            return lastKnownVisit.getOriginalLocation();
        }
    }

    /**
     * Return the start location with coordinates.
     * @return Start location address and coordinates
     */
    private String getStartLocationWithCoordinates() {
        Visit lastKnownVisit = visitDao.getLatestVisit();
        if (lastKnownVisit == null) {
            // TODO something better
            return "None";
        } else {
            return lastKnownVisit.getOriginalLocation() + "!" + lastKnownVisit.getLatitude() + "!" + lastKnownVisit.getLongitude();
        }
    }

    /**
     * Creates an error message.
     *
     * @param code Message code
     * @return Error message
     */
    private Message processErrorMessage(int code) {
        return createMessage(ERROR_UNKNOWN_CODE, code+"");
    }

    /**
     * Creates a message with the given code and info.
     *
     * @param code Message code
     * @param info Message information
     * @return Created message
     */
    private Message createMessage(int code, String info)  {
        // Setup the reply message
        Bundle bundle = new Bundle();
        bundle.putString(code+"", info);
        Message message = Message.obtain(null, code);
        message.setData(bundle);

        return message;
    }

    /**
     * Creates a message that has a list of strings as info.
     *
     * @param code Message code
     * @param info List of info strings
     * @return Created message
     */
    private Message createMessage(int code, ArrayList<String> info) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(code+"", info);
        Message message = Message.obtain(null, code);
        message.setData(bundle);

        return message;
    }

    /**
     * Returns a message that contains information of user's favorite places.
     *
     * @return User's favourite places as message
     */
    private Message getFavouritePlaces() {
        return createMessage(RESPOND_FAVOURITE_PLACES, favouritePlaceDao.getNamesOfFavouritePlaces());
    }
}
