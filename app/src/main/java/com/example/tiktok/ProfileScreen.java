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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
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
    private NotificationManager notificationManager;
    private CircleImageView avtProfile;
    private TextView tvUsername, user_id, follower, following, like;
    private EditText etBio;
    private TextView addBioBtn;
    private Context context;
    private View indicatorVideos, indicatorLiked;
    private ImageView tabVideos, tabLiked;
    private LinearLayout emptyStateContainer;
    private LinearLayout whiteSpace, bottomNavigation;
    private RelativeLayout topBar;
    private Button edit_profile_btn;
    private ImageButton addFriendBtn;
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
    private String viewUsername;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        initializeViews();
        setupClickListeners();

        language = getIntent().getStringExtra("language");
        if (language == null) {
            Log.d("LOGIN_CHECK_LANGUAGE", "Language is null");
        }else {
            Log.d("LOGIN_CHECK_LANGUAGE", "Language is: " + language);
        }

        if (language != null) {
            if (language.equals("English") || language.equals("Tiếng Anh")) {
                LocaleHelper.setLocale(this, "en");
            } else {
                LocaleHelper.setLocale(this, "vi");
            }
        } else {
            LocaleHelper.setLocale(this, "vi");
        }

        // Lấy username từ intent (nếu đang xem profile người khác)
        viewUsername = getIntent().getStringExtra("viewUsername");

        // Lấy username của người dùng hiện tại
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", null);

        if (viewUsername != null && !viewUsername.equals(currentUsername)) {
            // Đang xem profile người khác
            username = viewUsername;
            // Ẩn các nút chỉnh sửa profile
            edit_profile_btn.setVisibility(View.GONE);
            addBioBtn.setVisibility(View.GONE);
            addFriendBtn.setVisibility(View.VISIBLE);
            // Hiển thị nút Follow thay vì nút Edit Profile (nếu có)
            // followButton.setVisibility(View.VISIBLE);

            String userKey = sharedPreferences.getString("userKey", null);
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

            if (userKey != null) {
                userRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String currentUserIdName = snapshot.child("idName").getValue(String.class);
                            if (currentUserIdName != null && !currentUserIdName.isEmpty()) {
                                String videoUsername = viewUsername;
//                                    Toast.makeText(ProfileScreen.this,
//                                            "currentUserIdName: " + currentUserIdName + "\nvideoUsername: " + videoUsername,
//                                            Toast.LENGTH_SHORT).show();

                                if (videoUsername.startsWith("-OL")) {
                                    // Nếu là user ID, lấy idName của người đăng video
                                    userRef.child(videoUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot ownerSnapshot) {
                                            if (ownerSnapshot.exists()) {
                                                String videoOwnerIdName = ownerSnapshot.child("idName").getValue(String.class);

                                                if (videoOwnerIdName != null) {
                                                    // Kiểm tra xem người dùng hiện tại có phải là người đăng video không
                                                    if (currentUserIdName.equals(videoOwnerIdName)) {
                                                        // Nếu người đăng video là chính mình, không hiển thị icon
                                                        addFriendBtn.setVisibility(View.INVISIBLE);  // Ẩn icon
                                                    } else {
                                                        // Nếu không phải, kiểm tra trạng thái follow
                                                        checkFollowStatusAndUpdateIcon(currentUserIdName, videoOwnerIdName);
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });
                                } else {
                                    // Nếu video username không phải là Firebase userId mà đã là idName
                                    if (currentUserIdName.equals(videoUsername)) {
                                        // Nếu người đăng video là chính mình, không hiển thị icon
                                        addFriendBtn.setVisibility(View.INVISIBLE);  // Ẩn icon
                                    } else {
                                        // Nếu không phải, kiểm tra trạng thái follow
                                        checkFollowStatusAndUpdateIcon(currentUserIdName, videoUsername);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }

        } else {
            // Đang xem profile của chính mình
            username = currentUsername;
            edit_profile_btn.setVisibility(View.VISIBLE);
            addBioBtn.setVisibility(View.VISIBLE);
            addFriendBtn.setVisibility(View.GONE);
            // Hiển thị các nút khác liên quan đến chỉnh sửa profile
        }

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
        addFriendBtn = findViewById(R.id.addFriendBtn);

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

        // Follow in another profile screen
        addFriendBtn.setOnClickListener(v -> {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
            userRef.orderByChild("account").equalTo(viewUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String videoOwnerUserKey = userSnapshot.getKey();  // userKey của người đăng video
                            String videoOwnerIdName = userSnapshot.child("idName").getValue(String.class);

                            if (videoOwnerIdName != null && !videoOwnerIdName.isEmpty()) {
                                SharedPreferences sharedPreferences = ProfileScreen.this.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                String currentUserKey = sharedPreferences.getString("userKey", null);

                                if (currentUserKey != null) {
                                    userRef.child(currentUserKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                String currentUserIdName = snapshot.child("idName").getValue(String.class);

                                                if (currentUserIdName != null && !currentUserIdName.isEmpty()) {
                                                    // ======= Kiểm tra nếu người dùng không thể follow chính mình =======
                                                    if (currentUserIdName.equals(videoOwnerIdName)) {
                                                        Toast.makeText(ProfileScreen.this, "Bạn không thể follow chính mình", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }

                                                    // ======= Kiểm tra xem đã follow người đó chưa =======
                                                    DatabaseReference followerRef = userRef.child(videoOwnerUserKey).child("follower");
                                                    followerRef.child(currentUserIdName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot followerSnapshot) {
                                                            if (followerSnapshot.exists()) {
                                                                // Đã follow rồi, thực hiện unfollow
                                                                followerRef.child(currentUserIdName).removeValue().addOnCompleteListener(task -> {
                                                                    if (task.isSuccessful()) {
                                                                        // Giảm followerCount của người đăng video
                                                                        DatabaseReference followerCountRef = userRef.child(videoOwnerUserKey).child("followerCount");
                                                                        followerCountRef.setValue(ServerValue.increment(-1));

                                                                        // Xóa người đăng video khỏi list following của người dùng hiện tại
                                                                        DatabaseReference followingRef = userRef.child(currentUserKey).child("following");
                                                                        followingRef.child(videoOwnerIdName).removeValue().addOnCompleteListener(task1 -> {
                                                                            if (task1.isSuccessful()) {
                                                                                // Giảm followingCount của người dùng hiện tại
                                                                                DatabaseReference followingCountRef = userRef.child(currentUserKey).child("followingCount");
                                                                                followingCountRef.setValue(ServerValue.increment(-1)).addOnSuccessListener(aVoid -> {
                                                                                    // Sau khi giảm thành công, đọc lại giá trị mới
                                                                                    followingCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                            Long count = snapshot.getValue(Long.class);
                                                                                            if (count != null) {
                                                                                                follower.setText(String.valueOf(count));
                                                                                            } else {
                                                                                                follower.setText("0");
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                                                            follower.setText("0");
                                                                                        }
                                                                                    });

                                                                                    // Cập nhật lại icon và thông báo
                                                                                    addFriendBtn.setImageResource(R.drawable.add_user); // Đổi icon thành plus
                                                                                    Toast.makeText(ProfileScreen.this, "Đã hủy follow", Toast.LENGTH_SHORT).show();

                                                                                }).addOnFailureListener(e -> {
                                                                                    Toast.makeText(ProfileScreen.this, "Lỗi khi giảm followingCount: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                });
                                                                            }
                                                                        });

                                                                    }
                                                                });
                                                            } else {
                                                                // Chưa follow, thực hiện follow
                                                                followerRef.child(currentUserIdName).setValue(true).addOnCompleteListener(task -> {
                                                                    if (task.isSuccessful()) {
                                                                        // Tăng followerCount của người đăng video
                                                                        DatabaseReference followerCountRef = userRef.child(videoOwnerUserKey).child("followerCount");
                                                                        followerCountRef.setValue(ServerValue.increment(1));

                                                                        // Thêm người đăng video vào list following của người dùng hiện tại
                                                                        DatabaseReference followingRef = userRef.child(currentUserKey).child("following");
                                                                        followingRef.child(videoOwnerIdName).setValue(true).addOnCompleteListener(task1 -> {
                                                                            if (task1.isSuccessful()) {
                                                                                // Tăng followingCount của người dùng hiện tại
                                                                                DatabaseReference followingCountRef = userRef.child(currentUserKey).child("followingCount");
                                                                                followingCountRef.setValue(ServerValue.increment(1)).addOnSuccessListener(aVoid -> {

                                                                                    // Đọc lại giá trị mới của followingCount
                                                                                    followingCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                            Long followerCount = snapshot.getValue(Long.class);
                                                                                            follower.setText(followerCount != null ? String.valueOf(followerCount) : "0");
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                                                            follower.setText("0");
                                                                                        }
                                                                                    });

                                                                                    // Cập nhật lại icon và thông báo
                                                                                    addFriendBtn.setImageResource(R.drawable.unfollow); // Đổi icon thành minus
                                                                                    Toast.makeText(ProfileScreen.this, "Follow thành công", Toast.LENGTH_SHORT).show();

                                                                                }).addOnFailureListener(e -> {
                                                                                    Toast.makeText(ProfileScreen.this, "Lỗi khi tăng followingCount: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                });

                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Toast.makeText(ProfileScreen.this, "Lỗi khi kiểm tra trạng thái follow", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(ProfileScreen.this, "Lỗi khi lấy idName người dùng hiện tại", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                    } else {
                        Toast.makeText(ProfileScreen.this, "Không tìm thấy người đăng video", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileScreen.this, "Lỗi khi truy vấn người đăng video", Toast.LENGTH_SHORT).show();
                }
            });
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
            finish();
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
            finish();
        });

        followersSection.setOnClickListener(v -> {
            if(!username.equals(viewUsername)) {
                if (userKey != null) {
                    Intent intent = new Intent(ProfileScreen.this, NewFollowersScreen.class);
//                intent.putExtra("userKey", userKey); // Truyền userKey sang ListFollower
                    intent.putExtra("language", language); // Nếu bạn cần dùng ngôn ngữ
                    startActivity(intent);
                } else {
                    Toast.makeText(ProfileScreen.this, "Không thể lấy userKey!", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(ProfileScreen.this, "Không được xem Followers của người khác!", Toast.LENGTH_SHORT).show();
            }
        });

        followingSection.setOnClickListener(v -> {
            if(!username.equals(viewUsername)) {
                if (userKey != null) {
                    Intent intent = new Intent(ProfileScreen.this, NewFollowingScreen.class);
//                intent.putExtra("userKey", userKey); // Truyền userKey sang ListFollower
                    intent.putExtra("language", language); // Nếu bạn cần dùng ngôn ngữ
                    startActivity(intent);
                } else {
                    Toast.makeText(ProfileScreen.this, "Không thể lấy userKey!", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(ProfileScreen.this, "Không được xem Following của người khác!", Toast.LENGTH_SHORT).show();
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
            finish();
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
            finish();
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
                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            String currentUsername = sharedPreferences.getString("username", null);
                            if(username.equals(currentUsername)) {
                                addBioBtn.setVisibility(View.VISIBLE);
                            }
                            else{
                                addBioBtn.setVisibility(View.GONE);
                            }
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

    private void checkFollowStatusAndUpdateIcon(String currentUserIdName, String videoOwnerIdName) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("idName").equalTo(videoOwnerIdName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String videoOwnerUserKey = userSnapshot.getKey();

                        usersRef.child(videoOwnerUserKey).child("follower").child(currentUserIdName)
                                .addListenerForSingleValueEvent(new ValueEventListener() {


                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot followerSnapshot) {
                                        if (addFriendBtn.getVisibility() != View.VISIBLE) {
                                            addFriendBtn.setVisibility(View.VISIBLE); // Đảm bảo plusImageView hiển thị
                                        }
                                        if (followerSnapshot.exists()) {
                                            addFriendBtn.setImageResource(R.drawable.unfollow); // Đã follow -> icon minus
                                        } else {
                                            addFriendBtn.setImageResource(R.drawable.add_user); // Chưa follow -> icon plus
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

}

