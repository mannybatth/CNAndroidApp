package com.thecn.app.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.adapters.ThumbnailAdapters.GalleryThumbnailAdapter;
import com.thecn.app.fragments.MyFragments.MyFragment;
import com.thecn.app.tools.ServiceChecker;

import java.util.ArrayList;
import java.util.Arrays;

public class GalleryFragment extends MyFragment {
    private static final String TAG = GalleryFragment.class.getSimpleName();

    private GalleryThumbnailAdapter galleryThumbnailAdapter;
    private ArrayList<String> postImageFilePaths;//on creation, set to
    //an intent extra.  On pause, set to currently checked image file paths

    private void getImagesFromExternalStorage() {
        ContentResolver contentResolver = getActivity().getContentResolver();

        final String[] columns = {Media.DATA, Media._ID};

        Cursor imageCursor = contentResolver.query(
                Media.EXTERNAL_CONTENT_URI, columns, null, null, Media.DATE_ADDED + " DESC");

        if (postImageFilePaths == null)
            postImageFilePaths = getActivity().getIntent().getStringArrayListExtra("FILE_PATHS");

        int count = imageCursor.getCount();

        for (int i = 0; i < count; i++) {

            imageCursor.moveToPosition(i);

            int dataColumnIndex = imageCursor.getColumnIndex(Media.DATA);

            String path = imageCursor.getString(dataColumnIndex);

            boolean checked = false;

            for (String addedFilePath : postImageFilePaths)
                if (path.equals(addedFilePath)) {
                    checked = true;
                    break;
                }

            galleryThumbnailAdapter.add(path, checked);
        }
        imageCursor.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        Button resultButton = (Button) view.findViewById(R.id.return_button);
        resultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnWithResult();
            }
        });

        galleryThumbnailAdapter = new GalleryThumbnailAdapter(this);

        TextView selectedDisplay = (TextView) view.findViewById(R.id.selected_text);
        galleryThumbnailAdapter.setCheckedItemDisplay(selectedDisplay);

        Button deselectAllButton = (Button) view.findViewById(R.id.deselect_all_button);
        deselectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryThumbnailAdapter.uncheckAll();
            }
        });

        GridView imageGrid = (GridView) view.findViewById(R.id.IMAGE_GRID);

        int orientation = getActivity().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            imageGrid.setNumColumns(5);
        else imageGrid.setNumColumns(3);

        imageGrid.setAdapter(galleryThumbnailAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        galleryThumbnailAdapter.openDiskCacheIfClosed();
        if (ServiceChecker.isStorageAvailable()) getImagesFromExternalStorage();
        galleryThumbnailAdapter.setCheckedItemDisplayText();
    }

    @Override
    public void onPause() {
        super.onPause();

        String[] filePaths = galleryThumbnailAdapter.getCheckedItemFilePaths();
        postImageFilePaths = new ArrayList<String>(Arrays.asList(filePaths));
    }

    private void returnWithResult() {
        Intent returnIntent = new Intent();

        ArrayList<String> filePaths = new ArrayList<String>(
                Arrays.asList(galleryThumbnailAdapter.getCheckedItemFilePaths()));
        returnIntent.putStringArrayListExtra("FILE_PATHS", filePaths);

        Activity activity = getActivity();
        activity.setResult(Activity.RESULT_OK, returnIntent);
        activity.finish();
    }
}
