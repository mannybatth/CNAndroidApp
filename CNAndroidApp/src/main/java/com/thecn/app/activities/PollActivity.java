package com.thecn.app.activities;

import android.animation.LayoutTransition;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.adapters.PollSubmissionAdapter;
import com.thecn.app.adapters.RosterAdapter;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.models.PollItem;
import com.thecn.app.models.Post;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.DisplayUtil;
import com.thecn.app.tools.InternalURLSpan;
import com.thecn.app.tools.LoadingViewController;
import com.thecn.app.tools.PausingHandler;
import com.thecn.app.views.google.slidingtabs.SlidingTabLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PollActivity extends ActionBarActivity {

    private ViewPager mViewPager;
    private QuestionAdapter mQuestionAdapter;
    private SlidingTabLayout mSlidingTabLayout;

    private Post mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            try {
                mPost = (Post) getIntent().getSerializableExtra("post");
                if (!mPost.getPostType().equals("survey")) throw new IllegalStateException();
                if (mPost.getItems() == null) throw new NullPointerException();
            } catch (Exception e) {
                finishWithError("Could not get poll data.");
                return;
            }
        } else {
            mPost = (Post) savedInstanceState.getSerializable("post");
        }

        setContentView(R.layout.activity_poll);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Poll");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mQuestionAdapter = new QuestionAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mQuestionAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("post", mPost);
    }

    public Post getPost() {
        return mPost;
    }

    private void finishWithError(String error) {
        AppSession.showToast(error);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class QuestionAdapter extends FragmentStatePagerAdapter {

        public QuestionAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PollFragment.newInstance(mPost.getItems().get(position));
        }

        @Override
        public int getCount() {
            return mPost.getItems().size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Integer.toString(position + 1);
        }
    }

    public static class PollFragment extends MyFragment {

        private PollItem mPollItem;
        private static final String ARG_POLL_ITEM = "poll_item";

        private final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
        private final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

        private LinearLayout mRootLayout, mInputLayout, mOutputLayout;
        private Button mSubmitButton, mViewResultsButton;
        private EditText mShortAnswerText;

        //pie chart
        private GraphicalView mChartView;

        private boolean mHasDataFault;

        private RosterController mRosterController;
        private PausingHandler mPausingHandler;

        public static PollFragment newInstance(PollItem pollItem) {
            PollFragment fragment = new PollFragment();
            Bundle args = new Bundle();
            args.putSerializable(ARG_POLL_ITEM, pollItem);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPollItem = (PollItem) getArguments().getSerializable(ARG_POLL_ITEM);
            checkData();
            if (mHasDataFault) return;

            mPausingHandler = new PausingHandler();
            mRosterController = new RosterController(this);
            mDialogDisplayType = DialogDisplayType.NOT_SHOWING;
        }

        private void checkData() {
            mHasDataFault =
               mPollItem == null
            || mPollItem.getSurveyType() == null
            || mPollItem.getInputType() == null
            || mPollItem.getInputType() != PollItem.InputType.SHORT_ANSWER
                       && hasChoiceDataFault();
        }

        private boolean hasChoiceDataFault() {
            ArrayList<PollItem.Choice> choices = mPollItem.getChoices();
            if (choices == null || choices.size() == 0) {
                return true;
            } else {
                for (PollItem.Choice choice : mPollItem.getChoices()) {
                    if (choice == null || choice.getSequenceId() == null || choice.getSubject() == null) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            if (mHasDataFault) {
                return createViewForError(inflater, container);
            } else {
                return createQuestionView(inflater, container);
            }
        }

        private View createViewForError(LayoutInflater inflater, ViewGroup container) {
            View view = inflater.inflate(R.layout.message_page_layout, container, false);
            TextView errorMessage = (TextView) view.findViewById(R.id.message);
            errorMessage.setText("Error loading question data.");
            return view;
        }

        private View createQuestionView(LayoutInflater inflater, ViewGroup container) {
            RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_poll, container, false);

            mRootLayout = (LinearLayout) inflater.inflate(R.layout.poll_root_layout, null);

            TextView questionTitle = (TextView) mRootLayout.findViewById(R.id.question);
            questionTitle.setText(mPollItem.getText());

            mSubmitButton = (Button) mRootLayout.findViewById(R.id.submit);
            mSubmitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO submit
                }
            });

            initRespondentsText();

            mInputLayout = new LinearLayout(getActivity());
            mInputLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            mInputLayout.setGravity(Gravity.CENTER);
            mInputLayout.setOrientation(LinearLayout.VERTICAL);

            mOutputLayout = new LinearLayout(getActivity());
            mOutputLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            mOutputLayout.setOrientation(LinearLayout.VERTICAL);

            Log.d("OBS", "short answer: " + mPollItem.getIsShortAnswer());
            if (mPollItem.getIsShortAnswer()) {
                ListView listView = new ListView(getActivity());
                listView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                listView.setFooterDividersEnabled(false);
                listView.addHeaderView(mRootLayout);
                listView.setAdapter(new RosterAdapter(this));
                view.addView(listView);
            } else {
                ScrollView scrollView = new ScrollView(getActivity());
                scrollView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                scrollView.setFillViewport(true);
                scrollView.addView(mRootLayout);
                view.addView(scrollView);

                if (mPollItem.getDisplayResult()) {
                    showChart();
                }
            }

            doInputLayout();

            mRootLayout.setLayoutTransition(new LayoutTransition());
            return view;
        } //end createQuestionView()

        private void initRespondentsText() {
            TextView respondentsText = (TextView) mRootLayout.findViewById(R.id.respondents_text);
            respondentsText.setMovementMethod(LinkMovementMethod.getInstance());
            Integer submissionCount = mPollItem.getSubmissionCount();
            if (submissionCount == null) {
                respondentsText.setVisibility(View.GONE);
                return;
            }

            String linkText = "";
            String verb;
            String tail = " responded to this question";
            linkText += submissionCount.toString();
            if (submissionCount > 1) {
                linkText += " people";
                verb = " have";
            } else if (submissionCount == 1) {
                linkText += " person";
                verb = " has";
            } else {
                respondentsText.setText("No one has" + tail);
                return;
            }

            SpannableString link = new SpannableString(linkText);
            link.setSpan(new InternalURLSpan(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showRespondents();
                }
            }), 0, link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            CharSequence text = TextUtils.concat(link, verb, tail);
            respondentsText.setText(text);
        }

        @Override
        public void onDestroyView() {
            if (mShortAnswerText != null) {
                Editable shortAnswer = mShortAnswerText.getText();
                if (shortAnswer != null) {
                    mPollItem.setShortAnswer(shortAnswer);
                }
            }

            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }

            super.onDestroyView();
        }

        private PollActivity getPollActivity() {
            return (PollActivity) getActivity();
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            showDialogIfWasShowing();
        }

        private void doInputLayout() {
            if (mPollItem.getOwnerIsMe()) {
                mSubmitButton.setVisibility(View.GONE);
            }
            if (mPollItem.getUserHasSubmitted()) {
                mSubmitButton.setVisibility(View.GONE);
            } else {
                switch (mPollItem.getInputType()) {
                    case SHORT_ANSWER:
                        layoutShortAnswer();
                        break;
                    case ONE_CHOICE:
                        layoutOneChoice();
                        break;
                    case MULTIPLE_CHOICE:
                        layoutMultipleChoice();
                        break;
                }
            }
        }

        private void addInputLayout() {
            if (mInputLayout.getParent() == null) {
                mRootLayout.addView(mInputLayout, 1);
            }
        }

        private void removeInputLayout() {
            if (mInputLayout.getParent() != null) {
                mRootLayout.removeView(mInputLayout);
            }
        }

        private void addOutputLayout() {
            if (mOutputLayout.getParent() == null) {
                mRootLayout.addView(mOutputLayout);
            }
        }

        private void removeOutputLayout() {
            if (mOutputLayout.getParent() != null) {
                mRootLayout.removeView(mOutputLayout);
            }
        }

        private void layoutOneChoice() {

            mInputLayout.removeAllViews();

            RadioGroup group = new RadioGroup(getActivity());
            group.setLayoutParams(new RadioGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            group.setOrientation(LinearLayout.VERTICAL);
            group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int id) {
                    for (int i = 0; i < radioGroup.getChildCount(); i++) {
                        PollRadioButton button = (PollRadioButton) radioGroup.getChildAt(i);
                        button.setChoiceSelected(button.getId() == id);
                    }
                }
            });

            for (PollItem.Choice choice : mPollItem.getChoices()) {
                PollRadioButton button = new PollRadioButton(getActivity(), choice);
                button.setEnabled(!mPollItem.getOwnerIsMe());
                group.addView(button, new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                if (choice.isSelected()) button.setChecked(true);
            }

            mInputLayout.addView(group);
            addInputLayout();
        }

        private void layoutMultipleChoice() {

            mInputLayout.removeAllViews();

            for (PollItem.Choice choice : mPollItem.getChoices()) {
                PollCheckBox checkBox = new PollCheckBox(getActivity(), choice);
                checkBox.setEnabled(!mPollItem.getOwnerIsMe());
                checkBox.setChecked(choice.isSelected());
                checkBox.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                mInputLayout.addView(checkBox);
            }

            addInputLayout();
        }

        private void layoutShortAnswer() {

            mInputLayout.removeAllViews();

            mShortAnswerText = new EditText(getActivity());
            mShortAnswerText.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            mShortAnswerText.setEnabled(!mPollItem.getOwnerIsMe());
            mShortAnswerText.setHint("Answer");
            mShortAnswerText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);

            if (mPollItem.getShortAnswer() != null) {
                mShortAnswerText.setText(mPollItem.getShortAnswer());
            }

            mInputLayout.addView(mShortAnswerText);
            addInputLayout();
        }

        private Dialog mDialog;
        private DialogDisplayType mDialogDisplayType;

        private enum DialogDisplayType {
            USER_ANSWER, USER, ANSWER, NOT_SHOWING
        }

        private void showDialogIfWasShowing() {
            switch (mDialogDisplayType) {
                case USER_ANSWER:
                    showSubmissionsPopup();
                    break;
                case ANSWER:
                    showSubmissionsPopup();
                    break;
                case USER:
                    showRespondents();
                    break;
            }
        }

        private void showSubmissionsPopup() {
            PollItem.SubmissionDisplayType displayType = mPollItem.getSubmissionDisplayType();

            if (displayType == PollItem.SubmissionDisplayType.NOTHING) return;

            if (mDialog != null) {
                mDialog.dismiss();
            }

            if (displayType == PollItem.SubmissionDisplayType.USER_ANSWER) {
                mDialogDisplayType = DialogDisplayType.USER_ANSWER;
            } else if (displayType == PollItem.SubmissionDisplayType.ANSWER) {
                mDialogDisplayType = DialogDisplayType.ANSWER;
            } else {
                return;
            }

            ArrayList<PollItem.Submission> submissions = mPollItem.getSubmissions();
            if (submissions == null) {
                return;
            }

            PollSubmissionAdapter adapter = new PollSubmissionAdapter(this, displayType);
            adapter.addAll(submissions);

            mDialog = new Dialog(getActivity());
            mDialog.setTitle("BLARG");
            MyDialogListView listView = new MyDialogListView(getActivity());
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(null);
            mDialog.setContentView(listView);

            mDialog.show();
        }

        private void showRespondents() {
            if (mDialog != null) {
                mDialog.dismiss();
            }

            mDialogDisplayType = DialogDisplayType.USER;

            mDialog = new MyDialog(getActivity());
            Integer submissionCount = mPollItem.getSubmissionCount();
            String titleHead = "";
            String titleTail = "";
            if (submissionCount != null) {
                titleHead = submissionCount.toString() + " ";
                if (submissionCount > 1) titleTail += "s";
            }
            mDialog.setTitle(titleHead + "Respondent" + titleTail);

            ListView listView;
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                listView = new ListView(getActivity());
            } else {
                listView = new MyDialogListView(getActivity());
            }
            mRosterController.setListView(listView);
            mDialog.setContentView(listView);

            mDialog.show();
        }

        private void prepareChart() {

            prepareChartData();

            DefaultRenderer renderer = new DefaultRenderer();

            renderer.setChartTitleTextSize(20);
            renderer.setInScroll(true);
            renderer.setPanEnabled(false);
            renderer.setShowLabels(false);
            renderer.setZoomEnabled(false);
            renderer.setExternalZoomEnabled(false);
            renderer.setShowLegend(false); //screw your legend, I'll make my own
            renderer.setStartAngle(270);

            CategorySeries series = new CategorySeries(mPollItem.getText());

            for (PollItem.ChartMember member : mPollItem.getChartData()) {
                series.add(member.getName(), member.getCount());
                SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
                seriesRenderer.setColor(member.getColor());
                renderer.addSeriesRenderer(seriesRenderer);
            }

            mChartView = ChartFactory.getPieChartView(getActivity(), series, renderer);
        }

        //thanks to Niels Bosma for this idea http://stackoverflow.com/a/5651670
        private void prepareChartData() {
            ArrayList<PollItem.ChartMember> chartMembers = mPollItem.getChartData();
            if (chartMembers == null || chartMembers.size() == 0) return;

            LinkedList<PollItem.ChartMember> membersWithSlice = new LinkedList<PollItem.ChartMember>();

            int countTotal = 0;
            /**
             * count the total counts (or votes) by people who answered this poll
             * add chart members with at least one vote to a list
             * so their color can be added later
             */
            for (PollItem.ChartMember member : chartMembers) {
                int count = member.getCount();
                countTotal += count;

                if (count > 0) {
                    membersWithSlice.add(member);
                }
            }
            mPollItem.setCountTotal(countTotal);

            int baseColor = Color.rgb(237, 194, 64);
            membersWithSlice.getFirst().setColor(baseColor);

            float[] baseHSV = new float[3];
            Color.colorToHSV(baseColor, baseHSV);
            double step = 360.0 / (double) membersWithSlice.size();

            for (int i = 1; i < membersWithSlice.size(); i++) {
                /**
                 * derive other pie slice colors from base color
                 * "grab" these colors from all around the base color's hue continuum
                 */
                float[] newHSV = new float[3];
                newHSV[0] = (float) ((baseHSV[0] + step * ((double) i)) % 360.0);
                newHSV[1] = baseHSV[1];
                newHSV[2] = baseHSV[2];

                int newColor = Color.HSVToColor(newHSV);
                membersWithSlice.get(i).setColor(newColor);
            }
        }

        private void showChart() {
            prepareChart();

            mOutputLayout.addView(mChartView);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mChartView.getLayoutParams();
            params.height = (int) getActivity().getResources().getDimension(R.dimen.pie_chart_height);

            int largestColorAreaWidth = 0;
            int largestWidth = 0;
            List<LinearLayout> legendEntries = new ArrayList<LinearLayout>();

            for (PollItem.ChartMember member : mPollItem.getChartData()) {
                LinearLayout legendEntry = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.poll_chart_legend_entry, null);

                TextView colorArea = (TextView) legendEntry.findViewById(R.id.legend_item_color_area);
                if (member.getCount() == 0) {
                    colorArea.setBackgroundColor(Color.BLACK);
                    colorArea.setText("0%");
                } else {
                    colorArea.setBackgroundColor(member.getColor());
                    int percent = member.getCount() * 100 / mPollItem.getCountTotal();
                    colorArea.setText(Integer.toString(percent) + "%");
                }

                int colorAreaWidth = getProjectedWidth(colorArea);
                if (colorAreaWidth > largestColorAreaWidth)
                    largestColorAreaWidth = colorAreaWidth;

                TextView choiceText = (TextView) legendEntry.findViewById(R.id.text_area);
                choiceText.setText(member.getName());

                legendEntries.add(legendEntry);
            }

            for (LinearLayout legendEntry : legendEntries) {

                TextView colorArea = (TextView) legendEntry.findViewById(R.id.legend_item_color_area);
                int colorAreaWidth = colorArea.getMeasuredWidth(); //should be set from before
                //set right margin to make up for any difference in width between color area and text
                ViewGroup.MarginLayoutParams colorAreaParams = (ViewGroup.MarginLayoutParams) colorArea.getLayoutParams();
                int newMargin = largestColorAreaWidth - colorAreaWidth;
                if (colorAreaParams == null) {
                    colorAreaParams = new ViewGroup.MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                }

                colorAreaParams.leftMargin += newMargin;

                int width = getProjectedWidth(legendEntry);
                if (width > largestWidth) largestWidth = width;
            }

            int fragmentRootViewWidth = getFragmentRootViewWidth();
            int columns = fragmentRootViewWidth / largestWidth;
            int leftMargin = fragmentRootViewWidth % largestWidth;

            if (columns > 1) {
                mOutputLayout.setGravity(Gravity.CENTER);

                LinearLayout row = getGridRow(leftMargin);

                if (legendEntries.size() > 0) {
                    int numEntries = legendEntries.size();
                    for (int i = 0; i < numEntries - 1; i++) {
                        addLegendEntry(legendEntries.get(i), row, largestWidth);

                        if (i % columns == columns - 1) {
                            mOutputLayout.addView(row);
                            row = getGridRow(leftMargin);
                        }
                    }

                    addLegendEntry(legendEntries.get(numEntries - 1), row, largestWidth);
                    mOutputLayout.addView(row);
                }
            } else {
                mOutputLayout.setGravity(Gravity.LEFT);

                for (LinearLayout entry : legendEntries) {
                    addLegendEntry(entry, mOutputLayout, WRAP_CONTENT);
                }
            }

            addOutputLayout();
        }

        private int getFragmentRootViewWidth() {
            int padding = (int) getResources().getDimension(R.dimen.activity_horizontal_margin) * 2;
            return DisplayUtil.getDisplayWidth(getActivity()) - padding;
        }

        private int getProjectedWidth(View view) {
            //when using UNSPECIFIED, size input does not matter, view measures itself as large is it wants
            int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

            view.measure(widthSpec, heightSpec);

            //measured width is the projected width of a view based on MeasureSpecs passed in measure()
            //this is not necessarily the same as when the view is actually rendered
            return view.getMeasuredWidth();
        }

        private LinearLayout getGridRow(int leftMargin) {
            LinearLayout gridRow = new LinearLayout(getActivity());
            gridRow.setGravity(Gravity.LEFT);
            ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT);
            marginParams.setMargins(leftMargin, 0, 0, 0);
            gridRow.setLayoutParams(marginParams);

            return gridRow;
        }

        private void addLegendEntry(LinearLayout entry, ViewGroup parent, int width) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, WRAP_CONTENT);
            entry.setLayoutParams(params);
            parent.addView(entry);
        }

        private class MyDialog extends Dialog {

            public MyDialog(Context context) {
                super(context);
            }

            @Override
            public void show() {
                super.show();
                mPausingHandler.resume();
            }

            @Override
            public void dismiss() {
                mPausingHandler.pause();
                super.dismiss();
            }

            @Override
            public void cancel() {
                mPausingHandler.pause();
                mPausingHandler.clearQueue();
                super.cancel();
                mDialogDisplayType = DialogDisplayType.NOT_SHOWING;
                mRosterController.reset();
            }
        }

        private class MyDialogListView extends ListView {

            public MyDialogListView(Context context) {
                super(context);
            }

            int maxHeight = getResources().getDisplayMetrics().heightPixels * 5 / 9;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                //make sure height cannot exceed a certain limit
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }

        /**
         * Used to communicate between poll respondent data and a Dialog that displays it.
         */
        private class RosterController {
            private int offset = 0;
            private final int limit = 10;
            private boolean loading = false;
            private boolean noMore = false;

            private final Object layoutLock = new Object();

            private RosterAdapter mAdapter;
            private LoadingViewController mFooter;

            public RosterController(MyFragment fragment) {
                mAdapter = new RosterAdapter(fragment);
            }

            public void reset() {
                mAdapter.clear();
                offset = 0;
                loading = false;
                noMore = false;
            }

            /**
             * Sets up the given listView to display the current state of the data
             * @param listView listview to display the data
             */
            public void setListView(ListView listView) {
                synchronized (layoutLock) {
                    mFooter = new LoadingViewController(listView, LayoutInflater.from(listView.getContext()));
                    listView.setFooterDividersEnabled(false);
                    listView.setAdapter(mAdapter);
                    listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(AbsListView absListView, int i) {
                        }

                        @Override
                        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                            if ((totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
                                getUsers();
                            }
                        }
                    });
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            User user = mAdapter.getItem(i);
                            if (user == null) return;

                            Intent intent = new Intent(getActivity(), ProfileActivity.class);
                            intent.putExtra("user", user);
                            startActivity(intent);

                            mDialog.cancel();
                        }
                    });

                    if (loading) {
                        mFooter.setLoading();
                    } else if (mAdapter.getCount() == 0) {
                        getUsers();
                    } else {
                        mFooter.remove();
                    }
                }
            }

            public void getUsers() {
                if (!loading && !noMore) {
                    loading = true;
                    mFooter.setLoading();

                    String contentID = getPollActivity().getPost().getId();
                    String itemID = mPollItem.getId();
                    UserStore.getPollRespondents(contentID, itemID, limit, offset, new ResponseCallback(mPausingHandler) {
                        @Override
                        public void onSuccess(JSONObject response) {
                            ArrayList<User> users = UserStore.getListData(response);

                            if (users != null) {
                                mAdapter.addAll(users);
                                int nextOffset = StoreUtil.getNextOffset(response);
                                if (nextOffset != -1) offset = nextOffset;
                            } else {
                                noMore = true;
                            }
                        }

                        @Override
                        public void onFailure(JSONObject response) {
                            AppSession.showDataLoadError("user list");
                        }

                        @Override
                        public void onError(Exception error) {
                            StoreUtil.showExceptionMessage(error);
                        }

                        @Override
                        public void onPostExecute() {
                            onLoadingComplete();
                        }
                    });
                }
            }

            private void onLoadingComplete() {
                synchronized (layoutLock) {
                    loading = false;

                    mFooter.remove();
                }
            }
        } //end RosterController class

    } //end PollFragment class

    private static class PollRadioButton extends RadioButton {

        private PollItem.Choice mChoice;

        public void setChoiceSelected(boolean selected) {
            mChoice.setSelected(selected);
        }

        public PollRadioButton(Context context, PollItem.Choice choice) {
            super(context);

            mChoice = choice;
            setText(choice.getSubject());
        }
    }

    private static class PollCheckBox extends CheckBox {

        private PollItem.Choice mChoice;

        public PollCheckBox(Context context, PollItem.Choice choice) {
            super(context);

            mChoice = choice;
            setText(choice.getSubject());
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mChoice.setSelected(isChecked());
                }
            });
        }
    }
}
