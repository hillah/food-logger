# food-logger (AI 食事記録 & Health Connect ロガー)

食事の写真撮影・画像選択・テキスト入力から、**Google Gemini API** を活用して料理名・推定カロリー・PFC（タンパク質/脂質/炭水化物）・食塩相当量・食物繊維・ビタミン・ミネラル等の微量栄養素を高精度に解析し、Androidの **ヘルスコネクト（Health Connect）** に `NutritionRecord` として直接書き込むAndroidアプリです。

---

## 🌟 主な機能

1. **柔軟な食事入力**:
   - 📸 **カメラ撮影**: 目の前の食事を即座に撮影
   - 🖼️ **ギャラリー選択**: 撮影済みの写真から選択
   - ✍️ **ざっくりテキスト入力**: 「松屋のうまトマハンバーグ定食」「完全メシの汁なしヌードル」「居酒屋でビール2杯と焼き鳥」などの自然言語入力
2. **Gemini AI による高精度な栄養素解析**:
   - 食材・品目の分解（主食、主菜、副菜など）
   - カロリー、PFC（タンパク質・脂質・炭水化物）
   - 食塩相当量、食物繊維、糖質
   - 主要ミネラル（カルシウム、鉄分、亜鉛、マグネシウム、カリウム）
   - 主要ビタミン（ビタミンA, C, D, E, B群, 葉酸）
3. **直感的なプレビュー & 微調整 UI (Jetpack Compose)**:
   - PFC比率の動的ドーナツグラフ表示
   - 数値の手動微調整（カロリーや各グラム数の変更）
   - 朝食/昼食/夕食/間食の食事タイミング選択
4. **Health Connect への安全な書き込み**:
   - `NutritionRecord` として端末のHealth Connectに直接登録
   - 他のアプリ（`health-sync` 等）からスプレッドシートや外部分析へ自動連携可能

---

## 🛠️ 技術スタック & アーキテクチャ

- **言語**: Kotlin 2.1.0 (JVM 17)
- **UI**: Jetpack Compose (Material 3)
- **Health Connect**: `androidx.health.connect:connect-client:1.1.0-alpha11`
- **AI / LLM**: Google Generative AI SDK for Android (`com.google.ai.client.generativeai:generativeai`)
- **JSON解析**: `kotlinx.serialization`
- **画像処理**: Coil Compose
- **設定保存**: Jetpack DataStore Preferences
- **CI/CD**: GitHub Actions (Debug APK 自動ビルド & 成果物アップロード)

---

## 🚀 使い方 & セットアップ

### 1. Gemini API キーの取得
1. [Google AI Studio](https://aistudio.google.com/) にアクセスし、APIキーを発行します。
2. アプリ右上の **設定（⚙️）アイコン** をタップし、APIキーを入力して保存します。

### 2. Health Connect 権限の許可
初回起動時に Health Connect の書き込み権限（`androidx.health.permission.Nutrition.WRITE`）のリクエストが表示されます。「権限を許可する」をタップして許可してください。

### 3. 食事の記録
1. 写真を撮影・選択、またはテキストを入力します。
2. **「Gemini AI で栄養素を解析」** をタップします。
3. 解析結果・PFCバランスを確認し、必要に応じて数値を微調整します。
4. **「Health Connect に記録する」** をタップすると、即座に Health Connect へ登録されます。

---

## 📦 ビルド & インストール

### ローカルビルド (Android Studio / Gradle)
```bash
# Debug APK のビルド
./gradlew assembleDebug
```
生成先: `app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions による自動ビルド
GitHubリポジトリに `push` またはタグ（`v1.0.0` 等）を付けると、GitHub Actions上で自動的にAPKがビルドされ、Artifacts / Releasesからダウンロード可能です。
