package com.thecn.app.tools;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;

import com.thecn.app.AppSession;
import com.thecn.app.activities.ProfileActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by philjay on 4/25/14.
 */
public class CNNumberLinker {

    //this pattern is returned from server wherever a cn number
    //can link to a profile
    private static String cnNumberHTMLPattern =
            "<a[^>]*data-type=\"cn_number\"[^>]*data-id=\"(\\w{2}\\d{2,})\"[^>]*>[^<]*</a>";

    private static String cnNumberHTMLPattern2 =
            "<a[^>]*data-id=\"(\\w{2}\\d{2,})\"[^>]*data-type=\"cn_number\"[^>]*>[^<]*</a>";

    public CharSequence linkify(String text) {
        return substituteLinks(Html.fromHtml(substitutePatterns(text)));
    }

    private String substitutePatterns(String text) {
        return text.replaceAll(cnNumberHTMLPattern, "#$1#")
                .replaceAll(cnNumberHTMLPattern2, "#$1#");
    }

    //pattern used by replaceCNNumbers
    private Pattern cnNumberPattern = Pattern.compile("#\\w{2}\\d{2,}#");

    private CharSequence substituteLinks(CharSequence charSequence) {

        String text = charSequence.toString();
        Matcher cnNumberMatcher = cnNumberPattern.matcher(text);

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        int subStringStart = 0;
        int subStringEnd;
        while (cnNumberMatcher.find()) {
            subStringEnd = cnNumberMatcher.start();
            if (subStringEnd > 0) {
                stringBuilder.append(text.substring(subStringStart, subStringEnd));
            }

            final String cnNumberString =
                    text.substring(cnNumberMatcher.start(), cnNumberMatcher.end())
                            .replace("#", "");
            final SpannableString cnNumberSpan = new SpannableString(cnNumberString);
            cnNumberSpan.setSpan(new InternalURLSpan(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openProfilePage(cnNumberString);
                }
            }), 0, cnNumberSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            stringBuilder.append(cnNumberSpan);

            subStringStart = cnNumberMatcher.end();
        }
        if (subStringStart < text.length()) {
            stringBuilder.append(text.substring(subStringStart, text.length()));
        }

        return stringBuilder;
    }

    private void openProfilePage(String cnNumber) {
        Activity activity = AppSession.getInstance().getApplicationContext().getCurrentActivity();
        Intent intent = new Intent(activity, ProfileActivity.class);
        intent.putExtra("cn_number", cnNumber);
        activity.startActivity(intent);
    }
}
