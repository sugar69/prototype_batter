package com.example.batting_prototype

import android.R.attr.x
import android.R.attr.y
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SensorEventListener {

    //  音声系の変数
    private lateinit var soundPool: SoundPool
    private var soundResId = 0
    private var prepareSound = 0
    private var pitchingSound = 0
    private var swingSound = 0
    private var smallHitSound = 0
    private var bigHitSound = 0

    //  センサー系の変数
    private var time: Long = 0L
    var swingFlag = false

    //  クレジットの表示
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.credit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.credit -> {
                val intent = Intent(this, CreditActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        //  センサーの設定
//        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        //  SEを設定する枠の初期設定
        val audioAttributes = AudioAttributes.Builder() // USAGE_MEDIA
            // USAGE_GAME
            .setUsage(AudioAttributes.USAGE_GAME) // CONTENT_TYPE_MUSIC
            // CONTENT_TYPE_SPEECH, etc.
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes) // ストリーム数に応じて
            .setMaxStreams(5)
            .build()

        //  SEの登録
        prepareSound = soundPool.load(this, R.raw.prepare_machine, 1)
        pitchingSound = soundPool.load(this, R.raw.pitching_sound, 1)
        swingSound = soundPool.load(this, R.raw.swing_sound, 1)
        smallHitSound = soundPool.load(this, R.raw.small_hit, 1)
        bigHitSound = soundPool.load(this, R.raw.big_hit, 1)


        //  動作していないならピッチングの処理へ，動作中なら無視する（スイッチを無効化）
        startButton.setOnClickListener {
//            Toast.makeText(this, "ボタンが押されました", Toast.LENGTH_SHORT).show()
            soundPool.play(prepareSound, 1.0f, 1.0f, 1, 0, 1.0f)
            startButton.setEnabled(false)
            pitchingButtonTapped(it)
            //  3.5秒後にボタンが再び押せるようになる
            Handler().postDelayed(Runnable {
                startButton.setEnabled(true)
            }, 3500)
        }
    }

    //  ピッチング中の処理
    fun pitchingButtonTapped(view: View?) {
//        Toast.makeText(applicationContext, "func", Toast.LENGTH_SHORT).show()
        // 2秒後に投球音を流す
        Handler().postDelayed(Runnable {
            soundPool.play(pitchingSound, 1.0f, 1.0f, 1, 0, 1.0f)
            time = System.currentTimeMillis()
        }, 2000)
        //  投球の瞬間の時間を保存
//        time = System.currentTimeMillis()
    }

    //  センサーの監視
    override fun onSensorChanged(event: SensorEvent?) {
        var swingTime: Long = 0L

        if (event == null) return

        if ((event.sensor.type == Sensor.TYPE_ACCELEROMETER) && (swingFlag == false)) {
            if((event.values[0] > 5.0 && event.values[1] > 6.0)/* || (-event.values[0] > 10.0 && event.values[1] > 6.0)*/){
                swingFlag = true
//                Toast.makeText(applicationContext, "$time", Toast.LENGTH_SHORT).show()
                swingTime = System.currentTimeMillis()
                if ((400L <= swingTime - time && swingTime - time < 500L)
                    or (600L < swingTime - time && swingTime - time <= 700L)
                ) {
                    soundPool.play(smallHitSound, 1.0f, 1.0f, 1, 0, 1.0f)
                } else if (500L <= swingTime - time && swingTime - time <= 600L) {
                    soundPool.play(bigHitSound, 1.0f, 1.0f, 1, 0, 1.0f)
                } else {
                    soundPool.play(swingSound, 1.0f, 1.0f, 1, 0, 1.0f)
                }
            }

            // 2秒後にスイングのフラグを戻す
            Handler().postDelayed(Runnable {
                swingFlag = false
            }, 2000)

        }
    }

    override fun onAccuracyChanged(event: Sensor?, p1: Int) {

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()

        //  SEを設定する枠の初期設定
        val audioAttributes = AudioAttributes.Builder() // USAGE_MEDIA
            // USAGE_GAME
            .setUsage(AudioAttributes.USAGE_GAME) // CONTENT_TYPE_MUSIC
            // CONTENT_TYPE_SPEECH, etc.
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes) // ストリーム数に応じて
            .setMaxStreams(5)
            .build()
        //  音声の再登録
        prepareSound = soundPool.load(this, R.raw.prepare_machine, 1)
        pitchingSound = soundPool.load(this, R.raw.pitching_sound, 1)
        swingSound = soundPool.load(this, R.raw.swing_sound, 1)
        smallHitSound = soundPool.load(this, R.raw.small_hit, 1)
        bigHitSound = soundPool.load(this, R.raw.big_hit, 1)

        //  センサーの監視の再開
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()

        //  音声のメモリ解放
        soundPool.release()

        //  センサーの停止
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }

}