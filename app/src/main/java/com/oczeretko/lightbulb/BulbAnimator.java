package com.oczeretko.lightbulb;

import android.content.*;
import android.os.*;

public class BulbAnimator implements BulbController.StatusChangedListener {

    private static final int WHAT_FINISH = -123;
    private final BulbController controller;
    private final int startLevel;
    private final int endLevel;
    private final long time;

    private final int animationSteps;
    private final Handler handler;
    private boolean doneScheduling;

    private BulbAnimator(Context context, BulbController controller, int startLevel, int endLevel, long time) {
        this.controller = controller;
        this.startLevel = startLevel;
        this.endLevel = endLevel;
        this.time = time;
        handler = new Handler(context.getMainLooper(), this::handleCommandMessage);
        animationSteps = context.getResources().getInteger(R.integer.bulbanimator_animation_steps);
        controller.addListener(this);
    }

    public static BulbAnimator startAnimating(Context context, BulbController controller, int startLevel, int endLevel, long time) {
        BulbAnimator animator = new BulbAnimator(context, controller, startLevel, endLevel, time);
        animator.animate();
        return animator;
    }

    private void animate() {
        controller.setLevel(startLevel);

        if (controller.getStatus() == BulbController.Status.Connected) {
            doneScheduling = true;
            scheduleAnimation();
        } else {
            doneScheduling = false;
        }
    }

    private void scheduleAnimation() {
        for (int i = 2; i <= animationSteps; i++) {
            int level = (int)((double)i * (endLevel - startLevel) / animationSteps + startLevel);
            long delay = (long)((double)(i - 1) / animationSteps * time);
            handler.sendEmptyMessageDelayed(level, delay);
        }
        handler.sendEmptyMessageDelayed(WHAT_FINISH, time + 1);
    }

    private boolean handleCommandMessage(Message message) {
        if (message.what == WHAT_FINISH) {
            cancel();
        } else {
            int level = message.what;
            controller.setLevel(level);
        }

        return true;
    }

    public void cancel() {
        handler.removeCallbacksAndMessages(null);
        controller.removeListener(this);
    }

    @Override
    public void onStatusChanged(BulbController.Status newStatus) {
        if (newStatus == BulbController.Status.Disconnected) {
            cancel();
        } else if (newStatus == BulbController.Status.Connected && !doneScheduling) {
            doneScheduling = true;
            scheduleAnimation();
        }
    }
}
