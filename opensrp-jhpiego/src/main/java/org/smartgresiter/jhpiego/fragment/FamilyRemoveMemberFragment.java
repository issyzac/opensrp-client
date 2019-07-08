package org.smartgresiter.jhpiego.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.activity.FamilyRegisterActivity;
import org.smartgresiter.jhpiego.contract.FamilyRemoveMemberContract;
import org.smartgresiter.jhpiego.model.FamilyRemoveMemberModel;
import org.smartgresiter.jhpiego.presenter.FamilyRemoveMemberPresenter;
import org.smartgresiter.jhpiego.provider.FamilyRemoveMemberProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

import java.util.HashMap;
import java.util.Set;

public class FamilyRemoveMemberFragment extends BaseFamilyProfileMemberFragment implements FamilyRemoveMemberContract.View {

    String familyBaseEntityId;
    String familyHead;
    String primaryCareGiver;

    public static FamilyRemoveMemberFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        FamilyRemoveMemberFragment fragment = new FamilyRemoveMemberFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns, String familyHead, String primaryCaregiver) {
        FamilyRemoveMemberProvider provider = new FamilyRemoveMemberProvider(familyBaseEntityId, this.getActivity(), this.commonRepository(), visibleColumns, new RemoveMemberListener(), new FooterListener(), familyHead, primaryCaregiver);
        this.clientAdapter = new RecyclerViewPaginatedAdapter((Cursor) null, provider, this.context().commonrepository(this.tablename));
        this.clientAdapter.setCurrentlimit(100);
        this.clientsView.setAdapter(this.clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        familyHead = getArguments().getString(Constants.INTENT_KEY.FAMILY_HEAD);
        primaryCareGiver = getArguments().getString(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
        presenter = new FamilyRemoveMemberPresenter(this, new FamilyRemoveMemberModel(), null, familyBaseEntityId, familyHead, primaryCareGiver);
    }

    public FamilyRemoveMemberContract.Presenter getPresenter() {
        return (FamilyRemoveMemberContract.Presenter) presenter;
    }

    @Override
    public void removeMember(CommonPersonObjectClient client) {
        getPresenter().removeMember(client);
    }

    @Override
    public void displayChangeFamilyHeadDialog(final CommonPersonObjectClient client, final String familyHeadID) {
        FamilyProfileChangeDialog dialog = FamilyProfileChangeDialog.newInstance(getContext(), familyBaseEntityId,
                org.smartgresiter.jhpiego.util.Constants.PROFILE_CHANGE_ACTION.HEAD_OF_FAMILY);
        dialog.setOnSaveAndClose(new Runnable() {
            @Override
            public void run() {
                setFamilyHead(familyHeadID);
                refreshMemberList(FetchStatus.fetched);
                getPresenter().removeMember(client);
            }
        });
        dialog.show(getActivity().getFragmentManager(), "FamilyProfileChangeDialogHF");
    }

    @Override
    public void displayChangeCareGiverDialog(final CommonPersonObjectClient client, final String careGiverID) {
        FamilyProfileChangeDialog dialog = FamilyProfileChangeDialog.newInstance(getContext(), familyBaseEntityId,
                org.smartgresiter.jhpiego.util.Constants.PROFILE_CHANGE_ACTION.PRIMARY_CARE_GIVER);
        dialog.setOnSaveAndClose(new Runnable() {
            @Override
            public void run() {
                setPrimaryCaregiver(careGiverID);
                refreshMemberList(FetchStatus.fetched);
                getPresenter().removeMember(client);
            }
        });

        dialog.show(getActivity().getFragmentManager(), "FamilyProfileChangeDialogPC");
    }

    @Override
    public void closeFamily(String details) {

        getPresenter().removeEveryone(details);

    }

    @Override
    public void goToPrevious() {
        // open family register
        startActivity(new Intent(getContext(), FamilyRegisterActivity.class));
    }

    @Override
    public void startJsonActivity(JSONObject jsonObject) {
        // Intent intent = new Intent(getContext(), Utils.metadata().familyMemberFormActivity);
        Intent intent = new Intent(getActivity(), Utils.metadata().familyMemberFormActivity);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonObject.toString());

        Form form = new Form();
        form.setActionBarBackground(org.smartregister.family.R.color.family_actionbar);
        form.setWizard(false);
        form.setSaveLabel("Remove");
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void onMemberRemoved(String removalType) {
        // display alert
        if (getActivity() != null) {
            if (org.smartgresiter.jhpiego.util.Constants.EventType.REMOVE_FAMILY.equalsIgnoreCase(removalType)) {
                Intent intent = new Intent(getActivity(), FamilyRegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                getActivity().finish();
            }
        }
    }

    @Override
    public void onEveryoneRemoved() {
        // close family and return to main register
        Intent intent = new Intent(getActivity(), FamilyRegisterActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    public class RemoveMemberListener implements android.view.View.OnClickListener {
        @Override
        public void onClick(android.view.View v) {
            if (v.getTag(R.id.VIEW_ID) == BaseFamilyProfileMemberFragment.CLICK_VIEW_NEXT_ARROW ||
                    v.getTag(R.id.VIEW_ID) == BaseFamilyProfileMemberFragment.CLICK_VIEW_NORMAL) {
                final CommonPersonObjectClient pc = (CommonPersonObjectClient) v.getTag();

                String name = String.format("%s %s %s", pc.getColumnmaps().get(DBConstants.KEY.FIRST_NAME),
                        pc.getColumnmaps().get(DBConstants.KEY.MIDDLE_NAME),
                        pc.getColumnmaps().get(DBConstants.KEY.LAST_NAME));

                String dod = pc.getColumnmaps().get(DBConstants.KEY.DOD);

                if (StringUtils.isBlank(dod)) {
                    FamilyRemoveMemberConfrimDialog dialog = FamilyRemoveMemberConfrimDialog.newInstance(
                            String.format("Are you sure you want to remove %s's record? This will remove their entire health record from your device. This action cannot be undone.", name)
                    );
                    dialog.setContext(getContext());
                    dialog.show(getFragmentManager(), AddMemberFragment.DIALOG_TAG);
                    dialog.setOnRemove(new Runnable() {
                        @Override
                        public void run() {
                            removeMember(pc);
                        }
                    });
                }
            }
        }
    }

    public class FooterListener implements android.view.View.OnClickListener {
        @Override
        public void onClick(final android.view.View v) {

            HashMap<String, String> payload = (HashMap<String, String>) v.getTag();
            final String message = payload.get("message");
            final String name = payload.get("name");

            FamilyRemoveMemberConfrimDialog dialog = FamilyRemoveMemberConfrimDialog.newInstance(
                    String.format("Are you sure you want to remove the %s Family's record? This will remove the family's entire record AND all of the %s " +
                                    "family members' health records from your device. This action cannot be undone.",
                            name, name)
            );

            dialog.setContext(getContext());
            dialog.show(getFragmentManager(), AddMemberFragment.DIALOG_TAG);
            dialog.setOnRemove(new Runnable() {
                @Override
                public void run() {
                    closeFamily(message);
                    Toast.makeText(getContext(), "Removing entire family", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void refreshMemberList(final FetchStatus fetchStatus) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (fetchStatus.equals(FetchStatus.fetched)) {
                refreshListView();
            }
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    if (fetchStatus.equals(FetchStatus.fetched)) {
                        refreshListView();
                    }

                }
            });
        }

    }

    @Override
    protected String getMainCondition() {
        return presenter().getMainCondition();
    }

    @Override
    protected String getDefaultSortQuery() {
        return presenter().getDefaultSortQuery();
    }

}
