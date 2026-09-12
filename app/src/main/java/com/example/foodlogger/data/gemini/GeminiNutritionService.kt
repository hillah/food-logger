package com.example.foodlogger.data.gemini

import android.graphics.Bitmap
import com.example.foodlogger.data.model.NutritionAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GeminiNutritionService {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val systemInstruction = """
        あなたはプロの管理栄養士かつ食事分析AIです。
        ユーザーから提供された食事の写真またはテキスト説明を分析し、料理名、推定ポーション、食材、推定カロリー、PFC（タンパク質・脂質・炭水化物）、各種微量栄養素（ビタミン・ミネラル）を高精度に推定・算出してください。
        
        【要件】
        1. 必ず以下のJSON形式のみを出力してください（説明文やマークダウンは含めず、純粋なJSON文字列を出力すること）。
        2. 写真のパッケージ、商品ロゴ、メニュー表記、バーコード、またはユーザーの入力テキストから、コンビニ・食品メーカー（例: セブンイレブン、ローソン、ファミリーマート、日清、明治等）や外食チェーン・レストラン（例: 松屋、吉野家、すき家、サイゼリヤ、スターバックス、マクドナルド、モスバーガー等）の具体的な製商品が正確に特定できる場合、メーカーやチェーンが公式に公表している栄養成分情報・公表値を最優先で適用し、カロリー・PFC・各種栄養素の数値を正確に更新・反映してください。
        3. ブランドや店舗が特定できない一般的な料理や自炊・手料理の場合は、日本食品標準成分表（八訂等）をベースに食材・調味料・調理法・推定ポーション（重量）から高精度に推定してください。
        4. 食塩相当量 (salt_equivalent_g) とナトリウム (sodium_mg) は矛盾のないように算出してください（食塩相当量[g] ≒ ナトリウム[mg] × 2.54 ÷ 1000）。
        5. 写真に複数品目（例: 主食、主菜、味噌汁、小鉢、サイドメニューなど）がある場合は dishes リストに内訳を分解してください。
        6. meal_type は "BREAKFAST", "LUNCH", "DINNER", "SNACK" のいずれかを選択してください。
        7. 公式公表値が特定できた場合は、notes にその商品名/ブランド名と公式情報に基づいている旨（例: 「セブンイレブン『〇〇』の公式栄養成分値に基づき算出しました」など）を簡潔に記載してください。
        
        JSONスキーマ:
        {
          "meal_name": "料理の総称・セット名 (例: 松屋 うまトマハンバーグ定食)",
          "meal_type": "LUNCH",
          "dishes": [
            {
              "name": "料理名・品目名",
              "estimated_portion": "推定分量 (例: 1人前, 200g)",
              "calories_kcal": 0.0
            }
          ],
          "nutrients": {
            "calories_kcal": 0.0,
            "protein_g": 0.0,
            "fat_g": 0.0,
            "carbohydrate_g": 0.0,
            "fiber_g": 0.0,
            "sugar_g": 0.0,
            "sodium_mg": 0.0,
            "salt_equivalent_g": 0.0,
            "potassium_mg": 0.0,
            "calcium_mg": 0.0,
            "iron_mg": 0.0,
            "zinc_mg": 0.0,
            "magnesium_mg": 0.0,
            "vitamin_a_mcg": 0.0,
            "vitamin_b1_mg": 0.0,
            "vitamin_b2_mg": 0.0,
            "vitamin_b6_mg": 0.0,
            "vitamin_b12_mcg": 0.0,
            "vitamin_c_mg": 0.0,
            "vitamin_d_mcg": 0.0,
            "vitamin_e_mg": 0.0,
            "folate_mcg": 0.0,
            "saturated_fat_g": 0.0,
            "trans_fat_g": 0.0,
            "cholesterol_mg": 0.0
          },
          "notes": "特記事項や栄養アドバイス (例: 野菜が豊富でビタミンCが充実しています / セブンイレブン公式栄養成分値を参照)"
        }
    """.trimIndent()

    suspend fun analyzeMeal(
        apiKey: String,
        modelName: String,
        promptText: String,
        bitmaps: List<Bitmap> = emptyList()
    ): Result<NutritionAnalysisResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini APIキーが設定されていません。右上の設定アイコン（⚙️）からAPIキーを入力してください。"))
        }

        val targetModel = modelName.trim().removePrefix("models/").ifBlank { "gemini-flash-lite-latest" }
        val endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=${apiKey.trim()}"
        android.util.Log.d("FoodLogger", "=== Starting Gemini API Request ===")
        android.util.Log.d("FoodLogger", "Target Model: $targetModel")
        android.util.Log.d("FoodLogger", "Image count: ${bitmaps.size}, Prompt text length: ${promptText.length}")

        try {
            val inputPrompt = if (promptText.isNotBlank()) {
                "この食事の栄養素を分析してください: $promptText"
            } else {
                "この食事写真の栄養素を詳細に分析してください。"
            }

            // Construct JSON Payload for Gemini REST API
            val partsJsonArray = mutableListOf<String>()
            
            // Add inline_data (base64) for all bitmaps
            bitmaps.forEach { bitmap ->
                val stream = java.io.ByteArrayOutputStream()
                // Compress bitmap to JPEG with quality 85 to keep request size optimal
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                val base64Image = android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
                partsJsonArray.add("""{"inline_data":{"mime_type":"image/jpeg","data":"$base64Image"}}""")
            }

            // Add text part
            val escapedPrompt = escapeJsonString(inputPrompt)
            partsJsonArray.add("""{"text":"$escapedPrompt"}""")

            val escapedSystemInstruction = escapeJsonString(systemInstruction)

            val requestBodyJson = """
            {
              "system_instruction": {
                "parts": [
                  {"text": "$escapedSystemInstruction"}
                ]
              },
              "contents": [
                {
                  "role": "user",
                  "parts": [
                    ${partsJsonArray.joinToString(",")}
                  ]
                }
              ],
              "generationConfig": {
                "response_mime_type": "application/json",
                "temperature": 0.2
              }
            }
            """.trimIndent()

            val url = java.net.URL(endpointUrl)
            val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                doInput = true
                connectTimeout = 30000
                readTimeout = 60000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }

            connection.outputStream.use { os ->
                os.write(requestBodyJson.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                val errorStream = connection.errorStream ?: connection.inputStream
                errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "HTTP $responseCode"
            }

            android.util.Log.d("FoodLogger", "Gemini HTTP Response Code: $responseCode")
            android.util.Log.d("FoodLogger", "Gemini Response Body: $responseBody")

            if (responseCode !in 200..299) {
                val parsedErrorMessage = parseGoogleApiError(responseCode, responseBody, targetModel)
                return@withContext Result.failure(Exception(parsedErrorMessage))
            }

            // Extract candidate text from response JSON
            val textContent = extractGeneratedTextFromApiResponse(responseBody)
                ?: return@withContext Result.failure(IllegalStateException("APIからの応答に生成テキストが含まれていませんでした。\nレスポンス: $responseBody"))

            val cleanJson = extractJson(textContent)
            val result = jsonParser.decodeFromString<NutritionAnalysisResult>(cleanJson)
            Result.success(result)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "通信に失敗しました"
            Result.failure(Exception("Gemini解析エラー: $msg", e))
        }
    }

    private fun escapeJsonString(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun extractGeneratedTextFromApiResponse(responseJson: String): String? {
        try {
            // Fast regex extraction of candidate text from {"candidates": [{"content": {"parts": [{"text": "..."}]}}]}
            val textRegex = Regex(""""text"\s*:\s*"((?:[^"\\]|\\.)*)"""")
            val matches = textRegex.findAll(responseJson).toList()
            if (matches.isNotEmpty()) {
                // Usually the main output text is in the candidates
                val rawText = matches.last().groupValues[1]
                return unescapeJsonString(rawText)
            }
        } catch (_: Exception) {}
        return null
    }

    private fun unescapeJsonString(input: String): String {
        return input
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    private fun parseGoogleApiError(code: Int, body: String, model: String): String {
        return when (code) {
            404 -> {
                "【404 Not Found】モデル「$model」が見つからないか、エンドポイントが利用できません。\n" +
                "Google AI Studio (https://aistudio.google.com/) で発行したAPIキーであること、および「gemini-1.5-flash」または「gemini-2.0-flash」が有効か確認してください。\n" +
                "API詳細: $body"
            }
            400 -> {
                "【400 Bad Request】リクエストの形式に問題があります。\n詳細: $body"
            }
            403 -> {
                "【403 Forbidden】APIキーが無効、またはGenerative Language APIのアクセス権限がありません。\n" +
                "Google AI Studioで新しいAPIキーを作成してお試しください。\n詳細: $body"
            }
            429 -> {
                "【429 Too Many Requests】APIの利用枠（クォータ）を超過しました。しばらく待ってから再試行してください。\n詳細: $body"
            }
            else -> {
                "【HTTP $code エラー】\n$body"
            }
        }
    }

    private fun extractJson(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed
        }
        val regex = Regex("""```json\s*([\s\S]*?)\s*```""")
        val match = regex.find(trimmed)
        if (match != null) {
            return match.groupValues[1].trim()
        }
        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1)
        }
        return trimmed
    }
}
