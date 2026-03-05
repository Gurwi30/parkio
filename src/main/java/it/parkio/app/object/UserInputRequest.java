package it.parkio.app.object;

import java.util.function.Consumer;

public class UserInputRequest<T> { // GESTISCE GLI INPUT DALL'INTERFACCIA UTENTE, T OGGETTO GENERICO

    private final Runnable defaultCancelAction; // Azione di default eseguita quando si cancella

    private Consumer<T> inputHandler; // Callback quando arriva un input valido
    private Runnable cancelHandler; // Callback quando viene cancellato
    private Consumer<Throwable> exceptionHandler; // Callback per eccezioni
    private Runnable finishHandler; // Callback eseguita sempre alla fine

    private boolean completed = false;
    private boolean cancelled = false;
    private boolean finished = false;

    private T value; // Valore ricevuto dall’input

    public UserInputRequest(Runnable cancelAction) { // Costruttore con azione cancel custom
        this.defaultCancelAction = cancelAction;
    }

    public UserInputRequest() { // Costruttore senza azione cancel
        this(() -> {});
    }

    public UserInputRequest<T> onInput(Consumer<T> handler) { // DEFINISCE AZIONI DA ESEGUIRE QUANDO SI OTTIENE UN INPUT
        this.inputHandler = handler;

        if (completed && !cancelled) invokeInput(); // Se già completato, esegue subito

        return this;
    }

    public UserInputRequest<T> onCancel(Runnable handler) {
        this.cancelHandler = handler;

        if (cancelled) invokeCancel();

        return this;
    }

    public UserInputRequest<T> onException(Consumer<Throwable> handler) { // Callback eccezioni
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

    public void complete(T value) { // Completa con valore
        if (completed || cancelled) return; // Ignora se già completato/cancellato

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

    private void invokeInput() { // Esegue handler input
        if (inputHandler != null) inputHandler.accept(value);
    }

    private void invokeCancel() { // Esegue azioni cancel
        defaultCancelAction.run();
        if (cancelHandler != null) cancelHandler.run();
    }

    private void finish() { // Segnala fine
        if (finished) return;
        finished = true;

        if (finishHandler != null) finishHandler.run();
    }

    private void handleException(Throwable t) { // Gestione eccezioni
        if (exceptionHandler != null) exceptionHandler.accept(t);
        t.printStackTrace();
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isCancelled() {
        return cancelled;
    }

}