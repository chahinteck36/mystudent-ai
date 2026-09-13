package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.FlashcardDeckEntity
import com.example.data.local.entity.LectureNoteEntity
import com.example.data.local.entity.LessonSummaryEntity
import com.example.data.local.entity.LibraryItemEntity
import com.example.data.local.entity.ProblemSolutionEntity
import com.example.data.local.entity.QuizEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyMateDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    // Problem Solutions
    @Query("SELECT * FROM problem_solutions ORDER BY timestamp DESC")
    fun getAllSolutions(): Flow<List<ProblemSolutionEntity>>

    @Query("SELECT * FROM problem_solutions WHERE id = :id LIMIT 1")
    suspend fun getSolutionById(id: Long): ProblemSolutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolution(solution: ProblemSolutionEntity): Long

    @Query("DELETE FROM problem_solutions WHERE id = :id")
    suspend fun deleteSolution(id: Long)

    // Chat
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getChatMessages(conversationId: String = "main"): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun clearChatMessages(conversationId: String = "main")

    // Summaries
    @Query("SELECT * FROM lesson_summaries ORDER BY timestamp DESC")
    fun getAllSummaries(): Flow<List<LessonSummaryEntity>>

    @Query("SELECT * FROM lesson_summaries WHERE id = :id LIMIT 1")
    suspend fun getSummaryById(id: Long): LessonSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: LessonSummaryEntity): Long

    @Query("DELETE FROM lesson_summaries WHERE id = :id")
    suspend fun deleteSummary(id: Long)

    // Flashcard Decks
    @Query("SELECT * FROM flashcard_decks ORDER BY timestamp DESC")
    fun getAllDecks(): Flow<List<FlashcardDeckEntity>>

    @Query("SELECT * FROM flashcard_decks WHERE id = :id LIMIT 1")
    suspend fun getDeckById(id: Long): FlashcardDeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeckEntity): Long

    @Update
    suspend fun updateDeck(deck: FlashcardDeckEntity)

    @Query("DELETE FROM flashcard_decks WHERE id = :id")
    suspend fun deleteDeck(id: Long)

    // Quizzes
    @Query("SELECT * FROM quizzes ORDER BY timestamp DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :id LIMIT 1")
    suspend fun getQuizById(id: Long): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Query("DELETE FROM quizzes WHERE id = :id")
    suspend fun deleteQuiz(id: Long)

    // Lectures
    @Query("SELECT * FROM lecture_notes ORDER BY timestamp DESC")
    fun getAllLectures(): Flow<List<LectureNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLecture(lecture: LectureNoteEntity): Long

    @Query("DELETE FROM lecture_notes WHERE id = :id")
    suspend fun deleteLecture(id: Long)

    // Library
    @Query("SELECT * FROM library_items ORDER BY timestamp DESC")
    fun getAllLibraryItems(): Flow<List<LibraryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibraryItem(item: LibraryItemEntity): Long

    @Query("DELETE FROM library_items WHERE id = :id")
    suspend fun deleteLibraryItem(id: Long)
}
