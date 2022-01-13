package com.astroyodha.ui.user.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.astroyodha.databinding.ActivityThankYouBinding
import com.astroyodha.utils.Constants
import com.astroyodha.utils.makeGone

class ThankYouActivity : AppCompatActivity() {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityThankYouBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThankYouBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.getBooleanExtra(Constants.INTENT_IS_DIRECT_PAYMENT, false)) {
            //comes from add book event so required to go back to add data in booking history
            binding.tvGoToHome.makeGone()
        }
        setClickListener()
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgClose.setOnClickListener {
            onBackPressed()
        }
        binding.tvGoToHome.setOnClickListener {
            if (intent.getBooleanExtra(Constants.INTENT_IS_DIRECT_PAYMENT, false)) {
                //comes from add book event so required to go back to add data in booking history
                onBackPressed()
            } else {
                //comes from wallet fragment
                startActivity(
                    Intent(this, UserHomeActivity::class.java)
//                        .putExtra(Constants.INTENT_SHOW_TIMER, false)
                )
                finishAffinity()
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().putExtra(Constants.INTENT_TRANSACTION, true))
        super.onBackPressed()
    }
}