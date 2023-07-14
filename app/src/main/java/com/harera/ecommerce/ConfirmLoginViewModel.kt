package com.harera.confirm_login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.harera.common.base.BaseViewModel
import com.harera.common.local.UserDataStore
import com.harera.common.utils.Response
import com.harera.common.utils.Validity
import com.harera.repository.abstraction.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmLoginViewModel @Inject constructor(
    private val authManager: UserRepository,
    userDataStore: UserDataStore
) : BaseViewModel(userDataStore) {
    private var _code = MutableLiveData<String>("")
    val code: LiveData<String> = _code

    private var _phoneNumber = MutableLiveData<String>()
    val phoneNumber: LiveData<String> = _phoneNumber

    private var _codeValidity = MutableLiveData<Boolean>(false)
    val codeValidity: LiveData<Boolean> = _codeValidity

    private var _verificationCode = MutableLiveData<Response<String>>()
    val verificationCode: LiveData<Response<String>> = _verificationCode

    private var _loginOperation = MutableLiveData<Response<String>>()
    val loginOperation: LiveData<Response<String>> = _loginOperation

    fun codeChanged(ch: String) {
        code.value?.let {
            if (it.length < 6) {
                _code.value = it.plus(ch)
            }
        }
    }

    fun checkCodeValidity() {
        _codeValidity.value = Validity.checkCodeValidity(_code.value!!)
    }

    fun removeChar() {
        if (_code.value!!.length > 0) {
            _code.value = _code.value!!.dropLast(1)
        }
    }

    val createCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        }

        override fun onVerificationFailed(e: FirebaseException) {
            _verificationCode.value = Response.error(e, null)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            _verificationCode.value = Response.success(verificationId)

        }
    }

    fun login() {
        _loginOperation.value = Response.loading(null)
        viewModelScope.launch(Dispatchers.IO) {
            val credential = authManager
                .createCredential(verificationCode.value!!.data!!, code.value!!)
            authManager.login(credential)
                .addOnSuccessListener {
                    _loginOperation.value = Response.success(it.toString())
                }
                .addOnFailureListener {
                    _loginOperation.value = Response.error(it, null)
                }
        }
    }

    fun setPhoneNumber(phoneNumber: String) {
        _phoneNumber.value = phoneNumber
    }
}