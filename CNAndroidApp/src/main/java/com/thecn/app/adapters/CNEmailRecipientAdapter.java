package com.thecn.app.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.DropdownChipLayouter;
import com.android.ex.chips.RecipientEntry;
import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.models.User.User;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.SearchStore;
import com.thecn.app.tools.MyVolley;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by philjay on 6/13/14.
 */
public class CNEmailRecipientAdapter extends BaseRecipientAdapter {

    private volatile long lastRequestTime = 0;
    private Timer mRequestTimer = new Timer();
    private final Object mRequestTimerLock = new Object();

    private SearchRequestTask mRequestTask;
    private final Object mRequestTaskLock = new Object();

    private volatile String mSearchKeywords;

    private volatile boolean mWaitingForReply = false;

    /**
     * A timer task that makes a search request using the text content
     */
    private class SearchRequestTask extends TimerTask {
        @Override
        public void run() {
            synchronized (mRequestTaskLock) {
                if (mSearchKeywords != null) {
                    //always uses the current search keyword
                    makeSearchRequest();
                    mRequestTask = null;
                }
            }
        }
    }

    public CNEmailRecipientAdapter(Context context) {
        super(context);
    }

    public void performFiltering() {
        if (mSearchKeywords != null) {
            new Thread(new FilterRunnable(mSearchKeywords)).start();
        }
    }

    public void performFiltering(String filterText) {
        new Thread(new FilterRunnable(filterText)).start();
    }

    public interface SearchingCallbacks {
        public void onSearchStart();
        public void onSearchComplete();
        public void onSearchResultsSubmitted();
        public void onSearchingCancelled();
    }

    private SearchingCallbacks mSearchingCallbacks;

    public void registerSearchingCallbacks(SearchingCallbacks searchingCallbacks) {
        mSearchingCallbacks = searchingCallbacks;
    }

    public void removeSearchCallbacks() {
        mSearchingCallbacks = null;
    }

    private class FilterRunnable implements Runnable {

        private String mFilterText;

        public FilterRunnable(String filterText) {
            mFilterText = filterText;
        }

        @Override
        public void run() {

            mSearchKeywords = mFilterText;

            if (mSearchKeywords.length() > 0) {
                int lastPos = mSearchKeywords.length() - 1;
                char charAt = mSearchKeywords.charAt(lastPos);
                if (charAt == ',' || charAt == ';') {
                    mSearchKeywords = mSearchKeywords.substring(0, lastPos);
                }
            }

            mSearchKeywords =
                    mSearchKeywords
                    .replaceAll("[^,;]*[,;]\\s?", "").replaceAll("[,;]", "").trim().toLowerCase();

            if (mSearchKeywords.length() > 0) {
                char charAt = mSearchKeywords.charAt(0);
                if (charAt == '@') {
                    mSearchKeywords = mSearchKeywords.substring(1);
                }
            }

            //search keywords must be at least three characters
            if (mSearchKeywords.length() > 2 && !mSearchKeywords.contains("@")) {

                if (mEntriesSearchKeywords != null) {
                    //if the last search does not equal this search, then do a search
                    if (!mSearchKeywords.equals(mEntriesSearchKeywords)) {
                        search();
                    } else {
                        //if the last search does equal this search, show the last results
                        if (mSearchingCallbacks != null) mSearchingCallbacks.onSearchResultsSubmitted();
                    }
                } else {
                    //if no results have been collected, search for first time
                    search();
                }

            } else {
                if (mSearchingCallbacks != null) mSearchingCallbacks.onSearchingCancelled();

                synchronized (mRequestTaskLock) {
                    if (mRequestTask != null) {
                        mRequestTask.cancel();
                        mRequestTask = null;
                    }
                }

                synchronized (mRequestTimerLock) {
                    mRequestTimer.purge();
                }
            }
        }

        private void search() {

            if (mSearchingCallbacks != null) mSearchingCallbacks.onSearchStart();

            synchronized (mRequestTaskLock) {
                //if there is no request scheduled, check when next request should be made
                if (mRequestTask == null) {
                    long timeTillNextRequest = getTimeTillNextRequest();

                    //If next request should be made now, do it now.
                    //Otherwise, schedule a task to perform the request once
                    //the time has passed
                    if (timeTillNextRequest <= 0) {
                        makeSearchRequest();
                    } else {
                        mRequestTask = new SearchRequestTask();

                        synchronized (mRequestTimerLock) {
                            mRequestTimer.schedule(mRequestTask, timeTillNextRequest);
                        }
                    }
                }
            }
        }
    }

    //requests are made no less than 1 second apart (1000ms)
    private long getTimeTillNextRequest() {
        return 1000 - (SystemClock.elapsedRealtime() - lastRequestTime);
    }

    /**
     * Sets a timestamp and makes a request
     */
    private void makeSearchRequest() {
        lastRequestTime = SystemClock.elapsedRealtime();
        notifyWaitingForReply(true);

        final String textContent = mSearchKeywords;

        SearchStore.userSearchByKeyword(textContent, 10, new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if (mSearchingCallbacks != null) mSearchingCallbacks.onSearchComplete();

                ArrayList<User> users = SearchStore.getUserListData(response);

                if (users != null) {
                    //If the current search keyword does not equal what we searched for,
                    //do not update the entries
                    if (wasLastSearch(textContent)) {
                        updateEntries(CNRecipientEntry.constructEntriesFromUsers(users));
                        if (mSearchingCallbacks != null)
                            mSearchingCallbacks.onSearchResultsSubmitted();
                    }
                }
            }

            @Override
            public void onPostExecute() {
                notifyWaitingForReply(false);
            }
        });
    }

    //not waiting for reply only if there is no pending task
    private void notifyWaitingForReply(boolean waiting) {
        synchronized (mRequestTaskLock) {
            mWaitingForReply = waiting || mRequestTask != null;
        }
    }

    public boolean wasLastSearch(String text) {
        return mSearchKeywords.equals(text);
    }

    //Keeps track of the search keywords that were used to find the currently displayed entries
    //in the drop down list.
    private volatile String mEntriesSearchKeywords;

    @Override
    public void updateEntries(List<RecipientEntry> entries) {
        super.updateEntries(entries);
        mEntriesSearchKeywords = mSearchKeywords;
    }

    public void setEntries(List<RecipientEntry> entries) {
        mEntries = entries;
    }

    public ArrayList<RecipientEntry> getEntries() {
        ArrayList<RecipientEntry> retVal = null;

        if (mEntries instanceof ArrayList) {
            try {
                retVal = (ArrayList<RecipientEntry>) mEntries;
            } catch(ClassCastException e) {
                retVal = null;
            }
        }

        return retVal;
    }

    public String getEntriesSearchKeywords() {
        return mEntriesSearchKeywords;
    }

    public void setEntriesSearchKeywords(String keywords) {
        mEntriesSearchKeywords = keywords;
    }

    public void setSearchKeywords(String keywords) {
        mSearchKeywords = keywords;
    }

    public String getSearchKeywords() {
        return mSearchKeywords;
    }

    public boolean wereEntriesFoundBy(String text) {
        return mEntriesSearchKeywords != null && mEntriesSearchKeywords.equals(text);
    }

    public boolean isWaitingForReply() {
        return mWaitingForReply;
    }

    /**
     * Extension of RecipientEntry class to allow for association of a User object
     */
    public static class CNRecipientEntry extends RecipientEntry implements Serializable {
        protected CNRecipientEntry(int entryType, String displayName, String destination,
                               int destinationType, String destinationLabel, long contactId, Long directoryId,
                               long dataId, Uri photoThumbnailUri, boolean isFirstLevel, boolean isValid,
                               String lookupKey, User user) {

            super(entryType, displayName, destination, destinationType, destinationLabel,
                  contactId, directoryId, dataId, photoThumbnailUri, isFirstLevel, isValid, lookupKey);
            mUser = user;
        }

        private User mUser;
        private transient Bitmap mIconBitmap;

        public static CNRecipientEntry constructEntryFromUser(User user) {
            if (user != null) {
                String displayName = user.getDisplayName();
                String cnNumber = user.getCNNumber();
                cnNumber = cnNumber != null ? cnNumber : "";

                if (displayName != null) {
                    return new CNRecipientEntry(ENTRY_TYPE_PERSON, displayName,
                            cnNumber, ContactsContract.CommonDataKinds.Email.TYPE_OTHER,
                            "Email", 0, null, 0, null, true, true, null, user);
                }
            }

            return null;
        }

        public static List<RecipientEntry> constructEntriesFromUsers(List<User> users) {
            List<RecipientEntry> entries = new ArrayList<RecipientEntry>();

            for (User user : users) {
                RecipientEntry entry = constructEntryFromUser(user);
                if (entry != null) {
                    entries.add(entry);
                }
            }

            return entries;
        }

        public User getUser() {
            return mUser;
        }

        public void setIconBitmap(Bitmap bitmap) {
            mIconBitmap = bitmap;
        }

        public Bitmap getIconBitmap() {
            return mIconBitmap;
        }

    }

    public void clear() {
        updateEntries(new ArrayList<RecipientEntry>());
    }

    public static class CNDropdownChipLayouter extends DropdownChipLayouter {

        private ImageLoader mImageLoader = MyVolley.getImageLoader();

        public CNDropdownChipLayouter(LayoutInflater inflater, Context context) {
            super(inflater, context);
        }

        /**
         * Binds the avatar icon to the image view. If we don't want to show the image, hides the
         * image view.
         */
        @Override
        protected void bindIconToView(boolean showImage, RecipientEntry entry, ImageView view,
                                      AdapterType type, int position) {
            if (view == null) {
                return;
            }

            showImage = showImage && type == AdapterType.BASE_RECIPIENT && entry instanceof CNRecipientEntry;

            if (showImage) {

                view.setTag(position);

                final CNRecipientEntry cnEntry = (CNRecipientEntry) entry;

                int defaultPhotoID = getDefaultPhotoResId();
                view.setImageResource(defaultPhotoID);

                try {
                    String avatarUrl = cnEntry.getUser().getAvatar().getView_url() + ".w160.jpg";

                    mImageLoader.get(avatarUrl,
                            new MyVolley.IndexedImageListener(position, view, defaultPhotoID, defaultPhotoID) {
                                @Override
                                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                    super.onResponse(response, isImmediate);

                                    if (cnEntry != null && response.getBitmap() != null) {
                                        cnEntry.setIconBitmap(response.getBitmap());
                                    }
                                }
                            },
                            view.getWidth(), view.getHeight());

                    view.setVisibility(View.VISIBLE);

                } catch (NullPointerException e) {
                    view.setVisibility(View.GONE);
                }

            } else {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        protected void bindTextToView(CharSequence text, TextView view) {
            if (view == null) {
                return;
            }

            if (text != null && text.length() > 0) {
                view.setText(text);
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }
}
