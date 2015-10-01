package cz.anty.attendancemanager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import cz.anty.attendancemanager.receiver.TrackingReceiver;
import cz.anty.utils.Constants;
import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.TrackingMansManager;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 1.10.15.
 *
 * @author anty
 */
public class TrackingSpecialModule extends SpecialModule {

    private TrackingMansManager mansManager;
    private boolean hideItems = false;

    public TrackingSpecialModule(Context context) {
        super(context);
    }

    @Override
    protected boolean isInitOnThread() {
        return true;
    }

    @Override
    protected boolean isUpdateOnThread() {
        return true;
    }

    @Override
    protected void onInitialize() {
        mansManager = new TrackingMansManager(getContext());
        onUpdate();
    }

    @Override
    protected void onUpdate() {
        TrackingReceiver.refreshTrackingMans(getContext(), mansManager, true);
    }

    @Override
    protected SpecialItem[] getItems() {
        if (hideItems || mansManager.get().length == 0) return new SpecialItem[0];
        return new SpecialItem[]{
                new SpecialItem() {
                    private TextView title;
                    private LinearLayout mansLinearLayout;

                    @Override
                    public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
                        LinearLayout linearLayout = new LinearLayout(getContext());
                        linearLayout.setOrientation(LinearLayout.VERTICAL);

                        LayoutInflater.from(getContext()).inflate(R.layout
                                .base_multiline_text_item, linearLayout);

                        title = (TextView) linearLayout.findViewById(R.id.text_view_title);
                        linearLayout.findViewById(R.id.text_view_text).setVisibility(View.GONE);

                        mansLinearLayout = new LinearLayout(getContext());
                        mansLinearLayout.setOrientation(LinearLayout.VERTICAL);

                        linearLayout.addView(mansLinearLayout);
                        parent.addView(linearLayout);
                    }

                    @Override
                    public void onBindViewHolder(int itemPosition) {
                        title.setText(R.string.activity_title_tracking);

                        mansLinearLayout.removeAllViews();
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        for (Man man : mansManager.get()) {
                            View view = inflater.inflate(R.layout
                                    .text_widget_multi_line_list_item, mansLinearLayout, false);
                            ((TextView) view.findViewById(R.id.widget_text_view_title))
                                    .setText(man.getTitle(getContext(), 1));
                            ((TextView) view.findViewById(R.id.widget_text_view_text))
                                    .setText(man.getText(getContext(), 1));
                            mansLinearLayout.addView(view);
                        }
                    }

                    @Override
                    public void onClick() {
                        getContext().startActivity(
                                new Intent(getContext(), TrackingActivity.class));
                    }

                    @Override
                    public void onLongClick() {

                    }

                    @Override
                    public void onHideClick() {
                        hideItems = true;
                        notifyItemsChanged();
                    }

                    @Override
                    public boolean isShowHideButton() {
                        return true;
                    }

                    @Override
                    public int getPriority() {
                        return Constants.SPECIAL_ITEM_PRIORITY_TRACKING;
                    }
                }
        };
    }

    @Override
    protected int getModuleNameResId() {
        return R.string.activity_title_tracking;
    }
}
