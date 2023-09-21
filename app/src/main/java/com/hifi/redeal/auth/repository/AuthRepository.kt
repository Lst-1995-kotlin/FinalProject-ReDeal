package com.hifi.redeal.auth.repository

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AuthRepository {
    companion object {
        private val auth = FirebaseAuth.getInstance()

        @SuppressLint("StaticFieldLeak")
        private val firestore = FirebaseFirestore.getInstance()

        fun loginUser(
            email: String,
            password: String,
            successCallback: (AuthResult) -> Unit,
            errorCallback: (String) -> Unit
        ) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    successCallback(result)
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        val errorMessage = when (exception) {
                            is FirebaseAuthInvalidCredentialsException -> "잘못된 이메일 또는 비밀번호입니다. 다시 확인해주세요."
                            is FirebaseAuthInvalidUserException -> "존재하지 않는 사용자입니다. 회원가입을 진행해주세요."
                            else -> "인증에 실패했습니다. 나중에 다시 시도해주세요."
                        }
                        errorCallback(errorMessage)
                    } else {
                        errorCallback("인증에 실패했습니다. 나중에 다시 시도해주세요.")
                    }
                }
            }
        }

        fun addUserToFirestore(
            uid: String,
            userData: Map<String, Any>,
            successCallback: (String) -> Unit,
            errorCallback: (String) -> Unit
        ) {
            firestore.collection("userData").document(uid).set(userData)
                .addOnSuccessListener {
                    successCallback(uid)
                }
                .addOnFailureListener { e ->
                    val errorMessage = "사용자 정보를 저장하는 중 오류가 발생했습니다. 다시 시도해주세요."
                    errorCallback(errorMessage)
                }
        }

        fun registerUser(
            email: String,
            password: String,
            successCallback: (AuthResult) -> Unit,
            errorCallback: (String) -> Unit
        ) {
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        if (signInMethods != null && signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                            val errorMessage = "이미 가입된 이메일 주소입니다. 다른 이메일 주소를 사용하세요."
                            errorCallback(errorMessage)
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener(successCallback)
                                .addOnFailureListener { e ->
                                    val errorMessage = when (e) {
                                        is FirebaseAuthInvalidCredentialsException -> "잘못된 이메일 형식이거나 비밀번호가 너무 간단합니다. 다시 확인해주세요."
                                        is FirebaseAuthInvalidUserException -> "이미 가입된 이메일 주소입니다. 다른 이메일을 사용하세요."
                                        else -> "회원가입 중 오류가 발생했습니다. 다시 시도해주세요."
                                    }
                                    errorCallback(errorMessage)
                                }
                        }
                    } else {
                        val errorMessage = "이메일 확인 작업 중 오류가 발생했습니다. 다시 시도해주세요."
                        errorCallback(errorMessage)
                    }
                }
        }

        fun getNextIdx(callback: (Long) -> Unit, errorCallback: (String) -> Unit) {
            firestore.collection("userData")
                .orderBy("userIdx", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val lastUser = querySnapshot.documents[0]
                        val currentIdx = lastUser.getLong("userIdx") ?: 0
                        Log.d("getNextIdx", "가져온 IDX: $currentIdx")
                        // 다음 인덱스 계산
                        val nextIdx = currentIdx + 1
                        // 결과를 반환합니다.
                        callback(nextIdx)
                    } else {
                        // 컬렉션에 아무 문서도 없을 경우 기본값 0을 반환합니다.
                        callback(0)
                    }
                }
                .addOnFailureListener { e ->
                    // 실패 시 처리
                    val errorMessage = "인터넷 연결을 확인해주세요"
                    errorCallback(errorMessage)
                    Log.e("getNextIdx", errorMessage)
                }
        }

    }
}

