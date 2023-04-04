package fr.univnantes.pmc.project.api;

/**
 * Interface for a transaction
 *
 * @author Quentin GOMES DOS REIS
 * @author Matthéo LÉCRIVAIN
 */
public interface Transaction {

    /**
     * Begin the transaction
     */
    void begin();

    /**
     * Try to commit the transaction
     *
     * @throws AbortException if the transaction is aborted
     */
    void tryToCommit() throws AbortException;

    /**
     * Check if the transaction is committed
     *
     * @return true if the transaction is committed, false otherwise
     */
    boolean isCommitted();

    /**
     * Add a register to the list of read registers
     *
     * @param register the register to add
     */
    void addToLWS(Register<?> register);

    /**
     * Add a register to the list of written registers
     *
     * @param register the register to add
     */
    void addToLRS(Register<?> register);

    /**
     * Get the birthdate of the transaction
     *
     * @return the birthdate of the transaction
     */
    long getBirthdate();

}