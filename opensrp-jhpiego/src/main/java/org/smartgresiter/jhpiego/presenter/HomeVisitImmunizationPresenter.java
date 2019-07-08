package org.smartgresiter.jhpiego.presenter;

import org.joda.time.DateTime;
import org.smartgresiter.jhpiego.contract.HomeVisitImmunizationContract;
import org.smartgresiter.jhpiego.interactor.HomeVisitImmunizationInteractor;
import org.smartgresiter.jhpiego.task.UndoVaccineTask;
import org.smartgresiter.jhpiego.util.HomeVisitVaccineGroupDetails;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.util.DateUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class HomeVisitImmunizationPresenter implements HomeVisitImmunizationContract.Presenter {


    HomeVisitImmunizationContract.Interactor homeVisitImmunizationInteractor;
    private WeakReference<HomeVisitImmunizationContract.View> view;
    ArrayList<VaccineRepo.Vaccine> vaccinesDueFromLastVisit = new ArrayList<VaccineRepo.Vaccine>();
    private ArrayList<HomeVisitVaccineGroupDetails> allgroups = new ArrayList<HomeVisitVaccineGroupDetails>();
    private ArrayList<VaccineWrapper> notGivenVaccines = new ArrayList<VaccineWrapper>();
    private HomeVisitVaccineGroupDetails currentActiveGroup;
    private CommonPersonObjectClient childClient;
    private ArrayList<VaccineWrapper> vaccinesGivenThisVisit = new ArrayList<VaccineWrapper>();
    public String groupImmunizationSecondaryText = "";
    public String singleImmunizationSecondaryText = "";

    public HomeVisitImmunizationPresenter(HomeVisitImmunizationContract.View view) {
        this.view = new WeakReference<>(view);
        homeVisitImmunizationInteractor = new HomeVisitImmunizationInteractor();
    }

    @Override
    public void createAllVaccineGroups(List<Alert> alerts, List<Vaccine> vaccines, List<Map<String, Object>> sch) {
        allgroups = homeVisitImmunizationInteractor.determineAllHomeVisitVaccineGroupDetails(alerts, vaccines, notGivenVaccines, sch);
    }

    @Override
    public void getVaccinesNotGivenLastVisit() {
        if (vaccinesDueFromLastVisit.size() == 0) {
            if (homeVisitImmunizationInteractor.hasVaccinesNotGivenSinceLastVisit(allgroups)) {
                vaccinesDueFromLastVisit = homeVisitImmunizationInteractor.getNotGivenVaccinesLastVisitList(allgroups);
            }
        }
    }

    @Override
    public void calculateCurrentActiveGroup() {
        currentActiveGroup = homeVisitImmunizationInteractor.getCurrentActiveHomeVisitVaccineGroupDetail(allgroups);
        if (currentActiveGroup == null) {
            currentActiveGroup = homeVisitImmunizationInteractor.getLastActiveHomeVisitVaccineGroupDetail(allgroups);
        }
    }

    @Override
    public HomeVisitImmunizationContract.View getView() {
        if (view != null) {
            return view.get();
        } else {
            return null;
        }
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {

    }

    @Override
    public boolean isPartiallyComplete() {
        return getHomeVisitImmunizationInteractor().isPartiallyComplete(currentActiveGroup);
    }

    @Override
    public boolean isComplete() {
        return getHomeVisitImmunizationInteractor().isComplete(currentActiveGroup);
    }

    @Override
    public HomeVisitImmunizationContract.Interactor getHomeVisitImmunizationInteractor() {
        return homeVisitImmunizationInteractor;
    }

    @Override
    public void setHomeVisitImmunizationInteractor(HomeVisitImmunizationInteractor homeVisitImmunizationInteractor) {
        this.homeVisitImmunizationInteractor = homeVisitImmunizationInteractor;
    }

    @Override
    public void setView(WeakReference<HomeVisitImmunizationContract.View> view) {
        this.view = view;
    }

    @Override
    public ArrayList<VaccineRepo.Vaccine> getVaccinesDueFromLastVisit() {
        return vaccinesDueFromLastVisit;
    }

    @Override
    public void setVaccinesDueFromLastVisit(ArrayList<VaccineRepo.Vaccine> vaccinesDueFromLastVisit) {
        this.vaccinesDueFromLastVisit = vaccinesDueFromLastVisit;
    }

    @Override
    public ArrayList<HomeVisitVaccineGroupDetails> getAllgroups() {
        return allgroups;
    }

    @Override
    public void setAllgroups(ArrayList<HomeVisitVaccineGroupDetails> allgroups) {
        this.allgroups = allgroups;
    }

    @Override
    public ArrayList<VaccineWrapper> getNotGivenVaccines() {
        return notGivenVaccines;
    }

    @Override
    public void setNotGivenVaccines(ArrayList<VaccineWrapper> notGivenVaccines) {
        this.notGivenVaccines = notGivenVaccines;
    }

    @Override
    public HomeVisitVaccineGroupDetails getCurrentActiveGroup() {
        return currentActiveGroup;
    }

    @Override
    public void setCurrentActiveGroup(HomeVisitVaccineGroupDetails currentActiveGroup) {
        this.currentActiveGroup = currentActiveGroup;
    }

    @Override
    public boolean groupIsDue() {
        return homeVisitImmunizationInteractor.groupIsDue(currentActiveGroup);
    }

    @Override
    public ArrayList<VaccineWrapper> createVaccineWrappers(HomeVisitVaccineGroupDetails duevaccines) {

        ArrayList<VaccineWrapper> vaccineWrappers = new ArrayList<VaccineWrapper>();
        for (VaccineRepo.Vaccine vaccine : duevaccines.getDueVaccines()) {
            VaccineWrapper vaccineWrapper = new VaccineWrapper();
            vaccineWrapper.setVaccine(vaccine);
            vaccineWrapper.setName(vaccine.display());
            vaccineWrapper.setDefaultName(vaccine.display());
            vaccineWrappers.add(vaccineWrapper);
        }
        return vaccineWrappers;
    }

    @Override
    public CommonPersonObjectClient getchildClient() {
        return childClient;
    }

    @Override
    public void setChildClient(CommonPersonObjectClient childClient) {
        this.childClient = childClient;
    }

    @Override
    public void updateNotGivenVaccine(VaccineWrapper name) {
        if (!notGivenVaccines.contains(name)) {
            notGivenVaccines.add(name);
        }
    }

    @Override
    public ArrayList<VaccineWrapper> getVaccinesGivenThisVisit() {
        return vaccinesGivenThisVisit;
    }

    @Override
    public void assigntoGivenVaccines(ArrayList<VaccineWrapper> tagsToUpdate) {
        vaccinesGivenThisVisit.addAll(tagsToUpdate);
    }

    @Override
    public void undoGivenVaccines() {
        org.smartregister.util.Utils.startAsyncTask(new UndoVaccineTask(vaccinesGivenThisVisit, childClient), null);
    }

    @Override
    public void updateImmunizationState(HomeVisitImmunizationContract.InteractorCallBack callBack) {
        homeVisitImmunizationInteractor.updateImmunizationState(childClient, notGivenVaccines, callBack);
    }

    @Override
    public ArrayList<VaccineRepo.Vaccine> getVaccinesDueFromLastVisitStillDueState() {
        ArrayList<VaccineRepo.Vaccine> vaccinesToReturn = new ArrayList<VaccineRepo.Vaccine>();
        Stack<VaccineRepo.Vaccine> vaccinesStack = new Stack<VaccineRepo.Vaccine>();
        for (VaccineRepo.Vaccine vaccinedueLastVisit : vaccinesDueFromLastVisit) {
            vaccinesStack.add(vaccinedueLastVisit);
            for (VaccineWrapper givenThisVisit : vaccinesGivenThisVisit) {
                if (!vaccinesStack.isEmpty()) {
                    if (givenThisVisit.getDefaultName().equalsIgnoreCase(vaccinesStack.peek().display())) {
                        vaccinesStack.pop();
                    }
                }
            }
        }
        vaccinesToReturn.addAll(vaccinesStack);
        vaccinesStack = new Stack<VaccineRepo.Vaccine>();
        for (VaccineRepo.Vaccine vaccinesDueYetnotGiven : vaccinesToReturn) {
            vaccinesStack.add(vaccinesDueYetnotGiven);
            for (VaccineWrapper vaccine : notGivenVaccines) {
                if (vaccine.getDefaultName().equalsIgnoreCase(vaccinesStack.peek().display())) {
                    vaccinesStack.pop();
                }
            }
        }
        vaccinesToReturn = new ArrayList<VaccineRepo.Vaccine>();
        vaccinesToReturn.addAll(vaccinesStack);
        return vaccinesToReturn;
    }

    @Override
    public boolean isSingleVaccineGroupPartialComplete() {
        boolean toReturn = false;
        ArrayList<VaccineRepo.Vaccine> singleVaccineInDueState = getVaccinesDueFromLastVisitStillDueState();
        if (singleVaccineInDueState.size() == 0) {
            for (VaccineRepo.Vaccine vaccineDueLastVisit : vaccinesDueFromLastVisit) {
                for (VaccineWrapper notgivenVaccine : notGivenVaccines) {
                    if (notgivenVaccine.getDefaultName().equalsIgnoreCase(vaccineDueLastVisit.display())) {
                        toReturn = true;
                    }
                }
            }
        }
        return toReturn;
    }

    @Override
    public boolean isSingleVaccineGroupComplete() {
        boolean toReturn = true;
        ArrayList<VaccineRepo.Vaccine> singleVaccineInDueState = getVaccinesDueFromLastVisitStillDueState();
        if (singleVaccineInDueState.size() == 0) {
            for (VaccineRepo.Vaccine vaccineDueLastVisit : vaccinesDueFromLastVisit) {
                for (VaccineWrapper notgivenVaccine : notGivenVaccines) {
                    if (notgivenVaccine.getDefaultName().equalsIgnoreCase(vaccineDueLastVisit.display())) {
                        toReturn = false;
                    }
                }
            }
        }
        return toReturn;
    }

    @Override
    public void setGroupVaccineText(List<Map<String, Object>> sch) {
        ArrayList<VaccineRepo.Vaccine> allgivenVaccines = new ArrayList<VaccineRepo.Vaccine>();
        for (HomeVisitVaccineGroupDetails group : allgroups) {
            allgivenVaccines.addAll(group.getGivenVaccines());
        }
        LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>> groupedByDate = new LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>>();
        for (VaccineRepo.Vaccine vaccineGiven : allgivenVaccines) {
            for (Map<String, Object> mapToProcess : sch) {
                if (((VaccineRepo.Vaccine) mapToProcess.get("vaccine")).display().equalsIgnoreCase(vaccineGiven.display())) {
                    if (groupedByDate.get((DateTime) mapToProcess.get("date")) == null) {
                        ArrayList<VaccineRepo.Vaccine> givenVaccinesAtDate = new ArrayList<VaccineRepo.Vaccine>();
                        givenVaccinesAtDate.add(vaccineGiven);
                        groupedByDate.put((DateTime) mapToProcess.get("date"), givenVaccinesAtDate);
                    } else {
                        groupedByDate.get(mapToProcess.get("date")).add(vaccineGiven);
                    }
                }
            }
        }
        String groupSecondaryText = "";
        for (Map.Entry<DateTime, ArrayList<VaccineRepo.Vaccine>> entry : groupedByDate.entrySet()) {
            DateTime dateTime = entry.getKey();
            ArrayList<VaccineRepo.Vaccine> vaccines = entry.getValue();
            // now work with key and value...
            for (VaccineRepo.Vaccine vaccineGiven : vaccines) {
                groupSecondaryText = groupSecondaryText + vaccineGiven.display() + ", ";
            }

            if (groupSecondaryText.endsWith(", ")) {
                groupSecondaryText = groupSecondaryText.trim();
                groupSecondaryText = groupSecondaryText.substring(0, groupSecondaryText.length() - 1);

            }
            groupSecondaryText = groupSecondaryText + " provided on ";

            DateTime dueDate = (DateTime) dateTime;
            String duedateString = DateUtil.formatDate(dueDate.toLocalDate(), "dd MMM yyyy");
            groupSecondaryText = groupSecondaryText + duedateString + " \u00B7 ";

        }
        groupSecondaryText = groupSecondaryText + addNotGivenVaccines(sch);
        groupImmunizationSecondaryText = groupSecondaryText;
    }

    private String addNotGivenVaccines(List<Map<String, Object>> sch) {
        ArrayList<VaccineRepo.Vaccine> allgivenVaccines = new ArrayList<VaccineRepo.Vaccine>();
        for (HomeVisitVaccineGroupDetails group : allgroups) {
            allgivenVaccines.addAll(group.getNotGivenVaccines());
        }
        LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>> groupedByDate = new LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>>();
        for (VaccineRepo.Vaccine vaccineGiven : allgivenVaccines) {
            for (Map<String, Object> mapToProcess : sch) {
                if (((VaccineRepo.Vaccine) mapToProcess.get("vaccine")).display().equalsIgnoreCase(vaccineGiven.display())) {
                    if (groupedByDate.get((DateTime) mapToProcess.get("date")) == null) {
                        ArrayList<VaccineRepo.Vaccine> givenVaccinesAtDate = new ArrayList<VaccineRepo.Vaccine>();
                        givenVaccinesAtDate.add(vaccineGiven);
                        groupedByDate.put((DateTime) mapToProcess.get("date"), givenVaccinesAtDate);
                    } else {
                        groupedByDate.get(mapToProcess.get("date")).add(vaccineGiven);
                    }
                }
            }
        }
        String groupSecondaryText = "";
        for (Map.Entry<DateTime, ArrayList<VaccineRepo.Vaccine>> entry : groupedByDate.entrySet()) {
            DateTime dateTime = entry.getKey();
            ArrayList<VaccineRepo.Vaccine> vaccines = entry.getValue();
            // now work with key and value...
            for (VaccineRepo.Vaccine vaccineGiven : vaccines) {
                groupSecondaryText = groupSecondaryText + vaccineGiven.display() + ", ";
            }

            if (groupSecondaryText.endsWith(", ")) {
                groupSecondaryText = groupSecondaryText.trim();
                groupSecondaryText = groupSecondaryText.substring(0, groupSecondaryText.length() - 1);

            }
            groupSecondaryText = groupSecondaryText + " not given ";

            groupSecondaryText = groupSecondaryText + " \u00B7 ";

        }

        return groupSecondaryText;
    }

    @Override
    public void setSingleVaccineText(ArrayList<VaccineRepo.Vaccine> vaccinesDueFromLastVisit, List<Map<String, Object>> sch) {
        ArrayList<VaccineRepo.Vaccine> allgivenVaccines = new ArrayList<VaccineRepo.Vaccine>();
        for (VaccineRepo.Vaccine vaccineDueFromLastVisit : vaccinesDueFromLastVisit) {
            for (VaccineWrapper vaccineWrapper : vaccinesGivenThisVisit) {
                if (vaccineWrapper.getDefaultName().equalsIgnoreCase(vaccineDueFromLastVisit.display())) {
                    allgivenVaccines.add(vaccineDueFromLastVisit);

                }
            }
        }
        LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>> groupedByDate = new LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>>();
        for (VaccineRepo.Vaccine vaccineGiven : allgivenVaccines) {
            for (Map<String, Object> mapToProcess : sch) {
                if (((VaccineRepo.Vaccine) mapToProcess.get("vaccine")).display().equalsIgnoreCase(vaccineGiven.display())) {
                    if (groupedByDate.get((DateTime) mapToProcess.get("date")) == null) {
                        ArrayList<VaccineRepo.Vaccine> givenVaccinesAtDate = new ArrayList<VaccineRepo.Vaccine>();
                        givenVaccinesAtDate.add(vaccineGiven);
                        groupedByDate.put((DateTime) mapToProcess.get("date"), givenVaccinesAtDate);
                    } else {
                        groupedByDate.get(mapToProcess.get("date")).add(vaccineGiven);
                    }
                }
            }
        }
        String groupSecondaryText = "";
        for (Map.Entry<DateTime, ArrayList<VaccineRepo.Vaccine>> entry : groupedByDate.entrySet()) {
            DateTime dateTime = entry.getKey();
            ArrayList<VaccineRepo.Vaccine> vaccines = entry.getValue();
            // now work with key and value...
            for (VaccineRepo.Vaccine vaccineGiven : vaccines) {
                groupSecondaryText = groupSecondaryText + vaccineGiven.display() + ", ";
            }

            if (groupSecondaryText.endsWith(", ")) {
                groupSecondaryText = groupSecondaryText.trim();
                groupSecondaryText = groupSecondaryText.substring(0, groupSecondaryText.length() - 1);
            }
            groupSecondaryText = groupSecondaryText + " provided on ";

            DateTime dueDate = (DateTime) dateTime;
            String duedateString = DateUtil.formatDate(dueDate.toLocalDate(), "dd MMM yyyy");
            groupSecondaryText = groupSecondaryText + duedateString + " \u00B7 ";

        }
        singleImmunizationSecondaryText = groupSecondaryText;
    }

    @Override
    public String getGroupImmunizationSecondaryText() {
        return groupImmunizationSecondaryText;
    }

    @Override
    public void setGroupImmunizationSecondaryText(String groupImmunizationSecondaryText) {
        this.groupImmunizationSecondaryText = groupImmunizationSecondaryText;
    }

    @Override
    public String getSingleImmunizationSecondaryText() {
        return singleImmunizationSecondaryText;
    }

    @Override
    public void setSingleImmunizationSecondaryText(String singleImmunizationSecondaryText) {
        this.singleImmunizationSecondaryText = singleImmunizationSecondaryText;
    }
}
