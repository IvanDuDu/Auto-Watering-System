package com.example.project2_verse3.model

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.project2_verse3.controller.fetchUserList
import com.example.project2_verse3.greatToken
import getBossListId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MainViewModel : ViewModel() {

    // User list
    private val _userList = MutableStateFlow<List<UserModel>>(emptyList())
    val userList: StateFlow<List<UserModel>> = _userList

    // Boss list
    private val _bossListState = MutableStateFlow<List<BossModel>>(emptyList())
    val bossListState: StateFlow<List<BossModel>> = _bossListState

    // User đang đăng nhập
    private val _user = MutableStateFlow<UserModel?>(null)
    val user: StateFlow<UserModel?> = _user

    // Loading indicators
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isFetchingUserList = MutableStateFlow(false)
    val isFetchingUserList: StateFlow<Boolean> = _isFetchingUserList

    // **CỜ BÁO TRẠNG THÁI FETCH DATA**
    private val _isDataFetched = MutableStateFlow(false)
    val isDataFetched: StateFlow<Boolean> = _isDataFetched

    // Cờ báo có đang trong quá trình fetch data hay không
    private val _isFetchingData = MutableStateFlow(false)
    val isFetchingData: StateFlow<Boolean> = _isFetchingData

    init {
        // Mặc định chưa fetch gì cả
        Log.d("MainViewModel", "ViewModel initialized")
    }

    // Fetch Boss list cho user hiện tại - với cờ báo trạng thái
    fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("MainViewModel", "Starting fetchData process...")

            // Đặt cờ báo bắt đầu fetch
            _isFetchingData.value = true
            _isDataFetched.value = false

            try {
                val currentUser = _user.value
                if (currentUser == null) {
                    Log.e("MainViewModel", "No user logged in, cannot fetch data")
                    return@launch
                }

                if (greatToken.isEmpty()) {
                    Log.e("MainViewModel", "Token is empty, cannot fetch data")
                    return@launch
                }

                Log.d("MainViewModel", "Fetching boss list for user: ${currentUser.cusID}")
                Log.d("MainViewModel", "Using token: ${greatToken.take(20)}...")

                // Thực hiện fetch data
                val bossList = getBossListId(currentUser.cusID)

                withContext(Dispatchers.Main) {
                    _bossListState.value = bossList
                    Log.d("MainViewModel", "Boss list fetched successfully: ${bossList.size} items")

                    // **ĐẶT CỜ BÁO HOÀN THÀNH FETCH DATA**
                    _isDataFetched.value = true
                    Log.d("MainViewModel", "Data fetch completed - flag set to true")
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to fetch boss list: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _bossListState.value = emptyList()
                    // Vẫn set flag = true để không bị kẹt loading
                    _isDataFetched.value = true
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isFetchingData.value = false
                    Log.d("MainViewModel", "fetchData process completed")
                }
            }
        }
    }

    // Load toàn bộ User list từ API
    fun loadUserList() {
        viewModelScope.launch(Dispatchers.IO) {
            _isFetchingUserList.value = true
            try {
                if (greatToken.isEmpty()) {
                    Log.e("MainViewModel", "Token is empty, cannot fetch user list")
                    return@launch
                }

                Log.d("MainViewModel", "Fetching user list...")
                val result = fetchUserList()

                withContext(Dispatchers.Main) {
                    _userList.value = result
                    Log.d("MainViewModel", "User list loaded successfully: ${result.size} users")
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to load user list: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _userList.value = emptyList()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isFetchingUserList.value = false
                }
            }
        }
    }

// Xử lý login - BẮT ĐẦU FETCH DATA SAU KHI LOGIN THÀNH CÔNG
    fun login(
        email: String,
        password: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            try {
                // Validation input
                if (email.isBlank() || password.isBlank()) {
                    withContext(Dispatchers.Main) {
                        onResult("Email và mật khẩu không được để trống!")
                        _isLoading.value = false
                    }
                    return@launch
                }

                // Đợi user list được load nếu chưa có
                if (_userList.value.isEmpty()) {
                    Log.d("MainViewModel", "User list is empty, loading...")
                    loadUserList()

                    // Đợi load xong
                    while (_isFetchingUserList.value) {
                        delay(100)
                    }
                }

                delay(500) // Simulate loading time

                val matchedUser = _userList.value.find {
                    it.cusAcc == email && it.passWord == password
                }

                withContext(Dispatchers.Main) {
                    if (matchedUser != null) {
                        _user.value = matchedUser
                        Log.d("MainViewModel", "User logged in successfully: ${matchedUser.cusName}")

                        // **BẮT ĐẦU FETCH DATA SAU KHI LOGIN THÀNH CÔNG**
                        fetchData()

                        onResult("Đăng nhập thành công!")
                        mainUser= _user.value!!

                        // **KHÔNG NAVIGATION Ở ĐÂY - ĐỂ MAINSCREEN XỬ LÝ**

                    } else {
                        onResult("Sai email hoặc mật khẩu!")
                    }
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Login error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult("Có lỗi xảy ra khi đăng nhập!")
                    _isLoading.value = false
                }
            }
        }
    }
    // Hàm retry fetch data khi cần
    fun retryFetchData() {
        if (_user.value != null) {
            Log.d("MainViewModel", "Retrying fetch data...")
            fetchData()
        } else {
            Log.e("MainViewModel", "Cannot retry: no user logged in")
        }
    }

    // Reset tất cả trạng thái khi cần
    fun resetDataState() {
        _isDataFetched.value = false
        _isFetchingData.value = false
        _bossListState.value = emptyList()
        Log.d("MainViewModel", "Data state reset")
    }

    // Clear data khi logout
    fun logout() {
        _user.value = null
        _bossListState.value = emptyList()
        _userList.value = emptyList()
        _isLoading.value = false
        _isFetchingUserList.value = false
        _isDataFetched.value = false
        _isFetchingData.value = false
        Log.d("MainViewModel", "User logged out, all data cleared")
    }

    // Kiểm tra trạng thái tổng thể
    fun getAppState(): String {
        return "User: ${_user.value?.cusName ?: "None"}, " +
                "DataFetched: ${_isDataFetched.value}, " +
                "FetchingData: ${_isFetchingData.value}, " +
                "BossList: ${_bossListState.value.size}"
    }
}