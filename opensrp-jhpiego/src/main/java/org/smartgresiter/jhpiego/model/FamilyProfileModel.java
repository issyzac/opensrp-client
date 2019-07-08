package org.smartgresiter.jhpiego.model;

import org.json.JSONObject;
import org.smartgresiter.jhpiego.util.JsonFormUtils;
import org.smartgresiter.jhpiego.util.Utils;
import org.smartregister.family.model.BaseFamilyProfileModel;

public class FamilyProfileModel extends BaseFamilyProfileModel {

    private String familyName;

    public FamilyProfileModel(String familyName) {
        this.familyName = familyName;
    }

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = super.getFormAsJson(formName, entityId, currentLocationId);
        if (formName.equals(Utils.metadata().familyMemberRegister.formName)) {
            JsonFormUtils.updateJsonForm(jsonObject, familyName);
        }

        return jsonObject;
    }
}
