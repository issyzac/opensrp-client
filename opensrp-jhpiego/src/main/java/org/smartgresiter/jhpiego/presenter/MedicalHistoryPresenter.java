package org.smartgresiter.jhpiego.presenter;

import org.smartgresiter.jhpiego.contract.MedicalHistoryContract;
import org.smartgresiter.jhpiego.interactor.MedicalHistoryInteractor;
import org.smartgresiter.jhpiego.util.BaseService;
import org.smartgresiter.jhpiego.util.BaseVaccine;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class MedicalHistoryPresenter implements MedicalHistoryContract.Presenter, MedicalHistoryContract.InteractorCallBack {
    private WeakReference<MedicalHistoryContract.View> view;
    private MedicalHistoryContract.Interactor interactor;
    private Map<String, Date> recievedVaccines;
    private ArrayList<BaseVaccine> baseVaccineArrayList;
    private ArrayList<BaseService> growthNutritionArrayList;
    private ArrayList<String> birthCertifications;
    private ArrayList<String> obsIllnesses;

    public MedicalHistoryPresenter(MedicalHistoryContract.View view) {
        this.view = new WeakReference<>(view);
        interactor = new MedicalHistoryInteractor();
    }

    @Override
    public void setInitialVaccineList(Map<String, Date> veccineList) {
        recievedVaccines = veccineList;
        interactor.setInitialVaccineList(recievedVaccines, this);
    }

    @Override
    public void fetchGrowthNutrition(String baseEntity) {
        interactor.fetchGrowthNutritionData(baseEntity, this);
    }

    @Override
    public void fetchFullyImmunization(String dateOfBirth) {
        interactor.fetchFullyImmunizationData(dateOfBirth, recievedVaccines, this);
    }

    @Override
    public void fetchBirthAndIllnessData(CommonPersonObjectClient commonPersonObjectClient) {
        interactor.fetchBirthAndIllnessData(commonPersonObjectClient, this);

    }

    @Override
    public void updateFullyImmunization(String text) {
        getView().updateFullyImmunization(text);
    }

    @Override
    public ArrayList<BaseVaccine> getVaccineBaseItem() {
        return baseVaccineArrayList;
    }

    @Override
    public ArrayList<BaseService> getGrowthNutrition() {
        return growthNutritionArrayList;
    }

    @Override
    public ArrayList<String> getBirthCertification() {
        return birthCertifications;
    }

    @Override
    public ArrayList<String> getObsIllness() {
        return obsIllnesses;
    }

    @Override
    public void updateBirthCertification(ArrayList<String> birthCertification) {
        this.birthCertifications = birthCertification;
        getView().updateBirthCertification();

    }

    @Override
    public void updateIllnessData(ArrayList<String> obsIllnessArrayList) {
        this.obsIllnesses = obsIllnessArrayList;
        getView().updateObsIllness();

    }

    @Override
    public void updateVaccineData(ArrayList<BaseVaccine> baseVaccines) {
        this.baseVaccineArrayList = baseVaccines;

        getView().updateVaccinationData();

    }

    @Override
    public void updateGrowthNutrition(ArrayList<BaseService> growthNutritions) {
        this.growthNutritionArrayList = growthNutritions;
        getView().updateGrowthNutrition();

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
    public MedicalHistoryContract.View getView() {
        if (view != null) {
            return view.get();
        } else {
            return null;
        }
    }

}
