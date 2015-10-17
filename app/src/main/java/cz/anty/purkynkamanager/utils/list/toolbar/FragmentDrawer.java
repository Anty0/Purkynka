package cz.anty.purkynkamanager.utils.list.toolbar;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.list.listView.MultilineItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.list.recyclerView.RecyclerInflater;
import cz.anty.purkynkamanager.utils.list.recyclerView.RecyclerItemClickListener;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public class FragmentDrawer extends Fragment {

    private MultilineRecyclerAdapter<MultilineItem> adapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View containerView;
    private RecyclerItemClickListener.ClickListener drawerListener;

    public FragmentDrawer() {

    }

    public void setDrawerListener(RecyclerItemClickListener.ClickListener listener) {
        this.drawerListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new MultilineRecyclerAdapter<>(getContext(),
                R.layout.base_list_item_multi_line_text);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating view layout
        return RecyclerInflater.inflate(inflater, getContext(), container, false, R.layout.fragment_navigation_drawer,
                adapter, null, new RecyclerItemClickListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        mDrawerLayout.closeDrawer(containerView);
                        drawerListener.onClick(view, position);
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        drawerListener.onLongClick(view, position);
                    }
                });
    }


    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar, MultilineItem... data) {
        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar,
                R.string.but_open, R.string.but_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (Build.VERSION.SDK_INT >= 11)
                    getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (Build.VERSION.SDK_INT >= 11)
                    getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (Build.VERSION.SDK_INT >= 11)
                    toolbar.setAlpha(1 - slideOffset / 2);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        adapter.clearItems();
        adapter.addAllItems(data);
    }

    public MultilineRecyclerAdapter<MultilineItem> getAdapter() {
        return adapter;
    }
}