package com.thecn.app.fragments.Course;



import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.NavigationActivity;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.Post;
import com.thecn.app.models.Course.Task;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.InternalURLSpan;
import com.thecn.app.tools.ServiceChecker;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class CourseTasksFragment extends MyFragment implements OnRefreshListener {

    public static final String TAG = CourseTasksFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_COURSE_KEY = "course";

    private Course mCourse;
    private ArrayList<Task> mTasks;
    private final Object mTaskLock = new Object();

    protected PullToRefreshLayout mPullToRefreshLayout;

    private int tasksIndex;
    private int numTasks;
    private TaskLinkPatterns tlps;

    private Button leftTaskButton;
    private Button rightTaskButton;
    private TextView taskTitle;
    private TextView taskContent;

    public static CourseTasksFragment newInstance(Course mCourse) {
        CourseTasksFragment fragment = new CourseTasksFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_COURSE_KEY, mCourse);
        fragment.setArguments(args);

        return fragment;
    }

    public CourseTasksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCourse = (Course) getArguments().getSerializable(FRAGMENT_BUNDLE_COURSE_KEY);
        mTasks = mCourse.getTasks();
        numTasks = mTasks != null ? mTasks.size() : 0;
        tasksIndex = 0;

        tlps = new TaskLinkPatterns();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_tasks, container, false);

        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .options(Options.create()
                        .scrollDistance(.35f)
                        .build())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        taskTitle = (TextView) view.findViewById(R.id.task_title);
        taskContent = (TextView) view.findViewById(R.id.task_content);
        taskContent.setMovementMethod(LinkMovementMethod.getInstance());

        leftTaskButton = (Button) view.findViewById(R.id.left_task_button);
        leftTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLeftTaskButtonClick();
            }
        });
        leftTaskButton.setVisibility(View.INVISIBLE);

        rightTaskButton = (Button) view.findViewById(R.id.right_task_button);
        rightTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRightTaskButtonClick();
            }
        });
        rightTaskButton.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (numTasks > 0) {
            setTask();
            loadTasks();
        }
        else {
            taskTitle.setText("");
            taskContent.setText("No tasks available for this course.");
        }
    }

    private void setTextFieldsLoading() {
        taskTitle.setText("Loading...");
        taskContent.setText("");
    }


    private void loadTasks() {

        int[] indices = getIndicesOfTasksToLoad();

        synchronized (mTaskLock) {
            for (final int i : indices) {
                Task localTask = mTasks.get(i);

                if (localTask.getLoadingState() == Task.LoadingState.NOT_SET) {
                    localTask.setLoadingState(Task.LoadingState.LOADING);

                    CourseStore.getTaskDetails(localTask.getId(), new ResponseCallback(getHandler()) {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Task task = CourseStore.getTaskData(response);

                            if (task != null) {
                                new FormatTaskTask(i, task.getTitle(), task.getDisplayText()).execute();
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                            synchronized (mTaskLock) {
                                if (mTasks != null) {
                                    Task task = mTasks.get(i);
                                    task.setFormattedContent("Could not load task content");
                                    task.setLoadingState(Task.LoadingState.DONE_LOADING);
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private int[] getIndicesOfTasksToLoad() {

        ArrayList<Integer> indices = new ArrayList<Integer>();

        indices.add(tasksIndex);

        int holder;

        holder = tasksIndex - 1;
        if (holder >= 0) {
            indices.add(holder);

            holder--;

            if (holder >= 0) indices.add(holder);
        }

        holder = tasksIndex + 1;
        if (holder < numTasks) {
            indices.add(holder);

            holder++;

            if (holder < numTasks) indices.add(holder);
        }

        int[] returnIndices = new int[indices.size()];
        for (int i = 0; i < returnIndices.length; i++) {
            returnIndices[i] = indices.get(i);
        }

        return returnIndices;

    }

    private class FormatTaskTask extends AsyncTask<Void, Void, Void> {

        int mIndex;
        CharSequence mTitle;
        CharSequence mContent;

        public FormatTaskTask(int index, String title, String content) {
            mIndex = index;
            mTitle = title;
            mContent = content;
        }

        @Override
        protected Void doInBackground(Void... params) {

            mTitle = formatTaskTitle(mTitle.toString());
            mContent = formatTaskContent(mContent.toString());

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            synchronized (mTaskLock) {
                if (mTasks != null) {
                    Task task = mTasks.get(mIndex);

                    task.setFormattedTitle(mTitle);
                    task.setFormattedContent(mContent);

                    task.setLoadingState(Task.LoadingState.DONE_LOADING);

                    setTask();
                }
            }
        }
    }

    private void setTask() {

        synchronized (mTaskLock) {

            Task task = mTasks.get(tasksIndex);

            if (task.getLoadingState() == Task.LoadingState.DONE_LOADING) {
                taskTitle.setText(task.getFormattedTitle());
                taskContent.setText(task.getFormattedContent());
            } else {
                setTextFieldsLoading();
            }
        }

        if (tasksIndex != 0) {
            leftTaskButton.setEnabled(true);
            leftTaskButton.setVisibility(View.VISIBLE);
        }
        else {
            leftTaskButton.setEnabled(false);
            leftTaskButton.setVisibility(View.INVISIBLE);
        }

        if (tasksIndex < numTasks - 1) {
            rightTaskButton.setEnabled(true);
            rightTaskButton.setVisibility(View.VISIBLE);
        }
        else {
            rightTaskButton.setEnabled(false);
            rightTaskButton.setVisibility(View.INVISIBLE);
        }

    }

    private CharSequence formatTaskTitle(String taskTitle) {
        return Html.fromHtml(taskTitle);
    }

    private static class LinkAssociator {
        public String stringID;
        public SpannableString span;

        public LinkAssociator(final String stringID, SpannableString span) {
            this.stringID = stringID;
            this.span = span;
        }
    }

    private CharSequence formatTaskContent(String taskContent) {

        if (taskContent != null) {
            ArrayList<LinkAssociator> linkAssociations = new ArrayList<LinkAssociator>();

            String head = "#@%!&";
            String tail = "&!%@#";

            int linkIndex = 0;
            for (int typeIndex = 0; typeIndex < tlps.allPatterns.length; typeIndex++) {
                Pattern[] currentPatternSet = tlps.allPatterns[typeIndex];

                for (int patternIndex = 0; patternIndex < currentPatternSet.length; patternIndex++) {
                    Pattern pattern = currentPatternSet[patternIndex];

                    boolean keepGoing = true;
                    int matcherIndex = 0;
                    while (keepGoing) {

                        Matcher matcher = pattern.matcher(taskContent);
                        if (matcher.find(matcherIndex)) {

                            Matcher anchorCloseMatcher = tlps.anchorClosePattern.matcher(taskContent);
                            if (anchorCloseMatcher.find(matcher.end())) {
                                String replacement = head + Integer.toString(linkIndex) + tail;

                                final String linkContent = taskContent.substring(matcher.end(), anchorCloseMatcher.start());

                                taskContent = taskContent.substring(0, matcher.start())
                                        + replacement + taskContent.substring(anchorCloseMatcher.end(), taskContent.length());
                                matcherIndex = matcher.start() + replacement.length();

                                SpannableString finalSpan = new SpannableString(Html.fromHtml(linkContent));
                                //supported link
                                if (typeIndex == 0) {
                                    //possible location of the id is either in group 1 or 2 depending on how
                                    //regex was matched
                                    String contentID = matcher.group(1);
                                    final String finalContentID = contentID != null ? contentID : matcher.group(2);

                                    final InternalURLSpan ius = new InternalURLSpan();
                                    if (patternIndex < 2) {
                                        ius.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (!ius.isClickActionWorking()) {
                                                    ius.setClickActionWorking(true);
                                                    beginDownload(finalContentID);
                                                    ius.setClickActionWorking(false);
                                                }
                                            }
                                        });
                                    } else {
                                        ius.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (!ius.isClickActionWorking()) {
                                                    ius.setClickActionWorking(true);
                                                    PostStore.getPostById(finalContentID, new ResponseCallback(getHandler()) {
                                                        @Override
                                                        public void onSuccess(JSONObject response) {
                                                            Post post = PostStore.getData(response);

                                                            if (post != null) {
                                                                ((NavigationActivity) getActivity()).openPostPage(post, false);
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(JSONObject response) {
                                                            AppSession.showToast("Post does not exist.");
                                                        }

                                                        @Override
                                                        public void onError(Exception error) {
                                                            StoreUtil.showExceptionMessage(error);
                                                        }

                                                        @Override
                                                        public void onPostExecute() {
                                                            ius.setClickActionWorking(false);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                    finalSpan.setSpan(ius, 0, finalSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                } else {
                                    final InternalURLSpan ius = new InternalURLSpan();
                                    ius.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AppSession.showToast("Link not currently supported on Android.");
                                        }
                                    });
                                    finalSpan.setSpan(ius, 0, finalSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }

                                linkAssociations.add(new LinkAssociator(replacement, finalSpan));
                                linkIndex++;
                            }
                        } else {
                            keepGoing = false;
                        }
                    }
                }
            }

            CharSequence finalText = Html.fromHtml(taskContent);

            String[] stringIDs = new String[linkAssociations.size()];
            SpannableString[] links = new SpannableString[linkAssociations.size()];

            for (int i = 0; i < linkAssociations.size(); i++) {
                LinkAssociator linkAssociation = linkAssociations.get(i);
                stringIDs[i] = linkAssociation.stringID;
                links[i] = linkAssociation.span;
            }

            //return oldTaskContent;
            return TextUtils.replace(finalText, stringIDs, links);
        }

        return "(No task content)";
    }

    private void beginDownload(String contentID) {
        String url = "http://cndev.coursenetworking.com/program/attachment/view/" + contentID;
        Uri uri = Uri.parse(url);

        if (ServiceChecker.isDownloadManagerAvailable(getActivity())) {

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setDescription("CN: File downloaded for " + mCourse.getName());
            request.setTitle("CN File Download");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            }
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "theCN_file_" + contentID);

            DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);

            AppSession.showToast("Downloading...");

        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    private void onLeftTaskButtonClick() {
        if (tasksIndex != 0) {
            tasksIndex --;
            loadTasks();
            setTask();
        }
    }

    private void onRightTaskButtonClick() {
        if (tasksIndex < numTasks - 1) {
            tasksIndex ++;
            loadTasks();
            setTask();
        }
    }

    //private String postLinkPattern

    @Override
    public void onRefreshStarted(View view) {
        synchronized (mTaskLock) {
            for (Task t : mTasks) {
                t.setLoadingState(Task.LoadingState.NOT_SET);
            }
        }

        loadTasks();

        mPullToRefreshLayout.setRefreshComplete();
    }

    private static class TaskLinkPatterns {

        //for some reason there are two versions of valid html tags....so...
        public Pattern downloadAttachment;
        public Pattern downloadAttachment2;
        public Pattern uploadAttachment;
        public Pattern uploadAttachment2;
        public Pattern viewPost;
        public Pattern viewPost2;
        public Pattern viewPoll;
        public Pattern viewPoll2;
        public Pattern viewEvent;
        public Pattern viewEvent2;
        public Pattern viewQuiz;
        public Pattern viewQuiz2;
        public Pattern createPost;
        public Pattern createPost2;
        public Pattern createPoll;
        public Pattern createPoll2;

        public Pattern catchAllUnsupported;

        public Pattern anchorClosePattern;

        public Pattern[] postPatterns;
        public Pattern[] unsupportedPatterns;
        public Pattern[][] allPatterns;

        public TaskLinkPatterns() {
            String anchorStart = "<[^>]*a[^>]*";
            String typeHTML = "data-taskactionlink-type[\\s]*=[\\s]*\"";
            String quoteTail = "\"[^>]*";
            String id = "data-taskactionlink-data-id[\\s]*=[\\s]*\"([a-zA-Z0-9]*)\"[^>]*";
            String id2 = "data-taskactionlink-id[\\s]*=[\\s]*\"([a-zA-Z0-9]*)\"[^>]*";

            String head = anchorStart + typeHTML;
            String middle = quoteTail + id + ">|" + anchorStart + id + typeHTML;
            String middle2 = quoteTail + id2 + ">|" + anchorStart + id2 + typeHTML;
            String tail = quoteTail + ">";

            String type;

            type = "download_attachment";
            downloadAttachment = Pattern.compile(head + type + middle + type + tail);
            downloadAttachment2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "upload_attachment";
            uploadAttachment = Pattern.compile(head + type + middle + type + tail);
            uploadAttachment2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "view_post";
            viewPost = Pattern.compile(head + type + middle + type + tail);
            viewPost2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "view_survey";
            viewPoll = Pattern.compile(head + type + middle + type + tail);
            viewPoll2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "view_event";
            viewEvent = Pattern.compile(head + type + middle + type + tail);
            viewEvent2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "view_quiz";
            viewQuiz = Pattern.compile(head + type + middle + type + tail);
            viewQuiz2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "create_post";
            createPost = Pattern.compile(head + type + middle + type + tail);
            createPost2 = Pattern.compile(head + type + middle2 + type + tail);

            type = "create_survey";
            createPoll = Pattern.compile(head + type + middle + type + tail);
            createPoll2 = Pattern.compile(head + type + middle2 + type + tail);

            catchAllUnsupported = Pattern.compile(anchorStart + "href[\\s]*=[\\s]*\"javascript:;\"[^>]*>");

            anchorClosePattern = Pattern.compile("<[\\s]*/[\\s]*a[\\s]*>");

            postPatterns = new Pattern[]{
                    downloadAttachment,
                    downloadAttachment2,
                    viewPost,
                    viewPoll,
                    viewEvent,
                    viewQuiz,
                    viewPost2,
                    viewPoll2,
                    viewEvent2,
                    viewQuiz2
            };

            unsupportedPatterns = new Pattern[]{
                    catchAllUnsupported
            };

            allPatterns = new Pattern[][]{
                    postPatterns,
                    unsupportedPatterns
            };
        }
    }

}
