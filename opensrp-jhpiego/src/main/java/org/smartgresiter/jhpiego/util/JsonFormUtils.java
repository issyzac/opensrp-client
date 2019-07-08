package org.smartgresiter.jhpiego.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartgresiter.jhpiego.application.JhpiegoApplication;
import org.smartgresiter.jhpiego.repository.JhpiegoRepository;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.FormEntityConstants;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.domain.ProfileImage;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.ImageRepository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.FormUtils;
import org.smartregister.util.ImageUtils;
import org.smartregister.view.LocationPickerView;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by keyman on 13/11/2018.
 */
public class JsonFormUtils extends org.smartregister.family.util.JsonFormUtils {
    public static final String METADATA = "metadata";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final int REQUEST_CODE_GET_JSON = 2244;
    public static final String CURRENT_OPENSRP_ID = "current_opensrp_id";
    public static final String READ_ONLY = "read_only";
    private static final String TAG = org.smartregister.util.JsonFormUtils.class.getCanonicalName();
    private static HashMap<String, String> actionMap = null;

    public static JSONObject getBirthCertFormAsJson(JSONObject form, String baseEntityId, String currentLocationId, String dateOfBirthString) throws Exception {

        if (form == null) {
            return null;
        }
        dateOfBirthString = dateOfBirthString.contains("y") ? dateOfBirthString.substring(0, dateOfBirthString.indexOf("y")) : "";
        form.getJSONObject(METADATA).put(ENCOUNTER_LOCATION, currentLocationId);
        form.put(ENTITY_ID, baseEntityId);
        JSONArray field = fields(form);
        JSONObject mindate = getFieldJSONObject(field, "birth_cert_issue_date");
        //if(mindate!=null){
        mindate.put("min_date", "today-" + dateOfBirthString + "y");
        //}
        return form;

    }

    public static JSONObject getOnsIllnessFormAsJson(JSONObject form, String baseEntityId, String currentLocationId, String dateOfBirthString) throws Exception {

        if (form == null) {
            return null;
        }
        dateOfBirthString = dateOfBirthString.contains("y") ? dateOfBirthString.substring(0, dateOfBirthString.indexOf("y")) : "";
        form.getJSONObject(METADATA).put(ENCOUNTER_LOCATION, currentLocationId);
        form.put(ENTITY_ID, baseEntityId);
        JSONArray field = fields(form);
        JSONObject mindate = getFieldJSONObject(field, "date_of_illness");
        //if(mindate!=null){
        mindate.put("min_date", "today-" + dateOfBirthString + "y");
        //}
        return form;

    }

    public static Pair<Client, Event> processBirthAndIllnessForm(AllSharedPreferences allSharedPreferences, String jsonString) {
        try {

            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);
            if (!registrationFormParams.getLeft()) {
                return null;
            }

            JSONObject jsonForm = registrationFormParams.getMiddle();
            JSONArray fields = registrationFormParams.getRight();
            String entityId = getString(jsonForm, ENTITY_ID);
            String encounterType = getString(jsonForm, ENCOUNTER_TYPE);
            JSONObject metadata = getJSONObject(jsonForm, METADATA);
            JSONObject lastInteractedWith = new JSONObject();
            lastInteractedWith.put(org.smartregister.family.util.Constants.KEY.KEY, DBConstants.KEY.LAST_INTERACTED_WITH);
            lastInteractedWith.put(org.smartregister.family.util.Constants.KEY.VALUE, Calendar.getInstance().getTimeInMillis());

            fields.put(lastInteractedWith);
            String birthCert = org.smartregister.family.util.JsonFormUtils.getFieldValue(jsonString, "birth_cert");
            if (!TextUtils.isEmpty(birthCert) && birthCert.equalsIgnoreCase("Yes")) {
                JSONObject dobJSONObject = getFieldJSONObject(fields, "birth_notification");
                dobJSONObject.put(org.smartregister.family.util.Constants.KEY.VALUE, "No");
                fields.put(dobJSONObject);
            }
            FormTag formTag = new FormTag();
            formTag.providerId = allSharedPreferences.fetchRegisteredANM();
            formTag.appVersion = FamilyLibrary.getInstance().getApplicationVersion();
            formTag.databaseVersion = FamilyLibrary.getInstance().getDatabaseVersion();


            Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(fields, formTag, entityId);
            Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, metadata, formTag, entityId, encounterType, org.smartgresiter.jhpiego.util.Constants.TABLE_NAME.CHILD);
            String illness_acton = org.smartregister.family.util.JsonFormUtils.getFieldValue(jsonString, "action_taken");
            if (!TextUtils.isEmpty(illness_acton)) {
                baseEvent.addObs(new Obs("concept", "text", org.smartgresiter.jhpiego.util.Constants.FORM_CONSTANTS.ILLNESS_ACTION_TAKEN_LEVEL.CODE, "",
                        toList(actionMap().get(illness_acton)), toList(illness_acton), null, DBConstants.KEY.HIGHEST_EDU_LEVEL));

            }
            tagSyncMetadata(allSharedPreferences, baseEvent);// tag docs

            return Pair.create(baseClient, baseEvent);
        } catch (Exception e) {
            return null;
        }

    }

    private static HashMap<String, String> actionMap() {
        if (actionMap == null) {
            actionMap = new HashMap<>();
            actionMap.put("Managed", "140959AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            actionMap.put("Referred", "159494AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        }
        return actionMap;
    }

    private static List<Object> toList(String... vals) {
        List<Object> res = new ArrayList<>();
        for (String s : vals) {
            res.add(s);
        }
        return res;
    }

    public static JSONObject getFormAsJson(JSONObject form,
                                           String formName, String id,
                                           String currentLocationId, String familyID) throws Exception {
        if (form == null) {
            return null;
        }

        String entityId = id;
        form.getJSONObject(METADATA).put(ENCOUNTER_LOCATION, currentLocationId);

        if (Utils.metadata().familyRegister.formName.equals(formName) || Utils.metadata().familyMemberRegister.formName.equals(formName) || formName.equalsIgnoreCase("child_enrollment")) {
            if (StringUtils.isNotBlank(entityId)) {
                entityId = entityId.replace("-", "");
            }

            // Inject opensrp id into the form
            JSONArray field = fields(form);
            JSONObject uniqueId = getFieldJSONObject(field, DBConstants.KEY.UNIQUE_ID);
            if (uniqueId != null) {
                uniqueId.remove(org.smartregister.family.util.JsonFormUtils.VALUE);
                uniqueId.put(org.smartregister.family.util.JsonFormUtils.VALUE, entityId);
            }

            if (!isBlank(familyID)) {
                JSONObject metaDataJson = form.getJSONObject("metadata");
                JSONObject lookup = metaDataJson.getJSONObject("look_up");
                lookup.put("entity_id", "family");
                lookup.put("value", familyID);
            }

        } else {
            Log.w(TAG, "Unsupported form requested for launch " + formName);
        }
        Log.d(TAG, "form is " + form.toString());
        return form;
    }


    public static Pair<Client, Event> processChildRegistrationForm(AllSharedPreferences allSharedPreferences, String jsonString) {

        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

            if (!registrationFormParams.getLeft()) {
                return null;
            }

            JSONObject jsonForm = registrationFormParams.getMiddle();
            JSONArray fields = registrationFormParams.getRight();

            String entityId = getString(jsonForm, ENTITY_ID);
            if (isBlank(entityId)) {
                entityId = generateRandomUUIDString();
            }

            String encounterType = getString(jsonForm, ENCOUNTER_TYPE);
            JSONObject metadata = getJSONObject(jsonForm, METADATA);

            // String lastLocationName = null;
            // String lastLocationId = null;
            // TODO Replace values for location questions with their corresponding location IDs


            JSONObject lastInteractedWith = new JSONObject();
            lastInteractedWith.put(org.smartregister.family.util.Constants.KEY.KEY, DBConstants.KEY.LAST_INTERACTED_WITH);
            lastInteractedWith.put(org.smartregister.family.util.Constants.KEY.VALUE, Calendar.getInstance().getTimeInMillis());
            fields.put(lastInteractedWith);

            JSONObject dobUnknownObject = getFieldJSONObject(fields, Constants.JSON_FORM_KEY.DOB_UNKNOWN);
            JSONArray options = getJSONArray(dobUnknownObject, Constants.JSON_FORM_KEY.OPTIONS);
            JSONObject option = getJSONObject(options, 0);
            String dobUnKnownString = option != null ? option.getString(VALUE) : null;
            if (StringUtils.isNotBlank(dobUnKnownString) && Boolean.valueOf(dobUnKnownString)) {

                String ageString = getFieldValue(fields, Constants.JSON_FORM_KEY.AGE);
                if (StringUtils.isNotBlank(ageString) && NumberUtils.isNumber(ageString)) {
                    int age = Integer.valueOf(ageString);
                    JSONObject dobJSONObject = getFieldJSONObject(fields, Constants.JSON_FORM_KEY.DOB);
                    dobJSONObject.put(VALUE, Utils.getDob(age));

                    //Mark the birth date as an approximation
                    JSONObject isBirthdateApproximate = new JSONObject();
                    isBirthdateApproximate.put(Constants.KEY.KEY, FormEntityConstants.Person.birthdate_estimated);
                    isBirthdateApproximate.put(Constants.KEY.VALUE, Constants.BOOLEAN_INT.TRUE);
                    isBirthdateApproximate.put(Constants.OPENMRS.ENTITY, Constants.ENTITY.PERSON);//Required for value to be processed
                    isBirthdateApproximate.put(Constants.OPENMRS.ENTITY_ID, FormEntityConstants.Person.birthdate_estimated);
                    fields.put(isBirthdateApproximate);

                }
            }


            processChildEnrollMent(jsonForm, fields);

            FormTag formTag = new FormTag();
            formTag.providerId = allSharedPreferences.fetchRegisteredANM();
            formTag.appVersion = FamilyLibrary.getInstance().getApplicationVersion();
            formTag.databaseVersion = FamilyLibrary.getInstance().getDatabaseVersion();

            Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(fields, formTag, entityId);

            Event baseEvent = org.smartregister.util.JsonFormUtils.createEvent(fields, metadata, formTag, entityId, encounterType, org.smartgresiter.jhpiego.util.Constants.TABLE_NAME.CHILD);
            tagSyncMetadata(allSharedPreferences, baseEvent);

            JSONObject lookUpJSONObject = getJSONObject(metadata, "look_up");
            String lookUpEntityId = "";
            String lookUpBaseEntityId = "";
            if (lookUpJSONObject != null) {
                lookUpEntityId = getString(lookUpJSONObject, "entity_id");
                lookUpBaseEntityId = getString(lookUpJSONObject, "value");
            }
            if (lookUpEntityId.equals("family") && StringUtils.isNotBlank(lookUpBaseEntityId)) {
                Client ss = new Client(lookUpBaseEntityId);
                Context context = JhpiegoApplication.getInstance().getContext().applicationContext();
                addRelationship(context, ss, baseClient);
                SQLiteDatabase db = JhpiegoApplication.getInstance().getRepository().getReadableDatabase();
                JhpiegoRepository pathRepository = new JhpiegoRepository(context, JhpiegoApplication.getInstance().getContext());
                EventClientRepository eventClientRepository = new EventClientRepository(pathRepository);
                JSONObject clientjson = eventClientRepository.getClient(db, lookUpBaseEntityId);
                baseClient.setAddresses(getAddressFromClientJson(clientjson));
            }


            return Pair.create(baseClient, baseEvent);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    private static void processChildEnrollMent(JSONObject jsonForm, JSONArray fields) {

        try {

            JSONObject surnam_familyName_SameObject = getFieldJSONObject(fields, "surname_same_as_family_name");
            JSONArray surnam_familyName_Same_options = getJSONArray(surnam_familyName_SameObject, org.smartregister.family.util.Constants.JSON_FORM_KEY.OPTIONS);
            JSONObject surnam_familyName_Same_option = getJSONObject(surnam_familyName_Same_options, 0);
            String surnam_familyName_SameString = surnam_familyName_Same_option != null ? surnam_familyName_Same_option.getString(VALUE) : null;

            if (StringUtils.isNotBlank(surnam_familyName_SameString) && Boolean.valueOf(surnam_familyName_SameString)) {
                String familyId = jsonForm.getJSONObject("metadata").getJSONObject("look_up").getString("value");
                CommonPersonObject familyObject = JhpiegoApplication.getInstance().getContext().commonrepository("ec_family").findByCaseID(familyId);
                String lastname = familyObject.getColumnmaps().get(DBConstants.KEY.LAST_NAME);
                JSONObject surname_object = getFieldJSONObject(fields, "surname");
                surname_object.put(VALUE, lastname);
            }
        } catch (Exception e) {

        }

    }

    private static void addRelationship(Context context, Client parent, Client child) {
        try {
            String relationships = AssetHandler.readFileFromAssetsFolder(FormUtils.ecClientRelationships, context);
            JSONArray jsonArray = null;

            jsonArray = new JSONArray(relationships);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject rObject = jsonArray.getJSONObject(i);
                if (rObject.has("field") && getString(rObject, "field").equals(ENTITY_ID)) {
                    child.addRelationship(rObject.getString("client_relationship"), parent.getBaseEntityId());
                } /* else {
                    //TODO how to add other kind of relationships
                  } */
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    private static ArrayList<Address> getAddressFromClientJson(JSONObject clientjson) {
        ArrayList<Address> addresses = new ArrayList<Address>();
        try {
            JSONArray addressArray = clientjson.getJSONArray("addresses");
            for (int i = 0; i < addressArray.length(); i++) {
                Address address = new Address();
                address.setAddressType(addressArray.getJSONObject(i).getString("addressType"));
                JSONObject addressfields = addressArray.getJSONObject(i).getJSONObject("addressFields");

                Iterator<?> keys = addressfields.keys();

                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (addressfields.get(key) instanceof String) {
                        address.addAddressField(key, addressfields.getString(key));
                    }
                }
                addresses.add(address);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    public static void mergeAndSaveClient(ECSyncHelper ecUpdater, Client baseClient) throws Exception {
        JSONObject updatedClientJson = new JSONObject(org.smartregister.util.JsonFormUtils.gson.toJson(baseClient));

        JSONObject originalClientJsonObject = ecUpdater.getClient(baseClient.getBaseEntityId());

        JSONObject mergedJson = org.smartregister.util.JsonFormUtils.merge(originalClientJsonObject, updatedClientJson);

        //TODO Save edit log ?

        ecUpdater.addClient(baseClient.getBaseEntityId(), mergedJson);
    }

    public static void saveImage(String providerId, String entityId, String imageLocation) {
        if (isBlank(imageLocation)) {
            return;
        }

        File file = new File(imageLocation);

        if (!file.exists()) {
            return;
        }

        Bitmap compressedImageFile = FamilyLibrary.getInstance().getCompressor().compressToBitmap(file);
        saveStaticImageToDisk(compressedImageFile, providerId, entityId);

    }

    public static JSONObject getAutoPopulatedJsonEditFormString(String formName, Context context, CommonPersonObjectClient client) {
        try {
            JSONObject form = FormUtils.getInstance(context).getFormJson(formName);
            LocationPickerView lpv = new LocationPickerView(context);
            lpv.init();
            // JsonFormUtils.addWomanRegisterHierarchyQuestions(form);
            Log.d(TAG, "Form is " + form.toString());
            if (form != null) {
                form.put(org.smartregister.family.util.JsonFormUtils.ENTITY_ID, client.getCaseId());
                form.put(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE, Utils.metadata().familyRegister.updateEventType);

                JSONObject metadata = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.METADATA);
                String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem());

                metadata.put(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_LOCATION, lastLocationId);

                form.put(org.smartregister.family.util.JsonFormUtils.CURRENT_OPENSRP_ID, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false));

                //inject opensrp id into the form
                JSONObject stepOne = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    processPopulatableFields(client, jsonObject);

                }

                return form;
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    public static JSONObject getAutoPopulatedJsonEditFormString(Context context, CommonPersonObjectClient client) {
        return getAutoPopulatedJsonEditFormString(Utils.metadata().familyRegister.formName, context, client);
    }

    protected static void processPopulatableFields(CommonPersonObjectClient client, JSONObject jsonObject) throws JSONException {

        switch (jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).toLowerCase()) {
            case Constants.JSON_FORM_KEY.DOB_UNKNOWN:
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, false);
                JSONObject optionsObject = jsonObject.getJSONArray(Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
                optionsObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), Constants.JSON_FORM_KEY.DOB_UNKNOWN, false));

                break;
            case DBConstants.KEY.DOB:

                String dobString = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
                if (StringUtils.isNotBlank(dobString)) {
                    Date dob = Utils.dobStringToDate(dobString);
                    if (dob != null) {
                        jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, dd_MM_yyyy.format(dob));
                    }
                }

                break;

            case org.smartregister.family.util.Constants.KEY.PHOTO:

                Photo photo = ImageUtils.profilePhotoByClientID(client.getCaseId(), Utils.getProfileImageResourceIDentifier());
                if (StringUtils.isNotBlank(photo.getFilePath())) {
                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, photo.getFilePath());
                }

                break;

            case DBConstants.KEY.UNIQUE_ID:

                String uniqueId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, uniqueId.replace("-", ""));

                break;

            case "fam_name":

                String fam_name = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FIRST_NAME, false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, fam_name);

                break;

            case DBConstants.KEY.VILLAGE_TOWN:

                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, false));

                break;

            case DBConstants.KEY.QUATER_CLAN:

                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.QUATER_CLAN, false));

                break;

            case DBConstants.KEY.STREET:

                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.STREET, false));

                break;

            case DBConstants.KEY.LANDMARK:

                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.LANDMARK, false));

                break;

            case DBConstants.KEY.FAMILY_SOURCE_INCOME:

                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FAMILY_SOURCE_INCOME, false));

                break;

            case DBConstants.KEY.GPS:

                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.GPS, false));

                break;

            default:

                Log.e(TAG, "ERROR:: Unprocessed Form Object Key " + jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY));

                break;

        }

        if (jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.DOB)) {

            jsonObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, false);
            JSONObject optionsObject = jsonObject.getJSONArray(Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
            optionsObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false));

        }
    }

    private static void saveStaticImageToDisk(Bitmap image, String providerId, String entityId) {
        if (image == null || isBlank(providerId) || isBlank(entityId)) {
            return;
        }
        OutputStream os = null;
        try {

            if (entityId != null && !entityId.isEmpty()) {
                final String absoluteFileName = DrishtiApplication.getAppDir() + File.separator + entityId + ".JPEG";

                File outputFile = new File(absoluteFileName);
                os = new FileOutputStream(outputFile);
                Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
                if (compressFormat != null) {
                    image.compress(compressFormat, 100, os);
                } else {
                    throw new IllegalArgumentException("Failed to save static image, could not retrieve image compression format from name "
                            + absoluteFileName);
                }
                // insert into the db
                ProfileImage profileImage = new ProfileImage();
                profileImage.setImageid(UUID.randomUUID().toString());
                profileImage.setAnmId(providerId);
                profileImage.setEntityID(entityId);
                profileImage.setFilepath(absoluteFileName);
                profileImage.setFilecategory("profilepic");
                profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);
                ImageRepository imageRepo = Utils.context().imageRepository();
                imageRepo.add(profileImage);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to save static image to disk");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close static images output stream after attempting to write image");
                }
            }
        }

    }

    protected static Triple<Boolean, JSONObject, JSONArray> validateParameters(String jsonString) {

        JSONObject jsonForm = toJSONObject(jsonString);
        JSONArray fields = fields(jsonForm);

        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = Triple.of(jsonForm != null && fields != null, jsonForm, fields);
        return registrationFormParams;
    }

    public static Event tagSyncMetadata(AllSharedPreferences allSharedPreferences, Event event) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        event.setProviderId(providerId);
        event.setLocationId(locationId(allSharedPreferences));
        event.setChildLocationId(allSharedPreferences.fetchCurrentLocality());
        event.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        event.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));

        event.setClientApplicationVersion(FamilyLibrary.getInstance().getApplicationVersion());
        event.setClientDatabaseVersion(FamilyLibrary.getInstance().getDatabaseVersion());

        return event;
    }

    public static Vaccine tagSyncMetadata(AllSharedPreferences allSharedPreferences, Vaccine vaccine) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        vaccine.setAnmId(providerId);
        vaccine.setLocationId(locationId(allSharedPreferences));
        vaccine.setChildLocationId(allSharedPreferences.fetchCurrentLocality());
        vaccine.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        vaccine.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));
        return vaccine;
    }

    public static ServiceRecord tagSyncMetadata(AllSharedPreferences allSharedPreferences, ServiceRecord serviceRecord) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        serviceRecord.setAnmId(providerId);
        serviceRecord.setLocationId(locationId(allSharedPreferences));
        serviceRecord.setChildLocationId(allSharedPreferences.fetchCurrentLocality());
        serviceRecord.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        serviceRecord.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));
        return serviceRecord;
    }

    /**
     * @param familyID
     * @param allSharedPreferences
     * @param jsonObject
     * @param providerId
     * @return Returns a triple object <b>DateOfDeath as String, BaseEntityID , List of Events </b>that should be processed
     */
    public static Triple<Pair<Date, String>, String, List<Event>> processRemoveMemberEvent(String familyID, AllSharedPreferences allSharedPreferences, JSONObject jsonObject, String providerId) {

        try {

            List<Event> events = new ArrayList<>();

            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonObject.toString());

            if (!registrationFormParams.getLeft()) {
                return null;
            }

            Date dod = null;

            FormTag formTag = new FormTag();
            formTag.providerId = Utils.context().allSharedPreferences().fetchRegisteredANM();
            formTag.appVersion = FamilyLibrary.getInstance().getApplicationVersion();
            formTag.databaseVersion = FamilyLibrary.getInstance().getDatabaseVersion();

            JSONObject metadata = getJSONObject(registrationFormParams.getMiddle(), METADATA);
            String memberID = getString(registrationFormParams.getMiddle(), ENTITY_ID);

            JSONArray fields = new JSONArray();

            int x = 0;
            while (x < registrationFormParams.getRight().length()) {
                //JSONObject obj = registrationFormParams.getRight().getJSONObject(x);
                String myKey = registrationFormParams.getRight().getJSONObject(x).getString(KEY);

                if (myKey.equalsIgnoreCase(org.smartgresiter.jhpiego.util.Constants.FORM_CONSTANTS.REMOVE_MEMBER_FORM.DATE_MOVED) ||
                        myKey.equalsIgnoreCase(org.smartgresiter.jhpiego.util.Constants.FORM_CONSTANTS.REMOVE_MEMBER_FORM.REASON)
                ) {
                    fields.put(registrationFormParams.getRight().get(x));
                }
                if (myKey.equalsIgnoreCase(org.smartgresiter.jhpiego.util.Constants.FORM_CONSTANTS.REMOVE_MEMBER_FORM.DATE_DIED)) {
                    fields.put(registrationFormParams.getRight().get(x));
                    try {
                        dod = dd_MM_yyyy.parse(registrationFormParams.getRight().getJSONObject(x).getString(VALUE));
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                }
                x++;
            }

            String encounterType = getString(jsonObject, ENCOUNTER_TYPE);

            String eventType;
            String tableName;

            if (encounterType.equalsIgnoreCase(org.smartgresiter.jhpiego.util.Constants.EventType.REMOVE_CHILD)) {
                eventType = org.smartgresiter.jhpiego.util.Constants.EventType.REMOVE_CHILD;
                tableName = org.smartgresiter.jhpiego.util.Constants.TABLE_NAME.CHILD;
            } else if (encounterType.equalsIgnoreCase(org.smartgresiter.jhpiego.util.Constants.EventType.REMOVE_FAMILY)) {
                eventType = org.smartgresiter.jhpiego.util.Constants.EventType.REMOVE_FAMILY;
                tableName = org.smartgresiter.jhpiego.util.Constants.TABLE_NAME.FAMILY;
            } else {
                eventType = org.smartgresiter.jhpiego.util.Constants.EventType.REMOVE_MEMBER;
                tableName = org.smartgresiter.jhpiego.util.Constants.TABLE_NAME.FAMILY_MEMBER;
            }

            Event eventMember = JsonFormUtils.createEvent(fields, metadata, formTag, memberID,
                    eventType,
                    tableName
            );
            JsonFormUtils.tagSyncMetadata(Utils.context().allSharedPreferences(), eventMember);
            events.add(eventMember);


            return Triple.of(Pair.create(dod, encounterType), memberID, events);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
