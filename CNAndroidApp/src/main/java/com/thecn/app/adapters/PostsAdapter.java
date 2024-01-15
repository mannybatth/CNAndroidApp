package com.thecn.app.adapters;

import android.view.View;
import android.view.ViewGroup;

import com.thecn.app.fragments.BasePostListFragment;
import com.thecn.app.models.Post;
import com.thecn.app.tools.PostViewController;

import java.util.ArrayList;

public class PostsAdapter extends MyFragmentAdapter {

    private ArrayList<Post> mPosts = new ArrayList<Post>();

    public PostsAdapter(BasePostListFragment fragment) {
        super(fragment);
    }

    @Override
    public int getCount() {
        return mPosts.size();
    }

    @Override
    public Post getItem(int position) {
        return mPosts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(Post post) {
        mPosts.add(post);
        notifyDataSetChanged();
    }

    public void add(int index, Post post) {
        mPosts.add(index, post);
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<Post> posts) {
        mPosts.addAll(posts);
        notifyDataSetChanged();
    }

    public void addAll(int index, ArrayList<Post> posts) {
        mPosts.addAll(index, posts);
        notifyDataSetChanged();
    }

    public Post get(int index) {
        return mPosts.get(index);
    }

    public void set(int index, Post post) {
        mPosts.set(index, post);
        notifyDataSetChanged();
    }

    public void remove(int index) {
        mPosts.remove(index);
        notifyDataSetChanged();
    }

    public void remove(Post post) {
        mPosts.remove(post);
        notifyDataSetChanged();
    }

    public void clear() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Post post = getItem(position);

        PostViewController holder;

        if (convertView == null) {

            holder = new PostViewController(getMyListFragment());
            convertView = holder.getRootView();

            convertView.setTag(holder);

        } else {
            holder = (PostViewController) convertView.getTag();
        }

        holder.setUpView(post, position);

        return convertView;
    }
}
