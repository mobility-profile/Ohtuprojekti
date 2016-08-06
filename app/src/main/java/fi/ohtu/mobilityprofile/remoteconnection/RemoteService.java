package fi.ohtu.mobilityprofile.remoteconnection;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;

import java.util.ArrayList;
import java.util.List;

import fi.ohtu.mobilityprofile.CalendarConnection;
import fi.ohtu.mobilityprofile.suggestions.sources.CalendarSuggestions;
import fi.ohtu.mobilityprofile.suggestions.DestinationLogic;
import fi.ohtu.mobilityprofile.MainActivity;
import fi.ohtu.mobilityprofile.suggestions.sources.FavoriteSuggestions;
import fi.ohtu.mobilityprofile.suggestions.sources.RouteSuggestions;
import fi.ohtu.mobilityprofile.suggestions.SuggestionSource;
import fi.ohtu.mobilityprofile.suggestions.sources.VisitSuggestions;


/**
 * Used to enable cross-app communication.
 */
public class RemoteService extends Service {
    private Messenger messenger;

    @Override
    public IBinder onBind(Intent intent) {
        // If there is a security problem, we shouldn't allow any application to get information
        // from us. Check SecurityCheck.java for more information.
        if (!SecurityCheck.securityCheckOk(getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE))) {
            return null;
        }

        synchronized (RemoteService.class) {
            if (messenger == null) {

                List<SuggestionSource> suggestionSources = new ArrayList<>();
                suggestionSources.add(new CalendarSuggestions(new CalendarConnection(this)));

                suggestionSources.add(new VisitSuggestions());

                suggestionSources.add(new RouteSuggestions());
                suggestionSources.add(new FavoriteSuggestions());
                DestinationLogic destinationLogic = new DestinationLogic(suggestionSources);

                messenger = new Messenger(new RequestHandler(this, destinationLogic));
            }
        }

        return messenger.getBinder();
    }
}
