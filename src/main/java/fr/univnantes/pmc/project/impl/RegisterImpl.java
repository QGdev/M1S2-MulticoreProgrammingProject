package fr.univnantes.pmc.project.impl;

import fr.univnantes.pmc.project.api.AbortException;
import fr.univnantes.pmc.project.api.Register;
import fr.univnantes.pmc.project.api.Transaction;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Our implementation of the register
 *
 * @param <T> the type of the value of the register
 * @author Quentin GOMES DOS REIS
 * @author Matthéo LÉCRIVAIN
 */
public class RegisterImpl<T> implements Register<T> {

    /**
     * Our implementation of the register copy
     *
     * @param <T> the type of the value of the register
     */
    private static final class RegisterCopy<T> {
        T value;
        long date;

        /**
         * Create a new register copy
         *
         * @param value the value of the register
         * @param date  the date of the register
         */
        public RegisterCopy(T value, long date) {
            this.value = value;
            this.date = date;
        }
    }


    private final AtomicReference<T> value;
    private long date;
    private Transaction transactionLock = null;

    private RegisterCopy<T> lcx = null;

    /**
     * Create a new register
     *
     * @param value the value of the register
     * @param date  the date of the register
     */
    public RegisterImpl(T value, long date) {
        this.value = new AtomicReference<T>(value);
        this.date = date;
    }


    /**
     * Get the value of the register
     *
     * @return the value of the register
     */
    @Override
    public T getValue() {
        return value.get();
    }

    /**
     * Get the date of the last write in the register
     *
     * @return the date of the last write in the register
     */
    @Override
    public long getDate() {
        return date;
    }

    /**
     * Read the value of the register
     *
     * @param transaction the transaction that wants to read the register
     * @return the value of the register
     * @throws AbortException if the transaction is aborted
     */
    @Override
    public T read(Transaction transaction) throws AbortException {
        if (lcx != null)
            return lcx.value;

        //  No local copy ? Create one
        lcx = new RegisterCopy<>(value.get(), date);
        transaction.addToLRS(this);

        //  Check for inconsistencies
        if (lcx.date > transaction.getBirthdate())
            throw new AbortException();
        return lcx.value;
    }

    /**
     * Write a value in the register
     *
     * @param t the transaction that wants to write the register
     * @param v the value to write
     * @throws AbortException if the transaction is aborted
     */
    @Override
    public void write(Transaction t, T v) throws AbortException {
        //  No local copy ? Create one
        if (lcx == null)
            lcx = new RegisterCopy<T>(value.get(), date);

        lcx.value = v;
        t.addToLRS(this);
    }

    /**
     * Commit the value of the register
     *
     * @param transaction the transaction that wants to commit the register
     * @param commitDate  the date of the commit
     * @throws AbortException if the transaction is aborted
     */
    @Override
    public void commit(Transaction transaction, long commitDate) throws AbortException {
        //  Check if the transaction is the one that locked the register
        if (transactionLock != transaction)
            throw new AbortException("TL2Transaction - commit: Already locked by another transaction");

        this.date = commitDate;
        this.value.set(lcx.value);
        lcx = null;
    }

    /**
     * Lock the register
     *
     * @param transaction the transaction that wants to lock the register
     * @throws AbortException if the transaction is aborted
     */
    @Override
    public void lock(Transaction transaction) throws AbortException {
        //  Check if the register is already locked
        if (transactionLock != null)
            throw new AbortException("TL2Transaction - lock: Already locked by someone");

        transactionLock = transaction;
    }

    /**
     * Unlock the register
     *
     * @param transaction the transaction that wants to unlock the register
     * @throws AbortException if the transaction is aborted
     */
    @Override
    public void unlock(Transaction transaction) throws AbortException {
        //  Check if the transaction is the one that locked the register
        if (transactionLock != transaction)
            throw new AbortException("TL2Transaction - unlock: Locked by another transaction");

        transactionLock = null;
    }

    /**
     * Clone the register
     *
     * @return a copy of the register
     */
    @Override
    public RegisterImpl<T> clone() throws CloneNotSupportedException {
        return new RegisterImpl<T>(this.value.get(), this.date);
    }
}
