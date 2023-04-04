package fr.univnantes.pmc.project.api;

/**
 * Interface for a register
 *
 * @param <T> the type of the value
 */
public interface Register<T> {

    /**
     * Get the value of the register
     *
     * @return the value of the register
     */
    T getValue();

    /**
     * Get the date of the last write in the register
     *
     * @return the date of the last write in the register
     */
    long getDate();

    /**
     * Read the value of the register
     *
     * @param t the transaction that wants to read the register
     * @return the value of the register
     * @throws AbortException if the transaction is aborted
     */
    T read(Transaction t) throws AbortException;

    /**
     * Write a value in the register
     *
     * @param t the transaction that wants to write the register
     * @param v the value to write
     * @throws AbortException if the transaction is aborted
     */
    void write(Transaction t, T v) throws AbortException;

    /**
     * Commit the value of the register
     *
     * @param t          the transaction that wants to commit the register
     * @param commitDate the date of the commit
     * @throws AbortException if the transaction is aborted
     */
    void commit(Transaction t, int commitDate) throws AbortException;

    /**
     * Lock the register
     *
     * @param t the transaction that wants to lock the register
     * @throws AbortException if the transaction is aborted
     */
    void lock(Transaction t) throws AbortException;

    /**
     * Unlock the register
     *
     * @param t the transaction that wants to unlock the register
     * @throws AbortException if the transaction is aborted
     */
    void unlock(Transaction t) throws AbortException;
}