package org.smartgresiter.jhpiego.presenter;

import org.smartgresiter.jhpiego.contract.HomeVisitGrowthNutritionContract;
import org.smartgresiter.jhpiego.fragment.GrowthNutritionInputFragment;
import org.smartgresiter.jhpiego.interactor.HomeVisitGrowthNutritionInteractor;
import org.smartgresiter.jhpiego.util.ChildUtils;
import org.smartgresiter.jhpiego.util.GrowthServiceData;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.ServiceSchedule;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.util.DateUtil;
import org.smartregister.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class HomeVisitGrowthNutritionPresenter implements HomeVisitGrowthNutritionContract.Presenter, HomeVisitGrowthNutritionContract.InteractorCallBack {
    private WeakReference<HomeVisitGrowthNutritionContract.View> view;
    private HomeVisitGrowthNutritionContract.Interactor interactor;
    private Map<String, ServiceWrapper> serviceWrapperMap = new LinkedHashMap<>();
    private ServiceWrapper serviceWrapperExclusive;
    private ServiceWrapper serviceWrapperMnp;
    private ServiceWrapper serviceWrapperVitamin;
    private ServiceWrapper serviceWrapperDeworming;
    private Map<String, ServiceWrapper> saveStateMap = new LinkedHashMap<>();
    private Map<String, ServiceWrapper> notVisitStateMap = new LinkedHashMap<>();
    private CommonPersonObjectClient commonPersonObjectClient;
    private int growthListCount = 0;

    public HomeVisitGrowthNutritionPresenter(HomeVisitGrowthNutritionContract.View view) {
        this.view = new WeakReference<>(view);
        interactor = new HomeVisitGrowthNutritionInteractor();
    }

    public ArrayList<GrowthServiceData> getAllDueService() {
        ArrayList<GrowthServiceData> growthServiceDataList = new ArrayList<>();

        for (String key : serviceWrapperMap.keySet()) {
            ServiceWrapper serviceWrapper = serviceWrapperMap.get(key);
            if (serviceWrapper != null && serviceWrapper.getAlert() != null) {
                GrowthServiceData growthServiceData = new GrowthServiceData();
                growthServiceData.setDate(serviceWrapper.getAlert().startDate());
                growthServiceData.setName(serviceWrapper.getAlert().scheduleName());
                growthServiceData.setDisplayName(getDisplayNameBasedOnType(key, growthServiceData.getName()));
                String duedateString = DateUtil.formatDate(growthServiceData.getDate(), "dd MMM yyyy");
                growthServiceData.setDisplayAbleDate(duedateString);
                growthServiceDataList.add(growthServiceData);
            }

        }
        return growthServiceDataList;
    }

    private String getDisplayNameBasedOnType(String type, String name) {
        Object[] displayName = ChildUtils.getStringWithNumber(name);
        if (displayName.length > 1) {
            String str = (String) displayName[0];
            String no = (String) displayName[1];
            if (type.equalsIgnoreCase(GrowthNutritionInputFragment.GROWTH_TYPE.EXCLUSIVE.getValue())) {
                return str + " " + no + " month";
            } else if (type.equalsIgnoreCase(GrowthNutritionInputFragment.GROWTH_TYPE.MNP.getValue())) {
                return str + " " + ChildUtils.getFirstSecondAsNumber(no) + " pack";
            } else {
                return str + " " + ChildUtils.getFirstSecondAsNumber(no) + " dose";
            }
        }
        return "";

    }

    @Override
    public void parseRecordServiceData(CommonPersonObjectClient commonPersonObjectClient) {
        this.commonPersonObjectClient = commonPersonObjectClient;
        interactor.parseRecordServiceData(commonPersonObjectClient, this);
    }

    @Override
    public void setSaveState(String type, ServiceWrapper serviceWrapper) {
        saveStateMap.put(type, serviceWrapper);
        if (getView() != null) getView().statusImageViewUpdate(type, true);
    }

    @Override
    public void serNotVisitState(String type, ServiceWrapper serviceWrapper) {

        if (isSave(type)) return;
        notVisitStateMap.put(type, serviceWrapper);
        if (getView() != null) getView().statusImageViewUpdate(type, false);
    }

    @Override
    public void resetAllSaveState() {
        RecurringServiceRecordRepository recurringServiceRecordRepository = ImmunizationLibrary.getInstance().recurringServiceRecordRepository();

        for (String type : saveStateMap.keySet()) {
            ServiceWrapper serviceWrapper = saveStateMap.get(type);
            if (serviceWrapper != null) {
                recurringServiceRecordRepository.deleteServiceRecord(serviceWrapper.getDbKey());
                ServiceSchedule.updateOfflineAlerts(serviceWrapper.getType(), commonPersonObjectClient.entityId(), Utils.dobToDateTime(commonPersonObjectClient));
            }

        }
    }

    @Override
    public boolean isSelected(String type) {
        for (String key : saveStateMap.keySet()) {
            if (key.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAllSelected() {
        if (growthListCount == (saveStateMap.size() + notVisitStateMap.size())) {
            return true;
        } else {
            return false;
        }

    }


    @Override
    public void updateRecordVisitData(Map<String, ServiceWrapper> stringServiceWrapperMap) {
        growthListCount = 0;
        serviceWrapperMap = stringServiceWrapperMap;
        serviceWrapperExclusive = getServiceWrapperByType(GrowthNutritionInputFragment.GROWTH_TYPE.EXCLUSIVE.getValue());
        if (serviceWrapperExclusive != null) {
            Alert alert = serviceWrapperExclusive.getAlert();
            if (alert != null) {
                growthListCount++;
                if (getView() != null) getView().updateExclusiveFeedingData(alert.scheduleName());
            } else {
                String lastDoneExclusive = serviceWrapperExclusive.getServiceType().getName();

            }
        }
        serviceWrapperMnp = getServiceWrapperByType(GrowthNutritionInputFragment.GROWTH_TYPE.MNP.getValue());
        if (serviceWrapperMnp != null) {
            Alert alert = serviceWrapperMnp.getAlert();
            if (alert != null) {
                growthListCount++;
                if (getView() != null) getView().updateMnpData(alert.scheduleName());
            } else {
                String lastDoneExclusive = serviceWrapperMnp.getServiceType().getName();

            }
        }
        serviceWrapperVitamin = getServiceWrapperByType(GrowthNutritionInputFragment.GROWTH_TYPE.VITAMIN.getValue());
        if (serviceWrapperVitamin != null) {
            Alert alert = serviceWrapperVitamin.getAlert();
            if (alert != null) {
                growthListCount++;
                if (getView() != null) getView().updateVitaminAData(alert.scheduleName());
            } else {
                String lastDoneExclusive = serviceWrapperVitamin.getServiceType().getName();

            }

        }
        serviceWrapperDeworming = getServiceWrapperByType(GrowthNutritionInputFragment.GROWTH_TYPE.DEWORMING.getValue());
        if (serviceWrapperDeworming != null) {
            Alert alert = serviceWrapperDeworming.getAlert();
            if (alert != null) {
                growthListCount++;
                if (getView() != null) getView().updateDewormingData(alert.scheduleName());
            } else {
                String lastDoneVitamin = serviceWrapperDeworming.getServiceType().getName();

            }
        }
        if (getView() != null) getView().updateUpcomingService();

    }

    public ServiceWrapper getServiceWrapperByType(String type) {
        if (serviceWrapperMap != null) {
            try {
                return serviceWrapperMap.get(type);
            } catch (Exception e) {

            }

        }
        return null;

    }

    private boolean isSave(String type) {
        for (String savedType : saveStateMap.keySet()) {
            if (savedType.equalsIgnoreCase(type)) {
                return true;
            }

        }
        return false;
    }


    public ServiceWrapper getServiceWrapperExclusive() {
        return serviceWrapperExclusive;
    }

    public ServiceWrapper getServiceWrapperMnp() {
        return serviceWrapperMnp;
    }

    public ServiceWrapper getServiceWrapperVitamin() {
        return serviceWrapperVitamin;
    }

    public ServiceWrapper getServiceWrapperDeworming() {
        return serviceWrapperDeworming;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        view = null;//set to null on destroy

        // Inform interactor
        interactor.onDestroy(isChangingConfiguration);

        // Activity destroyed set interactor to null
        if (!isChangingConfiguration) {
            interactor = null;
        }
    }

    @Override
    public HomeVisitGrowthNutritionContract.View getView() {
        if (view != null) {
            return view.get();
        } else {
            return null;
        }
    }

}
