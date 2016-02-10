# Kadecot OAuth Android Sample
(2016/2/10時点で完成していません）

## 概要

AndroidアプリからKadecotを利用するさいに、OAuthが必要になります。
そのためのサンプルです。Kadecotがインストールされている同一端末内でしか機能しないのでご注意ください。

OAuth時の流れとしては、まず、OAuth用のブラウザを立ち上げ、認証が完了したらアプリに戻ってきます。通常KadecotはWebアプリからの利用を想定しているので、認証完了時にアプリに戻ってくるにはカスタムURIスキームを利用します。

結果はアクセストークンとして得られます。このトークンを用いて、Kadecot APIにアクセスすることになります。

## 方法

# トークン取得

まず、manifest内でカスタムURIスキームを作っておきます。

1.OAuthの返答を受け取るactivityの属性に、singleTaskを追加します。(OAuthから戻ってきたときに、Activityが二重起動することを避けるため）

2.このactivity内に、次のintent-filterを追加し、カスタムuriスキームkadecotを作ります。

            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="kadecot" />
            </intent-filter>

3.結果を受け取るactivityのソースコードでは、onNewIntent内でsetIntentしておいてください。

4.OAuthを開始したい箇所で、以下のURLをブラウザで開くようにします。(サンプルではボタンが押されたとき）

"http://localhost:31413/login.html?redirect_uri=kadecot://"+context.getPackageName()+"&scope=com.sonycsl.kadecot"

redirect_uriが、OAuth後に戻ってくるuriとなります。他のkadecot対応アプリとの干渉を避けるため、ホスト名としては、必ずアプリのパッケージ名を入れるようにして下し。このパッケージ名は、のちほどWebSocketで通信する際のoriginとしても使われます（一致していなければなりません）

5.OAuthの結果はonResumeで受け取ります。getIntent().getData()するとUriオブジェクトが返ってきます。
この#以降がアクセストークンです。

# WAMPでのアクセス

トークンが得られたら、次はそれを使ってWebSocketでサーバーにアクセスします。
このためには、もちろんmanifest内Applicationタグ内にインターネットアクセスのパーミッション追加が必要なので忘れないようにお願いします。

<uses-permission android:name="android.permission.INTERNET" />

4.アプリにwamp.jarとjava_websocket.jarをリンクします。

