package it.parkio.app.object;

import java.util.function.Consumer;

/**
 * Rappresenta una richiesta di input asincrona proveniente dall'interfaccia utente.
 *
 * <p>Questa classe è utile quando una certa operazione non produce subito un risultato,
 * ma deve attendere un'azione dell'utente, per esempio il disegno di un'area sulla mappa.</p>
 *
 * <p>Supporta quattro momenti principali:</p>
 * <ul>
 *     <li>arrivo di un input valido;</li>
 *     <li>annullamento;</li>
 *     <li>gestione di eventuali eccezioni;</li>
 *     <li>chiusura finale della richiesta.</li>
 * </ul>
 *
 * @param <T> tipo del valore atteso come risultato dell'input
 */
public class UserInputRequest<T> {

    /**
     * Azione di default da eseguire quando la richiesta viene annullata.
     *
     * <p>È utile per pulire stato grafico, cursori, overlay o risorse temporanee.</p>
     */
    private final Runnable defaultCancelAction;

    /**
     * Handler chiamato quando la richiesta viene completata con un valore valido.
     */
    private Consumer<T> inputHandler;

    /**
     * Handler chiamato quando la richiesta viene annullata.
     */
    private Runnable cancelHandler;

    /**
     * Handler chiamato in caso di eccezioni durante l'esecuzione dei callback.
     */
    private Consumer<Throwable> exceptionHandler;

    /**
     * Handler eseguito sempre al termine della richiesta, sia in caso di successo
     * sia in caso di annullamento.
     */
    private Runnable finishHandler;

    /**
     * Indica se la richiesta è stata completata con successo.
     */
    private boolean completed = false;

    /**
     * Indica se la richiesta è stata annullata.
     */
    private boolean cancelled = false;

    /**
     * Indica se la richiesta è già stata chiusa definitivamente.
     */
    private boolean finished = false;

    /**
     * Valore finale ottenuto dall'input dell'utente.
     */
    private T value;

    /**
     * Crea una richiesta con un'azione personalizzata da eseguire in caso di annullamento.
     *
     * @param cancelAction azione di default di cleanup o rollback
     */
    public UserInputRequest(Runnable cancelAction) {
        this.defaultCancelAction = cancelAction;
    }

    /**
     * Crea una richiesta senza alcuna azione di annullamento predefinita.
     */
    public UserInputRequest() {
        this(() -> {});
    }

    /**
     * Registra il callback da eseguire quando arriva un input valido.
     *
     * <p>Se la richiesta è già stata completata prima della registrazione,
     * il callback viene eseguito immediatamente. Questo rende la classe
     * più flessibile e simile a una piccola promessa/evento differito.</p>
     *
     * @param handler callback di gestione del valore
     * @return la richiesta stessa, per concatenare le chiamate
     */
    public UserInputRequest<T> onInput(Consumer<T> handler) {
        this.inputHandler = handler;

        if (completed && !cancelled) invokeInput();

        return this;
    }

    /**
     * Registra il callback da eseguire in caso di annullamento.
     *
     * <p>Se la richiesta è già stata annullata, il callback viene invocato subito.</p>
     *
     * @param handler callback di annullamento
     * @return la richiesta stessa
     */
    public UserInputRequest<T> onCancel(Runnable handler) {
        this.cancelHandler = handler;

        if (cancelled) invokeCancel();

        return this;
    }

    /**
     * Registra il callback per la gestione centralizzata delle eccezioni.
     *
     * @param handler callback per gli errori
     * @return la richiesta stessa
     */
    public UserInputRequest<T> onException(Consumer<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    /**
     * Registra il callback finale eseguito sempre al termine della richiesta.
     *
     * <p>Se la richiesta è già terminata, il callback viene eseguito subito.</p>
     *
     * @param handler callback finale
     * @return la richiesta stessa
     */
    public UserInputRequest<T> onFinish(Runnable handler) {
        this.finishHandler = handler;

        if (finished) handler.run();

        return this;
    }

    /**
     * Annulla la richiesta, se non è già stata completata o annullata.
     *
     * <p>Viene eseguita prima la logica di annullamento, poi il callback finale.</p>
     */
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

    /**
     * Completa la richiesta con un valore valido.
     *
     * <p>Se la richiesta è già chiusa, la chiamata viene ignorata.</p>
     *
     * @param value valore ottenuto dall'utente
     */
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

    /**
     * Invoca il callback di input, se presente.
     */
    private void invokeInput() {
        if (inputHandler != null) inputHandler.accept(value);
    }

    /**
     * Invoca la logica di annullamento:
     * prima l'azione di default, poi l'eventuale callback personalizzato.
     */
    private void invokeCancel() {
        defaultCancelAction.run();
        if (cancelHandler != null) cancelHandler.run();
    }

    /**
     * Segna la richiesta come terminata ed esegue l'handler finale una sola volta.
     */
    private void finish() {
        if (finished) return;
        finished = true;

        if (finishHandler != null) finishHandler.run();
    }

    /**
     * Gestisce un'eccezione generata dai callback.
     *
     * <p>Se è stato registrato un gestore dedicato, viene invocato.
     * In ogni caso l'eccezione viene stampata per facilitare il debug.</p>
     *
     * @param t eccezione intercettata
     */
    private void handleException(Throwable t) {
        if (exceptionHandler != null) exceptionHandler.accept(t);
        t.printStackTrace();
    }

    /**
     * @return {@code true} se la richiesta è stata completata con successo
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * @return {@code true} se la richiesta è stata annullata
     */
    public boolean isCancelled() {
        return cancelled;
    }

}