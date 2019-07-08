package org.smartgresiter.jhpiego.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.contract.FamilyProfileExtendedContract;
import org.smartgresiter.jhpiego.custom_view.FamilyFloatingMenu;
import org.smartgresiter.jhpiego.event.PermissionEvent;
import org.smartgresiter.jhpiego.fragment.FamilyProfileActivityFragment;
import org.smartgresiter.jhpiego.fragment.FamilyProfileDueFragment;
import org.smartgresiter.jhpiego.fragment.FamilyProfileMemberFragment;
import org.smartgresiter.jhpiego.listener.FloatingMenuListener;
import org.smartgresiter.jhpiego.model.FamilyProfileModel;
import org.smartgresiter.jhpiego.presenter.FamilyProfilePresenter;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.activity.BaseFamilyProfileActivity;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileActivityFragment;
import org.smartregister.family.fragment.BaseFamilyProfileDueFragment;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.util.PermissionUtils;
import org.smartregister.view.fragment.BaseRegisterFragment;

import de.hdodenhof.circleimageview.CircleImageView;

public class FamilyProfileActivity extends BaseFamilyProfileActivity implements FamilyProfileExtendedContract.View {

    private static final String TAG = FamilyProfileActivity.class.getCanonicalName();
    private String familyBaseEntityId;
    private boolean isFromFamilyServiceDue = false;

    BaseFamilyProfileMemberFragment profileMemberFragment;
    BaseFamilyProfileDueFragment profileDueFragment;
    BaseFamilyProfileActivityFragment profileActivityFragment;

    @Override
    protected void initializePresenter() {
        familyBaseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        isFromFamilyServiceDue = getIntent().getBooleanExtra(org.smartgresiter.jhpiego.util.Constants.INTENT_KEY.SERVICE_DUE, false);
        String familyHead = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_HEAD);
        String primaryCaregiver = getIntent().getStringExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
        String familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        presenter = new FamilyProfilePresenter(this, new FamilyProfileModel(familyName), familyBaseEntityId, familyHead, primaryCaregiver, familyName);
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        // Update profile border
        CircleImageView profileView = findViewById(R.id.imageview_profile);
        profileView.setBorderWidth(2);

        // add floating menu
        FamilyFloatingMenu familyFloatingMenu = new FamilyFloatingMenu(this);
        LinearLayout.LayoutParams linearLayoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        familyFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        addContentView(familyFloatingMenu, linearLayoutParams);
        familyFloatingMenu.setClickListener(new FloatingMenuListener(this, presenter().familyBaseEntityId()));
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        profileMemberFragment = FamilyProfileMemberFragment.newInstance(this.getIntent().getExtras());
        profileDueFragment = FamilyProfileDueFragment.newInstance(this.getIntent().getExtras());
        profileActivityFragment = FamilyProfileActivityFragment.newInstance(this.getIntent().getExtras());

        adapter.addFragment(profileMemberFragment, this.getString(org.smartregister.family.R.string.member).toUpperCase());
        adapter.addFragment(profileDueFragment, this.getString(org.smartregister.family.R.string.due).toUpperCase());
        adapter.addFragment(profileActivityFragment, this.getString(org.smartregister.family.R.string.activity).toUpperCase());

        viewPager.setAdapter(adapter);
        if (isFromFamilyServiceDue) {
            // int position=adapter.getItemPosition(profileDueFragment);
            viewPager.setCurrentItem(1);
            adapter.notifyDataSetChanged();
        }

        return viewPager;
    }

    public Bundle getProfileExtras() {
        return getIntent().getExtras();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem addMember = menu.findItem(R.id.add_member);
        if (addMember != null) {
            addMember.setVisible(false);
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PermissionUtils.PHONE_STATE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                Boolean granted = (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED);
                if (granted) {
                    PermissionEvent event = new PermissionEvent(requestCode, granted);
                    EventBus.getDefault().post(event);
                } else {
                    Toast.makeText(this, getText(R.string.allow_calls_denied), Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }

    public String getFamilyBaseEntityId() {
        return familyBaseEntityId;
    }

    public void startFormForEdit() {
        if (familyBaseEntityId != null) {
            ((FamilyProfilePresenter) presenter).fetchProfileData();
        }
    }

    // Child Form

    @Override
    public void startChildForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {
        ((FamilyProfilePresenter) presenter).startChildForm(formName, entityId, metadata, currentLocationId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case org.smartregister.family.util.JsonFormUtils.REQUEST_CODE_GET_JSON:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                        Log.d("JSONResult", jsonString);

                        JSONObject form = new JSONObject(jsonString);
                        if (form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE).equals(org.smartregister.family.util.Utils.metadata().familyRegister.registerEventType)
                                || form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE).equals(org.smartgresiter.jhpiego.util.Constants.EventType.CHILD_REGISTRATION)
                                ) {
                            ((FamilyProfilePresenter) presenter).saveChildForm(jsonString, false);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case org.smartgresiter.jhpiego.util.Constants.ProfileActivityResults.CHANGE_COMPLETED:
                if (resultCode == Activity.RESULT_OK) {
                    try {

                        String careGiverID = data.getStringExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
                        String familyHeadID = data.getStringExtra(Constants.INTENT_KEY.FAMILY_HEAD);

                        BaseFamilyProfileMemberFragment memberFragment = this.getProfileMemberFragment();
                        if (StringUtils.isNotBlank(careGiverID)) {
                            memberFragment.setPrimaryCaregiver(careGiverID);
                        }
                        if (StringUtils.isNotBlank(familyHeadID)) {
                            memberFragment.setFamilyHead(familyHeadID);
                        }
                        refreshMemberList(FetchStatus.fetched);
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

    }

    @Override
    public void refreshMemberList(FetchStatus fetchStatus) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            for (int i = 0; i < adapter.getCount(); i++) {
                refreshList(adapter.getItem(i));
            }
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    for (int i = 0; i < adapter.getCount(); i++) {
                        refreshList(adapter.getItem(i));
                    }
                }
            });
        }
    }

    private void refreshList(Fragment fragment) {
        if (fragment != null && fragment instanceof BaseRegisterFragment) {
            if (fragment instanceof FamilyProfileMemberFragment) {
                FamilyProfileMemberFragment familyProfileMemberFragment = ((FamilyProfileMemberFragment) fragment);
                if (familyProfileMemberFragment.presenter() != null) {
                    familyProfileMemberFragment.refreshListView();
                }
            } else if (fragment instanceof FamilyProfileDueFragment) {
                FamilyProfileDueFragment familyProfileDueFragment = ((FamilyProfileDueFragment) fragment);
                if (familyProfileDueFragment.presenter() != null) {
                    familyProfileDueFragment.refreshListView();
                }
            } else if (fragment instanceof FamilyProfileActivityFragment) {
                FamilyProfileActivityFragment familyProfileActivityFragment = ((FamilyProfileActivityFragment) fragment);
                if (familyProfileActivityFragment.presenter() != null) {
                    familyProfileActivityFragment.refreshListView();
                }
            }
        }
    }
}
