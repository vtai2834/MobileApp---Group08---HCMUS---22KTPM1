package com.example.tiktok;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileScreen extends AppCompatActivity {

    private static final String TAG = "ProfileScreen";

    private RecyclerView recyclerView;
    private ProfileVideoAdapter videoAdapter;
    private List<Video> videoList;
    private List<String> videoIds;

    private CircleImageView avtProfile;
    private TextView tvUsername, user_id, follower, following, like;
    private EditText etBio;
    private TextView addBioBtn;
    private View indicatorVideos, indicatorLiked;
    private ImageView tabVideos, tabLiked;
    private LinearLayout emptyStateContainer;
    private LinearLayout whiteSpace, bottomNavigation;
    private RelativeLayout topBar;
    private Button edit_profile_btn;
    private LinearLayout tiktokStudioBtn, ordersBtn;
    private LinearLayout home_button, followersSection, followingSection;
    private LinearLayout discover_button, upload_button, inbox_button, profile_button;
    public List<String> videoIdsItems;

    private String userKey;
    private String username;
    private String userIdName;
    private DatabaseReference databaseReference;
    private DatabaseReference videosReference;
    private boolean isEditingBio = false;
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        initializeViews();
        setupClickListeners();

        language = getIntent().getStringExtra("language");

        if (language != null) {
            if (language.equals("English") || language.equals("Tiếng Anh")) {
                LocaleHelper.setLocale(this, "en");
            } else {
                LocaleHelper.setLocale(this, "vi");
            }
        } else {
            LocaleHelper.setLocale(this, "vi");
        }

        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);

        // Get videos_list from HomeScreen
        videoIdsItems = getIntent().getStringArrayListExtra("VIDEOS_ARRAY_JSON");

        if (username == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        videosReference = FirebaseDatabase.getInstance().getReference("videos");
//
        // Load user data
//        getUserDataFromFirebase();

        // Initialize RecyclerView
        setupRecyclerView();
    }

    private void initializeViews() {
        // Profile info
        avtProfile = findViewById(R.id.avt_profile);
        tvUsername = findViewById(R.id.tvUsername);
        user_id = findViewById(R.id.user_id);
        follower = findViewById(R.id.follower);
        following = findViewById(R.id.following);
        like = findViewById(R.id.like);
        etBio = findViewById(R.id.etBio);
        addBioBtn = findViewById(R.id.addBioBtn);
        followersSection = findViewById(R.id.followersSection);
        followingSection = findViewById(R.id.followingSection);
        whiteSpace = findViewById(R.id.white_space);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        topBar = findViewById(R.id.topBar);

        // Tabs
        tabVideos = findViewById(R.id.tabVideos);
        tabLiked = findViewById(R.id.tabLiked);
        indicatorVideos = findViewById(R.id.indicatorVideos);
        indicatorLiked = findViewById(R.id.indicatorLiked);

        // Buttons
        edit_profile_btn = findViewById(R.id.edit_profile_btn);
        tiktokStudioBtn = findViewById(R.id.tiktokStudioBtn);
        ordersBtn = findViewById(R.id.ordersBtn);

        // Navigation
        home_button = findViewById(R.id.home_button);
        discover_button = findViewById(R.id.discover_button);
        upload_button = findViewById(R.id.upload_button);
        inbox_button = findViewById(R.id.inbox_button);
        profile_button = findViewById(R.id.profile_button);

        // RecyclerView and empty state
        recyclerView = findViewById(R.id.recyclerViewProfile);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);

        // Initialize video lists
        videoList = new ArrayList<>();
        videoIds = new ArrayList<>();

        // Lấy userKey từ Intent (giống ChangeBio.java)
        String userKey = getIntent().getStringExtra("userKey");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
    }

    private void setupRecyclerView() {
        // Set up RecyclerView with GridLayoutManager (3 columns)
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        // Initialize adapter
        videoAdapter = new ProfileVideoAdapter(videoList, videoIds, videoIdsItems, this);
        recyclerView.setAdapter(videoAdapter);
    }

    private void updateEmptyState() {
        if (videoList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // Tab selection
        tabVideos.setOnClickListener(v -> {
            indicatorVideos.setVisibility(View.VISIBLE);
            indicatorLiked.setVisibility(View.INVISIBLE);
            tabVideos.setColorFilter(getResources().getColor(android.R.color.black));
            tabLiked.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        });

        tabLiked.setOnClickListener(v -> {
            indicatorVideos.setVisibility(View.INVISIBLE);
            indicatorLiked.setVisibility(View.VISIBLE);
            tabVideos.setColorFilter(getResources().getColor(android.R.color.darker_gray));
            tabLiked.setColorFilter(getResources().getColor(android.R.color.black));

            // Show toast for liked videos (not implemented)
            Toast.makeText(this, "Chức năng video đã thích chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        // Bio editing
        addBioBtn.setOnClickListener(v -> {
            addBioBtn.setVisibility(View.GONE);
            etBio.setVisibility(View.VISIBLE);
            etBio.requestFocus();

            // Show keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(etBio, InputMethodManager.SHOW_IMPLICIT);

            isEditingBio = true;
        });

        etBio.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && isEditingBio) {
                updateBioToFirebase();
                isEditingBio = false;
            }
        });

        whiteSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etBio.hasFocus()) {
                    // 1. Ẩn bàn phím
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etBio.getWindowToken(), 0);

                    // 2. Bỏ focus
                    etBio.clearFocus();

                    // 3. Lấy nội dung bio
                    String newBio = etBio.getText().toString().trim();

                    if (newBio.length() == 0) {
                        databaseReference.child(userKey).child("bio").setValue("")
                                .addOnSuccessListener(aVoid -> {
                                    if (language.equals("English") || language.equals("Tiếng Anh")) {
                                        Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                        etBio.setVisibility(View.GONE);
                        addBioBtn.setVisibility(View.VISIBLE);
                        isEditingBio = false;
                        return;
                    }

                    // 4. Cập nhật bio nếu có userKey
                    if (userKey != null) {
                        databaseReference.child(userKey).child("bio").setValue(newBio)
                                .addOnSuccessListener(aVoid -> {
                                    if (language.equals("English") || language.equals("Tiếng Anh")) {
                                        Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getApplicationContext(), "Không tìm thấy userKey!", Toast.LENGTH_SHORT).show();
                    }

                    isEditingBio = false;
                }
            }
        });

        topBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etBio.hasFocus()) {
                    // 1. Ẩn bàn phím
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etBio.getWindowToken(), 0);

                    // 2. Bỏ focus
                    etBio.clearFocus();

                    // 3. Lấy nội dung bio
                    String newBio = etBio.getText().toString().trim();

                    if (newBio.length() == 0) {
                        databaseReference.child(userKey).child("bio").setValue("")
                                .addOnSuccessListener(aVoid -> {
                                    if (language.equals("English") || language.equals("Tiếng Anh")) {
                                        Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                        etBio.setVisibility(View.GONE);
                        addBioBtn.setVisibility(View.VISIBLE);
                        isEditingBio = false;
                        return;
                    }

                    // 4. Cập nhật bio nếu có userKey
                    if (userKey != null) {
                        databaseReference.child(userKey).child("bio").setValue(newBio)
                                .addOnSuccessListener(aVoid -> {
                                    if (language.equals("English") || language.equals("Tiếng Anh")) {
                                        Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getApplicationContext(), "Không tìm thấy userKey!", Toast.LENGTH_SHORT).show();
                    }

                    isEditingBio = false;
                }
            }
        });

        profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etBio.hasFocus()) {
                    // 1. Ẩn bàn phím
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etBio.getWindowToken(), 0);

                    // 2. Bỏ focus
                    etBio.clearFocus();

                    // 3. Lấy nội dung bio
                    String newBio = etBio.getText().toString().trim();

                    if (newBio.length() == 0) {
                        databaseReference.child(userKey).child("bio").setValue("")
                                .addOnSuccessListener(aVoid -> {
                                    if (language.equals("English") || language.equals("Tiếng Anh")) {
                                        Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                        etBio.setVisibility(View.GONE);
                        addBioBtn.setVisibility(View.VISIBLE);
                        isEditingBio = false;
                        return;
                    }

                    // 4. Cập nhật bio nếu có userKey
                    if (userKey != null) {
                        databaseReference.child(userKey).child("bio").setValue(newBio)
                                .addOnSuccessListener(aVoid -> {
                                    if (language.equals("English") || language.equals("Tiếng Anh")) {
                                        Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getApplicationContext(), "Không tìm thấy userKey!", Toast.LENGTH_SHORT).show();
                    }

                    isEditingBio = false;
                }
            }
        });

        // Navigation
        home_button.setOnClickListener(v -> {
            if (etBio.hasFocus()) {
                // 1. Ẩn bàn phím
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etBio.getWindowToken(), 0);

                // 2. Bỏ focus
                etBio.clearFocus();

                // 3. Lấy nội dung bio
                String newBio = etBio.getText().toString().trim();

                if (newBio.length() == 0) {
                    databaseReference.child(userKey).child("bio").setValue("")
                            .addOnSuccessListener(aVoid -> {
                                if (language.equals("English") || language.equals("Tiếng Anh")) {
                                    Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    etBio.setVisibility(View.GONE);
                    addBioBtn.setVisibility(View.VISIBLE);
                    isEditingBio = false;
                    return;
                }

                // 4. Cập nhật bio nếu có userKey
                if (userKey != null) {
                    databaseReference.child(userKey).child("bio").setValue(newBio)
                            .addOnSuccessListener(aVoid -> {
                                if (language.equals("English") || language.equals("Tiếng Anh")) {
                                    Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "Không tìm thấy userKey!", Toast.LENGTH_SHORT).show();
                }

                isEditingBio = false;
            }

            Intent intent = new Intent(ProfileScreen.this, HomeScreen.class);
//            intent.putExtra("USER_ID", username);
            intent.putExtra("language", language);
            startActivity(intent);
        });

        discover_button.setOnClickListener(v -> {
            if (etBio.hasFocus()) {
                // 1. Ẩn bàn phím
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etBio.getWindowToken(), 0);

                // 2. Bỏ focus
                etBio.clearFocus();

                // 3. Lấy nội dung bio
                String newBio = etBio.getText().toString().trim();

                if (newBio.length() == 0) {
                    databaseReference.child(userKey).child("bio").setValue("")
                            .addOnSuccessListener(aVoid -> {
                                if (language.equals("English") || language.equals("Tiếng Anh")) {
                                    Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    etBio.setVisibility(View.GONE);
                    addBioBtn.setVisibility(View.VISIBLE);
                    isEditingBio = false;
                    return;
                }

                // 4. Cập nhật bio nếu có userKey
                if (userKey != null) {
                    databaseReference.child(userKey).child("bio").setValue(newBio)
                            .addOnSuccessListener(aVoid -> {
                                if (language.equals("English") || language.equals("Tiếng Anh")) {
                                    Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "Không tìm thấy userKey!", Toast.LENGTH_SHORT).show();
                }

                isEditingBio = false;
            }

            Intent intent = new Intent(ProfileScreen.this, SearchScreen.class);
            intent.putExtra("language", language);
            startActivity(intent);
        });

        followersSection.setOnClickListener(v -> {
            if (userKey != null) {
                Intent intent = new Intent(ProfileScreen.this, NewFollowersScreen.class);
//                intent.putExtra("userKey", userKey); // Truyền userKey sang ListFollower
                intent.putExtra("language", language); // Nếu bạn cần dùng ngôn ngữ
                startActivity(intent);
            } else {
                Toast.makeText(ProfileScreen.this, "Không thể lấy userKey!", Toast.LENGTH_SHORT).show();
            }
        });

        followingSection.setOnClickListener(v -> {
            if (userKey != null) {
                Intent intent = new Intent(ProfileScreen.this, NewFollowingScreen.class);
//                intent.putExtra("userKey", userKey); // Truyền userKey sang ListFollower
                intent.putExtra("language", language); // Nếu bạn cần dùng ngôn ngữ
                startActivity(intent);
            } else {
                Toast.makeText(ProfileScreen.this, "Không thể lấy userKey!", Toast.LENGTH_SHORT).show();
            }
        });

        edit_profile_btn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, EditProfile.class);
            intent.putExtra("language", language);
            startActivity(intent);
        });

        upload_button.setOnClickListener(v -> {
            if (etBio.hasFocus()) {
                // 1. Ẩn bàn phím
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etBio.getWindowToken(), 0);

                // 2. Bỏ focus
                etBio.clearFocus();

                // 3. Lấy nội dung bio
                String newBio = etBio.getText().toString().trim();

                if (newBio.length() == 0) {
                    databaseReference.child(userKey).child("bio").setValue("")
                            .addOnSuccessListener(aVoid -> {
                                if (language.equals("English") || language.equals("Tiếng Anh")) {
                                    Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    etBio.setVisibility(View.GONE);
                    addBioBtn.setVisibility(View.VISIBLE);
                    isEditingBio = false;
                    return;
                }

                // 4. Cập nhật bio nếu có userKey
                if (userKey != null) {
                    databaseReference.child(userKey).child("bio").setValue(newBio)
                            .addOnSuccessListener(aVoid -> {
                                if (language.equals("English") || language.equals("Tiếng Anh")) {
                                    Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "Không tìm thấy userKey!", Toast.LENGTH_SHORT).show();
                }

                isEditingBio = false;
            }

            Intent intent = new Intent(ProfileScreen.this, CameraScreen.class);
            intent.putExtra("language", language);
            startActivity(intent);
        });

        inbox_button.setOnClickListener(v -> {
            if (etBio.hasFocus()) {
                // 1. Ẩn bàn phím
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etBio.getWindowToken(), 0);

                // 2. Bỏ focus
                etBio.clearFocus();

                // 3. Lấy nội dung bio
                String newBio = etBio.getText().toString().trim();

                if (newBio.length() == 0) {
                    databaseReference.child(userKey).child("bio").setValue("")
                            .addOnSuccessListener(aVoid -> {
                                if (language.equals("English") || language.equals("Tiếng Anh")) {
                                    Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    etBio.setVisibility(View.GONE);
                    addBioBtn.setVisibility(View.VISIBLE);
                    isEditingBio = false;
                    return;
                }

                // 4. Cập nhật bio nếu có userKey
                if (userKey != null) {
                    databaseReference.child(userKey).child("bio").setValue(newBio)
                            .addOnSuccessListener(aVoid -> {
                                if (language.equals("English") || language.equals("Tiếng Anh")) {
                                    Toast.makeText(getApplicationContext(), "Update bio successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Cập nhật tiểu sử thành công!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "Không tìm thấy userKey!", Toast.LENGTH_SHORT).show();
                }

                isEditingBio = false;
            }

            Intent intent = new Intent(ProfileScreen.this, InboxScreen.class);
            intent.putExtra("language", language);
            startActivity(intent);
        });

        // TikTok Studio and Orders (just show toast)
        tiktokStudioBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng TikTok Studio chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        ordersBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Đơn hàng chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserDataFromFirebase(); // Refresh data when returning to screen
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoAdapter != null) {
            videoAdapter.releaseAllPlayers();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoAdapter != null) {
            videoAdapter.releaseAllPlayers();
        }
    }

    private void getUserDataFromFirebase() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                boolean userFound = false;

                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String storedUsername = userSnapshot.child("account").getValue(String.class);
                    if (storedUsername != null && storedUsername.equals(username)) {
                        userFound = true;
                        userKey = userSnapshot.getKey();
                        String avt = userSnapshot.child("avatar").getValue(String.class);
                        String name = userSnapshot.child("name").getValue(String.class);
                        String idName = userSnapshot.child("idName").getValue(String.class);
                        Integer followerCount = userSnapshot.child("followerCount").getValue(Integer.class);
                        Integer followingCount = userSnapshot.child("followingCount").getValue(Integer.class);
                        Integer likeCount = userSnapshot.child("likeCount").getValue(Integer.class);
                        String bio = userSnapshot.child("bio").getValue(String.class);

                        // Save idName for later use
                        userIdName = idName;

                        // Update UI with user data
                        tvUsername.setText(name != null ? name : "Người dùng");
                        user_id.setText("@" + (idName != null ? idName : username));
                        follower.setText(followerCount != null ? String.valueOf(followerCount) : "0");
                        following.setText(followingCount != null ? String.valueOf(followingCount) : "0");
                        like.setText(likeCount != null ? String.valueOf(likeCount) : "0");

                        // Handle bio
                        if (bio != null && !bio.isEmpty()) {
                            etBio.setText(bio);
                            etBio.setVisibility(View.VISIBLE);
                            addBioBtn.setVisibility(View.GONE);
                        } else {
                            etBio.setVisibility(View.GONE);
                            addBioBtn.setVisibility(View.VISIBLE);
                        }

                        // Load profile image
                        if (avt != null && !avt.isEmpty()) {
                            Glide.with(this)
                                    .load(avt)
                                    .placeholder(R.drawable.default_profile)
                                    .error(R.drawable.default_profile)
                                    .into(avtProfile);
                        } else {
                            avtProfile.setImageResource(R.drawable.default_profile);
                        }

                        // Load user videos after getting user data
                        Log.d(TAG, "User ID Name khi query: '" + userIdName + "'");
                        loadUserVideos();
                        break;
                    }
                }

                if (!userFound) {
                    Toast.makeText(ProfileScreen.this, "Không tìm thấy dữ liệu tài khoản!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProfileScreen.this, "Lỗi khi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserVideos() {
        // Clear existing lists
        videoList.clear();
        videoIds.clear();

        String queryUsername = (userIdName != null) ? userIdName : username;


        // Query videos where username matches the current user's username
        videosReference.orderByChild("username").equalTo(userIdName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {

                for (DataSnapshot videoSnapshot : task.getResult().getChildren()) {
                    String videoId = videoSnapshot.getKey();
                    String video_user_name = videoSnapshot.child("username").getValue(String.class);
                    String videoUrl = videoSnapshot.child("videoUri").getValue(String.class);
                    String thumbnailUrl = videoSnapshot.child("thumbnailUrl").getValue(String.class);
                    String title = videoSnapshot.child("title").getValue(String.class);
                    String likes = videoSnapshot.child("likes").getValue(String.class);
                    String comments = videoSnapshot.child("comments").getValue(String.class);
                    String music = videoSnapshot.child("music").getValue(String.class);

                    if (videoUrl != null) {
                        Video video = new Video(
                                videoUrl,
                                thumbnailUrl != null ? thumbnailUrl : "",
                                userIdName != null ? userIdName : username,
                                title != null ? title : "",
                                likes != null ? likes : "0",
                                comments != null ? comments : "0",
                                music != null ? music : "Original Sound"
                        );

                        // Check if thumbnail is missing and generate one
                        if ((thumbnailUrl == null || thumbnailUrl.isEmpty()) && videoUrl != null && !videoUrl.isEmpty()) {
                            generateAndUpdateThumbnail(video, videoId);
                        }

                        videoList.add(video);
                        videoIds.add(videoId);
                    }
                }

                // Update adapter and UI
                if (videoAdapter != null) {

                    videoAdapter.notifyDataSetChanged();
                }
                updateEmptyState();

                Log.d(TAG, "Đã tải " + videoList.size() + " video cho người dùng: " + userIdName);
            } else {
                Log.e(TAG, "Lỗi khi tải video: " +
                        (task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định"));
                Toast.makeText(ProfileScreen.this, "Lỗi khi tải video!", Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }

        });
//        updateEmptyState();

    }

    private void generateAndUpdateThumbnail(Video video, String videoId) {
        ThumbnailGenerator.generateAndUploadThumbnail(
                this,
                video.getVideoUri(),
                new ThumbnailGenerator.ThumbnailCallback() {
                    @Override
                    public void onThumbnailGenerated(String thumbnailUrl) {
                        // Update the video object
                        video.setThumbnailUrl(thumbnailUrl);

                        // Update Firebase
                        videosReference.child(videoId).child("thumbnailUrl").setValue(thumbnailUrl)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Thumbnail updated for video: " + videoId);
                                    // Notify adapter of change
                                    int position = videoList.indexOf(video);
                                    if (position != -1 && videoAdapter != null) {
                                        videoAdapter.notifyItemChanged(position);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update thumbnail: " + e.getMessage());
                                });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error generating thumbnail: " + e.getMessage());
                    }
                }
        );
    }

    private void updateBioToFirebase() {
        String newBio = etBio.getText().toString().trim();

        if (newBio.isEmpty()) {
            etBio.setVisibility(View.GONE);
            addBioBtn.setVisibility(View.VISIBLE);
            return;
        }

        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String storedUsername = userSnapshot.child("account").getValue(String.class);
                    if (storedUsername != null && storedUsername.equals(username)) {
                        // Update bio in Firebase
                        userSnapshot.getRef().child("bio").setValue(newBio)
                                .addOnSuccessListener(unused -> {
                                    // Bio updated successfully
                                    hideKeyboard();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Lỗi khi cập nhật Bio!", Toast.LENGTH_SHORT).show());
                        return;
                    }
                }
            }
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

