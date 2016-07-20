package samuelbreck.transitpriority;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import android.os.*;

import com.transitionseverywhere.extra.Scale;
import com.transitionseverywhere.*;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

/**
 * This Fragment runs the java for the main_request.xml file
 */
public class Request_Fragment extends Fragment implements View.OnClickListener {

    private Button btnClick;

    // Layout for the overall fragment
    LinearLayout request_content;

    // Initial request priority icon layout
    LinearLayout request_button_content;

    // Layout for turn signals with a pulse
    LinearLayout turn_signals;

    // Layouts for the individual Pulsators
    PulsatorLayout pulsator_left;
    PulsatorLayout pulsator_right;
    PulsatorLayout pulsator_up;

    // Layout for turn signals without a pulse
    LinearLayout turn_signals_NOPULSE;

    // Layouts for individual turn signals without a pulse
    RelativeLayout left_NOPULSE;
    RelativeLayout up_NOPULSE;
    RelativeLayout right_NOPULSE;

    Button leftButton;
    Button upButton;
    Button rightButton;

    // For animation purposes
    boolean visible;

    // Holds the layout for the request that is received back
    LinearLayout result;

    Button approve;
    Button declined;

    boolean everyOther;

    public Request_Fragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_right, container, false);

        request_content = (LinearLayout) v.findViewById(R.id.request_content);

        request_button_content = (LinearLayout) v.findViewById(R.id.request_button_content);

        turn_signals = (LinearLayout) v.findViewById(R.id.turn_button_content);
        turn_signals_NOPULSE = (LinearLayout) v.findViewById(R.id.turn_button_content_NOPULSE);

        result = (LinearLayout) v.findViewById(R.id.result);

        turn_signals.setVisibility(View.GONE);
        turn_signals_NOPULSE.setVisibility(View.GONE);
        result.setVisibility(View.GONE);

        btnClick = (Button)v.findViewById(R.id.request_button);
        btnClick.setOnClickListener(this);

        left_NOPULSE = (RelativeLayout) v.findViewById(R.id.left_button_NOPULSE);
        up_NOPULSE = (RelativeLayout) v.findViewById(R.id.up_button_NOPULSE);
        right_NOPULSE = (RelativeLayout) v.findViewById(R.id.right_button_NOPULSE);

        pulsator_left = (PulsatorLayout) v.findViewById(R.id.pulsator_left);
        pulsator_up = (PulsatorLayout) v.findViewById(R.id.pulsator_up);
        pulsator_right = (PulsatorLayout) v.findViewById(R.id.pulsator_right);

        leftButton = (Button) v.findViewById(R.id.left_button);
        upButton = (Button) v.findViewById(R.id.up_button);
        rightButton = (Button) v.findViewById(R.id.right_button);

        approve = (Button) v.findViewById(R.id.approve);
        declined = (Button) v.findViewById(R.id.declined);

        everyOther = true;

        // set the pulsators to run in the background
        pulsator_left.start();
        pulsator_up.start();
        pulsator_right.start();

        return v;
    }

    // once the initial request button is clicked, this method executes
    public void onClick(View v) {

        if(v == btnClick) {

            boolean sendRequest = ((MainActivity)getActivity()).requestPriority("request");

            if(sendRequest) {

                // animations for the buttons
                visible = true;
                TransitionSet set_rButton = new TransitionSet()
                        .addTransition(new Scale(0.7f))
                        .addTransition(new Fade())
                        .setInterpolator(visible ? new LinearOutSlowInInterpolator() :
                                new FastOutLinearInInterpolator());

                TransitionManager.beginDelayedTransition(request_button_content, set_rButton);
                btnClick.setVisibility(visible ? View.GONE : View.VISIBLE);

                request_button_content.setVisibility(View.GONE);

                visible = false;
                TransitionSet set_turn = new TransitionSet()
                        .addTransition(new Scale(0.7f))
                        .addTransition(new Fade())
                        .setInterpolator(visible ? new LinearOutSlowInInterpolator() :
                                new FastOutLinearInInterpolator());

                TransitionManager.beginDelayedTransition(turn_signals_NOPULSE, set_turn);
                turn_signals_NOPULSE.setVisibility(visible ? View.GONE : View.VISIBLE);

                // left button selected
                leftButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        Context context = getActivity();
                        CharSequence text = "Left Turn";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();

                        // writing out to the bluetooth
                        ((MainActivity)getActivity()).requestPriority("left");

                        visible = true;
                        TransitionSet set_button = new TransitionSet()
                                .addTransition(new Scale(0.7f))
                                .addTransition(new Fade())
                                .setInterpolator(visible ? new LinearOutSlowInInterpolator() :
                                        new FastOutLinearInInterpolator());

                        TransitionManager.beginDelayedTransition(right_NOPULSE, set_button);
                        rightButton.setVisibility(visible ? View.GONE : View.VISIBLE);

                        visible = true;
                        TransitionSet set_button_again = new TransitionSet()
                                .addTransition(new Scale(0.7f))
                                .addTransition(new Fade())
                                .setInterpolator(visible ? new LinearOutSlowInInterpolator() :
                                        new FastOutLinearInInterpolator());

                        TransitionManager.beginDelayedTransition(up_NOPULSE, set_button_again);
                        upButton.setVisibility(visible ? View.GONE : View.VISIBLE);

                        final Handler handler1 = new Handler();
                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                turn_signals_NOPULSE.setVisibility(View.GONE);
                                pulsator_up.setVisibility(View.INVISIBLE);
                                pulsator_right.setVisibility(View.INVISIBLE);
                                turn_signals.setVisibility(View.VISIBLE);
                            }
                        }, 250);

                        final Handler handler2 = new Handler();
                        handler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                               s_restart();
                            }
                        }, 5000);

                    }
                });

                upButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        Context context = getActivity();
                        CharSequence text = "Straight";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();

                        ((MainActivity)getActivity()).requestPriority("up");

                        visible = true;
                        TransitionSet set_button = new TransitionSet()
                                .addTransition(new Scale(0.7f))
                                .addTransition(new Fade())
                                .setInterpolator(visible ? new LinearOutSlowInInterpolator() :
                                        new FastOutLinearInInterpolator());

                        TransitionManager.beginDelayedTransition(left_NOPULSE, set_button);
                        leftButton.setVisibility(visible ? View.GONE : View.VISIBLE);

                        visible = true;
                        TransitionSet set_button_again = new TransitionSet()
                                .addTransition(new Scale(0.7f))
                                .addTransition(new Fade())
                                .setInterpolator(visible ? new LinearOutSlowInInterpolator() :
                                        new FastOutLinearInInterpolator());

                        TransitionManager.beginDelayedTransition(right_NOPULSE, set_button_again);
                        rightButton.setVisibility(visible ? View.GONE : View.VISIBLE);

                        final Handler handler1 = new Handler();
                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                turn_signals_NOPULSE.setVisibility(View.GONE);
                                pulsator_left.setVisibility(View.INVISIBLE);
                                pulsator_right.setVisibility(View.INVISIBLE);
                                turn_signals.setVisibility(View.VISIBLE);
                            }
                        }, 250);

                        final Handler handler2 = new Handler();
                        handler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                s_restart();
                            }
                        }, 5000);

                    }
                });

                rightButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        Context context = getActivity();
                        CharSequence text = "Right Turn";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();

                        ((MainActivity)getActivity()).requestPriority("right");

                        visible = true;
                        TransitionSet set_button = new TransitionSet()
                                .addTransition(new Scale(0.7f))
                                .addTransition(new Fade())
                                .setInterpolator(visible ? new LinearOutSlowInInterpolator() :
                                        new FastOutLinearInInterpolator());

                        TransitionManager.beginDelayedTransition(left_NOPULSE, set_button);
                        leftButton.setVisibility(visible ? View.GONE : View.VISIBLE);

                        visible = true;
                        TransitionSet set_button_again = new TransitionSet()
                                .addTransition(new Scale(0.7f))
                                .addTransition(new Fade())
                                .setInterpolator(visible ? new LinearOutSlowInInterpolator() :
                                        new FastOutLinearInInterpolator());

                        TransitionManager.beginDelayedTransition(up_NOPULSE, set_button_again);
                        upButton.setVisibility(visible ? View.GONE : View.VISIBLE);


                        final Handler handler1 = new Handler();
                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                turn_signals_NOPULSE.setVisibility(View.GONE);
                                pulsator_left.setVisibility(View.INVISIBLE);
                                pulsator_up.setVisibility(View.INVISIBLE);
                                turn_signals.setVisibility(View.VISIBLE);
                            }
                        }, 250);

                        final Handler handler2 = new Handler();
                        handler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                s_restart();
                            }
                        }, 5000);
                    }
                });


            }

        }
    }

    // first part of resetting the cycle that prompts whether the priority request was accepted or denied
    public void s_restart() {

        leftButton.setVisibility(View.VISIBLE);
        upButton.setVisibility(View.VISIBLE);
        rightButton.setVisibility(View.VISIBLE);

        visible = true;
        TransitionSet set = new TransitionSet()
                .addTransition(new Scale(0.7f))
                .addTransition(new Fade())
                .setInterpolator(visible ? new LinearOutSlowInInterpolator() :
                        new FastOutLinearInInterpolator());

        TransitionManager.beginDelayedTransition(turn_signals, set);
        turn_signals.setVisibility(visible ? View.GONE : View.VISIBLE);

        approve.setVisibility(View.GONE);
        declined.setVisibility(View.GONE);
        result.setVisibility(View.VISIBLE);

        everyOther = !everyOther;

        // calls the function to display the correct approve or deny icons
        result(everyOther);
    }

    public void result(Boolean access) {

        final View myView;

        if(access) {
            myView = approve;
        }
        else {
            myView = declined;
        }

        // get the center for the clipping circle
        int cx = myView.getWidth() / 2;
        int cy = myView.getHeight() / 2;

        // get the final radius for the clipping circle
        float finalRadius = (float) Math.hypot(cx, cy);

        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        myView.setVisibility(View.VISIBLE);
        anim.start();

        myView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // get the center for the clipping circle
                int cx = myView.getWidth() / 2;
                int cy = myView.getHeight() / 2;

                // get the initial radius for the clipping circle
                float initialRadius = (float) Math.hypot(cx, cy);

                // create the animation (the final radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        myView.setVisibility(View.GONE);
                    }
                });

                // start the animation
                anim.start();

                // after the animation finishes, wait a 150 milliseconds before actually restarting the cycle
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        f_restart();
                    }
                }, 150);

            }
        });

    }

    // this method finishes restarting the cycle
    public void f_restart() {

        result.setVisibility(View.GONE);
        request_button_content.setVisibility(View.VISIBLE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pulsator_left.setVisibility(View.VISIBLE);
                pulsator_up.setVisibility(View.VISIBLE);
                pulsator_right.setVisibility(View.VISIBLE);

                visible = false;
                TransitionSet set_request = new TransitionSet()
                        .addTransition(new Scale(0.7f))
                        .addTransition(new Fade())
                        .setInterpolator(visible ? new LinearOutSlowInInterpolator() :
                                new FastOutLinearInInterpolator());

                TransitionManager.beginDelayedTransition(request_button_content, set_request);
                btnClick.setVisibility(visible ? View.GONE : View.VISIBLE);
            }
        }, 250);
    }
}
