package org.smartgresiter.jhpiego.listener;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.Toast;

import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.activity.AncActivity;
import org.smartgresiter.jhpiego.activity.ChildRegisterActivity;
import org.smartgresiter.jhpiego.activity.FamilyPlanningActivity;
import org.smartgresiter.jhpiego.activity.FamilyRegisterActivity;
import org.smartgresiter.jhpiego.activity.LandActivity;
import org.smartgresiter.jhpiego.activity.MalariaActivity;
import org.smartgresiter.jhpiego.activity.PncActivity;
import org.smartgresiter.jhpiego.adapter.NavigationAdapter;
import org.smartgresiter.jhpiego.util.Constants;

public class NavigationListener implements View.OnClickListener {

    private Activity activity;
    private NavigationAdapter navigationAdapter;
    DrawerLayout drawer;

    public NavigationListener(Activity activity, NavigationAdapter adapter) {
        this.activity = activity;
        this.navigationAdapter = adapter;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            if (v.getTag() instanceof String) {
                String tag = (String) v.getTag();


                drawer = activity.findViewById(R.id.drawer_layout);

                switch (tag) {
                    case Constants.DrawerMenu.ALL_FAMILIES:
                        startRegisterActivity(FamilyRegisterActivity.class);
                        break;

                    case Constants.DrawerMenu.ANC:
                        Toast.makeText(activity,"Start ANC Register", Toast.LENGTH_LONG).show();
                        drawer.closeDrawers();
                        // startRegisterActivity(AncActivity.class);
                        break;

                    case Constants.DrawerMenu.LD:
                        Toast.makeText(activity,"Start Land Register", Toast.LENGTH_LONG).show();
                        drawer.closeDrawers();
                        // startRegisterActivity(LandActivity.class);
                        break;

                    case Constants.DrawerMenu.PNC:
                        Toast.makeText(activity,"Start PNC Register", Toast.LENGTH_LONG).show();
                        drawer.closeDrawers();
                        // startRegisterActivity(PncActivity.class);
                        break;

                    case Constants.DrawerMenu.CHILDREN:
                        startRegisterActivity(ChildRegisterActivity.class);
                        break;

                    case Constants.DrawerMenu.FAMILY_PLANNING:
                        Toast.makeText(activity,"Start Family Planning Register", Toast.LENGTH_LONG).show();
                        drawer.closeDrawers();
                        // startRegisterActivity(FamilyPlanningActivity.class);
                        break;

                    case Constants.DrawerMenu.MALARIA:
                        Toast.makeText(activity,"Start Malaria Register", Toast.LENGTH_LONG).show();
                        drawer.closeDrawers();
                        // startRegisterActivity(MalariaActivity.class);
                        break;

                    default:
                        break;
                }


                navigationAdapter.setSelectedView(tag);
            }
        }
    }

    private void startRegisterActivity(Class registerClass){
        drawer.closeDrawers();

        Intent intent = new Intent(activity, registerClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        activity.finish();
    }
}
