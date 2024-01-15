package com.thecn.app.activities;

import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioGroup.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.thecn.app.R;

/**
 * Used for such content as Course, Profile, and Conexus pages
 * contains a SlidingUpPanelLayout for switching between pages
 * One fragment is on static pane, all other fragments are interchanged
 * using buttons on the sliding pane.
 */
public abstract class ContentPageActivity extends NavigationActivity {

    private SlidingUpPanelLayout slidingLayout;

    private FragmentPackage staticFragmentPkg; //fragment on static pane
    private FragmentPackage[] fragmentPkgs; //all other fragments on sliding pane
    private int currentFragmentIndex;

    private RadioGroup buttonLayout;

    /**
     * Used to get fragments from sub class
     */
    public interface FragmentCallback {
        public Fragment getFragment();
    }

    /**
     * Class to associate a Fragment, its name,
     * and its callback object (for getting the fragment itself)
     */
    public static class FragmentPackage {
        private String fragmentName;
        private String fragmentKey;
        private Fragment fragment;
        private FragmentCallback fragmentCallback;

        public FragmentPackage(String fragmentName, String fragmentKey, FragmentCallback fragmentCallback) {
            this.fragmentName = fragmentName;
            this.fragmentKey = fragmentKey;
            this.fragmentCallback = fragmentCallback;
        }

        public String getFragmentKey() { return fragmentKey; }

        public String getFragmentName() {
            return fragmentName;
        }

        public Fragment getFragment() {
            return fragment;
        }

        public void setFragment(Fragment fragment) {
            this.fragment = fragment;
        }

        public Fragment setFragmentFromCallback() {
            return fragment = fragmentCallback.getFragment();
        }

        public boolean isFragmentSet() {
            return fragment != null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        slidingLayout = (SlidingUpPanelLayout)
                findViewById(R.id.sliding_layout);
        slidingLayout.setSlidingEnabled(false);
        slidingLayout.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.post_button_height));


        //both of these methods specified by child class
        staticFragmentPkg = getStaticFragmentPackage();
        fragmentPkgs = getFragmentPackages();

        if (savedInstanceState != null) {
            currentFragmentIndex = savedInstanceState.getInt(FRAGMENT_INDEX_TAG);

            unbundleFragment(savedInstanceState, staticFragmentPkg);
            for (FragmentPackage pkg : fragmentPkgs) {
                unbundleFragment(savedInstanceState, pkg);
            }


        } else {
            currentFragmentIndex = -1;
        }

        createButtons();
    }

    @Override
    public void onBackPressed() {
        if (slidingLayout.isExpanded()) {
            slidingLayout.collapsePane();
            buttonLayout.check(0);
        } else {
            super.onBackPressed();
        }
    }

    private static final String FRAGMENT_INDEX_TAG = "FRAGMENT_INDEX";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        bundleFragmentPkg(outState, staticFragmentPkg);
        for (FragmentPackage pkg : fragmentPkgs) {
            bundleFragmentPkg(outState, pkg);
        }

        outState.putInt(FRAGMENT_INDEX_TAG, currentFragmentIndex);
    }

    private void bundleFragmentPkg(Bundle outState, FragmentPackage pkg) {
        if (pkg.isFragmentSet()) {
            String key = pkg.fragmentKey;
            Fragment fragment = pkg.fragment;
            getSupportFragmentManager().putFragment(outState, key, fragment);
        }
    }

    private void unbundleFragment(Bundle savedInstanceState, FragmentPackage pkg) {
        Fragment fragment = getSupportFragmentManager()
                .getFragment(savedInstanceState, pkg.fragmentKey);

        pkg.setFragment(fragment);
    }

    /**
     * Used to populate RadioGroup with radio buttons dynamically,
     * based on the number of fragments specified by the child class
     */
    private void createButtons() {
        buttonLayout = (RadioGroup) findViewById(R.id.controls_container);

        //this is a RadioGroup.LayoutParams
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.weight = 1;

        RadioButton buttonHolder;

        buttonHolder = getRadioButton(staticFragmentPkg);
        buttonHolder.setId(0);
        buttonHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingLayout.collapsePane();
            }
        });
        buttonLayout.addView(buttonHolder, params);

        for (int i = 0; i < fragmentPkgs.length; i++) {
            final FragmentPackage fp = fragmentPkgs[i];
            final int index = i;

            buttonHolder = getRadioButton(fp);
            buttonHolder.setId(i + 1);
            buttonHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //on click, switch to this fragment in top pane
                    //and slide the top pane over the bottom pane
                    openFragment(index);
                    slidingLayout.expandPane();
                }
            });

            buttonLayout.addView(buttonHolder, params);
        }

        buttonLayout.check(currentFragmentIndex + 1);
    }

    /**
     * Helper method for createButtons.  Customizes button.
     * @param fp fragment package that contains text to set in button
     * @return the instantiated RadioButton
     */
    private RadioButton getRadioButton(FragmentPackage fp) {
        RadioButton button = new RadioButton(this);

        button.setBackgroundResource(R.drawable.group_fragment_button);
        button.setButtonDrawable(new StateListDrawable()); //removes circle part of button
        button.setText(fp.getFragmentName());
        button.setGravity(Gravity.CENTER);

        return button;
    }

    /**
     * Sets initial fragments shown in layout
     * @param index index of fragment to add to sliding pane
     */
    public void initFragments(int index) {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_below, staticFragmentPkg.setFragmentFromCallback())
                .commit();

        //openFragment(index);
    }

    @Override
    protected void mySetContentView() {
        setContentView(R.layout.activity_content_page);
    }

    /**
     * Used by subclasses to specify static fragment on static pane
     * Any relevant fields MUST be instantiated if specified in this method (duh)
     * @return one fragment to rule them all
     */
    abstract FragmentPackage getStaticFragmentPackage();

    /**
     * Used by subclasses to specify fragments to open by index
     * Any relevant fields MUST be instantiated if specified in this method (duh)
     * @return list of Fragments grouped with their names and instance functions
     */
    abstract FragmentPackage[] getFragmentPackages();

    /**
     * Opens a fragment in the sliding view
     * @param index index of fragment
     */
    public void openFragment(int index) {

        int numFragments = fragmentPkgs.length;

        //if proper index
        if (-1 < index && index < numFragments) {
            Fragment fragment = fragmentPkgs[index].getFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (fragment == null) {
                fragment = fragmentPkgs[index].setFragmentFromCallback();
                transaction.add(R.id.container_above, fragment);
            } else {
                transaction.show(fragment);
            }

            //iterates through all other fragments in view and hides them
            //uses modular arithmetic
            for (int i = (index + 1) % numFragments;
                 i != index;
                 i = (i + 1) % numFragments) {

                Fragment fragmentToHide = fragmentPkgs[i].getFragment();

                if (fragmentToHide != null) {
                    transaction.hide(fragmentToHide);
                }

            }

            transaction.commit();
            currentFragmentIndex = index;
        }
    }

    protected int getCurrentFragmentIndex() {
        return currentFragmentIndex;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.content_page, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

}
