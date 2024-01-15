package com.thecn.app.adapters.ThumbnailAdapters;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.adapters.ThumbnailAdapters.BaseThumbnailAdapter;
import com.thecn.app.views.SquareView.SquareCheckbox;
import com.thecn.app.views.SquareView.SquareImageView;

import java.util.ArrayList;


public class GalleryThumbnailAdapter extends BaseThumbnailAdapter {

    private ArrayList<GalleryImagePackage> mImages;

    private TextView checkedItemDisplay;

    public class GalleryImagePackage extends BaseThumbnailAdapter.ImagePackage {
        private boolean checked;

        public GalleryImagePackage(String filePath, boolean checked) {
            super(filePath);
            setChecked(checked);
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public boolean toggle() {
            this.checked = !this.checked;
            return this.checked;
        }
    }

    static class ViewHolder {
        ImageView imageView;
        CheckBox checkbox;
    }

    public GalleryThumbnailAdapter(Fragment fragment) {
        super(fragment);

        mImages = new ArrayList<GalleryImagePackage>();
    }

    public void setCheckedItemDisplay(TextView view) {
        checkedItemDisplay = view;
    }

    public void add(String filePath, boolean checked) {
        mImages.add(new GalleryImagePackage(filePath, checked));
        notifyDataSetChanged();
    }

    @Override
    public GalleryImagePackage getItem(int position) {
        return mImages.get(position);
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    public int getNumCheckedItems() {
        int numCheckedItems = 0;
        for (GalleryImagePackage imgPkg : mImages)
            if (imgPkg.isChecked()) numCheckedItems++;

        return numCheckedItems;
    }

    public void uncheckAll() {
        for (GalleryImagePackage img : mImages)
            img.setChecked(false);
        notifyDataSetChanged();
        setCheckedItemDisplayText();
    }

    public void remove(ImagePackage imagePackage) {
        if (imagePackage instanceof GalleryImagePackage) {
            GalleryImagePackage imgPkg = (GalleryImagePackage) imagePackage;
            remove(imgPkg);
        }
    }

    public void remove(GalleryImagePackage imgPkg) {
        mImages.remove(imgPkg);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        mImages.remove(position);
        notifyDataSetChanged();
    }

    public void removeAll() {
        mImages.clear();
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

    public String[] getCheckedItemFilePaths() {
        ArrayList<String> filePaths = new ArrayList<String>();
        for (GalleryImagePackage imgPackage : mImages)
            if (imgPackage.isChecked())
                filePaths.add(imgPackage.getFilePath());

        return filePaths.toArray(new String[filePaths.size()]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = getLayoutInflater().inflate(R.layout.gallery_image_thumbnail, parent, false);

            holder.imageView = (SquareImageView) convertView.findViewById(R.id.image);
            holder.checkbox = (SquareCheckbox) convertView.findViewById(R.id.checkbox);

            convertView.setTag(holder);
        } else holder = (ViewHolder) convertView.getTag();

        final GalleryImagePackage mImagePackage = getItem(position);
        final CheckBox mCheckBox = holder.checkbox;

        loadBitmap(mImagePackage, holder.imageView);

        holder.checkbox.setChecked(getItem(position).isChecked());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mImagePackage.isChecked()) {
                    mCheckBox.setChecked(false);
                    mImagePackage.setChecked(false);
                } else if (getNumCheckedItems() < 15) {
                    mCheckBox.setChecked(true);
                    mImagePackage.setChecked(true);
                } else
                    AppSession.showToast("Cannot add more than 15 photos");
                setCheckedItemDisplayText();
            }
        });

        return convertView;
    }

    public void setCheckedItemDisplayText() {
        if (checkedItemDisplay != null) {
            String middle = " Photo";
            String end = " Selected";

            int numCheckedItems = getNumCheckedItems();
            if (numCheckedItems != 1)
                middle += "s";

            checkedItemDisplay.setText(Integer.toString(numCheckedItems)
                    + middle + end);
        }
    }
}
