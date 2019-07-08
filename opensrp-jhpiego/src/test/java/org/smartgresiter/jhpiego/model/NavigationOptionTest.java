package org.smartgresiter.jhpiego.model;

import org.junit.Test;
import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.util.Constants;

import static org.junit.Assert.assertEquals;

public class NavigationOptionTest {

    @Test
    public void navigationModel_TestConstructor() {
        NavigationOption navigationOption = new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, Constants.DrawerMenu.ALL_FAMILIES, 0);

        assertEquals(navigationOption.getMenuTitle(), Constants.DrawerMenu.ALL_FAMILIES);
        assertEquals(navigationOption.getResourceID(), R.mipmap.sidemenu_families);
        assertEquals(navigationOption.getRegisterCount(), 0);
    }

    @Test
    public void navigationModel_TestSettersAndGetters() {
        NavigationOption model = new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, Constants.DrawerMenu.ALL_FAMILIES, 0);

        model.setRegisterCount(2);
        model.setMenuTitle("Test Menu");
        model.setResourceID(R.mipmap.sidemenu_families);


        assertEquals(model.getRegisterCount(), 2);
        assertEquals(model.getResourceID(), R.mipmap.sidemenu_families);
        assertEquals(model.getMenuTitle(), "Test Menu");

    }
}