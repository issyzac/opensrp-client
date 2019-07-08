package org.smartgresiter.jhpiego.presenter;

import org.smartgresiter.jhpiego.util.ChildDBConstants;
import org.smartregister.family.contract.FamilyProfileDueContract;
import org.smartregister.family.presenter.BaseFamilyProfileDuePresenter;

public class FamilyProfileDuePresenter extends BaseFamilyProfileDuePresenter {

    public FamilyProfileDuePresenter(FamilyProfileDueContract.View view, FamilyProfileDueContract.Model model, String viewConfigurationIdentifier, String familyBaseEntityId) {
        super(view, model, viewConfigurationIdentifier, familyBaseEntityId);
    }

    @Override
    public String getMainCondition() {
        return super.getMainCondition() + " AND " + ChildDBConstants.childDueFilter();
    }

    @Override
    public String getDefaultSortQuery() {
        return ChildDBConstants.KEY.LAST_HOME_VISIT + ", " + ChildDBConstants.KEY.VISIT_NOT_DONE + " ASC ";
    }
}
