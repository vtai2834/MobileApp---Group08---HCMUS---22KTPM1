package com.example.tiktok;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SearchScreen extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private static final String TAG = "SearchScreen";

    private AutoCompleteTextView searchEditText;
    private ImageButton btnBack;
    private ImageButton btnClear;
    private ImageButton btnVoiceSearch;
    private RecyclerView searchSuggestionsList;
    private RecyclerView searchResultsList;
    private SearchSuggestionsAdapter suggestionsAdapter;
    private SearchResultsAdapter resultsAdapter;
    private TextView noResultsText;
    private View youMightLikeSection;
    private TabLayout searchTabLayout;
    private RecommendedResultsAdapter recommendedAdapter;

    private DatabaseReference videosRef;
    private List<Video> searchResults = new ArrayList<>();
    private List<String> videoIds = new ArrayList<>();
    private List<RecommendedItem> recommendedItems = new ArrayList<>();

    public List<String> videoIdsItems;

    private String userID;
    private boolean isSearchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

//        userID = getIntent().getStringExtra("USER_ID");
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUserID();
//            userIdName = currentUser.getIdName();
        }

        // Get videos_list from HomeScreen
        videoIdsItems = getIntent().getStringArrayListExtra("VIDEOS_ARRAY_JSON");

        // Initialize Firebase
        videosRef = FirebaseDatabase.getInstance().getReference("videos");

        initViews();
        setupListeners();
        setupRecyclerViews();
        loadRecommendedItems();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        btnBack = findViewById(R.id.btnBack);
        btnClear = findViewById(R.id.btnClear);
        btnVoiceSearch = findViewById(R.id.btnVoiceSearch);
        searchSuggestionsList = findViewById(R.id.searchSuggestionsList);
        searchResultsList = findViewById(R.id.searchResultsList);
        noResultsText = findViewById(R.id.noResultsText);
        youMightLikeSection = findViewById(R.id.youMightLikeSection);
        searchTabLayout = findViewById(R.id.searchTabLayout);

        // Set IME options for search button on keyboard
        searchEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        searchEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);

        // Initially hide no results text and search tabs
        noResultsText.setVisibility(View.GONE);
        searchTabLayout.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(SearchScreen.this, HomeScreen.class);
//            intent.putExtra("USER_ID", userID);
            startActivity(intent);
            finish();
        });

        btnClear.setOnClickListener(v -> {
            searchEditText.setText("");
            btnClear.setVisibility(View.GONE);

            // Reset search mode
            isSearchMode = false;

            // Clear search results
            searchResults.clear();
            videoIds.clear();
            resultsAdapter.notifyDataSetChanged();

            // Show suggestions, hide results
            showSuggestionsAndRecommendations();
        });

        btnVoiceSearch.setOnClickListener(v -> {
            speak();
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                // Update search suggestions based on text
                updateSearchSuggestions(s.toString());

                // If search field is empty, reset search mode
                if (s.length() == 0) {
                    isSearchMode = false;
                    showSuggestionsAndRecommendations();
                } else if (!isSearchMode) {
                    // If not in search mode and text is entered, show suggestions
                    showSuggestionsAndRecommendations();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add listener for search button on keyboard
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    isSearchMode = true;
                    performSearch(query);
                }
                return true;
            }
            return false;
        });

        // Setup tab selection listener
        searchTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Filter results based on selected tab
                // For now, we'll just show all results regardless of tab
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Add item click listener for AutoCompleteTextView
        searchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String suggestion = (String) parent.getItemAtPosition(position);
                searchEditText.setText(suggestion);
                isSearchMode = true;
                performSearch(suggestion);
            }
        });
    }

    private void showSuggestionsAndRecommendations() {
        searchSuggestionsList.setVisibility(View.VISIBLE);
        youMightLikeSection.setVisibility(View.VISIBLE);
        searchResultsList.setVisibility(View.GONE);
        searchTabLayout.setVisibility(View.GONE);
        noResultsText.setVisibility(View.GONE);

        // Make sure we're showing the recommended adapter
        searchResultsList.setLayoutManager(new LinearLayoutManager(this));
        searchResultsList.setAdapter(recommendedAdapter);
    }

    private void showSearchResults() {
        searchSuggestionsList.setVisibility(View.GONE);
        youMightLikeSection.setVisibility(View.GONE);
        searchTabLayout.setVisibility(View.VISIBLE);

        if (searchResults.isEmpty()) {
            searchResultsList.setVisibility(View.GONE);
            noResultsText.setVisibility(View.VISIBLE);
        } else {
            searchResultsList.setVisibility(View.VISIBLE);
            noResultsText.setVisibility(View.GONE);

            // Make sure we're showing the results adapter
            searchResultsList.setLayoutManager(new GridLayoutManager(this, 2));
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) searchResultsList.getLayoutParams();
            layoutParams.topMargin = (int) (50 * getResources().getDisplayMetrics().density); // Chuyển đổi dp sang pixel nếu cần
            searchResultsList.setLayoutParams(layoutParams);
            searchResultsList.setAdapter(resultsAdapter);
            resultsAdapter.notifyDataSetChanged();
        }
    }

    private void speak() {
        // Intent to show speech to text dialog
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói để tìm kiếm...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Thiết bị của bạn không hỗ trợ nhận diện giọng nói", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                searchEditText.setText(spokenText);
                isSearchMode = true;
                performSearch(spokenText);
            }
        }
    }

    private void setupRecyclerViews() {
        // Setup suggestions RecyclerView
        suggestionsAdapter = new SearchSuggestionsAdapter(new ArrayList<>());
        searchSuggestionsList.setLayoutManager(new LinearLayoutManager(this));
        searchSuggestionsList.setAdapter(suggestionsAdapter);

        // Setup search results RecyclerView with GridLayoutManager (2 columns)
        resultsAdapter = new SearchResultsAdapter(searchResults, videoIds);
        searchResultsList.setLayoutManager(new GridLayoutManager(this, 2));
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) searchResultsList.getLayoutParams();
        layoutParams.topMargin = (int) (50 * getResources().getDisplayMetrics().density); // Chuyển đổi dp sang pixel nếu cần
        searchResultsList.setLayoutParams(layoutParams);

        // Setup Google search suggestions
//        GoogleSuggestionsAdapter googleSuggestionsAdapter = new GoogleSuggestionsAdapter(this);
//        searchEditText.setAdapter(googleSuggestionsAdapter);

        // Setup recommended items adapter
        recommendedAdapter = new RecommendedResultsAdapter(recommendedItems);

        // Initially show recommendations
        searchResultsList.setLayoutManager(new LinearLayoutManager(this));
        searchResultsList.setAdapter(recommendedAdapter);
    }

    private void updateSearchSuggestions(String query) {
        // If we have a query, fetch suggestions from Google
        if (!query.isEmpty()) {
            GoogleSuggestionsProvider.getSuggestions(query, new GoogleSuggestionsProvider.OnSuggestionsLoadedListener() {
                @Override
                public void onSuggestionsLoaded(List<String> suggestions) {
                    runOnUiThread(() -> {
                        suggestionsAdapter.updateSuggestions(suggestions);
                    });
                }

                @Override
                public void onError(Exception e) {
                    // If error, use local suggestions
                    runOnUiThread(() -> {
                        List<String> fallbackSuggestions = new ArrayList<>();
                        fallbackSuggestions.add(query + " bên cồn");
                        fallbackSuggestions.add(query + " tutorial");
                        fallbackSuggestions.add(query + " dance");
                        fallbackSuggestions.add(query + " trend");
                        fallbackSuggestions.add(query + " music");
                        suggestionsAdapter.updateSuggestions(fallbackSuggestions);
                    });
                }
            });
        } else {
            // If empty query, clear suggestions
            suggestionsAdapter.updateSuggestions(new ArrayList<>());
        }

        // Set click listener for suggestions
        suggestionsAdapter.setOnItemClickListener(suggestion -> {
            searchEditText.setText(suggestion);
            searchEditText.setSelection(suggestion.length());  // Move cursor to end
            isSearchMode = true;
            performSearch(suggestion);
        });
    }

    private void loadRecommendedItems() {
        // Fetch trending searches from Google
        GoogleSuggestionsProvider.getTrendingSearches(new GoogleSuggestionsProvider.OnSuggestionsLoadedListener() {
            @Override
            public void onSuggestionsLoaded(List<String> suggestions) {
                recommendedItems.clear();

                // Convert suggestions to RecommendedItem objects
                for (int i = 0; i < Math.min(suggestions.size(), 10); i++) {
                    String suggestion = suggestions.get(i);
                    boolean isHighlighted = i < 2; // Highlight first two items

                    recommendedItems.add(new RecommendedItem(
                            suggestion,
                            i % 3 == 0 ? "Vừa xem" : "", // Add "Vừa xem" to some items
                            isHighlighted,
                            "" // No thumbnail URL for now
                    ));
                }

                // If we got fewer than 5 suggestions, add some defaults
                if (recommendedItems.size() < 5) {
                    addDefaultRecommendedItems();
                }

                // Update the UI
                runOnUiThread(() -> {
                    recommendedAdapter.notifyDataSetChanged();
                    fetchThumbnailsForRecommendedItems();
                });
            }

            @Override
            public void onError(Exception e) {
                // If there's an error, use default items
                recommendedItems.clear();
                addDefaultRecommendedItems();

                // Update the UI
                runOnUiThread(() -> {
                    recommendedAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void addDefaultRecommendedItems() {
        recommendedItems.add(new RecommendedItem("Live Liên Quân Trực Tiếp", "Vừa xem", true, ""));
        recommendedItems.add(new RecommendedItem("fpt flash hôm nay trận 1", "", true, ""));
        recommendedItems.add(new RecommendedItem("fpt vs one star", "Vừa xem", false, ""));
        recommendedItems.add(new RecommendedItem("cao thủ liên quân official", "", false, ""));
        recommendedItems.add(new RecommendedItem("1s vs sgp mới nhất", "", false, ""));
    }

    private void fetchThumbnailsForRecommendedItems() {
        for (RecommendedItem item : recommendedItems) {
            // Search for videos matching the title to get a relevant thumbnail
            videosRef.orderByChild("title")
                    .startAt(item.getTitle().toLowerCase())
                    .endAt(item.getTitle().toLowerCase() + "\uf8ff")
                    .limitToFirst(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                // Get the first matching video
                                DataSnapshot videoSnapshot = dataSnapshot.getChildren().iterator().next();

                                // Check if it has a thumbnail
                                if (videoSnapshot.hasChild("thumbnailUrl")) {
                                    String thumbnailUrl = videoSnapshot.child("thumbnailUrl").getValue(String.class);
                                    if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                                        // Update the item with the thumbnail URL
                                        item.setThumbnailUrl(thumbnailUrl);

                                        // Update the UI
                                        runOnUiThread(() -> {
                                            recommendedAdapter.notifyDataSetChanged();
                                        });
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Error fetching thumbnails: " + databaseError.getMessage());
                        }
                    });
        }
    }

    private void performSearch(String query) {
        Log.d(TAG, "Performing search with query: " + query);

        // Clear previous results
        searchResults.clear();
        videoIds.clear();

        // Search in the entire video list
        videosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String queryLower = query.toLowerCase();

                for (DataSnapshot videoSnapshot : dataSnapshot.getChildren()) {
                    String videoId = videoSnapshot.getKey();

                    // Check all 3 fields: title, username, music
                    String title = videoSnapshot.child("title").getValue(String.class);
                    String username = videoSnapshot.child("username").getValue(String.class);
                    String music = videoSnapshot.child("music").getValue(String.class);

                    // Convert to lowercase for case-insensitive search
                    title = (title != null) ? title.toLowerCase() : "";
                    username = (username != null) ? username.toLowerCase() : "";
                    music = (music != null) ? music.toLowerCase() : "";

                    // Check if query appears in any field
                    if (title.contains(queryLower) ||
                            username.contains(queryLower) ||
                            music.contains(queryLower)) {

                        // Create Video object and add to results
                        Video video = createVideoFromSnapshot(videoSnapshot);
                        if (video != null) {
                            searchResults.add(video);
                            videoIds.add(videoId);
                        }
                    }
                }

                // Update UI with search results
                runOnUiThread(() -> {
                    showSearchResults();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase query error: " + databaseError.getMessage());
                Toast.makeText(SearchScreen.this, "Lỗi tìm kiếm: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                runOnUiThread(() -> {
                    showSearchResults();
                });
            }
        });
    }

    private Video createVideoFromSnapshot(DataSnapshot snapshot) {
        try {
            String videoId = snapshot.getKey(); // Get the video ID from the snapshot

            String title = snapshot.child("title").getValue(String.class);
            String username = snapshot.child("username").getValue(String.class);
            String music = snapshot.child("music").getValue(String.class);
            String videoUri = snapshot.child("videoUri").getValue(String.class);
            String likes = snapshot.child("likes").getValue(String.class);
            String comments = snapshot.child("comments").getValue(String.class);
            String thumbnailUrl = "";

            // Check if thumbnailUrl exists
            if (snapshot.hasChild("thumbnailUrl") &&
                    snapshot.child("thumbnailUrl").getValue(String.class) != null &&
                    !snapshot.child("thumbnailUrl").getValue(String.class).isEmpty()) {

                thumbnailUrl = snapshot.child("thumbnailUrl").getValue(String.class);
            } else {
                // No thumbnail exists -> generate one from videoUri and save to db
                if (videoUri != null && !videoUri.isEmpty()) {
                    // Create a final copy of videoId for use in the callback
                    final String finalVideoId = videoId;

                    ThumbnailGenerator.generateAndUploadThumbnail(
                            getApplicationContext(),
                            videoUri,
                            new ThumbnailGenerator.ThumbnailCallback() {
                                @Override
                                public void onThumbnailGenerated(String generatedThumbnailUrl) {
                                    // Update the video with the new thumbnail URL
                                    videosRef.child(finalVideoId).child("thumbnailUrl").setValue(generatedThumbnailUrl);
                                    Log.d("ThumbnailUpdate", "Updated thumbnail for video: " + finalVideoId);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("ThumbnailUpdate", "Error updating thumbnail for video " +
                                            finalVideoId + ": " + e.getMessage());
                                }
                            }
                    );
                }
            }

            Video video = new Video();
            video.setTitle(title);
            video.setUsername(username);
            video.setMusic(music);
            video.setVideoUri(videoUri);
            video.setLikes(likes);
            video.setComments(comments);
            video.setThumbnailUrl(thumbnailUrl);
            return video;
        } catch (Exception e) {
            Log.e(TAG, "Error creating video from snapshot: " + e.getMessage());
            return null;
        }
    }

    // Model class for recommended items
    private static class RecommendedItem {
        private String title;
        private String subtitle;
        private boolean isHighlighted;
        private String thumbnailUrl;

        public RecommendedItem(String title, String subtitle, boolean isHighlighted, String thumbnailUrl) {
            this.title = title;
            this.subtitle = subtitle;
            this.isHighlighted = isHighlighted;
            this.thumbnailUrl = thumbnailUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public boolean isHighlighted() {
            return isHighlighted;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }

    // Adapter for recommended items
    private class RecommendedResultsAdapter extends RecyclerView.Adapter<RecommendedResultsAdapter.ViewHolder> {
        private List<RecommendedItem> items;

        public RecommendedResultsAdapter(List<RecommendedItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_might_light, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RecommendedItem item = items.get(position);

            holder.titleText.setText(item.getTitle());
            holder.subtitleText.setText(item.getSubtitle());

            // Set color dot based on highlight status
            holder.colorDot.setBackgroundResource(item.isHighlighted() ?
                    R.color.colorAccent : android.R.color.darker_gray);

            // Load thumbnail image
            if (item.getThumbnailUrl() != null && !item.getThumbnailUrl().isEmpty()) {
                Glide.with(holder.thumbnailImage.getContext())
                        .load(item.getThumbnailUrl())
                        .placeholder(R.drawable.ic_video_placeholder)
                        .into(holder.thumbnailImage);
            }

            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                searchEditText.setText(item.getTitle());
                isSearchMode = true;
                performSearch(item.getTitle());
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View colorDot;
            TextView titleText, subtitleText;
            ImageView thumbnailImage;

            ViewHolder(View itemView) {
                super(itemView);
                colorDot = itemView.findViewById(R.id.colorDot);
                titleText = itemView.findViewById(R.id.titleText);
                subtitleText = itemView.findViewById(R.id.subtitleText);
                thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
            }
        }
    }

    // Search Results Adapter (Grid Layout)
    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
        private List<Video> videos;
        private List<String> videoIds;

        public SearchResultsAdapter(List<Video> videos, List<String> videoIds) {
            this.videos = videos;
            this.videoIds = videoIds;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Video video = videos.get(position);
            String videoId = videoIds.get(position);

            holder.titleText.setText(video.getTitle());
            holder.channelName.setText(video.getUsername());
            holder.likeCount.setText(video.getLikes());

            // Set time indicator (dummy data for now)
            holder.timeIndicator.setText("2 ngày trước");

            // Load thumbnail image
            if (video.getThumbnailUrl() != null && !video.getThumbnailUrl().isEmpty()) {
                Glide.with(holder.thumbnailImage.getContext())
                        .load(video.getThumbnailUrl())
                        .placeholder(R.drawable.ic_video_placeholder)
                        .into(holder.thumbnailImage);
            }

            // Load channel avatar (using username as placeholder)
            Glide.with(holder.channelAvatar.getContext())
                    .load(R.drawable.default_profile)
                    .into(holder.channelAvatar);

            // Set click listener to open the video
            holder.itemView.setOnClickListener(v -> {
                // Open video in player activity or return to home screen with position
                int pos = videoIdsItems.indexOf(videoId);

                if (pos != -1) {
                    // Send position back to HomeScreen
                    Intent intent = new Intent(SearchScreen.this, HomeScreen.class);
                    intent.putExtra("SEARCH_VIDEO_POSITION", pos);
//                    intent.putExtra("USER_ID", userID);
                    startActivity(intent);
                    finish();
                } else {
                    // Case when videoId not found in the list
                    Log.e(TAG, "VideoId " + videoId + " not found in videoIdsFromHome");
                }
            });
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView thumbnailImage, channelAvatar;
            TextView titleText, channelName, likeCount, timeIndicator;

            ViewHolder(View itemView) {
                super(itemView);
                thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
                titleText = itemView.findViewById(R.id.titleText);
                channelName = itemView.findViewById(R.id.channelName);
                channelAvatar = itemView.findViewById(R.id.channelAvatar);
                likeCount = itemView.findViewById(R.id.likeCount);
                timeIndicator = itemView.findViewById(R.id.timeIndicator);
            }
        }
    }

    // Search Suggestions Adapter
    public static class SearchSuggestionsAdapter extends RecyclerView.Adapter<SearchSuggestionsAdapter.ViewHolder> {
        private List<String> suggestions;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(String suggestion);
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        public SearchSuggestionsAdapter(List<String> suggestions) {
            this.suggestions = suggestions;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_suggestion, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String suggestion = suggestions.get(position);
            holder.suggestionText.setText(suggestion);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(suggestion);
                }
            });

            holder.clearButton.setOnClickListener(v -> {
                // Remove this suggestion
                suggestions.remove(position);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return suggestions.size();
        }

        public void updateSuggestions(List<String> newSuggestions) {
            this.suggestions = newSuggestions;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView suggestionText;
            ImageButton clearButton;

            ViewHolder(View itemView) {
                super(itemView);
                suggestionText = itemView.findViewById(R.id.suggestionText);
                clearButton = itemView.findViewById(R.id.clearButton);
            }
        }
    }

    // Google Suggestions Provider
    public static class GoogleSuggestionsProvider {
        private static final String TAG = "GoogleSuggestions";
        private static final String SUGGESTIONS_URL = "https://suggestqueries.google.com/complete/search?client=firefox&q=";

        public interface OnSuggestionsLoadedListener {
            void onSuggestionsLoaded(List<String> suggestions);
            void onError(Exception e);
        }

        /**
         * Get search suggestions from Google for a given query
         *
         * @param query The search query
         * @param listener Callback to receive the suggestions or error
         */
        public static void getSuggestions(String query, OnSuggestionsLoadedListener listener) {
            new FetchSuggestionsTask(listener).execute(query);
        }

        /**
         * Get trending searches from Google (empty query)
         *
         * @param listener Callback to receive the trending searches or error
         */
        public static void getTrendingSearches(OnSuggestionsLoadedListener listener) {
            // For trending searches, we can use popular terms
            new FetchSuggestionsTask(listener).execute("game", "music", "video", "trending");
        }

        private static class FetchSuggestionsTask extends AsyncTask<String, Void, List<String>> {
            private OnSuggestionsLoadedListener listener;
            private Exception error;

            public FetchSuggestionsTask(OnSuggestionsLoadedListener listener) {
                this.listener = listener;
            }

            @Override
            protected List<String> doInBackground(String... params) {
                List<String> suggestions = new ArrayList<>();

                try {
                    // For trending searches, try multiple queries
                    if (params.length > 1) {
                        // If multiple params, we're looking for trending searches
                        for (String param : params) {
                            try {
                                String encodedQuery = URLEncoder.encode(param, "UTF-8");
                                URL url = new URL(SUGGESTIONS_URL + encodedQuery);

                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");

                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                StringBuilder response = new StringBuilder();
                                String line;

                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }

                                reader.close();

                                JSONArray jsonArray = new JSONArray(response.toString());
                                JSONArray suggestionsArray = jsonArray.getJSONArray(1);

                                // Add up to 3 suggestions from each query
                                for (int i = 0; i < Math.min(suggestionsArray.length(), 3); i++) {
                                    suggestions.add(suggestionsArray.getString(i));
                                }

                                // If we have enough suggestions, break
                                if (suggestions.size() >= 10) {
                                    break;
                                }
                            } catch (Exception e) {
                                // Continue with next param if one fails
                                continue;
                            }
                        }
                    } else if (params.length == 1) {
                        // Regular query suggestion
                        String query = params[0];
                        String encodedQuery = URLEncoder.encode(query, "UTF-8");
                        URL url = new URL(SUGGESTIONS_URL + encodedQuery);

                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        reader.close();

                        JSONArray jsonArray = new JSONArray(response.toString());
                        JSONArray suggestionsArray = jsonArray.getJSONArray(1);

                        for (int i = 0; i < suggestionsArray.length(); i++) {
                            suggestions.add(suggestionsArray.getString(i));
                        }
                    }

                    return suggestions;

                } catch (Exception e) {
                    Log.e(TAG, "Error fetching suggestions: " + e.getMessage());
                    error = e;
                    return suggestions;
                }
            }

            @Override
            protected void onPostExecute(List<String> suggestions) {
                if (error != null) {
                    listener.onError(error);
                } else {
                    listener.onSuggestionsLoaded(suggestions);
                }
            }
        }
    }

    // Google Suggestions Adapter for AutoCompleteTextView
    public class GoogleSuggestionsAdapter extends ArrayAdapter<String> implements Filterable {
        private List<String> suggestions = new ArrayList<>();

        public GoogleSuggestionsAdapter(Context context) {
            super(context, android.R.layout.simple_dropdown_item_1line);
        }

        @Override
        public int getCount() {
            return suggestions.size();
        }

        @Override
        public String getItem(int position) {
            return suggestions.get(position);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    if (constraint != null) {
                        // We'll return an empty list here and update it asynchronously
                        results.values = new ArrayList<String>();
                        results.count = 0;

                        // Start the async request
                        final String query = constraint.toString();
                        if (!query.isEmpty()) {
                            GoogleSuggestionsProvider.getSuggestions(query,
                                    new GoogleSuggestionsProvider.OnSuggestionsLoadedListener() {
                                        @Override
                                        public void onSuggestionsLoaded(List<String> loadedSuggestions) {
                                            // Update the suggestions on the UI thread
                                            suggestions = loadedSuggestions;
                                            notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            // Handle error - use empty list
                                            suggestions = new ArrayList<>();
                                            notifyDataSetChanged();
                                        }
                                    }
                            );
                        }
                    }

                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    // We're handling this in the onSuggestionsLoaded callback
                    notifyDataSetChanged();
                }
            };
        }
    }
}