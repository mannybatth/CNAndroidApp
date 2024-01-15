package com.thecn.app.models;

import android.text.Editable;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.models.User.User;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by pheebner on 1/22/14.
 */
public class PollItem implements Serializable {
    @SerializedName("chart_data")
    private ArrayList<ChartMember> chartData;

    @SerializedName("choices")
    private ArrayList<Choice> choices;

    @SerializedName("correct_response_text")
    private String correctResponse;

    @SerializedName("count_attachment")
    private Integer attachmentCount;

    @SerializedName("count_file")
    private Integer fileCount;

    @SerializedName("count_link")
    private Integer linkCount;

    @SerializedName("count_picture")
    private Integer pictureCount;

    @SerializedName("count_video")
    private Integer videoCount;

    //not sure what the purpose of this...seems to be a duplicate
    @SerializedName("display_correct_response_text")
    private String displayCorrectResponse;

    @SerializedName("display_submissions_count")
    private Integer displaySubmissionCount;

    @SerializedName("display_text")
    private String displayText;

    @SerializedName("has_submissions_count")
    private Boolean hasSubmissionsCount;

    @SerializedName("id")
    private String id;

    @SerializedName("is_display_result")
    private Boolean displayResult;

    @SerializedName("is_display_user")
    private Boolean displayUser;

    @SerializedName("is_enable")
    private Boolean enabled;

    @SerializedName("is_end")
    private Boolean ended;

    @SerializedName("is_owner")
    private Boolean ownerIsMe;

    @SerializedName("is_pictures")
    private Boolean hasPictures;

    @SerializedName("is_short_answer_type")
    private Boolean isShortAnswer;

    @SerializedName("is_user_submit")
    private Boolean userHasSubmitted;

    @SerializedName("question_count")
    private Integer questionCount;

    @SerializedName("question_order")
    private Integer questionOrder;

    @SerializedName("result_message")
    private Boolean showResultMessage;

    @SerializedName("submission_count")
    private Integer submissionCount;

    @SerializedName("survey_type")
    private String surveyType;

    @SerializedName("text")
    private String text;

    @SerializedName("submissions")
    private ArrayList<Submission> submissions;

    private int countTotal;

    public int getCountTotal() {
        return countTotal;
    }

    public void setCountTotal(int countTotal) {
        this.countTotal = countTotal;
    }

    private Editable shortAnswer;

    public enum InputType {
        SHORT_ANSWER, ONE_CHOICE, MULTIPLE_CHOICE
    }

    private InputType mInputType;

    public InputType getInputType() {

        if (mInputType == null) {
            mInputType = getInputTypeFromData();
        }

        return mInputType;
    }

    public InputType getInputTypeFromData() {
        InputType inputType = null;

        if (surveyType.equals("short_answer")) {
            inputType = InputType.SHORT_ANSWER;
        } else if (
                surveyType.equals("yes_no") ||
                        surveyType.equals("scale_5") ||
                        surveyType.equals("scale_10") ||
                        surveyType.equals("one_choice") ||
                        surveyType.equals("true_false") ||
                        surveyType.equals("agree_disagree") ||
                        surveyType.equals("agree_noopinion_disagree") ||
                        surveyType.equals("stronglyagree_agree_noopinion_disagree_stronglydisagree")
                ) {

            inputType = InputType.ONE_CHOICE;
        } else if (surveyType.equals("multiple_choice")) {
            inputType = InputType.MULTIPLE_CHOICE;
        }

        return inputType;
    }

    public void setInputType(InputType inputType) {
        mInputType = inputType;
    }

    public enum SubmissionDisplayType {
        USER_ANSWER, ANSWER, NOTHING
    }

    private SubmissionDisplayType mSubmissionDisplayType;

    public SubmissionDisplayType getSubmissionDisplayType() {

        if (mSubmissionDisplayType == null) {
            mSubmissionDisplayType = getSubmissionDisplayTypeFromData();
        }

        return mSubmissionDisplayType;
    }

    public SubmissionDisplayType getSubmissionDisplayTypeFromData() {
        SubmissionDisplayType displayType;

        if (getDisplayResult() && getDisplayUser()) {
            displayType = SubmissionDisplayType.USER_ANSWER;
        } else if (getDisplayResult()) {
            displayType = SubmissionDisplayType.ANSWER;
        } else {
            displayType = SubmissionDisplayType.NOTHING;
        }

        return displayType;
    }

    public void setSubmissionDisplayType(SubmissionDisplayType mSubmissionDisplayType) {
        this.mSubmissionDisplayType = mSubmissionDisplayType;
    }

    public Integer getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(Integer submissionCount) {
        this.submissionCount = submissionCount;
    }

    public Editable getShortAnswer() {
        return shortAnswer;
    }

    public void setShortAnswer(Editable shortAnswer) {
        this.shortAnswer = shortAnswer;
    }

    public ArrayList<ChartMember> getChartData() {
        return chartData;
    }

    public void setChartData(ArrayList<ChartMember> chartData) {
        this.chartData = chartData;
    }

    public ArrayList<Choice> getChoices() {
        return choices;
    }

    public void setChoices(ArrayList<Choice> choices) {
        this.choices = choices;
    }

    public String getCorrectResponse() {
        return correctResponse;
    }

    public void setCorrectResponse(String correctResponse) {
        this.correctResponse = correctResponse;
    }

    public Integer getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(Integer attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    public Integer getLinkCount() {
        return linkCount;
    }

    public void setLinkCount(Integer linkCount) {
        this.linkCount = linkCount;
    }

    public Integer getPictureCount() {
        return pictureCount;
    }

    public void setPictureCount(Integer pictureCount) {
        this.pictureCount = pictureCount;
    }

    public Integer getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(Integer videoCount) {
        this.videoCount = videoCount;
    }

    public String getDisplayCorrectResponse() {
        return displayCorrectResponse;
    }

    public void setDisplayCorrectResponse(String displayCorrectResponse) {
        this.displayCorrectResponse = displayCorrectResponse;
    }

    public Integer getDisplaySubmissionCount() {
        return displaySubmissionCount;
    }

    public void setDisplaySubmissionCount(Integer displaySubmissionCount) {
        this.displaySubmissionCount = displaySubmissionCount;
    }

    public String getSurveyType() {
        return surveyType;
    }

    public void setSurveyType(String surveyType) {
        this.surveyType = surveyType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public Boolean getHasSubmissionsCount() {
        return hasSubmissionsCount;
    }

    public void setHasSubmissionsCount(Boolean hasSubmissionsCount) {
        this.hasSubmissionsCount = hasSubmissionsCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getDisplayResult() {
        return displayResult;
    }

    public void setDisplayResult(Boolean displayResult) {
        this.displayResult = displayResult;
    }

    public Boolean getDisplayUser() {
        return displayUser;
    }

    public void setDisplayUser(Boolean displayUser) {
        this.displayUser = displayUser;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnded() {
        return ended;
    }

    public void setEnded(Boolean ended) {
        this.ended = ended;
    }

    public Boolean getOwnerIsMe() {
        return ownerIsMe;
    }

    public void setOwnerIsMe(Boolean ownerIsMe) {
        this.ownerIsMe = ownerIsMe;
    }

    public Boolean getHasPictures() {
        return hasPictures;
    }

    public void setHasPictures(Boolean hasPictures) {
        this.hasPictures = hasPictures;
    }

    public Boolean getIsShortAnswer() {
        return isShortAnswer;
    }

    public void setIsShortAnswer(Boolean isShortAnswer) {
        this.isShortAnswer = isShortAnswer;
    }

    public Boolean getUserHasSubmitted() {
        return userHasSubmitted;
    }

    public void setUserHasSubmitted(Boolean userHasSubmitted) {
        this.userHasSubmitted = userHasSubmitted;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }

    public Boolean getShowResultMessage() {
        return showResultMessage;
    }

    public void setShowResultMessage(Boolean showResultMessage) {
        this.showResultMessage = showResultMessage;
    }

    public ArrayList<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(ArrayList<Submission> submissions) {
        this.submissions = submissions;
    }

    public static class ChartMember implements Serializable {
        @SerializedName("name")
        private String name;

        @SerializedName("id")
        private String id;

        @SerializedName("count")
        private int count;

        private int color;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }

    public static class Choice implements Serializable {
        @SerializedName("seq_id")
        private String sequenceId;

        @SerializedName("subject")
        private String subject;

        private boolean selected;

        public Choice(String sequenceId, String subject) {
            this.sequenceId = sequenceId;
            this.subject = subject;
        }

        public String getSequenceId() {
            return sequenceId;
        }

        public void setSequenceId(String sequenceId) {
            this.sequenceId = sequenceId;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    public static class Submission implements Serializable {
        @SerializedName("answer")
        private ArrayList<String> answers;

        @SerializedName("user")
        private User user;

        public ArrayList<String> getAnswers() {
            return answers;
        }

        public void setAnswers(ArrayList<String> answers) {
            this.answers = answers;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }
}
