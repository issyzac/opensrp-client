package org.smartgresiter.jhpiego.adapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.smartgresiter.jhpiego.BuildConfig;
import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.activity.LoginActivity;
import org.smartgresiter.jhpiego.application.JhpiegoApplication;
import org.smartgresiter.jhpiego.model.NavigationOption;
import org.smartgresiter.jhpiego.util.Constants;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(application = JhpiegoApplication.class, constants = BuildConfig.class, sdk = 22)
public class NavigationAdapterTest {


    private LoginActivity activity;
    private ActivityController<LoginActivity> controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = Robolectric.buildActivity(LoginActivity.class).create().start();
        activity = controller.get();
    }


    @After
    public void tearDown() {
        try {
            activity.finish();
            controller.pause().stop().destroy(); //destroy controller if we can

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.gc();
    }

    @Test
    public void getItemCount() {
        NavigationOption model1 = new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, Constants.DrawerMenu.ALL_FAMILIES, 0);
        NavigationOption model2 = new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, Constants.DrawerMenu.ALL_FAMILIES, 0);

        NavigationAdapter adapter = new NavigationAdapter(asList(model1, model2), activity);

        assertEquals(adapter.getItemCount(), 2);
    }

    public void addAction() {

    }
}
