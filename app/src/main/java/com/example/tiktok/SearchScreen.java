package com.example.tiktok;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SearchScreen extends AppCompatActivity {

    private EditText searchEditText;
    private ImageButton btnBack;
    private ImageButton btnClear;
    private RecyclerView searchSuggestionsList;
    private RecyclerView searchResultsList;
    private SearchSuggestionsAdapter suggestionsAdapter;
    private SearchResultsAdapter resultsAdapter;
    private TextView noResultsText;

    private DatabaseReference videosRef;
    private List<Video> searchResults = new ArrayList<>();
    private List<String> videoIds = new ArrayList<>();

    public List<String> videoIdsItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        //Lấy videos_list từ HomeScreen -> trả về pos video sẽ tìm dc result:
        String videoItemsJson = getIntent().getStringExtra("VIDEOS_ARRAY_JSON");
//        Gson gson = new Gson();
//        videoIdsItems = gson.fromJson(videoItemsJson, new TypeToken<List<String>>(){}.getType());
        videoIdsItems = getIntent().getStringArrayListExtra("VIDEOS_ARRAY_JSON");

        // Initialize Firebase
        videosRef = FirebaseDatabase.getInstance().getReference("videos");

        initViews();
        setupListeners();
        setupRecyclerViews();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        btnBack = findViewById(R.id.btnBack);
        btnClear = findViewById(R.id.btnClear);
        searchSuggestionsList = findViewById(R.id.searchSuggestionsList);
        searchResultsList = findViewById(R.id.searchResultsList);
        noResultsText = findViewById(R.id.noResultsText);

        // Set IME options để hiển thị nút tìm kiếm trên bàn phím
        searchEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        searchEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);

        // Initially hide no results text
        noResultsText.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchScreen.this, HomeScreen.class);
                startActivity(intent);
                finish();
            }
        });

        btnClear.setOnClickListener(v -> {
            searchEditText.setText("");
            btnClear.setVisibility(View.GONE);
            // Clear search results
            searchResults.clear();
            videoIds.clear();
            resultsAdapter.notifyDataSetChanged();

            // Show suggestions, hide results
            searchSuggestionsList.setVisibility(View.VISIBLE);
            searchResultsList.setVisibility(View.GONE);
            noResultsText.setVisibility(View.GONE);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                updateSearchSuggestions(s.toString());

                // If search field is empty, hide results
                if (s.length() == 0) {
                    searchSuggestionsList.setVisibility(View.VISIBLE);
                    searchResultsList.setVisibility(View.GONE);
                    noResultsText.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Thêm listener cho nút tìm kiếm trên bàn phím
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerViews() {
        // Setup suggestions RecyclerView
        suggestionsAdapter = new SearchSuggestionsAdapter(new ArrayList<>());
        searchSuggestionsList.setLayoutManager(new LinearLayoutManager(this));
        searchSuggestionsList.setAdapter(suggestionsAdapter);

        // Setup search results RecyclerView
        resultsAdapter = new SearchResultsAdapter(searchResults, videoIds);
        searchResultsList.setLayoutManager(new LinearLayoutManager(this));
        searchResultsList.setAdapter(resultsAdapter);
        searchResultsList.setVisibility(View.GONE);  // Initially hidden
    }

    private void updateSearchSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        if (!query.isEmpty()) {
            suggestions.add(query + " bên cồn");
            suggestions.add(query + " tutorial");
            suggestions.add(query + " dance");
            suggestions.add(query + " trend");
            suggestions.add(query + " music");
        }
        suggestionsAdapter.updateSuggestions(suggestions);

        // Set click listener for suggestions
        suggestionsAdapter.setOnItemClickListener(new SearchSuggestionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String suggestion) {
                searchEditText.setText(suggestion);
                searchEditText.setSelection(suggestion.length());  // Move cursor to end
                performSearch(suggestion);
            }
        });
    }


    private void performSearch(String query) {
        Log.d("SearchScreen", "Performing search with query: " + query);

        // Clear previous results
        searchResults.clear();
        videoIds.clear();

        // Hide suggestions, show results list
        searchSuggestionsList.setVisibility(View.GONE);
        searchResultsList.setVisibility(View.VISIBLE);

        // Tìm kiếm trong toàn bộ danh sách video
        videosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String queryLower = query.toLowerCase();

                for (DataSnapshot videoSnapshot : dataSnapshot.getChildren()) {
                    String videoId = videoSnapshot.getKey();

                    // Kiểm tra tất cả 3 trường: title, username, music
                    String title = videoSnapshot.child("title").getValue(String.class);
                    String username = videoSnapshot.child("username").getValue(String.class);
                    String music = videoSnapshot.child("music").getValue(String.class);

                    // Chuyển đổi thành lowercase để tìm kiếm không phân biệt chữ hoa/thường
                    title = (title != null) ? title.toLowerCase() : "";
                    username = (username != null) ? username.toLowerCase() : "";
                    music = (music != null) ? music.toLowerCase() : "";

                    // Kiểm tra xem query có xuất hiện trong bất kỳ trường nào không
                    if (title.contains(queryLower) ||
                            username.contains(queryLower) ||
                            music.contains(queryLower)) {

                        // Tạo đối tượng Video và thêm vào kết quả
                        Video video = createVideoFromSnapshot(videoSnapshot);
                        if (video != null) {
                            searchResults.add(video);
                            videoIds.add(videoId);
                        }
                    }
                }

                // Update UI with search results
                updateSearchResultsUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("SearchScreen", "Firebase query error: " + databaseError.getMessage());
                Toast.makeText(SearchScreen.this, "Lỗi tìm kiếm: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                updateSearchResultsUI();
            }
        });
    }


    private Video createVideoFromSnapshot(DataSnapshot snapshot) {
        try {
            String title = snapshot.child("title").getValue(String.class);
            String username = snapshot.child("username").getValue(String.class);
            String music = snapshot.child("music").getValue(String.class);
            String videoUri = snapshot.child("videoUri").getValue(String.class);
            String likes = snapshot.child("likes").getValue(String.class);
            String comments = snapshot.child("comments").getValue(String.class);

            Video video = new Video();
            video.setTitle(title);
            video.setUsername(username);
            video.setMusic(music);
            video.setVideoUri(videoUri);
            video.setLikes(likes);
            video.setComments(comments);
            return video;
        } catch (Exception e) {
            Log.e("SearchScreen", "Error creating video from snapshot: " + e.getMessage());
            return null;
        }
    }

    private void updateSearchResultsUI() {
        // Hide loading indicator
        // loadingIndicator.setVisibility(View.GONE);

        if (searchResults.isEmpty()) {
            // Show no results message
            noResultsText.setVisibility(View.VISIBLE);
            searchResultsList.setVisibility(View.GONE);
        } else {
            // Show results
            noResultsText.setVisibility(View.GONE);
            searchResultsList.setVisibility(View.VISIBLE);
            resultsAdapter.notifyDataSetChanged();
        }
    }

    // Search Results Adapter
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

            holder.videoTitle.setText(video.getTitle());
            holder.videoUsername.setText(video.getUsername());
            holder.videoMusic.setText(video.getMusic());
            holder.videoLikes.setText(video.getLikes() + " lượt thích");

            // Set click listener to open the video
            holder.itemView.setOnClickListener(v -> {
                // Open video in player activity or return to home screen with position
                int pos = videoIdsItems.indexOf(videoId);

                if (pos != -1) {
                    // Gửi position về HomeScreen thay vì videoId
                    Intent intent = new Intent(SearchScreen.this, HomeScreen.class);
                    intent.putExtra("SEARCH_VIDEO_POSITION", pos);  // Thêm pos vào Intent
                    startActivity(intent);
                    finish();
                } else {
                    // Trường hợp không tìm thấy videoId trong danh sách
                    Log.e("SearchScreen", "VideoId " + videoId + " not found in videoIdsFromHome");
                }
            });
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView videoTitle, videoUsername, videoMusic, videoLikes;

            ViewHolder(View itemView) {
                super(itemView);
                videoTitle = itemView.findViewById(R.id.videoTitle);
                videoUsername = itemView.findViewById(R.id.videoUsername);
                videoMusic = itemView.findViewById(R.id.videoMusic);
                videoLikes = itemView.findViewById(R.id.videoLikes);
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
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String suggestion = suggestions.get(position);
            holder.textView.setText(suggestion);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(suggestion);
                }
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
            TextView textView;

            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}