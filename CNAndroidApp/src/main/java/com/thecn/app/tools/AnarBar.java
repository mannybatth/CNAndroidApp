package com.thecn.app.tools;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.models.Avatar;
import com.thecn.app.models.Course.Count;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.Course.ScoreUser;
import com.thecn.app.models.Course.Score;
import com.thecn.app.models.Course.ScoreSetting;
import com.thecn.app.models.Course.UserScore;
import com.thecn.app.models.User.User;

import java.util.ArrayList;

/**
 * Created by philjay on 4/3/14.
 */
public class AnarBar {

    private View anarBar;
    private Course course;

    public AnarBar(View anarBar, Course course) {
        this.anarBar = anarBar;
        this.course = course;

        initData();
    }

    private static float percentToGoal = 216f / 245f;
    private static float percentAfterGoal = 1f - percentToGoal;

    private boolean hasGoal, splitAnimations, showBar;

    private int userScore, topUserScore, goalScore, expectedScore, averageScore;

    private float firstAnimPercent, secondAnimPercent, avgOrDifAnimPercent;

    /**
     * Sets initial values and percentages for animations
     */
    private void initData() {

        userScore = getUserScore();

        showBar = canShowBar();

        if (showBar) {

            topUserScore = 0;

            ScoreUser topUser = getTopUser();
            if (topUser != null) {
                topUserScore = getTopUserScore(topUser);
            }

            hasGoal = hasGoal();

            if (hasGoal) {
                goalScore = getGoalScore();
                expectedScore = course.getExpectedScore();

                setLayoutForGoal(true);

                //determine values for user animation
                if (userScore >= goalScore) {
                    splitAnimations = true;

                    firstAnimPercent = percentToGoal;

                    float amountPastGoal = userScore - goalScore;
                    float amountTopUserPastGoal = topUserScore - goalScore;
                    float secondAnimDifference = percentAfterGoal * amountPastGoal / amountTopUserPastGoal;

                    secondAnimPercent = firstAnimPercent + secondAnimDifference;

                } else if (userScore >= expectedScore) {
                    splitAnimations = true;

                    firstAnimPercent = ((float) expectedScore) / ((float) goalScore) * percentToGoal;
                    secondAnimPercent = ((float) userScore) / ((float) goalScore) * percentToGoal;

                } else {
                    splitAnimations = false;

                    firstAnimPercent = ((float) userScore) / ((float) goalScore) * percentToGoal;
                }

                avgOrDifAnimPercent = ((float) expectedScore) / ((float) goalScore) * percentToGoal;

            } else {
                averageScore = getAverageScore();

                setLayoutForGoal(false);

                //determine value for user animation
                firstAnimPercent = ((float) userScore) / ((float) topUserScore);

                //determine value for average animation
                avgOrDifAnimPercent = ((float) averageScore) / ((float) topUserScore);

            }

            anarBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    initAnimationFields();
                    anarBar.getViewTreeObserver().removeGlobalOnLayoutListener(this); //api level for replacement method too high

                    animateIfBarShown();
                }
            });
        } else {
            hasGoal = false;
            hideBar();
        }

        initLayout();
    }

    private void setLayoutForGoal(boolean hasGoal) {
        int goalLayoutVisibility = hasGoal ? View.VISIBLE : View.GONE;
        int notGoalLayoutVisibility = hasGoal ? View.GONE : View.VISIBLE;
        int barImageResource = hasGoal ? R.drawable.anar_bar_transition : R.drawable.anar_bar_regular;

        anarBar.findViewById(R.id.anar_req_dif)
                .setVisibility(goalLayoutVisibility);

        anarBar.findViewById(R.id.flag_filler)
                .setVisibility(goalLayoutVisibility);

        anarBar.findViewById(R.id.flag_icon)
                .setVisibility(goalLayoutVisibility);

        anarBar.findViewById(R.id.finish_line)
                .setVisibility(goalLayoutVisibility);

        anarBar.findViewById(R.id.anar_average_layout)
                .setVisibility(notGoalLayoutVisibility);

        anarBar.findViewById(R.id.avg_filler)
                .setVisibility(notGoalLayoutVisibility);

        ((ImageView) anarBar.findViewById(R.id.anar_bar))
                .setImageResource(barImageResource);
    }

    /**
     * initializes elements in the layout
     */
    private void initLayout() {
        setAnarNumberText();

        if (showBar) {
            setScoreDisplays();
            setUserImages();
        }
    }

    /**
     * Determines whether or not to show the bar
     * @return true if can show bar, false otherwise
     */
    public boolean canShowBar() {
        ScoreUser topUser = getTopUser();

        if (topUser != null) {

            int highScore = getTopUserScore(topUser);
            boolean sufficientHighScore = highScore > 10;

            boolean bottomUserPresent = isBottomUserPresent();
            boolean atLeastTwoStudents = getStudentCount() >= 2;

            return sufficientHighScore && bottomUserPresent && atLeastTwoStudents;
        }

        return false;
    }

    /**
     * hides the bar
     */
    private void hideBar() {
        anarBar.findViewById(R.id.anar_bar_lower_half)
                .setVisibility(View.GONE);
    }

    /**
     * Sets text in the large text area at the top of view
     */
    private void setAnarNumberText() {
        String numberOfSeeds = Integer.toString(userScore);

        if (hasGoal) {
            numberOfSeeds += " of " + Integer.toString(goalScore);
        }

        numberOfSeeds += " Anar Seeds";

        ((TextView) anarBar.findViewById(R.id.anar_number_text))
                .setText(numberOfSeeds);
    }

    /**
     * Sets text in score TextViews of each user.
     * Sets text in average score TextView (if there is no goal).
     * Sets text in expected score TextView (if there is a goal).
     */
    private void setScoreDisplays() {
        ((TextView) anarBar.findViewById(R.id.anar_user_score))
                .setText(Integer.toString(userScore));

        ((TextView) anarBar.findViewById(R.id.anar_top_user_score))
                .setText(Integer.toString(topUserScore));

        if (hasGoal) {
            int scoreDifference = userScore - expectedScore;
            String scoreDifString = "";

            if (scoreDifference > 0) {
                scoreDifString += "+";
            }

            scoreDifString += scoreDifference;

            ((TextView) anarBar.findViewById(R.id.anar_req_dif_text))
                    .setText(scoreDifString);
        } else {
            ((TextView) anarBar.findViewById(R.id.anar_average_text))
                    .setText(Integer.toString(averageScore));
        }
    }

    private static ImageLoader imageLoader = MyVolley.getImageLoader();

    /**
     * Sets the images for the user and the top user
     */
    private void setUserImages() {
        User user;
        String avatarUrl;

        user = AppSession.getInstance().getUser();
        avatarUrl = getAvatarUrl(user);
        setImage(avatarUrl, R.id.anar_user_image);

        ScoreUser msu = getTopUser();
        if (msu != null) {
            user = msu.getModel();
        } else {
            user = null;
        }

        if (user != null) {
            avatarUrl = getAvatarUrl(user);
            setImage(avatarUrl, R.id.anar_top_user_image);
        }
    }

    /**
     * Helper method
     * Sets an image at a url into an ImageView
     * @param avatarUrl location of image
     * @param destID id of ImageView to set image in
     */
    private void setImage(String avatarUrl, int destID) {
        if (avatarUrl != null) {
            ImageView dest = (ImageView) anarBar.findViewById(destID);
            int fallbackImageID = R.drawable.default_user_icon;

            imageLoader.get(avatarUrl,
                    ImageLoader.getImageListener(dest, fallbackImageID, fallbackImageID));
        }
    }

    /**
     * Helper method
     * Gets url of a user's avatar from the model
     * @param user the user
     * @return the url of the user's avatar
     */
    private String getAvatarUrl(User user) {
        if (user != null) {
            Avatar avatar = user.getAvatar();

            if (avatar != null) {
                String viewUrl = avatar.getView_url();

                if (viewUrl != null) {
                    viewUrl += ".w160.jpg";
                    return viewUrl;
                }
            }
        }

        return null;
    }

    /**
     * Used to specify an action on clicking an image
     */
    public interface ImageCallback {
        public void onImageClick();
    }

    /**
     * Sets a callback for when the User is clicked
     * @param callback the callback
     */
    public void setOnUserClick(final ImageCallback callback) {
        anarBar.findViewById(R.id.anar_user)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onImageClick();
                    }
                });
    }

    /**
     * Sets a callback for when the Top User is clicked
     * @param callback the callback
     */
    public void setOnTopUserClick(final ImageCallback callback) {
        anarBar.findViewById(R.id.anar_top_user)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onImageClick();
                    }
                });
    }

    /**
     * Carry out animations for the Anar Bar
     */
    public void animateIfBarShown() {
        if (showBar) {
            if (hasGoal()) {
                tDrawable.resetTransition();
            }

            mMasterAnimatorSet.start();
        }
    }

    private static final int firstAnimDuration = 2000;
    private static final int secondAnimDuration = 1000;
    private static final int expectedAnimDuration = 1000;
    private static final int transitionDuration = 500;

    private TransitionDrawable tDrawable;
    private AnimatorSet mMasterAnimatorSet;

    /**
     * Sets all values necessary for doing the anarbar animation
     * Sets additional animations to run if animations are split between two sets
     */
    private void initAnimationFields() {
        mMasterAnimatorSet = new AnimatorSet();
        AnimatorSet firstWave = new AnimatorSet();
        AnimatorSet secondWave = new AnimatorSet();

        int barWidth = anarBar.findViewById(R.id.anar_bar).getWidth();

        View clipperView = anarBar.findViewById(R.id.anar_clipper);
        View userLayout = anarBar.findViewById(R.id.anar_user_layout);
        int avgOrDifID = hasGoal ? R.id.anar_req_dif : R.id.anar_average_layout;
        View avgOrDifView = anarBar.findViewById(avgOrDifID);
        View avgOrReqLine = anarBar.findViewById(R.id.avg_or_req_line);

        float scaleX1 = 1f - firstAnimPercent;
        float barX1 = barWidth * firstAnimPercent;
        float avgOrReqX = avgOrDifAnimPercent * barWidth;
        float avgOrExpectedX1 = hasGoal ? barX1 : avgOrReqX;
        //used to determine if the top numbers need to disappear, because they might go behind the flag
        float topViewEndRightX = barX1 + anarBar.findViewById(R.id.anar_req_dif_text).getWidth();

        int avgOrReqDuration = hasGoal ? expectedAnimDuration : firstAnimDuration;

        ObjectAnimator clipperAnimation = getScaleAnimation(clipperView, 1f, scaleX1, firstAnimDuration);
        ObjectAnimator userAnimation = getTranslateAnimation(userLayout, barX1, firstAnimDuration);
        ObjectAnimator avgOrReqAnimation = getTranslateAnimation(avgOrReqLine, avgOrReqX, avgOrReqDuration);
        ObjectAnimator topViewAnimation = getTranslateAnimation(avgOrDifView, avgOrExpectedX1, firstAnimDuration);

        firstWave.play(clipperAnimation)
                .with(userAnimation)
                .with(topViewAnimation)
                .with(avgOrReqAnimation);

        mMasterAnimatorSet.play(firstWave);

        if (hasGoal) {
            if (splitAnimations) {
                float scaleX2 = 1f - secondAnimPercent;
                float barX2 = barWidth * secondAnimPercent;
                topViewEndRightX += barX2;

                ObjectAnimator clipperAnimation2 = getScaleAnimation(clipperView, scaleX1, scaleX2, secondAnimDuration);
                ObjectAnimator userAnimation2 = getTranslateAnimation(userLayout, barX2, secondAnimDuration);
                ObjectAnimator topViewAnimation2 = getTranslateAnimation(avgOrDifView, barX2, secondAnimDuration);;

                secondWave.play(clipperAnimation2)
                        .with(userAnimation2)
                        .with(topViewAnimation2);

                setAnimationCallback(firstWave, new AnimationCallback() {
                    @Override
                    public void onAnimationEnd() {
                        tDrawable.startTransition(transitionDuration);
                    }
                });

                mMasterAnimatorSet.play(secondWave).after(firstWave);
            }

            tDrawable = ((TransitionDrawable)
                    ((ImageView) anarBar.findViewById(R.id.anar_bar))
                            .getDrawable());

            float flagTranslation = barWidth * percentToGoal;
            if (topViewEndRightX >= flagTranslation) {

                ObjectAnimator animator =
                        ObjectAnimator.ofFloat(avgOrDifView, "alpha", 1f, 0f).setDuration(500);

                mMasterAnimatorSet.play(animator).after(secondWave);
            } else {
                anarBar.findViewById(R.id.flag_icon).setBackgroundResource(0);
            }
        }
    }

    /**
     * Used to clean up appearance of code
     */
    private interface AnimationCallback {
        public void onAnimationEnd();
    }

    private void setAnimationCallback(AnimatorSet set, final AnimationCallback callback) {
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                callback.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private ObjectAnimator getScaleAnimation(View view, float startX, float endX, int duration) {
        return ObjectAnimator.ofFloat(view, "scaleX", startX, endX).setDuration(duration);
    }

    private ObjectAnimator getTranslateAnimation(View view, float translationX, int duration) {
        return ObjectAnimator.ofFloat(view, "translationX", translationX).setDuration(duration);
    }

    /**
     * Determines if course has a goal
     * @return true if has goal, false if not or data nonexistent
     */
    private boolean hasGoal(){
        ScoreSetting setting = course.getScoreSetting();
        boolean hasGoal = false;

        if (setting != null) {
            String gradebookItemType = setting.getGradebookItemType();

            if (gradebookItemType != null && gradebookItemType.length() > 0) {
                hasGoal = !gradebookItemType.equals("no_calculate");
            }
        }

        return hasGoal;
    }

    /**
     * Gets user's score
     * @return user score, -1 if object null
     */
    private int getUserScore() {
        UserScore userScore = course.getUserScore();
        int score = -1;

        if (userScore != null) {
            score = userScore.getSubTotal();
        }

        return score;
    }

    /**
     * sets topUser
     */
    private ScoreUser getTopUser() {
        ArrayList<ScoreUser> users = course.getMostScoreUsers();

        if (users != null && users.size() > 0) {
            return users.get(0);
        }

        return null;
    }

    /**
     * gets model for the top user
     * @return top user's model
     */
    public User getTopUserModel() {
        return getTopUser().getModel();
    }

    /**
     * Gets top user's score
     * @return top user score, -1 if object null
     */
    private int getTopUserScore(ScoreUser topUser) {
        UserScore userScore = topUser.getUserScore();
        int score = -1;

        if (userScore != null) {
            score = userScore.getSubTotal();
        }

        return score;
    }

    /**
     * Determines if there is a "least_score" user for course
     * @return true if there is a "least_score" user, false otherwise
     */
    private boolean isBottomUserPresent() {
        ArrayList<ScoreUser> users = course.getLeastScoreUsers();

        return users != null && users.size() > 0;
    }

    /**
     * Gets number of students in class
     * @return number of students in class
     */
    private int getStudentCount() {
        Count count = course.getCount();

        if (count != null) {
            return count.getStudentCount();
        }

        return -1;
    }

    /**
     * Gets course average (including instructors)
     * @return average, -1 if object null
     */
    private int getAverageScore() {
        Score score = course.getScore();
        int average = -1;

        if (score != null) {
            average = score.getStudentAverage();
        }

        return average;
    }

    /**
     * Gets course goal score
     * @return goal, -1 if object null
     */
    private int getGoalScore() {
        ScoreSetting setting = course.getScoreSetting();
        int goal = -1;

        if (setting != null) {
            goal = setting.getRequiredNumber();
        }

        return goal;
    }
}
