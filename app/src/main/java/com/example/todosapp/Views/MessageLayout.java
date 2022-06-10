package com.example.todosapp.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.todosapp.R;
import com.example.todosapp.Utils.Tools;


/**
 * @author NguyenTienDung
 * Layout to display Single Message
 * First, Create this element in XML file with:
 *      - width: match_parent;
 *      - height: wrap_content;
 *      - behavior: top;
 * Next, Display message by calling method addErrorMessage
 * @see #textSize: default 14sp
 * @see #duration: default 1000
 *
 */
public class MessageLayout extends LinearLayout {


    // Default 1000
    private int duration;
    private float textSize;
    private boolean wasModified;




    public MessageLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        removeAllViews();
        duration = 1500;
        wasModified = false;
    }

    /**
     * Remove all message before and display a new message
     * Message will be deleted after duration milli seconds
     * @param message String: message to display
     *
     * */
    public void addErrorMessage(String message){



        // Create View
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_message, null, true);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        ImageView icon =view.findViewById(R.id.icMessage);

        // remove old view
        this.removeAllViews();

        // Config View
        if(wasModified){
            configView(tvMessage, icon);
        }
        tvMessage.setText(message);

        //  add to parents
        this.removeAllViews();
        this.addView(view);

        // start animation
        Animation showAnimate = AnimationUtils.loadAnimation(getContext(), R.anim.anim_top_to_bottom_basic);
        showAnimate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(showAnimate);

        // hide animate
        getHandler().postDelayed((Runnable) () -> {
            Animation hideAnimate = AnimationUtils.loadAnimation(getContext(), R.anim.anim_bottom_to_top_basic);
            hideAnimate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    MessageLayout.this.removeView(view);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(hideAnimate);
        }, duration + showAnimate.getDuration());
    }


    /**
     * Remove all message before and display a new message
     * Message will be deleted after duration milli seconds
     * @param resId StringRes: message to display
     *
     * */
    public void addErrorMessage(@StringRes int resId){



        // Create View
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_message, null, true);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        ImageView icon =view.findViewById(R.id.icMessage);

        // remove old view
        this.removeAllViews();

        // Config View
        if(wasModified){
            configView(tvMessage, icon);
        }
        tvMessage.setText(getContext().getString(resId));

        //  add to parents
        this.removeAllViews();
        this.addView(view);

        // start animation
        Animation showAnimate = AnimationUtils.loadAnimation(getContext(), R.anim.anim_top_to_bottom_basic);
        showAnimate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(showAnimate);

        // hide animate
        getHandler().postDelayed((Runnable) () -> {
            Animation hideAnimate = AnimationUtils.loadAnimation(getContext(), R.anim.anim_bottom_to_top_basic);
            hideAnimate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    MessageLayout.this.removeView(view);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(hideAnimate);
        }, duration + showAnimate.getDuration());
    }


    /**
     * Time display message
     * @param duration int: milli seconds, > 0
     *
     */
    public void setDuration(int duration) {
        if(duration <= 0){
            Log.d("MessageLayout", "Duration must be more than 0");
            return;
        }
        this.duration = duration;
    }


    /**
    * After set text size, line height of message and size of icon will equals 1.5 * textSize
    * @param textSize float: dp unit
    */
    public void setTextSize(float textSize) {
        // convert dp to px
        this.textSize = Tools.convertDpToPx(textSize, getContext());
        wasModified = true;
    }

    private void configView(TextView tvMessage, ImageView icon){
        // set font size and line height for message
        tvMessage.setTextSize(textSize);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            tvMessage.setLineHeight((int) textSize);
        }

        // set width and height for icon
        LayoutParams layoutParams = (LayoutParams) icon.getLayoutParams();
        layoutParams.width = (int) (textSize * 1.5);
        layoutParams.height = (int) (textSize * 1.5);
        icon.setLayoutParams(layoutParams);
    }

}