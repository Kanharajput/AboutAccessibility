package com.example.learnaccessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Service;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import java.util.ArrayDeque;
import java.util.Deque;

public class GlobalActionBarService extends AccessibilityService {
    FrameLayout mLayout;        // storing the layout

    protected void onServiceConnected() {
        // Create an overlay and display the action bar
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayout = new FrameLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.action_bar, mLayout);
        wm.addView(mLayout, lp);

        configurePowerButton();    // to pop of the power off menu
        configureVolumeUp();        // let increase the volume it's party dude
        configureScrollButton();         // scroll if scrollable
        configureSwipeButton();         // swipe on the screen
    }

    private void configurePowerButton() {
        Button powerButton = mLayout.findViewById(R.id.power);
        powerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);     // global action are controlled by accessibility service
            }
        });
    }

    // without accessibility service we can make change in volume
    private void configureVolumeUp() {
        Button volumeUp =mLayout.findViewById(R.id.volume_up);
        volumeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                                AudioManager.ADJUST_RAISE,
                                                AudioManager.FLAG_SHOW_UI);
            }
        });
    }

    // AccessibilityNodeInfo is a tree of nodes(which have data regarding a view)
    // here we are finding which node contains scroll action and return that node
    // so we can scroll on display if no view have a scroll action then simply null(not gonna scroll)
    private AccessibilityNodeInfo findScrollableNode(AccessibilityNodeInfo root) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(root);
        while(!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();

            if(node.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node;
            }
            // if root not not contains scroll action then find in it's child same process for all nodes
            for(int i=0; i<node.getChildCount(); i++) {
                deque.addLast(node.getChild(i));
            }
        }
        return null;
    }

    // handle scroll button
    private void configureScrollButton() {
        Button scrollButton =mLayout.findViewById(R.id.scroll);
        scrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccessibilityNodeInfo scrollable = findScrollableNode(getRootInActiveWindow());   // find root node, send to findScrollableNode method
                if(scrollable != null) {
                    scrollable.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId());
                }
            }
        });
    }

    private void configureSwipeButton() {
        Button swipeButton = mLayout.findViewById(R.id.swipe);
        swipeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Path swipePath = new Path();
                swipePath.moveTo(1000,1000);
                swipePath.lineTo(100,100);
                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath,0,500));
                dispatchGesture(gestureBuilder.build(),null,null);
            }
        });
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }
}

