package org.smartgresiter.jhpiego.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.adapter.BirthAndIllnessAdapter;
import org.smartgresiter.jhpiego.adapter.GrowthAdapter;
import org.smartgresiter.jhpiego.adapter.VaccineAdapter;
import org.smartgresiter.jhpiego.contract.MedicalHistoryContract;
import org.smartgresiter.jhpiego.presenter.MedicalHistoryPresenter;
import org.smartgresiter.jhpiego.util.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.activity.SecuredActivity;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class MedicalHistoryActivity extends SecuredActivity implements MedicalHistoryContract.View {
    private TextView textViewTitle, textViewLastVisit, textViewFullyImmunization;
    private LinearLayout layoutImmunization, layoutGrowthAndNutrition, layoutBirthCert, layoutIllness;
    private RelativeLayout layoutFullyImmunizationBarAge1, layoutFullyImmunizationBarAge2;
    private RecyclerView recyclerViewImmunization, recyclerViewGrowthNutrition, recyclerViewBirthCert, recyclerViewIllness;
    private Map<String, Date> vaccineList;
    private String name, lastVisitDays, dateOfBirth;
    private CommonPersonObjectClient childClient;
    private MedicalHistoryContract.Presenter presenter;
    private VaccineAdapter vaccineAdapter;
    private GrowthAdapter growthAdapter;
    private BirthAndIllnessAdapter birthCertAdapter, illnessAdapter;

    public static void startMedicalHistoryActivity(Activity activity, CommonPersonObjectClient childClient, String childName, String lastVisitDays, String dateOfirth,
                                                   LinkedHashMap<String, Date> receivedVaccine) {
        Intent intent = new Intent(activity, MedicalHistoryActivity.class);
        intent.putExtra(Constants.INTENT_KEY.CHILD_COMMON_PERSON, childClient);
        intent.putExtra(Constants.INTENT_KEY.CHILD_NAME, childName);
        intent.putExtra(Constants.INTENT_KEY.CHILD_DATE_OF_BIRTH, dateOfirth);
        intent.putExtra(Constants.INTENT_KEY.CHILD_LAST_VISIT_DAYS, lastVisitDays);
        intent.putExtra(Constants.INTENT_KEY.CHILD_VACCINE_LIST, receivedVaccine);

        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_medical_history);
        setUpActionBar();
        textViewLastVisit = findViewById(R.id.home_visit_date);
        layoutImmunization = findViewById(R.id.immunization_bar);
        layoutFullyImmunizationBarAge1 = findViewById(R.id.immu_bar_age_1);
        layoutFullyImmunizationBarAge2 = findViewById(R.id.immu_bar_age_2);
        layoutBirthCert = findViewById(R.id.birth_cert_list);
        layoutIllness = findViewById(R.id.illness_list);
        textViewFullyImmunization = findViewById(R.id.fully_immunized);
        recyclerViewImmunization = findViewById(R.id.immunization_recycler_view);
        recyclerViewGrowthNutrition = findViewById(R.id.recycler_view_growth);
        recyclerViewBirthCert = findViewById(R.id.recycler_view_birth);
        recyclerViewIllness = findViewById(R.id.recycler_view_illness);
        recyclerViewImmunization.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGrowthNutrition.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewIllness.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBirthCert.setLayoutManager(new LinearLayoutManager(this));
        layoutGrowthAndNutrition = findViewById(R.id.growth_and_nutrition_list);
        parseBundleANdUpdateView();
    }

    @Override
    protected void onResumption() {

    }

    private void setUpActionBar() {
        Toolbar toolbar = findViewById(R.id.collapsing_toolbar);
        textViewTitle = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void parseBundleANdUpdateView() {
        childClient = (CommonPersonObjectClient) getIntent().getSerializableExtra(Constants.INTENT_KEY.CHILD_COMMON_PERSON);
        name = getIntent().getStringExtra(Constants.INTENT_KEY.CHILD_NAME);
        lastVisitDays = getIntent().getStringExtra(Constants.INTENT_KEY.CHILD_LAST_VISIT_DAYS);
        dateOfBirth = getIntent().getStringExtra(Constants.INTENT_KEY.CHILD_DATE_OF_BIRTH);
        vaccineList = (Map<String, Date>) getIntent().getSerializableExtra(Constants.INTENT_KEY.CHILD_VACCINE_LIST);
        if (TextUtils.isEmpty(name)) {
            textViewTitle.setVisibility(View.GONE);
        } else {
            textViewTitle.setText(getString(R.string.medical_title, name));
        }
        textViewLastVisit.setText(getString(R.string.medical_last_visit, lastVisitDays));
        initializePresenter();
        setInitialVaccineList();
        fetchFullYImmunization();
        fetchGrowthNutrition();
        fetchBirthCertIllness();
    }


    private void fetchFullYImmunization() {
        presenter.fetchFullyImmunization(dateOfBirth);
    }

    private void setInitialVaccineList() {
        presenter.setInitialVaccineList(vaccineList);

    }

    private void fetchGrowthNutrition() {
        presenter.fetchGrowthNutrition(childClient.entityId());

    }

    private void fetchBirthCertIllness() {
        presenter.fetchBirthAndIllnessData(childClient);
    }

    @Override
    public void updateFullyImmunization(String text) {
        if (text.equalsIgnoreCase("2")) {
            layoutFullyImmunizationBarAge1.setVisibility(View.VISIBLE);
            layoutFullyImmunizationBarAge2.setVisibility(View.VISIBLE);
        } else if (text.equalsIgnoreCase("1")) {
            layoutFullyImmunizationBarAge1.setVisibility(View.VISIBLE);
            layoutFullyImmunizationBarAge2.setVisibility(View.GONE);
        } else {
            layoutFullyImmunizationBarAge1.setVisibility(View.GONE);
            layoutFullyImmunizationBarAge2.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateVaccinationData() {
        if (presenter.getVaccineBaseItem() != null && presenter.getVaccineBaseItem().size() > 0) {
            layoutImmunization.setVisibility(View.VISIBLE);
            if (vaccineAdapter == null) {
                vaccineAdapter = new VaccineAdapter();
                vaccineAdapter.addItem(presenter.getVaccineBaseItem());
                recyclerViewImmunization.setAdapter(vaccineAdapter);
            } else {
                vaccineAdapter.notifyDataSetChanged();
            }
        } else {
            layoutImmunization.setVisibility(View.GONE);
        }


    }

    @Override
    public void updateGrowthNutrition() {
        if (presenter.getGrowthNutrition() != null && presenter.getGrowthNutrition().size() > 0) {
            layoutGrowthAndNutrition.setVisibility(View.VISIBLE);
            if (growthAdapter == null) {
                growthAdapter = new GrowthAdapter();
                growthAdapter.addItem(presenter.getGrowthNutrition());
                recyclerViewGrowthNutrition.setAdapter(growthAdapter);
            } else {
                growthAdapter.notifyDataSetChanged();
            }
        } else {
            layoutGrowthAndNutrition.setVisibility(View.GONE);
        }

    }

    @Override
    public void updateBirthCertification() {
        if (presenter.getBirthCertification() != null && presenter.getBirthCertification().size() > 0) {
            layoutBirthCert.setVisibility(View.VISIBLE);
            if (birthCertAdapter == null) {
                birthCertAdapter = new BirthAndIllnessAdapter();
                birthCertAdapter.setData(presenter.getBirthCertification());
                recyclerViewBirthCert.setAdapter(birthCertAdapter);
            } else {
                birthCertAdapter.notifyDataSetChanged();
            }
        } else {
            layoutBirthCert.setVisibility(View.GONE);
        }

    }

    @Override
    public void updateObsIllness() {
        if (presenter.getObsIllness() != null && presenter.getObsIllness().size() > 0) {
            layoutIllness.setVisibility(View.VISIBLE);
            if (illnessAdapter == null) {
                illnessAdapter = new BirthAndIllnessAdapter();
                illnessAdapter.setData(presenter.getObsIllness());
                recyclerViewIllness.setAdapter(illnessAdapter);
            } else {
                illnessAdapter.notifyDataSetChanged();
            }
        } else {
            layoutIllness.setVisibility(View.GONE);
        }
    }


    @Override
    public MedicalHistoryContract.Presenter initializePresenter() {
        presenter = new MedicalHistoryPresenter(this);
        return presenter;
    }
}
