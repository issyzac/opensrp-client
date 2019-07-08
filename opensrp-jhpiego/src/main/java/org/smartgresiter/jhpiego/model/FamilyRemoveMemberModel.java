package org.smartgresiter.jhpiego.model;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartgresiter.jhpiego.contract.FamilyRemoveMemberContract;
import org.smartgresiter.jhpiego.util.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.util.Date;

public class FamilyRemoveMemberModel extends FamilyProfileMemberModel implements FamilyRemoveMemberContract.Model {

    @Override
    public JSONObject prepareJsonForm(CommonPersonObjectClient client, String formType) {
        try {
            FormUtils formUtils = FormUtils.getInstance(Utils.context().applicationContext());
            JSONObject form = formUtils.getFormJson(formType);

            form.put(JsonFormUtils.ENTITY_ID, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false));
            // inject data into the form

            JSONObject stepOne = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.DOB)) {

                    String dobString = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
                    if (StringUtils.isNotBlank(dobString)) {
                        Date dob = Utils.dobStringToDate(dobString);
                        if (dob != null) {
                            jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, JsonFormUtils.dd_MM_yyyy.format(dob));
                        }
                    }
                } else if (jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).equalsIgnoreCase("details")) {

                    String dob = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
                    String dobString = Utils.getDuration(dob);
                    dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;

                    String details = String.format("%s %s, %s %s",
                            Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true),
                            Utils.getValue(client.getColumnmaps(), DBConstants.KEY.LAST_NAME, true),
                            dobString,
                            Utils.getValue(client.getColumnmaps(), DBConstants.KEY.GENDER, true)
                    );

                    jsonObject.put("text", details);

                }
            }

            return form;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONObject prepareFamilyRemovalForm(String familyID, String details) {
        try {
            FormUtils formUtils = FormUtils.getInstance(Utils.context().applicationContext());
            JSONObject form = formUtils.getFormJson(Constants.JSON_FORM.FAMILY_DETAILS_REMOVE_FAMILY);
            form.put(JsonFormUtils.ENTITY_ID, familyID);

            JSONObject stepOne = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).equalsIgnoreCase("details")) {
                    jsonObject.put("text", details);
                }
            }

            return form;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

