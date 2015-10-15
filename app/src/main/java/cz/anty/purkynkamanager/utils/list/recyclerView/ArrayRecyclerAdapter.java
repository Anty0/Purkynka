package cz.anty.purkynkamanager.utils.list.recyclerView;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by anty on 29.9.15.
 *
 * @author anty
 */
public abstract class ArrayRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final Object mLock = new Object();
    private final ArrayList<T> mData = new ArrayList<>();

    private boolean mNotifyOnChange = true;

    public ArrayRecyclerAdapter() {

    }

    public ArrayRecyclerAdapter(Collection<? extends T> data) {
        addAllItems(data);
    }

    public ArrayRecyclerAdapter(T... data) {
        addAllItems(data);
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void addItem(T object) {
        int index = mData.size();
        synchronized (mLock) {
            mData.add(object);
        }
        if (mNotifyOnChange) {
            notifyItemInserted(index);
            onDataSetChanged();
        }
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void addAllItems(Collection<? extends T> collection) {
        int index = mData.size();
        synchronized (mLock) {
            mData.addAll(collection);
        }
        if (mNotifyOnChange) {
            notifyItemRangeInserted(index, collection.size());
            onDataSetChanged();
        }
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAllItems(T... items) {
        int index = mData.size();
        synchronized (mLock) {
            Collections.addAll(mData, items);
        }
        if (mNotifyOnChange) {
            notifyItemRangeInserted(index, items.length);
            onDataSetChanged();
        }
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index  The index at which the object must be inserted.
     */
    public void insertItem(T object, int index) {
        synchronized (mLock) {
            mData.add(index, object);
        }
        if (mNotifyOnChange) {
            notifyItemInserted(index);
            onDataSetChanged();
        }
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void removeItem(T object) {
        if (mNotifyOnChange) {
            int index = mData.indexOf(object);
            synchronized (mLock) {
                mData.remove(object);
            }
            notifyItemRemoved(index);
            onDataSetChanged();
            return;
        }
        synchronized (mLock) {
            mData.remove(object);
        }
    }

    /**
     * Remove all elements from the list.
     */
    public void clearItems() {
        int size = mData.size();
        synchronized (mLock) {
            mData.clear();
        }
        if (mNotifyOnChange) {
            notifyItemRangeRemoved(0, size);
            onDataSetChanged();
        }
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *                   in this adapter.
     */
    public void sortItems(Comparator<? super T> comparator) {
        int size = mData.size();
        synchronized (mLock) {
            Collections.sort(mData, comparator);
        }
        if (mNotifyOnChange) {
            notifyItemRangeChanged(0, size);
            onDataSetChanged();
        }
    }

    protected void onDataSetChanged() {

    }

    public boolean isNotifyOnChange() {
        return mNotifyOnChange;
    }

    /**
     * Control whether methods that change the list ({@link #addItem},
     * {@link #insertItem}, {@link #removeItem}, {@link #clearItems}) automatically call
     * {@link #notifyDataSetChanged} and {@link #onDataSetChanged}.  If set to false, caller must
     * manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     * <p/>
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link
     *                       #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    public boolean isEmpty() {
        synchronized (mLock) {
            return mData.isEmpty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        synchronized (mLock) {
            return mData.size();
        }
    }

    /**
     * {@inheritDoc}
     */
    public T getItem(int position) {
        synchronized (mLock) {
            return mData.get(position);
        }
    }


    public T[] getItems(T[] contents) {
        synchronized (mLock) {
            return mData.toArray(contents);
        }
    }

    public ArrayList<T> getItems() {
        synchronized (mLock) {
            return (ArrayList<T>) mData.clone();
        }
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getItemPosition(T item) {
        synchronized (mLock) {
            return mData.indexOf(item);
        }
    }
}
