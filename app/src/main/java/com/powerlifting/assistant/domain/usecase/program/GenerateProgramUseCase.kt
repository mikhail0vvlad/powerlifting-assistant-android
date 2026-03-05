package com.powerlifting.assistant.domain.usecase.program

import com.powerlifting.assistant.domain.model.ProgramSchedule
import com.powerlifting.assistant.domain.repository.ProgramRepository
import java.time.LocalDate
import javax.inject.Inject

class GenerateProgramUseCase @Inject constructor(
    private val programRepository: ProgramRepository
) {
    suspend operator fun invoke(
        startDate: LocalDate? = null,
        weeks: Int? = null,
        schedule: ProgramSchedule? = null
    ) {
        programRepository.generate(startDate, weeks, schedule)
    }
}
