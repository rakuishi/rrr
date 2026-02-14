# RRR: Run, Rise, Roar

走った距離と軌跡を GPS で記録する Android アプリ。

## 技術スタック

- Kotlin, Jetpack Compose, Material 3
- Google Maps SDK (Maps Compose) で地図描画
- Room で活動データを永続化
- Coroutines + Flow で非同期処理
- Min SDK 33 / Target SDK 36

## アーキテクチャ

Single Activity 構成。MVVM で ViewModel が UiState を持ち、Room DAO → Repository → ViewModel の流れでデータを扱う。位置情報の取得は Foreground Service に委ねる。

## 画面構成

一画面構成。Google Maps が全画面表示され、待機／記録モードにより UI が切り替わる。

### 待機モード

- 現在地を中心に地図を表示する
- 画面下中央の FAB（▶）をタップすると記録を開始する
- FAB の左にリストボタンを配置する。タップするとボトムシートが開き、過去のアクティビティを最新順に一覧表示する
- アクティビティをタップすると地図上に過去の軌跡を Polyline で描画する。表示中は FAB を非表示にし、リストボタンが ×（閉じる）に変わる
- 各アクティビティにはゴミ箱ボタンがあり、確認ダイアログを経て削除できる

### 記録モード

- 走行ルートを Polyline でリアルタイム描画する
- 地図上部に経過時間と走行距離をオーバーレイ表示する
- FAB が停止ボタン（■）に変わる
- 停止タップで記録を終了し、Activity の合計時間・距離を集計して保存する

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

Foreground Service + `FusedLocationProviderClient` で GPS を 3 秒間隔で取得する。記録中は通知を出し続ける。経過時間は 1 秒ごとに更新する。

権限: `ACCESS_FINE_LOCATION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`

## バックアップ

Room の DB ファイルを Android Auto Backup の対象に含める。`allowBackup="true"` と backup rules で制御する。

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
│   ├── ActivityBottomSheet.kt
│   ├── FormatUtils.kt
│   ├── MainScreen.kt
│   ├── MainViewModel.kt
│   ├── RecordingOverlay.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── MainActivity.kt
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
