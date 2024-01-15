package com.thecn.app.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.GalleryActivity;
import com.thecn.app.activities.PostVisibilityActivity;
import com.thecn.app.adapters.ThumbnailAdapters.PostThumbnailAdapter;
import com.thecn.app.adapters.VideoLinkAdapter;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.Post;
import com.thecn.app.models.PostingGroup;
import com.thecn.app.stores.BaseStore;
import com.thecn.app.stores.ImageStore;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.views.ExpandableGridView;
import com.thecn.app.views.ExpandableListView;
import com.thecn.app.tools.PostChangeHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class CreatePostFragment extends MyFragment {

    private static final int POST_VISIBILITY_REQUEST = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    ExpandableGridView imageGrid;
    PostThumbnailAdapter postThumbnailAdapter;
    Uri currentImageUri;

    ExpandableListView videoList;
    VideoLinkAdapter videoLinkAdapter;//only uses youtube video links

    Activity mActivity;

    private PostWithImagesTask postTask;

    private String text;
    private ArrayList<PostingGroup> mVisibleGroups;
    private ArrayList<PostingGroup> mInvisibleGroups;
    private ArrayList<Course> mCourses;
    private ArrayList<Conexus> mConexuses;
    private String[] imageIDs, youtubeLinks;

    Button visibilityButton;
    Button removeAllPhotosButton;
    Button removeAllVideosButton;
    Button addVideoLinkButton;

    ImageButton submitPost;

    EditText videoLinkText;
    EditText postText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVisibleGroups = new ArrayList<PostingGroup>();
        mInvisibleGroups = new ArrayList<PostingGroup>();
        mCourses = new ArrayList<Course>();
        mConexuses = new ArrayList<Conexus>();
        getDataFromIntent();

        imageIDs = new String[]{};
        youtubeLinks = new String[]{};

        postThumbnailAdapter = new PostThumbnailAdapter(this);
        if(videoLinkAdapter == null)
            videoLinkAdapter = new VideoLinkAdapter(this);
    }

    private void getDataFromIntent() {
        Intent intent = getActivity().getIntent();

        if (intent != null) {
            Course course = (Course) intent.getSerializableExtra("COURSE");
            Conexus conexus = (Conexus) intent.getSerializableExtra("CONEXUS");

            if (course != null) {
                mCourses.add(course);
                mInvisibleGroups.add(PostingGroup.course);
            }
            else if (conexus != null) {
                mConexuses.add(conexus);
                mInvisibleGroups.add(PostingGroup.conexus);
            }
            else mVisibleGroups.add(PostingGroup.allMembers);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);

        mActivity = getActivity();

        postText = (EditText) view.findViewById(R.id.post_text);

        submitPost = (ImageButton) view.findViewById(R.id.post_button);
        submitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmitPostClicked();
            }
        });

        ImageButton backButton = (ImageButton) view.findViewById(R.id.cancel_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });

        visibilityButton = (Button) view.findViewById(R.id.visibility_button);
        visibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushPostVisibilityActivity();
            }
        });
        setVisibilityButtonText();

        ImageButton cameraButton = (ImageButton) view.findViewById(R.id.add_picture);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushTakePhotoActivity();
            }
        });

        Button galleryButton = (Button) view.findViewById(R.id.add_from_gallery);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushGalleryActivity();
            }
        });

        removeAllPhotosButton = (Button) view.findViewById(R.id.remove_photos_button);
        removeAllPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postThumbnailAdapter.removeAll();
            }
        });
        postThumbnailAdapter.setRemoveAllPhotosButton(removeAllPhotosButton);

        removeAllVideosButton = (Button) view.findViewById(R.id.remove_videos_button);
        removeAllVideosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoLinkAdapter.removeAll();
            }
        });
        videoLinkAdapter.setRemoveAllVideosButton(removeAllVideosButton);

        videoLinkText = (EditText) view.findViewById(R.id.add_videos_text);
        videoLinkAdapter.setLinkEditText(videoLinkText);

        addVideoLinkButton = (Button) view.findViewById(R.id.add_videos_button);
        addVideoLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String linkText = videoLinkText.getText().toString();
                if (linkText.length() > 0)
                    videoLinkAdapter.add(linkText);
            }
        });

        imageGrid = (ExpandableGridView) view.findViewById(R.id.image_thumbnail_view);
        int orientation = mActivity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            imageGrid.setNumColumns(4);
        else imageGrid.setNumColumns(2);

        videoList = (ExpandableListView) view.findViewById(R.id.video_link_view);

        postThumbnailAdapter.setButtonEnabled();
        videoLinkAdapter.setButtonEnabled();

        imageGrid.setAdapter(postThumbnailAdapter);
        videoList.setAdapter(videoLinkAdapter);

        return view;
    }

    private void onSubmitPostClicked() {
        submitPost.setEnabled(false);
        if (BaseStore.isOnline(mActivity)) {
            text = postText.getText().toString();

            youtubeLinks = videoLinkAdapter.getAllItems();

            if (text != null && text.length() > 0) {
                if (postThumbnailAdapter.getCount() > 0) {
                    String[] paths = postThumbnailAdapter.getFilePaths();
                    postTask = (PostWithImagesTask) new PostWithImagesTask().execute(paths);
                } else {
                    makePost();
                }
            } else {
                AppSession.showToast("Post text cannot be blank");
                submitPost.setEnabled(true);
            }
        } else {
            AppSession.showToast("No internet connection.  Try again later.");
            submitPost.setEnabled(true);
        }
    }

    private void makePost() {
        final ProgressDialog postProgressDialog = new ProgressDialog(mActivity);
        postProgressDialog.setCancelable(false);
        postProgressDialog.setMessage("Uploading Post...");

        String[] courseIDs = Course.getIds(mCourses);
        String[] conexusIDs = Conexus.getIds(mConexuses);
        String[] groupIDs = new String[mVisibleGroups.size() + mInvisibleGroups.size()];

        String[] visibleGroupIDs = PostingGroup.getIds(mVisibleGroups);
        String[] invisibleGroupIDs = PostingGroup.getIds(mInvisibleGroups);

        copyInto(visibleGroupIDs, groupIDs, 0);
        copyInto(invisibleGroupIDs, groupIDs, visibleGroupIDs.length);

        ResponseCallback callback = new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {

                Post post = PostStore.getData(response);

                if (post != null) {
                    AppSession.showToast("Post submitted");
                    PostChangeHandler.sendAddedBroadcast(post);
                } else {
                    AppSession.showToast("Post submitted, but could not get post data...");
                }
            }

            @Override
            public void onFailure(JSONObject response) {
                AppSession.showToast("Error submitting post");
            }

            @Override
            public void onError(Exception error) {
                StoreUtil.showExceptionMessage(error);
            }

            @Override
            public void executeWithHandler(JSONObject response) {
                submitPost.setEnabled(true);
                postProgressDialog.dismiss();

                if (getExecutionType() == ExecutionType.SUCCESS) {
                    mActivity.finish();
                }
            }
        };
        callback.setHandler(getHandler(), ResponseCallback.HANDLE_NONE);

        postProgressDialog.show();
        PostStore.makePost(text,
                courseIDs,
                conexusIDs,
                groupIDs,
                imageIDs,
                youtubeLinks,
                callback
        );
    }

    private void copyInto(String[] source, String[] dest, int startIndex) {
        int i = 0;
        int j = startIndex;

        while (j < dest.length && i < source.length) {
            dest[j] = source[i];

            i++;
            j++;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        postThumbnailAdapter.openDiskCacheIfClosed();
    }

    private void pushPostVisibilityActivity() {
        Intent intent = new Intent(mActivity, PostVisibilityActivity.class);
        intent.putExtra("GROUPS", mVisibleGroups);
        intent.putExtra("COURSES", mCourses);
        intent.putExtra("CONEXUSES", mConexuses);
        startActivityForResult(intent, POST_VISIBILITY_REQUEST);
    }

    private void pushGalleryActivity() {
        Intent intent = new Intent(mActivity, GalleryActivity.class);
        ArrayList<String> filePathList = new ArrayList<String>(Arrays.asList(postThumbnailAdapter.getFilePaths()));
        intent.putStringArrayListExtra("FILE_PATHS", filePathList);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void pushTakePhotoActivity() {
        if (postThumbnailAdapter.getCount() < 15) {
            PackageManager pkgManager = mActivity.getPackageManager();

            if (pkgManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(pkgManager) != null) {
                    currentImageUri = getTimestampUri();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            } else {
                AppSession.showToast("Unable to use the camera.  Permission must be granted.");
            }
        } else {
            AppSession.showToast("Cannot add more than 15 photos");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == POST_VISIBILITY_REQUEST) {
                mVisibleGroups = (ArrayList<PostingGroup>) data.getSerializableExtra("V_GROUPS");
                mInvisibleGroups = (ArrayList<PostingGroup>) data.getSerializableExtra("INV_GROUPS");
                mCourses = (ArrayList<Course>) data.getSerializableExtra("COURSES");
                mConexuses = (ArrayList<Conexus>) data.getSerializableExtra("CONEXUSES");

                setVisibilityButtonText();

            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                addImageFromCurrentUri();
                postThumbnailAdapter.setButtonEnabled();
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                postThumbnailAdapter.removeAll();
                ArrayList<String> paths = data.getStringArrayListExtra("FILE_PATHS");

                for (String path : paths) postThumbnailAdapter.add(path);
                postThumbnailAdapter.setButtonEnabled();
            }
        }
    }

    private void setVisibilityButtonText() {
        String text = "";

        boolean groupExists = mVisibleGroups.size() > 0;
        boolean onlyOneCourseOrConexus = false;

        if (!groupExists) {

            if (mCourses.size() == 1 && mConexuses.size() == 0) {

                text = mCourses.get(0).getName();
                onlyOneCourseOrConexus = true;

            } else if (mConexuses.size() == 1 && mCourses.size() == 0) {

                text = mConexuses.get(0).getName();
                onlyOneCourseOrConexus = true;

            }
        }

        if (!onlyOneCourseOrConexus) {
            int groupNameLastPos = mVisibleGroups.size() - 1;

            for (int i = 0; i < groupNameLastPos; i++)
                text += mVisibleGroups.get(i).getName() + ", ";

            if (groupExists) text += mVisibleGroups.get(groupNameLastPos).getName();

            int numCourses = mCourses.size();
            boolean courseExists = mCourses.size() > 0;

            if (courseExists) {
                if (groupExists) {
                    if (mVisibleGroups.size() > 1) {
                        text += ",\n";
                    } else {
                        text += ", ";
                    }
                }

                text += Integer.toString(numCourses) + " Course";
                if (numCourses > 1) text += "s";
            }

            int numConexuses = mConexuses.size();

            if (mConexuses.size() > 0) {
                if (groupExists || courseExists) text += ", ";

                text += Integer.toString(numConexuses) + " Conexus";
                if (numConexuses > 1) text += "es";
            }
        }

        visibilityButton.setText(text);
    }

    private void addImageFromCurrentUri() {
        String filePath = getPath(currentImageUri);
        if (filePath != null) {
            postThumbnailAdapter.add(filePath);
        }
    }

    private String getPath(Uri uri) {
        Cursor cursor = mActivity.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }

        return null;
    }

    private Uri getTimestampUri() {
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        String imageFileName = "THECN_IMG_CAPTURE_" + timeStamp + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, imageFileName);

        return mActivity
                .getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private class PostWithImagesTask extends AsyncTask<String, Integer, Void> {
        private final String TAG = PostWithImagesTask.class.getSimpleName();

        private ArrayList<String> mImageIDs;
        private int numImages;
        private boolean error;

        ProgressDialog imageProgressDialog;

        @Override
        protected void onPreExecute() {
            imageProgressDialog = new ProgressDialog(mActivity) {
                @Override
                public void onBackPressed() {
                    super.onBackPressed();
                    if (postTask != null) {
                        postTask.cancel(true);
                        submitPost.setEnabled(true);
                        AppSession.showToast("Uploads cancelled");
                        dismiss();
                    }
                }
            };
            imageProgressDialog.setCancelable(false);
            imageProgressDialog.setMessage("Uploading Images...");
            imageProgressDialog.show();

            numImages = postThumbnailAdapter.getCount();
            error = false;
        }

        @Override
        protected Void doInBackground(String... params) {
            mImageIDs = new ArrayList<String>();

            try {
                for (int i = 0; i < params.length; i++) {
                    if (isCancelled()) {
                        break;
                    } else {
                        publishProgress(i);

                        final String filePath = params[i];

                        ImageStore.uploadImage(filePath, new ImageStore.ImageCallback() {
                            @Override
                            public void response(JSONObject json) {
                                try {
                                    mImageIDs.add(json.getJSONObject("data").getString("id"));
                                } catch (JSONException e) {
                                    cancel(true);
                                    error = true;
                                }
                            }

                            @Override
                            public void error(Exception e) {
                                Log.d(TAG, e.getMessage());
                                cancel(true);
                                error = true;
                            }
                        });
                    }
                }
            } catch (Exception e) {
                // something went wrong
            }

            publishProgress(params.length);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            String num = (progress[0] + 1 > numImages) ? Integer.toString(numImages) : Integer.toString(progress[0] + 1);

            super.onProgressUpdate(progress[0]);
            setImageProcessDialogMessage(num, Integer.toString(numImages));

        }

        @Override
        protected void onPostExecute(Void result) {

            imageProgressDialog.dismiss();

            if (error) {
                AppSession.showToast("Error uploading images");
                submitPost.setEnabled(true);
            } else {
                imageIDs = mImageIDs.toArray(new String[mImageIDs.size()]);
                makePost();
            }
        }

        private void setImageProcessDialogMessage(String num1, String num2) {
            imageProgressDialog.setMessage("Uploading images... " + num1 + "/" + num2);
        }

        @Override
        protected void onCancelled(Void result) {
            submitPost.setEnabled(true);
            if (error)
                AppSession.showToast("Error uploading images.");
            imageProgressDialog.dismiss();
        }
    }
}