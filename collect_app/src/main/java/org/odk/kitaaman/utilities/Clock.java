package org.odk.kitaaman.utilities;

/**
 * An object that exposes the current time to its client. Useful for decoupling
 * objects from static methods such as {@link System#currentTimeMillis()}.
 */
public interface Clock {

    long getCurrentTime();
}
