package com.wrs.applibproject

import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wrs.applibproject.ui.theme.AppLibProjectTheme
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class MainActivity : ComponentActivity() {
    var  textureView: TextureView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContent {
//            AppLibProjectTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
//                }
//            }
//        }

        setContentView(R.layout.layout)

          textureView = findViewById(R.id.textureView)
        textureView?.surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                i: Int,
                i1: Int
            ) {
                initVLC()
                //                initVLCPlayer();
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

    }

    fun  initVLC() {
        val args = java.util.ArrayList<String>()
        args.add("-vvv")
        args.add("--no-drop-late-frames") //防止掉帧

        args.add("--no-skip-frames") //防止掉帧

        args.add("--rtsp-tcp") //强制使用TCP方式，强制rtsp-tcp，加快加载视频速度

        args.add("--avcodec-hw=any") //尝试使用硬件加速

        args.add("--live-caching=0") //缓冲时长

       val libVLC = LibVLC(this, args)
        val  mediaPlayer = MediaPlayer(libVLC)
//        mediaPlayer.setEventListener(this)

        mediaPlayer.getVLCVout().setVideoSurface(textureView!!.surfaceTexture)
        mediaPlayer.getVLCVout().setWindowSize(textureView!!.width, textureView!!.height)
        textureView!!.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom -> // 获取新的宽度和高度
            val newWidth = right - left
            val newHeight = bottom - top
            // 设置VLC播放器的宽高参数
            mediaPlayer.getVLCVout().setWindowSize(newWidth, newHeight)
        }

        mediaPlayer.getVLCVout().attachViews()

       val media = Media(libVLC, Uri.parse("https://dh2.v.netease.com/2017/cg/fxtpty.mp4"))
        mediaPlayer.media = media
        media.release()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppLibProjectTheme {
        Greeting("Android")
    }
}