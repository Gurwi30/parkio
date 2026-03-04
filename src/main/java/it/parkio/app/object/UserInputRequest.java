package it.parkio.app.object;

import java.util.function.Consumer;

public class UserInputRequest<T> {

    private Consumer<T> inputHandler;
    private Runnable cancelHandler;
    private Consumer<Throwable> exceptionHandler;

    private boolean completed = false;
    private boolean cancelled = false;

    private T value;

    public UserInputRequest<T> onInput(Consumer<T> handler) {
        this.inputHandler = handler;

        if (completed && !cancelled) {
            try {
                handler.accept(value);
            } catch (Throwable t) {
                handleException(t);
            }
        }

        return this;
    }

    public UserInputRequest<T> onCancel(Runnable handler) {
        this.cancelHandler = handler;

        if (cancelled) {
            try {
                handler.run();
            } catch (Throwable t) {
                handleException(t);
            }
        }

        return this;
    }

    public UserInputRequest<T> onException(Consumer<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    public void cancel() {
        if (completed || cancelled) return;

        cancelled = true;

        if (cancelHandler == null) return;

        try {
            cancelHandler.run();
        } catch (Throwable t) {
            handleException(t);
        }

    }

    public void complete(T value) {
        if (completed || cancelled) return;

        this.completed = true;
        this.value = value;

        if (inputHandler == null) return;

        try {
            inputHandler.accept(value);
        } catch (Throwable t) {
            handleException(t);
        }
    }

    private void handleException(Throwable t) {
        if (exceptionHandler == null) {
            t.printStackTrace();
            return;
        }

        exceptionHandler.accept(t);
    }


    public boolean isCompleted() {
        return completed;
    }

    public boolean isCancelled() {
        return cancelled;
    }

}