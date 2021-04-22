package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;

public class ActionWait implements Action {

    long delay;

    public ActionWait(long delay) {
        this.delay = delay;
    }

    @Override
    public void perform(FakePlayer player) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ActionType getType() {
        return ActionType.WAIT;
    }

    public long getDelay() {
        return delay;
    }
}
