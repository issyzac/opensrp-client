package org.smartgresiter.jhpiego.activity;

import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.custom_view.FamilyFloatingMenu;
import org.smartgresiter.jhpiego.fragment.FamilyOtherMemberProfileFragment;
import org.smartgresiter.jhpiego.listener.FloatingMenuListener;
import org.smartgresiter.jhpiego.model.FamilyOtherMemberProfileActivityModel;
import org.smartgresiter.jhpiego.presenter.FamilyOtherMemberActivityPresenter;
import org.smartregister.family.activity.BaseFamilyOtherMemberProfileActivity;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.fragment.BaseFamilyOtherMemberProfileFragment;
import org.smartregister.family.util.Constants;

public class FamilyOtherMemberProfileActivity extends BaseFamilyOtherMemberProfileActivity {
    @Override
    protected void initializePresenter() {
        String familyBaseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        String baseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID);
        String familyHead = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_HEAD);
        String primaryCaregiver = getIntent().getStringExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
        String villageTown = getIntent().getStringExtra(Constants.INTENT_KEY.VILLAGE_TOWN);
        String familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        presenter = new FamilyOtherMemberActivityPresenter(this, new FamilyOtherMemberProfileActivityModel(), null, familyBaseEntityId, baseEntityId, familyHead, primaryCaregiver, villageTown, familyName);
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(String.format(getString(R.string.return_to_family_name), presenter().getFamilyName()));

        // add floating menu
        FamilyFloatingMenu familyFloatingMenu = new FamilyFloatingMenu(this);
        LinearLayout.LayoutParams linearLayoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        familyFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        addContentView(familyFloatingMenu, linearLayoutParams);
        familyFloatingMenu.setClickListener(new FloatingMenuListener(this, presenter().getFamilyBaseEntityId()));
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        BaseFamilyOtherMemberProfileFragment profileOtherMemberFragment = FamilyOtherMemberProfileFragment.newInstance(this.getIntent().getExtras());
        adapter.addFragment(profileOtherMemberFragment, "");

        viewPager.setAdapter(adapter);

        return viewPager;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public FamilyOtherMemberActivityPresenter presenter() {
        return (FamilyOtherMemberActivityPresenter) presenter;
    }
}
