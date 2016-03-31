package org.flowerplatform.flowerino_plugin.util;

/**
 * A {@link Runnable} extension which is able to notify its user when it has started/ended running.
 * In addition, this class wraps around a real instance of {@link Runnable}.
 *
 * This is used when invoking various processes in the Arduino editor, in order to know when the process
 * has finished. It seems that the Arduino editor runs most of its tasks such as compiling, uploading to board, etc
 * by launching some threads.
 * 
 * @author Andrei Taras
 */
public class RunnableWithListener implements Runnable {
	private Runnable delegate;
	private StartEndListener startEndListener;
	
	public RunnableWithListener(Runnable delegate, StartEndListener startEndListener) {
		if (delegate == null) {
			throw new NullPointerException("Can't build runnable with null delegate.");
		}
		if (startEndListener == null) {
			throw new NullPointerException("Can't build runnable with null listener.");
		}
		
		this.delegate = delegate;
		this.startEndListener = startEndListener;
	}

	@Override
	public void run() {
		startEndListener.start();
		try {
			delegate.run();
		} finally {
			startEndListener.end();
		}
	}
}
