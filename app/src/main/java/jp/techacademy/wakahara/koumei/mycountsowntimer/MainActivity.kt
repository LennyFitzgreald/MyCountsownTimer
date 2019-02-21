package jp.techacademy.wakahara.koumei.mycountsowntimer

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    // SoundPoolクラスのインスタンス、後で初期化するのでlateinit
    private lateinit var soundPool: SoundPool
    // サウンドファイルのリソースIDを保持するプロパティ
    private var soundResId = 0

    // CountDownTimerを継承したインナークラスを作成
    // 第一引数：残り時間---ミリ秒、第二引数：onTickメソッドを実行する間隔---ミリ秒
    // ＊onTickとonFinishメソッドのオーバーライド必須
    inner class MyCountDownTimer(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {

        var isRunning = false //フローティングアクションボタンでスタート／ストップを切り替えるために必要なプロパティ

        // タイマー終了時に呼び出されるメソッド（必須）
        override fun onFinish() {
            timerText.text = "0:00"
            soundPool.play(soundResId, 1.0f, 100f, 0, 0, 1.0f)
        }

        // コンストラクタで指定した間隔で呼び出されるメソッド（必須）
        // 引数：タイマーの残り時間（ミリ秒）
        override fun onTick(millisUntilFinished: Long) {
            val minute = millisUntilFinished / 1000L / 60L
            val second = millisUntilFinished / 1000L % 60L
            timerText.text = "%1d:%2$02d".format(minute, second) // format関数 "フォーマット文字列.format(値, 値, ...)"
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText.text = "3:00"
        val timer = MyCountDownTimer(3 * 60 * 1000, 100) // ミリ秒で3分、0.1秒間隔
        // フローティングアクションボタンを押したときのリスナーを設定
        playStop.setOnClickListener {
            when (timer.isRunning) {
                true -> timer.apply {
                    isRunning = false
                    cancel()
                    playStop.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                }
                false -> timer.apply {
                    isRunning = true
                    start()
                    playStop.setImageResource(R.drawable.ic_stop_black_24dp)
                }
            }
        }
    }

    // メモリを節約するため、アクティビティが表示されたときに音楽データをメモリにロード
    override fun onResume() {
        super.onResume()
        soundPool =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // API21(Lollipop)以上では非推奨とされる方法
                    @Suppress("DEPRECATION")
                    SoundPool(2, AudioManager.STREAM_ALARM, 0)
                    // 第一引数：同時に再生できる音源数、
                    // 第二引数：オーディオのストリームタイプ(p.234)、
                    // 第三引数：品質（現在は効果がないのでとりあえず0を指定）

                } else {
                    // API21(Lollipop)以上で推奨されるSoundPool.Builderクラスを使用する方法
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    SoundPool.Builder()
                        .setMaxStreams(1) // 同時に鳴らす音の数を設定
                        .setAudioAttributes(audioAttributes) // setAudioAttributesメソッドの引数にAudioAttributesクラスのインスタンスを設定
                        .build()
                }

        soundResId = soundPool.load(this, R.raw.bellsound, 1)
        // 第一引数：context, アクティビティを指定、
        // 第二引数：サウンドファイルのリソースID「R.raw.ファイル名」＊＊拡張子は書かない
        // ┗事前にリソースにrawフォルダを作りダウンロードいた音源を入れてある
        // 第三引数：音の優先順位（現在は効果がないので互換性のために1を指定)

    }

    // アクティビティが非表示になったら音楽データで使用したメモリを解放
    override fun onPause() {
        super.onPause()
        soundPool.release()
    }
}
