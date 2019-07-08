package org.smartgresiter.jhpiego.contract;

import android.app.Activity;

import org.smartgresiter.jhpiego.model.NavigationOption;

import java.util.Date;
import java.util.List;

public interface NavigationContract {

    interface Presenter {

        NavigationContract.View getNavigationView();

        void refreshNavigationCount(Activity activity);

        void refreshLastSync();

        void displayCurrentUser();

        void Sync(Activity activity);

        List<NavigationOption> getOptions();
    }

    interface View {

        void prepareViews(Activity activity);

        void refreshLastSync(Date lastSync);

        void refreshCurrentUser(String name);

        void logout(Activity activity);

        void refreshCount();

        void displayToast(Activity activity, String message);
    }

    interface Model {

        List<NavigationOption> getNavigationItems();

        String getCurrentUser();

        void setNavigationOptions(List<NavigationOption> navigationOptions);
    }

    interface Interactor {

        String getUser();

        Date getLastSync();

        void getFamilyCount(InteractorCallback<Integer> callback);

        void getAncCount(InteractorCallback<Integer> callback);

        void getLandCount(InteractorCallback<Integer> callback);

        void getPncCount(InteractorCallback<Integer> callback);

        void getFamilyPlanningCount(InteractorCallback<Integer> callback);

        void getMalariaCount(InteractorCallback<Integer> callback);

        void getChildrenCount(InteractorCallback<Integer> callback);

        Date Sync();

    }

    interface InteractorCallback<T> {
        void onResult(T result);

        void onError(Exception e);
    }

}
