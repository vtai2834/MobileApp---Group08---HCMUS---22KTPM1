package com.example.tiktok;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class SearchScreen extends AppCompatActivity {

    public class SearchSuggestionsAdapter extends RecyclerView.Adapter<SearchSuggestionsAdapter.ViewHolder> {
        private List<String> suggestions;

        public SearchSuggestionsAdapter(List<String> suggestions) {
            this.suggestions = suggestions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String suggestion = suggestions.get(position);
            holder.textView.setText(suggestion);
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
    private EditText searchEditText;
    private ImageButton btnBack;
    private ImageButton btnClear;
    private RecyclerView searchSuggestionsList;
    private SearchSuggestionsAdapter suggestionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        initViews();
        setupListeners();
        setupRecyclerView();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        btnBack = findViewById(R.id.btnBack);
        btnClear = findViewById(R.id.btnClear);
        searchSuggestionsList = findViewById(R.id.searchSuggestionsList);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchScreen.this, HomeScreen.class);
                startActivity(intent);
            }
        });

        btnClear.setOnClickListener(v -> {
            searchEditText.setText("");
            btnClear.setVisibility(View.GONE);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                updateSearchSuggestions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        suggestionsAdapter = new SearchSuggestionsAdapter(new ArrayList<>());
        searchSuggestionsList.setLayoutManager(new LinearLayoutManager(this));
        searchSuggestionsList.setAdapter(suggestionsAdapter);
    }

    private void updateSearchSuggestions(String query) {
        // Here you would typically:
        // 1. Call your API or database to get search suggestions
        // 2. Update the adapter with new suggestions
        // For demonstration, we'll just update with dummy data
        List<String> suggestions = new ArrayList<>();
        if (!query.isEmpty()) {
            suggestions.add(query + " có lạnh không");
            suggestions.add(query + " mặc gì nam");
            suggestions.add(query + " ngày bao nhiêu");
            suggestions.add(query + " hợp màu gì");
            suggestions.add(query + " con giáp gì");
        }
        suggestionsAdapter.updateSuggestions(suggestions);
    }
}