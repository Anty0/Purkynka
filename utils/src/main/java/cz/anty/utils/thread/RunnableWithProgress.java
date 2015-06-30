package cz.anty.utils.thread;

/**
 * Created by anty on 29.6.15.
 *
 * @author anty
 */
public interface RunnableWithProgress {

    String run(ProgressReporter reporter);
}
