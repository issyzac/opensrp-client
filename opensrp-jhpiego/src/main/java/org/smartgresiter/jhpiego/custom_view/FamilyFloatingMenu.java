package org.smartgresiter.jhpiego.custom_view;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.listener.OnClickFloatingMenu;

public class FamilyFloatingMenu extends LinearLayout implements View.OnClickListener {
    private RelativeLayout activityMain;
    private FloatingActionButton fab;
    private LinearLayout menuBar;
    private Animation fabOpen, fabClose, rotateForward, rotateBack;
    private boolean isFabMenuOpen = false;
    private OnClickFloatingMenu onClickFloatingMenu;

    private View callLayout, familyDetail, addNewMember, removeMember, changeHead, changePrimary;

    public FamilyFloatingMenu(Context context) {
        super(context);
        initUi();
    }

    public FamilyFloatingMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUi();
    }

    public FamilyFloatingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUi();
    }

    private void initUi() {
        inflate(getContext(), R.layout.view_family_floating_menu, this);
        activityMain = findViewById(R.id.activity_main);
        menuBar = findViewById(R.id.menu_bar);
        fab = findViewById(R.id.fab);

        fabOpen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_forward);
        rotateBack = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_back);

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });

        callLayout = findViewById(R.id.call_layout);
        callLayout.setOnClickListener(this);

        familyDetail = findViewById(R.id.family_detail_layout);
        familyDetail.setOnClickListener(this);

        addNewMember = findViewById(R.id.add_new_member_layout);
        addNewMember.setOnClickListener(this);

        removeMember = findViewById(R.id.remove_member_layout);
        removeMember.setOnClickListener(this);

        changeHead = findViewById(R.id.change_head_layout);
        changeHead.setOnClickListener(this);

        changePrimary = findViewById(R.id.change_primary_layout);
        changePrimary.setOnClickListener(this);

        callLayout.setClickable(false);
        familyDetail.setClickable(false);
        addNewMember.setClickable(false);
        removeMember.setClickable(false);
        changeHead.setClickable(false);
        changePrimary.setClickable(false);

        menuBar.setVisibility(GONE);

    }

    public void setClickListener(OnClickFloatingMenu onClickFloatingMenu) {
        this.onClickFloatingMenu = onClickFloatingMenu;
    }

    public void animateFAB() {
        if (menuBar.getVisibility() == GONE) {
            menuBar.setVisibility(VISIBLE);
        }

        if (isFabMenuOpen) {
            activityMain.setBackgroundResource(R.color.transparent);

            fab.startAnimation(rotateBack);
            fab.setImageResource(R.drawable.ic_edit_white);

            callLayout.startAnimation(fabClose);
            familyDetail.startAnimation(fabClose);
            addNewMember.startAnimation(fabClose);
            removeMember.startAnimation(fabClose);
            changeHead.startAnimation(fabClose);
            changePrimary.startAnimation(fabClose);

            callLayout.setClickable(false);
            familyDetail.setClickable(false);
            addNewMember.setClickable(false);
            removeMember.setClickable(false);
            changeHead.setClickable(false);
            changePrimary.setClickable(false);

            isFabMenuOpen = false;

        } else {
            activityMain.setBackgroundResource(R.color.black_tranparent_50);

            fab.startAnimation(rotateForward);
            fab.setImageResource(R.drawable.ic_input_add);

            callLayout.startAnimation(fabOpen);
            familyDetail.startAnimation(fabOpen);
            addNewMember.startAnimation(fabOpen);
            removeMember.startAnimation(fabOpen);
            changeHead.startAnimation(fabOpen);
            changePrimary.startAnimation(fabOpen);

            callLayout.setClickable(true);
            familyDetail.setClickable(true);
            addNewMember.setClickable(true);
            removeMember.setClickable(true);
            changeHead.setClickable(true);
            changePrimary.setClickable(true);

            isFabMenuOpen = true;
        }
    }

    @Override
    public void onClick(View v) {
        onClickFloatingMenu.onClickMenu(v.getId());
        animateFAB();
    }
}
