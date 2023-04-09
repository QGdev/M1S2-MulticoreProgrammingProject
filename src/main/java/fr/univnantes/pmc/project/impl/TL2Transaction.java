package fr.univnantes.pmc.project.impl;

import fr.univnantes.pmc.project.api.AbortException;
import fr.univnantes.pmc.project.api.Register;
import fr.univnantes.pmc.project.api.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Our implementation of the TL2 transaction
 *
 * @author Quentin GOMES DOS REIS
 * @author Matthéo LÉCRIVAIN
 * @see <a href="https://hal.inria.fr/hal-00646909">"Software Transactional Memories: An Approach for Multicore Programming"</a>
 */
public class TL2Transaction implements Transaction {

    private static final AtomicLong CLOCK = new AtomicLong(0L);
    private final List<Register<?>> localReadingSet = new ArrayList<>();
    private final List<Register<?>> localWritingSet = new ArrayList<>();
    private boolean isCommitted = false;
    private long commitDate = 0L;
    private long birthdate;


    /**
     * Begin the transaction
     */
    @Override
    public void begin() {
        localReadingSet.clear();
        localWritingSet.clear();
        birthdate = CLOCK.get();
    }


    /**
     * Try to commit the transaction
     *
     * @throws AbortException if the transaction is aborted
     */
    @Override
    public void tryToCommit() throws AbortException {
        isCommitted = false;

        // We lock all the write registers
        lockLWS();

        // We check the coherence of the transaction
        for (Register<?> register : localReadingSet) {
            if (register.getDate() > birthdate) {
                releaseLocks();
                throw new AbortException("TL2Transaction - tryToCommit : Incoherence between the birthdate of the transaction and the date of the register");
            }
        }

        // Write down the committed date and increment it for the next transaction
        commitDate = CLOCK.getAndIncrement();

        // We commit all the write registers
        for (Register<?> register : localWritingSet) {
            register.commit(this, commitDate);
        }

        // We release all the locks and set the transaction as committed
        releaseLocks();
        isCommitted = true;
    }


    /**
     * Check if the transaction is committed
     *
     * @return true if the transaction is committed, false otherwise
     */
    @Override
    public boolean isCommitted() {
        return isCommitted;
    }

    /**
     * Add a register to the list of read registers
     *
     * @param register the register to add
     */
    @Override
    public void addToLWS(Register<?> register) {
        this.localWritingSet.add(register);
    }


    /**
     * Add a register to the list of written registers
     *
     * @param register the register to add
     */
    @Override
    public void addToLRS(Register<?> register) {
        this.localReadingSet.add(register);
    }


    /**
     * Get the birthdate of the transaction
     *
     * @return the birthdate of the transaction
     */
    @Override
    public long getBirthdate() {
        return birthdate;
    }


    /**
     * Release all the locks
     *
     * @throws AbortException if a register is already locked by another transaction
     */
    private void releaseLocks() throws AbortException {
        boolean hasBeenAborted = false;

        // We release all the locks
        // If a register is already locked by another
        // transaction, we throw an abort exception,
        // and we will continue to release the other locks
        for (Register<?> register : localReadingSet) {
            try {
                register.unlock(this);
            } catch (AbortException e) {
                // We set the boolean to true to throw
                // an abort exception after every register
                // has been processed
                hasBeenAborted = true;
            }
        }
        if (hasBeenAborted)
            throw new AbortException("TL2Transaction - releaseAllLocks : some register are already locked by other transaction");
    }


    /**
     * Lock all the registers in lws
     *
     * @throws AbortException if a register is already locked by another transaction
     */
    private void lockLWS() throws AbortException {
        try {
            // We lock all the registers in lws
            for (Register<?> register : localWritingSet) {
                register.lock(this);
            }
        } catch (AbortException e) {
            // If we have an abort exception, a register is
            // already locked, so we must release all the locks
            releaseLocks();
            throw new AbortException("TL2Transaction - lockLWS : a register is already locked by another transaction");
        }
    }
}