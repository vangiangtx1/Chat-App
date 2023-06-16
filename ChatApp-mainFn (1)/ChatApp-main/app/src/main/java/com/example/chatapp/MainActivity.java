package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Adapter.RequestAdapter;
import com.example.chatapp.Login.SignInActivity;
import com.example.chatapp.Models.Request;
import com.example.chatapp.Models.User;
import com.example.chatapp.Utilities.Utilities;
import com.example.chatapp.View.MyProfile;
import com.example.chatapp.fragments.CallFragment;
import com.example.chatapp.fragments.ChatsFragment;
import com.example.chatapp.fragments.ContactFragment;
import com.example.chatapp.fragments.FriendsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int FRAGMENT_CHAT = 1;
    private static final int FRAGMENT_FRIEND = 2;
    private static final int FRAGMENT_CONTACT = 3;
    private static final int FRAGMENT_CALL = 4;
    public static final int MY_REQUEST_CODE = 10;
    private int currentFragment = FRAGMENT_CHAT;
    private int backPressCount = 0;
    long numberNotification;
    private boolean doubleBackToExitPressedOnce = false;
    BottomNavigationView mBottomNavigationView;

    DrawerLayout drawer_layout;
    Toolbar toolbar;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    TextView textViewBadge;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserReference, mRequestReference, mDatabaseReference;
    FirebaseStorage storage;
    StorageReference mStorageReference;
    String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setControl();
        setEvent();

    }


    //Xin cấp quyền thông báo

    public void setControl() {
        toolbar = findViewById(R.id.toolbarMain);
        drawer_layout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        myId = mUser.getUid();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid());
        mRequestReference = FirebaseDatabase.getInstance().getReference().child("Requests");
        storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference().child("profilePic/default_avatar.png");
    }

    public void setEvent() {
        checkPermissionNotification(); //Kiểm tra quyền nhận thông báo
        loadHeaderNavigation();  // Tải thông tin cho HeaderNavigation
        bottomNavigation(); /* Xử lý logic cho Bottom Navigation trong MainActivity */
        setActionDrawerToggle(); //Xử lý cho DrawerToggle
        actionToolbar(); //Xử lý cho Toolbar
        replaceFragment(new ChatsFragment()); //Đặt Fragment đầu tiên là ChatFragment
        actionNavigationDrawer(); // Xử lý logic cho Novigation Drawer
        setTitleToolBar(); // Hàm xử lý đổi title cho toolbar cho các fragment khác nhau
        updateFCMToken(); //Xử lý khi đăng nhập thành công sẽ cập nhật một token mới
    }

    private void checkPermissionNotification() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                return;
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Quyền bị từ chối\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                .check();
    }

    private void loadHeaderNavigation() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        View headerNavigation = navigationView.getHeaderView(0);
        CircleImageView nav_header_userPhoto = (CircleImageView) headerNavigation.findViewById(R.id.nav_header_userPhoto);
        TextView nav_header_userName = (TextView) headerNavigation.findViewById(R.id.nav_header_userName);
        TextView nav_header_userEmail = (TextView) headerNavigation.findViewById(R.id.nav_header_userEmail);
        headerNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyProfile.class);
                startActivity(intent);
            }
        });

        mUserReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.default_avatar).into(nav_header_userPhoto);
                        nav_header_userName.setText(user.getUserName());
                        nav_header_userEmail.setText(user.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void bottomNavigation() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_chats:
                        openChatFragment();
                        navigationView.setCheckedItem(R.id.nav_chat);
                        break;
                    case R.id.action_friends:
                        openFriendFragment();
                        navigationView.setCheckedItem(R.id.nav_friend);
                        break;
                    case R.id.action_contacts:
                        openContactFragment();
                        navigationView.setCheckedItem(R.id.nav_contact);
                        break;
                    case R.id.action_calls:
                        openCallFragment();
                        navigationView.setCheckedItem(R.id.nav_call);
                        break;
                }
                setTitleToolBar();
                return true;
            }
        });
    }

    private void setActionDrawerToggle() {
        drawerToggle = new ActionBarDrawerToggle(this, drawer_layout, R.string.app_name, R.string.app_name);
        drawer_layout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void actionToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.icon_menu_navigation);
    }

    private void actionNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_chat);
        mBottomNavigationView.getMenu().findItem(R.id.action_chats).setChecked(true);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_chat) {
            openChatFragment();
            mBottomNavigationView.getMenu().findItem(R.id.action_chats).setChecked(true);
        } else if (id == R.id.nav_friend) {
            openFriendFragment();
            mBottomNavigationView.getMenu().findItem(R.id.action_friends).setChecked(true);
        } else if (id == R.id.nav_contact) {
            openContactFragment();
            mBottomNavigationView.getMenu().findItem(R.id.action_contacts).setChecked(true);
        } else if (id == R.id.nav_call) {
            openCallFragment();
            mBottomNavigationView.getMenu().findItem(R.id.action_calls).setChecked(true);
        } else if (id == R.id.nav_logout) {
            openDialogConfirmLogout(Gravity.CENTER);
        }
        setTitleToolBar();
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    // Mở Dialog xác nhận Đăng xuất
    private void openDialogConfirmLogout(int gravity) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_logout);
        Window window = (Window) dialog.getWindow();
        if (window == null) {
            return;
        } else {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            window.setAttributes(windowAttributes);

            if (Gravity.CENTER == gravity) {
                dialog.setCancelable(true);
            } else {
                dialog.setCancelable(false);
            }
            Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
            Button btnCancelConfirm = dialog.findViewById(R.id.btnCancelConfirm);

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Xóa FCM_TOKEN
                    if (mUser != null) {
                        mUserReference.child(mUser.getUid()).child("fcmToken").removeValue();
                    }
                    Utilities.statusActivity("Offline");
                    mAuth.signOut();
                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                }
            });
            btnCancelConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    // Mở Dialog nhận thông báo kết bạn
    private void openDialogNotifications(int gravity) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_notifications);
        Window window = (Window) dialog.getWindow();
        if (window == null) {
            return;
        } else {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            window.setAttributes(windowAttributes);

            if (Gravity.CENTER == gravity) {
                dialog.setCancelable(true);
            } else {
                dialog.setCancelable(false);
            }
            RecyclerView rvListRequests = dialog.findViewById(R.id.rvListRequests);
            RequestAdapter requestAdapter;
            ArrayList<Request> listRequests = new ArrayList<>();
            requestAdapter = new RequestAdapter(this, listRequests);
            rvListRequests.setAdapter(requestAdapter);
            LinearLayoutManager layoutManager1 = new LinearLayoutManager(this); /* Khởi tạo một LinearLayout và gán vào RecycleView */
            rvListRequests.setLayoutManager(layoutManager1);
            mRequestReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    listRequests.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Request request = dataSnapshot.getValue(Request.class);
                        String check = request.getStatus().trim();
                        if (mUser != null && !check.equals("pending")) {
                            request.setUserID(dataSnapshot.getKey());
                            listRequests.add(request);
                        }
                    }
                    requestAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        dialog.show();
    }


    /* Xét trạng thái hoạt động của CurrentUser */
    @Override
    protected void onStart(){
        Utilities.statusActivity("Online");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Utilities.statusActivity("Online");
        super.onResume();
    }
    @Override
    protected void onRestart() {
        Utilities.statusActivity("Online");
        super.onRestart();
    }
    /* ------------------------------------------------------------------------------------------- */

    //Xử lý khi ấn back 2 lần
    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
            backPressCount = 0;
        }
    };

    @Override
    public void onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                finishAffinity();
                super.onBackPressed();
                return;
            }

            doubleBackToExitPressedOnce = true;
            backPressCount++;

            if (backPressCount == 1) {
                Toast.makeText(this, "Nhấn back lần nữa để thoát", Toast.LENGTH_SHORT).show();
            } else if (backPressCount == 2) {
                Toast.makeText(this, "Thoát ứng dụng", Toast.LENGTH_SHORT).show();
                finishAffinity();
                return;
            }
            mHandler.postDelayed(mRunnable, 2000);
        }


    }

    private void openChatFragment() {
        if (currentFragment != FRAGMENT_CHAT) {
            replaceFragment(new ChatsFragment());
            currentFragment = FRAGMENT_CHAT;
        }
    }

    private void openFriendFragment() {
        if (currentFragment != FRAGMENT_FRIEND) {
            replaceFragment(new FriendsFragment());
            currentFragment = FRAGMENT_FRIEND;
        }
    }

    private void openContactFragment() {
        if (currentFragment != FRAGMENT_CONTACT) {
            replaceFragment(new ContactFragment());
            currentFragment = FRAGMENT_CONTACT;
        }
    }

    private void openCallFragment() {
        if (currentFragment != FRAGMENT_CALL) {
            replaceFragment(new CallFragment());
            currentFragment = FRAGMENT_CALL;
        }
    }

    /* Xét lại title cho mỗi Fragment */
    private void setTitleToolBar() {
        String title = "";
        switch (currentFragment) {
            case FRAGMENT_CHAT:
                title = getString(R.string.message);
                break;
            case FRAGMENT_FRIEND:
                title = getString(R.string.friend);
                break;
            case FRAGMENT_CONTACT:
                title = getString(R.string.user);
                break;
            case FRAGMENT_CALL:
                title = getString(R.string.call);
                break;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /* Thay thế fragment này bằng một fragment khác */
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_view, fragment);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_notification) {
            openDialogNotifications(Gravity.CENTER);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* -------------------------------- Đếm số lượng cho Notifications Badge -------------------------------- */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_main_activity, menu);
        MenuItem menuItem = menu.findItem(R.id.action_notification);
        mRequestReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    numberNotification = 0;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (mUser != null && dataSnapshot.child("status").getValue().toString().equals("wait_confirm")) {
                            numberNotification++;
                        }
                    }
                    if (numberNotification == 0) {
                        menuItem.setActionView(null);
                    } else {
                        menuItem.setActionView(R.layout.custom_notification_layout);
                        View view = menuItem.getActionView();
                        textViewBadge = view.findViewById(R.id.textViewBadge);
                        textViewBadge.setText(String.valueOf(numberNotification));
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openDialogNotifications(Gravity.CENTER);
                            }
                        });
                    }

                } else {
                    menuItem.setActionView(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void updateFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    String fcmToken = task.getResult();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("fcmToken", fcmToken);
                    if (mUser != null) {
                        mUserReference.child(mUser.getUid()).updateChildren(hashMap);
                    }
                }
            }
        });
    }
}
