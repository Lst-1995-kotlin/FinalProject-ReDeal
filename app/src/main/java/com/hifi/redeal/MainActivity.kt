package com.hifi.redeal

import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.transition.MaterialSharedAxis
import com.hifi.redeal.auth.AuthFindPwFragment
import com.hifi.redeal.auth.AuthJoinFragment
import com.hifi.redeal.auth.AuthLoginFragment
import com.hifi.redeal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var activityMainBinding: ActivityMainBinding

    var newFragment: Fragment? = null
    var oldFragment: Fragment? = null

    companion object {
        val AUTH_LOGIN_FRAGMENT = "AuthLoginFragment"
        val AUTH_JOIN_FRAGMENT = "AuthJoinFragment"
        val AUTH_FIND_PW_FRAGMENT = "AuthFindPwFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        replaceFragment(AUTH_LOGIN_FRAGMENT,false,null)
    }

    fun replaceFragment(name: String, addToBackStack: Boolean, bundle: Bundle?) {

        SystemClock.sleep(200)

        // Fragment 교체 상태로 설정한다.
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // newFragment 에 Fragment가 들어있으면 oldFragment에 넣어준다.
        if (newFragment != null) {
            oldFragment = newFragment
        }

        // 새로운 Fragment를 담을 변수
        newFragment = when (name) {
            AUTH_LOGIN_FRAGMENT -> AuthLoginFragment()
            AUTH_JOIN_FRAGMENT -> AuthJoinFragment()
            AUTH_FIND_PW_FRAGMENT -> AuthFindPwFragment()
            else -> Fragment()
        }

        newFragment?.arguments = bundle

        if (newFragment != null) {

            // 애니메이션 설정
            if (oldFragment != null) {
                oldFragment?.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                oldFragment?.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
                oldFragment?.enterTransition = null
                oldFragment?.returnTransition = null
            }
            newFragment?.exitTransition = null
            newFragment?.reenterTransition = null
            newFragment?.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
            newFragment?.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

            // Fragment를 교체한다.
            fragmentTransaction.replace(R.id.mainContainer, newFragment!!)

            if (addToBackStack == true) {
                // Fragment를 Backstack에 넣어 이전으로 돌아가는 기능이 동작할 수 있도록 한다.
                fragmentTransaction.addToBackStack(name)
            }
            // 교체 명령이 동작하도록 한다.
            fragmentTransaction.commit()
        }
    }
    fun removeFragment(name: String) {
        supportFragmentManager.popBackStack(name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}