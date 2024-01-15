package com.thecn.app.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.adapters.ReflectionsAdapter;
import com.thecn.app.fragments.MyFragments.MyListFragment;
import com.thecn.app.models.Post;
import com.thecn.app.models.Reflection;
import com.thecn.app.stores.ReflectionStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CNNumberLinker;
import com.thecn.app.tools.LoadingViewController;
import com.thecn.app.tools.PostChangeHandler;
import com.thecn.app.tools.PostViewController;

import org.json.JSONObject;

import java.util.ArrayList;

public class PostFragment extends MyListFragment implements PostChangeHandler.Listener {

    public static final String TAG = PostFragment.class.getSimpleName();
    static final String FRAGMENT_BUNDLE_POST_KEY = "post";

    ReflectionsAdapter mReflectionsAdapter;
    View headerView;
    LoadingViewController mFooter;

    Post mPost;
    PostViewController postViewController;

    int limit;
    int offset;
    boolean loading;
    boolean noMore;

    boolean hasReceivedData;

    TextView contentTextView;
    EditText reflectionText;
    RelativeLayout showReflectionsRelativeLayout;
    TextView showReflectionsTextView;
    Button sendReflection;
    ProgressBar showReflectionsProgressBar;

    boolean textFocus;

    public static PostFragment newInstance(Post mPost, boolean textFocus) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_POST_KEY, mPost);
        args.putBoolean("TEXT_FOCUS", textFocus);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        limit = 10;
        offset = 0;
        loading = false;
        noMore = false;

        hasReceivedData = false;

        mPost = (Post) getArguments().getSerializable(FRAGMENT_BUNDLE_POST_KEY);
        mPost.setFullView(true);
        mPost.processData();
        mReflectionsAdapter = new ReflectionsAdapter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        headerView = inflater.inflate(R.layout.post_reflections_header, null, false);
        return inflater.inflate(R.layout.fragment_post_reflections, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postViewController = new PostViewController(this, headerView);
        postViewController.setFullView(true);
        postViewController.setUpView(mPost);
        postViewController.registerDeleteCallback(new PostViewController.DeleteCallback() {
            @Override
            public void onRequest() {
                postViewController.allowInteraction(false);
                sendReflection.setEnabled(false);
            }

            @Override
            public void onResponse() {
                postViewController.allowInteraction(true);
                sendReflection.setEnabled(true);
            }

            @Override
            public void onConfirm() {
                if (isAdded()) {
                    getActivity().finish();
                }
            }
        });

        showReflectionsRelativeLayout = (RelativeLayout)headerView.findViewById(R.id.showReflectionsRelativeLayout);
        showReflectionsTextView = (TextView)headerView.findViewById(R.id.showReflectionsTextView);
        showReflectionsProgressBar = (ProgressBar)headerView.findViewById(R.id.showReflectionsProgressBar);

        showReflectionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getReflections();
            }
        });

        textFocus = getArguments().getBoolean("TEXT_FOCUS");

        contentTextView = (TextView) headerView.findViewById(R.id.post_content);
        contentTextView.setMaxLines(Integer.MAX_VALUE);
        contentTextView.setEllipsize(null);

        reflectionText = (EditText) view.findViewById(R.id.submit_reflection_text);
        if (textFocus) {
            focusReflectionTextBox();
        }

        setListAdapter(null);

        ListView listView = getListView();
        listView.setDivider(null);
        listView.addHeaderView(headerView, null, false);
        mFooter = new LoadingViewController(listView, getLayoutInflater(savedInstanceState));
        mFooter.setNoneMessage("No one has made a reflection yet.");
        listView.setFooterDividersEnabled(false);
        listView.setBackgroundColor(getResources().getColor(R.color.base_listview_background_color));

        setListAdapter(mReflectionsAdapter);

        sendReflection = (Button) view.findViewById(R.id.submit_reflection);
        sendReflection.setOnClickListener(mReflectionButtonClickListener);
    }

    private View.OnClickListener mReflectionButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String text = reflectionText.getText().toString();
            if (text.length() > 0) {
                sendReflection.setEnabled(false);
                ResponseCallback callback = new ResponseCallback() {
                    @Override
                    public void onSuccess(final JSONObject response) {
                        Reflection reflection = ReflectionStore.getData(response);

                        if (reflection != null) {
                            //sendReflection button will be enabled after this task completes
                            new ProcessAddReflectionTask(reflection).execute();
                        }
                    }

                    @Override
                    public void onFailure(JSONObject response) {
                        AppSession.showToast("Could not post reflection to server");
                    }

                    @Override
                    public void onError(Exception error) {
                        StoreUtil.showExceptionMessage(error);
                    }

                    @Override
                    public void executeWithHandler(JSONObject response) {
                        ExecutionType type = getExecutionType();
                        if (type == ExecutionType.FAILURE || type == ExecutionType.ERROR) {
                            sendReflection.setEnabled(true);
                        }
                    }
                };
                //update UI before showing successful submission message
                callback.setHandler(getHandler(), (ResponseCallback.HANDLE_SUCCESS));

                ReflectionStore.makeReflection(mPost, text, callback);
            }
        }
    };

    private class ProcessAddReflectionTask extends AsyncTask<Void, Void, Void> {

        private Reflection mReflection;

        public ProcessAddReflectionTask(Reflection reflection) {
            mReflection = reflection;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mReflection == null) return null;

            CNNumberLinker numberLinker = new CNNumberLinker();
            String text = mReflection.getText();
            if (text != null) {
                CharSequence processedText = numberLinker.linkify(text);
                mReflection.setProcessedText(processedText);
            } else {
                mReflection = null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            final boolean error = mReflection == null;
            if (error) {
                AppSession.showToast("Error sending reflection.");
            }

            getHandler().postWhenResumed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(reflectionText.getWindowToken(), 0);

                    if (!error) {
                        offset++;
                        mReflectionsAdapter.add(mReflection);
                        getListView().setSelection(getListView().getCount() - 1);
                        reflectionText.setText("");
                        mLoadingCallback.onLoadingComplete();

                        mPost.getCount().setReflections(mPost.getCount().getReflections() + 1);
                        postViewController.setInteractNumText();
                        PostChangeHandler.sendUpdatedBroadcast(mPost);

                        AppSession.showToast("Reflection submitted");
                    }

                    sendReflection.setEnabled(true);
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!hasReceivedData) {
            getReflectionsFirstTime();
        } else if (mReflectionsAdapter.getCount() == 0) {
            mFooter.showNoneMessage();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //TODO
    }

    @Override
    public void onPostAdded(Post post, String[] ids) {}

    @Override
    public void onPostUpdated(Post post) {
        if (mPost.getId().equals(post.getId())) {
            postViewController.setUpView(post);
        }
    }

   @Override
    public void onPostDeleted(Post post) {
        if (post.getId().equals(post.getId())) {
            getActivity().finish();
        }
    }

    public void getReflectionsFirstTime() {
        mFooter.setLoading();

        mLoadingCallback = new LoadingCallback() {
            @Override
            public void onLoadingComplete() {
                loading = false;
                hasReceivedData = true;

                if (mReflectionsAdapter.getCount() == 0) {
                    mFooter.showNoneMessage();
                } else mFooter.clear();

                setHiddenReflectionLoader(true);

                mLoadingCallback = new LoadingCallback() {
                    @Override
                    public void onLoadingComplete() {
                        loading = false;

                        mFooter.clear();

                        setHiddenReflectionLoader(true);
                    }
                };
            }
        };

        getReflections();
    }

    private class ProcessReflectionsTask extends AsyncTask<Void, Void, Void> {

        private ArrayList<Reflection> mReflections;
        private boolean noReflections = false;

        public ProcessReflectionsTask(ArrayList<Reflection> reflections) {
            mReflections = reflections;

            noReflections = mReflections == null || mReflections.size() == 0;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (noReflections) {
                return null;
            }

            CNNumberLinker numberLinker = new CNNumberLinker();

            for (Reflection reflection : mReflections) {
                try {
                    String text = reflection.getText();
                    CharSequence processedText = numberLinker.linkify(text);
                    reflection.setProcessedText(processedText);
                } catch (NullPointerException e) {
                    //do nothing
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (isAdded()) {
                if (!noReflections) {
                    mReflectionsAdapter.addAll(mReflections);
                }
                mLoadingCallback.onLoadingComplete();
            }
        }
    }

    private void onNoMoreReflections() {
        noMore = true;
        showReflectionsRelativeLayout.setVisibility(View.GONE);
    }

    public void getReflections() {
        if (!loading && !noMore) {
            loading = true;

            ReflectionStore.getPostReflections(mPost.getId(), limit, offset, new ResponseCallback(getHandler()) {
                @Override
                public void onSuccess(JSONObject response) {
                    ArrayList<Reflection> reflections = ReflectionStore.getListData(response);

                    if (reflections != null) {
                        if (mReflectionsAdapter.getCount() == 0)
                            showReflectionsRelativeLayout.setVisibility(View.VISIBLE);
                        if (reflections.size() < limit) {
                            onNoMoreReflections();
                        }

                        int nextOffset = StoreUtil.getNextOffset(response);
                        if (nextOffset != -1) offset = nextOffset;
                    } else {
                        onNoMoreReflections();
                    }

                    new ProcessReflectionsTask(reflections).execute();
                }

                @Override
                public void onFailure(JSONObject response) {
                    AppSession.showToast("Could not load reflection data.");
                    mLoadingCallback.onLoadingComplete();
                }

                @Override
                public void onError(Exception error) {
                    StoreUtil.showExceptionMessage(error);
                    mLoadingCallback.onLoadingComplete();
                }
            });
        } else {
            mLoadingCallback.onLoadingComplete();
        }
    }

    private LoadingCallback mLoadingCallback;

    private interface LoadingCallback {
        public void onLoadingComplete();
    }

    public void setHiddenReflectionLoader(boolean hidden) {
        if (hidden) {
            showReflectionsProgressBar.setVisibility(View.GONE);
            showReflectionsTextView.setVisibility(View.VISIBLE);
        } else {
            showReflectionsProgressBar.setVisibility(View.VISIBLE);
            showReflectionsTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void focusReflectionTextBox() {
        reflectionText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(reflectionText, InputMethodManager.SHOW_IMPLICIT);
    }
}
