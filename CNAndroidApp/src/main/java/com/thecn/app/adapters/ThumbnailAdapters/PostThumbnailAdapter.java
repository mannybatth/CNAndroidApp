package com.thecn.app.adapters.ThumbnailAdapters;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.thecn.app.R;
import com.thecn.app.views.SquareView;

import java.util.ArrayList;

public class PostThumbnailAdapter extends BaseThumbnailAdapter {

    private static final String TAG = PostThumbnailAdapter.class.getSimpleName();

    private ArrayList<PostImagePackage> mImages;

    private Button removeAllPhotosButton;

    public class PostImagePackage extends BaseThumbnailAdapter.ImagePackage {
        private String filePath;

        public PostImagePackage(String filePath) {
            super(filePath);
        }
    }

    private static class ViewHolder {
        ImageView imageView;
        ImageView cancelView;
    }

    public PostThumbnailAdapter(Fragment fragment) {
        super(fragment);

        mImages = new ArrayList<PostImagePackage>();
    }

    public void setRemoveAllPhotosButton(Button button) {
        removeAllPhotosButton = button;
    }

    public void add(String filePath) {
        mImages.add(new PostImagePackage(filePath));
        setButtonEnabled();
        notifyDataSetChanged();
    }

    @Override
    public PostImagePackage getItem(int position) {
        return mImages.get(position);
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    public void remove(ImagePackage imagePackage) {
        if (imagePackage instanceof PostImagePackage) {
            PostImagePackage imgPkg = (PostImagePackage) imagePackage;
            remove(imgPkg);
        }
    }

    public void remove(PostImagePackage imgPkg) {
        mImages.remove(imgPkg);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        mImages.remove(position);
        setButtonEnabled();
        notifyDataSetChanged();
    }

    public void removeAll() {
        mImages.clear();
        setButtonEnabled();
        notifyDataSetChanged();
    }

    public String getFilePath(int index) {
        return mImages.get(index).getFilePath();
    }

    public String[] getFilePaths() {
        String[] filePaths = new String[getCount()];
        for (int i = 0; i < getCount(); i++) {
            filePaths[i] = mImages.get(i).getFilePath();
        }
        return filePaths;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {

            convertView = getLayoutInflater().inflate(R.layout.post_image_thumbnail, parent, false);

            holder = new ViewHolder();

            holder.imageView = (SquareView.SquareImageView) convertView.findViewById(R.id.image);
            holder.cancelView = (ImageView) convertView.findViewById(R.id.cancel);

            convertView.setTag(holder);
        } else holder = (ViewHolder) convertView.getTag();

        loadBitmap(getItem(position), holder.imageView);

        final int pos = position;

        holder.cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(pos);
            }
        });

        return convertView;
    }

    public void setButtonEnabled() {
        removeAllPhotosButton.setEnabled(getCount() > 0);
    }
}
