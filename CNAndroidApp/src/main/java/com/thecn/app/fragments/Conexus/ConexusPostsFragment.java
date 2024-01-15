package com.thecn.app.fragments.Conexus;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.R;
import com.thecn.app.fragments.BasePostListFragment;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Conexus.UserScore;
import com.thecn.app.models.Post;
import com.thecn.app.stores.PostStore;
import com.thecn.app.tools.MyVolley;


public class ConexusPostsFragment extends BasePostListFragment {
    public static final String TAG = ConexusPostsFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_CONEXUS_KEY = "conexus";

    private Conexus mConexus;

    private View headerView;

    ImageLoader imageLoader = MyVolley.getImageLoader();

    public static ConexusPostsFragment newInstance(Conexus mConexus) {
        ConexusPostsFragment fragment = new ConexusPostsFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_CONEXUS_KEY, mConexus);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConexus = (Conexus) getArguments().getSerializable(FRAGMENT_BUNDLE_CONEXUS_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        headerView = getLayoutInflater(savedInstanceState).inflate(R.layout.conexus_header, null);
        return inflater.inflate(R.layout.fragment_post_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().addHeaderView(headerView, null, false);

        setUpHeaderView();

        addPostButton(R.id.post_button, R.id.header_post_button);
    }

    private void setUpHeaderView() {

        String avatarUrl = mConexus.getLogoURL() + ".w160.jpg";

        ImageView mImageView = (ImageView) headerView.findViewById(R.id.avatarImg);
        imageLoader.get(avatarUrl,
                ImageLoader.getImageListener(mImageView,
                        R.drawable.default_user_icon,
                        R.drawable.default_user_icon));

        TextView conexusNameTxtView = (TextView) headerView.findViewById(R.id.conexusName);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        conexusNameTxtView.setTypeface(typeface);
        conexusNameTxtView.setText(mConexus.getName());

        String conexusNumber = mConexus.getConexusNumber();
        if (conexusNumber == null) conexusNumber = "";
        TextView conexusIdTxtView = (TextView) headerView.findViewById(R.id.conexus_number);
        conexusIdTxtView.setTypeface(typeface);
        conexusIdTxtView.setText(conexusNumber);

        setUserScore();
    }

    private void setUserScore() {
        UserScore score = mConexus.getUserScore();
        String scoreText = "";

        if (score != null) {
            scoreText = Integer.toString(score.getSubTotal());
            scoreText += " Anar Seeds";

            ((TextView) headerView.findViewById(R.id.anar_number_text))
                    .setText(scoreText);
        }

        if (scoreText.length() == 0) {
            headerView.findViewById(R.id.anar_display_parent)
                    .setVisibility(View.GONE);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        position --;
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onPostAdded(Post post, String[] ids) {
        String thisId = TAG + mConexus.getId();
        for (String id : ids) {
            if (id.equals(thisId)) {
                super.onPostAdded(post, ids);
                break;
            }
        }
    }

    public void loadPosts() {
        PostStore.getPostsFromConexus(mConexus.getId(), getLimit(), getOffset(), new PostsCallback(getHandler()));
    }

    public String getFragmentID() {
        return TAG + mConexus.getId();
    }
}