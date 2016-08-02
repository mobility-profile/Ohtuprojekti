package fi.ohtu.mobilityprofile.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

import fi.ohtu.mobilityprofile.R;
import fi.ohtu.mobilityprofile.domain.FavouritePlace;
import fi.ohtu.mobilityprofile.domain.SignificantPlace;
import fi.ohtu.mobilityprofile.ui.list_adapters.FavouritesListAdapter;
import fi.ohtu.mobilityprofile.ui.list_adapters.SignificantsListAdapter;

/**
 * The class creates a component called FavouritesFragment.
 *
 * FavouritesFragment handles everything concerning the FAVOURITES tab in the UI.
 */
public class FavouritesFragment extends Fragment {

    /**
     * The title of the fragment.
     */
    private static final String title = "FAVOURITES";

    /**
     * The position of the fragment in the "queue" of all fragments.
     */
    private static final int page = 2;
    private Context context;

    /**
     * Creates a new instance of FavouritesFragment.
     *
     * @return favourites fragment
     */
    public static FavouritesFragment newInstance() {
        FavouritesFragment favouritesFragment = new FavouritesFragment();
        Bundle args = new Bundle();
        args.putInt("page", page);
        args.putString("title", title);
        favouritesFragment.setArguments(args);
        return favouritesFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {

        setFavouritesListView(view);
        setSuggestionsListView(view);
    }

    private void setFavouritesListView(View view) {
        List<FavouritePlace> favouritePlaces = getFavouritePlaces();

        final FavouritesListAdapter adapter = new FavouritesListAdapter(context, R.layout.favourites_list_item, favouritePlaces, this);
        ListView listView = (ListView) view.findViewById(R.id.favourites_listView);
        listView.setAdapter(adapter);

        addButtonListener(view, adapter);
    }

    private void setSuggestionsListView(View view) {
        List<SignificantPlace> significantPlaces = getSignificantPlaces();

        final SignificantsListAdapter adapter = new SignificantsListAdapter(context, R.layout.favourites_list_item, significantPlaces, this);
        ListView listView = (ListView) view.findViewById(R.id.significants_listView);
        listView.setAdapter(adapter);
        listView.setVisibility(View.GONE);

        switchListener(view, listView);
    }

    private List<SignificantPlace> getSignificantPlaces() {
        List<SignificantPlace> significantPlaces = new ArrayList<>();
        try {
            significantPlaces = SignificantPlace.listAll(SignificantPlace.class);
            List<SignificantPlace> remove = new ArrayList<>();

            for (SignificantPlace s : significantPlaces) {
                if (s.isFavourite() || s.isRemoved()) {
                    remove.add(s);
                }
            }

            significantPlaces.removeAll(remove);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return significantPlaces;
    }

    private List<FavouritePlace> getFavouritePlaces() {
        List<FavouritePlace> favouritePlaces = new ArrayList<>();
        try {
            favouritePlaces = FavouritePlace.listAll(FavouritePlace.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return favouritePlaces;
    }


    /**
     * Listener for see suggestions switch
     * @param view view inside the fragment
     * @param listView the list of suggestions
     */
    private void switchListener(View view, final ListView listView) {
        Switch seeSuggestions = (Switch) view.findViewById(R.id.switchSuggestions);

        seeSuggestions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listView.setVisibility(View.VISIBLE);
                } else {
                    listView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favourites_fragment, container, false);
    }

    /**
     * Listener to add favourite place button
     * @param view view inside the fragment
     * @param adapter FavouritesListAdapter
     */
    private void addButtonListener(final View view, final FavouritesListAdapter adapter) {

        Button button = (Button) view.findViewById(R.id.add_favourite_button);
        final EditText addFavouriteName = (EditText) view.findViewById(R.id.add_favourite_name);
        final EditText addFavouriteAddress = (EditText) view.findViewById(R.id.add_favourite_address);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (!addFavouriteName.getText().toString().equals("") && !addFavouriteAddress.getText().toString().equals("")) {
                    FavouritePlace fav = new FavouritePlace(addFavouriteName.getText().toString(), addFavouriteAddress.getText().toString());
                    fav.save();
                    updateView(adapter);
                    addFavouriteName.setText("");
                    addFavouriteAddress.setText("");
                }
            }

        });

    }

    /**
     * Updates the favourites fragment view
     * @param adapter FavouritesListAdapter
     */
    private void updateView(FavouritesListAdapter adapter) {
        FragmentTransaction tr = getFragmentManager().beginTransaction();
        tr.detach(this);
        tr.attach(this);
        tr.commit();
        adapter.notifyDataSetChanged();
    }

}