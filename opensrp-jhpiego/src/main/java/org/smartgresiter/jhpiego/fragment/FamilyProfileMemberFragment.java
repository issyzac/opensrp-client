package org.smartgresiter.jhpiego.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.activity.ChildProfileActivity;
import org.smartgresiter.jhpiego.activity.FamilyOtherMemberProfileActivity;
import org.smartgresiter.jhpiego.model.FamilyProfileMemberModel;
import org.smartgresiter.jhpiego.presenter.FamilyProfileMemberPresenter;
import org.smartgresiter.jhpiego.util.ChildDBConstants;
import org.smartgresiter.jhpiego.util.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.util.Constants;

public class FamilyProfileMemberFragment extends BaseFamilyProfileMemberFragment {

    public static BaseFamilyProfileMemberFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        BaseFamilyProfileMemberFragment fragment = new FamilyProfileMemberFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializePresenter() {
        String familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        String familyHead = getArguments().getString(Constants.INTENT_KEY.FAMILY_HEAD);
        String primaryCareGiver = getArguments().getString(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
        presenter = new FamilyProfileMemberPresenter(this, new FamilyProfileMemberModel(), null, familyBaseEntityId, familyHead, primaryCareGiver);
    }


    @Override
    protected void onViewClicked(View view) {
        super.onViewClicked(view);
        switch (view.getId()) {
            case R.id.patient_column:
                if (view.getTag() != null && view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
                    goToProfileActivity(view);
                }
                break;
            case R.id.next_arrow:
                if (view.getTag() != null && view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NEXT_ARROW) {
                    goToProfileActivity(view);
                }
            default:
                break;
        }
    }

    public void goToProfileActivity(View view) {
        CommonPersonObjectClient commonPersonObjectClient = (CommonPersonObjectClient) view.getTag();
        String entityType = Utils.getValue(commonPersonObjectClient.getColumnmaps(), ChildDBConstants.KEY.ENTITY_TYPE, false);
        if (org.smartgresiter.jhpiego.util.Constants.TABLE_NAME.FAMILY_MEMBER.equals(entityType)) {
            goToOtherMemberProfileActivity(commonPersonObjectClient);
        } else {
            goToChildProfileActivity(commonPersonObjectClient);
        }
    }

    public void goToOtherMemberProfileActivity(CommonPersonObjectClient patient) {
        Intent intent = new Intent(getActivity(), FamilyOtherMemberProfileActivity.class);
        intent.putExtras(getArguments());
        intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        startActivity(intent);
    }

    public void goToChildProfileActivity(CommonPersonObjectClient patient) {
        Intent intent = new Intent(getActivity(), ChildProfileActivity.class);
        intent.putExtras(getArguments());
        intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        startActivity(intent);
    }
}
