package com.thecn.app.activities;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.R;
import com.thecn.app.activities.MyActivities.MyFragmentActivity;
import com.thecn.app.models.Picture;
import com.thecn.app.tools.MyVolley;

import java.util.ArrayList;

import uk.co.senab.photoview.HackyViewPager;
import uk.co.senab.photoview.PhotoView;

public class PhotoGalleryViewerActivity extends MyFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery_viewer);

        ArrayList<Picture> pics = (ArrayList<Picture>) getIntent().getSerializableExtra("pics");
        int currentIndex = getIntent().getIntExtra("currentIndex", 0);

        ViewPager mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        setContentView(mViewPager);

        mViewPager.setAdapter(new SamplePagerAdapter(pics));
        mViewPager.setCurrentItem(currentIndex);
    }

    static class SamplePagerAdapter extends PagerAdapter {

        private ArrayList<Picture> pics;
        ImageLoader imageLoader = MyVolley.getImageLoader();

        public SamplePagerAdapter(ArrayList<Picture> pics) {
            super();
            this.pics = pics;
        }

        @Override
        public int getCount() {
            return pics.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {

            String imgURL = pics.get(position).getPictureURL();

            final PhotoView photoView = new PhotoView(container.getContext());
            photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            photoView.setImageResource(R.drawable.ic_menu_gallery);
            imageLoader.get(imgURL, new ImageLoader.ImageListener() {

                public void onErrorResponse(VolleyError arg0) {

                }

                public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                    if (response.getBitmap() != null) {
                        photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        photoView.setImageBitmap(response.getBitmap());
                    }
                }
            });

            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
}
