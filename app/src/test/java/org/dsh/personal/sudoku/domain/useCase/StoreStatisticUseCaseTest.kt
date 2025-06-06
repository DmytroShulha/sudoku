package org.dsh.personal.sudoku.domain.useCase

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.dsh.personal.sudoku.domain.repository.SudokuRepository
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class StoreStatisticUseCaseTest {

    private lateinit var mockRepository: SudokuRepository
    private lateinit var storeStatisticUseCase: StoreStatisticUseCase

    @Before
    fun setUp() {
        // relaxUnitFun = true because storeStatistic() is a suspend function returning Unit
        mockRepository = mockk(relaxUnitFun = true)
        storeStatisticUseCase = StoreStatisticUseCase(mockRepository)
    }

    @Test
    fun `invoke calls repository storeStatistic`() = runTest {
        // Act
        storeStatisticUseCase() // Invokes the use case

        // Assert
        // Verify that storeStatistic was called on the repository exactly once
        coVerify(exactly = 1) { mockRepository.storeStatistic() }
    }
}