package cz.anty.utils.thread;

/**
 * Created by anty on 29.6.15.
 *
 * @author anty
 */
public interface ProgressReporter {

    void setMaxProgress(int max);

    void reportProgress(int progress);
}
