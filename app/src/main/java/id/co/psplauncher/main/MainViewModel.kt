package id.co.psplauncher.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.psplauncher.data.local.UserPreferences
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.data.network.auth.AuthRepository
import id.co.psplauncher.data.network.response.LoginResponse
import id.co.psplauncher.data.network.response.PackageListResponse
import id.co.psplauncher.data.network.response.UpdateResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private var userPreferences: UserPreferences
) : ViewModel() {

    private var _loginResponse: MutableLiveData<Resource<LoginResponse>> = MutableLiveData()
    val loginResponse: LiveData<Resource<LoginResponse>> get() = _loginResponse

    fun login(username: String, password: String) = viewModelScope.launch {
        _loginResponse.value = Resource.Loading
        _loginResponse.value = authRepository.login(username, password)
    }

    private var _updateResponse: MutableLiveData<Resource<UpdateResponse>> = MutableLiveData()
    val updateResponse: LiveData<Resource<UpdateResponse>> get() = _updateResponse
    fun checkUpdate(id: String) = viewModelScope.launch {
        _updateResponse.value = Resource.Loading
        _updateResponse.value = authRepository.checkUpdate(id)
    }

    private var _packageAppResponse: MutableLiveData<Resource<PackageListResponse>> = MutableLiveData()
    val packageAppResponse: LiveData<Resource<PackageListResponse>> get() = _packageAppResponse
    fun getPackageApp() = viewModelScope.launch {
        _packageAppResponse.value = Resource.Loading
        _packageAppResponse.value = authRepository.getPackageApp()
    }

    fun getActivePackageList() = viewModelScope.launch{
        userPreferences.getActivePackageList()
        Log.i("getViewModel", "called")
    }

    fun savePackageList(data: String) = viewModelScope.launch {
        userPreferences.saveActivePackageList(data)
        Log.i("Save to UserPreferences", "called")
    }
}