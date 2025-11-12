package com.example.minhascompras.data.supabase

import com.example.minhascompras.data.ItemCompra
import com.example.minhascompras.data.ItemCompraDao
import com.example.minhascompras.data.ShoppingListHistory
import com.example.minhascompras.data.HistoryDao
import com.example.minhascompras.data.SupabaseConfig
import com.example.minhascompras.utils.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

/**
 * Serviço de sincronização entre Room (local) e Supabase (remoto)
 * Implementa estratégia híbrida: Room como fonte de verdade local, Supabase como backup remoto
 */
class SupabaseSyncService(
    private val itemCompraDao: ItemCompraDao,
    private val historyDao: HistoryDao
) {
    private val supabase: SupabaseClient? = if (SupabaseConfig.isConfigured()) {
        SupabaseConfig.createClient()
    } else {
        null
    }

    /**
     * Sincroniza todos os itens locais para o Supabase
     * @param userId ID do usuário autenticado (null se não autenticado)
     */
    suspend fun syncAllItemsToSupabase(userId: String? = null): Result<Unit> {
        if (supabase == null || userId == null) {
            Logger.d(TAG, "Supabase não configurado ou usuário não autenticado")
            return Result.failure(Exception("Supabase não configurado ou usuário não autenticado"))
        }

        return try {
            val localItems = itemCompraDao.getAllItens().first()
            
            if (localItems.isEmpty()) {
                Logger.d(TAG, "Nenhum item local para sincronizar")
                return Result.success(Unit)
            }

            // Converter e enviar para Supabase
            val supabaseItems = localItems.map { it.toSupabase(userId) }
            
            // Deletar itens remotos do usuário e inserir os locais (upsert)
            try {
                supabase.postgrest.from("itens_compra")
                    .delete {
                        filter {
                            eq("user_id", userId)
                        }
                    }
            } catch (e: Exception) {
                // Ignorar erro se não houver itens para deletar
                Logger.d(TAG, "Nenhum item remoto para deletar")
            }
            
            if (supabaseItems.isNotEmpty()) {
                supabase.postgrest.from("itens_compra")
                    .insert(supabaseItems)
            }

            Logger.d(TAG, "Sincronizados ${supabaseItems.size} itens para Supabase")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Erro ao sincronizar itens para Supabase", e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza itens do Supabase para o Room (local)
     * @param userId ID do usuário autenticado
     */
    suspend fun syncItemsFromSupabase(userId: String): Result<Unit> {
        if (supabase == null) {
            Logger.d(TAG, "Supabase não configurado")
            return Result.failure(Exception("Supabase não configurado"))
        }

        return try {
            val remoteItems = supabase.postgrest
                .from("itens_compra")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("data_criacao", ascending = false)
                }
                .decodeList<ItemCompraSupabase>()

            // Converter para Room e salvar localmente
            val roomItems = remoteItems.map { it.toRoom() }
            
            if (roomItems.isNotEmpty()) {
                itemCompraDao.replaceAllItems(roomItems)
            }

            Logger.d(TAG, "Sincronizados ${roomItems.size} itens do Supabase")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Erro ao sincronizar itens do Supabase", e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza um único item para o Supabase
     */
    suspend fun syncItemToSupabase(item: ItemCompra, userId: String?): Result<Unit> {
        if (supabase == null || userId == null) {
            return Result.failure(Exception("Supabase não configurado ou usuário não autenticado"))
        }

        return try {
            val supabaseItem = item.toSupabase(userId)
            
            if (item.id > 0 && supabaseItem.id != null) {
                // Atualizar item existente
                supabase.postgrest.from("itens_compra")
                    .update(supabaseItem.copy(id = item.id)) {
                        filter {
                            eq("id", item.id)
                            eq("user_id", userId)
                        }
                    }
            } else {
                // Inserir novo item
                supabase.postgrest.from("itens_compra")
                    .insert(supabaseItem.copy(id = null))
            }

            Logger.d(TAG, "Item sincronizado: ${item.nome}")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Erro ao sincronizar item: ${item.nome}", e)
            Result.failure(e)
        }
    }

    /**
     * Remove um item do Supabase
     */
    suspend fun deleteItemFromSupabase(itemId: Long, userId: String?): Result<Unit> {
        if (supabase == null || userId == null) {
            return Result.failure(Exception("Supabase não configurado ou usuário não autenticado"))
        }

        return try {
            supabase.postgrest.from("itens_compra")
                .delete {
                    filter {
                        eq("id", itemId)
                        eq("user_id", userId)
                    }
                }
            
            Logger.d(TAG, "Item removido do Supabase: $itemId")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Erro ao remover item do Supabase: $itemId", e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza histórico para o Supabase
     */
    suspend fun syncHistoryToSupabase(history: ShoppingListHistory, userId: String?): Result<Long?> {
        if (supabase == null || userId == null) {
            return Result.failure(Exception("Supabase não configurado ou usuário não autenticado"))
        }

        return try {
            val supabaseHistory = history.toSupabase(userId)
            
            val result = if (history.id > 0 && supabaseHistory.id != null) {
                // Atualizar
                supabase.postgrest.from("shopping_list_history")
                    .update(supabaseHistory.copy(id = history.id)) {
                        filter {
                            eq("id", history.id)
                            eq("user_id", userId)
                        }
                    }
                history.id
            } else {
                // Inserir
                val inserted = supabase.postgrest.from("shopping_list_history")
                    .insert(supabaseHistory.copy(id = null)) {
                        select(Columns.ALL)
                    }
                    .decodeSingle<ShoppingListHistorySupabase>()
                inserted.id
            }

            Logger.d(TAG, "Histórico sincronizado: ${history.listName}")
            Result.success(result)
        } catch (e: Exception) {
            Logger.e(TAG, "Erro ao sincronizar histórico", e)
            Result.failure(e)
        }
    }

    /**
     * Verifica se o Supabase está disponível e configurado
     */
    fun isAvailable(): Boolean {
        return supabase != null && SupabaseConfig.isConfigured()
    }

    companion object {
        private const val TAG = "SupabaseSyncService"
    }
}

