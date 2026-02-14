# TODO

- [x] Gradle に依存ライブラリを追加。Room, Google Maps SDK (Maps Compose), Coroutines, ViewModel, Location Services を libs.versions.toml と build.gradle.kts に追加する。ビルドが通ることを実機で確認
- [ ] Google Maps を全画面表示。MainScreen.kt を作成し、Google Maps を全画面で表示する。MainActivity から呼び出す。実機で地図が表示されることを確認
- [ ] 現在地の表示と権限リクエスト。ACCESS_FINE_LOCATION の権限リクエストを実装し、現在地を地図の中心に表示する。実機で位置情報の許可ダイアログと現在地表示を確認
- [ ] 待機モードの UI。FAB（▶）を画面下中央に配置する。タップで記録モードに遷移する状態管理を MainViewModel に実装。実機で FAB の表示と状態遷移を確認
- [ ] 記録モードの UI。FAB を停止ボタン（■）に切り替える
- [ ] Room のデータ層を実装。Activity, Point エンティティ、DAO、AppDatabase、ActivityRepository を作成する。実機で起動してクラッシュしないことを確認
- [ ] Foreground Service で位置情報を記録。TrackingService を作成し、記録モード中に GPS 座標を取得して Room に保存する。通知チャンネルと Foreground Notification を実装。実機で通知が出ること、バックグラウンドでも記録が続くことを確認
- [ ] 走行ルートを Polyline で描画し、経過時間と走行距離を地図上部にオーバーレイ表示する。実機でリアルタイム描画と数値更新を確認
- [ ] 停止ボタンタップで確認ダイアログを表示し、保存して待機モードに戻る。Activity テーブルに合計時間・距離を書き込む。実機で停止→保存→待機モード復帰の流れを確認
- [ ] 過去の走行記録の表示。待機モードから過去の記録一覧にアクセスできる UI を実装する。表示内容は日時・距離・時間。実機で一覧の表示と件数を確認
- [ ] バックアップ設定。backup_rules.xml を編集し、Room の DB ファイルを Android Auto Backup の対象に含める。allowBackup="true" が有効であることを確認
