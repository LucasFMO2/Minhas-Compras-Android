package com.example.minhascompras.ui.viewmodel

import com.example.minhascompras.data.ItemCompra
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.data.SortOrder
import com.example.minhascompras.data.UserPreferencesManager
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListaComprasViewModelTest {

    @MockK(relaxed = true)
    lateinit var repository: ItemCompraRepository

    @MockK(relaxed = true)
    lateinit var preferencesManager: UserPreferencesManager

    private val dispatcher = StandardTestDispatcher()

    private lateinit var itensFlow: MutableStateFlow<List<ItemCompra>>
    private lateinit var sortOrderFlow: MutableStateFlow<SortOrder>

    private lateinit var viewModel: ListaComprasViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)

        itensFlow = MutableStateFlow(emptyList())
        sortOrderFlow = MutableStateFlow(SortOrder.BY_DATE_DESC)

        every { repository.allItens } returns itensFlow
        every { repository.getFilteredItens(any(), any(), any()) } answers { itensFlow }
        coEvery { repository.update(any()) } returns Unit
        coEvery { repository.deleteComprados() } returns Unit
        coEvery { repository.insert(any()) } returns 0L

        every { preferencesManager.sortOrder } returns sortOrderFlow
        coEvery { preferencesManager.setSortOrder(any()) } returns Unit

        viewModel = ListaComprasViewModel(repository, preferencesManager)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `arquivarLista arquiva itens e emite mensagem de sucesso`() = runTest(dispatcher) {
        val collectJob = launch { viewModel.allItens.collect { } }
        advanceUntilIdle()

        val itens = listOf(
            ItemCompra(id = 1, nome = "Arroz", comprado = true),
            ItemCompra(id = 2, nome = "Feijão", comprado = true)
        )
        itensFlow.value = itens
        advanceUntilIdle()
        coEvery { repository.archiveCurrentList(any()) } returns Unit

        val messageDeferred = async { viewModel.uiMessages.first() }
        viewModel.arquivarLista()
        advanceUntilIdle()
        val message = messageDeferred.await()
        val capturedItens = slot<List<ItemCompra>>()
        coVerify(exactly = 1) { repository.archiveCurrentList(capture(capturedItens)) }
        val enviados = capturedItens.captured
        assertEquals(itens.size, enviados.size)
        assertTrue(enviados.containsAll(itens))
        assertFalse(viewModel.isArchiving.value)
        assertTrue(message is ListaComprasViewModel.UiMessage.Success)
        assertEquals("Lista arquivada com sucesso!", message.message)
        collectJob.cancel()
    }

    @Test
    fun `arquivarLista com lista vazia nao chama repository e avisa usuario`() = runTest(dispatcher) {
        val collectJob = launch { viewModel.allItens.collect { } }
        advanceUntilIdle()

        itensFlow.value = emptyList()
        advanceUntilIdle()

        val messageDeferred = async { viewModel.uiMessages.first() }
        viewModel.arquivarLista()
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.archiveCurrentList(any()) }
        val message = messageDeferred.await()
        assertTrue(message is ListaComprasViewModel.UiMessage.Info)
        assertEquals("Não há itens para arquivar.", message.message)
        assertFalse(viewModel.isArchiving.value)
        collectJob.cancel()
    }

    @Test
    fun `arquivarLista com erro emite mensagem de falha e reseta estado`() = runTest(dispatcher) {
        val collectJob = launch { viewModel.allItens.collect { } }
        advanceUntilIdle()

        val itens = listOf(ItemCompra(id = 3, nome = "Leite", comprado = true))
        itensFlow.value = itens
        advanceUntilIdle()
        coEvery { repository.archiveCurrentList(any()) } throws RuntimeException("falha")

        val messageDeferred = async { viewModel.uiMessages.first() }
        viewModel.arquivarLista()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.archiveCurrentList(any()) }
        val message = messageDeferred.await()
        assertTrue(message is ListaComprasViewModel.UiMessage.Error)
        assertEquals("Erro ao arquivar lista. Tente novamente.", message.message)
        assertFalse(viewModel.isArchiving.value)
        collectJob.cancel()
    }
}

