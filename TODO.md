# TODO

- [x] Gradle 依存ライブラリの追加（Room, Maps Compose, Coroutines, ViewModel, Location Services）
- [x] Google Maps 全画面表示と API キー設定（local.properties 管理）
- [x] 現在地の表示と権限リクエスト（ACCESS_FINE_LOCATION, POST_NOTIFICATIONS）
- [x] 待機モード・記録モードの UI。FAB で ▶/■ を切り替え、MainViewModel で状態管理
- [x] Room データ層の実装（Activity, Point, DAO, Repository）
- [x] Foreground Service で GPS 位置情報を記録。3 秒間隔で取得し Room に保存
- [x] 停止時に Activity の合計時間・距離を集計して保存
- [x] Polyline でルート描画、経過時間・距離を地図上部にオーバーレイ表示
- [x] 過去の走行記録をボトムシートで一覧表示。タップで軌跡プレビュー、削除機能付き
- [x] Auto Backup 設定（Room DB を対象に含める）
- [x] Google Maps の要素がステータスバーとナビゲーションバーに重なっているのを修正
- [x] 記録中の時刻・距離 UI のデザインを調整
- [x] 位置情報の異常値検出・ノイズフィルタリング
- [ ] アプリアイコンの作成
- [ ] 音声コーチング（TTS で経過時間・距離を時間ベースで読み上げ）
