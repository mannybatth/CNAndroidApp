package com.thecn.app.fragments.Profile;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.models.Country;
import com.thecn.app.models.User.UserCurrentWork;
import com.thecn.app.models.User.UserPosition;
import com.thecn.app.models.User.User;
import com.thecn.app.models.User.UserProfile;

public class ProfileAboutFragment extends Fragment{

    public static final String TAG = ProfileAboutFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_USER_KEY = "user";

    private User mUser;

    public static ProfileAboutFragment newInstance(User mUser) {
        ProfileAboutFragment fragment = new ProfileAboutFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_USER_KEY, mUser);
        fragment.setArguments(args);

        return fragment;
    }

    public ProfileAboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mUser = (User) getArguments().getSerializable(FRAGMENT_BUNDLE_USER_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_about, container, false);

        UserProfile profile = mUser.getUserProfile();
        if (profile != null) {

            boolean noInformationAtAll = true;

            String genderContent = profile.getGender();
            if (genderContent != null && genderContent.length() > 0) {
                if (genderContent.equals("prefer")) {
                    genderContent = "Not specified";
                } else {
                    genderContent = getCapitalizedString(genderContent);
                }
                ((TextView) view.findViewById(R.id.gender_content))
                        .setText(genderContent);
                noInformationAtAll = false;
            } else {
                view.findViewById(R.id.gender_layout)
                        .setVisibility(View.GONE);
            }

            UserPosition userPosition = profile.getUserPosition();
            String positionContent = getUserPositionString(userPosition);
            if (positionContent != null && positionContent.length() > 0) {
                ((TextView) view.findViewById(R.id.position_content))
                        .setText(positionContent);
                noInformationAtAll = false;
            } else {
                view.findViewById(R.id.position_layout)
                        .setVisibility(View.GONE);
            }

            UserCurrentWork userCurrentWork = profile.getUserCurrentWork();
            boolean hasWorkPosition = false;
            if (userCurrentWork != null) {
                String workPosition = userCurrentWork.getPosition();
                hasWorkPosition = workPosition != null;

                if (hasWorkPosition) {
                    String company = userCurrentWork.getCompany();
                    company = company != null ? " at " + company : "";

                    ((TextView) view.findViewById(R.id.current_work_content))
                            .setText(workPosition + company);
                    noInformationAtAll = false;
                }
            }
            if (!hasWorkPosition) {
                view.findViewById(R.id.current_work_layout)
                        .setVisibility(View.GONE);
            }

            String primaryLanguage = profile.getPrimaryLanguage();
            if (primaryLanguage != null && primaryLanguage.length() > 0) {
                ((TextView) view.findViewById(R.id.primary_language_content))
                        .setText(primaryLanguage);
                noInformationAtAll = false;
            } else {
                view.findViewById(R.id.primary_language_layout)
                        .setVisibility(View.GONE);
            }

            Country country = mUser.getCountry();
            if (country != null) {
                String countryString = country.getName();
                if (countryString != null && countryString.length() > 0) {
                    ((TextView) view.findViewById(R.id.country_content))
                            .setText(countryString);
                    noInformationAtAll = false;
                } else {
                    view.findViewById(R.id.country_layout)
                            .setVisibility(View.GONE);
                }
            }

            String timeZone = profile.getTimeZone();
            if (timeZone != null && timeZone.length() > 0) {
                ((TextView) view.findViewById(R.id.time_zone_content))
                        .setText(timeZone);
                noInformationAtAll = false;
            } else {
                view.findViewById(R.id.time_zone_layout)
                        .setVisibility(View.GONE);
            }

            String userAbout = profile.getAbout();
            if (userAbout != null && userAbout.length() > 0) {
                ((TextView) view.findViewById(R.id.about_content))
                        .setText(userAbout);

            } else if (noInformationAtAll) {
                displayNoInformation(view);

            } else {
                view.findViewById(R.id.about_layout)
                        .setVisibility(View.GONE);
            }
        } else {
            displayNoInformation(view);
        }

        return view;
    }

    private void displayNoInformation(View view) {
        ((TextView) view.findViewById(R.id.about_content))
                .setText("No information available...");

        view.findViewById(R.id.about_label)
                .setVisibility(View.GONE);

        view.findViewById(R.id.gender_layout)
                .setVisibility(View.GONE);
        view.findViewById(R.id.position_layout)
                .setVisibility(View.GONE);
        view.findViewById(R.id.current_work_layout)
                .setVisibility(View.GONE);
        view.findViewById(R.id.primary_language_layout)
                .setVisibility(View.GONE);
        view.findViewById(R.id.country_layout)
                .setVisibility(View.GONE);
        view.findViewById(R.id.time_zone_layout)
                .setVisibility(View.GONE);
    }

    private String getUserPositionString(UserPosition userPosition) {

        String positionContent = null;

        if (userPosition != null) {
            String position = userPosition.getPosition();
            position = position != null ? position : "";

            String schoolName = userPosition.getSchoolName();
            schoolName = schoolName != null ? schoolName : "";

            String type = userPosition.getType();
            type = type != null ? type : "";

            if (position.equals("other")) {
                positionContent = type;
            } else {
                if (position.length() > 0) {
                    String positionCaps = getCapitalizedString(position);
                    String schoolNameCaps = getCapitalizedString(schoolName);

                    positionContent =  positionCaps + " at " + schoolNameCaps;
                }
            }
        }

        return positionContent;
    }

    private String getCapitalizedString(String string) {
        if (string.length() == 1) {
            return Character.toString(Character.toUpperCase(string.charAt(0)));
        } else if (string.length() > 1) {
            return Character.toUpperCase(string.charAt(0)) + string.substring(1);
        } else return string;
    }
}
