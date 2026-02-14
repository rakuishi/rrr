# RRR: Run, Rise, Roar

走った距離と軌跡を GPS で記録する Android アプリ。

## 技術スタック

- Kotlin, Jetpack Compose, Material 3
- Google Maps SDK (Maps Compose) で地図描画
- Room で走行データを永続化
- Coroutines + Flow で非同期処理
- Min SDK 33 / Target SDK 35
- Gradle Kotlin DSL
- パッケージ: `com.rakuishi.rrr`

## アーキテクチャ

Single Activity 構成。MVVM で ViewModel が UiState を持ち、Room DAO → Repository → ViewModel の流れでデータを扱う。位置情報の取得は Foreground Service に委ねる。

## 画面構成

画面は一つ。Google Maps が全面に広がり、モードによって UI が切り替わる。

### 待機モード

- 現在地を中心に地図を表示
- 画面下中央の FAB（▶）をタップすると記録開始
- 過去の走行記録へのアクセス手段を設ける（見せ方は未定）

### 記録モード

- 走行ルートを Polyline でリアルタイム描画
- 地図上部に経過時間と走行距離をオーバーレイ
- FAB が停止ボタン（■）に変わる
- 停止タップ → 確認ダイアログ → 保存して待機モードへ

## データモデル

### activity

| カラム | 型 | 説明 |
|---|---|---|
| id | Long (PK, auto) | 活動 ID |
| total_time_ms | Long | 合計時間（ms） |
| total_distance_m | Double | 合計距離（m） |
| created_at | Long | 記録日時（epoch ms） |

### point

| カラム | 型 | 説明 |
|---|---|---|
| id | Long (PK, auto) | 座標 ID |
| activity_id | Long (FK) | 紐づく activity |
| latitude | Double | 緯度 |
| longitude | Double | 経度 |
| timestamp | Long | 取得時刻（epoch ms） |

## 位置情報

Foreground Service + `FusedLocationProviderClient` で GPS を取得する。記録中は通知を出し続ける。

権限: `ACCESS_FINE_LOCATION`, `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS`

## バックアップ

Room の DB ファイルを Android Auto Backup の対象に含める。`allowBackup="true"` と backup rules で制御。25MB 以内に収める。

## 後回し

- 音声コーチング（TTS で経過時間・距離を時間ベースで読み上げ）

## パッケージ構成

```
com.rakuishi.rrr/
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── Activity.kt
│   │   ├── Point.kt
│   │   ├── ActivityDao.kt
│   │   └── PointDao.kt
│   └── repository/
│       └── ActivityRepository.kt
├── service/
│   └── TrackingService.kt
├── ui/
│   ├── MainScreen.kt
│   ├── MainViewModel.kt
│   └── theme/
│       └── Theme.kt
└── App.kt
```

## ビルド

```bash
./gradlew assembleDebug   # デバッグビルド
./gradlew test            # ユニットテスト
./gradlew connectedCheck  # インストゥルメンテーションテスト
```

## コーディング規約

- Kotlin 公式規約に準拠
- State は ViewModel 内で `StateFlow<UiState>` として管理
- コメントは日本語可、コミットメッセージは英語
