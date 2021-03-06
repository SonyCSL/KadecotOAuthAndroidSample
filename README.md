﻿# Kadecot OAuth Android Sample

# 概要

AndroidアプリからKadecotを利用するさいに、OAuthを行い、トークンを得たうえでWAMPのメッセージを送ることになります。
そのためのサンプルです。
Kadecotがインストールされ、起動されている同一端末内でしか機能しないのでご注意ください。


OAuth時の流れとしては、まず、OAuth用のブラウザを立ち上げ、認証が完了したらアプリに戻ってきます。通常KadecotはWebアプリからの利用を想定しているので、認証完了時にアプリに戻ってくるにはカスタムURIスキームを利用します。

結果はアクセストークンとして得られます。このトークンを用いて、Kadecot APIにアクセスすることになります。

このサンプルでは、WebSocketで接続したうえ、WAMPメッセージを送って機器一覧を得るところまで実装されています。

# 方法

## トークン取得

まず、manifest内でカスタムURIスキームkadecotを作っておきます(kadecot以外のスキームではOAuthを通過できません)。

1.OAuthの返答を受け取るactivityの属性に、singleTaskを追加します。(OAuthから戻ってきたときに、Activityが二重起動することを避けるため）

2.このactivity内に、次のintent-filterを追加し、カスタムuriスキームkadecotを作ります。
　<data>タグ内に android:host属性としてパッケージ名を入れるようにしてください。

            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="kadecot" android:host="com.sonycsl.kadecotoauthandroidsample" />
            </intent-filter>

3.結果を受け取るactivityのソースコードでは、onNewIntent内でsetIntentしておいてください。

4.OAuthを開始したい箇所で、以下のURLをブラウザで開くようにします。(サンプルではボタンが押されたとき）

"http://localhost:31413/login.html?redirect_uri=kadecot://"+context.getPackageName()+"&scope=com.sonycsl.kadecot"

redirect_uriが、OAuth後に戻ってくるuriとなります。他のkadecot対応アプリとの干渉を避けるため、ホスト名としては、必ずアプリのパッケージ名を入れるようにしてください。このパッケージ名は、のちほどWebSocketで通信する際のoriginとしても使われます（一致していなければなりません）

5.OAuthの結果はonResumeで受け取ります。getIntent().getData()するとUriオブジェクトが返ってきます。
この#以降がアクセストークンです。このサンプルではトークンは変数に格納されているだけなので、アプリが再起動されると消えてしまいますが、Preferenceなどに保存しておけば、Kadecotのデータがクリアされない限りは次回以降も使えます。

## WAMPでのアクセス

トークンが得られたら、次はそれを使ってWebSocketでサーバーにアクセスします。
このためには、もちろんmanifestにインターネットアクセスのパーミッション追加が必要なので忘れないようにお願いします。

<uses-permission android:name="android.permission.INTERNET" />

6.アプリにwamp.jarとjava_websocket.jarをリンクします。

7.WebSocket/WAMPでKadecotでつなぐためのトランスポートオブジェクトWampWebSocketTransportのインスタンスを作り、メッセージの授受をするためのリスナーWampWebSocketTransport.OnWampMessageListenerを設定した上でopen()で接続します。

接続先はlocalhost(同一端末内のため）、ポートは41314、originとして"kadecot://"+context.getPackageName()、最後の引数に先ほど得られたトークンを入れます。

mWampTransport.open("localhost", 41314, "kadecot://" + context.getPackageName(), mToken);

8.WAMPの規約に従い、HELLOメッセージを送ります。WAMPのメッセージはWampMessageFactory.create****で作れます。****はWAMPのメッセージの種類の数だけ存在します。

        mWampTransport.send(WampMessageFactory.createHello("realm", new JSONObject()));

9.返答は先程設定したリスナーのonMessage()で受け取ります。受け取ったメッセージの処理についてはMainActivity.javaを参照してください。
この例では、最初のHELLOの返答としてWELCOMEが返ってきたら、機器一覧を問い合わせるCALLを送信し、その返答のRESULTメッセージで機器ごとに情報を表示するようにしています。
