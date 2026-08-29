package com.m57.hermescontrol.data.remote

import com.m57.hermescontrol.data.model.ApproveResult
import com.m57.hermescontrol.data.model.DashboardResponse
import com.m57.hermescontrol.data.model.EmployeeCard
import com.m57.hermescontrol.data.model.EmployeeProfile
import com.m57.hermescontrol.data.model.TaskDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * emp-api（自建员工状态接口）的 Retrofit API。
 *
 * 注意：emp-api 是独立 Bearer API_SERVER_KEY 鉴权，与主 HermesApiService
 * （dashboard cookie/bearer）完全隔离。baseUrl 由 [EmpApiClient] 拼到
 * `/emp-api/`，这里只用相对路径。
 */
interface EmpApiService {
    /** 驾驶舱聚合（KPI + 待拍板 + 员工状态看板）。 */
    @GET("api/dashboard")
    suspend fun dashboard(): Response<DashboardResponse>

    /** 员工名片。 */
    @GET("api/employees/{name}/profile")
    suspend fun employeeProfile(
        @Path("name") name: String,
    ): Response<EmployeeProfile>

    /** 更新员工名片（总裁改）。body: 部分字段可选。 */
    @PUT("api/employees/{name}/profile")
    suspend fun updateEmployeeProfile(
        @Path("name") name: String,
        @Body body: Map<String, Any?>,
    ): Response<EmployeeProfile>

    // ── 任务详情 + 任务群聊 + 拍板（PRD 5.3）──
    @GET("api/tasks/{taskId}")
    suspend fun getTaskDetail(
        @Path("taskId") taskId: String,
    ): Response<TaskDetailResponse>

    /** 在任务群聊发评论（author 默认 boss）。 */
    @POST("api/tasks/{taskId}/comment")
    suspend fun postTaskComment(
        @Path("taskId") taskId: String,
        @Body body: Map<String, String>,
    ): Response<Map<String, Any?>>

    /** 总裁拍板：一句话 → 卡住解除变进行中。 */
    @POST("api/tasks/{taskId}/approve")
    suspend fun approveTask(
        @Path("taskId") taskId: String,
        @Body body: Map<String, String>,
    ): Response<ApproveResult>

    // ── 员工名片聚合（PRD 5.4/5.5）──
    @GET("api/employees/{name}")
    suspend fun getEmployeeCard(
        @Path("name") name: String,
    ): Response<EmployeeCard>
}
