package org.smartgresiter.jhpiego.custom_view;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.adapter.NavigationAdapter;
import org.smartgresiter.jhpiego.application.JhpiegoApplication;
import org.smartgresiter.jhpiego.contract.NavigationContract;
import org.smartgresiter.jhpiego.presenter.NavigationPresenter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NavigationMenu implements NavigationContract.View {

    private static NavigationMenu instance;
    private String TAG = NavigationMenu.class.getCanonicalName();
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    private NavigationAdapter navigationAdapter;
    private RecyclerView recyclerView;
    private TextView tvLogout;
    private View rootView = null;

    private NavigationContract.Presenter mPresenter;


    private NavigationMenu() {

    }

    public static NavigationMenu getInstance(Activity activity, View parentView, Toolbar myToolbar) {

        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (instance == null) {
                instance = new NavigationMenu();
            }

            instance.init(activity, parentView, myToolbar);
            return instance;
        } else {
            return null;
        }
    }

    public NavigationAdapter getNavigationAdapter() {
        return navigationAdapter;
    }

    private void init(Activity activity, View parentView, Toolbar myToolbar) {
        // parentActivity = activity;
        try {
            setParentView(activity, parentView);
            toolbar = myToolbar;
            mPresenter = new NavigationPresenter(this);
            prepareViews(activity);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void setParentView(Activity activity, View parentView) {
        if (parentView != null) {
            rootView = parentView;
        } else {
            // get current view
            // ViewGroup current = parentActivity.getWindow().getDecorView().findViewById(android.R.id.content);
            ViewGroup current = (ViewGroup) ((ViewGroup) (activity.findViewById(android.R.id.content))).getChildAt(0);
            if (!(current instanceof DrawerLayout)) {

                if (current.getParent() != null) {
                    ((ViewGroup) current.getParent()).removeView(current); // <- fix
                }

                // swap content view
                LayoutInflater mInflater = LayoutInflater.from(activity);
                ViewGroup contentView = (ViewGroup) mInflater.inflate(R.layout.activity_base, null);
                activity.setContentView(contentView);

                rootView = activity.findViewById(R.id.nav_view);
                RelativeLayout rl = activity.findViewById(R.id.nav_content);

                if (current.getParent() != null) {
                    ((ViewGroup) current.getParent()).removeView(current); // <- fix
                }

                if (current instanceof RelativeLayout) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    current.setLayoutParams(params);
                    rl.addView(current);
                } else {
                    rl.addView(current);
                }
            } else {
                rootView = current;
            }
        }
        //
    }

    @Override
    public void prepareViews(Activity activity) {

        drawer = activity.findViewById(R.id.drawer_layout);
        recyclerView = rootView.findViewById(R.id.rvOptions);
        navigationView = rootView.findViewById(R.id.nav_view);
        tvLogout = rootView.findViewById(R.id.tvLogout);
        recyclerView = rootView.findViewById(R.id.rvOptions);

        // register all objects
        registerDrawer(activity);
        registerNavigation(activity);
        registerLogout(activity);
        registerSync(activity);

        // update all actions
        mPresenter.refreshLastSync();
        mPresenter.refreshNavigationCount(activity);
    }

    public void lockDrawer(Activity activity) {
        prepareViews(activity);
        if (drawer != null) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void registerDrawer(Activity parentActivity) {
        if (drawer != null) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    parentActivity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

        }
    }

    private void registerNavigation(Activity parentActivity) {
        if (recyclerView != null) {

            if (navigationAdapter == null) {
                navigationAdapter = new NavigationAdapter(mPresenter.getOptions(), parentActivity);
            }

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(parentActivity);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(navigationAdapter);
        }
    }

    private void registerLogout(final Activity parentActivity) {
        mPresenter.displayCurrentUser();
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(parentActivity);
            }
        });
    }

    private void registerSync(final Activity parentActivity) {

        TextView tvSync = rootView.findViewById(R.id.tvSync);
        ImageView ivSync = rootView.findViewById(R.id.ivSyncIcon);

        View.OnClickListener syncClicker = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(parentActivity, parentActivity.getResources().getText(R.string.action_start_sync), Toast.LENGTH_SHORT).show();
                mPresenter.Sync(parentActivity);
            }
        };


        tvSync.setOnClickListener(syncClicker);
        ivSync.setOnClickListener(syncClicker);
    }

    public boolean onBackPressed() {
        boolean res = false;
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                res = true;
            }
        }
        return res;
    }

    @Override
    public void refreshLastSync(Date lastSync) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh.mm aa , MMM d", Locale.getDefault());
        if (rootView != null) {
            TextView tvLastSyncTime = rootView.findViewById(R.id.tvSyncTime);
            if (lastSync != null) {
                tvLastSyncTime.setVisibility(View.VISIBLE);
                tvLastSyncTime.setText(sdf.format(lastSync));
            } else {
                tvLastSyncTime.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void refreshCurrentUser(String name) {
        if (tvLogout != null) {
            tvLogout.setText(String.format("%s %s", "Logout as", name));
        }
    }

    @Override
    public void logout(Activity activity) {
        Toast.makeText(activity.getApplicationContext(), activity.getResources().getText(R.string.action_log_out), Toast.LENGTH_SHORT).show();
        JhpiegoApplication.getInstance().logoutCurrentUser();
    }

    @Override
    public void refreshCount() {
        navigationAdapter.notifyDataSetChanged();
    }

    @Override
    public void displayToast(Activity activity, String message) {
        if (activity != null) {
            Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
