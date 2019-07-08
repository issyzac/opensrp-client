package org.smartgresiter.jhpiego.listener;

import android.app.Activity;
import android.content.Intent;

import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.activity.ChildProfileActivity;
import org.smartgresiter.jhpiego.activity.FamilyProfileActivity;
import org.smartgresiter.jhpiego.activity.FamilyProfileMenuActivity;
import org.smartgresiter.jhpiego.activity.FamilyRemoveMemberActivity;
import org.smartgresiter.jhpiego.fragment.AddMemberFragment;
import org.smartgresiter.jhpiego.fragment.FamilyCallDialogFragment;
import org.smartgresiter.jhpiego.util.Constants;

public class FloatingMenuListener implements OnClickFloatingMenu {

    private Activity context;
    private String familyBaseEntityId;

    public FloatingMenuListener(Activity context, String familyBaseEntityId) {
        this.context = context;
        this.familyBaseEntityId = familyBaseEntityId;
    }

    @Override
    public void onClickMenu(int viewId) {
        switch (viewId) {
            case R.id.call_layout:
                // Toast.makeText(context, "Go to call screen", Toast.LENGTH_SHORT).show();
                FamilyCallDialogFragment.launchDialog(context, familyBaseEntityId);
                //go to child add form activity
                break;
            case R.id.family_detail_layout:

                ((FamilyProfileActivity) context).startFormForEdit();

                break;
            case R.id.add_new_member_layout:
                ((FamilyProfileActivity) context).startFormActivity(Constants.JSON_FORM.FAMILY_MEMBER_REGISTER, null, null);

//                AddMemberFragment addmemberFragment = AddMemberFragment.newInstance();
//                addmemberFragment.setContext(context);
//                addmemberFragment.show(context.getFragmentManager(), AddMemberFragment.DIALOG_TAG);

                break;

            case R.id.remove_member_layout:

                Intent frm_intent = new Intent(context, FamilyRemoveMemberActivity.class);
                frm_intent.putExtras(context.getIntent().getExtras());
                context.startActivityForResult(frm_intent, Constants.ProfileActivityResults.CHANGE_COMPLETED);

                break;
            case R.id.change_head_layout:

                Intent fh_intent = new Intent(context, FamilyProfileMenuActivity.class);
                fh_intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, familyBaseEntityId);
                fh_intent.putExtra(FamilyProfileMenuActivity.MENU, FamilyProfileMenuActivity.MenuType.ChangeHead);
                context.startActivityForResult(fh_intent, Constants.ProfileActivityResults.CHANGE_COMPLETED);

                break;
            case R.id.change_primary_layout:

                Intent pc_intent = new Intent(context, FamilyProfileMenuActivity.class);
                pc_intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, familyBaseEntityId);
                pc_intent.putExtra(FamilyProfileMenuActivity.MENU, FamilyProfileMenuActivity.MenuType.ChangePrimaryCare);
                context.startActivityForResult(pc_intent, Constants.ProfileActivityResults.CHANGE_COMPLETED);

                break;
            case R.id.refer_to_facility_layout:
                ((ChildProfileActivity) context).startFormForEdit();

                break;
        }
    }
}
