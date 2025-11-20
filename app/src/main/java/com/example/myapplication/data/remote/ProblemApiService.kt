package com.example.myapplication.data.remote

import com.example.myapplication.data.model.HintResponse
import com.example.myapplication.data.model.Problem
import com.example.myapplication.data.model.SubmissionRequest
import com.example.myapplication.data.model.SubmissionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProblemApiService {

    @GET("api/problems/tenProblem")
    suspend fun getTenProblems(): Response<List<Problem>>

    @POST("api/problems/submit")
    suspend fun submitAnswer(@Body request: SubmissionRequest): Response<SubmissionResponse>

    @GET("api/problems/hint/{problemId}/{hintCount}")
    suspend fun getHint(
        @Path("problemId") problemId: Long,
        @Path("hintCount") hintCount: Int
    ): Response<HintResponse>
}