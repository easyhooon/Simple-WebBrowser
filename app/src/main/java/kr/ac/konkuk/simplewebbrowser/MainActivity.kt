package kr.ac.konkuk.simplewebbrowser

import android.graphics.Bitmap
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.ContentLoadingProgressBar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    //프로퍼티 정의
    private val goHomeButton: ImageButton by lazy {
        findViewById(R.id.btn_goHome)
    }

    private val addressBar: EditText by lazy {
        findViewById(R.id.addressBar)
    }

    private val goBackButton: ImageButton by lazy {
        findViewById(R.id.btn_goBack)
    }

    private val goForwardButton: ImageButton by lazy {
        findViewById(R.id.btn_goForward)
    }

    private val webView: WebView by lazy {
        findViewById(R.id.webView)
    }

    private val refreshLayout: SwipeRefreshLayout by lazy {
        findViewById(R.id.refreshLayout)
    }

    private val progressBar: ContentLoadingProgressBar by lazy {
        findViewById(R.id.progressBar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        bindViews()
    }

    private fun initViews() {
        //디폴트 앱브라우저가 아닌 앱에서 로드 하려면 웹뷰의 동작을 오버라이드해야함
        //이 코드가 있어야 지정한 웹뷰영역에 원하는 사이트가 열림

        //세번이상 webView. 에 접근하므로 apply로 묶어주면 세번이상 웹뷰를 호출해주지않아도 됨
        webView.apply{
            webViewClient = WebViewClient()
            //웹뷰내에 자바스크립트로 구현된 동작들을 수행할 수 있게 함
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true
            loadUrl(DEFAULT_URL)
        }
    }

    //연결
    private fun bindViews() {
        addressBar.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                //https를 입력을 안해도 자동으로 붙혀주도록
                val loadingUrl = v.text.toString()
                if(URLUtil.isNetworkUrl(loadingUrl)){
                    //true면 앞에 http또는 https가 붙어있는 것
                    webView.loadUrl(loadingUrl)
                } else {
                    //앞에 http://를 붙혀줌
                    webView.loadUrl("http://$loadingUrl")
                }

                webView.loadUrl(v.text.toString())
            }

            //키보드를 내리기 위해
            return@setOnEditorActionListener false
        }

        goBackButton.setOnClickListener {
            webView.goBack()
        }

        goForwardButton.setOnClickListener {
            webView.goForward()
        }

        goHomeButton.setOnClickListener {
            webView.loadUrl(DEFAULT_URL)
        }

        refreshLayout.setOnRefreshListener {
            webView.reload()
        }
    }

    //inner를 붙혀주지 않으면 메인액티비티의 프로퍼티에 접근을 하지못함 -> refreshLayout에 접근을 하지 못함
    //inner를 붙혀줌으로써 상위 클래스에 접근이 가능해진다.
    //단순하게 웹뷰만 띄워줄때 사용
    inner class WebViewClient: android.webkit.WebViewClient() {

        //생기고 사라지는것 구현
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            progressBar.show()
        }

        //페이지 로드가 완료되었을 때
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            refreshLayout.isRefreshing = false
            progressBar.hide()
            //뒤로 갈 창이 없을 경우 뒤로가기 버튼 비활성화
            goBackButton.isEnabled = webView.canGoBack()
            //앞으로 ''
            goForwardButton.isEnabled = webView.canGoBack()

            //최종적으로 로딩된 주소를 set해줌 (www.naver.com으로 검색해도 결과는 m.naver.com으로 띄워주는 것처럼 m.naver.com(최종 url))
            addressBar.setText(url)

        }
    }

    //브라우저 차원(관점)의 이벤트들을 오버라이드해서 사용할때 많이 사용
    inner class WebChromeClient: android.webkit.WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)

            progressBar.progress = newProgress //DEFAULT
        }
    }



    //분기에 따라 웹뷰의 뒤로가기를 할지, 앱자체를 뒤로가기할지 결정
    override fun onBackPressed() {
        //뒤로 갈 수 있는지를 확인
        if(webView.canGoBack()){
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    //하드코딩하지 않고 이렇게 상수로 빼놓으면 수정사항이 생길때 상수만 바꿔주면 됨
    companion object {
        private const val DEFAULT_URL = "http://www.google.com"
    }
}