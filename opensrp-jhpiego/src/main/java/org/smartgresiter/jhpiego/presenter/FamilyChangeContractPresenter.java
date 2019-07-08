package org.smartgresiter.jhpiego.presenter;

import android.content.Context;

import org.smartgresiter.jhpiego.contract.FamilyChangeContract;
import org.smartgresiter.jhpiego.interactor.FamilyChangeContractInteractor;
import org.smartgresiter.jhpiego.model.FamilyChangeContractModel;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.view.LocationPickerView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

public class FamilyChangeContractPresenter implements FamilyChangeContract.Presenter {

    protected WeakReference<FamilyChangeContract.View> view;
    protected FamilyChangeContract.Model model;
    protected FamilyChangeContract.Interactor interactor;
    protected String familyID;

    public FamilyChangeContractPresenter(FamilyChangeContract.View view, String familyID) {
        this.view = new WeakReference<>(view);
        this.familyID = familyID;
        this.model = new FamilyChangeContractModel();
        this.interactor = new FamilyChangeContractInteractor();
    }

    @Override
    public void getAdultMembersExcludePCG() {
        if (view != null && view.get() != null) {
            interactor.getAdultMembersExcludePCG(familyID, this);
        }
    }

    @Override
    public void saveFamilyMember(Context context, HashMap<String, String> member) {

        LocationPickerView lpv = new LocationPickerView(context);
        lpv.init();
        String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem());

        interactor.updateFamilyMember(context, member, familyID, lastLocationId, this);
    }

    @Override
    public void renderAdultMembersExcludePCG(List<HashMap<String, String>> clients, String primaryCareID, String headOfHouseID) {
        if (view != null && view.get() != null) {
            List<HashMap<String, String>> res = model.getMembersExcluding(clients, primaryCareID, headOfHouseID, primaryCareID);
            view.get().refreshMembersView(res);
        }
    }

    @Override
    public void getAdultMembersExcludeHOF() {
        if (view != null && view.get() != null) {
            interactor.getAdultMembersExcludeHOF(familyID, this);
        }
    }

    @Override
    public void renderAdultMembersExcludeHOF(List<HashMap<String, String>> clients, String primaryCareID, String headOfHouseID) {
        if (view != null && view.get() != null) {
            List<HashMap<String, String>> res = model.getMembersExcluding(clients, primaryCareID, headOfHouseID, headOfHouseID);
            view.get().refreshMembersView(res);
        }
    }


    @Override
    public void saveCompleted(String familyHeadID, String careGiverID) {
        if (view != null && view.get() != null) {
            view.get().saveComplete(familyHeadID, careGiverID);
        }
    }

    @Override
    public void getMembers(String familyID) {

    }
}

