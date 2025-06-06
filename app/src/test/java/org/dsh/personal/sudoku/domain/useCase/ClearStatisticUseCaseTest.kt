package org.dsh.personal.sudoku.domain.useCase


import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.repository.SudokuRepository
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ClearStatisticUseCaseTest {

    private lateinit var mockRepository: SudokuRepository
    private lateinit var clearStatisticUseCase: ClearStatisticUseCase

    @Before
    fun setUp() {
        mockRepository = mockk(relaxUnitFun = true) // relaxUnitFun for suspend fun returning Unit
        clearStatisticUseCase = ClearStatisticUseCase(mockRepository)
    }

    @Test
    fun `invoke calls repository clearStatistic`() = runTest {
        // Act
        clearStatisticUseCase() // Invokes the use case

        // Assert
        coVerify(exactly = 1) { mockRepository.clearStatistic() }
    }
}