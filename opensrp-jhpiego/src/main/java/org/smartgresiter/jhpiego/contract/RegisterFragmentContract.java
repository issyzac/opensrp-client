package org.smartgresiter.jhpiego.contract;

import org.smartregister.family.contract.FamilyRegisterFragmentContract;

public interface RegisterFragmentContract {

    interface Presenter extends FamilyRegisterFragmentContract.Presenter {

        String getDueFilterCondition();

    }

}
