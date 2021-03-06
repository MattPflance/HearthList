package com.mattpflance.hearthlist;

import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mattpflance.hearthlist.data.HearthListContract;
import com.mattpflance.hearthlist.dialogs.CardFiltersDialog;
import com.mattpflance.hearthlist.models.Card;

import java.util.ArrayList;

/**
 * Fragment that displays HearthStone cards
 */
public class CardsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // For selection args
    public static final int ARGS_MIN_MANA = 0;
    public static final int ARGS_MAX_MANA = 1;
    public static final int ARGS_CLASS = 2;
    public static final int ARGS_CARD_SET = 3;
    public static final int ARGS_MECHANICS = 4;

    private static final int CARD_LOADER = 0;

    private CardsAdapter mCardsAdapter;
    private static ArrayList<String> mSelectionArgs;

    RecyclerView mRecyclerView;
    TextView mEmptyView;
    FloatingActionButton mFilterFab;

    public CardsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static CardsFragment newInstance(int minMana, int maxMana) {
        mSelectionArgs = new ArrayList<>();
        mSelectionArgs.add(ARGS_MIN_MANA, minMana+"");
        mSelectionArgs.add(ARGS_MAX_MANA, maxMana+"");
        mSelectionArgs.add(ARGS_CLASS, null);
        mSelectionArgs.add(ARGS_CARD_SET, null);
        mSelectionArgs.add(ARGS_MECHANICS, null);
        return new CardsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cards, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.cards_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        mEmptyView = (TextView) view.findViewById(R.id.cards_recycler_empty_view);

        mCardsAdapter = new CardsAdapter(getActivity(), new CardsAdapter.CardsAdapterOnClickHandler() {
            @Override
            public void onClick(Cursor cursor) {
                Intent intent = new Intent(getActivity(), CardDetailsActivity.class);
                Card card = (cursor != null) ? new Card(cursor) : null;
                intent.putExtra(CardDetailsActivity.CARD_ARG_ID, card);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Start Activity with Transitions
                    Bundle bundle = ActivityOptions
                            .makeSceneTransitionAnimation(getActivity())
                            .toBundle();
                    startActivity(intent, bundle);
                } else {
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.top_to_bottom, R.anim.right_to_left);
                }
            }
        }, mEmptyView);

        mRecyclerView.setAdapter(mCardsAdapter);

        mFilterFab = (FloatingActionButton) view.findViewById(R.id.cards_fab);
        mFilterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CardFiltersDialog.FILTER_CODE) {
            mSelectionArgs = data.getStringArrayListExtra(CardFiltersDialog.SELECTION_ARG_KEY);
            getLoaderManager().restartLoader(CARD_LOADER, null, this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // Init the loader
        getLoaderManager().initLoader(CARD_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder = HearthListContract.CardEntry.COLUMN_COST + " ASC";
        return new CursorLoader(
                getActivity(),
                HearthListContract.CardEntry.CONTENT_URI,
                Card.CARD_COLUMNS,
                null,                                       // Implemented in ContentProvider
                mSelectionArgs.toArray(new String[mSelectionArgs.size()]),
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCardsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCardsAdapter.swapCursor(null);
    }

    private void showDialog() {
        CardFiltersDialog dialog = CardFiltersDialog.newInstance(mSelectionArgs);
        dialog.setTargetFragment(this, CardFiltersDialog.FILTER_CODE);
        dialog.show(getFragmentManager(), null);
    }
}
