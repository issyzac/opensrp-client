package org.smartgresiter.jhpiego.listener;

import org.smartgresiter.jhpiego.util.ImmunizationState;

import java.util.Date;
import java.util.Map;

public interface FamilyMemberImmunizationListener {
    void onFamilyMemberState(ImmunizationState state);

    void onSelfStatus(Map<String, Date> vaccines, Map<String, Object> nv, ImmunizationState state);

}
