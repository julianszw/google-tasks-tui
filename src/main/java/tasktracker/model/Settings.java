package tasktracker.model;

public class Settings {

    private boolean hideEmptyLists;
    private boolean hideCompletedTasks;

    public boolean isHideEmptyLists() {
        return hideEmptyLists;
    }

    public void setHideEmptyLists(boolean hideEmptyLists) {
        this.hideEmptyLists = hideEmptyLists;
    }

    public boolean isHideCompletedTasks() {
        return hideCompletedTasks;
    }

    public void setHideCompletedTasks(boolean hideCompletedTasks) {
        this.hideCompletedTasks = hideCompletedTasks;
    }
}
