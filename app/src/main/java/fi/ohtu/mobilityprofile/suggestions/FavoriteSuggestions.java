package fi.ohtu.mobilityprofile.suggestions;

import java.util.ArrayList;
import java.util.List;

import fi.ohtu.mobilityprofile.data.FavouritePlaceDao;
import fi.ohtu.mobilityprofile.domain.FavouritePlace;
import fi.ohtu.mobilityprofile.domain.Place;

/**
 * This class creates suggestions based on favorites the user has saved.
 */
public class FavoriteSuggestions implements SuggestionSource {
    private FavouritePlaceDao favouritePlaceDao;

    /**
     * Creates the FavoriteSuggestions
     *
     * @param favouritePlaceDao DAO for favorite places
     */
    public FavoriteSuggestions(FavouritePlaceDao favouritePlaceDao) {
        this.favouritePlaceDao = favouritePlaceDao;
    }

    /**
     * Returns three most used favorite places.
     *
     * @param startLocation Location where the user starts the journey
     * @return Three favorite places
     */
    @Override
    public List<Suggestion> getSuggestions(Place startLocation) {
        List<Suggestion> suggestions = new ArrayList<>();

        for (FavouritePlace favouritePlace : favouritePlaceDao.FindAmountOrderByCounter(3)) {
            Suggestion suggestion = new Suggestion(favouritePlace.getAddress(), SuggestionAccuracy.MODERATE, FAVORITE_SUGGESTION);
            suggestions.add(suggestion);
        }

        return suggestions;
    }
}