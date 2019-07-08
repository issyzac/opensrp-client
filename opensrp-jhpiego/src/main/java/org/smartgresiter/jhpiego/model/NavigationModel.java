package org.smartgresiter.jhpiego.model;

import android.app.Activity;
import android.util.Log;

import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.contract.NavigationContract;
import org.smartgresiter.jhpiego.util.Constants;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class NavigationModel implements NavigationContract.Model {

    private static NavigationModel instance;
    List<NavigationOption> navigationOptions = new ArrayList<>();
    NavigationOption op1 = new NavigationOption(R.mipmap.sidemenu_familiesxxhdpi, R.mipmap.sidemenu_families_activexxhdpi, Constants.DrawerMenu.ALL_FAMILIES, 0);
    NavigationOption op2 = new NavigationOption(R.mipmap.sidemenu_ancxxhdpi, R.mipmap.sidemenu_anc_activexxhdpi, Constants.DrawerMenu.ANC, 0);
//    NavigationOption op3 = new NavigationOption(R.mipmap.sidemenu_landxxhdpi, R.mipmap.sidemenu_land_activexxhdpi, Constants.DrawerMenu.LD, 0);
    NavigationOption op4 = new NavigationOption(R.mipmap.sidemenu_pncxxhdpi, R.mipmap.sidemenu_pnc_activexxhdpi, Constants.DrawerMenu.PNC, 0);
    NavigationOption op5 = new NavigationOption(R.mipmap.sidemenu_childrenxxhdpi, R.mipmap.sidemenu_children_activexxhdpi, Constants.DrawerMenu.CHILDREN, 0);
    NavigationOption op6 = new NavigationOption(R.mipmap.sidemenu_familiesxxhdpi, R.mipmap.sidemenu_families_activexxhdpi, Constants.DrawerMenu.FAMILY_PLANNING, 0);
    NavigationOption op7 = new NavigationOption(R.mipmap.sidemenu_malariaxxhdpi, R.mipmap.sidemenu_malaria_activexxhdpi, Constants.DrawerMenu.MALARIA, 0);
    private String TAG = NavigationModel.class.getCanonicalName();
    private Activity mActivity;

    private NavigationModel() {
        navigationOptions.clear();
        navigationOptions.addAll(asList(op1, op2, op4, op5, op6, op7));
    }

    public static NavigationModel getInstance() {
        if (instance == null)
            instance = new NavigationModel();

        return instance;
    }

    @Override
    public List<NavigationOption> getNavigationItems() {
        return navigationOptions;
    }

    @Override
    public void setNavigationOptions(List<NavigationOption> navigationOptions) {
        this.navigationOptions = navigationOptions;
    }

    @Override
    public String getCurrentUser() {
        String res = "";
        try {
            res = Utils.getPrefferedName().split(" ")[0];
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return res;
    }

}
