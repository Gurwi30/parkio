package it.parkio.app.object;

import java.util.function.Consumer;

public class UserInputRequest<T> {

    private final Runnable defaultCancelAction;

    private Consumer<T> inputHandler;
    private Runnable cancelHandler;
    private Consumer<Throwable> exceptionHandler;
    private Runnable finishHandler;

    private boolean completed = false;
    private boolean cancelled = false;
    private boolean finished = false;

    private T value;

    public UserInputRequest(Runnable cancelAction) {
        this.defaultCancelAction = cancelAction;
    }

    public UserInputRequest() {
        this(() -> {});
    }

    public UserInputRequest<T> onInput(Consumer<T> handler) {
        this.inputHandler = handler;

        if (completed && !cancelled) invokeInput();

        return this;
    }

    public UserInputRequest<T> onCancel(Runnable handler) {
        this.cancelHandler = handler;

        if (cancelled) invokeCancel();

        return this;
    }

    public UserInputRequest<T> onException(Consumer<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    public UserInputRequest<T> onFinish(Runnable handler) {
        this.finishHandler = handler;

        if (finished) handler.run();

        return this;
    }

    public void cancel() {
        if (completed || cancelled) return;

        cancelled = true;

        try {
            invokeCancel();
        } catch (Throwable t) {
            handleException(t);
        } finally {
            finish();
        }
    }

    public void complete(T value) {
        if (completed || cancelled) return;

        this.completed = true;
        this.value = value;

        try {
            invokeInput();
        } catch (Throwable t) {
            handleException(t);
        } finally {
            finish();
        }
    }

    private void invokeInput() {
        if (inputHandler != null) inputHandler.accept(value);
    }

    private void invokeCancel() {
        defaultCancelAction.run();
        if (cancelHandler != null) cancelHandler.run();
    }

    private void finish() {
        if (finished) return;
        finished = true;

        if (finishHandler != null) finishHandler.run();
    }

    private void handleException(Throwable t) {
        if (exceptionHandler != null) {
            exceptionHandler.accept(t);
        } else {
            t.printStackTrace();
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isCancelled() {
        return cancelled;
    }

}