package com.hanumanchalisa.counter.presentation.state

/**
 * The outcome of the most recent import, waiting to be shown to the user.
 *
 * Every failure case leaves the counts exactly as they were, and each message says so: the user
 * should never have to wonder whether a rejected file damaged their practice record.
 */
sealed interface ImportStatus {

    /** The counts were written; the new total is quoted in the confirmation. */
    data class Succeeded(val totalCount: Int, val targetCount: Int) : ImportStatus

    /** The file's counts already match this phone, so there was nothing to do. */
    data object AlreadyUpToDate : ImportStatus

    /** The chosen file is not a Chalisa Counter export. */
    data object NotAnExport : ImportStatus

    /** The file looks like an export but has been edited or is incomplete. */
    data object Damaged : ImportStatus

    /** A valid export whose days are all zero. */
    data object NothingToImport : ImportStatus

    /** The file could not be opened or read. */
    data object Unreadable : ImportStatus

    /** The file was fine but saving the counts failed. */
    data object Failed : ImportStatus
}
