package org.smartgresiter.jhpiego.provider;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.smartgresiter.jhpiego.R;
import org.smartgresiter.jhpiego.contract.FamilyRemoveMemberContract;
import org.smartgresiter.jhpiego.interactor.FamilyRemoveMemberInteractor;
import org.smartgresiter.jhpiego.util.Constants;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.provider.FamilyMemberRegisterProvider;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;

import java.util.HashMap;
import java.util.Set;

public class FamilyRemoveMemberProvider extends FamilyMemberRegisterProvider {

    Context context;
    android.view.View.OnClickListener footerClickListener;
    String familyID;

    public FamilyRemoveMemberProvider(String familyID, Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener, String familyHead, String primaryCaregiver) {
        super(context, commonRepository, visibleColumns, onClickListener, paginationClickListener, familyHead, primaryCaregiver);

        this.familyID = familyID;
        this.context = context;
        this.footerClickListener = paginationClickListener;
    }


    @Override
    public RecyclerView.ViewHolder createFooterHolder(ViewGroup parent) {
        View view = inflater().inflate(R.layout.family_remove_member_footer, parent, false);
        view.findViewById(R.id.top).setVisibility(View.GONE);
        return new RemoveFooterViewHolder(view);
    }

    @Override
    public void getFooterView(RecyclerView.ViewHolder viewHolder, final int currentPageCount, final int totalPageCount, boolean hasNext, boolean hasPrevious) {
        // do nothing
        FamilyRemoveMemberInteractor familyRemoveMemberInteractor = FamilyRemoveMemberInteractor.getInstance();
        final RemoveFooterViewHolder footerViewHolder = (RemoveFooterViewHolder) viewHolder;
        familyRemoveMemberInteractor.getFamilySummary(familyID, new FamilyRemoveMemberContract.InteractorCallback<HashMap<String, String>>() {
            @Override
            public void onResult(HashMap<String, String> result) {
                Integer children = Integer.valueOf(result.get(Constants.TABLE_NAME.CHILD));
                Integer members = Integer.valueOf(result.get(Constants.TABLE_NAME.FAMILY_MEMBER));

                if (children != null && members != null) {
                    int adults = members - children;

                    HashMap<String, String> payload = new HashMap<>();
                    payload.put(Constants.GLOBAL.MESSAGE, String.format("%s adults and %s U5 children", String.valueOf(adults), String.valueOf(children)));
                    payload.put(Constants.GLOBAL.NAME, result.get(Constants.GLOBAL.NAME));

                    footerViewHolder.hint.setText(payload.get(Constants.GLOBAL.MESSAGE));


                    footerViewHolder.view.setTag(payload);
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });

        footerViewHolder.view.setOnClickListener(footerClickListener);
    }

    public class RemoveFooterViewHolder extends FooterViewHolder {
        public CustomFontTextView hint;
        public View view;

        public RemoveFooterViewHolder(android.view.View view) {
            super(view);
            this.hint = view.findViewById(R.id.hint);
            this.view = view;

            hint.setFontVariant(FontVariant.REGULAR);
        }
    }
}

