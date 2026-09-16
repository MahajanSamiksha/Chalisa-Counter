package com.hanumanchalisa.counter.di

import android.content.Context
import com.hanumanchalisa.counter.data.export.DocumentTextFileWriter
import com.hanumanchalisa.counter.data.export.PlainTextSadhanaReportFormat
import com.hanumanchalisa.counter.data.local.ChalisaDatabase
import com.hanumanchalisa.counter.data.repository.RoomChalisaCountRepository
import com.hanumanchalisa.counter.data.time.SystemTimeProvider
import com.hanumanchalisa.counter.domain.export.SadhanaReportFormat
import com.hanumanchalisa.counter.domain.export.TextFileWriter
import com.hanumanchalisa.counter.domain.export.TimeProvider
import com.hanumanchalisa.counter.domain.model.SadhanaConfig
import com.hanumanchalisa.counter.domain.repository.ChalisaCountRepository
import com.hanumanchalisa.counter.domain.usecase.DecrementDayCountUseCase
import com.hanumanchalisa.counter.domain.usecase.ExportSadhanaUseCase
import com.hanumanchalisa.counter.domain.usecase.IncrementDayCountUseCase
import com.hanumanchalisa.counter.domain.usecase.ObserveSadhanaProgressUseCase
import com.hanumanchalisa.counter.domain.usecase.ResetSadhanaUseCase

/**
 * The single place where concrete implementations are chosen and wired together.
 *
 * This is manual constructor injection — no Dagger or Hilt, which keeps the build small and the
 * wiring easy to read for an app this size. It still satisfies Dependency Inversion: every other
 * class receives its collaborators through its constructor and depends only on interfaces, so this
 * container is the only file that knows Room is the storage engine.
 *
 * Dependencies are created lazily, so the database file is opened on first use rather than at
 * app start-up.
 */
class AppContainer(
    context: Context,
    private val config: SadhanaConfig = SadhanaConfig.HANUMAN_CHALISA_40_DAYS,
) {
    private val appContext: Context = context.applicationContext

    private val database: ChalisaDatabase by lazy { ChalisaDatabase.create(appContext) }

    private val repository: ChalisaCountRepository by lazy {
        RoomChalisaCountRepository(database.dayCountDao())
    }

    val sadhanaConfig: SadhanaConfig get() = config

    val observeSadhanaProgress: ObserveSadhanaProgressUseCase by lazy {
        ObserveSadhanaProgressUseCase(repository, config)
    }

    val incrementDayCount: IncrementDayCountUseCase by lazy {
        IncrementDayCountUseCase(repository, config)
    }

    val decrementDayCount: DecrementDayCountUseCase by lazy {
        DecrementDayCountUseCase(repository, config)
    }

    val resetSadhana: ResetSadhanaUseCase by lazy {
        ResetSadhanaUseCase(repository)
    }

    private val reportFormat: SadhanaReportFormat by lazy { PlainTextSadhanaReportFormat() }

    private val textFileWriter: TextFileWriter by lazy { DocumentTextFileWriter(appContext) }

    private val timeProvider: TimeProvider by lazy { SystemTimeProvider() }

    val exportSadhana: ExportSadhanaUseCase by lazy {
        ExportSadhanaUseCase(
            reportFormat = reportFormat,
            fileWriter = textFileWriter,
            timeProvider = timeProvider,
        )
    }
}
