package org.smartgresiter.jhpiego.presenter;

import android.app.Activity;
import android.util.Log;

import org.smartgresiter.jhpiego.contract.NavigationContract;
import org.smartgresiter.jhpiego.interactor.NavigationInteractor;
import org.smartgresiter.jhpiego.model.NavigationModel;
import org.smartgresiter.jhpiego.model.NavigationOption;
import org.smartgresiter.jhpiego.util.Constants;
import org.smartregister.job.SyncServiceJob;

import java.lang.ref.WeakReference;
import java.util.List;

public class NavigationPresenter implements NavigationContract.Presenter {

    private NavigationContract.Model mModel;
    private NavigationContract.Interactor mInteractor;
    private WeakReference<NavigationContract.View> mView;

    public NavigationPresenter(NavigationContract.View view) {
        mView = new WeakReference<>(view);
        mInteractor = NavigationInteractor.getInstance();
        mModel = NavigationModel.getInstance();
    }

    @Override
    public NavigationContract.View getNavigationView() {
        return mView.get();
    }

    @Override
    public void refreshNavigationCount(final Activity activity) {

        int x = 0;
        while (x < mModel.getNavigationItems().size()) {

            final int finalX = x;
            switch (mModel.getNavigationItems().get(x).getMenuTitle()) {
                case Constants.DrawerMenu.ALL_FAMILIES:
                    mInteractor.getFamilyCount(new NavigationContract.InteractorCallback<Integer>() {
                        @Override
                        public void onResult(Integer result) {
                            mModel.getNavigationItems().get(finalX).setRegisterCount(result);
                            Log.d("NavigationPresenter", String.valueOf(result));
                            getNavigationView().refreshCount();
                        }

                        @Override
                        public void onError(Exception e) {
                            getNavigationView().displayToast(activity, "Error retrieving count for " + Constants.DrawerMenu.ALL_FAMILIES);
                        }
                    });
                    break;
                case Constants.DrawerMenu.ANC:
                    mInteractor.getAncCount(new NavigationContract.InteractorCallback<Integer>() {
                        @Override
                        public void onResult(Integer result) {
                            mModel.getNavigationItems().get(finalX).setRegisterCount(result);
                            Log.d("NavigationPresenter", String.valueOf(result));
                            getNavigationView().refreshCount();
                        }

                        @Override
                        public void onError(Exception e) {
                            getNavigationView().displayToast(activity, "Error retrieving count for " + Constants.DrawerMenu.ANC);
                        }
                    });
                    break;
                case Constants.DrawerMenu.LD:
                    mInteractor.getLandCount(new NavigationContract.InteractorCallback<Integer>() {
                        @Override
                        public void onResult(Integer result) {
                            mModel.getNavigationItems().get(finalX).setRegisterCount(result);
                            Log.d("NavigationPresenter", String.valueOf(result));
                            getNavigationView().refreshCount();
                        }

                        @Override
                        public void onError(Exception e) {
                            getNavigationView().displayToast(activity, "Error retrieving count for " + Constants.DrawerMenu.LD);
                        }
                    });
                    break;
                case Constants.DrawerMenu.PNC:
                    mInteractor.getPncCount(new NavigationContract.InteractorCallback<Integer>() {
                        @Override
                        public void onResult(Integer result) {
                            mModel.getNavigationItems().get(finalX).setRegisterCount(result);
                            Log.d("NavigationPresenter", String.valueOf(result));
                            getNavigationView().refreshCount();
                        }

                        @Override
                        public void onError(Exception e) {
                            getNavigationView().displayToast(activity, "Error retrieving count for " + Constants.DrawerMenu.PNC);
                        }
                    });
                    break;
                case Constants.DrawerMenu.CHILDREN:
                    mInteractor.getChildrenCount(new NavigationContract.InteractorCallback<Integer>() {
                        @Override
                        public void onResult(Integer result) {
                            mModel.getNavigationItems().get(finalX).setRegisterCount(result);
                            getNavigationView().refreshCount();
                        }

                        @Override
                        public void onError(Exception e) {
                            getNavigationView().displayToast(activity, "Error retrieving count for " + Constants.DrawerMenu.CHILDREN);
                        }
                    });
                    break;
                case Constants.DrawerMenu.FAMILY_PLANNING:
                    mInteractor.getFamilyPlanningCount(new NavigationContract.InteractorCallback<Integer>() {
                        @Override
                        public void onResult(Integer result) {
                            mModel.getNavigationItems().get(finalX).setRegisterCount(result);
                            Log.d("NavigationPresenter", String.valueOf(result));
                            getNavigationView().refreshCount();
                        }

                        @Override
                        public void onError(Exception e) {
                            getNavigationView().displayToast(activity, "Error retrieving count for " + Constants.DrawerMenu.FAMILY_PLANNING);
                        }
                    });
                    break;
                case Constants.DrawerMenu.MALARIA:
                    mInteractor.getMalariaCount(new NavigationContract.InteractorCallback<Integer>() {
                        @Override
                        public void onResult(Integer result) {
                            mModel.getNavigationItems().get(finalX).setRegisterCount(result);
                            Log.d("NavigationPresenter", String.valueOf(result));
                            getNavigationView().refreshCount();
                        }

                        @Override
                        public void onError(Exception e) {
                            getNavigationView().displayToast(activity, "Error retrieving count for " + Constants.DrawerMenu.MALARIA);
                        }
                    });
                    break;
                default:
                    break;
            }

            x++;
        }

    }


    @Override
    public void refreshLastSync() {
        // get last sync date
        getNavigationView().refreshLastSync(mInteractor.Sync());
    }

    @Override
    public void displayCurrentUser() {
        getNavigationView().refreshCurrentUser(mModel.getCurrentUser());
    }

    @Override
    public void Sync(Activity activity) {
        SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
    }

    @Override
    public List<NavigationOption> getOptions() {
        return mModel.getNavigationItems();
    }
}
