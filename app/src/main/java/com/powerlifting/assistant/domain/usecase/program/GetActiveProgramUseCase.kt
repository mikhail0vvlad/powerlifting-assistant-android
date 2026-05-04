package com.powerlifting.assistant.domain.usecase.program

import com.powerlifting.assistant.domain.model.ActiveProgram
import com.powerlifting.assistant.domain.repository.ProgramRepository
import javax.inject.Inject

class GetActiveProgramUseCase @Inject constructor(
    private val programRepository: ProgramRepository
) {
    suspend operator fun invoke(): ActiveProgram = programRepository.getActive()
}
