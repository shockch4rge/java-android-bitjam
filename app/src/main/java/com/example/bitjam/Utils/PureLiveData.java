package com.example.bitjam.Utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The whole point of this class is to initialise {@link MutableLiveData} objects with non-null values, to get rid of those pesky nullable lints!
 */
public class PureLiveData<@NotNull T> extends MutableLiveData<@NotNull T> {
    /* Credits to Zechariah Tan for this concept! */

    /**
     * @param value {@link NonNull} and {@link NotNull}
     *              All objects that are instantiated with this constructor MUST be given a value.
     *              For example:
     *              {@code PureLiveData<String> obj = new PureLiveData<>("")}
     */
    public PureLiveData(@NotNull @NonNull T value) {
        super(value);
    }

    /**
     * @param value The new value.
     */
    @Override
    public void postValue(@NonNull @NotNull T value) {
        super.postValue(value);
    }

    /**
     * @param value The new value.
     */
    @Override
    public void setValue(@NonNull @NotNull T value) {
        super.setValue(value);
    }

    /**
     * @return A value that must be {@link NotNull} or has been initialised beforehand.
     */
    @NonNull
    @NotNull
    @Override
    public T getValue() {
        return Objects.requireNonNull(super.getValue());
    }

    /**
     * @param owner    The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     */
    public void observe(@NotNull LifecycleOwner owner, @NotNull Observer<? super T> observer) {
        super.observe(owner, observer);
    }

    /**
     * Create a custom nested {@link Observer}.
     *
     * @param <T> The type of the parameter
     */
    public interface Observer<@NotNull T> extends androidx.lifecycle.Observer<@NotNull T> {
        @Override
        void onChanged(@NotNull @NonNull T t);
    }
}
