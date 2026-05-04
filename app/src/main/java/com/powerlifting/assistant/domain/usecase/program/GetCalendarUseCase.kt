package com.powerlifting.assistant.domain.usecase.program

import com.powerlifting.assistant.domain.model.TrainingCalendar
import com.powerlifting.assistant.domain.repository.ProgramRepository
import javax.inject.Inject

class GetCalendarUseCase @Inject constructor(
    private val programRepository: ProgramRepository
) {
    suspend operator fun invoke(from: String, to: String): TrainingCalendar =
        programRepository.calendar(from, to)
}
