package panto.technoevents.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import panto.technoevents.R;
import panto.technoevents.apimodels.djs.DjModel;
import panto.technoevents.db.RemoteDataBase;
import panto.technoevents.recyclerview.EventAdapter;
import panto.technoevents.viewmodel.FragmentsViewModel;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

public class EventsFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private static String artistImageUrl;
    private static String artistName;
    private EventAdapter eventAdapter;
    private FragmentsViewModel fragmentsViewModel;
    private DjModel djModel;

    public EventsFragment() {
    }

    static EventsFragment newInstance(@NonNull final DjModel djModel) {
        final Bundle bundle = new Bundle();
        EventsFragment fragment = new EventsFragment();
        bundle.putParcelable("DJ", djModel);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) djModel = getArguments().getParcelable("DJ");
        eventAdapter = new EventAdapter();
        fragmentsViewModel = ViewModelProviders.of(this).get(FragmentsViewModel.class);
        artistImageUrl = djModel.getImage();
        artistName = djModel.getName();
        fragmentsViewModel.loadEvents(djModel.getId());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String SHARED_PREFS_KEY = getString(R.string.preference_file_key);
        sharedPreferences = view.getContext().getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        ImageView eventArtistImageView = view.findViewById(R.id.event_list_artist_ImageView);
        TextView eventArtistNameTextView = view.findViewById(R.id.event_list_artist_name_textView);
        RecyclerView recyclerView = view.findViewById(R.id.event_list_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), VERTICAL, false));
        recyclerView.setAdapter(eventAdapter);
        Picasso.get()
          .load(artistImageUrl)
          .into(eventArtistImageView);
        eventArtistNameTextView.setText(artistName);
        fragmentsViewModel.djEvents.observe(this, events -> eventAdapter.setData(events));
        ToggleButton favoriteButton = view.findViewById(R.id.favorite_button);
        sharedPreferences.getBoolean("filled", false);
        favoriteButton.setChecked(sharedPreferences.getBoolean(djModel.getName(), false));
        favoriteButton.setOnClickListener(v -> {
            if (favoriteButton.isChecked()) {
                sharedPreferences.edit().putBoolean(djModel.getName(), true).apply();
                RemoteDataBase.getInstance().addFavorite(djModel);
            } else {
                sharedPreferences.edit().putBoolean(djModel.getName(), false).apply();
                RemoteDataBase.getInstance().removeFavorite(djModel);
            }
        });
    }
}
