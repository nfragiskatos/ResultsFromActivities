package dev.nicholasfragiskatos.resultsfromactivities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.nicholasfragiskatos.resultsfromactivities.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val inputFromMainActivity = intent.getStringExtra(MainActivity.MAIN_ACTIVITY_BUNDLE_ID)

        binding.tvInput.text = inputFromMainActivity

        binding.btnSendResult.setOnClickListener {
            val result = binding.etResult.text.toString()
            val intent = Intent().putExtra(SECOND_ACTIVITY_BUNDLE_ID, result)
            setResult(SECOND_ACTIVITY_RESULT_CODE, intent)
            finish()
        }
    }

    companion object {
        const val SECOND_ACTIVITY_RESULT_CODE = 600
        const val SECOND_ACTIVITY_BUNDLE_ID = "SecondActivityBundleId"
    }
}
