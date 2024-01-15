package com.thecn.app.models;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.AppSession;
import com.thecn.app.activities.PostActivity;
import com.thecn.app.models.Conexus.Conexus;
import com.thecn.app.models.Course.Course;
import com.thecn.app.models.User.User;
import com.thecn.app.tools.CNNumberLinker;
import com.thecn.app.tools.InternalURLSpan;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Post implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("display_time")
    private String displayTime;

    @SerializedName("ctime")
    private double cTime;

    @SerializedName("title")
    private String title;

    @SerializedName("text")
    private String text;

    @SerializedName("user")
    private User user;

    @SerializedName("type")
    private String postType;

    @SerializedName("user_position")
    private String userPosition;

    @SerializedName("courses")
    private ArrayList<Course> courses;

    @SerializedName("conexuses")
    private ArrayList<Conexus> conexuses;

    @SerializedName("pictures")
    private ArrayList<Picture> pictures;

    @SerializedName("has_set_good")
    private boolean isLiked;

    @SerializedName("is_deletable")
    private boolean isDeletable;

    @SerializedName("is_editable")
    private boolean isEditable;

    @SerializedName("is_from_admin")
    private boolean isFromAdmin;

    @SerializedName("is_owner")
    private boolean isOwner;

    @SerializedName("is_repostable")
    private boolean isRepostable;

    @SerializedName("count")
    private ContentCount count;

    //FOR EVENTS
    @SerializedName("where")
    private String eventLocation;

    @SerializedName("display_start_time")
    private String eventStartTime;

    @SerializedName("display_end_time")
    private String eventEndTime;

    //FOR POLLS
    @SerializedName("items")
    private ArrayList<PollItem> items;

    //FOR QUIZES
    @SerializedName("grade_type")
    private String gradeType;

    @SerializedName("view_submissions")
    private String viewSubmissions;

    @SerializedName("total_score")
    private String totalPointValue;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    //FOR CLASS CAST
    @SerializedName("length")
    private int length;

    public static String getYoutubeLinkRegex() {
        return youtubeLinkRegex;
    }

    //FOR SHARELINK
    @SerializedName("description")
    private String description;

    @SerializedName("videos")
    private ArrayList<Video> videos;

    @SerializedName("original_share_link")
    private String originalLink;

    private static final String youtubeLinkRegex = "(https?://)?(www\\.)?" +
            "(youtube(-nocookie)?\\.com/(((e(mbed)?|v|user)/.*?)|((watch)?(\\?feature=player_embedded)?[\\?&]v=))" +
            "|(youtu\\.be/))" +
            "[A-Za-z0-9-_]{11}.*";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition;
    }

    public ArrayList<Picture> getPictures() {
        if(isShareLinkVideoPost()){
            ArrayList<Picture> picturesWithoutVideoThumb = new ArrayList<Picture>(pictures);
            if (picturesWithoutVideoThumb.size() > 0)
                picturesWithoutVideoThumb.remove(0);

            return picturesWithoutVideoThumb;
        } else return pictures;
    }

    public void setPictures(ArrayList<Picture> pictures) {
        this.pictures = pictures;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public boolean isDeletable() {
        return isDeletable;
    }

    public void setDeletable(boolean isDeletable) {
        this.isDeletable = isDeletable;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public boolean isFromAdmin() {
        return isFromAdmin;
    }

    public void setFromAdmin(boolean isFromAdmin) {
        this.isFromAdmin = isFromAdmin;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public boolean isRepostable() {
        return isRepostable;
    }

    public void setRepostable(boolean isRepostable) {
        this.isRepostable = isRepostable;
    }

    public ContentCount getCount() {
        return count;
    }

    public void setCount(ContentCount count) {
        this.count = count;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(String eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public String getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(String eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public ArrayList<PollItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<PollItem> items) {
        this.items = items;
    }

    public String getGradeType() {
        return gradeType;
    }

    public void setGradeType(String gradeType) {
        this.gradeType = gradeType;
    }

    public String getViewSubmissions() {
        return viewSubmissions;
    }

    public void setViewSubmissions(String viewSubmissions) {
        this.viewSubmissions = viewSubmissions;
    }

    public String getTotalPointValue() {
        return totalPointValue;
    }

    public void setTotalPointValue(String totalPointValue) {
        this.totalPointValue = totalPointValue;
    }

    public double getcTime() {
        return cTime;
    }

    public void setcTime(double cTime) {
        this.cTime = cTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<Course> courses) {
        this.courses = courses;
    }

    public ArrayList<Conexus> getConexuses() {
        return conexuses;
    }

    public void setConexuses(ArrayList<Conexus> conexuses) {
        this.conexuses = conexuses;
    }

    public ArrayList<Video> getVideos() {
        if(isShareLinkVideoPost()){
            ArrayList<Video> videosWithLink = new ArrayList<Video>(videos);
            videosWithLink.add(0, new Post.Video(originalLink));
            return videosWithLink;
        } else return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    public void setOriginalLink(String originalLink) {
        this.originalLink = originalLink;
    }

    private boolean isShareLinkVideoPost() {
        return getPostType().equals("sharelink")
                && originalLink != null
                && originalLink.matches(youtubeLinkRegex);
    }

    public static class Video implements Serializable{

        @SerializedName("view_url")
        private String viewURL;

        public String getViewURL() {
            return viewURL;
        }

        public void setViewURL(String viewURL) {
            this.viewURL = viewURL;
        }

        public Video(String viewURL) {
            setViewURL(viewURL);
        }

        private static Pattern[] videoIDPatterns = new Pattern[] {
                Pattern.compile("youtube\\.com/v/([\\w0-9_-]{11})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("youtube\\.com/watch\\?*v=([\\w0-9_-]{11})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("youtu\\.be/([\\w0-9_-]{11})", Pattern.CASE_INSENSITIVE),
        };

        public String getVideoID() {
            for (Pattern p : videoIDPatterns) {
                Matcher matcher = p.matcher(viewURL);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }

            return null;
        }
    }

    /**
     * code below is for processing post information in preparation for display
     * intended to be done in a background thread BEFORE displaying
     */

    private transient boolean isFullView;

    public void setFullView(boolean isFullView) {
        this.isFullView = isFullView;
    }

    public boolean isFullView() {
        return isFullView;
    }

    public void processData() {
        initEnumPostType();
        initTimeText();
        initProcessedTitle();
        initContentText();
        initPostFromText();
    }

    private transient CharSequence contentText;

    private void initContentText() {
        switch (enumType) {
            case POST:
                contentText = formatContent(text);
                break;
            case SHARELINK:
                contentText = getShareLinkContent();
                break;
            case POLL:
                contentText = getPollContent();
                break;
            case EVENT:
                contentText = getEventContent();
                break;
            case QUIZ:
                contentText = getQuizContent();
                break;
            case CLASSCAST:
                contentText = getClasscastContent();
                break;
        }
    }

    public CharSequence getContentText() {
        return contentText;
    }

    private CharSequence getShareLinkContent() {
        String content = text;

        if (description != null && description.length() > 0) {
            if (content.length() != 0)
                content += "<br><br>";
            content += description;
        }

        CharSequence formattedContent = formatContent(content);

        if (originalLink != null) {
            formattedContent = TextUtils.concat(formattedContent, "\n\n", originalLink);
        }
        return formattedContent;
    }

    private CharSequence getPollContent() {
        String content = "";

        if (items != null) {
            for (PollItem item : items) {
                content += item.getDisplayText() + "<br>";
            }

            CharSequence formattedContent = formatContent(content);
            return TextUtils.concat(formattedContent, "\n\n",
                    "For now, please use a desktop computer to access this Poll.");
        }

        return content;
    }

    private CharSequence getEventContent() {
        String content = "";

        if (eventStartTime != null)
            content += "<b>Begins:</b> " + eventStartTime;

        if (eventEndTime != null)
            content += "<br><b>Ends:</b> " + eventEndTime;

        if (eventLocation != null)
            content += "<br><b>Where:</b> " + eventLocation;

        if (text != null)
            content += "<br><br>" + text;

        return formatContent(content);
    }

    private CharSequence getQuizContent() {
        String content = "";

        if (processedTitle != null)
            content += "<b>Quiz Name:</b> " + processedTitle;

        if (text != null)
            content += "<br><b>Description:</b> " + text;

        content += "<br><b>Record:</b> " + getProcessedGradeType();
        content += "<br><b>View Submission:</b> " + getProcessedViewSubmissions();

        if (totalPointValue != null)
            content += "<br><b>Total Point Value:</b> " + totalPointValue + " PTS";

        content += "<br><b>Available Date:</b> " + getProcessedStartTime();
        content += "<br><b>End Date:</b> " + getProcessedEndTime();

        CharSequence formattedContent = formatContent(content);

        return TextUtils.concat(formattedContent, "\n\n",
                "For now, please use a desktop computer to access this Quiz.");
    }

    private CharSequence getClasscastContent() {
        String content = "";

        if (text != null) content += text;

        content += "<br><b>When:</b> "
                + getProcessedStartTime() + " to " + getProcessedEndTime();
        content += "<br><b>Duration:</b> " + getTimeStringFromMinutes(length);
        content += "<br><b>Status:</b> " + getClassCastStatus();

        CharSequence formattedContent = formatContent(content);
        return TextUtils.concat(formattedContent, "\n\n",
                "Please use a desktop computer to access this ClassCast.");
    }

    private String getProcessedGradeType() {
        if (gradeType != null) {
                 if (gradeType.equals("highest")) return "HIGHEST SCORE";
            else if (gradeType.equals("last"))    return "LAST SCORE";
            else if (gradeType.equals("average")) return "AVERAGE SCORE";
        }

        return "";
    }

    private String getProcessedViewSubmissions() {
        if (viewSubmissions != null) {
            if (viewSubmissions.equals("yes"))
                return "Students can View Submissions without Correct Answers";
            else if (viewSubmissions.equals("yes_n_answer"))
                return "Students can View Submissions with Correct Answers";
            else if (viewSubmissions.equals("no"))
                return "Students cannot View Submissions";
        }

        return "";
    }

    public String getInteractNumText() {
        try {
            String likeText = " Like";
            String reflectionText = " Reflection";
            int numLikes = count.getLikes();
            int numReflections = count.getReflections();

            likeText += numLikes == 1 ? "   " : "s   ";
            reflectionText += numReflections == 1 ? "   " : "s   ";

            return numLikes + likeText + numReflections + reflectionText;
        } catch (NullPointerException e) {
            // oops
        }

        return "";
    }

    private transient String postFromText;

    private void initPostFromText() {
        postFromText = "";
        int otherCount = 0;

        boolean entryExists = courses != null && courses.size() > 0;

        if (entryExists) {
            try {
                postFromText += courses.get(0).getName();
            } catch (NullPointerException e) {
                // no name
            }

            for (int i = 1; i < courses.size(); i++) {
                otherCount++;
            }
        }

        if (conexuses != null && conexuses.size() > 0) {
            try {
                if (entryExists) otherCount++;
                else postFromText += conexuses.get(0).getName();
            } catch (NullPointerException e) {
                // no name
            }

            for (int i = 1; i < conexuses.size(); i++) {
                otherCount++;
            }
        }

        if (otherCount > 0) {
            postFromText += ", " + otherCount + " other";

            if (otherCount > 1)
                postFromText += "s";
        }
    }

    public String getPostFromText() {
        return postFromText;
    }

    public enum Type {
        POST, SHARELINK, POLL, EVENT, QUIZ, CLASSCAST
    }

    private transient Type enumType;

    public Type getEnumType() {
        return enumType;
    }

    private void initEnumPostType() {
        if (postType != null) {
            if (postType.equals("post"))      enumType = Type.POST;
            else if (postType.equals("sharelink")) enumType = Type.SHARELINK;
            else if (postType.equals("survey"))    enumType = Type.POLL;
            else if (postType.equals("event"))     enumType = Type.EVENT;
            else if (postType.equals("quiz"))      enumType = Type.QUIZ;
            else if (postType.equals("classcast")) enumType = Type.CLASSCAST;
        }
    }

    private transient String timeText;

    public String getTimeText() {
        return timeText;
    }

    private void initTimeText() {
        timeText = displayTime;
    }

//    private void initTimeText() {
//        long diffTime = System.currentTimeMillis() - ((long) (cTime * 1000L));
//
//        if (diffTime <= 0) {
//            timeText = "just now";
//        } else {
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(diffTime);
//
//            int years, months, days, hours, minutes, seconds;
//
//            years = calendar.get(Calendar.YEAR) - 1970;
//            if (years == 0) {
//                months = calendar.get(Calendar.MONTH);
//                if (months == 0) {
//                    days = calendar.get(Calendar.DAY_OF_MONTH) - 1;
//                    if (days == 0) {
//                        hours = calendar.get(Calendar.HOUR_OF_DAY);
//                        if (hours == 0) {
//                            minutes = calendar.get(Calendar.MINUTE);
//                            if (minutes == 0) {
//                                seconds = calendar.get(Calendar.SECOND);
//
//                                if (seconds <= 30) {
//                                    timeText = "just now";
//                                } else {
//                                    timeText = seconds + " seconds";
//                                }
//                            } else {
//                                timeText = getPluralityString("minute", minutes);
//                            }
//                        } else {
//                            timeText = getPluralityString("hour", hours);
//                        }
//                    } else {
//                        timeText = getPluralityString("day", days);
//                    }
//                } else {
//                    timeText = displayTime;
//                }
//            } else {
//                timeText = displayTime;
//            }
//        }
//    }

    private String getPluralityString(String base, int number) {
        String text = number + " ";

        if (number > 1) base += "s";
        return text + base;
    }

    private static final Pattern breakPattern = Pattern.compile("<\\s*/?\\s*br\\s*/?\\s*>");
    private static final int contentCharLimit = 360;

    private transient boolean contentTruncated = false;

    //fragment is used to get Activity when content is clicked, so as to open another activity
    private CharSequence formatContent(String text) {
        contentTruncated = false;
        if (!isFullView) text = truncateTextByNumLines(text);

        CNNumberLinker linker = new CNNumberLinker();

        CharSequence formatText = linker.linkify(text);

        if (!isFullView && formatText.length() > contentCharLimit) {
            formatText = formatText.subSequence(0, contentCharLimit);
            contentTruncated = true;
        }

        if (contentTruncated) {
            String ellipses = "...";
            final SpannableString linkToFullView = new SpannableString("(Read more)");
            linkToFullView.setSpan(new InternalURLSpan(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openPostPage(Post.this);
                }}), 0, linkToFullView.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            formatText = TextUtils.concat(formatText, ellipses, linkToFullView);
        }

        return formatText;
    }

    public void openPostPage(Post post) {
        Activity activity = AppSession.getInstance().getApplicationContext().getCurrentActivity();
        final Intent intent = new Intent(activity, PostActivity.class);
        intent.putExtra("post", post);
        intent.putExtra("textFocus", false);
        activity.startActivity(intent);
    }

    private String truncateTextByNumLines(String text) {
        Matcher breakMatcher = breakPattern.matcher(text);

        boolean keepGoing = true;
        int matchCount = 0;
        while (keepGoing) {
            if (breakMatcher.find()) {
                matchCount ++;
                if (matchCount > 7) {
                    text = text.substring(0, breakMatcher.start() - 1);
                    contentTruncated = true;
                    keepGoing = false;
                }
            } else {
                keepGoing = false;
            }
        }

        return text;
    }

    private transient Spanned processedTitle;

    public void initProcessedTitle() {
        if (title != null) {
            processedTitle = Html.fromHtml(title);
        }
    }

    public Spanned getProcessedTitle() {
        return processedTitle;
    }

    private String getProcessedStartTime() {
        if (startTime != null) {
            try {
                Long time = Long.parseLong(startTime);
                return getStringFromTimestamp(time);
            } catch (NumberFormatException e) {
                // whoops
            }
        }

        return "";
    }

    private String getProcessedEndTime() {
        if (endTime != null) {
            try {
                Long time = Long.parseLong(endTime);
                return getStringFromTimestamp(time);
            } catch (NumberFormatException e) {
                // whoops
            }
        }

        return "";
    }

    private String getStringFromTimestamp(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy, h:mm a");

        return dateFormat.format(calendar.getTime());
    }

    private String getTimeStringFromMinutes(int minutes) {
        int numHours = minutes / 60;
        int numExtraMinutes = minutes % 60;

        String hourString = Integer.toString(numHours) + " Hour";
        if (numHours != 1) hourString += "s";

        String minuteString = "";
        if (numExtraMinutes != 0) {
            minuteString = " and " + Integer.toString(numExtraMinutes) + " Minute";
            if (numExtraMinutes != 1) minuteString += "s";
        }

        return hourString + minuteString;
    }

    private String getClassCastStatus() {
        try {
            long begin = Long.parseLong(startTime) * 1000;
            long end = Long.parseLong(endTime) * 1000;

            long currTime = Calendar.getInstance().getTimeInMillis();

            if (begin < currTime)
                if (currTime < end)
                    return "OPEN";
                else
                    return "ENDED";
            else return "COMING SOON";

        } catch (NumberFormatException e) {
            // oopsie
        } catch (NullPointerException e) {
            // poopsie
        }

        return "";
    }

    public interface ProcessCallback {
        public void onProcessComplete(Post post);
    }

    public static class ProcessTask extends AsyncTask<Void, Void, Void> {

        private Post mPost;
        private ProcessCallback mCallback;

        public ProcessTask(Post post, ProcessCallback callback) {
            mPost = post;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {

            mPost.processData();

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            mCallback.onProcessComplete(mPost);
        }
    }

    public interface ProcessListCallback {
        public void onProcessComplete(ArrayList<Post> posts);
    }

    public static class ProcessListTask extends AsyncTask<Void, Void, Void> {

        private ArrayList<Post> mPosts;
        private ProcessListCallback mCallback;

        public ProcessListTask(ArrayList<Post> posts, ProcessListCallback callback) {
            mPosts = posts;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {

            for (Post post : mPosts) {
                post.setFullView(false);
                post.processData();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            mCallback.onProcessComplete(mPosts);
        }
    }
}
